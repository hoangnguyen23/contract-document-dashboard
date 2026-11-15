import React, { createContext, useCallback, useContext, useRef, useState } from 'react';
import { CheckCircle2, XCircle, AlertTriangle, Info, X } from 'lucide-react';

// ─── Types ─────────────────────────────────────────────────────────────────
export type ToastVariant = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  variant: ToastVariant;
  title: string;
  message?: string;
  duration?: number; // ms, 0 = persistent
}

interface ToastContextType {
  toasts: Toast[];
  toast: (opts: Omit<Toast, 'id'>) => void;
  success: (title: string, message?: string) => void;
  error: (title: string, message?: string) => void;
  warning: (title: string, message?: string) => void;
  info: (title: string, message?: string) => void;
  dismiss: (id: string) => void;
}

// ─── Context ───────────────────────────────────────────────────────────────
const ToastContext = createContext<ToastContextType | undefined>(undefined);

// ─── Provider ──────────────────────────────────────────────────────────────
export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const timers = useRef<Record<string, ReturnType<typeof setTimeout>>>({});

  const dismiss = useCallback((id: string) => {
    clearTimeout(timers.current[id]);
    delete timers.current[id];
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const toast = useCallback(
    ({ duration = 5000, ...opts }: Omit<Toast, 'id'>) => {
      const id = crypto.randomUUID();
      setToasts((prev) => [...prev.slice(-4), { id, duration, ...opts }]); // keep max 5
      if (duration > 0) {
        timers.current[id] = setTimeout(() => dismiss(id), duration);
      }
    },
    [dismiss]
  );

  const success = useCallback((title: string, message?: string) => toast({ variant: 'success', title, message }), [toast]);
  const error   = useCallback((title: string, message?: string) => toast({ variant: 'error',   title, message, duration: 7000 }), [toast]);
  const warning = useCallback((title: string, message?: string) => toast({ variant: 'warning', title, message, duration: 6000 }), [toast]);
  const info    = useCallback((title: string, message?: string) => toast({ variant: 'info',    title, message }), [toast]);

  return (
    <ToastContext.Provider value={{ toasts, toast, success, error, warning, info, dismiss }}>
      {children}
      <ToastContainer toasts={toasts} dismiss={dismiss} />
    </ToastContext.Provider>
  );
};

// ─── Hook ──────────────────────────────────────────────────────────────────
export const useToast = (): ToastContextType => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
};

// ─── Toast Container UI ────────────────────────────────────────────────────
const ICONS: Record<ToastVariant, React.ReactNode> = {
  success: <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />,
  error:   <XCircle      className="w-5 h-5 text-rose-400 shrink-0" />,
  warning: <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0" />,
  info:    <Info          className="w-5 h-5 text-indigo-400 shrink-0" />,
};

const BORDER: Record<ToastVariant, string> = {
  success: 'border-emerald-500/30',
  error:   'border-rose-500/30',
  warning: 'border-amber-500/30',
  info:    'border-indigo-500/30',
};

const BAR: Record<ToastVariant, string> = {
  success: 'bg-emerald-500',
  error:   'bg-rose-500',
  warning: 'bg-amber-500',
  info:    'bg-indigo-500',
};

function ToastContainer({
  toasts,
  dismiss,
}: {
  toasts: Toast[];
  dismiss: (id: string) => void;
}) {
  if (toasts.length === 0) return null;

  return (
    <div
      className="fixed bottom-6 right-6 z-[200] flex flex-col gap-3 w-80 max-w-[calc(100vw-2rem)] pointer-events-none"
      aria-live="polite"
      aria-label="Thông báo hệ thống"
    >
      {toasts.map((t) => (
        <ToastItem key={t.id} toast={t} onDismiss={() => dismiss(t.id)} />
      ))}
    </div>
  );
}

function ToastItem({ toast: t, onDismiss }: { toast: Toast; onDismiss: () => void }) {
  return (
    <div
      className={`
        pointer-events-auto relative overflow-hidden
        bg-slate-900 border ${BORDER[t.variant]}
        rounded-2xl shadow-2xl shadow-black/40
        animate-in slide-in-from-right-5 fade-in duration-300
      `}
      role="alert"
    >
      {/* Top color bar */}
      <div className={`absolute top-0 left-0 right-0 h-0.5 ${BAR[t.variant]}`} />

      <div className="flex items-start gap-3 p-4 pr-10">
        {ICONS[t.variant]}
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold text-white leading-tight">{t.title}</p>
          {t.message && (
            <p className="text-xs text-slate-400 mt-0.5 leading-relaxed break-words">{t.message}</p>
          )}
        </div>
      </div>

      {/* Dismiss button */}
      <button
        onClick={onDismiss}
        className="absolute top-3 right-3 p-1 rounded-lg text-slate-500 hover:text-white hover:bg-slate-800 transition-colors"
        aria-label="Đóng thông báo"
      >
        <X className="w-3.5 h-3.5" />
      </button>
    </div>
  );
}
