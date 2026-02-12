import React from 'react';
import { api } from '@services/api/client';

type ScheduleItem = {
  id?: number;
  userId?: number;
  date: string; // ISO date
  skillId?: number | null;
  skillName?: string | null;
  activityDescription?: string | null;
};

const defaultUserId = 1;

export default function SchedulePage() {
  const [userId] = React.useState<number>(defaultUserId);
  const [start, setStart] = React.useState<string>(''); // YYYY-MM-DD
  const [end, setEnd] = React.useState<string>('');     // YYYY-MM-DD

  const [items, setItems] = React.useState<ScheduleItem[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    setItems([]);
    try {
      const url = `/schedule/${userId}`;
      const params: any = {};
      if (start) params.start = start;
      if (end) params.end = end;

      const { data } = await api.get(url, { params });
      const arr: ScheduleItem[] = Array.isArray(data) ? data : [];
      setItems(arr);
    } catch (e: any) {
      setError(e?.response?.data?.message || e?.message || 'Failed to fetch schedule');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="grid gap-6">
      <header className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Practice Schedule</h2>
        <div className="text-sm text-gray-600">User ID: {userId}</div>
      </header>

      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="font-semibold mb-3">Load Schedule</h3>
        <div className="grid gap-4 sm:grid-cols-5">
          <div className="grid gap-1">
            <label className="text-sm text-gray-600">Start (YYYY-MM-DD)</label>
            <input
              type="date"
              value={start}
              onChange={(e) => setStart(e.target.value)}
              className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            />
          </div>
          <div className="grid gap-1">
            <label className="text-sm text-gray-600">End (YYYY-MM-DD)</label>
            <input
              type="date"
              value={end}
              onChange={(e) => setEnd(e.target.value)}
              className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            />
          </div>
          <div className="grid items-end">
            <button
              onClick={load}
              disabled={loading}
              className="inline-flex items-center justify-center rounded-md bg-brand-600 px-4 py-2 text-white hover:bg-brand-700 transition disabled:opacity-60"
            >
              {loading ? 'Loading...' : 'Get Schedule'}
            </button>
          </div>
          {!start && !end && (
            <div className="sm:col-span-2 self-end text-xs text-gray-500">
              Tip: Leave blank to load the next 14 days by default.
            </div>
          )}
        </div>
        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
      </div>

      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="font-semibold mb-3">Daily Plan</h3>
        {loading ? (
          <p className="text-sm text-gray-600">Loading schedule...</p>
        ) : items.length === 0 ? (
          <p className="text-sm text-gray-600">No schedule loaded. Use the controls above.</p>
        ) : (
          <ul className="divide-y">
            {items.map((it, idx) => (
              <li key={it.id ?? `${it.date}-${idx}`} className="py-3 flex items-start justify-between">
                <div>
                  <div className="text-xs text-gray-500">{it.date} {it.skillName ? '· ' + it.skillName : ''}</div>
                  <div className="text-sm">{it.activityDescription ?? 'Practice session'}</div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  );
}
