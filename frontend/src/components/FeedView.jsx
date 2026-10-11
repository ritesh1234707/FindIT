import React, { useState, useMemo } from 'react';
import {
  Search,
  MapPin,
  PlusCircle,
  Zap,
  Tag,
  Calendar,
  User,
  CheckCircle,
  RefreshCw,
  X,
  ArrowRight,
  Sparkles,
  Filter,
  Eye,
  AlertCircle
} from 'lucide-react';

export default function FeedView({
  items = [],
  stats,
  matches = [],
  myItems = [],
  currentUser,
  onSwitchView,
  onOpenClaimModal,
  onOpenDetailsModal,
  onRefreshItems
}) {
  const [search, setSearch] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedLocation, setSelectedLocation] = useState('ALL');
  const [selectedStatus, setSelectedStatus] = useState('OPEN');
  const [sortBy, setSortBy] = useState('newest');
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Dynamic Metrics
  const openLostCount = useMemo(() => {
    return items.filter(i => i.reportType === 'LOST' && i.status === 'OPEN').length;
  }, [items]);

  const openFoundCount = useMemo(() => {
    return items.filter(i => i.reportType === 'FOUND' && i.status === 'OPEN').length;
  }, [items]);

  const myReportsCount = useMemo(() => {
    return myItems?.length || items.filter(i => i.userId === currentUser?.userId).length;
  }, [myItems, items, currentUser]);

  const recoveryRate = stats?.recoveryRate || 0;

  // Distinct locations from active items
  const campusLocations = useMemo(() => {
    const defaultLocs = [
      'Central Library',
      'Main Canteen',
      'Engineering Block A',
      'Science Block B',
      'Sports Complex',
      'Auditorium',
      'Computer Lab'
    ];
    const itemLocs = items.map(i => i.location).filter(Boolean);
    const combined = Array.from(new Set([...defaultLocs, ...itemLocs]));
    return combined.sort();
  }, [items]);

  // Categories list
  const categories = [
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

  // Filtering & Sorting
  const filteredItems = useMemo(() => {
    return items
      .filter(item => {
        // Type match
        if (selectedType !== 'ALL' && item.reportType !== selectedType) return false;
        // Category match
        if (selectedCategory !== 'ALL' && item.category !== selectedCategory) return false;
        // Location match
        if (selectedLocation !== 'ALL' && !item.location?.toLowerCase().includes(selectedLocation.toLowerCase())) {
          return false;
        }
        // Status match
        if (selectedStatus !== 'ALL' && item.status !== selectedStatus) return false;
        // Search match
        if (search.trim()) {
          const q = search.toLowerCase();
          const inTitle = item.title?.toLowerCase().includes(q);
          const inDesc  = item.description?.toLowerCase().includes(q);
          const inLoc   = item.location?.toLowerCase().includes(q);
          const inBrand = item.brand?.toLowerCase().includes(q);
          const inColor = item.color?.toLowerCase().includes(q);
          const inRep   = item.reporterName?.toLowerCase().includes(q);
          if (!inTitle && !inDesc && !inLoc && !inBrand && !inColor && !inRep) return false;
        }
        return true;
      })
      .sort((a, b) => {
        if (sortBy === 'newest') return (b.itemId || 0) - (a.itemId || 0);
        if (sortBy === 'oldest') return (a.itemId || 0) - (b.itemId || 0);
        if (sortBy === 'title')  return (a.title || '').localeCompare(b.title || '');
        return 0;
      });
  }, [items, selectedType, selectedCategory, selectedLocation, selectedStatus, search, sortBy]);

  const hasActiveFilters = Boolean(
    search.trim() ||
    selectedType !== 'ALL' ||
    selectedCategory !== 'ALL' ||
    selectedLocation !== 'ALL' ||
    selectedStatus !== 'OPEN'
  );

  function resetFilters() {
    setSearch('');
    setSelectedType('ALL');
    setSelectedCategory('ALL');
    setSelectedLocation('ALL');
    setSelectedStatus('OPEN');
    setSortBy('newest');
  }

  async function handleRefresh() {
    if (!onRefreshItems) return;
    setIsRefreshing(true);
    await onRefreshItems();
    setTimeout(() => setIsRefreshing(false), 500);
  }

  function formatDate(dStr) {
    if (!dStr) return 'Recently';
    try {
      const parts = dStr.split('-');
      if (parts.length === 3) {
        const date = new Date(parts[0], parts[1] - 1, parts[2]);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
      }
      return dStr;
    } catch {
      return dStr;
    }
  }

  const firstName = currentUser?.fullName ? currentUser.fullName.split(' ')[0] : 'Student';

  return (
    <div className="view-panel active animate-fade-in">

      {/* ── 1. STUDENT WELCOME & HERO PANEL ─────────────────────────────── */}
      <div className="hero-section">
        <div className="hero-content">
          <div className="hero-badge">
            <Sparkles size={12} style={{ display: 'inline', marginRight: 5, verticalAlign: 'middle' }} />
            Campus Lost &amp; Found Portal
          </div>
          <h1 className="hero-title">
            Hello, {firstName}! 👋 <br />
            <span className="gradient-text">Recover What You Lost. Return What You Found.</span>
          </h1>
          <p className="hero-subtitle">
            FindIT uses keyword cross-indexing and category matching to quickly reunite students
            with lost campus belongings. Browse live reports below or file a report in 60 seconds.
          </p>

          <div className="hero-cta-group" style={{ flexDirection: 'row', flexWrap: 'wrap', marginTop: 22 }}>
            <button className="btn btn-primary" onClick={() => onSwitchView('report')}>
              <PlusCircle size={16} />
              <span>Report an Item</span>
            </button>
            <button className="btn btn-secondary" onClick={() => onSwitchView('matches')}>
              <Zap size={16} />
              <span>Smart Matches {matches.length > 0 && `(${matches.length})`}</span>
            </button>
            <button className="btn btn-secondary" onClick={() => onSwitchView('my-items')}>
              <User size={16} />
              <span>My Activity ({myReportsCount})</span>
            </button>
          </div>
        </div>

        {/* Hero Quick Metrics Badges */}
        <div className="hero-stats-capsule">
          <div className="stat-capsule-item">
            <span className="capsule-val">{openLostCount}</span>
            <span className="capsule-lbl">Open Lost</span>
          </div>
          <div className="capsule-divider" />
          <div className="stat-capsule-item">
            <span className="capsule-val">{openFoundCount}</span>
            <span className="capsule-lbl">Open Found</span>
          </div>
          <div className="capsule-divider" />
          <div className="stat-capsule-item">
            <span className="capsule-val">{recoveryRate}%</span>
            <span className="capsule-lbl">Recovery</span>
          </div>
        </div>
      </div>

      {/* ── 2. SMART MATCH NOTIFICATION CALLOUT (IF ANY) ──────────────────── */}
      {matches.length > 0 && (
        <div className="match-callout-banner animate-fade-in" onClick={() => onSwitchView('matches')}>
          <div className="callout-left">
            <div className="callout-icon">
              <Zap size={20} />
            </div>
            <div>
              <strong>{matches.length} Potential Smart Match{matches.length > 1 ? 'es' : ''} Detected!</strong>
              <p>Our heuristic algorithm found potential matches between open reports and your items.</p>
            </div>
          </div>
          <button className="btn btn-sm btn-primary">
            Review Matches <ArrowRight size={14} />
          </button>
        </div>
      )}

      {/* ── 3. ADVANCED SEARCH & MULTI-FILTER SUITE ───────────────────────── */}
      <div className="feed-filters-bar glass-panel">
        {/* Search input with live clear */}
        <div className="search-box">
          <Search size={18} />
          <input
            type="text"
            placeholder="Search items by title, brand, description, location..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button
              type="button"
              className="clear-search-btn"
              onClick={() => setSearch('')}
              title="Clear search"
            >
              <X size={14} />
            </button>
          )}
        </div>

        {/* Report Type Switcher Buttons */}
        <div className="type-filter-group">
          <button
            type="button"
            className={`type-filter-btn ${selectedType === 'ALL' ? 'active' : ''}`}
            onClick={() => setSelectedType('ALL')}
          >
            All ({items.length})
          </button>
          <button
            type="button"
            className={`type-filter-btn ${selectedType === 'LOST' ? 'active' : ''}`}
            onClick={() => setSelectedType('LOST')}
          >
            🔴 Lost
          </button>
          <button
            type="button"
            className={`type-filter-btn ${selectedType === 'FOUND' ? 'active' : ''}`}
            onClick={() => setSelectedType('FOUND')}
          >
            🟢 Found
          </button>
        </div>

        {/* Category Filter */}
        <select
          className="filter-select"
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
        >
          <option value="ALL">All Categories</option>
          {categories.map(cat => (
            <option key={cat} value={cat}>{cat}</option>
          ))}
        </select>

        {/* Campus Location Filter */}
        <select
          className="filter-select"
          value={selectedLocation}
          onChange={(e) => setSelectedLocation(e.target.value)}
        >
          <option value="ALL">All Locations</option>
          {campusLocations.map(loc => (
            <option key={loc} value={loc}>{loc}</option>
          ))}
        </select>

        {/* Status Filter */}
        <select
          className="filter-select"
          value={selectedStatus}
          onChange={(e) => setSelectedStatus(e.target.value)}
        >
          <option value="ALL">All Statuses</option>
          <option value="OPEN">Available (Open)</option>
          <option value="CLAIM_PENDING">Claim Pending</option>
          <option value="CLAIMED">Claimed</option>
          <option value="RETURNED">Returned</option>
        </select>

        {/* Sort selector */}
        <select
          className="filter-select"
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          title="Sort items"
        >
          <option value="newest">Sort: Newest First</option>
          <option value="oldest">Sort: Oldest First</option>
          <option value="title">Sort: Title (A-Z)</option>
        </select>

        {/* Reset Filters & Refresh Action */}
        {hasActiveFilters && (
          <button
            type="button"
            className="btn btn-sm btn-secondary reset-filter-btn"
            onClick={resetFilters}
          >
            <X size={13} /> Reset
          </button>
        )}

        <button
          type="button"
          className="btn btn-sm btn-secondary refresh-feed-btn"
          onClick={handleRefresh}
          title="Refresh Feed"
          disabled={isRefreshing}
        >
          <RefreshCw size={13} className={isRefreshing ? 'spin-anim' : ''} />
        </button>
      </div>

      {/* ── 4. ITEMS GRID HEADER ────────────────────────────────────────── */}
      <div className="items-grid-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <strong>Showing {filteredItems.length} {filteredItems.length === 1 ? 'item' : 'items'}</strong>
          {hasActiveFilters && (
            <span style={{ fontSize: '12px', color: 'var(--card-text-muted)' }}>
              (Filtered from {items.length} total)
            </span>
          )}
        </div>

        <div style={{ display: 'flex', gap: '12px', fontSize: '12px' }}>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#EF4444' }} />
            Lost: {items.filter(i => i.reportType === 'LOST').length}
          </span>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#10B981' }} />
            Found: {items.filter(i => i.reportType === 'FOUND').length}
          </span>
        </div>
      </div>

      {/* ── 5. ITEMS GRID ───────────────────────────────────────────────── */}
      <div className="items-grid">
        {filteredItems.length === 0 ? (
          <div className="feed-empty-state glass-panel">
            <div className="empty-icon-bubble">🔍</div>
            <h3>No items matched your current filters</h3>
            <p>
              Try clearing your search query, changing the campus location, or setting the status to "All Statuses".
            </p>
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'center', marginTop: '16px' }}>
              {hasActiveFilters && (
                <button className="btn btn-secondary" onClick={resetFilters}>
                  Clear All Filters
                </button>
              )}
              <button className="btn btn-primary" onClick={() => onSwitchView('report')}>
                <PlusCircle size={15} /> Report New Item
              </button>
            </div>
          </div>
        ) : (
          filteredItems.map(item => {
            const isLost = item.reportType === 'LOST';
            const isMine = item.userId === currentUser?.userId;
            const canClaim = !isLost && item.status === 'OPEN' && !isMine;

            return (
              <div key={item.itemId} className="item-card glass-panel">
                <div>
                  {/* Card Top: Badges & Date */}
                  <div className="card-header">
                    <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                      <span className={`badge ${isLost ? 'badge-lost' : 'badge-found'}`}>
                        {isLost ? 'LOST ITEM' : 'FOUND ITEM'}
                      </span>
                      <span className={`badge-status ${(item.status || 'open').toLowerCase()}`}>
                        {(item.status || 'OPEN').replace('_', ' ')}
                      </span>
                    </div>

                    <div className="card-date-badge" title="Report Date">
                      <Calendar size={12} />
                      <span>{formatDate(item.eventDate)}</span>
                    </div>
                  </div>

                  {/* Title */}
                  <h3 className="card-title" onClick={() => onOpenDetailsModal(item)} style={{ cursor: 'pointer' }}>
                    {item.title}
                  </h3>

                  {/* Metadata Chips: Category, Color, Brand */}
                  <div className="card-tags">
                    <span className="tag">{item.category}</span>
                    {item.color && (
                      <span className="tag color-tag">
                        <span className="color-dot" style={{ backgroundColor: item.color.toLowerCase() }} />
                        {item.color}
                      </span>
                    )}
                    {item.brand && <span className="tag brand-tag">{item.brand}</span>}
                  </div>

                  {/* Description snippet */}
                  <p className="card-desc">
                    {item.description || 'No additional details provided for this campus report.'}
                  </p>

                  {/* Reporter line */}
                  <div className="card-reporter-line">
                    {isMine ? (
                      <span className="mine-pill">⭐ You reported this item</span>
                    ) : (
                      <span className="reporter-pill">
                        <User size={12} /> Reported by {item.reporterName || 'Campus Student'}
                      </span>
                    )}
                  </div>
                </div>

                {/* Card Footer */}
                <div className="card-footer">
                  <div className="card-location" title={`Location: ${item.location || 'Campus'}`}>
                    <MapPin size={13} />
                    <span>{item.location || 'Campus'}</span>
                  </div>

                  <div className="card-actions">
                    {canClaim && (
                      <button
                        type="button"
                        className="btn btn-sm btn-primary"
                        onClick={() => onOpenClaimModal(item)}
                        title="Submit proof of ownership for this found item"
                      >
                        Claim Item
                      </button>
                    )}
                    <button
                      type="button"
                      className="btn btn-sm btn-secondary"
                      onClick={() => onOpenDetailsModal(item)}
                      title="View complete details"
                    >
                      <Eye size={13} /> Details
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
