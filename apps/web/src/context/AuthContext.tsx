import React, { createContext, useContext, useEffect, useState } from 'react';
import type { SessionData, User } from '@/types/auth';
import { authApi } from '@/features/auth/api';
import { setCsrfToken, setUnauthenticatedHandler } from '@/lib/api';

interface AuthContextType {
  session: SessionData | null;
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  refreshSession: () => Promise<SessionData>;
  login: (username: string, password: string) => Promise<SessionData>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [session, setSession] = useState<SessionData | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const refreshSession = async (): Promise<SessionData> => {
    try {
      const data = await authApi.getSession();
      setSession(data);
      if (data.csrfToken) {
        setCsrfToken(data.csrfToken);
      }
      return data;
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    // Wire 401 handler: when any API call gets 401, redirect to login
    setUnauthenticatedHandler(() => {
      setSession(null);
      setCsrfToken(null);
      // Use replace to avoid back-button loop
      window.location.replace('/login');
    });

    refreshSession().catch(() => {
      setIsLoading(false);
    });
  }, []);

  const login = async (username: string, password: string): Promise<SessionData> => {
    // If we don't have a session/CSRF token yet, bootstrap it first
    if (!session?.csrfToken) {
      await refreshSession();
    }
    const data = await authApi.login(username, password);
    setSession(data);
    if (data.csrfToken) {
      setCsrfToken(data.csrfToken);
    }
    return data;
  };

  const logout = async (): Promise<void> => {
    try {
      await authApi.logout();
    } finally {
      await refreshSession();
    }
  };

  return (
    <AuthContext.Provider
      value={{
        session,
        user: session?.user ?? null,
        isLoading,
        isAuthenticated: !!session?.authenticated,
        refreshSession,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
