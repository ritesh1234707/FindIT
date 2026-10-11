import React from 'react';

export default function ItemDetailsModal({
  item,
  currentUser,
  onClose,
  onOpenClaimModal
}) {
  if (!item) return null;

  const isLost = item.reportType === 'LOST';
  const canClaim = !isLost && item.status === 'OPEN' && item.userId !== currentUser?.userId;

  return (
    <div className="modal-overlay">
      <div className="modal-card glass-panel">
        <div className="modal-header">
          <h3>{item.title}</h3>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>

        <div style={{ display: 'flex', gap: '8px', marginBottom: '14px' }}>
          <span className={`badge ${isLost ? 'badge-lost' : 'badge-found'}`}>
            {item.reportType}
          </span>
          <span className={`badge-status ${(item.status || 'open').toLowerCase()}`}>
            {(item.status || 'OPEN').replace('_', ' ')}
          </span>
          <span className="tag">{item.category}</span>
        </div>

        <p style={{ marginBottom: '14px', fontSize: '14px', color: 'var(--card-text-secondary)' }}>
          {item.description || 'No additional description provided.'}
        </p>

        <div className="modal-details-box">
          <div><strong>Color:</strong> {item.color || 'Not specified'}</div>
          <div><strong>Brand:</strong> {item.brand || 'Not specified'}</div>
          <div><strong>Location:</strong> {item.location || 'Campus'}</div>
          <div><strong>Event Date:</strong> {item.eventDate || 'N/A'}</div>
        </div>

        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
          {canClaim && (
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => {
                onClose();
                onOpenClaimModal(item);
              }}
            >
              Claim Item
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
