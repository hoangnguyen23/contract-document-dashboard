import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { approvalApi } from '@/lib/governanceApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { ActionButton } from '@/components/common/ActionGuard';
import { useToast } from '@/context/ToastContext';
import {
  CheckSquare,
  Search,
  Filter,
  RefreshCw,
  CheckCircle,
  XCircle,
  RotateCcw,
  ChevronRight,
  Eye,
  ShieldCheck,
  Hash,
} from 'lucide-react';
import type { Round, ApprovalStep, EvidenceCheck } from '@/types/governance';

export const ApprovalsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { success, showApiError } = useToast();

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedRound, setSelectedRound] = useState<Round | null>(null);

  // Snapshot modal
  const [isSnapshotOpen, setIsSnapshotOpen] = useState(false);
  const [decisionReason, setDecisionReason] = useState('');

  // Modals for actions
  const [isDecisionModalOpen, setIsDecisionModalOpen] = useState(false);
  const [decisionAction, setDecisionAction] = useState<'APPROVE' | 'RETURN' | 'REJECT'>('APPROVE');
  const [decisionLevel, setDecisionLevel] = useState<'L1' | 'L2'>('L1');

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['rounds-list'],
    queryFn: () => approvalApi.listRounds(),
  });

  // Query snapshot when requested
  const { data: snapshotData, isLoading: snapshotLoading } = useQuery({
    queryKey: ['round-snapshot', selectedRound?.id],
    queryFn: () => approvalApi.getRoundSnapshot(selectedRound!.id),
    enabled: isSnapshotOpen && !!selectedRound,
  });

  // Decide mutation (APPROVE / RETURN / REJECT)
  const decideMutation = useMutation({
    mutationFn: () => {
      if (!selectedRound) throw new Error('Chưa chọn vòng thẩm định');
      if (!decisionReason.trim()) throw new Error('Vui lòng nhập lý do quyết định');

      if (decisionAction === 'APPROVE') {
        return approvalApi.approve(selectedRound.id, decisionLevel, decisionReason.trim());
      } else if (decisionAction === 'RETURN') {
        return approvalApi.returnRound(selectedRound.id, decisionLevel, decisionReason.trim());
      } else {
        return approvalApi.reject(selectedRound.id, decisionLevel, decisionReason.trim());
      }
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['rounds-list'] });
      queryClient.invalidateQueries({ queryKey: ['dossiers-list'] });
      queryClient.invalidateQueries({ queryKey: ['contracts-list'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-metrics'] });
      setIsDecisionModalOpen(false);
      setSelectedRound(updated);
      setDecisionReason('');
      success('Thực hiện thẩm định thành công', `Trạng thái vòng duyệt: ${updated.state}`);
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  // Verify evidence mutation
  const verifyEvidenceMutation = useMutation({
    mutationFn: () => {
      if (!selectedRound) throw new Error('Chưa chọn vòng thẩm định');

      const nowIso = new Date().toISOString().replace(/\.\d{3}Z$/, '.000Z');
      const checks: EvidenceCheck[] = (selectedRound.evidenceChecks || []).map((ec) => ({
        ...ec,
        result: 'PASS',
        reason: 'Đã đối soát hợp lệ với bản gốc',
        checkedAt: nowIso,
      }));

      return approvalApi.verifyEvidence(
        selectedRound.id,
        checks,
        selectedRound.mandatoryChecks || [],
        true
      );
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['rounds-list'] });
      setSelectedRound(updated);
      success('Xác minh bằng chứng thành công', 'Các tiêu chí đã được đánh dấu PASS.');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const rounds = data?.items || [];

  const filteredRounds = rounds.filter((r) => {
    const matchSearch =
      r.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.targetId.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || r.state === statusFilter;
    return matchSearch && matchStatus;
  });

  const getRoundBadge = (state: string) => {
    switch (state) {
      case 'OPEN':
        return <Badge variant="indigo">Đang chờ duyệt (OPEN)</Badge>;
      case 'APPROVED':
        return <Badge variant="emerald">Đã chấp thuận (APPROVED)</Badge>;
      case 'RETURNED':
        return <Badge variant="amber">Yêu cầu bổ sung (RETURNED)</Badge>;
      case 'REJECTED':
        return <Badge variant="rose">Từ chối (REJECTED)</Badge>;
      case 'CANCELLED':
        return <Badge variant="slate">Đã hủy (CANCELLED)</Badge>;
      default:
        return <Badge variant="slate">{state}</Badge>;
    }
  };

  const openDecisionDialog = (action: 'APPROVE' | 'RETURN' | 'REJECT', level: 'L1' | 'L2') => {
    setDecisionAction(action);
    setDecisionLevel(level);
    setDecisionReason('');
    setIsDecisionModalOpen(true);
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <CheckSquare className="w-5 h-5 text-indigo-500" />
            <span>Quy trình Thẩm định & Phê duyệt (Approval Rounds)</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Thẩm định hồ sơ L1/L2 theo nguyên tắc 4 mắt (Checker độc lập) và tra cứu snapshot bất biến.
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
            placeholder="Tìm theo Mã vòng duyệt (Round ID), Mã đối tượng (Target ID)..."
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
            <option value="OPEN">Chờ duyệt (OPEN)</option>
            <option value="APPROVED">Đã chấp thuận (APPROVED)</option>
            <option value="RETURNED">Yêu cầu bổ sung (RETURNED)</option>
            <option value="REJECTED">Từ chối (REJECTED)</option>
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
                <th className="py-3 px-4">Mã Vòng (Round ID)</th>
                <th className="py-3 px-4">Loại đối tượng</th>
                <th className="py-3 px-4">Mã Đối tượng (Target ID)</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4">Tiến độ bước duyệt</th>
                <th className="py-3 px-4">Hạn xử lý (Due At)</th>
                <th className="py-3 px-4 text-right">Chi tiết</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang nạp danh sách các vòng thẩm định...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredRounds.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có vòng duyệt nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Các hồ sơ sau khi Nộp (SUBMIT) sẽ tự động mở vòng thẩm định tại đây.
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
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-semibold text-slate-300">
                        {r.targetKind}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-300">
                      {r.targetId.slice(0, 8)}...
                    </td>
                    <td className="py-3.5 px-4">{getRoundBadge(r.state)}</td>
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-1.5">
                        {r.steps.map((s, idx) => (
                          <span
                            key={idx}
                            className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${
                              s.state === 'APPROVED'
                                ? 'bg-emerald-500/20 text-emerald-400'
                                : s.state === 'REJECTED'
                                ? 'bg-rose-500/20 text-rose-400'
                                : s.state === 'RETURNED'
                                ? 'bg-amber-500/20 text-amber-400'
                                : 'bg-slate-800 text-slate-400'
                            }`}
                          >
                            {s.level}: {s.state}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-3.5 px-4 text-slate-400 font-mono text-[11px]">
                      {r.dueAt ? new Date(r.dueAt).toLocaleString('vi-VN') : '--'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedRound(r)}
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

      {/* ─── Detail Modal (Item 6.5 Round Detail & Snapshot) ────────────────── */}
      {selectedRound && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto p-6 space-y-6 shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <CheckSquare className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Chi tiết Vòng Thẩm định (Approval Round)</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedRound(null)}>
                Đóng
              </Button>
            </div>

            {/* Overview */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã vòng duyệt:</span>
                <span className="font-mono text-indigo-300 font-semibold">{selectedRound.id.slice(0, 12)}...</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Loại & Mã đối tượng:</span>
                <span className="font-mono text-slate-200 font-semibold">{selectedRound.targetKind} · {selectedRound.targetId.slice(0, 8)}...</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Trạng thái vòng:</span>
                {getRoundBadge(selectedRound.state)}
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Hạn xử lý (SLA):</span>
                <span className="text-slate-300 font-mono text-[11px]">{new Date(selectedRound.dueAt).toLocaleDateString('vi-VN')}</span>
              </div>
            </div>

            {/* Steps Timeline */}
            <div className="space-y-3">
              <span className="font-semibold text-slate-300 uppercase tracking-wider text-xs block">
                Tiến độ các bước thẩm định (Approval Steps)
              </span>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {selectedRound.steps.map((step: ApprovalStep, i: number) => (
                  <div key={i} className="p-3.5 bg-slate-950/70 border border-slate-800 rounded-xl space-y-2 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-indigo-300">Cấp độ {step.level}</span>
                      <Badge variant={step.state === 'APPROVED' ? 'emerald' : step.state === 'REJECTED' ? 'rose' : step.state === 'RETURNED' ? 'amber' : 'slate'}>
                        {step.state}
                      </Badge>
                    </div>
                    <div className="space-y-1 text-slate-400 text-[11px]">
                      <div>Người được chỉ định: <span className="font-mono text-slate-300">{step.reviewerId ? `${step.reviewerId.slice(0, 8)}...` : 'Chưa gán'}</span></div>
                      {step.decidedBy && <div>Người quyết định: <span className="font-mono text-slate-300">{step.decidedBy.slice(0, 8)}...</span></div>}
                      {step.decisionAt && <div>Thời gian: <span className="font-mono text-slate-300">{new Date(step.decisionAt).toLocaleString('vi-VN')}</span></div>}
                      {step.reason && <div className="text-slate-200 mt-1 italic">"{step.reason}"</div>}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Snapshot Trigger (Item 6.5) */}
            <div className="p-4 bg-indigo-950/40 border border-indigo-500/30 rounded-xl flex items-center justify-between">
              <div>
                <p className="font-bold text-indigo-200 text-xs flex items-center gap-1.5">
                  <Hash className="w-4 h-4 text-indigo-400" />
                  <span>Xem Bản chốt dữ liệu bất biến (Round Snapshot)</span>
                </p>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  Theo B §6: Snapshot bảo toàn bản ghi lịch sử chính xác tại thời điểm nộp, bất biến trước các sửa đổi sau này.
                </p>
              </div>
              <Button size="sm" variant="outline" onClick={() => setIsSnapshotOpen(true)} className="gap-1.5 text-xs">
                <Eye className="w-3.5 h-3.5" />
                <span>Xem Snapshot</span>
              </Button>
            </div>

            {/* Server-governed Action Toolbar */}
            <div className="p-4 rounded-xl bg-slate-950/80 border border-indigo-500/20 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-200 uppercase tracking-wider">
                  Quyết định Thẩm định (Kiểm soát 4 mắt)
                </span>
                <span className="text-[11px] text-slate-500">
                  Căn cứ vào allowedActions của vòng duyệt
                </span>
              </div>

              <div className="flex flex-wrap items-center gap-2.5 pt-2 border-t border-slate-800">
                {/* APPROVE Button */}
                <ActionButton
                  action="APPROVE"
                  allowedActions={selectedRound.allowedActions}
                  blockedActions={selectedRound.blockedActions}
                  onClick={() => openDecisionDialog('APPROVE', 'L1')}
                  className="gap-1.5"
                >
                  <CheckCircle className="w-3.5 h-3.5" />
                  <span>Chấp thuận (APPROVE)</span>
                </ActionButton>

                {/* RETURN Button */}
                <ActionButton
                  action="RETURN"
                  allowedActions={selectedRound.allowedActions}
                  blockedActions={selectedRound.blockedActions}
                  variant="secondary"
                  onClick={() => openDecisionDialog('RETURN', 'L1')}
                  className="gap-1.5"
                >
                  <RotateCcw className="w-3.5 h-3.5" />
                  <span>Yêu cầu bổ sung (RETURN)</span>
                </ActionButton>

                {/* REJECT Button */}
                <ActionButton
                  action="REJECT"
                  allowedActions={selectedRound.allowedActions}
                  blockedActions={selectedRound.blockedActions}
                  variant="danger"
                  onClick={() => openDecisionDialog('REJECT', 'L1')}
                  className="gap-1.5"
                >
                  <XCircle className="w-3.5 h-3.5" />
                  <span>Từ chối (REJECT)</span>
                </ActionButton>

                {/* VERIFY_EVIDENCE Button */}
                <ActionButton
                  action="VERIFY_EVIDENCE"
                  allowedActions={selectedRound.allowedActions}
                  blockedActions={selectedRound.blockedActions}
                  variant="outline"
                  isLoading={verifyEvidenceMutation.isPending}
                  onClick={() => verifyEvidenceMutation.mutate()}
                  className="gap-1.5"
                >
                  <ShieldCheck className="w-3.5 h-3.5 text-indigo-400" />
                  <span>Xác minh bằng chứng (VERIFY_EVIDENCE)</span>
                </ActionButton>
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setSelectedRound(null)}>
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Decision Dialog Modal ─────────────────────────────────────────── */}
      {isDecisionModalOpen && selectedRound && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-[70] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-white flex items-center gap-2">
              {decisionAction === 'APPROVE' && <CheckCircle className="w-5 h-5 text-emerald-400" />}
              {decisionAction === 'RETURN' && <RotateCcw className="w-5 h-5 text-amber-400" />}
              {decisionAction === 'REJECT' && <XCircle className="w-5 h-5 text-rose-400" />}
              <span>
                {decisionAction === 'APPROVE' && 'Chấp thuận thẩm định'}
                {decisionAction === 'RETURN' && 'Yêu cầu hoàn thiện lại hồ sơ'}
                {decisionAction === 'REJECT' && 'Từ chối phê duyệt'}
              </span>
            </h3>

            <div className="space-y-3 text-xs">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Cấp độ phê duyệt *
                </label>
                <select
                  value={decisionLevel}
                  onChange={(e) => setDecisionLevel(e.target.value as 'L1' | 'L2')}
                  className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-slate-200"
                >
                  <option value="L1">Cấp độ L1 (Kiểm soát viên độc lập)</option>
                  <option value="L2">Cấp độ L2 (Lãnh đạo phê duyệt)</option>
                </select>
              </div>

              <Input
                label="Lý do / Ý kiến thẩm định *"
                placeholder="Nhập nhận xét hoặc cơ sở phê duyệt..."
                value={decisionReason}
                onChange={(e) => setDecisionReason(e.target.value)}
              />
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsDecisionModalOpen(false)}>
                Hủy bỏ
              </Button>
              <Button
                variant={decisionAction === 'REJECT' ? 'danger' : 'primary'}
                size="sm"
                isLoading={decideMutation.isPending}
                disabled={!decisionReason.trim()}
                onClick={() => decideMutation.mutate()}
              >
                Xác nhận
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Round Snapshot Modal (Item 6.5) ─────────────────────────────────── */}
      {isSnapshotOpen && (
        <div className="fixed inset-0 bg-black/85 backdrop-blur-md z-[80] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full max-h-[85vh] overflow-y-auto p-6 space-y-5 shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Hash className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Bản chốt Bất biến (Immutable Round Snapshot)</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setIsSnapshotOpen(false)}>
                Đóng
              </Button>
            </div>

            {snapshotLoading ? (
              <div className="py-12 flex flex-col items-center justify-center gap-2 text-slate-400 text-xs">
                <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                <span>Đang truy xuất Snapshot từ máy chủ...</span>
              </div>
            ) : !snapshotData ? (
              <div className="py-8 text-center text-xs text-slate-400">
                Không thể tải thông tin Snapshot.
              </div>
            ) : (
              <div className="space-y-4 text-xs">
                {snapshotData.targetKind === 'DOSSIER' && 'revision' in snapshotData && (
                  <>
                    <div className="grid grid-cols-2 gap-3 text-slate-300">
                      <div className="p-3 bg-slate-950/70 border border-slate-800 rounded-xl">
                        <span className="text-slate-500 text-[11px] block">Mã bản chốt (Revision ID):</span>
                        <span className="font-mono text-indigo-300 font-semibold">{snapshotData.revision.id}</span>
                      </div>
                      <div className="p-3 bg-slate-950/70 border border-slate-800 rounded-xl">
                        <span className="text-slate-500 text-[11px] block">Số thứ tự bản chốt:</span>
                        <span className="font-mono text-amber-300 font-semibold">Bản #{snapshotData.revision.number}</span>
                      </div>
                    </div>

                    <div className="p-3.5 bg-slate-950/70 border border-slate-800 rounded-xl space-y-2">
                      <span className="font-semibold text-slate-400 block text-[11px] uppercase tracking-wider">
                        Băm toàn vẹn dữ liệu (Cryptographic Hashes)
                      </span>
                      <div className="space-y-1 font-mono text-[11px] break-all">
                        <div className="text-slate-400">
                          Facts Hash: <span className="text-emerald-400">{snapshotData.revision.factsHash}</span>
                        </div>
                        <div className="text-slate-400">
                          Evidence Hash: <span className="text-emerald-400">{snapshotData.revision.evidenceHash}</span>
                        </div>
                      </div>
                    </div>

                    <div className="p-3.5 bg-slate-950/70 border border-slate-800 rounded-xl space-y-2">
                      <span className="font-semibold text-slate-400 block text-[11px] uppercase tracking-wider">
                        Điều khoản chốt tại vòng duyệt
                      </span>
                      <div className="grid grid-cols-2 gap-2 text-slate-300 text-[11px]">
                        <div>Biểu phí: <span className="font-semibold text-white">{parseInt(snapshotData.revision.terms.feeVnd, 10).toLocaleString('vi-VN')} VND</span></div>
                        <div>Kỳ hạn: <span>{snapshotData.revision.terms.termKind}</span></div>
                        <div>Hiệu lực: <span>{snapshotData.revision.terms.startOn}</span></div>
                        <div>Phiên bản: <span>{snapshotData.revision.terms.clauseVersion}</span></div>
                      </div>
                    </div>
                  </>
                )}
              </div>
            )}

            <div className="flex justify-end pt-3 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setIsSnapshotOpen(false)}>
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
