import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fileApi } from '@/lib/businessApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  Files,
  Search,
  Filter,
  RefreshCw,
  UploadCloud,
  Download,
  FileText,
} from 'lucide-react';

export const DocumentsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isUploading, setIsUploading] = useState(false);

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['files-list'],
    queryFn: () => fileApi.list(),
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) =>
      fileApi.upload(file, '00000000-0000-4000-8000-000000000001', 'EVIDENCE'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['files-list'] });
      setIsUploading(false);
    },
    onError: (err: unknown) => {
      const error = err as { message?: string };
      alert(`Upload file thất bại: ${error.message || 'Lỗi không xác định'}`);
      setIsUploading(false);
    },
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setIsUploading(true);
      uploadMutation.mutate(e.target.files[0]);
    }
  };

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
        return <Badge variant="indigo">Đã che mờ bảo mật (REDACTED)</Badge>;
      case 'DISPOSED':
        return <Badge variant="slate">Đã tiêu hủy (DISPOSED)</Badge>;
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

          <label className="inline-flex items-center justify-center font-medium rounded-lg text-sm px-4 py-2 gap-2 bg-indigo-600 hover:bg-indigo-700 text-white shadow-sm cursor-pointer select-none transition-all">
            <UploadCloud className="w-4 h-4" />
            <span>{isUploading ? 'Đang tải lên...' : 'Tải lên tài liệu'}</span>
            <input
              type="file"
              onChange={handleFileChange}
              disabled={isUploading}
              className="hidden"
            />
          </label>
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
                      Bấm "Tải lên tài liệu" để gửi file đính kèm hồ sơ và quét virus tự động.
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
    </div>
  );
};
