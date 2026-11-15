import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/context/AuthContext';
import { dashboardApi } from '@/lib/businessApi';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import {
  CheckCircle2,
  Clock,
  Building,
  AlertTriangle,
  FileCheck,
  RefreshCw,
  BellRing,
  Layers,
} from 'lucide-react';
import { Button } from '@/components/ui/Button';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  const {
    data: metrics,
    isLoading,
    isError,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ['dashboard-metrics'],
    queryFn: () => dashboardApi.getMetrics(),
  });

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 border border-indigo-500/20 rounded-2xl p-6 sm:p-8 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6 shadow-xl">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold uppercase tracking-wider text-indigo-400">Bảng điều hành</span>
            <Badge variant="indigo" className="text-[10px]">Thời gian thực</Badge>
          </div>
          <h2 className="text-2xl font-bold text-white tracking-tight">
            Xin chào, {user?.fullName || user?.username}!
          </h2>
          <p className="text-sm text-slate-400">
            Dữ liệu vận hành hồ sơ và hợp đồng được trích xuất trực tiếp từ Backend.
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
            <span>Làm mới dữ liệu</span>
          </Button>

          <div className="flex items-center gap-3 bg-slate-950/80 border border-slate-800 rounded-xl px-4 py-2.5">
            <Building className="w-4 h-4 text-indigo-400" />
            <div className="text-right">
              <p className="text-[10px] text-slate-500 uppercase font-semibold">Phạm vi Chi nhánh</p>
              <p className="text-xs font-bold text-white">
                {metrics?.branchIds?.length ? `${metrics.branchIds.length} Chi nhánh` : 'Toàn hệ thống'}
              </p>
            </div>
          </div>
        </div>
      </div>

      {isError && (
        <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            <span>Không thể nạp một phần dữ liệu thống kê hoặc vai trò hiện tại chưa có quyền xem báo cáo chi nhánh.</span>
          </div>
          <Button variant="ghost" size="sm" onClick={() => refetch()} className="text-xs">
            Thử lại
          </Button>
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card hover>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Hợp đồng hiệu lực</span>
              <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-500 border border-emerald-500/20">
                <CheckCircle2 className="w-5 h-5" />
              </div>
            </div>
            <div className="mt-4 flex items-baseline gap-2">
              <span className="text-3xl font-extrabold text-slate-900 dark:text-white">
                {isLoading ? '...' : (metrics?.activeAgreements ?? 0)}
              </span>
              <span className="text-xs text-emerald-500 font-semibold">Đang hoạt động</span>
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Sắp hết hạn (30 ngày)</span>
              <div className="p-2.5 rounded-xl bg-amber-500/10 text-amber-500 border border-amber-500/20">
                <Clock className="w-5 h-5" />
              </div>
            </div>
            <div className="mt-4 flex items-baseline gap-2">
              <span className="text-3xl font-extrabold text-slate-900 dark:text-white">
                {isLoading ? '...' : (metrics?.expiring30 ?? 0)}
              </span>
              <span className="text-xs text-amber-500 font-semibold">Cần chú ý gia hạn</span>
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Thẩm định quá hạn</span>
              <div className="p-2.5 rounded-xl bg-rose-500/10 text-rose-500 border border-rose-500/20">
                <AlertTriangle className="w-5 h-5" />
              </div>
            </div>
            <div className="mt-4 flex items-baseline gap-2">
              <span className="text-3xl font-extrabold text-slate-900 dark:text-white">
                {isLoading ? '...' : (metrics?.reviewOverdue ?? 0)}
              </span>
              <span className="text-xs text-rose-500 font-semibold">Vòng duyệt tồn đọng</span>
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Nghĩa vụ quá hạn</span>
              <div className="p-2.5 rounded-xl bg-indigo-500/10 text-indigo-500 border border-indigo-500/20">
                <FileCheck className="w-5 h-5" />
              </div>
            </div>
            <div className="mt-4 flex items-baseline gap-2">
              <span className="text-3xl font-extrabold text-slate-900 dark:text-white">
                {isLoading ? '...' : (metrics?.overdueObligations ?? 0)}
              </span>
              <span className="text-xs text-indigo-500 font-semibold">Cần đôn đốc</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Breakdown by Dossier State & Operations */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <CardHeader className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Layers className="w-5 h-5 text-indigo-500" />
              <h3 className="font-bold text-sm text-slate-900 dark:text-white">
                Phân bổ Trạng thái Hồ sơ thu hộ (Dossiers)
              </h3>
            </div>
            <span className="text-xs text-slate-400">Thời điểm: {metrics?.asOf ? new Date(metrics.asOf).toLocaleTimeString('vi-VN') : '--'}</span>
          </CardHeader>
          <CardContent className="p-6">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              {metrics?.dossiersByState?.map((st) => (
                <div key={st.state} className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-800">
                  <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
                    {st.state}
                  </span>
                  <p className="text-2xl font-extrabold text-slate-900 dark:text-white">{st.count}</p>
                </div>
              )) || (
                <div className="col-span-4 text-center py-6 text-xs text-slate-400">
                  {isLoading ? 'Đang nạp phân bổ hồ sơ...' : 'Chưa có dữ liệu hồ sơ'}
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Operational Indicators */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <BellRing className="w-4 h-4 text-indigo-500" />
              <h3 className="font-bold text-sm text-slate-900 dark:text-white">Cảnh báo Vận hành</h3>
            </div>
          </CardHeader>
          <CardContent className="p-6 space-y-3.5">
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-100 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800">
              <span className="text-xs text-slate-600 dark:text-slate-300">Sự cố vi phạm lịch sử:</span>
              <Badge variant={metrics?.historicalBreaches ? 'rose' : 'slate'}>
                {metrics?.historicalBreaches ?? 0}
              </Badge>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-100 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800">
              <span className="text-xs text-slate-600 dark:text-slate-300">Thông báo gửi thất bại:</span>
              <Badge variant={metrics?.noticeFailures ? 'rose' : 'slate'}>
                {metrics?.noticeFailures ?? 0}
              </Badge>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-100 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800">
              <span className="text-xs text-slate-600 dark:text-slate-300">Yêu cầu kiểm soát đang chờ:</span>
              <Badge variant={metrics?.pendingServiceControls ? 'amber' : 'slate'}>
                {metrics?.pendingServiceControls ?? 0}
              </Badge>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-100 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800">
              <span className="text-xs text-slate-600 dark:text-slate-300">Nghĩa vụ thanh lý tồn đọng:</span>
              <Badge variant={metrics?.unresolvedCloseout ? 'amber' : 'slate'}>
                {metrics?.unresolvedCloseout ?? 0}
              </Badge>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
