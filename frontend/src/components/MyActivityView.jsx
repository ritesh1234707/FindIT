import React, { useState } from 'react';
import { MapPin } from 'lucide-react';

export default function MyActivityView({
  myItems,
  myClaims,
  onOpenDetailsModal
}) {
  const [activeTab, setActiveTab] = useState('reports');

  return (
    <div className="view-panel active animate-fade-in">
      <div className="view-header">
        <h2>My Campus Activity</h2>
        <p>Track all items you have reported and the ownership claims you have submitted.</p>
      </div>

      <div className="tab-pill-bar">
        <button
          className={`tab-pill-btn ${activeTab === 'reports' ? 'active' : ''}`}
          onClick={() => setActiveTab('reports')}
        >
          My Reported Items ({myItems.length})
        </button>

        <button
          className={`tab-pill-btn ${activeTab === 'claims' ? 'active' : ''}`}
          onClick={() => setActiveTab('claims')}
        >
          My Ownership Claims ({myClaims.length})
        </button>
      </div>

      {activeTab === 'reports' ? (
        <div className="items-grid">
          {myItems.length === 0 ? (
            <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }} className="glass-panel">
              You have not reported any lost or found items yet.
            </div>
          ) : (
            myItems.map(item => (
              <div key={item.itemId} className="item-card glass-panel">
                <div>
                  <div className="card-header">
                    <span className={`badge ${item.reportType === 'LOST' ? 'badge-lost' : 'badge-found'}`}>
                      {item.reportType}
                    </span>
                    <span className={`badge-status ${(item.status || 'open').toLowerCase()}`}>
                      {(item.status || 'OPEN').replace('_', ' ')}
                    </span>
                  </div>
                  <h3 className="card-title">{item.title}</h3>
                  <p className="card-desc">{item.description || 'No description provided.'}</p>
                </div>
                <div className="card-footer">
                  <div className="card-location">
                    <MapPin size={13} />
                    <span>{item.location}</span>
                  </div>
                  <button
                    className="btn btn-sm btn-secondary"
                    onClick={() => onOpenDetailsModal(item)}
                  >
                    Details
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      ) : (
        <div className="claims-list">
          {myClaims.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }} className="glass-panel">
              You have not submitted any ownership claims yet.
            </div>
          ) : (
            myClaims.map(c => (
              <div key={c.claimId} className="claim-card glass-panel">
                <div className="claim-info">
                  <div style={{ display: 'flex', gap: '10px', alignItems: 'center', marginBottom: '6px' }}>
                    <h4>Claim #{c.claimId} on {c.itemTitle || `Item #${c.itemId}`}</h4>
                    <span className={`badge-status ${(c.status || 'pending').toLowerCase()}`}>
                      {c.status}
                    </span>
                  </div>
                  <div className="claim-proof">
                    <strong>Your Verification:</strong> {c.verificationDetails}
                  </div>
                  {(c.reviewNotes || c.adminNotes) && (
                    <div style={{ fontSize: '12.5px', color: c.status === 'APPROVED' ? '#059669' : '#DC2626', marginTop: '6px', background: '#F8FAFC', padding: '6px 10px', borderRadius: '6px' }}>
                      <strong>Admin Response:</strong> {c.reviewNotes || c.adminNotes}
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
