import React, { useState, useEffect } from 'react';
import type { FormattedError, ApiProblem } from '@/lib/apiUtils';
import { formatApiError } from '@/lib/apiUtils';
import { AlertCircle, Copy, Check, RefreshCw, X, ShieldAlert, FileWarning } from 'lucide-react';
import { Button } from './Button';
import { Badge } from './Badge';

interface ApiErrorModalProps {
  error: FormattedError | ApiProblem | Error | unknown | null;
  isOpen: boolean;
  onClose: () => void;
  onRefresh?: () => void;
}

export const ApiErrorModal: React.FC<ApiErrorModalProps> = ({
  error,
  isOpen,
  onClose,
  onRefresh,
}) => {
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen || !error) return null;

  const formatted: FormattedError =
    'violations' in (error as object) && 'status' in (error as object) && 'title' in (error as object)
      ? (error as FormattedError)
      : formatApiError(error);

  const handleCopyTrace = () => {
    if (formatted.traceId) {
      navigator.clipboard.writeText(formatted.traceId);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const getStatusBadgeVariant = (status: number) => {
    if (status === 401 || status === 403) return 'rose';
    if (status === 409 || status === 412) return 'amber';
    if (status === 422) return 'rose';
    if (status === 429 || status === 503) return 'amber';
    return 'slate';
  };

  return (
    <div
      className="fixed inset-0 bg-black/70 backdrop-blur-sm z-[250] flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="api-error-title"
    >
      <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full p-6 space-y-5 shadow-2xl animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-start justify-between gap-4 pb-4 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
              {formatted.status === 403 ? (
                <ShieldAlert className="w-5 h-5" />
              ) : formatted.status === 422 ? (
                <FileWarning className="w-5 h-5" />
              ) : (
                <AlertCircle className="w-5 h-5" />
              )}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 id="api-error-title" className="font-bold text-base text-white">
                  {formatted.title}
                </h3>
                <Badge variant={getStatusBadgeVariant(formatted.status)}>
                  HTTP {formatted.status} · {formatted.code}
                </Badge>
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                Quy ước xử lý sự cố RFC9457 & Hợp đồng B1.3.0
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-500 hover:text-white hover:bg-slate-800 transition-colors"
            aria-label="Đóng cửa sổ"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Detail message */}
        <div className="p-3.5 bg-slate-950/70 rounded-xl border border-slate-800/80 text-xs text-slate-200 leading-relaxed break-words">
          {formatted.message}
        </div>

        {/* 412 Stale Version notification */}
        {formatted.status === 412 && (
          <div className="p-3.5 bg-amber-500/10 border border-amber-500/20 rounded-xl text-xs space-y-2">
            <div className="flex items-center justify-between text-amber-300 font-semibold">
              <span>Phiên bản máy chủ hiện tại: v{formatted.currentVersion ?? 'mới hơn'}</span>
              {onRefresh && (
                <Button size="sm" variant="outline" onClick={onRefresh} className="h-7 text-xs gap-1 border-amber-500/40 text-amber-300 hover:bg-amber-500/20">
                  <RefreshCw className="w-3 h-3" />
                  <span>Làm mới dữ liệu</span>
                </Button>
              )}
            </div>
            <p className="text-slate-400 text-[11px]">
              Dữ liệu của bạn không khớp với phiên bản máy chủ (ETag stale). Vui lòng làm mới để đồng bộ trạng thái mới nhất trước khi thực hiện thao tác.
            </p>
          </div>
        )}

        {/* Violations table (422) */}
        {formatted.violations && formatted.violations.length > 0 && (
          <div className="space-y-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
              Chi tiết vi phạm dữ liệu ({formatted.violations.length} trường):
            </span>
            <div className="max-h-48 overflow-y-auto rounded-xl border border-slate-800 bg-slate-950/50">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-800/60 uppercase font-semibold text-slate-400 border-b border-slate-800 text-[10px]">
                  <tr>
                    <th className="py-2 px-3">Vị trí (JSON Pointer)</th>
                    <th className="py-2 px-3">Mã vi phạm</th>
                    <th className="py-2 px-3">Mô tả</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {formatted.violations.map((v, i) => (
                    <tr key={i} className="hover:bg-slate-900/60 font-mono text-[11px]">
                      <td className="py-2 px-3 text-indigo-300 font-semibold">{v.path || '/'}</td>
                      <td className="py-2 px-3 text-amber-400">{v.code}</td>
                      <td className="py-2 px-3 text-slate-300 font-sans">{v.message}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Footer & Trace ID */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-3 border-t border-slate-800">
          <div className="flex items-center gap-2 text-[11px] text-slate-500">
            <span>Trace ID:</span>
            {formatted.traceId ? (
              <button
                onClick={handleCopyTrace}
                className="flex items-center gap-1 font-mono text-slate-400 hover:text-white bg-slate-800/60 px-2 py-0.5 rounded transition-colors"
                title="Sao chép Trace ID vào bộ nhớ tạm"
              >
                <span>{formatted.traceId.slice(0, 16)}...</span>
                {copied ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
              </button>
            ) : (
              <span className="font-mono text-slate-600">Không có</span>
            )}
          </div>

          <div className="flex items-center gap-2 justify-end">
            <Button variant="secondary" size="sm" onClick={onClose}>
              Đóng
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};
