import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { contractApi } from '@/lib/businessApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { ActionButton } from '@/components/common/ActionGuard';
import { useToast } from '@/context/ToastContext';
import {
  FileText,
  Search,
  Filter,
  RefreshCw,
  ChevronRight,
  PauseCircle,
  PlayCircle,
  XCircle,
} from 'lucide-react';
import type { Contract } from '@/types/business';

export const ContractsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { success, showApiError } = useToast();

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [selectedContract, setSelectedContract] = useState<Contract | null>(null);

  // Action Modals
  const [isHoldModalOpen, setIsHoldModalOpen] = useState(false);
  const [isReleaseModalOpen, setIsReleaseModalOpen] = useState(false);
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);

  // Form states
  const [holdReason, setHoldReason] = useState('');
  const [releaseBasis, setReleaseBasis] = useState('');
  const [cancelReason, setCancelReason] = useState('');

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['contracts-list'],
    queryFn: () => contractApi.list(),
  });

  // Mutations for Contract Commands (Item 6.4)
  const holdMutation = useMutation({
    mutationFn: () => {
      if (!selectedContract) throw new Error('Chưa chọn hợp đồng');
      return contractApi.holdReadiness(
        selectedContract.id,
        holdReason.trim() || 'Tạm hoãn sẵn sàng do phát hiện rủi ro',
        ['00000000-0000-4000-8000-000000000001']
      );
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['contracts-list'] });
      setIsHoldModalOpen(false);
      setSelectedContract(updated);
      setHoldReason('');
      success('Tạm hoãn sẵn sàng thành công', 'Trạng thái sẵn sàng đã chuyển sang NOT_READY');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const releaseMutation = useMutation({
    mutationFn: () => {
      if (!selectedContract) throw new Error('Chưa chọn hợp đồng');
      const riskId = '00000000-0000-4000-8000-000000000001';
      return contractApi.releaseReadiness(
        selectedContract.id,
        riskId,
        releaseBasis.trim() || 'Rủi ro đã được giải tỏa đầy đủ căn cứ',
        ['00000000-0000-4000-8000-000000000001']
      );
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['contracts-list'] });
      setIsReleaseModalOpen(false);
      setSelectedContract(updated);
      setReleaseBasis('');
      success('Giải tỏa sẵn sàng thành công', 'Hợp đồng đã trở lại trạng thái READY');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const cancelMutation = useMutation({
    mutationFn: () => {
      if (!selectedContract) throw new Error('Chưa chọn hợp đồng');
      return contractApi.cancel(
        selectedContract.id,
        cancelReason.trim() || 'Hủy hợp đồng trước hiệu lực',
        ['00000000-0000-4000-8000-000000000001']
      );
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['contracts-list'] });
      setIsCancelModalOpen(false);
      setSelectedContract(updated);
      setCancelReason('');
      success('Hủy hợp đồng thành công', 'Hợp đồng và hồ sơ liên quan đã chuyển sang CANCELLED');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const contracts = data?.items || [];

  const filteredContracts = contracts.filter((c) => {
    const matchSearch =
      c.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.dossierId?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.branchId?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || c.state === statusFilter;
    return matchSearch && matchStatus;
  });

  const getStatusBadge = (state: string) => {
    switch (state) {
      case 'ACTIVE':
        return <Badge variant="emerald">Đang hiệu lực (ACTIVE)</Badge>;
      case 'PREPARED':
        return <Badge variant="indigo">Đã lập xong (PREPARED)</Badge>;
      case 'TERMINATED':
        return <Badge variant="rose">Đã chấm dứt (TERMINATED)</Badge>;
      case 'CLOSED':
        return <Badge variant="slate">Đã thanh lý (CLOSED)</Badge>;
      default:
        return <Badge variant="slate">{state}</Badge>;
    }
  };

  const getReadinessBadge = (readiness: string) => {
    switch (readiness) {
      case 'READY':
        return <Badge variant="emerald">Sẵn sàng (READY)</Badge>;
      case 'NOT_READY':
        return <Badge variant="amber">Chưa sẵn sàng (NOT_READY)</Badge>;
      case 'EXPIRED':
        return <Badge variant="rose">Hết hạn sẵn sàng (EXPIRED)</Badge>;
      default:
        return <Badge variant="slate">{readiness}</Badge>;
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Title & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <FileText className="w-5 h-5 text-indigo-500" />
            <span>Quản lý Hợp đồng (Contracts)</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Tra cứu hợp đồng đã phát hành từ hồ sơ thẩm định và theo dõi vòng đời ký kết.
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

      {/* Filter Toolbar */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo Mã hợp đồng, Mã hồ sơ..."
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
            <option value="PREPARED">Đã lập xong (PREPARED)</option>
            <option value="ACTIVE">Đang hiệu lực (ACTIVE)</option>
            <option value="TERMINATED">Đã chấm dứt (TERMINATED)</option>
            <option value="CLOSED">Đã thanh lý (CLOSED)</option>
          </select>
        </div>
      </div>

      {/* Contracts Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/75 dark:bg-slate-800/60 uppercase font-bold text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="py-3 px-4">Mã Hợp đồng</th>
                <th className="py-3 px-4">Mã Hồ sơ gốc</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4">Độ sẵn sàng (Readiness)</th>
                <th className="py-3 px-4">Dịch vụ</th>
                <th className="py-3 px-4">Ngày tạo</th>
                <th className="py-3 px-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang nạp danh sách hợp đồng...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredContracts.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có hợp đồng nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Hợp đồng sẽ được phát hành tự động sau khi Hồ sơ thu hộ được L1/L2 phê duyệt thành công.
                    </p>
                  </td>
                </tr>
              ) : (
                filteredContracts.map((c) => (
                  <tr
                    key={c.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="py-3.5 px-4 font-mono font-semibold text-indigo-400">
                      {c.id.slice(0, 8)}...
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-300">
                      {c.dossierId ? `${c.dossierId.slice(0, 8)}...` : '--'}
                    </td>
                    <td className="py-3.5 px-4">{getStatusBadge(c.state)}</td>
                    <td className="py-3.5 px-4">{getReadinessBadge(c.readiness)}</td>
                    <td className="py-3.5 px-4 font-mono text-slate-300">
                      {c.serviceState}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {c.createdAt ? new Date(c.createdAt).toLocaleDateString('vi-VN') : '--'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedContract(c)}
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

      {/* ─── Detail Modal (Contract Actions - Item 6.4) ──────────────────────── */}
      {selectedContract && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6 space-y-6 shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <FileText className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Chi tiết Hợp đồng Thu hộ</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedContract(null)}>
                Đóng
              </Button>
            </div>

            {/* Overview Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã hợp đồng:</span>
                <span className="font-mono text-indigo-300 font-semibold">{selectedContract.id.slice(0, 12)}...</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Trạng thái:</span>
                {getStatusBadge(selectedContract.state)}
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Độ sẵn sàng:</span>
                {getReadinessBadge(selectedContract.readiness)}
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Trạng thái dịch vụ:</span>
                <span className="font-mono text-slate-200 font-semibold">{selectedContract.serviceState}</span>
              </div>
            </div>

            {/* Approved Terms */}
            {selectedContract.approvedTerms && (
              <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800 space-y-2 text-xs">
                <span className="font-semibold text-slate-300 uppercase tracking-wider text-[11px] block">
                  Điều khoản đã phê duyệt (Approved Terms)
                </span>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-slate-300">
                  <div>
                    <span className="text-slate-500 text-[11px] block">Mức phí:</span>
                    <span className="font-bold text-white">
                      {parseInt(selectedContract.approvedTerms.feeVnd, 10).toLocaleString('vi-VN')} VND
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-500 text-[11px] block">Chế độ giá:</span>
                    <span>{selectedContract.approvedTerms.pricingMode}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 text-[11px] block">Kỳ hạn:</span>
                    <span>{selectedContract.approvedTerms.termKind}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 text-[11px] block">Bắt đầu:</span>
                    <span>{selectedContract.approvedTerms.startOn}</span>
                  </div>
                </div>
              </div>
            )}

            {/* Server-governed Action Toolbar (Item 6.4 Contract Actions) */}
            <div className="p-4 rounded-xl bg-slate-950/80 border border-indigo-500/20 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-200 uppercase tracking-wider">
                  Thao tác Quản lý Hợp đồng (B §6)
                </span>
                <span className="text-[11px] text-slate-500">
                  Căn cứ vào allowedActions của Hợp đồng
                </span>
              </div>

              <div className="flex flex-wrap items-center gap-2.5 pt-2 border-t border-slate-800">
                {/* HOLD_READINESS Button */}
                <ActionButton
                  action="HOLD_READINESS"
                  allowedActions={selectedContract.allowedActions}
                  blockedActions={selectedContract.blockedActions}
                  variant="secondary"
                  onClick={() => setIsHoldModalOpen(true)}
                  className="gap-1.5"
                >
                  <PauseCircle className="w-3.5 h-3.5 text-amber-400" />
                  <span>Tạm hoãn sẵn sàng (HOLD_READINESS)</span>
                </ActionButton>

                {/* RELEASE_READINESS Button */}
                <ActionButton
                  action="RELEASE_READINESS"
                  allowedActions={selectedContract.allowedActions}
                  blockedActions={selectedContract.blockedActions}
                  variant="primary"
                  onClick={() => setIsReleaseModalOpen(true)}
                  className="gap-1.5"
                >
                  <PlayCircle className="w-3.5 h-3.5" />
                  <span>Giải tỏa sẵn sàng (RELEASE_READINESS)</span>
                </ActionButton>

                {/* CANCEL Button */}
                <ActionButton
                  action="CANCEL"
                  allowedActions={selectedContract.allowedActions}
                  blockedActions={selectedContract.blockedActions}
                  variant="danger"
                  onClick={() => setIsCancelModalOpen(true)}
                  className="gap-1.5"
                >
                  <XCircle className="w-3.5 h-3.5" />
                  <span>Hủy hợp đồng (CANCEL)</span>
                </ActionButton>
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setSelectedContract(null)}>
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Hold Readiness Modal ──────────────────────────────────────────── */}
      {isHoldModalOpen && selectedContract && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-[70] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-amber-400 flex items-center gap-2">
              <PauseCircle className="w-5 h-5" />
              <span>Tạm hoãn sẵn sàng Hợp đồng</span>
            </h3>
            <p className="text-xs text-slate-400">
              Kiểm soát viên độc lập kích hoạt tạm hoãn khi phát hiện rủi ro pháp lý hoặc tài khoản chưa đủ điều kiện.
            </p>
            <Input
              label="Lý do tạm hoãn *"
              placeholder="VD: Nghi ngờ thông tin tài khoản đối chiếu chưa xác thực"
              value={holdReason}
              onChange={(e) => setHoldReason(e.target.value)}
            />
            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsHoldModalOpen(false)}>
                Hủy bỏ
              </Button>
              <Button
                variant="primary"
                size="sm"
                isLoading={holdMutation.isPending}
                disabled={!holdReason.trim()}
                onClick={() => holdMutation.mutate()}
              >
                Xác nhận Tạm hoãn
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Release Readiness Modal ───────────────────────────────────────── */}
      {isReleaseModalOpen && selectedContract && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-[70] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-emerald-400 flex items-center gap-2">
              <PlayCircle className="w-5 h-5" />
              <span>Giải tỏa sẵn sàng Hợp đồng</span>
            </h3>
            <p className="text-xs text-slate-400">
              Kiểm soát viên độc lập xác nhận rủi ro đã được giải tỏa đầy đủ căn cứ bằng chứng.
            </p>
            <Input
              label="Cơ sở giải tỏa rủi ro *"
              placeholder="VD: Đã đối soát xác thực văn bản ủy quyền bổ sung"
              value={releaseBasis}
              onChange={(e) => setReleaseBasis(e.target.value)}
            />
            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsReleaseModalOpen(false)}>
                Hủy bỏ
              </Button>
              <Button
                size="sm"
                isLoading={releaseMutation.isPending}
                disabled={!releaseBasis.trim()}
                onClick={() => releaseMutation.mutate()}
              >
                Xác nhận Giải tỏa
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Cancel Contract Modal ─────────────────────────────────────────── */}
      {isCancelModalOpen && selectedContract && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-[70] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-rose-400 flex items-center gap-2">
              <XCircle className="w-5 h-5" />
              <span>Hủy bỏ Hợp đồng trước hiệu lực</span>
            </h3>
            <Input
              label="Lý do hủy hợp đồng *"
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
                Xác nhận Hủy hợp đồng
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
