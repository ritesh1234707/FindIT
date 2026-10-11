import React from 'react';
import { Search, PlusCircle, Zap, FileText, Shield, LogOut } from 'lucide-react';

export default function Navbar({
  currentView,
  onSwitchView,
  users,
  currentUser,
  onSelectUser,
  stats,
  onLogout
}) {
  return (
    <header className="navbar">
      {/* Brand Logo matching Reunited. style */}
      <div className="nav-left">
        <div className="brand" onClick={() => onSwitchView('feed')}>
          <span className="brand-wordmark">FindIT<span className="brand-dot">.</span></span>
        </div>
      </div>

      {/* Clean Nav Links */}
      <nav className="nav-center">
        <button
          className={`nav-link ${currentView === 'feed' ? 'active' : ''}`}
          onClick={() => onSwitchView('feed')}
        >
          Search Items
        </button>

        <button
          className={`nav-link ${currentView === 'report' ? 'active' : ''}`}
          onClick={() => onSwitchView('report')}
        >
          Report Item
        </button>

        <button
          className={`nav-link ${currentView === 'matches' ? 'active' : ''}`}
          onClick={() => onSwitchView('matches')}
        >
          Smart Matches
        </button>

        <button
          className={`nav-link ${currentView === 'my-items' ? 'active' : ''}`}
          onClick={() => onSwitchView('my-items')}
        >
          Claim Log
        </button>

        {currentUser?.role === 'ADMIN' && (
          <button
            className={`nav-link ${currentView === 'admin' ? 'active' : ''}`}
            onClick={() => onSwitchView('admin')}
          >
            Admin Hub
          </button>
        )}
      </nav>

      {/* Right: Clean profile & demo switch */}
      <div className="nav-right">
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

        <div className="user-pill">
          <span className="user-name">{currentUser?.fullName?.split(' ')[0] || 'User'}</span>
        </div>

        <button
          type="button"
          className="btn-ghost-logout"
          onClick={onLogout}
          title="Sign Out"
        >
          <LogOut size={15} />
        </button>
      </div>
    </header>
  );
}
