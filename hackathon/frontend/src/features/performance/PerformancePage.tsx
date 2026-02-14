import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '@services/api/client';
import { useAuthStore } from '@app/store/auth';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  RadarChart,
  Radar,
  PolarAngleAxis,
  PolarGrid,
  PolarRadiusAxis,
  RadialBarChart,
  RadialBar
} from 'recharts';

type SkillPerformance = {
  skillName: string;
  accuracy?: number;   // percentage 0..100
  score?: number;      // fallback if API returns score instead of accuracy
};

type ReadinessResponse = {
  readinessScore?: number; // 0..100
} | number;


function useSkillPerformance(userId: number | null) {
  return useQuery({
    queryKey: ['performance', userId],
    enabled: !!userId,
    queryFn: async () => {
      const { data } = await api.get(`/performance/${userId}`);
      const skills = Array.isArray(data?.skills) ? data.skills : [];
      return skills.map((it: any) => ({
        skillName: it.skillName ?? it.skill ?? 'Unknown',
        // Backend returns accuracy in [0,1]; convert to percent
        accuracy: typeof it.accuracy === 'number'
          ? Math.round(Math.max(0, Math.min(1, it.accuracy)) * 100)
          : (typeof it.score === 'number' ? Math.round(Math.max(0, Math.min(1, it.score)) * 100) : 0)
      })) as SkillPerformance[];
    }
  });
}

function useReadiness(userId: number | null) {
  return useQuery({
    queryKey: ['readiness', userId],
    enabled: !!userId,
    queryFn: async () => {
      const { data } = await api.get(`/performance/readiness-score/${userId}`);
      if (typeof data === 'number') {
        // Backend endpoint returns normalized [0,1]; convert to percent
        return Math.round(Math.max(0, Math.min(1, data)) * 100);
      }
      if (data && typeof data.readinessScore === 'number') {
        const v = data.readinessScore;
        // If normalized, convert; if already percent, keep
        return v <= 1 ? Math.round(v * 100) : Math.round(v);
      }
      return 0;
    }
  });
}

export default function PerformancePage() {
  const userId = useAuthStore((s) => s.userId ?? null);
  const perfQ = useSkillPerformance(userId);
  const readyQ = useReadiness(userId);

  const skills = perfQ.data ?? [];
  const readiness = readyQ.data ?? 0;

  return (
    <section className="grid gap-6">
      <header className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Performance Analytics</h2>
        <div className="text-sm text-gray-600">User ID: {userId ?? '—'}</div>
      </header>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Readiness Gauge */}
        <div className="rounded-lg border bg-white p-4 shadow-sm">
          <h3 className="font-semibold mb-3">Readiness Score</h3>
          {readyQ.isLoading ? (
            <p className="text-sm text-gray-600">Loading readiness...</p>
          ) : readyQ.isError ? (
            <p className="text-sm text-red-600">Failed to load readiness</p>
          ) : (
            <div className="h-60">
              <ResponsiveContainer>
                <RadialBarChart
                  innerRadius="60%"
                  outerRadius="100%"
                  data={[{ name: 'readiness', value: Math.max(0, Math.min(100, readiness)) }]}
                  startAngle={180}
                  endAngle={0}
                >
                  <RadialBar
                    background
                    dataKey="value"
                    fill="#2a7dff"
                    cornerRadius={8}
                  />
                  {/* Center label */}
                  <text x="50%" y="62%" textAnchor="middle" dominantBaseline="middle" className="fill-gray-800" style={{ fontSize: 24, fontWeight: 600 }}>
                    {Math.round(readiness)}%
                  </text>
                </RadialBarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Skill Accuracy Bar Chart */}
        <div className="rounded-lg border bg-white p-4 shadow-sm lg:col-span-2">
          <h3 className="font-semibold mb-3">Skill-wise Accuracy</h3>
          {perfQ.isLoading ? (
            <p className="text-sm text-gray-600">Loading performance...</p>
          ) : perfQ.isError ? (
            <p className="text-sm text-red-600">Failed to load performance</p>
          ) : skills.length === 0 ? (
            <p className="text-sm text-gray-600">No performance data available.</p>
          ) : (
            <div className="h-64">
              <ResponsiveContainer>
                <BarChart data={skills}>
                  <XAxis dataKey="skillName" tick={{ fontSize: 12 }} interval={0} angle={-20} textAnchor="end" height={60} />
                  <YAxis domain={[0, 100]} tickFormatter={(v) => `${v}%`} />
                  <Tooltip formatter={(v: number) => `${v}%`} />
                  <Bar dataKey={(d: SkillPerformance) => d.accuracy ?? d.score ?? 0} name="Accuracy" fill="#2a7dff" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>

      {/* Radar for strength vs weakness */}
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="font-semibold mb-3">Strengths vs Weaknesses</h3>
        {perfQ.isLoading ? (
          <p className="text-sm text-gray-600">Loading...</p>
        ) : perfQ.isError ? (
          <p className="text-sm text-red-600">Failed to load data</p>
        ) : skills.length === 0 ? (
          <p className="text-sm text-gray-600">No data yet.</p>
        ) : (
          <div className="h-72">
            <ResponsiveContainer>
              <RadarChart data={skills}>
                <PolarGrid />
                <PolarAngleAxis dataKey="skillName" />
                <PolarRadiusAxis angle={30} domain={[0, 100]} />
                <Radar
                  name="Proficiency"
                  dataKey={(d: SkillPerformance) => d.accuracy ?? d.score ?? 0}
                  stroke="#184dc0"
                  fill="#8ec3ff"
                  fillOpacity={0.6}
                />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {/* AI Summary placeholder (to be filled from backend endpoint later) */}
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="font-semibold mb-2">AI Summary</h3>
        <p className="text-sm text-gray-600">
          This section will display the AI-generated natural language summary of your performance once the endpoint is connected.
        </p>
      </div>
    </section>
  );
}
