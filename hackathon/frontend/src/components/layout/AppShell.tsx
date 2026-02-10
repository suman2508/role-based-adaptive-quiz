import React from 'react';
import { Link, NavLink } from 'react-router-dom';
import clsx from 'clsx';

const navItems = [
  { to: '/', label: 'Home', end: true },
  { to: '/role', label: 'Role' },
  { to: '/roadmap', label: 'Roadmap' },
  { to: '/quiz', label: 'Quiz' },
  { to: '/performance', label: 'Performance' },
  { to: '/schedule', label: 'Schedule' }
];

export default function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-10 bg-white/80 backdrop-blur border-b">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 py-3 flex items-center justify-between">
          <Link to="/" className="font-semibold text-lg text-brand-700">
            AI Adaptive Quiz
          </Link>
          <nav className="flex items-center gap-2 text-sm">
            {navItems.map((n) => (
              <NavLink
                key={n.to}
                to={n.to}
                end={n.end as boolean | undefined}
                className={({ isActive }) =>
                  clsx(
                    'px-3 py-1.5 rounded-md hover:text-brand-600',
                    isActive ? 'bg-brand-50 text-brand-700' : 'text-gray-700'
                  )
                }
              >
                {n.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main className="flex-1">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 py-6">{children}</div>
      </main>
      <footer className="border-t bg-white/60">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 py-4 text-xs text-gray-500">
          &copy; {new Date().getFullYear()} AI Role-Based Adaptive Quiz Platform
        </div>
      </footer>
    </div>
  );
}
