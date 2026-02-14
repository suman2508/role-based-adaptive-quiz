import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '@services/api/client';
import { getUserId } from '@app/store/auth';

type RoadmapItem = {
  id?: number;
  weekNumber: number;
  skillName?: string;
  description?: string;
  progress?: number; // 0..100 (optional client-side augmentation)
};


export default function RoadmapPage() {
  const userId = (getUserId() as number | null | undefined) ?? null;

  const roadmapQ = useQuery({
    queryKey: ['roadmap', userId],
    enabled: !!userId,
    queryFn: async () => {
      const { data } = await api.get(`/roadmap/${userId}`);
      const items: RoadmapItem[] = Array.isArray(data) ? data : (data?.items ?? []);
      // normalize + add client-side progress placeholder if missing
      return items
        .map((it: any) => ({
          id: it.id ?? undefined,
          weekNumber: it.weekNumber ?? it.week ?? 0,
          skillName: it.skillName ?? it.skill ?? 'Unknown',
          description: it.description ?? '',
          progress: typeof it.progress === 'number' ? it.progress : undefined
        }))
        .sort((a, b) => a.weekNumber - b.weekNumber);
    }
  });

  return (
    <section className="grid gap-6">
      <header className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Weekly Roadmap</h2>
        <div className="text-sm text-gray-600">User ID: {userId ?? '—'}</div>
      </header>

      <div className="rounded-lg border bg-white p-4 shadow-sm">
        {roadmapQ.isLoading ? (
          <p className="text-sm text-gray-600">Loading roadmap...</p>
        ) : roadmapQ.isError ? (
          <p className="text-sm text-red-600">Failed to load roadmap</p>
        ) : (roadmapQ.data ?? []).length === 0 ? (
          <p className="text-sm text-gray-600">No roadmap available. Generate one from backend.</p>
        ) : (
          <ol className="relative border-s pl-6 space-y-6">
            {(roadmapQ.data ?? []).map((item, idx) => (
              <li key={`${item.id ?? idx}-${item.weekNumber}`} className="ms-2">
                <span className="absolute -start-1.5 mt-1.5 h-3 w-3 rounded-full bg-brand-500 ring-4 ring-brand-100"></span>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h3 className="font-medium">
                      Week {item.weekNumber}: {item.skillName}
                    </h3>
                    <p className="text-sm text-gray-600">{item.description}</p>
                  </div>
                  {typeof item.progress === 'number' && (
                    <div className="min-w-[140px]">
                      <div className="flex items-center justify-between text-xs text-gray-600 mb-1">
                        <span>Progress</span>
                        <span>{Math.round(item.progress)}%</span>
                      </div>
                      <div className="h-2 w-full rounded bg-gray-100 overflow-hidden">
                        <div
                          className="h-full bg-brand-500"
                          style={{ width: `${Math.max(0, Math.min(100, item.progress))}%` }}
                        />
                      </div>
                    </div>
                  )}
                </div>
              </li>
            ))}
          </ol>
        )}
      </div>
    </section>
  );
}
