import React from 'react';
import { Card, CardContent } from '@/components/ui/Card';
import { Construction } from 'lucide-react';
import { Badge } from '@/components/ui/Badge';

interface PlaceholderProps {
  title: string;
  description: string;
  phase: string;
}

export const FeaturePlaceholder: React.FC<PlaceholderProps> = ({ title, description, phase }) => {
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">{title}</h2>
          <p className="text-sm text-slate-400">{description}</p>
        </div>
        <Badge variant="indigo">{phase}</Badge>
      </div>

      <Card>
        <CardContent className="p-12 text-center space-y-4">
          <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-500 flex items-center justify-center mx-auto">
            <Construction className="w-6 h-6" />
          </div>
          <div className="space-y-1">
            <h3 className="font-bold text-slate-900 dark:text-white">Giao diện phân hệ sẽ được triển khai ở {phase}</h3>
            <p className="text-xs text-slate-400 max-w-md mx-auto">
              Theo lộ trình tuần tự, các bảng dữ liệu, bộ lọc và form nghiệp vụ của phân hệ này sẽ được xây dựng và kết nối trực tiếp với backend trong giai đoạn kế tiếp.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
