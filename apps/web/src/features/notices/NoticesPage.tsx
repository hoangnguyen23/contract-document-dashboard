import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { noticeApi } from '@/lib/governanceApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  Bell,
  Search,
  Filter,
  RefreshCw,
  CheckCircle2,
  Mail,
  ChevronRight,
} from 'lucide-react';
import type { Notice } from '@/types/governance';

export const NoticesPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedNotice, setSelectedNotice] = useState<Notice | null>(null);

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['notices-list'],
    queryFn: () => noticeApi.list(),
  });

  const ackMutation = useMutation({
    mutationFn: (id: string) => noticeApi.acknowledge(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notices-list'] });
      setSelectedNotice(null);
    },
    onError: (err: unknown) => {
      const error = err as { message?: string };
      alert(`Không thể xác nhận văn bản: ${error.message || 'Lỗi không xác định'}`);
    },
  });

  const notices = data?.items || [];

  const filteredNotices = notices.filter((n) => {
    const matchSearch =
      n.subject?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      n.id.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || n.state === statusFilter;
    return matchSearch && matchStatus;
  });

  const getNoticeBadge = (state: string) => {
    switch (state) {
      case 'DELIVERED':
        return <Badge variant="indigo">Đã gửi đến (DELIVERED)</Badge>;
      case 'ACKNOWLEDGED':
        return <Badge variant="emerald">Đã ký nhận (ACKNOWLEDGED)</Badge>;
      case 'DISPATCHED':
        return <Badge variant="sky">Đang chuyển phát (DISPATCHED)</Badge>;
      case 'FAILED':
        return <Badge variant="rose">Gửi thất bại (FAILED)</Badge>;
      case 'CANCELLED':
        return <Badge variant="slate">Đã hủy (CANCELLED)</Badge>;
      default:
        return <Badge variant="slate">{state}</Badge>;
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Bell className="w-5 h-5 text-indigo-500" />
            <span>Thông báo & Giao nhận Văn bản (Notices)</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Quản lý thông báo hợp đồng, lưu vết gửi và biên nhận giao nhận văn bản pháp lý.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            disabled={isFetching}
            className="border-slate-700 text-slate-300 hover:text-white hover:bg-slate-800"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin text-indigo-400' : ''}`} />
            <span>Làm mới</span>
          </Button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo Tiêu đề, Mã văn bản..."
            className="w-full pl-9 pr-4 py-2 text-xs bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-400 shrink-0" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="text-xs bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl px-3 py-2 text-slate-700 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="DELIVERED">DELIVERED (Đã nhận)</option>
            <option value="ACKNOWLEDGED">ACKNOWLEDGED (Đã xác nhận)</option>
            <option value="DISPATCHED">DISPATCHED (Đang gửi)</option>
            <option value="FAILED">FAILED (Thất bại)</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/75 dark:bg-slate-800/60 uppercase font-bold text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="py-3 px-4">Tiêu đề thông báo</th>
                <th className="py-3 px-4">Mục đích</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4">Kênh gửi</th>
                <th className="py-3 px-4">Hợp đồng liên quan</th>
                <th className="py-3 px-4">Ngày gửi</th>
                <th className="py-3 px-4 text-right">Chi tiết</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang nạp danh sách thông báo...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredNotices.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có thông báo nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Các thông báo chính thức và nhắc hạn nghĩa vụ sẽ hiển thị tại đây khi hệ thống phát sinh sự kiện.
                    </p>
                  </td>
                </tr>
              ) : (
                filteredNotices.map((n) => (
                  <tr
                    key={n.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="py-3.5 px-4 font-semibold text-slate-200 flex items-center gap-2">
                      <Mail className="w-4 h-4 text-indigo-400 shrink-0" />
                      <span>{n.subject || 'Thông báo hợp đồng'}</span>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-semibold text-slate-300">
                        {n.purpose}
                      </span>
                    </td>
                    <td className="py-3.5 px-4">{getNoticeBadge(n.state)}</td>
                    <td className="py-3.5 px-4 font-mono text-slate-300">{n.channel}</td>
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      {n.contractId ? `${n.contractId.slice(0, 8)}...` : '--'}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {n.createdAt ? new Date(n.createdAt).toLocaleDateString('vi-VN') : '--'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedNotice(n)}
                        className="text-xs gap-1"
                      >
                        <span>Xem</span>
                        <ChevronRight className="w-3.5 h-3.5" />
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Detail Modal */}
      {selectedNotice && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full p-6 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Bell className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Nội dung Thông báo</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedNotice(null)}>
                Đóng
              </Button>
            </div>

            <div className="space-y-4 text-xs">
              <div>
                <span className="text-slate-500 block mb-1">Tiêu đề:</span>
                <p className="font-bold text-sm text-slate-100">{selectedNotice.subject}</p>
              </div>

              <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Nội dung văn bản:</span>
                <p className="text-slate-300 whitespace-pre-wrap leading-relaxed">
                  {selectedNotice.bodyText || 'Không có nội dung mô tả thêm.'}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                  <span className="text-slate-500 block mb-1">Trạng thái gửi:</span>
                  {getNoticeBadge(selectedNotice.state)}
                </div>
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                  <span className="text-slate-500 block mb-1">Kênh giao nhận:</span>
                  <span className="font-semibold text-slate-200">{selectedNotice.channel}</span>
                </div>
              </div>
            </div>

            {selectedNotice.state === 'DELIVERED' && (
              <div className="p-3.5 rounded-xl bg-indigo-950/40 border border-indigo-500/20 flex items-center justify-between text-xs">
                <span className="text-slate-300">Xác nhận đã nhận và đối chiếu văn bản:</span>
                <Button
                  size="sm"
                  onClick={() => ackMutation.mutate(selectedNotice.id)}
                  isLoading={ackMutation.isPending}
                  className="gap-1.5"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Ký nhận (ACK)</span>
                </Button>
              </div>
            )}

            <div className="flex justify-end pt-4 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setSelectedNotice(null)}>
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
