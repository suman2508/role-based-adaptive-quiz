import React from 'react';
import { api } from '@services/api/client';

export default function RolePage() {
  const [roleName, setRoleName] = React.useState('Backend Developer');
  const [topN, setTopN] = React.useState<number>(10);
  const [skills, setSkills] = React.useState<{ skillName: string; priorityScore: number }[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  async function analyzeRole() {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.post('/roles/analyze', { roleName, topN });
      setSkills((data?.skills ?? []).map((s: any) => ({ skillName: s.skillName, priorityScore: s.priorityScore })));
    } catch (e: any) {
      setError(e?.message ?? 'Failed to analyze role');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="grid gap-6">
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <h2 className="text-lg font-semibold mb-3">Analyze Target Role</h2>
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="grid gap-1">
            <label className="text-sm text-gray-600">Role Name</label>
            <input
              value={roleName}
              onChange={(e) => setRoleName(e.target.value)}
              placeholder="e.g., Backend Developer"
              className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            />
          </div>
          <div className="grid gap-1">
            <label className="text-sm text-gray-600">Top-N Skills</label>
            <input
              type="number"
              min={1}
              max={50}
              value={topN}
              onChange={(e) => setTopN(Number(e.target.value))}
              className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            />
          </div>
          <div className="grid items-end">
            <button
              onClick={analyzeRole}
              disabled={loading}
              className="inline-flex items-center justify-center rounded-md bg-brand-600 px-4 py-2 text-white hover:bg-brand-700 transition disabled:opacity-60"
            >
              {loading ? 'Analyzing...' : 'Analyze'}
            </button>
          </div>
        </div>
        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
      </div>

      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="font-semibold mb-3">Extracted Skills</h3>
        {skills.length === 0 ? (
          <p className="text-sm text-gray-600">No skills yet. Analyze a role to see results.</p>
        ) : (
          <ul className="divide-y">
            {skills.map((s, i) => (
              <li key={`${s.skillName}-${i}`} className="py-2 flex items-center justify-between">
                <span>{s.skillName}</span>
                <span className="text-xs rounded bg-brand-50 text-brand-700 px-2 py-1">
                  Priority {s.priorityScore}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  );
}
