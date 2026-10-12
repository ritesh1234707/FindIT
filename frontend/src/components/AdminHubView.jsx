import React, { useState } from 'react';
import { Users, AlertCircle, CheckCircle, RotateCcw, ShieldCheck, Check, X, Filter } from 'lucide-react';

export default function AdminHubView({
  stats,
  allClaims = [],
  onReviewClaim
}) {
  const [filter, setFilter] = useState('ALL'); // 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'
  const rate = stats?.recoveryRate || 0;

  const pendingCount = allClaims.filter(c => c.status === 'PENDING').length;
  const approvedCount = allClaims.filter(c => c.status === 'APPROVED').length;
  const rejectedCount = allClaims.filter(c => c.status === 'REJECTED').length;

  const displayedClaims = allClaims.filter(c => {
    if (filter === 'ALL') return true;
    return c.status === filter;
  });

  return (
    <div className="view-panel active animate-fade-in">
      <div className="view-header">
        <div className="admin-badge">
          <ShieldCheck size={13} style={{ display: 'inline', marginRight: 4, verticalAlign: 'middle' }} />
          Campus Administration Portal
        </div>
        <h2>Campus Administration Hub</h2>
        <p>Live campus statistics, claim verification queue, and asset recovery metrics.</p>
      </div>

      {/* Metric Cards */}
      <div className="stats-grid">
        <div className="stat-card glass-panel">
          <div className="stat-icon-wrapper purple">
            <Users size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Active Users</span>
            <span className="stat-value">{stats?.totalUsers || 0}</span>
          </div>
        </div>

        <div className="stat-card glass-panel">
          <div className="stat-icon-wrapper rose">
            <AlertCircle size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Open Lost Reports</span>
            <span className="stat-value">{stats?.totalLost || 0}</span>
          </div>
        </div>

        <div className="stat-card glass-panel">
          <div className="stat-icon-wrapper cyan">
            <CheckCircle size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Open Found Items</span>
            <span className="stat-value">{stats?.totalFound || 0}</span>
          </div>
        </div>

        <div className="stat-card glass-panel">
          <div className="stat-icon-wrapper emerald">
            <RotateCcw size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Returned Items</span>
            <span className="stat-value">{stats?.returned || 0}</span>
          </div>
        </div>
      </div>

      {/* Recovery Rate Banner */}
      <div className="recovery-banner glass-panel">
        <div>
          <h3>Campus Recovery Rate</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
            Percentage of reported items successfully restored to rightful student owners.
          </p>
        </div>
        <div className="recovery-meter">
          <div className="meter-bar-track">
            <div className="meter-bar-fill" style={{ width: `${rate}%` }} />
          </div>
          <span className="meter-percent">{rate}%</span>
        </div>
      </div>

      {/* Claims Review Table */}
      <div className="admin-table-section glass-panel">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <h3>Ownership Claims Review Queue</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '13px', marginTop: '2px' }}>
              Verify student evidence and proof before approving ownership.
            </p>
          </div>

          {/* Filter Pills */}
          <div style={{ display: 'flex', gap: '6px' }}>
            <button
              type="button"
              className={`filter-pill ${filter === 'ALL' ? 'active' : ''}`}
              onClick={() => setFilter('ALL')}
            >
              All ({allClaims.length})
            </button>
            <button
              type="button"
              className={`filter-pill ${filter === 'PENDING' ? 'active' : ''}`}
              onClick={() => setFilter('PENDING')}
            >
              Pending ({pendingCount})
            </button>
            <button
              type="button"
              className={`filter-pill ${filter === 'APPROVED' ? 'active' : ''}`}
              onClick={() => setFilter('APPROVED')}
            >
              Approved ({approvedCount})
            </button>
            <button
              type="button"
              className={`filter-pill ${filter === 'REJECTED' ? 'active' : ''}`}
              onClick={() => setFilter('REJECTED')}
            >
              Rejected ({rejectedCount})
            </button>
          </div>
        </div>

        <div className="table-responsive" style={{ marginTop: '18px' }}>
          <table className="admin-table">
            <thead>
              <tr>
                <th>Claim ID</th>
                <th>Item</th>
                <th>Claimant</th>
                <th>Verification Evidence</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayedClaims.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '36px', color: 'var(--text-muted)' }}>
                    No {filter !== 'ALL' ? filter.toLowerCase() : ''} claims in the system.
                  </td>
                </tr>
              ) : (
                displayedClaims.map(c => {
                  const isPending = c.status === 'PENDING';
                  return (
                    <tr key={c.claimId}>
                      <td><strong>#{c.claimId}</strong></td>
                      <td>
                        <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                          {c.itemTitle || `Item #${c.itemId}`}
                        </span>
                        <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)' }}>
                          Item ID #{c.itemId}
                        </span>
                      </td>
                      <td>
                        <span style={{ fontWeight: 500 }}>
                          {c.claimantName || `Student #${c.claimantId}`}
                        </span>
                      </td>
                      <td style={{ maxWidth: '280px', fontSize: '12.5px', color: 'var(--text-secondary)' }}>
                        {c.verificationDetails}
                      </td>
                      <td>
                        <span className={`badge-status ${(c.status || '').toLowerCase()}`}>
                          {c.status}
                        </span>
                      </td>
                      <td>
                        {isPending ? (
                          <div style={{ display: 'flex', gap: '8px' }}>
                            <button
                              className="btn btn-sm btn-success"
                              onClick={() => onReviewClaim(c.claimId, 'APPROVE')}
                              title="Approve claim"
                            >
                              <Check size={14} style={{ marginRight: 3, verticalAlign: 'middle' }} /> Approve
                            </button>
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => onReviewClaim(c.claimId, 'REJECT')}
                              title="Reject claim"
                            >
                              <X size={14} style={{ marginRight: 3, verticalAlign: 'middle' }} /> Reject
                            </button>
                          </div>
                        ) : (
                          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                            {c.reviewNotes ? `Notes: ${c.reviewNotes}` : 'Resolved'}
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
