import React from 'react';
import { MapPin, Calendar, Tag, User, ShieldCheck, CheckCircle } from 'lucide-react';

export default function ItemDetailsModal({
  item,
  currentUser,
  onClose,
  onOpenClaimModal
}) {
  if (!item) return null;

  const isLost = item.reportType === 'LOST';
  const isAdmin = currentUser?.role === 'ADMIN';
  const isMine = item.userId === currentUser?.userId;
  const canClaim = !isAdmin && !isLost && item.status === 'OPEN' && !isMine;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card glass-panel animate-fade-in" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <span className={`badge ${isLost ? 'badge-lost' : 'badge-found'}`} style={{ marginRight: 8 }}>
              {item.reportType}
            </span>
            <span className={`badge-status ${(item.status || 'open').toLowerCase()}`}>
              {(item.status || 'OPEN').replace('_', ' ')}
            </span>
          </div>
          <button className="modal-close" onClick={onClose} aria-label="Close modal">&times;</button>
        </div>

        <h2 style={{ fontSize: '22px', fontWeight: 700, margin: '12px 0 8px', color: 'var(--text-primary)' }}>
          {item.title}
        </h2>

        <p style={{ marginBottom: '18px', fontSize: '14px', lineHeight: 1.6, color: 'var(--text-secondary)' }}>
          {item.description || 'No detailed description provided by reporter.'}
        </p>

        {/* Details Grid */}
        <div className="modal-details-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px', background: '#F8FAFC', padding: '16px', borderRadius: '12px', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
            <Tag size={15} style={{ color: '#64748B' }} />
            <div>
              <span style={{ color: '#94A3B8', fontSize: '11px', display: 'block' }}>Category</span>
              <strong>{item.category || 'General'}</strong>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
            <MapPin size={15} style={{ color: '#64748B' }} />
            <div>
              <span style={{ color: '#94A3B8', fontSize: '11px', display: 'block' }}>Location</span>
              <strong>{item.location || 'Campus'}</strong>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
            <Calendar size={15} style={{ color: '#64748B' }} />
            <div>
              <span style={{ color: '#94A3B8', fontSize: '11px', display: 'block' }}>Date</span>
              <strong>{item.eventDate || 'Recently'}</strong>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
            <User size={15} style={{ color: '#64748B' }} />
            <div>
              <span style={{ color: '#94A3B8', fontSize: '11px', display: 'block' }}>Reported By</span>
              <strong>{isMine ? 'You (My Report)' : item.reporterName || 'Campus Student'}</strong>
            </div>
          </div>

          {item.brand && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
              <span style={{ fontSize: '14px' }}>🏷️</span>
              <div>
                <span style={{ color: '#94A3B8', fontSize: '11px', display: 'block' }}>Brand</span>
                <strong>{item.brand}</strong>
              </div>
            </div>
          )}

          {item.color && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
              <span style={{ fontSize: '14px' }}>🎨</span>
              <div>
                <span style={{ color: '#94A3B8', fontSize: '11px', display: 'block' }}>Color</span>
                <strong>{item.color}</strong>
              </div>
            </div>
          )}
        </div>

        {/* Claim status info */}
        {item.status === 'CLAIM_PENDING' && (
          <div style={{ padding: '10px 14px', background: '#FEF3C7', color: '#92400E', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' }}>
            ⏳ An ownership claim is currently pending administrative review for this item.
          </div>
        )}

        {item.status === 'RETURNED' && (
          <div style={{ padding: '10px 14px', background: '#D1FAE5', color: '#065F46', borderRadius: '8px', fontSize: '13px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <CheckCircle size={16} /> This item has been verified and returned to its owner.
          </div>
        )}

        {/* Modal Actions Footer */}
        <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
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
              Claim This Item
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
