import React from 'react';
import { Routes, Route, Navigate, Link, Outlet } from 'react-router-dom';
import { API_BASE_URL } from '@config/env';

/**
 * Temporary in-file pages and layout to get the app routing working end-to-end.
 * We will split these into feature-based folders (pages/components) in subsequent commits.
 */

function AppLayout() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-10 bg-white/80 backdrop-blur border-b">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 py-3 flex items-center justify-between">
          <Link to="/" className="font-semibold text-lg text-brand-700">
            AI Adaptive Quiz
          </Link>
          <nav className="flex items-center gap-4 text-sm">
            <Link className="hover:text-brand-600" to="/role">Role</Link>
            <Link className="hover:text-brand-600" to="/roadmap">Roadmap</Link>
            <Link className="hover:text-brand-600" to="/quiz">Quiz</Link>
            <Link className="hover:text-brand-600" to="/performance">Performance</Link>
            <Link className="hover:text-brand-600" to="/schedule">Schedule</Link>
          </nav>
        </div>
      </header>
      <main className="flex-1">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 py-6">
          <Outlet />
        </div>
      </main>
      <footer className="border-t bg-white/60">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 py-4 text-xs text-gray-500">
          &copy; {new Date().getFullYear()} AI Role-Based Adaptive Quiz Platform
        </div>
      </footer>
    </div>
  );
}

function LandingPage() {
  return (
    <section className="grid gap-6 lg:grid-cols-2 items-center">
      <div className="space-y-4">
        <h1 className="text-3xl sm:text-4xl font-bold tracking-tight">
          Prepare smarter with AI-driven roadmaps and adaptive quizzes
        </h1>
        <p className="text-gray-600">
          Select your target role, follow a weekly learning plan, practice with adaptive quizzes,
          and track your readiness with rich analytics.
        </p>
        <div className="flex gap-3">
          <Link
            to="/role"
            className="inline-flex items-center justify-center rounded-md bg-brand-600 px-4 py-2 text-white hover:bg-brand-700 transition"
          >
            Get Started
          </Link>
          <Link
            to="/performance"
            className="inline-flex items-center justify-center rounded-md border px-4 py-2 hover:bg-gray-50 transition"
          >
            View Analytics
          </Link>
        </div>
        <ul className="grid gap-2 text-sm text-gray-700 list-disc pl-5">
          <li>AI-generated skill roadmap</li>
          <li>Adaptive quizzes with difficulty progression</li>
          <li>Readiness score and skill gap insights</li>
        </ul>
      </div>
      <div className="rounded-xl border bg-white p-6 shadow-sm">
        <div className="h-56 sm:h-72 bg-gradient-to-br from-brand-100 to-white rounded-lg grid place-items-center">
          <span className="text-brand-700 font-medium">Modern Dashboard Preview</span>
        </div>
      </div>
    </section>
  );
}

function RolePage() {
  const [roleName, setRoleName] = React.useState('Backend Developer');
  const [topN, setTopN] = React.useState<number>(10);
  const [skills, setSkills] = React.useState<{ skillName: string; priorityScore: number }[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  async function analyzeRole() {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE_URL}/roles/analyze`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ roleName, topN })
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
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

function RoadmapPage() {
  return (
    <section className="grid gap-4">
      <h2 className="text-xl font-semibold">Roadmap</h2>
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <p className="text-sm text-gray-600">Weekly learning roadmap will appear here with timeline and progress.</p>
      </div>
    </section>
  );
}

function QuizPage() {
  return (
    <section className="grid gap-4">
      <h2 className="text-xl font-semibold">Quiz</h2>
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <p className="text-sm text-gray-600">Adaptive quiz UI will render here with question, options, and timer.</p>
      </div>
    </section>
  );
}

function PerformancePage() {
  return (
    <section className="grid gap-4">
      <h2 className="text-xl font-semibold">Performance Analytics</h2>
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <p className="text-sm text-gray-600">Charts and readiness score will be presented here.</p>
      </div>
    </section>
  );
}

function SchedulePage() {
  return (
    <section className="grid gap-4">
      <h2 className="text-xl font-semibold">Practice Schedule</h2>
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <p className="text-sm text-gray-600">Your daily practice plan and calendar will be displayed here.</p>
      </div>
    </section>
  );
}

export default function AppRouter() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<LandingPage />} />
        <Route path="role" element={<RolePage />} />
        <Route path="roadmap" element={<RoadmapPage />} />
        <Route path="quiz" element={<QuizPage />} />
        <Route path="performance" element={<PerformancePage />} />
        <Route path="schedule" element={<SchedulePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
