import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '@services/api/client';
import { setAuthTokens } from '@app/store/auth';

export default function SignUpPage() {
  const navigate = useNavigate();

  const [name, setName] = React.useState('');
  const [email, setEmail] = React.useState('');
  const [password, setPassword] = React.useState('');
  const [targetRole, setTargetRole] = React.useState('');
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const { data } = await api.post('/auth/register', { name, email, password, targetRole });
      if (!data?.accessToken) throw new Error('Invalid auth response');
      setAuthTokens({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken ?? null,
        userEmail: email,
        userId: typeof data.userId === 'number' ? data.userId : null
      });
      // After sign up, guide user to Role analysis to set/confirm skills for roadmap
      navigate('/role', { replace: true });
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Sign up failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="mx-auto max-w-md">
      <h1 className="text-2xl font-semibold mb-4">Create your account</h1>
      <form onSubmit={onSubmit} className="space-y-4 rounded-lg border bg-white p-6 shadow-sm">
        <div className="grid gap-1">
          <label htmlFor="name" className="text-sm text-gray-600">Full name</label>
          <input
            id="name"
            required
            minLength={2}
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            placeholder="Jane Doe"
          />
        </div>
        <div className="grid gap-1">
          <label htmlFor="email" className="text-sm text-gray-600">Email</label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            placeholder="you@example.com"
          />
        </div>
        <div className="grid gap-1">
          <label htmlFor="password" className="text-sm text-gray-600">Password</label>
          <input
            id="password"
            type="password"
            required
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            placeholder="********"
          />
        </div>
        <div className="grid gap-1">
          <label htmlFor="targetRole" className="text-sm text-gray-600">Target Role (optional)</label>
          <input
            id="targetRole"
            value={targetRole}
            onChange={(e) => setTargetRole(e.target.value)}
            className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            placeholder="Backend Developer"
          />
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="w-full inline-flex items-center justify-center rounded-md bg-brand-600 px-4 py-2 text-white hover:bg-brand-700 transition disabled:opacity-60"
        >
          {loading ? 'Creating account...' : 'Sign up'}
        </button>
        <p className="text-sm text-gray-600">
          Already have an account?{' '}
          <Link to="/signin" className="text-brand-700 hover:underline">Sign in</Link>
        </p>
      </form>
    </section>
  );
}
