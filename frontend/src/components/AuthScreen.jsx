import React, { useState } from 'react';
import { Search, Mail, Lock, User, ShieldCheck, ArrowRight, Sparkles } from 'lucide-react';

export default function AuthScreen({ onLoginSuccess }) {
  const [isSignUp, setIsSignUp] = useState(false);

  // Sign In Form State
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [loginError, setLoginError] = useState('');
  const [loginLoading, setLoginLoading] = useState(false);

  // Sign Up Form State
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirmPassword, setRegConfirmPassword] = useState('');
  const [regError, setRegError] = useState('');
  const [regLoading, setRegLoading] = useState(false);

  // Handle Login
  async function handleLogin(e) {
    e.preventDefault();
    setLoginError('');
    setLoginLoading(true);

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: loginEmail.trim(),
          password: loginPassword
        })
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.error || 'Invalid credentials');
      }

      onLoginSuccess(data.user);
    } catch (err) {
      setLoginError(err.message);
    } finally {
      setLoginLoading(false);
    }
  }

  // Handle Register
  async function handleRegister(e) {
    e.preventDefault();
    setRegError('');

    if (regPassword !== regConfirmPassword) {
      setRegError('Passwords do not match.');
      return;
    }

    setRegLoading(true);

    try {
      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fullName: regName.trim(),
          email: regEmail.trim(),
          password: regPassword,
          confirmPassword: regConfirmPassword
        })
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.error || 'Registration failed');
      }

      onLoginSuccess(data.user);
    } catch (err) {
      setRegError(err.message);
    } finally {
      setRegLoading(false);
    }
  }

  // Quick Demo Auto-Fill Handlers
  function fillStudentDemo() {
    setIsSignUp(false);
    setLoginEmail('ritesh@college.edu');
    setLoginPassword('Student@123');
    setLoginError('');
  }

  function fillAdminDemo() {
    setIsSignUp(false);
    setLoginEmail('admin@findit.com');
    setLoginPassword('Admin@123');
    setLoginError('');
  }

  return (
    <div className="auth-page-wrapper">
      {/* Centered Double-Panel Sliding Auth Card */}
      <div className={`auth-card ${isSignUp ? 'sign-up-mode' : ''}`}>

        {/* ── 1. SIGN IN FORM CONTAINER ─────────────────────────────────── */}
        <div className="form-container sign-in-container">
          <form onSubmit={handleLogin} className="auth-form">
            <div className="auth-header">
              <h2 className="auth-title">Welcome Back</h2>
              <p className="auth-subtitle">Sign in to your FindIT campus account</p>
            </div>

            {/* Quick Demo Credentials Bar */}
            <div className="quick-demo-box">
              <span className="demo-label">
                <Sparkles size={13} className="sparkle-icon" /> 1-Click Demo Fill:
              </span>
              <div className="demo-chips">
                <button
                  type="button"
                  className="demo-chip student"
                  onClick={fillStudentDemo}
                  title="Fill Student account credentials"
                >
                  Student (Ritesh)
                </button>
                <button
                  type="button"
                  className="demo-chip admin"
                  onClick={fillAdminDemo}
                  title="Fill Admin account credentials"
                >
                  Admin
                </button>
              </div>
            </div>

            {loginError && (
              <div className="auth-alert error animate-fade-in">
                {loginError}
              </div>
            )}

            <div className="input-group">
              <div className="input-icon">
                <Mail size={18} />
              </div>
              <input
                type="email"
                placeholder="College Email"
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <div className="input-icon">
                <Lock size={18} />
              </div>
              <input
                type="password"
                placeholder="Password"
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-auth-action"
              disabled={loginLoading}
            >
              <span>{loginLoading ? 'Signing In...' : 'SIGN IN'}</span>
              <ArrowRight size={16} />
            </button>

            <div className="mobile-toggle-hint">
              <span>Don't have an account?</span>
              <button
                type="button"
                className="text-toggle-btn"
                onClick={() => setIsSignUp(true)}
              >
                Create Account
              </button>
            </div>
          </form>
        </div>

        {/* ── 2. SIGN UP FORM CONTAINER ─────────────────────────────────── */}
        <div className="form-container sign-up-container">
          <form onSubmit={handleRegister} className="auth-form">
            <div className="auth-header">
              <h2 className="auth-title">Create Account</h2>
              <p className="auth-subtitle">Register to track and recover campus belongings</p>
            </div>

            {regError && (
              <div className="auth-alert error animate-fade-in">
                {regError}
              </div>
            )}

            <div className="input-group">
              <div className="input-icon">
                <User size={18} />
              </div>
              <input
                type="text"
                placeholder="Full Name (e.g. Ritesh Kumar)"
                value={regName}
                onChange={(e) => setRegName(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <div className="input-icon">
                <Mail size={18} />
              </div>
              <input
                type="email"
                placeholder="College Email Address"
                value={regEmail}
                onChange={(e) => setRegEmail(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <div className="input-icon">
                <Lock size={18} />
              </div>
              <input
                type="password"
                placeholder="Password (min 6 chars)"
                value={regPassword}
                onChange={(e) => setRegPassword(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <div className="input-icon">
                <ShieldCheck size={18} />
              </div>
              <input
                type="password"
                placeholder="Confirm Password"
                value={regConfirmPassword}
                onChange={(e) => setRegConfirmPassword(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-auth-action"
              disabled={regLoading}
            >
              <span>{regLoading ? 'Creating...' : 'SIGN UP'}</span>
              <ArrowRight size={16} />
            </button>

            <div className="mobile-toggle-hint">
              <span>Already registered?</span>
              <button
                type="button"
                className="text-toggle-btn"
                onClick={() => setIsSignUp(false)}
              >
                Sign In
              </button>
            </div>
          </form>
        </div>

        {/* ── 3. SLIDING OVERLAY CONTAINER (Teal / Indigo Gradient) ───── */}
        <div className="overlay-container">
          <div className="overlay">

            {/* Left Overlay Panel (Shows when in Sign Up mode) */}
            <div className="overlay-panel overlay-left">
              <div className="overlay-brand">
                <div className="overlay-brand-icon">
                  <Search size={22} strokeWidth={2.6} />
                </div>
                <span>Find<strong className="text-highlight">IT</strong></span>
              </div>

              <h1 className="overlay-heading">Welcome Back!</h1>
              <p className="overlay-text">
                To stay connected with your campus lost & found network, please log in with your credentials.
              </p>
              <button
                type="button"
                className="btn btn-outline-white"
                onClick={() => setIsSignUp(false)}
              >
                SIGN IN
              </button>
            </div>

            {/* Right Overlay Panel (Shows when in Sign In mode) */}
            <div className="overlay-panel overlay-right">
              <div className="overlay-brand">
                <div className="overlay-brand-icon">
                  <Search size={22} strokeWidth={2.6} />
                </div>
                <span>Find<strong className="text-highlight">IT</strong></span>
              </div>

              <h1 className="overlay-heading">Hello, Student!</h1>
              <p className="overlay-text">
                Lost something on campus or found someone else's belonging? Join FindIT to reconnect items instantly.
              </p>
              <button
                type="button"
                className="btn btn-outline-white"
                onClick={() => setIsSignUp(true)}
              >
                SIGN UP
              </button>
            </div>

          </div>
        </div>

      </div>
    </div>
  );
}
