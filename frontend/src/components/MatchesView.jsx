import React, { useState } from 'react';
import { RefreshCw, Zap } from 'lucide-react';

export default function MatchesView({
  matches,
  onRecalculate,
  onOpenClaimModal
}) {
  const [isRecalculating, setIsRecalculating] = useState(false);

  async function handleRecalculateClick() {
    setIsRecalculating(true);
    await onRecalculate();
    setIsRecalculating(false);
  }

  return (
    <div className="view-panel active animate-fade-in">
      <div className="view-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <div>
          <h2>⚡ Heuristic Smart Matches</h2>
          <p>
            Automated recommendations comparing open lost and found reports
            using five-factor weighted heuristic scoring.
          </p>
        </div>

        <button
          className="btn btn-secondary"
          onClick={handleRecalculateClick}
          disabled={isRecalculating}
        >
          <RefreshCw size={15} className={isRecalculating ? 'spin-anim' : ''} />
          <span>{isRecalculating ? 'Recalculating...' : 'Recalculate All Matches'}</span>
        </button>
      </div>

      {/* Algorithm Weights Banner */}
      <div className="algorithm-factors-bar glass-panel">
        <span className="factors-title">Scoring Weights:</span>
        <span className="factor-badge">Category: <strong>35%</strong></span>
        <span className="factor-badge">Keywords: <strong>25%</strong></span>
        <span className="factor-badge">Color: <strong>15%</strong></span>
        <span className="factor-badge">Location: <strong>15%</strong></span>
        <span className="factor-badge">Date Proximity: <strong>10%</strong></span>
      </div>

      {/* Matches List */}
      <div className="matches-container">
        {matches.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '60px 20px', color: 'var(--text-muted)' }} className="glass-panel">
            <Zap size={40} style={{ margin: '0 auto 12px', color: 'var(--accent-amber)' }} />
            <h3>No High-Confidence Matches Found Yet</h3>
            <p style={{ fontSize: '13.5px', marginTop: '6px' }}>
              As students report more items, our matching engine flags candidates with scores ≥ 30%.
            </p>
          </div>
        ) : (
          matches.map(m => {
            const scoreColor =
              m.matchScore >= 80 ? 'var(--accent-emerald)' :
              m.matchScore >= 50 ? 'var(--accent-cyan)' : 'var(--accent-amber)';

            return (
              <div key={m.matchId || `${m.lostItemId}-${m.foundItemId}`} className="match-card glass-panel">
                <div className="match-item-pane">
                  <span className="pane-role lost">Lost Report (#{m.lostItemId})</span>
                  <h4 className="match-item-title">{m.lostItemTitle}</h4>
                  <span className="match-item-meta">Reported by student</span>
                </div>

                <div className="match-score-gauge">
                  <div className="gauge-score" style={{ color: scoreColor }}>
                    {m.matchScore}%
                  </div>
                  <div className="gauge-label">Match Score</div>
                </div>

                <div className="match-item-pane">
                  <span className="pane-role found">Found Item (#{m.foundItemId})</span>
                  <h4 className="match-item-title">{m.foundItemTitle}</h4>
                  <span className="match-item-meta">Reported in campus</span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button
                    className="btn btn-sm btn-primary"
                    onClick={() => onOpenClaimModal({ itemId: m.foundItemId, title: m.foundItemTitle })}
                  >
                    Claim Found
                  </button>
                </div>

                <div className="match-reasons-snippet">
                  <strong>Factor Breakdown:</strong> {m.matchingReasons || 'Category and keyword correlation'}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
