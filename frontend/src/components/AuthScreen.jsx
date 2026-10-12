import React, { useState } from 'react';
import { Search, Mail, Lock, User, ShieldCheck, ArrowRight, GraduationCap, Sparkles } from 'lucide-react';

export default function AuthScreen({ onLoginSuccess }) {
  // 'student' or 'admin'
  const [portalMode, setPortalMode] = useState('student');
  const [isSignUp, setIsSignUp] = useState(false);

  // Sign In Form State (shared by student & admin)
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [loginError, setLoginError] = useState('');
  const [loginLoading, setLoginLoading] = useState(false);

  // Sign Up Form State (students only)
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirmPassword, setRegConfirmPassword] = useState('');
  const [regError, setRegError] = useState('');
  const [regLoading, setRegLoading] = useState(false);

  // Switch to Student Portal
  function switchToStudent() {
    setPortalMode('student');
    setIsSignUp(false);
    setLoginEmail('');
    setLoginPassword('');
    setLoginError('');
  }

  // Switch to Admin Portal
  function switchToAdmin() {
    setPortalMode('admin');
    setIsSignUp(false);
    setLoginEmail('admin@findit.com');
    setLoginPassword('Admin@123');
    setLoginError('');
  }

  // Handle Login
  async function handleLogin(e) {
    e.preventDefault();
    setLoginError('');
    setLoginLoading(true);

    try {
      const emailToSend = loginEmail.trim();
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: emailToSend,
          password: loginPassword
        })
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.error || 'Invalid credentials');
      }

      // Check if admin portal is used with student credentials or vice-versa
      if (portalMode === 'admin' && data.user.role !== 'ADMIN') {
        throw new Error('This account does not have Administrator privileges. Please use the Student Portal.');
      }

      onLoginSuccess(data.user);
    } catch (err) {
      setLoginError(err.message);
    } finally {
      setLoginLoading(false);
    }
  }

  // Handle Register (Students only)
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

  // Quick autofill helpers
  function fillStudentCredentials() {
    setLoginEmail('ritesh@college.edu');
    setLoginPassword('Student@123');
    setLoginError('');
  }

  function fillAdminCredentials() {
    setLoginEmail('admin@findit.com');
    setLoginPassword('Admin@123');
    setLoginError('');
  }

  return (
    <div className="auth-page-wrapper">
      {/* Centered Double-Panel Sliding Auth Card */}
      <div className={`auth-card ${isSignUp && portalMode === 'student' ? 'sign-up-mode' : ''} ${portalMode === 'admin' ? 'admin-mode' : ''}`}>

        {/* ── 1. SIGN IN FORM CONTAINER ─────────────────────────────────── */}
        <div className="form-container sign-in-container">
          <form onSubmit={handleLogin} className="auth-form">

            {/* Portal Switcher Tabs */}
            <div className="auth-portal-selector">
              <button
                type="button"
                className={`portal-select-btn ${portalMode === 'student' ? 'active' : ''}`}
                onClick={switchToStudent}
              >
                <GraduationCap size={15} />
                <span>Student Portal</span>
              </button>
              <button
                type="button"
                className={`portal-select-btn ${portalMode === 'admin' ? 'active' : ''}`}
                onClick={switchToAdmin}
              >
                <ShieldCheck size={15} />
                <span>Admin Portal</span>
              </button>
            </div>

            {/* Header */}
            <div className="auth-header">
              {portalMode === 'admin' ? (
                <>
                  <div className="admin-lock-badge">
                    <ShieldCheck size={16} />
                    <span>Campus Staff Access</span>
                  </div>
                  <h2 className="auth-title admin-title">Admin Sign In</h2>
                  <p className="auth-subtitle">Manage campus claims, items, and verification</p>
                </>
              ) : (
                <>
                  <h2 className="auth-title">Welcome Back</h2>
                  <p className="auth-subtitle">Sign in to your FindIT student account</p>
                </>
              )}
            </div>

            {loginError && (
              <div className="auth-alert error animate-fade-in">
                {loginError}
              </div>
            )}

            {/* Username / Email Field */}
            <div className="input-group">
              <div className="input-icon">
                {portalMode === 'admin' ? <ShieldCheck size={18} /> : <Mail size={18} />}
              </div>
              <input
                type={portalMode === 'admin' ? 'text' : 'email'}
                placeholder={portalMode === 'admin' ? 'Admin Username or Email (admin@findit.com)' : 'College Email (e.g. ritesh@college.edu)'}
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                required
                autoComplete={portalMode === 'admin' ? 'username' : 'email'}
              />
            </div>

            {/* Password Field */}
            <div className="input-group">
              <div className="input-icon">
                <Lock size={18} />
              </div>
              <input
                type="password"
                placeholder={portalMode === 'admin' ? 'Admin Password' : 'Password'}
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
            </div>

            {/* Action Button */}
            <button
              type="submit"
              className={`btn btn-auth-action ${portalMode === 'admin' ? 'btn-admin-action' : ''}`}
              disabled={loginLoading}
            >
              <span>{loginLoading ? 'Authenticating...' : portalMode === 'admin' ? 'SIGN IN AS ADMIN' : 'SIGN IN'}</span>
              <ArrowRight size={16} />
            </button>

            {/* Autofill / Helper notes */}
            {portalMode === 'student' ? (
              <div className="auth-footer-helper">
                <button
                  type="button"
                  className="demo-autofill-link"
                  onClick={fillStudentCredentials}
                >
                  <Sparkles size={12} /> Fill Student Demo (ritesh@college.edu)
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
              </div>
            ) : (
              <div className="auth-footer-helper">
                <button
                  type="button"
                  className="demo-autofill-link admin-autofill"
                  onClick={fillAdminCredentials}
                >
                  <Sparkles size={12} /> Fill Admin Demo (admin@findit.com / Admin@123)
                </button>
                <button
                  type="button"
                  className="portal-back-link"
                  onClick={switchToStudent}
                >
                  ← Back to Student Portal
                </button>
              </div>
            )}
          </form>
        </div>

        {/* ── 2. SIGN UP FORM CONTAINER (Students only) ─────────────────── */}
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

        {/* ── 3. SLIDING OVERLAY CONTAINER ──────────────────────────────── */}
        <div className="overlay-container">
          <div className="overlay">

            {/* Left Overlay Panel (Shown when in Sign Up mode) */}
            <div className="overlay-panel overlay-left">
              <div className="overlay-brand">
                <div className="overlay-brand-icon">
                  <Search size={22} strokeWidth={2.6} />
                </div>
                <span>FIND <strong className="text-highlight">IT</strong></span>
              </div>

              <h1 className="overlay-heading">Welcome Back!</h1>
              <p className="overlay-text">
                To stay connected with your campus lost &amp; found network, please log in with your personal credentials.
              </p>
              <button
                type="button"
                className="btn btn-outline-white"
                onClick={() => setIsSignUp(false)}
              >
                SIGN IN
              </button>
            </div>

            {/* Right Overlay Panel (Shown when in Sign In mode) */}
            <div className="overlay-panel overlay-right">
              <div className="overlay-brand">
                <div className="overlay-brand-icon">
                  <Search size={22} strokeWidth={2.6} />
                </div>
                <span>FIND <strong className="text-highlight">IT</strong></span>
              </div>

              {portalMode === 'admin' ? (
                <>
                  <h1 className="overlay-heading">Campus Administration</h1>
                  <p className="overlay-text">
                    Authorised staff portal to verify claims, monitor recovered assets, and maintain campus safety.
                  </p>
                  <button
                    type="button"
                    className="btn btn-outline-white"
                    onClick={switchToStudent}
                  >
                    STUDENT PORTAL
                  </button>
                </>
              ) : (
                <>
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
                </>
              )}
            </div>

          </div>
        </div>

      </div>
    </div>
  );
}
