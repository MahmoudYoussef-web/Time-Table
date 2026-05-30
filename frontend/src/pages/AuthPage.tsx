import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { login, register } from '../api/auth';
import { useAuthStore } from '../store/authStore';

type Tab = 'signin' | 'signup';

/* Timetable data for the decorative grid */
const HOURS = ['8:00','9:00','10:00','11:00','12:00','13:00','14:00','15:00'];
const DAYS_SHORT = ['SAT','SUN','MON','TUE','WED','THU'];
const BLOCKS = [
  { col: 0, row: 0, span: 2, code: 'CS401', light: true },
  { col: 0, row: 4, span: 2, code: 'CS302', light: false },
  { col: 1, row: 1, span: 2, code: 'CS305', light: false },
  { col: 1, row: 5, span: 2, code: 'CS403', light: false },
  { col: 2, row: 0, span: 3, code: 'CS450', light: true },
  { col: 2, row: 4, span: 2, code: 'CS401', light: false },
  { col: 3, row: 1, span: 2, code: 'CS305', light: false },
  { col: 3, row: 4, span: 2, code: 'CS302', light: true },
  { col: 4, row: 0, span: 2, code: 'CS450', light: true },
  { col: 4, row: 3, span: 2, code: 'CS403', light: false },
  { col: 5, row: 2, span: 2, code: 'CS401', light: false },
  { col: 5, row: 5, span: 2, code: 'CS302', light: true },
];
const RH = 44;
const CW = 80;
const LW = 36;

