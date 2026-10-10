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
  const activeItemsCount = (stats?.totalLost || 0) + (stats?.totalFound || 0);
  const recoveryRate = stats?.recoveryRate || 0;

  return (
    <header className="navbar">
      <div className="nav-left">
        <div className="brand" onClick={() => onSwitchView('feed')}>
          <div className="brand-icon">
            <Search size={22} strokeWidth={2.5} />
          </div>
          <div className="brand-text">
            <span className="brand-name">Find<span className="highlight">IT</span></span>
            <span className="brand-tag">Campus Recovery Engine</span>
          </div>
        </div>

        <div className="quick-stats-ticker">
          <span className="ticker-pill">
            <strong>{activeItemsCount}</strong> Active Items
          </span>
          <span className="ticker-pill success">
            <strong>{recoveryRate}%</strong> Recovery Rate
          </span>
        </div>
      </div>

      <nav className="nav-center">
        <button
          className={`nav-btn ${currentView === 'feed' ? 'active' : ''}`}
          onClick={() => onSwitchView('feed')}
        >
          <Search size={16} />
          <span>Browse Feed</span>
        </button>

        <button
          className={`nav-btn ${currentView === 'report' ? 'active' : ''}`}
          onClick={() => onSwitchView('report')}
        >
          <PlusCircle size={16} />
          <span>Report Item</span>
        </button>

        <button
          className={`nav-btn ${currentView === 'matches' ? 'active' : ''}`}
          onClick={() => onSwitchView('matches')}
        >
          <Zap size={16} />
          <span>Smart Matches</span>
        </button>

        <button
          className={`nav-btn ${currentView === 'my-items' ? 'active' : ''}`}
          onClick={() => onSwitchView('my-items')}
        >
          <FileText size={16} />
          <span>My Activity</span>
        </button>

        {currentUser?.role === 'ADMIN' && (
          <button
            className={`nav-btn ${currentView === 'admin' ? 'active' : ''}`}
            onClick={() => onSwitchView('admin')}
          >
            <Shield size={16} />
            <span>Admin Hub</span>
          </button>
        )}
      </nav>

      <div className="nav-right">
        <div className="role-switcher-dropdown">
          <span className="role-switcher-label">Demo Switch:</span>
          <select
            className="quick-user-select"
            value={currentUser?.userId || ''}
            onChange={(e) => {
              const selected = users.find(u => u.userId === parseInt(e.target.value));
              if (selected) onSelectUser(selected);
            }}
          >
            {users.map(u => (
              <option key={u.userId} value={u.userId}>
                {u.fullName} ({u.role})
              </option>
            ))}
          </select>
        </div>

        <div className="user-profile-badge">
          <div className="user-avatar">
            {(currentUser?.fullName || 'U').charAt(0).toUpperCase()}
          </div>
          <div className="user-info">
            <span className="user-name">{currentUser?.fullName || 'Guest'}</span>
            <span className={`user-role-badge ${currentUser?.role?.toLowerCase()}`}>
              {currentUser?.role || 'STUDENT'}
            </span>
          </div>
        </div>

        <button
          type="button"
          className="btn-icon"
          onClick={onLogout}
          title="Sign Out"
        >
          <LogOut size={16} />
        </button>
      </div>
    </header>
  );
}
