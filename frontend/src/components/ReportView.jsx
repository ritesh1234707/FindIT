import React, { useState } from 'react';
import { Send, MapPin } from 'lucide-react';

export default function ReportView({
  currentUser,
  onReportSubmitted,
  onCancel
}) {
  const [reportType, setReportType] = useState('LOST');
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('');
  const [color, setColor] = useState('');
  const [brand, setBrand] = useState('');
  const [eventDate, setEventDate] = useState(new Date().toISOString().split('T')[0]);
  const [location, setLocation] = useState('');
  const [description, setDescription] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!title.trim() || !category || !location.trim() || !eventDate) {
      alert('Please fill out all required fields.');
      return;
    }

    setIsSubmitting(true);
    try {
      const res = await fetch('/api/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: currentUser?.userId || 1,
          title: title.trim(),
          category,
          color: color.trim() || null,
          brand: brand.trim() || null,
          description: description.trim(),
          location: location.trim(),
          eventDate,
          reportType
        })
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to submit report');

      onReportSubmitted(data.item);
    } catch (err) {
      alert(err.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  const isLost = reportType === 'LOST';

  return (
    <div className="view-panel active animate-fade-in">
      <div className="view-header">
        <h2>Report a Campus Item</h2>
        <p>
          Enter the details below. Our heuristic algorithm will automatically cross-reference
          active reports across campus to find potential matches.
        </p>
      </div>

      <div className="report-layout">
        {/* Form Column */}
        <form className="report-form glass-panel" onSubmit={handleSubmit}>
          {/* Report Type Switcher */}
          <div className="form-group">
            <label className="form-label">What are you reporting?</label>
            <div className="report-type-toggle">
              <div
                className={`type-toggle-option ${isLost ? 'active' : ''}`}
                onClick={() => setReportType('LOST')}
              >
                <span className="toggle-icon">🔍</span>
                <span className="toggle-text">I LOST an Item</span>
              </div>

              <div
                className={`type-toggle-option ${!isLost ? 'active' : ''}`}
                onClick={() => setReportType('FOUND')}
              >
                <span className="toggle-icon">🎁</span>
                <span className="toggle-text">I FOUND an Item</span>
              </div>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group flex-2">
              <label className="form-label">Item Title / Headline *</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. Black JBL Wave Buds, Blue Wildcraft Backpack"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>

            <div className="form-group flex-1">
              <label className="form-label">Category *</label>
              <select
                className="form-select"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                required
              >
                <option value="" disabled>Select Category</option>
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
            </div>
          </div>

          <div className="form-row">
            <div className="form-group flex-1">
              <label className="form-label">Primary Color</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. Black, Navy Blue, Silver"
                value={color}
                onChange={(e) => setColor(e.target.value)}
              />
            </div>

            <div className="form-group flex-1">
              <label className="form-label">Brand / Model</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. Apple, Casio, Wildcraft"
                value={brand}
                onChange={(e) => setBrand(e.target.value)}
              />
            </div>

            <div className="form-group flex-1">
              <label className="form-label">Event Date *</label>
              <input
                type="date"
                className="form-input"
                value={eventDate}
                onChange={(e) => setEventDate(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Campus Location *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Computer Science Lab 3rd Floor, Central Library, Canteen Block"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Detailed Description & Marks</label>
            <textarea
              className="form-textarea"
              rows={4}
              placeholder="Mention identifiable features (stickers, minor scratches, keychains, initials, contents)..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={onCancel}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              <span>{isSubmitting ? 'Publishing...' : 'Publish Report'}</span>
              <Send size={15} />
            </button>
          </div>
        </form>

        {/* Live Preview Column */}
        <div className="preview-sticky">
          <h3>Live Card Preview</h3>
          <div className="item-card preview-card glass-panel">
            <div className="card-header">
              <span className={`badge ${isLost ? 'badge-lost' : 'badge-found'}`}>
                {isLost ? 'LOST ITEM' : 'FOUND ITEM'}
              </span>
              <span className="card-date">{eventDate || 'Today'}</span>
            </div>

            <h4 className="card-title">{title.trim() || 'Item Title Preview'}</h4>

            <div className="card-tags">
              <span className="tag">{category || 'Category'}</span>
              <span className="tag">{color.trim() || 'Color'}</span>
              <span className="tag">{brand.trim() || 'Brand'}</span>
            </div>

            <p className="card-desc">
              {description.trim() || 'Your description will preview here in real-time as you type...'}
            </p>

            <div className="card-footer">
              <div className="card-location">
                <MapPin size={13} />
                <span>{location.trim() || 'Campus Location'}</span>
              </div>
            </div>
          </div>

          <div className="matching-info-box glass-panel">
            <h4>💡 How Matching Works</h4>
            <p>
              Once published, our backend algorithm computes heuristic overlap scores
              against all opposing reports across campus and notifies you immediately.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
