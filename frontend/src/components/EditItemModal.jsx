import React, { useState } from 'react';
import { Save, X, AlertTriangle } from 'lucide-react';

export default function EditItemModal({
  item,
  onClose,
  onSave
}) {
  if (!item) return null;

  const [title, setTitle] = useState(item.title || '');
  const [category, setCategory] = useState(item.category || 'Other');
  const [color, setColor] = useState(item.color || '');
  const [brand, setBrand] = useState(item.brand || '');
  const [location, setLocation] = useState(item.location || '');
  const [eventDate, setEventDate] = useState(item.eventDate || new Date().toISOString().split('T')[0]);
  const [reportType, setReportType] = useState(item.reportType || 'LOST');
  const [status, setStatus] = useState(item.status || 'OPEN');
  const [description, setDescription] = useState(item.description || '');
  const [isSubmitting, setIsSubmitting] = useState(false);

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

  async function handleSubmit(e) {
    e.preventDefault();
    if (!title.trim() || !category || !location.trim()) {
      alert('Title, Category, and Location are required.');
      return;
    }

    setIsSubmitting(true);
    await onSave(item.itemId, {
      title: title.trim(),
      category,
      color: color.trim() || null,
      brand: brand.trim() || null,
      location: location.trim(),
      eventDate,
      reportType,
      status,
      description: description.trim()
    });
    setIsSubmitting(false);
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card glass-panel animate-fade-in" style={{ maxWidth: '580px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '18px' }}>✏️</span>
            <div>
              <h3 style={{ margin: 0, fontSize: '18px' }}>Edit Item #{item.itemId}</h3>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Admin Moderation & Content Management</span>
            </div>
          </div>
          <button className="modal-close" onClick={onClose} aria-label="Close modal">&times;</button>
        </div>

        <form onSubmit={handleSubmit} style={{ marginTop: '16px' }}>
          <div className="form-group" style={{ marginBottom: '14px' }}>
            <label className="form-label">Item Title *</label>
            <input
              type="text"
              className="form-input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              placeholder="e.g. Black JBL Earbuds"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px', marginBottom: '14px' }}>
            <div className="form-group">
              <label className="form-label">Category *</label>
              <select
                className="form-select"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                required
              >
                {categories.map(c => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Report Type *</label>
              <select
                className="form-select"
                value={reportType}
                onChange={(e) => setReportType(e.target.value)}
                required
              >
                <option value="LOST">Lost Item</option>
                <option value="FOUND">Found Item</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px', marginBottom: '14px' }}>
            <div className="form-group">
              <label className="form-label">Location *</label>
              <input
                type="text"
                className="form-input"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                required
                placeholder="e.g. CS Lab 3rd floor"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Event Date</label>
              <input
                type="date"
                className="form-input"
                value={eventDate}
                onChange={(e) => setEventDate(e.target.value)}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px', marginBottom: '14px' }}>
            <div className="form-group">
              <label className="form-label">Color</label>
              <input
                type="text"
                className="form-input"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                placeholder="e.g. Black"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Brand</label>
              <input
                type="text"
                className="form-input"
                value={brand}
                onChange={(e) => setBrand(e.target.value)}
                placeholder="e.g. Apple"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Status *</label>
              <select
                className="form-select"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
                required
              >
                <option value="OPEN">OPEN</option>
                <option value="CLAIM_PENDING">CLAIM PENDING</option>
                <option value="CLAIMED">CLAIMED</option>
                <option value="RETURNED">RETURNED</option>
                <option value="CLOSED">CLOSED</option>
              </select>
            </div>
          </div>

          <div className="form-group" style={{ marginBottom: '18px' }}>
            <label className="form-label">Description &amp; Identifying Details</label>
            <textarea
              className="form-textarea"
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Cleaned or corrected description..."
            />
          </div>

          <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              <Save size={15} style={{ marginRight: 4, verticalAlign: 'middle' }} />
              <span>{isSubmitting ? 'Saving...' : 'Save Changes'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
