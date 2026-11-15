import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { AppLayout } from './components/layout/AppLayout';
import { LoginPage } from './features/auth/LoginPage';
import React, { Suspense, lazy } from 'react';

// Lazy-load all page components for code splitting
const DashboardPage   = lazy(() => import('./features/dashboard/DashboardPage').then(m => ({ default: m.DashboardPage })));
const ContractsPage   = lazy(() => import('./features/contracts/ContractsPage').then(m => ({ default: m.ContractsPage })));
const DossiersPage    = lazy(() => import('./features/dossiers/DossiersPage').then(m => ({ default: m.DossiersPage })));
const DocumentsPage   = lazy(() => import('./features/documents/DocumentsPage').then(m => ({ default: m.DocumentsPage })));
const ApprovalsPage   = lazy(() => import('./features/approvals/ApprovalsPage').then(m => ({ default: m.ApprovalsPage })));
const NoticesPage     = lazy(() => import('./features/notices/NoticesPage').then(m => ({ default: m.NoticesPage })));
const UsersPage       = lazy(() => import('./features/identity/UsersPage').then(m => ({ default: m.UsersPage })));
const BranchesPage    = lazy(() => import('./features/identity/BranchesPage').then(m => ({ default: m.BranchesPage })));
const AuditLogsPage   = lazy(() => import('./features/audit/AuditLogsPage').then(m => ({ default: m.AuditLogsPage })));
const SettingsPage    = lazy(() => import('./features/settings/SettingsPage').then(m => ({ default: m.SettingsPage })));

function PageLoader() {
  return (
    <div className="flex items-center justify-center h-64 text-slate-400">
      <div className="flex flex-col items-center gap-3">
        <svg className="animate-spin h-6 w-6 text-indigo-500" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
        <span className="text-xs uppercase tracking-wider font-semibold">Đang tải trang...</span>
      </div>
    </div>
  );
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 30_000,
      gcTime: 5 * 60_000,
    },
  },
});

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center text-slate-400">
        <div className="flex flex-col items-center gap-3">
          <svg className="animate-spin h-8 w-8 text-indigo-500" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          <span className="text-xs uppercase tracking-wider font-semibold">Đang nạp phiên làm việc...</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

function AppRoutes() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center text-slate-400">
        <div className="flex flex-col items-center gap-3">
          <svg className="animate-spin h-8 w-8 text-indigo-500" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          <span className="text-xs uppercase tracking-wider font-semibold">Đang khởi tạo bảo mật...</span>
        </div>
      </div>
    );
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />}
      />

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard"  element={<Suspense fallback={<PageLoader />}><DashboardPage /></Suspense>} />
        <Route path="contracts"  element={<Suspense fallback={<PageLoader />}><ContractsPage /></Suspense>} />
        <Route path="dossiers"   element={<Suspense fallback={<PageLoader />}><DossiersPage /></Suspense>} />
        <Route path="documents"  element={<Suspense fallback={<PageLoader />}><DocumentsPage /></Suspense>} />
        <Route path="approvals"  element={<Suspense fallback={<PageLoader />}><ApprovalsPage /></Suspense>} />
        <Route path="notices"    element={<Suspense fallback={<PageLoader />}><NoticesPage /></Suspense>} />
        <Route path="audit-logs" element={<Suspense fallback={<PageLoader />}><AuditLogsPage /></Suspense>} />
        <Route path="users"      element={<Suspense fallback={<PageLoader />}><UsersPage /></Suspense>} />
        <Route path="branches"   element={<Suspense fallback={<PageLoader />}><BranchesPage /></Suspense>} />
        <Route path="settings"   element={<Suspense fallback={<PageLoader />}><SettingsPage /></Suspense>} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <AppRoutes />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
