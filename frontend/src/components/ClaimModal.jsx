import React, { useState } from 'react';

export default function ClaimModal({
  item,
  currentUser,
  onClose,
  onSubmitClaim
}) {
  const [verification, setVerification] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!item) return null;

  async function handleSubmit(e) {
    e.preventDefault();
    if (!verification.trim()) return;

    setIsSubmitting(true);
    await onSubmitClaim(item.itemId, verification.trim());
    setIsSubmitting(false);
  }

  return (
    <div className="modal-overlay">
      <div className="modal-card glass-panel">
        <div className="modal-header">
          <h3>Submit Ownership Claim</h3>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-item-info">
            <strong>Claiming:</strong> {item.title} ({item.category})<br />
            <small style={{ color: 'var(--text-secondary)' }}>Found near: {item.location}</small>
          </div>

          <div className="form-group">
            <label className="form-label">Proof of Ownership / Verification Details *</label>
            <textarea
              className="form-textarea"
              rows={4}
              placeholder="Describe unique identifying marks, contents, lock screen picture, serial number, or exact student details on the item..."
              value={verification}
              onChange={(e) => setVerification(e.target.value)}
              required
            />
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Submitting...' : 'Submit Claim for Review'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
