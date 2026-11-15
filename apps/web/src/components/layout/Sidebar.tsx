import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { usePermission } from '@/hooks/usePermission';
import {
  LayoutDashboard,
  FileText,
  Files,
  CheckSquare,
  Bell,
  Users,
  History,
  Settings,
  ShieldCheck,
  Building2,
  FolderGit2,
  Lock,
  X,
} from 'lucide-react';
import { cn } from '@/components/ui/Button';

interface NavItem {
  title: string;
  path: string;
  icon: React.ReactNode;
  show?: boolean;
}

interface NavGroup {
  group: string;
  items: NavItem[];
}

export const Sidebar: React.FC<{ onClose?: () => void }> = ({ onClose }) => {
  const { user } = useAuth();
  const { canManageUsers, canViewAuditLogs, canApprove, isAdmin } = usePermission();

  const navigationGroups: NavGroup[] = [
    {
      group: 'Tổng quan & Điều hành',
      items: [
        { title: 'Bảng điều khiển', path: '/dashboard', icon: <LayoutDashboard className="w-5 h-5" />, show: true },
      ],
    },
    {
      group: 'Nghiệp vụ Hợp đồng',
      items: [
        { title: 'Hồ sơ thu hộ (Dossiers)',  path: '/dossiers',  icon: <FolderGit2 className="w-5 h-5" />,  show: true },
        { title: 'Hợp đồng (Contracts)',      path: '/contracts', icon: <FileText   className="w-5 h-5" />,  show: true },
        { title: 'Quy trình phê duyệt',       path: '/approvals', icon: <CheckSquare className="w-5 h-5" />, show: canApprove || isAdmin },
        { title: 'Quản lý tài liệu (Files)',  path: '/documents', icon: <Files       className="w-5 h-5" />, show: true },
      ],
    },
    {
      group: 'Theo dõi & Giám sát',
      items: [
        { title: 'Thông báo & Văn bản',           path: '/notices',    icon: <Bell    className="w-5 h-5" />, show: true },
        { title: 'Nhật ký kiểm toán (Audit)',      path: '/audit-logs', icon: <History className="w-5 h-5" />, show: canViewAuditLogs },
      ],
    },
    {
      group: 'Hệ thống & Tổ chức',
      items: [
        { title: 'Người dùng & Phân quyền', path: '/users',    icon: <Users     className="w-5 h-5" />, show: canManageUsers },
        { title: 'Chi nhánh & Khách hàng',  path: '/branches', icon: <Building2 className="w-5 h-5" />, show: canManageUsers },
        { title: 'Cài đặt hệ thống',        path: '/settings', icon: <Settings  className="w-5 h-5" />, show: true },
      ],
    },
  ];

  return (
    <aside className="w-72 bg-slate-900 border-r border-slate-800 flex flex-col h-screen select-none shrink-0">
      {/* Brand Header */}
      <div className="h-16 flex items-center justify-between px-6 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-indigo-600 flex items-center justify-center text-white shadow-md shadow-indigo-500/20">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <h1 className="font-bold text-sm tracking-wide text-white leading-tight">CONTRACT DOC</h1>
            <p className="text-[10px] text-slate-400 font-medium tracking-wider uppercase">Enterprise Governance</p>
          </div>
        </div>

        {/* Mobile close button */}
        {onClose && (
          <button
            onClick={onClose}
            className="md:hidden p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
            aria-label="Đóng thanh điều hướng"
          >
            <X className="w-5 h-5" />
          </button>
        )}
      </div>

      {/* Nav Menu */}
      <div className="flex-1 overflow-y-auto px-4 py-6 space-y-6 scrollbar-thin scrollbar-thumb-slate-800">
        {navigationGroups.map((group, idx) => {
          const visible = group.items.filter((i) => i.show !== false);
          if (visible.length === 0) return null;
          return (
            <div key={idx} className="space-y-1.5">
              <h3 className="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                {group.group}
              </h3>
              <div className="space-y-0.5">
                {visible.map((item) => (
                  <NavLink
                    key={item.path}
                    to={item.path}
                    onClick={onClose}
                    className={({ isActive }) =>
                      cn(
                        'flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all group',
                        isActive
                          ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30'
                          : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
                      )
                    }
                  >
                    <span className="text-slate-400 group-[.active]:text-white group-hover:text-white transition-colors shrink-0">
                      {item.icon}
                    </span>
                    <span>{item.title}</span>
                  </NavLink>
                ))}
              </div>
            </div>
          );
        })}
      </div>

      {/* User Mini Bar at Footer */}
      <div className="p-4 border-t border-slate-800 bg-slate-950/40">
        <div className="flex items-center gap-3 p-2 rounded-xl bg-slate-800/40 border border-slate-800">
          <div className="w-9 h-9 rounded-lg bg-indigo-500/20 border border-indigo-500/30 text-indigo-400 flex items-center justify-center font-bold text-xs uppercase">
            {user?.username?.slice(0, 2) || 'US'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-xs font-semibold text-white truncate">{user?.fullName || user?.username || 'Người dùng'}</p>
            <div className="flex items-center gap-1.5 mt-0.5">
              <span className="inline-block w-1.5 h-1.5 rounded-full bg-emerald-400" />
              <span className="text-[10px] text-slate-400 truncate uppercase font-mono">
                {user?.roles?.[0] || 'AUTHENTICATED'}
              </span>
            </div>
          </div>
          <div className="text-slate-500" title="Bảo mật phiên đăng nhập">
            <Lock className="w-4 h-4" />
          </div>
        </div>
      </div>
    </aside>
  );
};
