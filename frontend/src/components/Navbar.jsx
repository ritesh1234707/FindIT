import React from 'react';

export default function Navbar({
  currentView,
  onSwitchView,
  currentUser,
  onLogout
}) {
  const isAdmin = currentUser?.role === 'ADMIN';

  return (
    <header className="figma-navbar">
      {/* Brand */}
      <div className="figma-nav-left" onClick={() => onSwitchView(isAdmin ? 'admin' : 'home')}>
        <div className="figma-logo-box">
          <span className="box-emoji" role="img" aria-label="Lost & Found Box">📦</span>
          <span className="umbrella-emoji" role="img" aria-label="Umbrella">☂️</span>
        </div>
        <div className="figma-brand-text">
          <div className="brand-title">FIND IT</div>
          <div className="brand-tagline">Discover. Connect. Reclaim.</div>
        </div>
      </div>

      {/* Center Nav Links — different for Student vs Admin */}
      <nav className="figma-nav-center">
        {isAdmin ? (
          <>
            {/* Admin-only navigation */}
            <button
              type="button"
              className={`figma-nav-btn ${currentView === 'admin' ? 'active' : ''}`}
              onClick={() => onSwitchView('admin')}
            >
              Dashboard
            </button>

            <button
              type="button"
              className={`figma-nav-btn ${currentView === 'lost' ? 'active' : ''}`}
              onClick={() => onSwitchView('lost')}
            >
              Lost Items
            </button>

            <button
              type="button"
              className={`figma-nav-btn ${currentView === 'found' ? 'active' : ''}`}
              onClick={() => onSwitchView('found')}
            >
              Found Items
            </button>

            <button
              type="button"
              className={`figma-nav-btn ${currentView === 'matches' ? 'active' : ''}`}
              onClick={() => onSwitchView('matches')}
            >
              Matches
            </button>
          </>
        ) : (
          <>
            {/* Student navigation */}
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
          </>
        )}
      </nav>

      {/* Right: User info & Sign Out */}
      <div className="figma-nav-right">
        <div className="figma-user-info">
          <span className="figma-user-name">{currentUser?.fullName || 'User'}</span>
          <span className="figma-user-role">{isAdmin ? 'Admin' : 'Student'}</span>
        </div>

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
