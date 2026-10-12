import React, { useState, useMemo } from 'react';
import { Search, ChevronLeft, ChevronRight, Menu, ArrowRight } from 'lucide-react';

export default function FeedView({
  items = [],
  stats,
  matches = [],
  myItems = [],
  currentUser,
  currentView = 'home',
  onSwitchView,
  onOpenClaimModal,
  onOpenDetailsModal
}) {
  const [search, setSearch] = useState('');

  // Filter items by current page mode (Lost vs Found) and search query
  const displayedItems = useMemo(() => {
    return items.filter(item => {
      if (currentView === 'lost' && item.reportType !== 'LOST') return false;
      if (currentView === 'found' && item.reportType !== 'FOUND') return false;

      if (search.trim()) {
        const q = search.toLowerCase();
        const inTitle = item.title?.toLowerCase().includes(q);
        const inDesc  = item.description?.toLowerCase().includes(q);
        const inLoc   = item.location?.toLowerCase().includes(q);
        const inCat   = item.category?.toLowerCase().includes(q);
        if (!inTitle && !inDesc && !inLoc && !inCat) return false;
      }
      return true;
    });
  }, [items, currentView, search]);

  // Clean date formatter
  function formatDate(dStr) {
    if (!dStr) return 'Recently';
    try {
      const parts = dStr.split('-');
      if (parts.length === 3) {
        const date = new Date(parts[0], parts[1] - 1, parts[2]);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
      }
      return dStr;
    } catch {
      return dStr;
    }
  }

  // Get initial character for card avatar
  function getInitial(item) {
    if (item.title) return item.title.trim().charAt(0).toUpperCase();
    return 'A';
  }

  // ─────────────────────────────────────────────────────────────
  // 1. HOME VIEW (Figma Slide 1)
  // ─────────────────────────────────────────────────────────────
  if (currentView === 'home') {
    return (
      <div className="figma-page-canvas animate-fade-in">
        {/* Hero Section */}
        <section className="figma-hero-section">
          <div className="figma-hero-left">
            <h1 className="figma-hero-headline">
              Find &amp;<br />
              Recover<br />
              <span className="figma-green-gradient">With Ease</span>
            </h1>
            <p className="figma-hero-subtext">
              Experience effortless recovery with our dedicated lost and found service.
            </p>
          </div>

          <div className="figma-hero-right">
            {/* Top Action Buttons: Lost & Found */}
            <div className="figma-hero-buttons-col">
              <button
                type="button"
                className="figma-action-btn btn-lost-coral"
                onClick={() => onSwitchView('lost')}
              >
                <span className="btn-text">Lost</span>
                <span className="btn-icon-box">📦☂️</span>
              </button>

              <button
                type="button"
                className="figma-action-btn btn-found-green"
                onClick={() => onSwitchView('found')}
              >
                <span className="btn-text">Found</span>
                <span className="btn-icon-glass">🔍</span>
              </button>
            </div>

            {/* Collage Preview Below Buttons */}
            <div className="figma-collage-wrap">
              <div className="collage-card card-1">
                <img
                  src="https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=320&q=80"
                  alt="Reviewing notes"
                  className="collage-img"
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
                <div className="collage-fallback-badge">🔎 Inspect</div>
              </div>

              <div className="collage-card card-2">
                <img
                  src="https://images.unsplash.com/photo-1548802673-380ab8ebc7b7?auto=format&fit=crop&w=320&q=80"
                  alt="Lost flyer on tree"
                  className="collage-img"
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
                <div className="collage-fallback-badge">📄 Notice</div>
              </div>

              <div className="collage-card card-3">
                <img
                  src="https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?auto=format&fit=crop&w=320&q=80"
                  alt="Compass navigation"
                  className="collage-img"
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
                <div className="collage-fallback-badge">🧭 Locate</div>
              </div>
            </div>
          </div>
        </section>

        {/* Quick Overview Section */}
        <section className="figma-home-recent-section">
          <div className="section-header-row">
            <div>
              <h2 className="section-headline">Active Campus Reports</h2>
              <p className="section-subline">Browse recent items reported lost or found by students and staff.</p>
            </div>
            <div className="section-quick-links">
              <button
                type="button"
                className="pill-link-btn"
                onClick={() => onSwitchView('lost')}
              >
                All Lost Items <ArrowRight size={14} />
              </button>
              <button
                type="button"
                className="pill-link-btn"
                onClick={() => onSwitchView('found')}
              >
                All Found Items <ArrowRight size={14} />
              </button>
            </div>
          </div>

          <div className="figma-cards-grid">
            {items.slice(0, 4).map((item) => {
              const isLost = item.reportType === 'LOST';
              return (
                <div
                  key={item.itemId}
                  className="figma-item-card"
                  onClick={() => onOpenDetailsModal(item)}
                >
                  {/* Top: Avatar & Date */}
                  <div className="card-top-header">
                    <div className="avatar-circle">
                      {getInitial(item)}
                    </div>
                    <div className="card-author-meta">
                      <span className="author-name">{item.title}</span>
                      <span className="author-date">Date: {formatDate(item.eventDate)}</span>
                    </div>
                  </div>

                  {/* Middle: Shape / Image Placeholder with Carousel Arrows */}
                  <div className="card-visual-stage">
                    <button type="button" className="carousel-arrow arrow-left" onClick={(e) => e.stopPropagation()}>
                      <ChevronLeft size={16} />
                    </button>
                    <div className="geometric-placeholder">
                      <div className="geo-triangle" />
                      <div className="geo-row">
                        <div className="geo-square" />
                        <div className="geo-circle" />
                      </div>
                    </div>
                    <button type="button" className="carousel-arrow arrow-right" onClick={(e) => e.stopPropagation()}>
                      <ChevronRight size={16} />
                    </button>
                  </div>

                  {/* Bottom: Info & Contact Button */}
                  <div className="card-bottom-info">
                    <h3 className="card-item-title">{item.title}</h3>
                    <p className="card-item-location">{item.location || 'Campus Center'}</p>
                    <p className="card-item-desc">
                      {item.description ? item.description.slice(0, 75) + '...' : 'No additional details provided.'}
                    </p>

                    <div className="card-action-bar">
                      <button
                        type="button"
                        className="figma-contact-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          if (!isLost && item.status === 'OPEN' && item.userId !== currentUser?.userId) {
                            onOpenClaimModal(item);
                          } else {
                            onOpenDetailsModal(item);
                          }
                        }}
                      >
                        {!isLost && item.status === 'OPEN' && item.userId !== currentUser?.userId ? 'Claim' : 'Contact'}
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      </div>
    );
  }

  // ─────────────────────────────────────────────────────────────
  // 2. LOST ITEMS OR FOUND ITEMS VIEW (Figma Slide 2 & 3)
  // ─────────────────────────────────────────────────────────────
  const isLostPage = currentView === 'lost';
  const pageTitle = isLostPage ? 'Lost Items' : 'Found Items';

  return (
    <div className="figma-page-canvas animate-fade-in">
      {/* Centered Large Title matching Figma */}
      <div className="figma-title-header">
        <h1 className={`figma-page-hero-title ${isLostPage ? 'title-lost' : 'title-found'}`}>
          {pageTitle}
        </h1>
      </div>

      {/* Search Bar + Report Button Row */}
      <div className="figma-controls-row">
        <div className="figma-pill-search">
          <Menu size={18} className="search-pill-icon menu-icon" />
          <input
            type="text"
            placeholder="Item Name"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <Search size={18} className="search-pill-icon submit-icon" />
        </div>

        {/* Action Button: Coral Report Button (Lost) or Green Report Button (Found) */}
        {isLostPage ? (
          <button
            type="button"
            className="figma-report-btn report-lost-btn"
            onClick={() => onSwitchView('report-lost')}
          >
            <span className="report-btn-label">Report</span>
            <span className="report-btn-emoji">📦☂️</span>
          </button>
        ) : (
          <button
            type="button"
            className="figma-report-btn report-found-btn"
            onClick={() => onSwitchView('report-found')}
          >
            <span className="report-btn-label">Report</span>
            <span className="report-btn-emoji">🔍✓</span>
          </button>
        )}
      </div>

      {/* 4-Column Cards Grid matching Figma */}
      <div className="figma-cards-grid">
        {displayedItems.length === 0 ? (
          <div className="figma-empty-container">
            <p className="empty-heading">No {isLostPage ? 'lost' : 'found'} items listed yet</p>
            <p className="empty-subtext">
              {search ? 'No reports matched your query.' : `Be the first to file a ${isLostPage ? 'lost' : 'found'} report.`}
            </p>
            <button
              type="button"
              className={`figma-report-btn ${isLostPage ? 'report-lost-btn' : 'report-found-btn'}`}
              style={{ margin: '18px auto 0' }}
              onClick={() => onSwitchView(isLostPage ? 'report-lost' : 'report-found')}
            >
              <span className="report-btn-label">Report {isLostPage ? 'Lost' : 'Found'} Item</span>
            </button>
          </div>
        ) : (
          displayedItems.map((item) => {
            const isMine = item.userId === currentUser?.userId;
            const canClaim = !isLostPage && item.status === 'OPEN' && !isMine;

            return (
              <div
                key={item.itemId}
                className="figma-item-card"
                onClick={() => onOpenDetailsModal(item)}
              >
                {/* Top: Avatar Circle & Metadata */}
                <div className="card-top-header">
                  <div className="avatar-circle">
                    {getInitial(item)}
                  </div>
                  <div className="card-author-meta">
                    <span className="author-name">{item.title}</span>
                    <span className="author-date">Date: {formatDate(item.eventDate)}</span>
                  </div>
                </div>

                {/* Middle: Shape / Image Placeholder with Carousel Arrows */}
                <div className="card-visual-stage">
                  <button
                    type="button"
                    className="carousel-arrow arrow-left"
                    onClick={(e) => e.stopPropagation()}
                    aria-label="Previous image"
                  >
                    <ChevronLeft size={16} />
                  </button>

                  <div className="geometric-placeholder">
                    <div className="geo-triangle" />
                    <div className="geo-row">
                      <div className="geo-square" />
                      <div className="geo-circle" />
                    </div>
                  </div>

                  <button
                    type="button"
                    className="carousel-arrow arrow-right"
                    onClick={(e) => e.stopPropagation()}
                    aria-label="Next image"
                  >
                    <ChevronRight size={16} />
                  </button>
                </div>

                {/* Bottom: Info & Contact Button */}
                <div className="card-bottom-info">
                  <h3 className="card-item-title">{item.title}</h3>
                  <p className="card-item-location">{item.location || 'Campus Center'}</p>
                  <p className="card-item-desc">
                    {item.description
                      ? item.description.slice(0, 80) + (item.description.length > 80 ? '...' : '')
                      : 'No additional details provided.'}
                  </p>

                  <div className="card-action-bar">
                    <button
                      type="button"
                      className="figma-contact-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        if (canClaim) {
                          onOpenClaimModal(item);
                        } else {
                          onOpenDetailsModal(item);
                        }
                      }}
                    >
                      {canClaim ? 'Claim' : 'Contact'}
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