export function AuthPage() {
  const navigate = useNavigate();
  const loginStore = useAuthStore((s) => s.login);
  const [tab, setTab] = useState<Tab>('signin');
  const [loading, setLoading] = useState(false);
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');

  const switchTab = (t: Tab) => { setTab(t); setError(''); };

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const res = await login({ email, password });
      if (!res.success) { setError(res.message); return; }
      loginStore(res.token, res.user);
      toast.success('Welcome back!');
      navigate('/dashboard');
    } catch { setError('Sign in failed. Check your credentials.'); }
    finally { setLoading(false); }
  };

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const res = await register({ email, password, fullName });
      if (!res.success) { setError(res.message); return; }
      loginStore(res.token, res.user);
      toast.success('Account created!');
      navigate('/dashboard');
    } catch { setError('Registration failed.'); }
    finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen flex" style={{ background: 'var(--background)' }}>

      {/* ── LEFT PANEL ────────────────────────────────── */}
      <div
        className="hidden lg:flex flex-col"
        style={{
          width: '58%',
          background: '#0D0D0D',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* Top bar: logo */}
        <div className="flex items-center gap-2.5" style={{ padding: '28px 40px' }}>
          <div style={{
            width: 28, height: 28, borderRadius: 6,
            background: 'rgba(255,255,255,0.10)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            flexShrink: 0,
          }}>
            <svg width="14" height="14" viewBox="0 0 18 18" fill="none">
              <rect x="2" y="2" width="14" height="14" rx="1.5"
                stroke="rgba(255,255,255,0.85)" strokeWidth="1.2" fill="none"/>
              <line x1="2" y1="6.5" x2="16" y2="6.5"
                stroke="rgba(255,255,255,0.85)" strokeWidth="1.2"/>
              <line x1="9" y1="2" x2="9" y2="16"
                stroke="rgba(255,255,255,0.85)" strokeWidth="1.2"/>
              <line x1="6" y1="6.5" x2="6" y2="16"
                stroke="rgba(255,255,255,0.4)" strokeWidth="0.8"/>
              <line x1="12" y1="6.5" x2="12" y2="16"
                stroke="rgba(255,255,255,0.4)" strokeWidth="0.8"/>
            </svg>
          </div>
          <span style={{
            fontSize: 13, fontWeight: 600,
            color: 'rgba(255,255,255,0.85)',
            letterSpacing: '0.01em',
          }}>CampusGrid</span>
        </div>

        {/* Middle: quote */}
        <div style={{
          flex: 1, display: 'flex', flexDirection: 'column',
          justifyContent: 'center', padding: '0 40px 0 40px',
        }}>
          <p style={{
            fontSize: 11, fontWeight: 500, letterSpacing: '0.12em',
            textTransform: 'uppercase', color: 'rgba(255,255,255,0.30)',
            marginBottom: 24,
          }}>Academic Scheduling</p>

          <h1 style={{
            fontFamily: 'Instrument Serif, serif',
            fontSize: 'clamp(52px, 5.5vw, 88px)',
            fontWeight: 400,
            fontStyle: 'italic',
            lineHeight: 0.9,
            letterSpacing: '-0.025em',
            color: 'rgba(255,255,255,0.95)',
            marginBottom: 32,
          }}>
            Scheduling<br />without<br />conflict.
          </h1>

          <p style={{
            fontSize: 14, lineHeight: 1.65,
            color: 'rgba(255,255,255,0.38)',
            maxWidth: 340,
          }}>
            Generate conflict-free timetables for every department,
            room, and instructor — automatically.
          </p>
        </div>

        {/* Bottom: decorative timetable grid */}
        <div style={{ padding: '0 40px 40px', opacity: 0.85 }}>
          <svg
            viewBox={`0 0 ${DAYS_SHORT.length * CW + LW + 4} ${HOURS.length * RH + 20}`}
            style={{ width: '100%', maxWidth: 520, display: 'block' }}
          >
            {DAYS_SHORT.map((d, i) => (
              <text
                key={d}
                x={LW + i * CW + CW / 2}
                y={12}
                textAnchor="middle"
                fontSize="8"
                fontFamily="Geist, sans-serif"
                fontWeight="500"
                letterSpacing="0.1em"
                fill="rgba(255,255,255,0.22)"
              >{d}</text>
            ))}
            {HOURS.map((h, i) => (
              <text
                key={h}
                x={LW - 4}
                y={20 + i * RH + RH / 2 + 4}
                textAnchor="end"
                fontSize="8"
                fontFamily="DM Mono, monospace"
                fill="rgba(255,255,255,0.18)"
              >{h}</text>
            ))}
            {HOURS.map((_, i) => (
              <line
                key={`h${i}`}
                x1={LW} y1={20 + i * RH}
                x2={LW + DAYS_SHORT.length * CW} y2={20 + i * RH}
                stroke="rgba(255,255,255,0.06)" strokeWidth="0.5"
              />
            ))}
            {DAYS_SHORT.map((_, i) => (
              <line
                key={`v${i}`}
                x1={LW + i * CW} y1={20}
                x2={LW + i * CW} y2={20 + HOURS.length * RH}
                stroke="rgba(255,255,255,0.06)" strokeWidth="0.5"
              />
            ))}
            {BLOCKS.map((b, i) => (
              <g key={i}>
                <rect
                  x={LW + b.col * CW + 2}
                  y={20 + b.row * RH + 2}
                  width={CW - 4}
                  height={b.span * RH - 4}
                  rx="3"
                  fill={b.light
                    ? 'rgba(200,184,154,0.55)'
                    : 'rgba(255,255,255,0.11)'}
                />
                <text
                  x={LW + b.col * CW + 8}
                  y={20 + b.row * RH + 16}
                  fontSize="7.5"
                  fontFamily="Geist, sans-serif"
                  fontWeight="600"
                  fill={b.light
                    ? 'rgba(30,20,10,0.75)'
                    : 'rgba(255,255,255,0.65)'}
                >{b.code}</text>
              </g>
            ))}
          </svg>
        </div>

        <div style={{
          position: 'absolute', right: 0, top: 0, bottom: 0, width: 1,
          background: 'rgba(255,255,255,0.05)',
        }} />
      </div>

      {/* ── RIGHT PANEL ───────────────────────────────── */}
      <div style={{
        flex: 1,
        background: 'var(--card)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px 32px',
        minHeight: '100vh',
      }}>
        <div style={{ width: '100%', maxWidth: 360 }}>

          <div style={{
            display: 'flex',
            background: 'var(--muted)',
            borderRadius: 999,
            padding: 3,
            width: 'fit-content',
            margin: '0 auto 40px',
          }}>
            {(['signin', 'signup'] as Tab[]).map((t) => (
              <button
                key={t}
                onClick={() => switchTab(t)}
                style={{
                  padding: '6px 20px',
                  borderRadius: 999,
                  fontSize: 13,
                  fontWeight: 500,
                  cursor: 'pointer',
                  transition: 'all 0.18s ease',
                  background: tab === t ? 'var(--foreground)' : 'transparent',
                  color: tab === t ? 'var(--primary-foreground)' : 'var(--muted-foreground)',
                  border: 'none',
                  outline: 'none',
                }}
              >
                {t === 'signin' ? 'Sign In' : 'Sign Up'}
              </button>
            ))}
          </div>

          <AnimatePresence mode="wait">
            {tab === 'signin' ? (
              <motion.form
                key="signin"
                onSubmit={handleSignIn}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -8 }}
                transition={{ duration: 0.2, ease: 'easeOut' }}
              >
                <p style={{ fontSize: 11, fontWeight: 500, letterSpacing: '0.1em', textTransform: 'uppercase', color: 'var(--muted-foreground)', marginBottom: 8 }}>Welcome Back</p>
                <h2 style={{ fontFamily: 'Instrument Serif, serif', fontSize: 26, fontWeight: 400, lineHeight: 1.2, marginBottom: 36, color: 'var(--foreground)' }}>
                  Sign in to CampusGrid
                </h2>

                <div style={{ marginBottom: 20 }}>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@university.edu"
                    required
                    style={{
                      width: '100%', background: 'transparent',
                      border: 'none', borderBottom: '1px solid var(--border)',
                      height: 44, padding: '0',
                      fontSize: 14, color: 'var(--foreground)',
                      outline: 'none', transition: 'border-color 0.2s',
                      fontFamily: 'inherit',
                    }}
                    onFocus={(e) => e.target.style.borderBottomColor = 'var(--foreground)'}
                    onBlur={(e) => e.target.style.borderBottomColor = 'var(--border)'}
                  />
                </div>

                <div style={{ position: 'relative', marginBottom: 8 }}>
                  <input
                    type={showPw ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Password"
                    required
                    style={{
                      width: '100%', background: 'transparent',
                      border: 'none', borderBottom: '1px solid var(--border)',
                      height: 44, padding: '0 28px 0 0',
                      fontSize: 14, color: 'var(--foreground)',
                      outline: 'none', transition: 'border-color 0.2s',
                      fontFamily: 'inherit',
                    }}
                    onFocus={(e) => e.target.style.borderBottomColor = 'var(--foreground)'}
                    onBlur={(e) => e.target.style.borderBottomColor = 'var(--border)'}
                  />
                  <button type="button" onClick={() => setShowPw(p => !p)} style={{
                    position: 'absolute', right: 0, top: '50%', transform: 'translateY(-50%)',
                    background: 'none', border: 'none', cursor: 'pointer',
                    color: 'var(--muted-foreground)', padding: 4,
                  }}>
                    {showPw ? <EyeOff size={15} /> : <Eye size={15} />}
                  </button>
                </div>

                {error && <p style={{ color: 'var(--destructive)', fontSize: 13, marginTop: 8, marginBottom: 4 }}>{error}</p>}

                <button
                  type="submit"
                  disabled={loading}
                  style={{
                    width: '100%', height: 44, marginTop: 28,
                    background: 'var(--primary)', color: 'var(--primary-foreground)',
                    border: 'none', borderRadius: 6,
                    fontSize: 13, fontWeight: 500, cursor: loading ? 'not-allowed' : 'pointer',
                    opacity: loading ? 0.6 : 1,
                    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                    transition: 'opacity 0.15s',
                    fontFamily: 'inherit',
                  }}
                >
                  {loading ? <Loader2 size={15} className="animate-spin" /> : null}
                  {loading ? 'Signing in\u2026' : 'Sign In'}
                </button>
              </motion.form>
            ) : (
              <motion.form
                key="signup"
                onSubmit={handleSignUp}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -8 }}
                transition={{ duration: 0.2, ease: 'easeOut' }}
              >
                <p style={{ fontSize: 11, fontWeight: 500, letterSpacing: '0.1em', textTransform: 'uppercase', color: 'var(--muted-foreground)', marginBottom: 8 }}>Get Started</p>
                <h2 style={{ fontFamily: 'Instrument Serif, serif', fontSize: 26, fontWeight: 400, lineHeight: 1.2, marginBottom: 36, color: 'var(--foreground)' }}>
                  Create your account
                </h2>

                {[
                  { value: fullName, setter: setFullName, placeholder: 'Full Name', type: 'text', required: true, minLength: undefined },
                  { value: email, setter: setEmail, placeholder: 'you@university.edu', type: 'email', required: true, minLength: undefined },
                  { value: password, setter: setPassword, placeholder: 'Password (min 8 characters)', type: 'password', required: true, minLength: 8 },
                ].map((field, i) => (
                  <div key={i} style={{ marginBottom: 20 }}>
                    <input
                      type={field.type}
                      value={field.value}
                      onChange={(e) => field.setter(e.target.value)}
                      placeholder={field.placeholder}
                      required={field.required}
                      minLength={field.minLength}
                      style={{
                        width: '100%', background: 'transparent',
                        border: 'none', borderBottom: '1px solid var(--border)',
                        height: 44, padding: 0,
                        fontSize: 14, color: 'var(--foreground)',
                        outline: 'none', transition: 'border-color 0.2s',
                        fontFamily: 'inherit',
                      }}
                      onFocus={(e) => e.target.style.borderBottomColor = 'var(--foreground)'}
                      onBlur={(e) => e.target.style.borderBottomColor = 'var(--border)'}
                    />
                  </div>
                ))}

                {error && <p style={{ color: 'var(--destructive)', fontSize: 13, marginBottom: 4 }}>{error}</p>}

                <button
                  type="submit"
                  disabled={loading}
                  style={{
                    width: '100%', height: 44, marginTop: 28,
                    background: 'var(--primary)', color: 'var(--primary-foreground)',
                    border: 'none', borderRadius: 6,
                    fontSize: 13, fontWeight: 500, cursor: loading ? 'not-allowed' : 'pointer',
                    opacity: loading ? 0.6 : 1,
                    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                    transition: 'opacity 0.15s',
                    fontFamily: 'inherit',
                  }}
                >
                  {loading ? <Loader2 size={15} className="animate-spin" /> : null}
                  {loading ? 'Creating account\u2026' : 'Create Account'}
                </button>
              </motion.form>
            )}
          </AnimatePresence>

          <p style={{ textAlign: 'center', fontSize: 13, color: 'var(--muted-foreground)', marginTop: 24 }}>
            {tab === 'signin' ? "Don't have an account? " : 'Already have an account? '}
            <button
              onClick={() => switchTab(tab === 'signin' ? 'signup' : 'signin')}
              style={{ color: 'var(--foreground)', fontWeight: 500, background: 'none', border: 'none', cursor: 'pointer', fontFamily: 'inherit', fontSize: 13 }}
            >
              {tab === 'signin' ? 'Sign Up' : 'Sign In'}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
