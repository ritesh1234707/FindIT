import React from 'react';
import { Users, AlertCircle, CheckCircle, RotateCcw } from 'lucide-react';

export default function AdminHubView({
  stats,
  allClaims,
  onReviewClaim
}) {
  const rate = stats?.recoveryRate || 0;

  return (
    <div className="view-panel active animate-fade-in">
      <div className="view-header">
        <div className="admin-badge">Admin Access</div>
        <h2>Campus Administration Hub</h2>
        <p>Live database statistics and student ownership claim verification queue.</p>
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
            Percentage of reported items successfully restored to their rightful owners.
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
        <div>
          <h3>Pending Ownership Claims Review</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px', marginTop: '2px' }}>
            Verify evidence provided by students before approving return.
          </p>
        </div>

        <div className="table-responsive">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Claim ID</th>
                <th>Item ID</th>
                <th>Claimant</th>
                <th>Verification Evidence</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {allClaims.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
                    No claims submitted in the system.
                  </td>
                </tr>
              ) : (
                allClaims.map(c => {
                  const isPending = c.status === 'PENDING';
                  return (
                    <tr key={c.claimId}>
                      <td><strong>#{c.claimId}</strong></td>
                      <td>Item #{c.itemId}</td>
                      <td>User #{c.claimantId}</td>
                      <td style={{ maxWidth: '280px', fontSize: '12.5px' }}>
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
                            >
                              Approve
                            </button>
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => onReviewClaim(c.claimId, 'REJECT')}
                            >
                              Reject
                            </button>
                          </div>
                        ) : (
                          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Resolved</span>
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
