import React, { useState } from 'react';
import { Search, MapPin, PlusCircle, Zap } from 'lucide-react';

export default function FeedView({
  items,
  currentUser,
  onSwitchView,
  onOpenClaimModal,
  onOpenDetailsModal
}) {
  const [search, setSearch] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedStatus, setSelectedStatus] = useState('OPEN');

  const filteredItems = items.filter(item => {
    // Type match
    if (selectedType !== 'ALL' && item.reportType !== selectedType) return false;
    // Category match
    if (selectedCategory !== 'ALL' && item.category !== selectedCategory) return false;
    // Status match
    if (selectedStatus !== 'ALL' && item.status !== selectedStatus) return false;
    // Search match
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

  return (
    <div className="view-panel active animate-fade-in">
      {/* Hero Section */}
      <div className="hero-section">
        <div className="hero-content">
          <div className="hero-badge">Smart Campus Network</div>
          <h1 className="hero-title">
            Recover What You Lost. <br />
            <span className="gradient-text">Return What You Found.</span>
          </h1>
          <p className="hero-subtitle">
            FindIT combines keyword matching, category classification, and color indexing
            to reconnect lost student belongings with their rightful owners across campus.
          </p>
        </div>
        <div className="hero-cta-group">
          <button className="btn btn-primary" onClick={() => onSwitchView('report')}>
            <PlusCircle size={16} />
            <span>Report an Item</span>
          </button>
          <button className="btn btn-secondary" onClick={() => onSwitchView('matches')}>
            <Zap size={16} />
            <span>View AI Matches</span>
          </button>
        </div>
      </div>

      {/* Filter Controls Bar */}
      <div className="feed-filters-bar glass-panel">
        <div className="search-box">
          <Search size={18} />
          <input
            type="text"
            placeholder="Search by title, brand, description, location..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="type-filter-group">
          <button
            className={`type-filter-btn ${selectedType === 'ALL' ? 'active' : ''}`}
            onClick={() => setSelectedType('ALL')}
          >
            All Feed
          </button>
          <button
            className={`type-filter-btn ${selectedType === 'LOST' ? 'active' : ''}`}
            onClick={() => setSelectedType('LOST')}
          >
            Lost Only
          </button>
          <button
            className={`type-filter-btn ${selectedType === 'FOUND' ? 'active' : ''}`}
            onClick={() => setSelectedType('FOUND')}
          >
            Found Only
          </button>
        </div>

        <select
          className="filter-select"
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
        >
          <option value="ALL">All Categories</option>
          <option value="ID Card">ID Card</option>
          <option value="Earbuds / Headphones">Earbuds / Headphones</option>
          <option value="Phone / Electronics">Phone / Electronics</option>
          <option value="Bag / Backpack">Bag / Backpack</option>
          <option value="Calculator">Calculator</option>
          <option value="Keys / Keychain">Keys / Keychain</option>
          <option value="Water Bottle">Water Bottle</option>
          <option value="Wallet / Purse">Wallet / Purse</option>
          <option value="Books / Notes">Books / Notes</option>
          <option value="Other">Other</option>
        </select>

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
      </div>

      {/* Grid Header */}
      <div className="items-grid-header">
        <span>Showing {filteredItems.length} {filteredItems.length === 1 ? 'item' : 'items'}</span>
      </div>

      {/* Items Grid */}
      <div className="items-grid">
        {filteredItems.length === 0 ? (
          <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '60px 20px', color: 'var(--text-muted)' }}>
            <div style={{ fontSize: '40px', marginBottom: '12px' }}>🔍</div>
            <h3>No items match your filters</h3>
            <p style={{ fontSize: '13.5px', marginTop: '6px' }}>
              Try adjusting your search query, type toggle, or category selector.
            </p>
          </div>
        ) : (
          filteredItems.map(item => {
            const isLost = item.reportType === 'LOST';
            const canClaim = !isLost && item.status === 'OPEN' && item.userId !== currentUser?.userId;

            return (
              <div key={item.itemId} className="item-card glass-panel">
                <div>
                  <div className="card-header">
                    <span className={`badge ${isLost ? 'badge-lost' : 'badge-found'}`}>
                      {isLost ? 'LOST ITEM' : 'FOUND ITEM'}
                    </span>
                    <span className={`badge-status ${(item.status || 'open').toLowerCase()}`}>
                      {(item.status || 'OPEN').replace('_', ' ')}
                    </span>
                  </div>

                  <h3 className="card-title">{item.title}</h3>

                  <div className="card-tags">
                    <span className="tag">{item.category}</span>
                    {item.color && <span className="tag">{item.color}</span>}
                    {item.brand && <span className="tag">{item.brand}</span>}
                  </div>

                  <p className="card-desc">
                    {item.description || 'No additional details provided.'}
                  </p>
                </div>

                <div className="card-footer">
                  <div className="card-location">
                    <MapPin size={13} />
                    <span>{item.location || 'Campus'}</span>
                  </div>

                  <div className="card-actions">
                    {canClaim && (
                      <button
                        className="btn btn-sm btn-primary"
                        onClick={() => onOpenClaimModal(item)}
                      >
                        Claim Item
                      </button>
                    )}
                    <button
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
