import React from 'react';
import { Routes, Route, Navigate, Link, Outlet } from 'react-router-dom';
import ProtectedRoute from '@components/auth/ProtectedRoute';
import SignInPage from '@features/auth/SignInPage';
import RolePage from '@features/role/RolePage';
import RoadmapPage from '@features/roadmap/RoadmapPage';
import PerformancePage from '@features/performance/PerformancePage';
import QuizPage from '@features/quiz/QuizPage';
import SchedulePage from '@features/schedule/SchedulePage';

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

/* Using feature modules for Role page */

/* Using feature modules for Roadmap page */

/* Using feature modules for Quiz page */

/* Using feature modules for Performance page */

/* Using feature modules for Schedule page */

export default function AppRouter() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<LandingPage />} />
        <Route path="role" element={<RolePage />} />
        <Route path="roadmap" element={<ProtectedRoute><RoadmapPage /></ProtectedRoute>} />
        <Route path="quiz" element={<ProtectedRoute><QuizPage /></ProtectedRoute>} />
        <Route path="performance" element={<ProtectedRoute><PerformancePage /></ProtectedRoute>} />
        <Route path="schedule" element={<ProtectedRoute><SchedulePage /></ProtectedRoute>} />
      </Route>
      <Route path="signin" element={<SignInPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
