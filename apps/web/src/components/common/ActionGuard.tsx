import React from 'react';
import type { BlockedAction } from '@/types/business';
import { AlertCircle } from 'lucide-react';

interface ActionGuardProps {
  action: string;
  allowedActions?: string[];
  blockedActions?: BlockedAction[];
  children: (props: {
    isAllowed: boolean;
    isBlocked: boolean;
    blocker?: BlockedAction;
  }) => React.ReactNode;
}

/**
 * Renders action controls according to B §6 contract:
 * - allowedActions: server hint that current user/role/state can perform the action.
 * - blockedActions: server hint that action belongs to workflow but is blocked with reason.
 * - If action is outside (allowedActions ∪ blockedActions), it should NOT be rendered.
 */
export const ActionGuard: React.FC<ActionGuardProps> = ({
  action,
  allowedActions = [],
  blockedActions = [],
  children,
}) => {
  const isAllowed = allowedActions.includes(action);
  const blocker = blockedActions.find((b) => b.action === action);
  const isBlocked = !!blocker;

  // Action is not in the union -> do not render
  if (!isAllowed && !isBlocked) {
    return null;
  }

  return <>{children({ isAllowed, isBlocked, blocker })}</>;
};

interface ActionButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  action: string;
  allowedActions?: string[];
  blockedActions?: BlockedAction[];
  isLoading?: boolean;
  variant?: 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost';
  children: React.ReactNode;
}

export const ActionButton: React.FC<ActionButtonProps> = ({
  action,
  allowedActions = [],
  blockedActions = [],
  isLoading = false,
  variant = 'primary',
  className = '',
  children,
  onClick,
  ...rest
}) => {
  return (
    <ActionGuard action={action} allowedActions={allowedActions} blockedActions={blockedActions}>
      {({ isAllowed, isBlocked, blocker }) => {
        const baseStyle =
          'relative inline-flex items-center justify-center font-medium rounded-xl text-xs transition-all px-3 py-2 gap-1.5 focus:outline-none focus:ring-2 focus:ring-offset-2';

        const variants = {
          primary: 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/20 focus:ring-indigo-500',
          secondary: 'bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 focus:ring-slate-600',
          outline: 'bg-transparent hover:bg-slate-800 text-slate-300 border border-slate-700 focus:ring-slate-600',
          danger: 'bg-rose-600 hover:bg-rose-500 text-white shadow-lg shadow-rose-600/20 focus:ring-rose-500',
          ghost: 'bg-transparent hover:bg-slate-800 text-slate-400 hover:text-white',
        };

        if (isBlocked) {
          return (
            <div className="relative group inline-block">
              <button
                type="button"
                disabled
                className={`${baseStyle} bg-slate-800/60 border border-slate-800 text-slate-500 cursor-not-allowed opacity-60 ${className}`}
                {...rest}
              >
                {children}
                <AlertCircle className="w-3 h-3 text-amber-500/80" />
              </button>

              {/* Blocked tooltip */}
              {blocker && (
                <div className="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 hidden group-hover:flex flex-col p-2 bg-slate-950 border border-slate-800 rounded-lg shadow-xl text-[10px] text-slate-300 w-56 z-50 pointer-events-none">
                  <span className="font-semibold text-amber-400 uppercase tracking-wider mb-0.5">
                    Đang bị chặn ({blocker.code})
                  </span>
                  <span className="leading-tight">{blocker.reason}</span>
                </div>
              )}
            </div>
          );
        }

        return (
          <button
            type="button"
            disabled={isLoading || !isAllowed}
            onClick={onClick}
            className={`${baseStyle} ${variants[variant]} ${isLoading ? 'opacity-70 cursor-wait' : ''} ${className}`}
            {...rest}
          >
            {isLoading && (
              <svg className="animate-spin h-3.5 w-3.5 text-current" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
              </svg>
            )}
            {children}
          </button>
        );
      }}
    </ActionGuard>
  );
};
