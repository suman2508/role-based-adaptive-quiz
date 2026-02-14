import React from 'react';
import { api } from '@services/api/client';
import { useAuthStore } from '@app/store/auth';

type QuizQuestion = {
  id: number;
  questionText: string;
  options?: string[] | null;
  difficulty?: string;
  skillName?: string;
};

type SubmitResult = {
  questionId: number;
  correct: boolean;
  score: number; // 0..1
  correctAnswer?: string;
  explanation?: string;
};


export default function QuizPage() {
  const userId = useAuthStore((s) => s.userId ?? null);
  const [difficulty, setDifficulty] = React.useState<'easy' | 'medium' | 'hard'>('medium');
  const [limit, setLimit] = React.useState<number>(5);

  const [batch, setBatch] = React.useState<QuizQuestion[]>([]);
  const [loadingBatch, setLoadingBatch] = React.useState(false);

  const [current, setCurrent] = React.useState<QuizQuestion | null>(null);
  const [loadingNext, setLoadingNext] = React.useState(false);

  const [selectedAnswer, setSelectedAnswer] = React.useState<string>('');
  const [submitting, setSubmitting] = React.useState(false);
  const [lastResult, setLastResult] = React.useState<SubmitResult | null>(null);
  const [error, setError] = React.useState<string | null>(null);

  async function startQuiz() {
    if (!userId) { setError('Please sign in to start a quiz.'); return; }
    setLoadingBatch(true);
    setError(null);
    setBatch([]);
    try {
      const { data } = await api.post('/quiz/start', {
        userId,
        difficulty,
        limit
      });
      const arr = Array.isArray(data) ? data : [];
      // Options may be stringified in backend storage; attempt to normalize
      const normalized = arr.map((q: any) => ({
        id: q.id,
        questionText: q.questionText ?? q.question ?? '',
        options: Array.isArray(q.options) ? q.options : (typeof q.options === 'string' ? safeParseArray(q.options) : []),
        difficulty: q.difficulty,
        skillName: q.skillName ?? q.skill
      })) as QuizQuestion[];
      setBatch(normalized);
    } catch (e: any) {
      setError(e?.response?.data?.message || e?.message || 'Failed to start quiz');
    } finally {
      setLoadingBatch(false);
    }
  }

  async function nextAdaptive() {
    if (!userId) { setError('Please sign in to continue.'); return; }
    setLoadingNext(true);
    setError(null);
    setCurrent(null);
    setSelectedAnswer('');
    setLastResult(null);
    try {
      const { data } = await api.get(`/quiz/next-adaptive`, { params: { userId } });
      const q: QuizQuestion = {
        id: data.id,
        questionText: data.questionText ?? data.question ?? '',
        options: Array.isArray(data.options) ? data.options : (typeof data.options === 'string' ? safeParseArray(data.options) : []),
        difficulty: data.difficulty,
        skillName: data.skillName ?? data.skill
      };
      setCurrent(q);
    } catch (e: any) {
      setError(e?.response?.data?.message || e?.message || 'Failed to fetch next question');
    } finally {
      setLoadingNext(false);
    }
  }

  async function submitAnswer() {
    if (!current) return;
    if (!userId) { setError('Please sign in to submit.'); return; }
    if (!selectedAnswer) {
      setError('Please select an answer');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const { data } = await api.post('/quiz/submit', {
        userId,
        questionId: current.id,
        selectedAnswer
      });
      setLastResult(data as SubmitResult);
    } catch (e: any) {
      setError(e?.response?.data?.message || e?.message || 'Failed to submit answer');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="grid gap-6">
      <header className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Adaptive Quiz</h2>
        <div className="text-sm text-gray-600">User ID: {userId ?? '—'}</div>
      </header>

      {/* Start batch controls */}
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="font-semibold mb-3">Start a Quiz Batch</h3>
        <div className="grid gap-4 sm:grid-cols-4">
          <div className="grid gap-1">
            <label className="text-sm text-gray-600">Difficulty</label>
            <select
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value as any)}
              className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            >
              <option value="easy">Easy</option>
              <option value="medium">Medium</option>
              <option value="hard">Hard</option>
            </select>
          </div>
          <div className="grid gap-1">
            <label className="text-sm text-gray-600">Limit</label>
            <input
              type="number"
              min={1}
              max={20}
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value))}
              className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
            />
          </div>
          <div className="grid items-end">
            <button
              onClick={startQuiz}
              disabled={loadingBatch}
              className="inline-flex items-center justify-center rounded-md bg-brand-600 px-4 py-2 text-white hover:bg-brand-700 transition disabled:opacity-60"
            >
              {loadingBatch ? 'Loading...' : 'Start Quiz'}
            </button>
          </div>
        </div>

        {/* Batch questions list */}
        <div className="mt-4">
          {loadingBatch ? (
            <p className="text-sm text-gray-600">Fetching questions...</p>
          ) : batch.length === 0 ? (
            <p className="text-sm text-gray-600">No batch loaded.</p>
          ) : (
            <ul className="divide-y">
              {batch.map((q) => (
                <li key={q.id} className="py-3">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="text-xs text-gray-500">{q.skillName} · {q.difficulty}</div>
                      <p className="font-medium">{q.questionText}</p>
                    </div>
                  </div>
                  {Array.isArray(q.options) && q.options.length > 0 && (
                    <ul className="mt-2 grid gap-2">
                      {q.options.map((opt, idx) => (
                        <li key={idx} className="text-sm text-gray-700">• {opt}</li>
                      ))}
                    </ul>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {/* Adaptive flow */}
      <div className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="font-semibold mb-3">Adaptive Flow</h3>
        <div className="flex items-center gap-3">
          <button
            onClick={nextAdaptive}
            disabled={loadingNext}
            className="inline-flex items-center justify-center rounded-md bg-brand-600 px-4 py-2 text-white hover:bg-brand-700 transition disabled:opacity-60"
          >
            {loadingNext ? 'Loading...' : 'Next Adaptive Question'}
          </button>
          {current && (
            <button
              onClick={() => { setCurrent(null); setSelectedAnswer(''); setLastResult(null); }}
              className="inline-flex items-center justify-center rounded-md border px-3 py-2 hover:bg-gray-50 transition"
            >
              Clear
            </button>
          )}
        </div>

        {current && (
          <div className="mt-4 grid gap-3">
            <div className="text-xs text-gray-500">{current.skillName} · {current.difficulty}</div>
            <p className="font-medium">{current.questionText}</p>
            {Array.isArray(current.options) && current.options.length > 0 ? (
              <div className="grid gap-2">
                {current.options.map((opt, idx) => (
                  <label key={idx} className="inline-flex items-center gap-2 text-sm">
                    <input
                      type="radio"
                      name="answer"
                      value={opt}
                      checked={selectedAnswer === opt}
                      onChange={(e) => setSelectedAnswer(e.target.value)}
                    />
                    <span>{opt}</span>
                  </label>
                ))}
              </div>
            ) : (
              <input
                className="rounded-md border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-300"
                placeholder="Type your answer"
                value={selectedAnswer}
                onChange={(e) => setSelectedAnswer(e.target.value)}
              />
            )}

            <div className="flex items-center gap-3">
              <button
                onClick={submitAnswer}
                disabled={submitting || !selectedAnswer}
                className="inline-flex items-center justify-center rounded-md bg-brand-600 px-4 py-2 text-white hover:bg-brand-700 transition disabled:opacity-60"
              >
                {submitting ? 'Submitting...' : 'Submit'}
              </button>
            </div>

            {lastResult && (
              <div className={`mt-2 rounded border p-3 ${lastResult.correct ? 'border-green-300 bg-green-50' : 'border-red-300 bg-red-50'}`}>
                <div className="font-medium">{lastResult.correct ? 'Correct!' : 'Incorrect'}</div>
                {!lastResult.correct && lastResult.correctAnswer && (
                  <div className="text-sm">Correct answer: <span className="font-mono">{lastResult.correctAnswer}</span></div>
                )}
                {lastResult.explanation && (
                  <div className="text-sm text-gray-700 mt-1">{lastResult.explanation}</div>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      {error && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
          {error}
        </div>
      )}
    </section>
  );
}

function safeParseArray(s: string | null | undefined): string[] {
  if (!s) return [];
  try {
    const v = JSON.parse(s);
    return Array.isArray(v) ? v : [];
  } catch {
    return [];
  }
}
