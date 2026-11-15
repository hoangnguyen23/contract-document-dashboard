import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { identityApi } from '@/lib/governanceApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  Building2,
  Search,
  RefreshCw,
  Building,
  CheckCircle,
} from 'lucide-react';

export const BranchesPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['branches-list'],
    queryFn: () => identityApi.listBranches(),
  });

  const branches = data?.items || [];

  const filteredBranches = branches.filter((b) => {
    return (
      b.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      b.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
      b.id.toLowerCase().includes(searchTerm.toLowerCase())
    );
  });

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Building2 className="w-5 h-5 text-indigo-500" />
            <span>Chi nhánh & Điểm Giao dịch (Branches)</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Danh mục chi nhánh quản lý thu hộ, phân định phạm vi truy cập dữ liệu của từng cán bộ.
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

      {/* Filter */}
      <div className="relative max-w-md">
        <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Tìm theo Mã chi nhánh, Tên chi nhánh..."
          className="w-full pl-9 pr-4 py-2 text-xs bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      {/* Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/75 dark:bg-slate-800/60 uppercase font-bold text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="py-3 px-4">Mã Chi nhánh</th>
                <th className="py-3 px-4">Tên Chi nhánh</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4">Mã định danh (UUID)</th>
                <th className="py-3 px-4">Ngày thiết lập</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang nạp danh sách chi nhánh...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredBranches.length === 0 ? (
                <tr>
                  <td colSpan={5} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có chi nhánh nào phù hợp</p>
                  </td>
                </tr>
              ) : (
                filteredBranches.map((b) => (
                  <tr
                    key={b.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="py-3.5 px-4 font-mono font-bold text-indigo-400">
                      {b.code}
                    </td>
                    <td className="py-3.5 px-4 font-semibold text-slate-200 flex items-center gap-2">
                      <Building className="w-4 h-4 text-slate-500 shrink-0" />
                      <span>{b.name}</span>
                    </td>
                    <td className="py-3.5 px-4">
                      {b.active ? (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                          <CheckCircle className="w-3 h-3" />
                          <span>Đang hoạt động</span>
                        </span>
                      ) : (
                        <Badge variant="rose">Tạm dừng</Badge>
                      )}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400 select-all">
                      {b.id}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {b.createdAt ? new Date(b.createdAt).toLocaleDateString('vi-VN') : '--'}
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
