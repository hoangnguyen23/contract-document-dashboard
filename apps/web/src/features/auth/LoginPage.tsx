import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuth } from '@/context/AuthContext';
import { ShieldCheck, Lock, User, AlertCircle, KeyRound, Info } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Card, CardContent } from '@/components/ui/Card';

const loginSchema = z.object({
  username: z.string().min(1, 'Vui lòng nhập tên đăng nhập'),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      await login(data.username, data.password);
    } catch (err: unknown) {
      const error = err as { message?: string; code?: string };
      setErrorMessage(error.message || 'Đăng nhập không thành công. Vui lòng kiểm tra lại thông tin.');
    } finally {
      setIsLoading(false);
    }
  };

  const setDemoCredentials = (username: string) => {
    setValue('username', username);
    setErrorMessage(null);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center items-center p-6 relative overflow-hidden">
      {/* Background Glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-md w-full relative z-10 space-y-6">
        {/* Brand Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 rounded-2xl bg-indigo-600/10 border border-indigo-500/20 text-indigo-400 mb-2">
            <ShieldCheck className="w-10 h-10" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white">
            Contract Document Dashboard
          </h1>
          <p className="text-sm text-slate-400">
            Hệ thống quản trị hồ sơ và hợp đồng thu hộ doanh nghiệp
          </p>
        </div>

        {/* Login Card */}
        <Card className="bg-slate-900/90 border-slate-800 shadow-2xl backdrop-blur">
          <CardContent className="p-8 space-y-6">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              {errorMessage && (
                <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-start gap-2.5">
                  <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                  <span>{errorMessage}</span>
                </div>
              )}

              <Input
                label="Tên đăng nhập"
                placeholder="Nhập tên tài khoản..."
                icon={<User className="w-4 h-4" />}
                error={errors.username?.message}
                {...register('username')}
              />

              <Input
                label="Mật khẩu"
                type="password"
                placeholder="Nhập mật khẩu..."
                icon={<Lock className="w-4 h-4" />}
                error={errors.password?.message}
                {...register('password')}
              />

              <Button type="submit" className="w-full py-2.5 mt-2" isLoading={isLoading}>
                Đăng nhập hệ thống
              </Button>
            </form>

            {/* Demo Quick Select */}
            <div className="pt-4 border-t border-slate-800 space-y-3">
              <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                <KeyRound className="w-3.5 h-3.5 text-indigo-400" />
                <span>Tài khoản Demo có sẵn (Seed Data):</span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <button
                  type="button"
                  onClick={() => setDemoCredentials('demo.admin')}
                  className="px-3 py-2 rounded-lg bg-slate-800/80 hover:bg-slate-800 text-slate-300 border border-slate-700/60 text-left transition-colors"
                >
                  <span className="font-semibold block text-indigo-400">demo.admin</span>
                  <span className="text-[11px] text-slate-400">Quản trị viên (Admin)</span>
                </button>
                <button
                  type="button"
                  onClick={() => setDemoCredentials('demo.maker')}
                  className="px-3 py-2 rounded-lg bg-slate-800/80 hover:bg-slate-800 text-slate-300 border border-slate-700/60 text-left transition-colors"
                >
                  <span className="font-semibold block text-indigo-400">demo.maker</span>
                  <span className="text-[11px] text-slate-400">Chuyên viên lập (Maker)</span>
                </button>
                <button
                  type="button"
                  onClick={() => setDemoCredentials('demo.checker')}
                  className="px-3 py-2 rounded-lg bg-slate-800/80 hover:bg-slate-800 text-slate-300 border border-slate-700/60 text-left transition-colors"
                >
                  <span className="font-semibold block text-indigo-400">demo.checker</span>
                  <span className="text-[11px] text-slate-400">Kiểm soát viên (Checker)</span>
                </button>
                <button
                  type="button"
                  onClick={() => setDemoCredentials('demo.auditor')}
                  className="px-3 py-2 rounded-lg bg-slate-800/80 hover:bg-slate-800 text-slate-300 border border-slate-700/60 text-left transition-colors"
                >
                  <span className="font-semibold block text-indigo-400">demo.auditor</span>
                  <span className="text-[11px] text-slate-400">Kiểm toán viên (Auditor)</span>
                </button>
              </div>

              <div className="p-3 bg-slate-950/60 rounded-lg border border-slate-800/80 text-[11px] text-slate-400 flex items-start gap-2">
                <Info className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
                <span>
                  Mật khẩu demo được sinh ngẫu nhiên và bảo mật tại biến <code className="text-slate-300">CG_DEMO_PASSWORD</code> trong file <code className="text-slate-300">.env</code>.
                </span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
