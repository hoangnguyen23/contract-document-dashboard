import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { noticeApi } from '@/lib/governanceApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { ActionButton } from '@/components/common/ActionGuard';
import { useToast } from '@/context/ToastContext';
import {
  Bell,
  Search,
  Filter,
  RefreshCw,
  Send,
  FileCheck,
  CheckCircle2,
  XCircle,
  ChevronRight,
  Inbox,
  ShieldCheck,
} from 'lucide-react';
import type { Notice } from '@/types/governance';

export const NoticesPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { success, showApiError } = useToast();

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedNotice, setSelectedNotice] = useState<Notice | null>(null);

  // Modals
  const [isCaptureOpen, setIsCaptureOpen] = useState(false);
  const [isAckModalOpen, setIsAckModalOpen] = useState(false);
  const [isVerifyAckModalOpen, setIsVerifyAckModalOpen] = useState(false);
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);

  // Action fields
  const [ackBasis, setAckBasis] = useState('Biên nhận xác nhận thông báo từ doanh nghiệp');
  const [verifyDecision, setVerifyDecision] = useState<'ACCEPT' | 'RETURN'>('ACCEPT');
  const [verifyReason, setVerifyReason] = useState('Đã đối soát hợp lệ bằng chứng ký nhận');
  const [cancelReason, setCancelReason] = useState('');

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['notices-list'],
    queryFn: () => noticeApi.list(),
  });

  // Query local mailbox capture
  const { data: captureData, isLoading: captureLoading } = useQuery({
    queryKey: ['notice-capture', selectedNotice?.id],
    queryFn: () => noticeApi.getCapture(selectedNotice!.id),
    enabled: isCaptureOpen && !!selectedNotice,
  });

  // Mutations
  const dispatchMutation = useMutation({
    mutationFn: (id: string) => noticeApi.dispatch(id),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['notices-list'] });
      setSelectedNotice(updated);
      success('Chuyển phát thông báo thành công', 'Thông báo đã vào hàng đợi LOCAL_CAPTURE');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const recordAckMutation = useMutation({
    mutationFn: () => {
      if (!selectedNotice) throw new Error('Chưa chọn thông báo');
      const nowIso = new Date().toISOString().replace(/\.\d{3}Z$/, '.000Z');
      return noticeApi.recordAck(selectedNotice.id, {
        artifactHash: selectedNotice.artifactHash || '0000000000000000000000000000000000000000000000000000000000000000',
        recipientSnapshotHash: selectedNotice.recipientSnapshotHash || '0000000000000000000000000000000000000000000000000000000000000000',
        acknowledgedAt: nowIso,
        evidenceFileIds: ['00000000-0000-4000-8000-000000000001'],
        basis: ackBasis.trim() || 'Xác nhận hợp lệ từ khách hàng',
      });
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['notices-list'] });
      setIsAckModalOpen(false);
      setSelectedNotice(updated);
      success('Ghi nhận biên nhận thành công', 'Bản ghi đang chờ Kiểm soát viên thẩm tra (VERIFY_ACK).');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const verifyAckMutation = useMutation({
    mutationFn: () => {
      if (!selectedNotice) throw new Error('Chưa chọn thông báo');
      return noticeApi.verifyAck(
        selectedNotice.id,
        '00000000-0000-4000-8000-000000000001',
        verifyDecision,
        verifyReason.trim()
      );
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['notices-list'] });
      setIsVerifyAckModalOpen(false);
      setSelectedNotice(updated);
      success('Thẩm định biên nhận thành công', `Trạng thái thông báo: ${updated.state}`);
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const cancelMutation = useMutation({
    mutationFn: () => {
      if (!selectedNotice) throw new Error('Chưa chọn thông báo');
      return noticeApi.cancel(selectedNotice.id, cancelReason.trim() || 'Hủy thông báo');
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['notices-list'] });
      setIsCancelModalOpen(false);
      setSelectedNotice(updated);
      success('Hủy thông báo thành công', 'Thông báo đã chuyển sang CANCELLED');
    },
    onError: (err) => showApiError(err, () => refetch()),
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
      case 'DRAFT':
        return <Badge variant="slate">Bản nháp (DRAFT)</Badge>;
      case 'IN_REVIEW':
        return <Badge variant="indigo">Đang duyệt (IN_REVIEW)</Badge>;
      case 'APPROVED':
        return <Badge variant="emerald">Đã duyệt (APPROVED)</Badge>;
      case 'QUEUED':
        return <Badge variant="amber">Hàng đợi gửi (QUEUED)</Badge>;
      case 'DISPATCHED':
        return <Badge variant="sky">Đã chuyển phát (DISPATCHED)</Badge>;
      case 'ACKNOWLEDGED':
        return <Badge variant="emerald">Đã ký nhận (ACKNOWLEDGED)</Badge>;
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
            placeholder="Tìm theo Tiêu đề, Mã thông báo..."
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
            <option value="DRAFT">Bản nháp (DRAFT)</option>
            <option value="IN_REVIEW">Đang duyệt (IN_REVIEW)</option>
            <option value="APPROVED">Đã duyệt (APPROVED)</option>
            <option value="DISPATCHED">Đã chuyển phát (DISPATCHED)</option>
            <option value="ACKNOWLEDGED">Đã ký nhận (ACKNOWLEDGED)</option>
            <option value="CANCELLED">Đã hủy (CANCELLED)</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/75 dark:bg-slate-800/60 uppercase font-bold text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="py-3 px-4">Mã Thông báo</th>
                <th className="py-3 px-4">Tiêu đề thông báo</th>
                <th className="py-3 px-4">Mục đích</th>
                <th className="py-3 px-4">Phương thức</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4">Ký nhận (ACK)</th>
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
                      Các thông báo biểu phí, thanh lý và sự kiện hợp đồng sẽ hiển thị tại đây.
                    </p>
                  </td>
                </tr>
              ) : (
                filteredNotices.map((n) => (
                  <tr
                    key={n.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="py-3.5 px-4 font-mono font-semibold text-indigo-400">
                      {n.id.slice(0, 8)}...
                    </td>
                    <td className="py-3.5 px-4 font-semibold text-slate-200 max-w-xs truncate">
                      {n.subject}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-semibold">
                        {n.purpose}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400 text-[11px]">
                      {n.transport}
                    </td>
                    <td className="py-3.5 px-4">{getNoticeBadge(n.state)}</td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {n.acknowledgedAt ? (
                        <span className="text-emerald-400 font-semibold flex items-center gap-1">
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          <span>{new Date(n.acknowledgedAt).toLocaleDateString('vi-VN')}</span>
                        </span>
                      ) : (
                        <span className="text-slate-500">Chưa ký nhận</span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedNotice(n)}
                        className="text-xs gap-1"
                      >
                        <span>Chi tiết</span>
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

      {/* ─── Detail Modal (Full Notice Workflow - Item 6.6) ──────────────────── */}
      {selectedNotice && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6 space-y-6 shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Bell className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Chi tiết Thông báo Văn bản</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedNotice(null)}>
                Đóng
              </Button>
            </div>

            {/* Overview */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã thông báo:</span>
                <span className="font-mono text-indigo-300 font-semibold">{selectedNotice.id.slice(0, 12)}...</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Trạng thái:</span>
                {getNoticeBadge(selectedNotice.state)}
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Kênh & Phương thức:</span>
                <span className="font-mono text-slate-300">{selectedNotice.channel} · {selectedNotice.transport}</span>
              </div>
            </div>

            {/* Subject and Body */}
            <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800 space-y-2 text-xs">
              <span className="text-slate-500 text-[11px] block">Tiêu đề:</span>
              <p className="font-bold text-white text-sm">{selectedNotice.subject}</p>
              <span className="text-slate-500 text-[11px] block pt-2 border-t border-slate-800">Nội dung văn bản:</span>
              <p className="text-slate-300 whitespace-pre-wrap leading-relaxed">{selectedNotice.bodyText}</p>
            </div>

            {/* Recipient */}
            <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800 space-y-1.5 text-xs">
              <span className="font-semibold text-slate-300 uppercase tracking-wider text-[11px] block">
                Người nhận thông báo
              </span>
              <div className="grid grid-cols-2 gap-2 text-slate-300 text-[11px]">
                <div>Họ tên: <span className="font-semibold text-white">{selectedNotice.recipient.personName}</span></div>
                <div>Chức danh: <span>{selectedNotice.recipient.roleLabel}</span></div>
                <div>Email: <span className="font-mono">{selectedNotice.recipient.email}</span></div>
                <div>Điện thoại: <span className="font-mono">{selectedNotice.recipient.phone}</span></div>
              </div>
            </div>

            {/* Mailbox Capture Button */}
            {selectedNotice.transport === 'LOCAL_CAPTURE' && (
              <div className="p-3.5 bg-indigo-950/40 border border-indigo-500/30 rounded-xl flex items-center justify-between">
                <div>
                  <p className="font-bold text-indigo-300 text-xs flex items-center gap-1.5">
                    <Inbox className="w-4 h-4" />
                    <span>Hộp thư cục bộ (Local Mailbox Capture)</span>
                  </p>
                  <p className="text-slate-400 text-[11px] mt-0.5">
                    Kiểm tra messageId và bức thư mô phỏng không gửi ra mạng bên ngoài.
                  </p>
                </div>
                <Button size="sm" variant="outline" onClick={() => setIsCaptureOpen(true)} className="gap-1.5 text-xs">
                  <span>Mở hộp thư</span>
                </Button>
              </div>
            )}

            {/* Server-governed Action Toolbar */}
            <div className="p-4 rounded-xl bg-slate-950/80 border border-indigo-500/20 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-200 uppercase tracking-wider">
                  Thao tác Quy trình Thông báo (B §6)
                </span>
                <span className="text-[11px] text-slate-500">
                  Căn cứ vào allowedActions
                </span>
              </div>

              <div className="flex flex-wrap items-center gap-2.5 pt-2 border-t border-slate-800">
                {/* DISPATCH Button */}
                <ActionButton
                  action="DISPATCH"
                  allowedActions={selectedNotice.allowedActions}
                  blockedActions={selectedNotice.blockedActions}
                  isLoading={dispatchMutation.isPending}
                  onClick={() => dispatchMutation.mutate(selectedNotice.id)}
                  className="gap-1.5"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>Chuyển phát (DISPATCH)</span>
                </ActionButton>

                {/* RECORD_ACK Button */}
                <ActionButton
                  action="RECORD_ACK"
                  allowedActions={selectedNotice.allowedActions}
                  blockedActions={selectedNotice.blockedActions}
                  onClick={() => setIsAckModalOpen(true)}
                  className="gap-1.5"
                >
                  <FileCheck className="w-3.5 h-3.5" />
                  <span>Ghi nhận Ký nhận (RECORD_ACK)</span>
                </ActionButton>

                {/* VERIFY_ACK Button */}
                <ActionButton
                  action="VERIFY_ACK"
                  allowedActions={selectedNotice.allowedActions}
                  blockedActions={selectedNotice.blockedActions}
                  onClick={() => setIsVerifyAckModalOpen(true)}
                  className="gap-1.5"
                >
                  <ShieldCheck className="w-3.5 h-3.5" />
                  <span>Thẩm tra Ký nhận (VERIFY_ACK)</span>
                </ActionButton>

                {/* CANCEL Button */}
                <ActionButton
                  action="CANCEL"
                  allowedActions={selectedNotice.allowedActions}
                  blockedActions={selectedNotice.blockedActions}
                  variant="danger"
                  onClick={() => setIsCancelModalOpen(true)}
                  className="gap-1.5"
                >
                  <XCircle className="w-3.5 h-3.5" />
                  <span>Hủy thông báo (CANCEL)</span>
                </ActionButton>
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setSelectedNotice(null)}>
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Local Mailbox Capture Modal ────────────────────────────────────── */}
      {isCaptureOpen && (
        <div className="fixed inset-0 bg-black/85 backdrop-blur-md z-[80] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h3 className="font-bold text-base text-white flex items-center gap-2">
                <Inbox className="w-5 h-5 text-indigo-400" />
                <span>Hộp thư giả lập (Local Mailbox Envelope)</span>
              </h3>
              <Button variant="ghost" size="sm" onClick={() => setIsCaptureOpen(false)}>
                Đóng
              </Button>
            </div>

            {captureLoading ? (
              <div className="py-8 flex flex-col items-center justify-center gap-2 text-slate-400 text-xs">
                <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                <span>Đang truy vấn hộp thư cục bộ...</span>
              </div>
            ) : !captureData ? (
              <div className="py-6 text-center text-xs text-slate-400">
                Chưa có dữ liệu chuyển phát trong hộp thư cục bộ.
              </div>
            ) : (
              <div className="space-y-3 text-xs">
                <div className="p-3 bg-slate-950/70 border border-slate-800 rounded-xl space-y-1.5 font-mono text-[11px]">
                  <div>Message ID: <span className="text-indigo-300 font-semibold">{captureData.messageId}</span></div>
                  <div>Thời điểm nhận: <span className="text-slate-300">{new Date(captureData.acceptedAt).toLocaleString('vi-VN')}</span></div>
                  <div>Artifact Hash: <span className="text-emerald-400 break-all">{captureData.artifactHash}</span></div>
                </div>

                <div className="p-3 bg-slate-950/70 border border-slate-800 rounded-xl space-y-1.5">
                  <span className="font-semibold text-slate-300 block text-[11px] uppercase tracking-wider">Nội dung thư</span>
                  <p className="font-bold text-white text-xs">{captureData.subject}</p>
                  <p className="text-slate-300 whitespace-pre-wrap text-[11px]">{captureData.bodyText}</p>
                </div>
              </div>
            )}

            <div className="flex justify-end pt-3 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setIsCaptureOpen(false)}>
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Record ACK Modal ──────────────────────────────────────────────── */}
      {isAckModalOpen && selectedNotice && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-[70] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-white flex items-center gap-2">
              <FileCheck className="w-5 h-5 text-indigo-400" />
              <span>Ghi nhận Ký nhận Văn bản (RECORD_ACK)</span>
            </h3>
            <p className="text-xs text-slate-400">
              Cán bộ phụ trách ghi nhận biên nhận bàn giao hoặc phản hồi đồng ý của doanh nghiệp.
            </p>
            <Input
              label="Cơ sở biên nhận / Bằng chứng *"
              value={ackBasis}
              onChange={(e) => setAckBasis(e.target.value)}
            />
            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsAckModalOpen(false)}>
                Hủy bỏ
              </Button>
              <Button
                size="sm"
                isLoading={recordAckMutation.isPending}
                disabled={!ackBasis.trim()}
                onClick={() => recordAckMutation.mutate()}
              >
                Ghi nhận ACK
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Verify ACK Modal ──────────────────────────────────────────────── */}
      {isVerifyAckModalOpen && selectedNotice && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-[70] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-white flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-emerald-400" />
              <span>Thẩm tra Ký nhận (VERIFY_ACK - Checker)</span>
            </h3>
            <div className="space-y-3 text-xs">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Quyết định thẩm tra *
                </label>
                <select
                  value={verifyDecision}
                  onChange={(e) => setVerifyDecision(e.target.value as 'ACCEPT' | 'RETURN')}
                  className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-slate-200"
                >
                  <option value="ACCEPT">Chấp nhận biên nhận (ACCEPT → ACKNOWLEDGED)</option>
                  <option value="RETURN">Trả lại yêu cầu làm rõ (RETURN)</option>
                </select>
              </div>
              <Input
                label="Lý do thẩm tra *"
                value={verifyReason}
                onChange={(e) => setVerifyReason(e.target.value)}
              />
            </div>
            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsVerifyAckModalOpen(false)}>
                Hủy bỏ
              </Button>
              <Button
                size="sm"
                isLoading={verifyAckMutation.isPending}
                disabled={!verifyReason.trim()}
                onClick={() => verifyAckMutation.mutate()}
              >
                Xác nhận thẩm tra
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Cancel Notice Modal ───────────────────────────────────────────── */}
      {isCancelModalOpen && selectedNotice && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-[70] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-rose-400 flex items-center gap-2">
              <XCircle className="w-5 h-5" />
              <span>Hủy bỏ thông báo</span>
            </h3>
            <Input
              label="Lý do hủy bỏ *"
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
            />
            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsCancelModalOpen(false)}>
                Hủy bỏ
              </Button>
              <Button
                variant="danger"
                size="sm"
                isLoading={cancelMutation.isPending}
                disabled={!cancelReason.trim()}
                onClick={() => cancelMutation.mutate()}
              >
                Hủy thông báo
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
