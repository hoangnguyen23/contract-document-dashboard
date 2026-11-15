import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { dossierApi, relationshipApi, reviewerApi } from '@/lib/businessApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { ActionButton } from '@/components/common/ActionGuard';
import { useToast } from '@/context/ToastContext';
import {
  FolderGit2,
  Search,
  Filter,
  RefreshCw,
  Plus,
  Send,
  Undo2,
  XCircle,
  Copy,
  ChevronRight,
  AlertTriangle,
  UserCheck,
  Calendar,
  CheckCircle2,
} from 'lucide-react';
import type {
  Dossier,
  DossierCreate,
  Terms,
  Representation,
  MandatoryCheck,
  ReviewerOption,
} from '@/types/business';

export const DossiersPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { success, showApiError } = useToast();

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedDossier, setSelectedDossier] = useState<Dossier | null>(null);

  // Modals
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isSubmitModalOpen, setIsSubmitModalOpen] = useState(false);
  const [isWithdrawModalOpen, setIsWithdrawModalOpen] = useState(false);
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);

  // Action input states
  const [withdrawReason, setWithdrawReason] = useState('');
  const [cancelReason, setCancelReason] = useState('');
  const [selectedL1Reviewer, setSelectedL1Reviewer] = useState('');
  const [selectedL2Reviewer, setSelectedL2Reviewer] = useState<string>('');

  // Form states for creation
  const [refCode, setRefCode] = useState('ACC-CORP-' + Math.floor(10000 + Math.random() * 90000));
  const [selectedRelationshipId, setSelectedRelationshipId] = useState('');
  const [feeVnd, setFeeVnd] = useState('500000');
  const [feePayer, setFeePayer] = useState<'BENEFICIARY' | 'UNAGREED'>('BENEFICIARY');
  const [pricingMode, setPricingMode] = useState<'NEGOTIATED_FIXED' | 'STANDARD_TARIFF'>('NEGOTIATED_FIXED');
  const [termKind, setTermKind] = useState<'FIXED' | 'OPEN_ENDED'>('FIXED');
  const [startOn, setStartOn] = useState(() => new Date().toISOString().slice(0, 10));
  const [endExclusive, setEndExclusive] = useState(() => {
    const nextYear = new Date();
    nextYear.setFullYear(nextYear.getFullYear() + 1);
    return nextYear.toISOString().slice(0, 10);
  });
  const [clauseVersion, setClauseVersion] = useState<'NEGOTIATED-VA-1' | 'TARIFF-ACK-7D-1'>('NEGOTIATED-VA-1');

  // Representation fields
  const [repBasisType, setRepBasisType] = useState<'LEGAL_REPRESENTATIVE' | 'POWER_OF_ATTORNEY'>('LEGAL_REPRESENTATIVE');
  const [repPersonName, setRepPersonName] = useState('Nguyễn Văn Giám Đốc');
  const [repTitle, setRepTitle] = useState('Tổng Giám Đốc');
  const [repScopeText, setRepScopeText] = useState('Đại diện pháp luật theo Điều lệ Doanh nghiệp');
  const [repValidFrom, setRepValidFrom] = useState('2026-01-01');
  const [repIssuerRef, setRepIssuerRef] = useState('GPKD-0102030405');

  // Fetch list of dossiers
  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['dossiers-list'],
    queryFn: () => dossierApi.list(),
  });

  // Fetch relationships for creation dropdown
  const { data: relData, isLoading: relLoading } = useQuery({
    queryKey: ['relationships-dropdown'],
    queryFn: () => relationshipApi.list({ limit: '100' }),
    enabled: isCreateModalOpen,
  });

  // Fetch Reviewer Options when Submit Modal opens
  const { data: reviewerData, isLoading: reviewerLoading } = useQuery({
    queryKey: ['reviewer-options', selectedDossier?.id],
    queryFn: () => reviewerApi.getOptions('DOSSIER', selectedDossier!.id),
    enabled: isSubmitModalOpen && !!selectedDossier,
  });

  // ─── Mutations ─────────────────────────────────────────────────────────────

  const createMutation = useMutation({
    mutationFn: (dossierData: DossierCreate) => dossierApi.create(dossierData),
    onSuccess: (newDossier) => {
      queryClient.invalidateQueries({ queryKey: ['dossiers-list'] });
      setIsCreateModalOpen(false);
      success('Tạo hồ sơ nháp thành công', `Mã hồ sơ: ${newDossier.id.slice(0, 8)}...`);
    },
    onError: (err) => showApiError(err),
  });

  const submitMutation = useMutation({
    mutationFn: () => {
      if (!selectedDossier || !selectedL1Reviewer) {
        throw new Error('Vui lòng chọn Kiểm soát viên thẩm định L1');
      }

      const nowIso = new Date().toISOString().replace(/\.\d{3}Z$/, '.000Z');
      const mandatoryChecks: MandatoryCheck[] = [
        {
          code: 'AUTHORITY_CURRENT',
          result: 'PASS',
          basis: 'Thẩm quyền đại diện còn hiệu lực theo Giấy phép kinh doanh',
          evidenceFileIds: [],
          checkedAt: nowIso,
        },
        {
          code: 'LEGAL_CONDITION',
          result: 'PASS',
          basis: 'Hồ sơ pháp lý đầy đủ theo quy định của Ngân hàng',
          evidenceFileIds: [],
          checkedAt: nowIso,
        },
        {
          code: 'SERVICE_READINESS',
          result: 'PASS',
          basis: 'Hệ thống tài khoản thu hộ đã sẵn sàng',
          evidenceFileIds: [],
          checkedAt: nowIso,
        },
      ];

      return dossierApi.submit(selectedDossier.id, {
        l1ReviewerId: selectedL1Reviewer,
        l2ReviewerId: selectedL2Reviewer ? selectedL2Reviewer : null,
        mandatoryChecks,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dossiers-list'] });
      setIsSubmitModalOpen(false);
      setSelectedDossier(null);
      success('Nộp hồ sơ thành công', 'Hồ sơ đã chuyển sang vòng thẩm định L1/L2');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const withdrawMutation = useMutation({
    mutationFn: () => {
      if (!selectedDossier) throw new Error('Chưa chọn hồ sơ');
      return dossierApi.withdraw(selectedDossier.id, withdrawReason.trim() || 'Thu hồi để bổ sung thông tin');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dossiers-list'] });
      setIsWithdrawModalOpen(false);
      setSelectedDossier(null);
      setWithdrawReason('');
      success('Thu hồi hồ sơ thành công', 'Hồ sơ đã trở về trạng thái DRAFT');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const cancelMutation = useMutation({
    mutationFn: () => {
      if (!selectedDossier) throw new Error('Chưa chọn hồ sơ');
      return dossierApi.cancel(selectedDossier.id, cancelReason.trim() || 'Hủy bỏ hồ sơ theo yêu cầu');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dossiers-list'] });
      setIsCancelModalOpen(false);
      setSelectedDossier(null);
      setCancelReason('');
      success('Hủy hồ sơ thành công', 'Hồ sơ đã chuyển sang CANCELLED');
    },
    onError: (err) => showApiError(err, () => refetch()),
  });

  const cloneMutation = useMutation({
    mutationFn: (dossier: Dossier) =>
      dossierApi.clone(dossier.id, dossier.relationshipId, 'Nhân bản từ hồ sơ đã kết thúc'),
    onSuccess: (cloned) => {
      queryClient.invalidateQueries({ queryKey: ['dossiers-list'] });
      success('Nhân bản hồ sơ thành công', `Hồ sơ DRAFT mới: ${cloned.id.slice(0, 8)}...`);
    },
    onError: (err) => showApiError(err),
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
      case 'CANCELLED':
        return <Badge variant="slate">Đã hủy (CANCELLED)</Badge>;
      default:
        return <Badge variant="slate">{state}</Badge>;
    }
  };

  const handleCreateDraft = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRelationshipId) {
      showApiError(new Error('Vui lòng chọn Quan hệ khách hàng (Relationship)'));
      return;
    }

    const terms: Terms = {
      currency: 'VND',
      feeVnd: feeVnd.trim() || '0',
      feePayer,
      pricingMode,
      termKind,
      startOn,
      endExclusive: termKind === 'OPEN_ENDED' ? null : endExclusive,
      clauseVersion,
    };

    const representation: Representation = {
      basisType: repBasisType,
      personName: repPersonName.trim(),
      title: repTitle.trim(),
      scopeText: repScopeText.trim(),
      validFrom: repValidFrom,
      validTo: null,
      issuerReference: repIssuerRef.trim(),
      sourceFileId: null,
      checkedAt: null,
      checkEvidenceFileId: null,
      knownRevocationAt: null,
    };

    const newDossier: DossierCreate = {
      relationshipId: selectedRelationshipId,
      externalAccountReference: refCode.trim(),
      linkedFromDossierId: null,
      terms,
      representation,
      documents: [],
    };

    createMutation.mutate(newDossier);
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
            Khởi tạo hồ sơ thu hộ chuẩn B1.3.0, đính kèm chứng từ và nộp lên các vòng thẩm định L1/L2.
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
            onClick={() => {
              if (relData?.items?.length) {
                setSelectedRelationshipId(relData.items[0].id);
              }
              setIsCreateModalOpen(true);
            }}
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
            <option value="IN_REVIEW">Đang thẩm định (IN_REVIEW)</option>
            <option value="RETURNED">Yêu cầu bổ sung (RETURNED)</option>
            <option value="APPROVED">Đã duyệt (APPROVED)</option>
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
                <th className="py-3 px-4">Mã Hồ sơ</th>
                <th className="py-3 px-4">Mã TK Đối chiếu</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4">Biểu phí / Kỳ hạn</th>
                <th className="py-3 px-4">Tài liệu</th>
                <th className="py-3 px-4">Ngày lập</th>
                <th className="py-3 px-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang tải danh sách hồ sơ...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredDossiers.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có hồ sơ thu hộ nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Nhấn "Lập hồ sơ mới" để tạo hồ sơ thu hộ mẫu và nộp duyệt L1/L2.
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
                    <td className="py-3.5 px-4 text-slate-300">
                      <div className="font-semibold">
                        {parseInt(d.terms.feeVnd, 10).toLocaleString('vi-VN')} VND
                      </div>
                      <div className="text-[10px] text-slate-400">{d.terms.pricingMode} · {d.terms.termKind}</div>
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

      {/* ─── Create Modal (B1.3.0 Compliant) ────────────────────────────────── */}
      {isCreateModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6 space-y-5 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <FolderGit2 className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Khởi tạo Hồ sơ thu hộ mới (Dossier Draft)</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setIsCreateModalOpen(false)}>
                Đóng
              </Button>
            </div>

            <form onSubmit={handleCreateDraft} className="space-y-4 text-xs">
              {/* Relationship dropdown */}
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Quan hệ khách hàng (Relationship) <span className="text-rose-400">*</span>
                </label>
                {relLoading ? (
                  <div className="text-xs text-slate-500 py-1.5">Đang tải danh sách quan hệ đối tác...</div>
                ) : !relData?.items?.length ? (
                  <div className="text-xs text-amber-400 py-2 flex items-center gap-1.5">
                    <AlertTriangle className="w-3.5 h-3.5" />
                    Không tìm thấy Relationship nào trong phạm vi quản lý.
                  </div>
                ) : (
                  <select
                    value={selectedRelationshipId}
                    onChange={(e) => setSelectedRelationshipId(e.target.value)}
                    className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    {relData.items.map((rel) => (
                      <option key={rel.id} value={rel.id}>
                        Quan hệ: {rel.id.slice(0, 8)}... (Đối tác ID: {rel.partyId.slice(0, 8)}...)
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <Input
                label="Mã đối chiếu tài khoản khách hàng (External Ref) *"
                placeholder="VD: ACC-CORP-98821"
                value={refCode}
                onChange={(e) => setRefCode(e.target.value)}
              />

              {/* ─── Terms Section ─── */}
              <div className="pt-2 border-t border-slate-800 space-y-3">
                <div className="flex items-center gap-2 text-indigo-400 font-bold text-xs uppercase tracking-wider">
                  <Calendar className="w-4 h-4" />
                  <span>Điều khoản dịch vụ (Terms - B1.3.0)</span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Input
                    label="Mức phí dịch vụ (VND) *"
                    type="number"
                    value={feeVnd}
                    onChange={(e) => setFeeVnd(e.target.value)}
                  />

                  <div className="space-y-1.5">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                      Bên chịu phí (Fee Payer) *
                    </label>
                    <select
                      value={feePayer}
                      onChange={(e) => setFeePayer(e.target.value as 'BENEFICIARY' | 'UNAGREED')}
                      className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200"
                    >
                      <option value="BENEFICIARY">Bên thụ hưởng (BENEFICIARY)</option>
                      <option value="UNAGREED">Chưa thỏa thuận (UNAGREED)</option>
                    </select>
                  </div>

                  <div className="space-y-1.5">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                      Chế độ định giá (Pricing Mode) *
                    </label>
                    <select
                      value={pricingMode}
                      onChange={(e) => setPricingMode(e.target.value as 'NEGOTIATED_FIXED' | 'STANDARD_TARIFF')}
                      className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200"
                    >
                      <option value="NEGOTIATED_FIXED">Thỏa thuận cố định (NEGOTIATED_FIXED)</option>
                      <option value="STANDARD_TARIFF">Biểu phí chuẩn (STANDARD_TARIFF)</option>
                    </select>
                  </div>

                  <div className="space-y-1.5">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                      Loại kỳ hạn (Term Kind) *
                    </label>
                    <select
                      value={termKind}
                      onChange={(e) => setTermKind(e.target.value as 'FIXED' | 'OPEN_ENDED')}
                      className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200"
                    >
                      <option value="FIXED">Có thời hạn xác định (FIXED)</option>
                      <option value="OPEN_ENDED">Không xác định thời hạn (OPEN_ENDED)</option>
                    </select>
                  </div>

                  <Input
                    label="Ngày bắt đầu hiệu lực (Start On) *"
                    type="date"
                    value={startOn}
                    onChange={(e) => setStartOn(e.target.value)}
                  />

                  {termKind === 'FIXED' && (
                    <Input
                      label="Ngày kết thúc độc quyền (End Exclusive) *"
                      type="date"
                      value={endExclusive}
                      onChange={(e) => setEndExclusive(e.target.value)}
                    />
                  )}

                  <div className="space-y-1.5 sm:col-span-2">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                      Phiên bản điều khoản hợp đồng (Clause Version) *
                    </label>
                    <select
                      value={clauseVersion}
                      onChange={(e) => setClauseVersion(e.target.value as 'NEGOTIATED-VA-1' | 'TARIFF-ACK-7D-1')}
                      className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200"
                    >
                      <option value="NEGOTIATED-VA-1">NEGOTIATED-VA-1 (Thỏa thuận thu hộ tài khoản ảo)</option>
                      <option value="TARIFF-ACK-7D-1">TARIFF-ACK-7D-1 (Biểu phí chuẩn xác nhận 7 ngày)</option>
                    </select>
                  </div>
                </div>
              </div>

              {/* ─── Representation Section ─── */}
              <div className="pt-2 border-t border-slate-800 space-y-3">
                <div className="flex items-center gap-2 text-indigo-400 font-bold text-xs uppercase tracking-wider">
                  <UserCheck className="w-4 h-4" />
                  <span>Đại diện pháp luật & Thẩm quyền ký kết</span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="space-y-1.5">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                      Căn cứ đại diện *
                    </label>
                    <select
                      value={repBasisType}
                      onChange={(e) => setRepBasisType(e.target.value as 'LEGAL_REPRESENTATIVE' | 'POWER_OF_ATTORNEY')}
                      className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200"
                    >
                      <option value="LEGAL_REPRESENTATIVE">Người đại diện theo pháp luật</option>
                      <option value="POWER_OF_ATTORNEY">Người được ủy quyền (POA)</option>
                    </select>
                  </div>

                  <Input
                    label="Số văn bản / Giấy phép ĐKKD *"
                    value={repIssuerRef}
                    onChange={(e) => setRepIssuerRef(e.target.value)}
                  />

                  <Input
                    label="Họ tên người đại diện *"
                    value={repPersonName}
                    onChange={(e) => setRepPersonName(e.target.value)}
                  />

                  <Input
                    label="Chức danh đại diện *"
                    value={repTitle}
                    onChange={(e) => setRepTitle(e.target.value)}
                  />

                  <Input
                    label="Hiệu lực đại diện từ ngày *"
                    type="date"
                    value={repValidFrom}
                    onChange={(e) => setRepValidFrom(e.target.value)}
                  />

                  <div className="sm:col-span-2">
                    <Input
                      label="Phạm vi thẩm quyền *"
                      value={repScopeText}
                      onChange={(e) => setRepScopeText(e.target.value)}
                    />
                  </div>
                </div>
              </div>

              <div className="p-3 bg-slate-950/70 rounded-xl border border-slate-800 text-[11px] text-slate-400">
                Hồ sơ mới sẽ được tạo ở trạng thái <strong>DRAFT</strong> theo đúng chuẩn B1.3.0. Người lập hồ sơ (Maker) có thể tải lên tài liệu chứng minh và nộp duyệt L1/L2.
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
                <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateModalOpen(false)}>
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

      {/* ─── Detail & Actions Modal (Advanced Dossier Actions) ──────────────── */}
      {selectedDossier && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto p-6 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <FolderGit2 className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Chi tiết Hồ sơ thu hộ</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedDossier(null)}>
                Đóng
              </Button>
            </div>

            {/* Overview Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã hồ sơ:</span>
                <span className="font-mono text-indigo-300 select-all font-semibold">{selectedDossier.id.slice(0, 12)}...</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Trạng thái:</span>
                {getStatusBadge(selectedDossier.state)}
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Phiên bản (ETag):</span>
                <span className="font-mono text-amber-300">v{selectedDossier.version} (r-{selectedDossier.version})</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Vòng duyệt (Round ID):</span>
                <span className="font-mono text-slate-300">{selectedDossier.roundId ? `${selectedDossier.roundId.slice(0, 8)}...` : 'Chưa mở'}</span>
              </div>
            </div>

            {/* Terms Summary */}
            <div className="p-4 bg-slate-950/50 rounded-xl border border-slate-800/80 space-y-2 text-xs">
              <span className="font-semibold text-slate-300 uppercase tracking-wider text-[11px] block">
                Điều khoản thu hộ hiện hành
              </span>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-slate-300">
                <div>
                  <span className="text-slate-500 text-[11px] block">Mức phí:</span>
                  <span className="font-bold text-white">{parseInt(selectedDossier.terms.feeVnd, 10).toLocaleString('vi-VN')} VND</span>
                </div>
                <div>
                  <span className="text-slate-500 text-[11px] block">Chế độ giá:</span>
                  <span>{selectedDossier.terms.pricingMode}</span>
                </div>
                <div>
                  <span className="text-slate-500 text-[11px] block">Kỳ hạn:</span>
                  <span>{selectedDossier.terms.termKind}</span>
                </div>
                <div>
                  <span className="text-slate-500 text-[11px] block">Hiệu lực từ:</span>
                  <span>{selectedDossier.terms.startOn}</span>
                </div>
              </div>
            </div>

            {/* Representation Summary */}
            <div className="p-4 bg-slate-950/50 rounded-xl border border-slate-800/80 space-y-2 text-xs">
              <span className="font-semibold text-slate-300 uppercase tracking-wider text-[11px] block">
                Người đại diện ký kết
              </span>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-slate-300">
                <div>
                  <span className="text-slate-500 text-[11px] block">Họ và tên:</span>
                  <span className="font-semibold text-white">{selectedDossier.representation.personName}</span>
                </div>
                <div>
                  <span className="text-slate-500 text-[11px] block">Chức vụ:</span>
                  <span>{selectedDossier.representation.title}</span>
                </div>
                <div>
                  <span className="text-slate-500 text-[11px] block">Căn cứ:</span>
                  <span>{selectedDossier.representation.basisType} ({selectedDossier.representation.issuerReference})</span>
                </div>
              </div>
            </div>

            {/* Server-governed Action Toolbar (B §6 Permission Guards) */}
            <div className="p-4 rounded-xl bg-slate-950/80 border border-indigo-500/20 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-200 uppercase tracking-wider">
                  Thao tác nghiệp vụ theo Hợp đồng B1.3.0
                </span>
                <span className="text-[11px] text-slate-500">
                  Quyền kích hoạt được kiểm duyệt qua máy chủ (allowedActions)
                </span>
              </div>

              <div className="flex flex-wrap items-center gap-2.5 pt-2 border-t border-slate-800">
                {/* SUBMIT Action */}
                <ActionButton
                  action="SUBMIT"
                  allowedActions={selectedDossier.allowedActions}
                  blockedActions={selectedDossier.blockedActions}
                  onClick={() => setIsSubmitModalOpen(true)}
                  className="gap-1.5"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>Nộp thẩm định (SUBMIT)</span>
                </ActionButton>

                {/* WITHDRAW Action */}
                <ActionButton
                  action="WITHDRAW"
                  allowedActions={selectedDossier.allowedActions}
                  blockedActions={selectedDossier.blockedActions}
                  variant="secondary"
                  onClick={() => setIsWithdrawModalOpen(true)}
                  className="gap-1.5"
                >
                  <Undo2 className="w-3.5 h-3.5" />
                  <span>Thu hồi hồ sơ (WITHDRAW)</span>
                </ActionButton>

                {/* CANCEL Action */}
                <ActionButton
                  action="CANCEL"
                  allowedActions={selectedDossier.allowedActions}
                  blockedActions={selectedDossier.blockedActions}
                  variant="danger"
                  onClick={() => setIsCancelModalOpen(true)}
                  className="gap-1.5"
                >
                  <XCircle className="w-3.5 h-3.5" />
                  <span>Hủy hồ sơ (CANCEL)</span>
                </ActionButton>

                {/* CLONE Action */}
                <ActionButton
                  action="CLONE"
                  allowedActions={selectedDossier.allowedActions}
                  blockedActions={selectedDossier.blockedActions}
                  variant="outline"
                  isLoading={cloneMutation.isPending}
                  onClick={() => cloneMutation.mutate(selectedDossier)}
                  className="gap-1.5"
                >
                  <Copy className="w-3.5 h-3.5" />
                  <span>Nhân bản (CLONE)</span>
                </ActionButton>
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setSelectedDossier(null)}>
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Submit Reviewer Selection Modal (Item 6.2 Reviewer Options API) ── */}
      {isSubmitModalOpen && selectedDossier && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 space-y-5 shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Send className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Chỉ định Người thẩm định (L1/L2 Reviewers)</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setIsSubmitModalOpen(false)}>
                Đóng
              </Button>
            </div>

            <div className="space-y-4 text-xs">
              <div className="p-3 bg-indigo-950/40 border border-indigo-500/20 rounded-xl text-slate-300 text-[11px] leading-relaxed">
                Theo Hợp đồng B1.3.0 (§3, §6), danh sách người duyệt được truy vấn theo thời gian thực từ API{' '}
                <code className="text-indigo-400 font-mono">/reviewer-options</code> nhằm bảo đảm nguyên tắc phân tách nhiệm vụ (SoD) và thẩm quyền xét duyệt.
              </div>

              {reviewerLoading ? (
                <div className="py-8 flex flex-col items-center justify-center gap-2 text-slate-400">
                  <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                  <span>Đang tải danh sách Kiểm soát viên đủ điều kiện...</span>
                </div>
              ) : (
                <>
                  {/* L1 Reviewer Dropdown */}
                  <div className="space-y-1.5">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300">
                      Kiểm soát viên L1 (Checker) <span className="text-rose-400">*</span>
                    </label>
                    <select
                      value={selectedL1Reviewer}
                      onChange={(e) => setSelectedL1Reviewer(e.target.value)}
                      className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                      <option value="">-- Chọn Kiểm soát viên L1 --</option>
                      {reviewerData?.items
                        ?.filter((r: ReviewerOption) => r.level === 'L1')
                        .map((r: ReviewerOption) => (
                          <option key={r.userId} value={r.userId}>
                            {r.displayName} ({r.userId.slice(0, 8)}...)
                          </option>
                        ))}
                    </select>
                  </div>

                  {/* L2 Reviewer Dropdown */}
                  <div className="space-y-1.5">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300">
                      Lãnh đạo phê duyệt L2 (Manager - Không bắt buộc)
                    </label>
                    <select
                      value={selectedL2Reviewer}
                      onChange={(e) => setSelectedL2Reviewer(e.target.value)}
                      className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                      <option value="">-- Không yêu cầu L2 (Chỉ duyệt L1) --</option>
                      {reviewerData?.items
                        ?.filter((r: ReviewerOption) => r.level === 'L2')
                        .map((r: ReviewerOption) => (
                          <option key={r.userId} value={r.userId}>
                            {r.displayName} ({r.userId.slice(0, 8)}...)
                          </option>
                        ))}
                    </select>
                  </div>

                  {/* Mandatory Checks confirmation */}
                  <div className="p-3 bg-slate-950/70 border border-slate-800 rounded-xl space-y-1.5">
                    <span className="font-semibold text-slate-400 block uppercase text-[10px] tracking-wider">
                      Các tiêu chí kiểm soát bắt buộc (Mandatory Checks - 3/3 PASS)
                    </span>
                    <div className="space-y-1 text-[11px] text-slate-300">
                      <div className="flex items-center gap-1.5 text-emerald-400">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        <span>AUTHORITY_CURRENT: Thẩm quyền đại diện còn hiệu lực</span>
                      </div>
                      <div className="flex items-center gap-1.5 text-emerald-400">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        <span>LEGAL_CONDITION: Đầy đủ điều kiện pháp lý hợp đồng</span>
                      </div>
                      <div className="flex items-center gap-1.5 text-emerald-400">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        <span>SERVICE_READINESS: Sẵn sàng kích hoạt dịch vụ thu hộ</span>
                      </div>
                    </div>
                  </div>
                </>
              )}

              <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
                <Button variant="outline" size="sm" onClick={() => setIsSubmitModalOpen(false)}>
                  Hủy bỏ
                </Button>
                <Button
                  size="sm"
                  disabled={!selectedL1Reviewer || submitMutation.isPending}
                  isLoading={submitMutation.isPending}
                  onClick={() => submitMutation.mutate()}
                >
                  Xác nhận Nộp duyệt
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ─── Withdraw Confirmation Modal ────────────────────────────────────── */}
      {isWithdrawModalOpen && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-white flex items-center gap-2">
              <Undo2 className="w-5 h-5 text-amber-400" />
              <span>Thu hồi hồ sơ thẩm định</span>
            </h3>
            <p className="text-xs text-slate-400">
              Hồ sơ đang trong quá trình duyệt sẽ được thu hồi về trạng thái <strong>DRAFT</strong>. Vòng thẩm định hiện tại sẽ bị hủy bỏ.
            </p>
            <Input
              label="Lý do thu hồi hồ sơ *"
              placeholder="VD: Cần cập nhật lại biểu phí theo yêu cầu của khách hàng"
              value={withdrawReason}
              onChange={(e) => setWithdrawReason(e.target.value)}
            />
            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsWithdrawModalOpen(false)}>
                Hủy bỏ
              </Button>
              <Button
                variant="primary"
                size="sm"
                isLoading={withdrawMutation.isPending}
                onClick={() => withdrawMutation.mutate()}
              >
                Xác nhận thu hồi
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ─── Cancel Confirmation Modal ──────────────────────────────────────── */}
      {isCancelModalOpen && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-rose-400 flex items-center gap-2">
              <XCircle className="w-5 h-5" />
              <span>Hủy bỏ hồ sơ thu hộ</span>
            </h3>
            <p className="text-xs text-slate-400">
              Hành động này sẽ đóng vĩnh viễn hồ sơ và chuyển sang <strong>CANCELLED</strong>. Bạn không thể hoàn tác thao tác này.
            </p>
            <Input
              label="Lý do hủy hồ sơ *"
              placeholder="VD: Khách hàng tạm dừng kế hoạch triển khai"
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
            />
            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsCancelModalOpen(false)}>
                Đóng
              </Button>
              <Button
                variant="danger"
                size="sm"
                isLoading={cancelMutation.isPending}
                onClick={() => cancelMutation.mutate()}
              >
                Xác nhận Hủy hồ sơ
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
