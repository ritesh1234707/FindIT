import React from 'react';
import { Package, LogOut, Sparkles } from 'lucide-react';

export default function Navbar({
  currentView,
  onSwitchView,
  users = [],
  currentUser,
  onSelectUser,
  stats,
  onLogout
}) {
  return (
    <header className="figma-navbar">
      {/* Brand: I FOUND with Box Icon & Tagline */}
      <div className="figma-nav-left" onClick={() => onSwitchView('home')}>
        <div className="figma-logo-box">
          <span className="box-emoji" role="img" aria-label="Lost & Found Box">📦</span>
          <span className="umbrella-emoji" role="img" aria-label="Umbrella">☂️</span>
        </div>
        <div className="figma-brand-text">
          <div className="brand-title">I FOUND</div>
          <div className="brand-tagline">Discover. Connect. Reclaim.</div>
        </div>
      </div>

      {/* Center Nav Links matching Figma */}
      <nav className="figma-nav-center">
        <button
          type="button"
          className={`figma-nav-btn ${currentView === 'home' ? 'active' : ''}`}
          onClick={() => onSwitchView('home')}
        >
          Home
        </button>

        <button
          type="button"
          className={`figma-nav-btn ${currentView === 'lost' ? 'active' : ''}`}
          onClick={() => onSwitchView('lost')}
        >
          Lost
        </button>

        <button
          type="button"
          className={`figma-nav-btn ${currentView === 'report-lost' ? 'active' : ''}`}
          onClick={() => onSwitchView('report-lost')}
        >
          Report Lost
        </button>

        <button
          type="button"
          className={`figma-nav-btn ${currentView === 'found' ? 'active' : ''}`}
          onClick={() => onSwitchView('found')}
        >
          Found
        </button>

        <button
          type="button"
          className={`figma-nav-btn ${currentView === 'report-found' ? 'active' : ''}`}
          onClick={() => onSwitchView('report-found')}
        >
          Report Found
        </button>

        <button
          type="button"
          className={`figma-nav-btn ${currentView === 'my-items' ? 'active' : ''}`}
          onClick={() => onSwitchView('my-items')}
        >
          Profile
        </button>

        <button
          type="button"
          className={`figma-nav-btn ${currentView === 'matches' ? 'active' : ''}`}
          onClick={() => onSwitchView('matches')}
        >
          Matches
        </button>

        {currentUser?.role === 'ADMIN' && (
          <button
            type="button"
            className={`figma-nav-btn ${currentView === 'admin' ? 'active' : ''}`}
            onClick={() => onSwitchView('admin')}
          >
            Admin
          </button>
        )}
      </nav>

      {/* Right Controls: Account Demo Switcher & Sign Out */}
      <div className="figma-nav-right">
        {users.length > 0 && (
          <div className="demo-switch-wrap">
            <select
              className="clean-select demo-select"
              value={currentUser?.userId || ''}
              onChange={(e) => {
                const selected = users.find(u => u.userId === parseInt(e.target.value));
                if (selected) onSelectUser(selected);
              }}
              title="Switch demo account"
            >
              {users.map(u => (
                <option key={u.userId} value={u.userId}>
                  {u.fullName.split(' ')[0]} ({u.role})
                </option>
              ))}
            </select>
          </div>
        )}

        <button
          type="button"
          className="figma-signout-btn"
          onClick={onLogout}
        >
          Sign Out
        </button>
      </div>
    </header>
  );
}

