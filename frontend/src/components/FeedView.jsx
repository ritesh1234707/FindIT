import React, { useState, useMemo } from 'react';
import { Search, MapPin, PlusCircle, ArrowUpRight, Zap, Check } from 'lucide-react';

export default function FeedView({
  items = [],
  stats,
  matches = [],
  myItems = [],
  currentUser,
  onSwitchView,
  onOpenClaimModal,
  onOpenDetailsModal
}) {
  const [search, setSearch] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  // Categories list
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

  // Map category to a tasteful subtle icon/emoji placeholder
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

  // Filtered Items (clean and fast)
  const filteredItems = useMemo(() => {
    return items.filter(item => {
      // Type filter
      if (selectedType !== 'ALL' && item.reportType !== selectedType) return false;
      // Category filter
      if (selectedCategory !== 'ALL' && item.category !== selectedCategory) return false;
      // Search
      if (search.trim()) {
        const q = search.toLowerCase();
        const inTitle = item.title?.toLowerCase().includes(q);
        const inDesc  = item.description?.toLowerCase().includes(q);
        const inLoc   = item.location?.toLowerCase().includes(q);
        const inBrand = item.brand?.toLowerCase().includes(q);
        if (!inTitle && !inDesc && !inLoc && !inBrand) return false;
      }
      return true;
    });
  }, [items, selectedType, selectedCategory, search]);

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

  return (
    <div className="view-panel active animate-fade-in">

      {/* ── Editorial Hero Section (Inspired by Reunited.) ── */}
      <section className="editorial-hero">
        <div className="hero-text-block">
          <h1 className="editorial-headline">
            How you get it <em>back</em>.
          </h1>
          <p className="editorial-description">
            Left behind in a classroom, on the field, or at the library — staff collect it, or whoever finds it logs it here. Every item is verified and securely restored.
          </p>
          <div className="editorial-actions">
            <button className="btn btn-primary" onClick={() => onSwitchView('report')}>
              Report an Item <ArrowUpRight size={15} />
            </button>
            <button className="btn btn-secondary" onClick={() => onSwitchView('matches')}>
              <Zap size={14} /> Smart Matches {matches.length > 0 && `(${matches.length})`}
            </button>
          </div>
        </div>
      </section>

      {/* ── Minimalist, Uncluttered Search & Filter Strip ── */}
      <div className="minimal-filter-strip">
        <div className="clean-search-box">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            placeholder="Search by item, location, or brand..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button type="button" className="clear-btn" onClick={() => setSearch('')}>
              &times;
            </button>
          )}
        </div>

        <div className="minimal-segmented-toggle">
          <button
            type="button"
            className={`segment-btn ${selectedType === 'ALL' ? 'active' : ''}`}
            onClick={() => setSelectedType('ALL')}
          >
            All Items
          </button>
          <button
            type="button"
            className={`segment-btn ${selectedType === 'LOST' ? 'active' : ''}`}
            onClick={() => setSelectedType('LOST')}
          >
            Lost
          </button>
          <button
            type="button"
            className={`segment-btn ${selectedType === 'FOUND' ? 'active' : ''}`}
            onClick={() => setSelectedType('FOUND')}
          >
            Found
          </button>
        </div>

        <select
          className="clean-select category-select"
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
        >
          {categories.map(c => (
            <option key={c} value={c}>
              {c === 'ALL' ? 'All Categories' : c}
            </option>
          ))}
        </select>
      </div>

      {/* ── Feed Count Header ── */}
      <div className="clean-feed-header">
        <span className="count-label">
          Showing {filteredItems.length} {filteredItems.length === 1 ? 'item' : 'items'}
        </span>
      </div>

      {/* ── Refined Editorial Cards Grid ── */}
      <div className="editorial-cards-grid">
        {filteredItems.length === 0 ? (
          <div className="clean-empty-state">
            <p className="empty-title">No items found</p>
            <p className="empty-sub">
              Try adjusting your search query or reset the category filter.
            </p>
            <button
              className="btn btn-secondary btn-sm"
              style={{ marginTop: 14 }}
              onClick={() => { setSearch(''); setSelectedType('ALL'); setSelectedCategory('ALL'); }}
            >
              Reset Filters
            </button>
          </div>
        ) : (
          filteredItems.map(item => {
            const isLost = item.reportType === 'LOST';
            const isMine = item.userId === currentUser?.userId;
            const canClaim = !isLost && item.status === 'OPEN' && !isMine;

            return (
              <div
                key={item.itemId}
                className="clean-item-card"
                onClick={() => onOpenDetailsModal(item)}
              >
                {/* Visual Image / Category Placeholder */}
                <div className="item-visual-box">
                  <span className="visual-emoji">
                    {getCategoryVisual(item.category, item.title)}
                  </span>
                  <span className={`pill-type ${isLost ? 'lost' : 'found'}`}>
                    {isLost ? 'Lost' : 'Found'}
                  </span>
                </div>

                {/* Card Text Content */}
                <div className="item-card-body">
                  <h3 className="item-title">{item.title}</h3>
                  <p className="item-category">{item.category}</p>

                  <div className="item-meta-row">
                    <span className="item-location">
                      <MapPin size={12} /> {item.location || 'Campus'}
                    </span>
                    <span className="item-date">
                      {isLost ? 'Lost' : 'Found'} {formatDate(item.eventDate)}
                    </span>
                  </div>

                  {/* Actions Bar */}
                  <div className="item-card-actions" onClick={(e) => e.stopPropagation()}>
                    {canClaim ? (
                      <button
                        type="button"
                        className="btn btn-sm btn-primary"
                        onClick={() => onOpenClaimModal(item)}
                      >
                        Claim Item
                      </button>
                    ) : isMine ? (
                      <span className="mine-indicator">Your report</span>
                    ) : null}

                    <button
                      type="button"
                      className="btn btn-sm btn-secondary"
                      onClick={() => onOpenDetailsModal(item)}
                    >
                      Details
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
