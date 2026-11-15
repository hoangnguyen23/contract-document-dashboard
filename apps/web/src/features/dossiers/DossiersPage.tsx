import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { dossierApi, relationshipApi, branchRefApi } from '@/lib/businessApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import {
  FolderGit2,
  Search,
  Filter,
  RefreshCw,
  Plus,
  Send,
  ChevronRight,
  AlertTriangle,
} from 'lucide-react';
import type { Dossier } from '@/types/business';

export const DossiersPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedDossier, setSelectedDossier] = useState<Dossier | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  // Form states for creation
  const [refCode, setRefCode] = useState('');
  const [serviceType, setServiceType] = useState('PAYMENT_COLLECTION');
  const [maxAmount, setMaxAmount] = useState('500000000');
  const [selectedRelationshipId, setSelectedRelationshipId] = useState('');
  const [selectedBranchId, setSelectedBranchId] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['dossiers-list'],
    queryFn: () => dossierApi.list(),
  });

  // Fetch relationships for dropdown
  const { data: relData, isLoading: relLoading } = useQuery({
    queryKey: ['relationships-dropdown'],
    queryFn: () => relationshipApi.list({ pageSize: '100' }),
    enabled: isCreateModalOpen,
  });

  // Fetch branches for dropdown
  const { data: branchData, isLoading: branchLoading } = useQuery({
    queryKey: ['branches-dropdown'],
    queryFn: () => branchRefApi.list(),
    enabled: isCreateModalOpen,
  });

  const submitMutation = useMutation({
    mutationFn: (id: string) => dossierApi.submit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dossiers-list'] });
      setSelectedDossier(null);
    },
    onError: (err: unknown) => {
      const error = err as { message?: string };
      alert(`Không thể nộp hồ sơ: ${error.message || 'Lỗi không xác định'}`);
    },
  });

  const createMutation = useMutation({
    mutationFn: (dossierData: {
      relationshipId: string;
      branchId: string;
      externalAccountReference: string;
      terms: Record<string, unknown>;
    }) => dossierApi.create(dossierData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dossiers-list'] });
      setIsCreateModalOpen(false);
      setRefCode('');
      setErrorMessage(null);
    },
    onError: (err: unknown) => {
      const error = err as { message?: string };
      setErrorMessage(error.message || 'Không thể tạo hồ sơ nháp');
    },
  });

  const dossiers = data?.items || [];

  const filteredDossiers = dossiers.filter((d) => {
    const matchSearch =
      d.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      d.externalAccountReference?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || d.state === statusFilter;
    return matchSearch && matchStatus;
  });

  const getStatusBadge = (state: string) => {
    switch (state) {
      case 'DRAFT':
        return <Badge variant="slate">Bản nháp (DRAFT)</Badge>;
      case 'IN_REVIEW':
        return <Badge variant="indigo">Đang thẩm định (IN_REVIEW)</Badge>;
      case 'APPROVED':
        return <Badge variant="emerald">Đã duyệt (APPROVED)</Badge>;
      case 'RETURNED':
        return <Badge variant="amber">Yêu cầu bổ sung (RETURNED)</Badge>;
      case 'REJECTED':
        return <Badge variant="rose">Từ chối (REJECTED)</Badge>;
      case 'COMPLETED':
        return <Badge variant="sky">Hoàn tất (COMPLETED)</Badge>;
      default:
        return <Badge variant="slate">{state}</Badge>;
    }
  };

  const handleCreateDraft = (e: React.FormEvent) => {
    e.preventDefault();
    if (!refCode.trim()) {
      setErrorMessage('Vui lòng nhập Mã đối chiếu tài khoản khách hàng');
      return;
    }
    if (!selectedRelationshipId) {
      setErrorMessage('Vui lòng chọn Quan hệ khách hàng (Relationship)');
      return;
    }
    if (!selectedBranchId) {
      setErrorMessage('Vui lòng chọn Chi nhánh xử lý');
      return;
    }
    createMutation.mutate({
      relationshipId: selectedRelationshipId,
      branchId: selectedBranchId,
      externalAccountReference: refCode.trim(),
      terms: {
        serviceType,
        settlementCycle: 'DAILY',
        maxCollectionAmount: parseInt(maxAmount, 10) || 500000000,
      },
    });
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <FolderGit2 className="w-5 h-5 text-indigo-500" />
            <span>Hồ sơ Thu hộ Doanh nghiệp (Dossiers)</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Khởi tạo hồ sơ nháp, đính kèm chứng từ và nộp lên các vòng thẩm định L1/L2.
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

          <Button
            size="sm"
            onClick={() => setIsCreateModalOpen(true)}
            className="gap-1.5"
          >
            <Plus className="w-4 h-4" />
            <span>Lập hồ sơ mới</span>
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
            placeholder="Tìm theo Mã hồ sơ, Mã đối chiếu..."
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
            <option value="RETURNED">Bổ sung (RETURNED)</option>
            <option value="APPROVED">Đã duyệt (APPROVED)</option>
            <option value="COMPLETED">Hoàn tất (COMPLETED)</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/75 dark:bg-slate-800/60 uppercase font-bold text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="py-3 px-4">Mã Hồ sơ</th>
                <th className="py-3 px-4">Mã TK Đối chiếu</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4">Vòng duyệt</th>
                <th className="py-3 px-4">Tài liệu đính kèm</th>
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
                      <span>Đang nạp danh sách hồ sơ...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredDossiers.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có hồ sơ thu hộ nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Bấm "Lập hồ sơ mới" để tạo hồ sơ thu hộ mẫu và nộp duyệt L1/L2.
                    </p>
                  </td>
                </tr>
              ) : (
                filteredDossiers.map((d) => (
                  <tr
                    key={d.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="py-3.5 px-4 font-mono font-semibold text-indigo-400">
                      {d.id.slice(0, 8)}...
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-300">
                      {d.externalAccountReference || '--'}
                    </td>
                    <td className="py-3.5 px-4">{getStatusBadge(d.state)}</td>
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      {d.roundId ? `${d.roundId.slice(0, 8)}...` : 'Chưa mở vòng'}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-[11px] font-semibold">
                        {d.documents?.length || 0} tệp
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {d.createdAt ? new Date(d.createdAt).toLocaleDateString('vi-VN') : '--'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedDossier(d)}
                        className="text-xs"
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

      {/* Create Modal */}
      {isCreateModalOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <h3 className="font-bold text-base text-white">Khởi tạo Hồ sơ thu hộ (Dossier Draft)</h3>
              <Button variant="ghost" size="sm" onClick={() => setIsCreateModalOpen(false)}>
                Đóng
              </Button>
            </div>

            <form onSubmit={handleCreateDraft} className="space-y-4">
              {errorMessage && (
                <div className="p-3 bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs rounded-xl flex items-start gap-2">
                  <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
                  <span>{errorMessage}</span>
                </div>
              )}

              {/* Relationship dropdown */}
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Quan hệ khách hàng (Relationship) <span className="text-rose-400">*</span>
                </label>
                {relLoading ? (
                  <div className="text-xs text-slate-500 py-2">Đang nạp danh sách quan hệ...</div>
                ) : !relData?.items?.length ? (
                  <div className="text-xs text-amber-400 py-2 flex items-center gap-1.5">
                    <AlertTriangle className="w-3.5 h-3.5" />
                    Không tìm thấy Relationship nào — Backend chưa có dữ liệu hoặc bạn không có quyền truy cập.
                  </div>
                ) : (
                  <select
                    value={selectedRelationshipId}
                    onChange={(e) => setSelectedRelationshipId(e.target.value)}
                    className="w-full text-xs bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="">-- Chọn quan hệ khách hàng --</option>
                    {relData.items.map((rel) => (
                      <option key={rel.id} value={rel.id}>
                        {rel.counterpartyName} ({rel.id.slice(0, 8)}…)
                      </option>
                    ))}
                  </select>
                )}
              </div>

              {/* Branch dropdown */}
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Chi nhánh xử lý <span className="text-rose-400">*</span>
                </label>
                {branchLoading ? (
                  <div className="text-xs text-slate-500 py-2">Đang nạp danh sách chi nhánh...</div>
                ) : !branchData?.length ? (
                  <div className="text-xs text-amber-400 py-2 flex items-center gap-1.5">
                    <AlertTriangle className="w-3.5 h-3.5" />
                    Không tìm thấy Chi nhánh nào — Backend chưa có dữ liệu.
                  </div>
                ) : (
                  <select
                    value={selectedBranchId}
                    onChange={(e) => setSelectedBranchId(e.target.value)}
                    className="w-full text-xs bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="">-- Chọn chi nhánh --</option>
                    {branchData.map((b) => (
                      <option key={b.id} value={b.id}>
                        {b.name} ({b.code})
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <Input
                label="Mã đối chiếu tài khoản khách hàng (External Ref)"
                placeholder="VD: ACC-CORP-98821"
                value={refCode}
                onChange={(e) => setRefCode(e.target.value)}
              />

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Loại dịch vụ thu hộ
                </label>
                <select
                  value={serviceType}
                  onChange={(e) => setServiceType(e.target.value)}
                  className="w-full text-xs bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200"
                >
                  <option value="PAYMENT_COLLECTION">Thu hộ tự động định kỳ (PAYMENT_COLLECTION)</option>
                  <option value="DIRECT_DEBIT">Ủy nhiệm thu tự động (DIRECT_DEBIT)</option>
                </select>
              </div>

              <Input
                label="Hạn mức thu hộ tối đa (VND)"
                type="number"
                value={maxAmount}
                onChange={(e) => setMaxAmount(e.target.value)}
              />

              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800 text-[11px] text-slate-400">
                Hồ sơ mới sẽ được tạo ở trạng thái <strong>DRAFT</strong>. Sau khi nộp, hồ sơ sẽ chuyển sang <strong>IN_REVIEW</strong> và mở vòng thẩm định xét duyệt L1.
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setIsCreateModalOpen(false)}
                >
                  Hủy bỏ
                </Button>
                <Button type="submit" size="sm" isLoading={createMutation.isPending}>
                  Tạo hồ sơ nháp
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Detail & Action Modal */}
      {selectedDossier && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full p-6 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <FolderGit2 className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Chi tiết Hồ sơ thu hộ</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedDossier(null)}>
                Đóng
              </Button>
            </div>

            <div className="grid grid-cols-2 gap-4 text-xs">
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã hồ sơ:</span>
                <span className="font-mono text-indigo-300 select-all">{selectedDossier.id}</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã tài khoản đối chiếu:</span>
                <span className="font-mono text-slate-300">{selectedDossier.externalAccountReference}</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Trạng thái hiện tại:</span>
                {getStatusBadge(selectedDossier.state)}
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Vòng thẩm định (Round ID):</span>
                <span className="font-mono text-slate-300">{selectedDossier.roundId || 'Chưa mở vòng'}</span>
              </div>
            </div>

            {selectedDossier.state === 'DRAFT' && (
              <div className="p-4 rounded-xl bg-indigo-950/40 border border-indigo-500/20 text-xs flex items-center justify-between">
                <div>
                  <p className="font-bold text-indigo-300">Nộp hồ sơ thẩm định (SUBMIT)</p>
                  <p className="text-slate-400 text-[11px] mt-0.5">
                    Khóa bản chốt hồ sơ và chuyển giao cho Kiểm soát viên (Checker) phê duyệt L1.
                  </p>
                </div>
                <Button
                  size="sm"
                  onClick={() => submitMutation.mutate(selectedDossier.id)}
                  isLoading={submitMutation.isPending}
                  className="gap-1.5"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>Nộp hồ sơ ngay</span>
                </Button>
              </div>
            )}

            <div className="flex justify-end pt-4 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setSelectedDossier(null)}>
                Đóng cửa sổ
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
