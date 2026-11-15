import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '@/lib/governanceApi';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import {
  History,
  Search,
  Filter,
  RefreshCw,
  Download,
} from 'lucide-react';

export const AuditLogsPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [actionFilter, setActionFilter] = useState('ALL');
  const [isExporting, setIsExporting] = useState(false);

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['audit-events-list'],
    queryFn: () => auditApi.listEvents(),
  });

  const handleExportCsv = async () => {
    setIsExporting(true);
    try {
      await auditApi.exportCsv();
    } catch {
      alert('Không thể xuất file CSV nhật ký kiểm toán');
    } finally {
      setIsExporting(false);
    }
  };

  const events = data?.items || [];

  const filteredEvents = events.filter((e) => {
    const matchSearch =
      e.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
      e.resourceType.toLowerCase().includes(searchTerm.toLowerCase()) ||
      e.resourceId.toLowerCase().includes(searchTerm.toLowerCase());
    const matchAction = actionFilter === 'ALL' || e.action === actionFilter;
    return matchSearch && matchAction;
  });

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <History className="w-5 h-5 text-indigo-500" />
            <span>Nhật ký Kiểm toán Hệ thống (Audit Logs)</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Ghi vết bất biến toàn bộ hành vi truy cập, thay đổi hồ sơ, duyệt hợp đồng và thao tác dữ liệu.
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
            onClick={handleExportCsv}
            disabled={isExporting}
            className="gap-1.5"
          >
            <Download className="w-4 h-4" />
            <span>{isExporting ? 'Đang xuất CSV...' : 'Xuất file CSV'}</span>
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
            placeholder="Tìm theo Hành động, Loại tài nguyên, Mã tài nguyên..."
            className="w-full pl-9 pr-4 py-2 text-xs bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-400 shrink-0" />
          <select
            value={actionFilter}
            onChange={(e) => setActionFilter(e.target.value)}
            className="text-xs bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl px-3 py-2 text-slate-700 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="ALL">Tất cả hành động</option>
            <option value="LOGIN">LOGIN</option>
            <option value="CREATE">CREATE</option>
            <option value="UPDATE">UPDATE</option>
            <option value="APPROVE">APPROVE</option>
            <option value="SUBMIT">SUBMIT</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/75 dark:bg-slate-800/60 uppercase font-bold text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="py-3 px-4">Thời gian sự kiện</th>
                <th className="py-3 px-4">Hành động</th>
                <th className="py-3 px-4">Tài nguyên</th>
                <th className="py-3 px-4">Mã tài nguyên</th>
                <th className="py-3 px-4">Người thực hiện (Actor ID)</th>
                <th className="py-3 px-4">Mã Trace ID</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="text-center py-12 text-slate-400">
                    <div className="flex flex-col items-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-indigo-500" />
                      <span>Đang nạp nhật ký kiểm toán...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredEvents.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-12 text-slate-400">
                    <p className="text-sm font-semibold">Chưa có sự kiện kiểm toán nào</p>
                    <p className="text-xs text-slate-500 mt-1">
                      Mọi hành vi thay đổi trạng thái hồ sơ và ký kết đều sẽ ghi nhận vết tại đây.
                    </p>
                  </td>
                </tr>
              ) : (
                filteredEvents.map((evt) => (
                  <tr
                    key={evt.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="py-3.5 px-4 text-slate-300 font-mono text-[11px]">
                      {new Date(evt.occurredAt).toLocaleString('vi-VN')}
                    </td>
                    <td className="py-3.5 px-4 font-mono font-bold text-indigo-400">
                      {evt.action}
                    </td>
                    <td className="py-3.5 px-4 font-semibold text-slate-200">
                      {evt.resourceType}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      {evt.resourceId ? `${evt.resourceId.slice(0, 8)}...` : '--'}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      {evt.actorId ? `${evt.actorId.slice(0, 8)}...` : 'System'}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-[11px] text-slate-500">
                      {evt.traceId ? `${evt.traceId.slice(0, 8)}...` : '--'}
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
