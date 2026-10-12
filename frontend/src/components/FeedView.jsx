import React, { useState, useMemo } from 'react';
import { Search, ChevronLeft, ChevronRight, Menu, ArrowRight, MapPin, X, PlusCircle } from 'lucide-react';

export default function FeedView({
  items = [],
  stats,
  matches = [],
  myItems = [],
  currentUser,
  currentView = 'home',
  onSwitchView,
  onOpenClaimModal,
  onOpenDetailsModal,
  onEditItem,
  onDeleteItem
}) {
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  const categories = [
    'ALL',
    'ID Card',
    'Earbuds / Headphones',
    'Phone / Electronics',
    'Bag / Backpack',
    'Calculator',
    'Keys / Keychain',
    'Water Bottle',
    'Wallet / Purse',
    'Books / Notes',
    'Other'
  ];

  // Map category to a tasteful icon/emoji
  function getCategoryVisual(category, title = '') {
    const c = (category || '').toLowerCase();
    const t = (title || '').toLowerCase();
    if (c.includes('earbud') || c.includes('headphone') || t.includes('jbl') || t.includes('bud')) return '🎧';
    if (c.includes('phone') || c.includes('electronic')) return '📱';
    if (c.includes('id') || c.includes('card')) return '🪪';
    if (c.includes('bag') || c.includes('backpack')) return '🎒';
    if (c.includes('calculator')) return '🔢';
    if (c.includes('water') || c.includes('bottle')) return '🍶';
    if (c.includes('key')) return '🔑';
    if (c.includes('wallet') || c.includes('purse')) return '👛';
    if (c.includes('book') || c.includes('notes')) return '📖';
    if (t.includes('watch')) return '⌚';
    if (t.includes('cap') || t.includes('hat')) return '🧢';
    return '📦';
  }

  // Filter items by current page mode (Home vs Lost vs Found), category, and search query
  const displayedItems = useMemo(() => {
    return items.filter(item => {
      if (currentView === 'lost' && item.reportType !== 'LOST') return false;
      if (currentView === 'found' && item.reportType !== 'FOUND') return false;
      if (selectedCategory !== 'ALL' && item.category !== selectedCategory) return false;

      if (search.trim()) {
        const q = search.toLowerCase();
        const inTitle = item.title?.toLowerCase().includes(q);
        const inDesc  = item.description?.toLowerCase().includes(q);
        const inLoc   = item.location?.toLowerCase().includes(q);
        const inCat   = item.category?.toLowerCase().includes(q);
        const inBrand = item.brand?.toLowerCase().includes(q);
        if (!inTitle && !inDesc && !inLoc && !inCat && !inBrand) return false;
      }
      return true;
    });
  }, [items, currentView, selectedCategory, search]);

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

  const isAdmin = currentUser?.role === 'ADMIN';

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
              Experience effortless recovery with our smart campus lost &amp; found network.
            </p>

            {/* Quick stats in Hero */}
            <div style={{ display: 'flex', gap: '20px', marginTop: '24px' }}>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '24px', fontWeight: 800, color: '#059669' }}>
                  {stats?.totalItems || items.length}
                </span>
                <span style={{ fontSize: '12px', color: '#64748B' }}>Total Reports</span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '24px', fontWeight: 800, color: '#2563EB' }}>
                  {stats?.returned || 0}
                </span>
                <span style={{ fontSize: '12px', color: '#64748B' }}>Returned Safely</span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '24px', fontWeight: 800, color: '#D97706' }}>
                  {stats?.recoveryRate || 0}%
                </span>
                <span style={{ fontSize: '12px', color: '#64748B' }}>Recovery Rate</span>
              </div>
            </div>
          </div>

          <div className="figma-hero-right">
            {/* Top Action Buttons: Lost & Found */}
            <div className="figma-hero-buttons-col">
              <button
                type="button"
                className="figma-action-btn btn-lost-coral"
                onClick={() => onSwitchView('lost')}
              >
                <span className="btn-text">Lost Items</span>
                <span className="btn-icon-box">📦☂️</span>
              </button>

              <button
                type="button"
                className="figma-action-btn btn-found-green"
                onClick={() => onSwitchView('found')}
              >
                <span className="btn-text">Found Items</span>
                <span className="btn-icon-glass">🔍</span>
              </button>
            </div>

            {/* Collage Preview Below Buttons */}
            <div className="figma-collage-wrap">
              <div className="collage-card card-1" onClick={() => onSwitchView('lost')} style={{ cursor: 'pointer' }}>
                <img
                  src="https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=320&q=80"
                  alt="Reviewing notes"
                  className="collage-img"
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
                <div className="collage-fallback-badge">🔎 Lost Items</div>
              </div>

              <div className="collage-card card-2" onClick={() => onSwitchView('found')} style={{ cursor: 'pointer' }}>
                <img
                  src="https://images.unsplash.com/photo-1548802673-380ab8ebc7b7?auto=format&fit=crop&w=320&q=80"
                  alt="Lost flyer on tree"
                  className="collage-img"
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
                <div className="collage-fallback-badge">📄 Found Items</div>
              </div>

              <div className="collage-card card-3" onClick={() => onSwitchView('matches')} style={{ cursor: 'pointer' }}>
                <img
                  src="https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?auto=format&fit=crop&w=320&q=80"
                  alt="Compass navigation"
                  className="collage-img"
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
                <div className="collage-fallback-badge">⚡ Smart Matches</div>
              </div>
            </div>
          </div>
        </section>

        {/* Search & Categories Bar on Home View */}
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

          {/* Search Bar + Category Pills on Home */}
          <div className="figma-controls-row" style={{ marginTop: '16px', marginBottom: '20px' }}>
            <div className="figma-pill-search">
              <Search size={18} className="search-pill-icon submit-icon" />
              <input
                type="text"
                placeholder="Search by title, location, brand..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              {search && (
                <button
                  type="button"
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748B', padding: '0 8px' }}
                  onClick={() => setSearch('')}
                >
                  <X size={16} />
                </button>
              )}
            </div>

            {/* Category Select Dropdown */}
            <select
              className="clean-select"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              style={{ padding: '10px 16px', borderRadius: '9999px', border: '1px solid #E2E8F0', background: '#FFFFFF', fontSize: '13px', fontWeight: 500 }}
            >
              {categories.map(c => (
                <option key={c} value={c}>{c === 'ALL' ? 'All Categories' : c}</option>
              ))}
            </select>
          </div>

          {/* Cards Grid */}
          <div className="figma-cards-grid">
            {displayedItems.length === 0 ? (
              <div className="figma-empty-container">
                <p className="empty-heading">No items matching criteria</p>
                <p className="empty-subtext">Try clearing your search query or selecting a different category.</p>
                <button
                  type="button"
                  className="figma-report-btn report-lost-btn"
                  style={{ margin: '14px auto 0' }}
                  onClick={() => { setSearch(''); setSelectedCategory('ALL'); }}
                >
                  <span className="report-btn-label">Reset Filters</span>
                </button>
              </div>
            ) : (
              displayedItems.map((item) => {
                const isLost = item.reportType === 'LOST';
                const isMine = item.userId === currentUser?.userId;
                const canClaim = !isAdmin && !isLost && item.status === 'OPEN' && !isMine;

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

                    {/* Middle: Visual Stage with Category Emoji */}
                    <div className="card-visual-stage" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', background: isLost ? 'rgba(239, 68, 68, 0.05)' : 'rgba(16, 185, 129, 0.05)' }}>
                      <span style={{ fontSize: '42px', filter: 'drop-shadow(0 2px 8px rgba(0,0,0,0.1))' }}>
                        {getCategoryVisual(item.category, item.title)}
                      </span>
                      <span className={`pill-type ${isLost ? 'lost' : 'found'}`} style={{ marginTop: 8 }}>
                        {isLost ? 'Lost Item' : 'Found Item'}
                      </span>
                    </div>

                    {/* Bottom: Info & Contact Button */}
                    <div className="card-bottom-info">
                      <h3 className="card-item-title">{item.title}</h3>
                      <p className="card-item-location">
                        <MapPin size={12} style={{ display: 'inline', marginRight: 3, verticalAlign: 'middle' }} />
                        {item.location || 'Campus Center'}
                      </p>
                      <p className="card-item-desc">
                        {item.description ? item.description.slice(0, 75) + '...' : 'No additional details provided.'}
                      </p>

                      <div className="card-action-bar" style={{ display: 'flex', gap: '6px' }}>
                        <button
                          type="button"
                          className="figma-contact-btn"
                          style={{ flex: 1 }}
                          onClick={(e) => {
                            e.stopPropagation();
                            if (canClaim) {
                              onOpenClaimModal(item);
                            } else {
                              onOpenDetailsModal(item);
                            }
                          }}
                        >
                          {canClaim ? 'Claim' : 'View Details'}
                        </button>
                        {isAdmin && (
                          <>
                            <button
                              type="button"
                              className="btn btn-sm btn-secondary"
                              style={{ padding: '6px 10px', color: '#059669', borderColor: '#10B981' }}
                              onClick={(e) => {
                                e.stopPropagation();
                                if (onEditItem) onEditItem(item);
                              }}
                              title="Edit item report"
                            >
                              ✏️
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-danger"
                              style={{ padding: '6px 10px' }}
                              onClick={(e) => {
                                e.stopPropagation();
                                if (onDeleteItem) onDeleteItem(item.itemId);
                              }}
                              title="Delete inappropriate report"
                            >
                              🗑️
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })
            )}
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
        <p style={{ color: '#64748B', fontSize: '14px', marginTop: '6px' }}>
          {isLostPage
            ? 'Browse all items reported lost across campus or submit a report to get notified on a match.'
            : 'Browse all items found by students and security staff. Submit proof of ownership to claim.'}
        </p>
      </div>

      {/* Search Bar + Category Dropdown + Report Button Row */}
      <div className="figma-controls-row">
        <div className="figma-pill-search">
          <Search size={18} className="search-pill-icon submit-icon" />
          <input
            type="text"
            placeholder={isLostPage ? 'Search lost items by name, brand, location...' : 'Search found items by name, brand, location...'}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button
              type="button"
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748B', padding: '0 8px' }}
              onClick={() => setSearch('')}
            >
              <X size={16} />
            </button>
          )}
        </div>

        {/* Category Filter Dropdown */}
        <select
          className="clean-select"
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          style={{ padding: '10px 16px', borderRadius: '9999px', border: '1px solid #E2E8F0', background: '#FFFFFF', fontSize: '13px', fontWeight: 500 }}
        >
          {categories.map(c => (
            <option key={c} value={c}>{c === 'ALL' ? 'All Categories' : c}</option>
          ))}
        </select>

        {/* Action Button: Coral Report Button (Lost) or Green Report Button (Found) for students */}
        {!isAdmin && (
          isLostPage ? (
            <button
              type="button"
              className="figma-report-btn report-lost-btn"
              onClick={() => onSwitchView('report-lost')}
            >
              <span className="report-btn-label">Report Lost</span>
              <span className="report-btn-emoji">📦☂️</span>
            </button>
          ) : (
            <button
              type="button"
              className="figma-report-btn report-found-btn"
              onClick={() => onSwitchView('report-found')}
            >
              <span className="report-btn-label">Report Found</span>
              <span className="report-btn-emoji">🔍✓</span>
            </button>
          )
        )}
      </div>

      {/* Category Pills Bar for Quick Filtering */}
      <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '12px', marginBottom: '16px' }}>
        {categories.map(cat => (
          <button
            key={cat}
            type="button"
            className={`tab-pill-btn ${selectedCategory === cat ? 'active' : ''}`}
            onClick={() => setSelectedCategory(cat)}
            style={{ fontSize: '12px', padding: '6px 14px', whiteSpace: 'nowrap' }}
          >
            {cat === 'ALL' ? 'All Items' : cat}
          </button>
        ))}
      </div>

      {/* 4-Column Cards Grid matching Figma */}
      <div className="figma-cards-grid">
        {displayedItems.length === 0 ? (
          <div className="figma-empty-container">
            <p className="empty-heading">No {isLostPage ? 'lost' : 'found'} items listed yet</p>
            <p className="empty-subtext">
              {search || selectedCategory !== 'ALL'
                ? 'No reports matched your search or category filter.'
                : `Be the first to file a ${isLostPage ? 'lost' : 'found'} report.`}
            </p>
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'center', marginTop: '16px' }}>
              {(search || selectedCategory !== 'ALL') && (
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => { setSearch(''); setSelectedCategory('ALL'); }}
                >
                  Reset Filters
                </button>
              )}
              {!isAdmin && (
                <button
                  type="button"
                  className={`figma-report-btn ${isLostPage ? 'report-lost-btn' : 'report-found-btn'}`}
                  onClick={() => onSwitchView(isLostPage ? 'report-lost' : 'report-found')}
                >
                  <span className="report-btn-label">Report {isLostPage ? 'Lost' : 'Found'} Item</span>
                </button>
              )}
            </div>
          </div>
        ) : (
          displayedItems.map((item) => {
            const isMine = item.userId === currentUser?.userId;
            const canClaim = !isAdmin && !isLostPage && item.status === 'OPEN' && !isMine;

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

                {/* Middle: Visual Stage with Category Emoji */}
                <div className="card-visual-stage" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', background: isLostPage ? 'rgba(239, 68, 68, 0.05)' : 'rgba(16, 185, 129, 0.05)' }}>
                  <span style={{ fontSize: '42px', filter: 'drop-shadow(0 2px 8px rgba(0,0,0,0.1))' }}>
                    {getCategoryVisual(item.category, item.title)}
                  </span>
                  <span className={`pill-type ${isLostPage ? 'lost' : 'found'}`} style={{ marginTop: 8 }}>
                    {item.category || (isLostPage ? 'Lost' : 'Found')}
                  </span>
                </div>

                {/* Bottom: Info & Contact Button */}
                <div className="card-bottom-info">
                  <h3 className="card-item-title">{item.title}</h3>
                  <p className="card-item-location">
                    <MapPin size={12} style={{ display: 'inline', marginRight: 3, verticalAlign: 'middle' }} />
                    {item.location || 'Campus Center'}
                  </p>
                  <p className="card-item-desc">
                    {item.description
                      ? item.description.slice(0, 80) + (item.description.length > 80 ? '...' : '')
                      : 'No additional details provided.'}
                  </p>

                  <div className="card-action-bar" style={{ display: 'flex', gap: '6px' }}>
                    <button
                      type="button"
                      className="figma-contact-btn"
                      style={{ flex: 1 }}
                      onClick={(e) => {
                        e.stopPropagation();
                        if (canClaim) {
                          onOpenClaimModal(item);
                        } else {
                          onOpenDetailsModal(item);
                        }
                      }}
                    >
                      {canClaim ? 'Claim' : 'View Details'}
                    </button>
                    {isAdmin && (
                      <>
                        <button
                          type="button"
                          className="btn btn-sm btn-secondary"
                          style={{ padding: '6px 10px', color: '#059669', borderColor: '#10B981' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            if (onEditItem) onEditItem(item);
                          }}
                          title="Edit item report"
                        >
                          ✏️
                        </button>
                        <button
                          type="button"
                          className="btn btn-sm btn-danger"
                          style={{ padding: '6px 10px' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            if (onDeleteItem) onDeleteItem(item.itemId);
                          }}
                          title="Delete inappropriate report"
                        >
                          🗑️
                        </button>
                      </>
                    )}
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
