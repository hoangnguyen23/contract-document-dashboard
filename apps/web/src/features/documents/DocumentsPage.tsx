import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fileApi, relationshipApi } from '@/lib/businessApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { useToast } from '@/context/ToastContext';
import {
  Files,
  Search,
  Filter,
  RefreshCw,
  UploadCloud,
  Download,
  FileText,
  AlertTriangle,
} from 'lucide-react';

export const DocumentsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { success, showApiError } = useToast();

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  // Upload form state
  const [targetKind, setTargetKind] = useState<'RELATIONSHIP' | 'DOSSIER' | 'CONTRACT'>('RELATIONSHIP');
  const [targetId, setTargetId] = useState('');
  const [branchId, setBranchId] = useState('');
  const [purpose, setPurpose] = useState<
    | 'SERVICE_APPLICATION'
    | 'CORPORATE_ID'
    | 'REPRESENTATION_BASIS'
    | 'SERVICE_TERMS'
    | 'IDENTITY_IMAGE'
    | 'SIGNING_COPY'
    | 'NOTICE_EVIDENCE'
    | 'DUTY_EVIDENCE'
    | 'CONTROL_EVIDENCE'
    | 'DATA_BASIS'
  >('REPRESENTATION_BASIS');

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['files-list'],
    queryFn: () => fileApi.list(),
  });

  // Relationships to select targetId and branchId
  const { data: relData } = useQuery({
    queryKey: ['relationships-for-upload'],
    queryFn: () => relationshipApi.list({ limit: '50' }),
    enabled: isUploadModalOpen,
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) => {
      const activeBranchId = branchId || relData?.items?.[0]?.branchId || '00000000-0000-4000-8000-000000000001';
      const activeTargetId = targetId || relData?.items?.[0]?.id || '00000000-0000-4000-8000-00000000000f';
      return fileApi.upload(file, activeBranchId, purpose, targetKind, activeTargetId);
    },
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['files-list'] });
      setIsUploadModalOpen(false);
      setSelectedFile(null);
      success('Tải lên tệp thành công', `${res.filename} đã được gửi tới ClamAV quét mã độc.`);
    },
    onError: (err) => showApiError(err),
  });

  const files = data?.items || [];

  const filteredFiles = files.filter((f) => {
    const matchSearch =
      f.filename.toLowerCase().includes(searchTerm.toLowerCase()) ||
      f.id.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || f.state === statusFilter;
    return matchSearch && matchStatus;
  });

  const getScanBadge = (state: string) => {
    switch (state) {
      case 'CLEAN':
        return <Badge variant="emerald">An toàn (CLEAN)</Badge>;
      case 'PENDING_SCAN':
        return <Badge variant="amber">Đang quét virus</Badge>;
      case 'INFECTED':
        return <Badge variant="rose">Phát hiện virus (INFECTED)</Badge>;
      case 'REDACTED':
        return <Badge variant="indigo">Đã che mờ (REDACTED)</Badge>;
      case 'DISPOSED':
        return <Badge variant="slate">Đã tiêu hủy (DISPOSED)</Badge>;
      case 'SCAN_FAILED':
        return <Badge variant="rose">Quét lỗi (SCAN_FAILED)</Badge>;
      default:
        return <Badge variant="slate">{state}</Badge>;
    }
  };

  const handleStartUpload = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      showApiError(new Error('Vui lòng chọn tệp tin cần tải lên'));
      return;
    }
    uploadMutation.mutate(selectedFile);
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Files className="w-5 h-5 text-indigo-500" />
            <span>Quản lý Tài liệu & Bằng chứng (Documents)</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Lưu trữ bằng chứng hợp đồng, kiểm tra an toàn virus ClamAV và áp dụng chính sách bảo mật tệp.
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
                setTargetId(relData.items[0].id);
                setBranchId(relData.items[0].branchId);
              }
              setIsUploadModalOpen(true);
            }}
            className="gap-1.5"
          >
            <UploadCloud className="w-4 h-4" />
            <span>Tải lên tài liệu</span>
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
            placeholder="Tìm theo Tên tệp tin, Mã UUID..."
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
            <option value="ALL">Tất cả trạng thái quét</option>
            <option value="CLEAN">CLEAN (An toàn)</option>
            <option value="PENDING_SCAN">PENDING_SCAN (Chờ quét)</option>
            <option value="REDACTED">REDACTED (Che mờ)</option>
            <option value="INFECTED">INFECTED (Mã độc)</option>
            <option value="DISPOSED">DISPOSED (Tiêu hủy)</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/75 dark:bg-slate-800/60 uppercase font-bold text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="py-3 px-4">Tên tệp</th>
                <th className="py-3 px-4">Định dạng</th>
                <th className="py-3 px-4">Kích thước</th>
                <th className="py-3 px-4">Kiểm tra An toàn (ClamAV)</th>
                <th className="py-3 px-4">Mục đích</th>
                <th className="py-3 px-4">Ngày tải lên</th>
                <th className="py-3 px-4 text-right">Tải về</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang nạp danh sách tệp tài liệu...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredFiles.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có tệp tài liệu nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Nhấn "Tải lên tài liệu" để gửi file đính kèm hồ sơ và quét virus tự động.
                    </p>
                  </td>
                </tr>
              ) : (
                filteredFiles.map((f) => (
                  <tr
                    key={f.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="py-3.5 px-4 font-semibold text-slate-200 flex items-center gap-2">
                      <FileText className="w-4 h-4 text-indigo-400 shrink-0" />
                      <span className="truncate max-w-xs">{f.filename}</span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-400 font-mono text-[11px]">
                      {f.mediaType || 'application/octet-stream'}
                    </td>
                    <td className="py-3.5 px-4 text-slate-300 font-mono text-[11px]">
                      {(f.sizeBytes / 1024).toFixed(1)} KB
                    </td>
                    <td className="py-3.5 px-4">{getScanBadge(f.state)}</td>
                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-semibold text-slate-300">
                        {f.purpose}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {f.createdAt ? new Date(f.createdAt).toLocaleDateString('vi-VN') : '--'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => fileApi.download(f.id, f.filename)}
                        className="text-xs gap-1.5"
                      >
                        <Download className="w-3.5 h-3.5" />
                        <span>Tải tệp</span>
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>

      {/* ─── Upload Modal (B1.3.0 Compliant) ────────────────────────────────── */}
      {isUploadModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-5 shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <UploadCloud className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Tải lên Tài liệu / Bằng chứng</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setIsUploadModalOpen(false)}>
                Đóng
              </Button>
            </div>

            <form onSubmit={handleStartUpload} className="space-y-4 text-xs">
              {/* File input */}
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Chọn tệp tin (PDF, JPEG, PNG tối đa 10MiB) <span className="text-rose-400">*</span>
                </label>
                <input
                  type="file"
                  accept=".pdf,.png,.jpg,.jpeg"
                  onChange={(e) => {
                    if (e.target.files && e.target.files[0]) {
                      setSelectedFile(e.target.files[0]);
                    }
                  }}
                  className="w-full text-xs text-slate-300 file:mr-3 file:py-2 file:px-3 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-indigo-600 file:text-white hover:file:bg-indigo-500 bg-slate-950 border border-slate-700 rounded-xl p-2 cursor-pointer"
                />
              </div>

              {/* Purpose dropdown */}
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Mục đích tài liệu (Purpose) <span className="text-rose-400">*</span>
                </label>
                <select
                  value={purpose}
                  onChange={(e) => setPurpose(e.target.value as typeof purpose)}
                  className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="REPRESENTATION_BASIS">Căn cứ đại diện (REPRESENTATION_BASIS)</option>
                  <option value="CORPORATE_ID">Đăng ký kinh doanh (CORPORATE_ID)</option>
                  <option value="SERVICE_APPLICATION">Đơn đăng ký dịch vụ (SERVICE_APPLICATION)</option>
                  <option value="SERVICE_TERMS">Điều khoản dịch vụ thỏa thuận (SERVICE_TERMS)</option>
                  <option value="IDENTITY_IMAGE">Ảnh CCCD/Hộ chiếu người đại diện (IDENTITY_IMAGE)</option>
                  <option value="SIGNING_COPY">Bản sao hợp đồng đã ký (SIGNING_COPY)</option>
                  <option value="NOTICE_EVIDENCE">Bằng chứng gửi/nhận thông báo (NOTICE_EVIDENCE)</option>
                  <option value="CONTROL_EVIDENCE">Bằng chứng kiểm soát dịch vụ (CONTROL_EVIDENCE)</option>
                </select>
              </div>

              {/* Target Kind */}
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                    Gắn vào đối tượng *
                  </label>
                  <select
                    value={targetKind}
                    onChange={(e) => setTargetKind(e.target.value as typeof targetKind)}
                    className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-slate-200"
                  >
                    <option value="RELATIONSHIP">Quan hệ khách hàng (RELATIONSHIP)</option>
                    <option value="DOSSIER">Hồ sơ thu hộ (DOSSIER)</option>
                    <option value="CONTRACT">Hợp đồng (CONTRACT)</option>
                  </select>
                </div>

                <div className="space-y-1.5">
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                    Đối tác áp dụng
                  </label>
                  <select
                    value={targetId}
                    onChange={(e) => {
                      setTargetId(e.target.value);
                      const rel = relData?.items?.find((r) => r.id === e.target.value);
                      if (rel) setBranchId(rel.branchId);
                    }}
                    className="w-full text-xs bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-slate-200"
                  >
                    {relData?.items?.map((rel) => (
                      <option key={rel.id} value={rel.id}>
                        {rel.id.slice(0, 8)}... (CN {rel.branchId.slice(0, 8)}...)
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="p-3 bg-slate-950/70 border border-slate-800 rounded-xl text-[11px] text-slate-400 flex items-start gap-2">
                <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                <span>
                  Tệp tải lên sẽ được chuyển ngay vào chế độ cách ly và quét virus bởi <strong>ClamAV (Port 3310)</strong>. Chỉ các tệp đạt <strong>CLEAN</strong> mới được phép đính kèm hồ sơ và cấp quyền tải về.
                </span>
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
                <Button type="button" variant="outline" size="sm" onClick={() => setIsUploadModalOpen(false)}>
                  Hủy bỏ
                </Button>
                <Button type="submit" size="sm" isLoading={uploadMutation.isPending}>
                  Bắt đầu tải lên
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
