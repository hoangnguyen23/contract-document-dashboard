import React from 'react';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import {
  Settings,
  Server,
  ShieldCheck,
  Sliders,
} from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const { session } = useAuth();

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="pb-4 border-b border-slate-200 dark:border-slate-800">
        <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <Settings className="w-5 h-5 text-indigo-500" />
          <span>Cài đặt & Thông số Hệ thống</span>
        </h2>
        <p className="text-xs text-slate-400 mt-1">
          Xem thông số kết nối hạ tầng thực tế, phiên làm việc hiện hành và các quy định bảo mật.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Runtime info */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Server className="w-4 h-4 text-indigo-500" />
              <h3 className="font-bold text-sm text-white">Môi trường Backend Runtime</h3>
            </div>
          </CardHeader>
          <CardContent className="space-y-3.5 text-xs">
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-400">Địa chỉ Backend API:</span>
              <span className="font-mono text-indigo-300">http://127.0.0.1:8081</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-400">Hạ tầng Quét mã độc:</span>
              <span className="font-mono text-emerald-400">ClamAV (Port 3310)</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-400">Cơ sở dữ liệu:</span>
              <span className="font-mono text-slate-200">PostgreSQL 18.6 (Port 56432)</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-400">Nền tảng Java:</span>
              <span className="font-mono text-slate-200">Oracle JDK 25 LTS</span>
            </div>
          </CardContent>
        </Card>

        {/* Security & Session */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-500" />
              <h3 className="font-bold text-sm text-white">Bảo mật Phiên làm việc</h3>
            </div>
          </CardHeader>
          <CardContent className="space-y-3.5 text-xs">
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-400">Tên Cookie phiên:</span>
              <Badge variant="indigo">CG_SESSION</Badge>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-400">Cờ bảo vệ Cookie:</span>
              <span className="font-mono text-emerald-400 font-semibold">HttpOnly, Secure, Lax</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-400">Thời gian hết hạn phiên:</span>
              <span className="font-mono text-slate-200">{session?.expiresAt || 'Không xác định'}</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-400">Giới hạn Rate-limit:</span>
              <span className="font-mono text-amber-400">10 lần/phút/IP</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Unsupported settings alert */}
      <Card>
        <CardContent className="p-6">
          <div className="flex items-start gap-3 text-xs text-slate-400">
            <Sliders className="w-5 h-5 text-indigo-400 shrink-0 mt-0.5" />
            <div className="space-y-1">
              <h4 className="font-bold text-slate-200">Cấu hình tùy chỉnh ứng dụng</h4>
              <p>
                Hệ thống backend hiện đang sử dụng cơ chế nạp cấu hình tập trung từ biến môi trường (`.env`) và tài liệu canonical contract. Các chức năng thay đổi tham số động từ giao diện người dùng hiện <strong>chưa khả dụng (Backend chưa hỗ trợ API cấu hình runtime)</strong> và được bảo vệ theo nguyên tắc bất biến của hệ sinh thái quản trị hợp đồng.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
