import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { contractApi } from '@/lib/businessApi';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  FileText,
  Search,
  Filter,
  RefreshCw,
  AlertCircle,
  ChevronRight,
} from 'lucide-react';
import type { Contract } from '@/types/business';

export const ContractsPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [selectedContract, setSelectedContract] = useState<Contract | null>(null);

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['contracts-list'],
    queryFn: () => contractApi.list(),
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
            <option value="ACTIVE">ACTIVE (Hiệu lực)</option>
            <option value="PREPARED">PREPARED (Chờ ký)</option>
            <option value="TERMINATED">TERMINATED (Chấm dứt)</option>
            <option value="CLOSED">CLOSED (Thanh lý)</option>
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
                <th className="py-3 px-4">Hồ sơ gốc</th>
                <th className="py-3 px-4">Trạng thái HĐ</th>
                <th className="py-3 px-4">Độ sẵn sàng</th>
                <th className="py-3 px-4">Dịch vụ</th>
                <th className="py-3 px-4">Phiên bản</th>
                <th className="py-3 px-4">Ngày tạo</th>
                <th className="py-3 px-4 text-right">Chi tiết</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={8} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang nạp danh sách hợp đồng...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredContracts.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có hợp đồng nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Hợp đồng được sinh tự động sau khi Hồ sơ thu hộ (Dossier) vượt qua 2 vòng duyệt L1/L2.
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
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      {c.dossierId ? `${c.dossierId.slice(0, 8)}...` : '--'}
                    </td>
                    <td className="py-3.5 px-4">{getStatusBadge(c.state)}</td>
                    <td className="py-3.5 px-4">{getReadinessBadge(c.readiness)}</td>
                    <td className="py-3.5 px-4">
                      <span className="font-semibold text-slate-300">{c.serviceState}</span>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-mono">
                        v{c.version}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {c.createdAt ? new Date(c.createdAt).toLocaleDateString('vi-VN') : '--'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedContract(c)}
                        className="text-xs"
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

      {/* Contract Detail Modal */}
      {selectedContract && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full p-6 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <FileText className="w-5 h-5 text-indigo-400" />
                <h3 className="font-bold text-base text-white">Chi tiết Hợp đồng</h3>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedContract(null)}>
                Đóng
              </Button>
            </div>

            <div className="grid grid-cols-2 gap-4 text-xs">
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã hợp đồng (UUID):</span>
                <span className="font-mono text-indigo-300 select-all">{selectedContract.id}</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Mã hồ sơ gốc:</span>
                <span className="font-mono text-slate-300">{selectedContract.dossierId || 'Không có'}</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Trạng thái:</span>
                {getStatusBadge(selectedContract.state)}
              </div>
              <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Độ sẵn sàng:</span>
                {getReadinessBadge(selectedContract.readiness)}
              </div>
            </div>

            {selectedContract.readinessRisks?.length > 0 && (
              <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs space-y-1">
                <p className="font-bold flex items-center gap-1.5">
                  <AlertCircle className="w-4 h-4" />
                  <span>Rủi ro về độ sẵn sàng:</span>
                </p>
                <ul className="list-disc pl-5 space-y-0.5">
                  {selectedContract.readinessRisks.map((r, idx) => (
                    <li key={idx}>{r}</li>
                  ))}
                </ul>
              </div>
            )}

            <div className="flex justify-end pt-4 border-t border-slate-800">
              <Button variant="secondary" size="sm" onClick={() => setSelectedContract(null)}>
                Đóng cửa sổ
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
