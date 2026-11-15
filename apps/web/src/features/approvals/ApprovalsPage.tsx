import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { approvalApi } from '@/lib/governanceApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  CheckSquare,
  Search,
  Filter,
  RefreshCw,
  CheckCircle,
  XCircle,
  RotateCcw,
  ChevronRight,
} from 'lucide-react';
import type { Round } from '@/types/governance';

export const ApprovalsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedRound, setSelectedRound] = useState<Round | null>(null);
  const [approvalNotes, setApprovalNotes] = useState('');

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['rounds-list'],
    queryFn: () => approvalApi.listRounds(),
  });

  const decideMutation = useMutation({
    mutationFn: ({
      roundId,
      action,
      notes,
    }: {
      roundId: string;
      action: 'APPROVE' | 'REJECT' | 'REQUEST_CHANGES';
      notes: string;
    }) => approvalApi.decide(roundId, action, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rounds-list'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-metrics'] });
      setSelectedRound(null);
      setApprovalNotes('');
    },
    onError: (err: unknown) => {
      const error = err as { message?: string };
      alert(`Không thể thực hiện thẩm định: ${error.message || 'Lỗi không xác định'}`);
    },
  });

  const rounds = data?.items || [];

  const filteredRounds = rounds.filter((r) => {
    const matchSearch =
      r.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.targetId?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || r.state === statusFilter;
    return matchSearch && matchStatus;
  });

  const getRoundBadge = (state: string) => {
    switch (state) {
      case 'OPEN':
        return <Badge variant="indigo">Đang chờ duyệt (OPEN)</Badge>;
      case 'APPROVED':
        return <Badge variant="emerald">Đã chấp thuận (APPROVED)</Badge>;
      case 'REJECTED':
        return <Badge variant="rose">Từ chối (REJECTED)</Badge>;
      case 'EXPIRED':
        return <Badge variant="slate">Đã hết hạn (EXPIRED)</Badge>;
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
            <CheckSquare className="w-5 h-5 text-indigo-500" />
            <span>Quy trình Phê duyệt (Approval Rounds)</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Thẩm định hồ sơ L1/L2 theo nguyên tắc 4 mắt (Checker độc lập) trước khi phát hành hợp đồng.
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
            placeholder="Tìm theo Mã vòng duyệt, Mã đối tượng..."
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
            <option value="OPEN">OPEN (Đang mở)</option>
            <option value="APPROVED">APPROVED (Đã duyệt)</option>
            <option value="REJECTED">REJECTED (Từ chối)</option>
            <option value="EXPIRED">EXPIRED (Hết hạn)</option>
          </select>
        </div>
      </div>

      {/* Rounds Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/75 dark:bg-slate-800/60 uppercase font-bold text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="py-3 px-4">Mã Vòng duyệt</th>
                <th className="py-3 px-4">Đối tượng</th>
                <th className="py-3 px-4">Mã Đối tượng</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4">Hạn hoàn tất</th>
                <th className="py-3 px-4">Các bước duyệt</th>
                <th className="py-3 px-4 text-right">Thẩm định</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang nạp danh sách vòng phê duyệt...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredRounds.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Hiện chưa có vòng phê duyệt nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Khi hồ sơ thu hộ (Dossier) được nộp (`SUBMIT`), vòng thẩm định L1/L2 sẽ tự động được mở tại đây.
                    </p>
                  </td>
                </tr>
              ) : (
                filteredRounds.map((r) => (
                  <tr
                    key={r.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="py-3.5 px-4 font-mono font-semibold text-indigo-400">
                      {r.id.slice(0, 8)}...
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="font-semibold text-slate-200">{r.targetKind}</span>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      {r.targetId.slice(0, 8)}...
                    </td>
                    <td className="py-3.5 px-4">{getRoundBadge(r.state)}</td>
                    <td className="py-3.5 px-4 text-slate-300">
                      {r.dueAt ? new Date(r.dueAt).toLocaleDateString('vi-VN') : '--'}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-mono">
                        {r.steps?.length || 0} bước
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedRound(r)}
                        className="text-xs gap-1"
                      >
                        <span>Xử lý</span>
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

      {/* Decision Modal */}
      {selectedRound && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full p-6 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <CheckSquare className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Thẩm định Vòng duyệt</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedRound(null)}>
                Đóng
              </Button>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã vòng duyệt:</span>
                <span className="font-mono text-indigo-300 select-all">{selectedRound.id}</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Đối tượng:</span>
                <span className="font-semibold text-slate-200">
                  {selectedRound.targetKind} ({selectedRound.targetId.slice(0, 8)}...)
                </span>
              </div>
            </div>

            {/* Note input */}
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                Ghi chú thẩm định / Ý kiến xét duyệt:
              </label>
              <textarea
                value={approvalNotes}
                onChange={(e) => setApprovalNotes(e.target.value)}
                placeholder="Nhập lý do chấp thuận, từ chối hoặc yêu cầu bổ sung..."
                rows={3}
                className="w-full text-xs bg-slate-950 border border-slate-800 rounded-xl p-3 text-slate-200 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>

            {selectedRound.state === 'OPEN' && (
              <div className="grid grid-cols-3 gap-2.5 pt-2">
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() =>
                    decideMutation.mutate({
                      roundId: selectedRound.id,
                      action: 'APPROVE',
                      notes: approvalNotes,
                    })
                  }
                  isLoading={decideMutation.isPending}
                  className="bg-emerald-600 hover:bg-emerald-700 gap-1.5"
                >
                  <CheckCircle className="w-4 h-4" />
                  <span>Duyệt hồ sơ</span>
                </Button>

                <Button
                  variant="outline"
                  size="sm"
                  onClick={() =>
                    decideMutation.mutate({
                      roundId: selectedRound.id,
                      action: 'REQUEST_CHANGES',
                      notes: approvalNotes,
                    })
                  }
                  isLoading={decideMutation.isPending}
                  className="border-amber-600/50 text-amber-400 hover:bg-amber-950/30 gap-1.5"
                >
                  <RotateCcw className="w-4 h-4" />
                  <span>Yêu cầu sửa</span>
                </Button>

                <Button
                  variant="danger"
                  size="sm"
                  onClick={() =>
                    decideMutation.mutate({
                      roundId: selectedRound.id,
                      action: 'REJECT',
                      notes: approvalNotes,
                    })
                  }
                  isLoading={decideMutation.isPending}
                  className="gap-1.5"
                >
                  <XCircle className="w-4 h-4" />
                  <span>Từ chối</span>
                </Button>
              </div>
            )}

            <div className="flex justify-end pt-4 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setSelectedRound(null)}>
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
