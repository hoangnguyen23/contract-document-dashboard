import React from 'react';
import { useAuth } from '@/context/AuthContext';
import { LogOut, Bell, Search, Shield, ChevronRight, Menu } from 'lucide-react';
import { Button } from '@/components/ui/Button';

interface HeaderProps {
  breadcrumbs?: { label: string; href?: string }[];
  onMenuClick?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ breadcrumbs = [{ label: 'Trang chủ' }], onMenuClick }) => {
  const { user, logout } = useAuth();

  return (
    <header className="h-16 bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between px-4 md:px-8 z-10 shrink-0">
      {/* Left: hamburger (mobile) + breadcrumb */}
      <div className="flex items-center gap-3">
        {/* Hamburger — mobile only */}
        <button
          className="md:hidden p-2 rounded-lg text-slate-500 hover:text-white hover:bg-slate-800 transition-colors"
          onClick={onMenuClick}
          aria-label="Mở menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-sm text-slate-500">
          {breadcrumbs.map((crumb, idx) => (
            <React.Fragment key={idx}>
              {idx > 0 && <ChevronRight className="w-4 h-4 text-slate-400" />}
              <span className={idx === breadcrumbs.length - 1 ? 'font-semibold text-slate-900 dark:text-white' : 'hover:text-slate-700'}>
                {crumb.label}
              </span>
            </React.Fragment>
          ))}
        </div>
      </div>

      {/* Right Controls */}
      <div className="flex items-center gap-4">
        {/* Quick Search */}
        <div className="relative w-64 hidden md:block">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Tìm kiếm hợp đồng, hồ sơ..."
            aria-label="Tìm kiếm hợp đồng hoặc hồ sơ"
            className="w-full pl-9 pr-4 py-1.5 text-xs bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </div>

        {/* Notifications Icon Button */}
        <button
          className="relative p-2 text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          title="Thông báo"
          aria-label="Xem thông báo hệ thống"
        >
          <Bell className="w-5 h-5" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-indigo-500 rounded-full"></span>
        </button>

        <div className="h-6 w-px bg-slate-200 dark:bg-slate-800"></div>

        {/* Profile Pill & Logout */}
        <div className="flex items-center gap-3">
          <div className="text-right hidden sm:block">
            <p className="text-xs font-bold text-slate-900 dark:text-white">{user?.fullName || user?.username}</p>
            <div className="flex items-center justify-end gap-1 text-[11px] text-indigo-600 dark:text-indigo-400 font-medium">
              <Shield className="w-3 h-3" />
              <span>{user?.roles?.[0] || 'User'}</span>
            </div>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={() => logout()}
            className="text-slate-500 hover:text-rose-600 dark:text-slate-400 dark:hover:text-rose-400"
            title="Đăng xuất khỏi hệ thống"
          >
            <LogOut className="w-4 h-4" />
            <span className="hidden sm:inline">Đăng xuất</span>
          </Button>
        </div>
      </div>
    </header>
  );
};
