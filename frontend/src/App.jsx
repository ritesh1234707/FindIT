import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import FeedView from './components/FeedView';
import ReportView from './components/ReportView';
import MatchesView from './components/MatchesView';
import MyActivityView from './components/MyActivityView';
import AdminHubView from './components/AdminHubView';
import Footer from './components/Footer';
import ClaimModal from './components/ClaimModal';
import ItemDetailsModal from './components/ItemDetailsModal';
import EditItemModal from './components/EditItemModal';
import AuthScreen from './components/AuthScreen';
import './App.css';

export default function App() {
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem('findit_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [currentView, setCurrentView] = useState(() => {
    return currentUser?.role === 'ADMIN' ? 'admin' : 'home';
  });


  const [items, setItems] = useState([]);
  const [matches, setMatches] = useState([]);
  const [myItems, setMyItems] = useState([]);
  const [myClaims, setMyClaims] = useState([]);
  const [allClaims, setAllClaims] = useState([]);
  const [stats, setStats] = useState(null);

  // Modals
  const [claimTargetItem, setClaimTargetItem] = useState(null);
  const [detailsTargetItem, setDetailsTargetItem] = useState(null);
  const [editTargetItem, setEditTargetItem] = useState(null);

  // Toasts
  const [toasts, setToasts] = useState([]);

  function showToast(message, type = 'info') {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 3500);
  }

  // 1. Initial Load: Global Stats
  useEffect(() => {
    async function init() {
      try {
        const res = await fetch('/api/dashboard/stats');
        const data = await res.json();
        if (data.stats) {
          setStats(data.stats);
        }
      } catch (err) {
        console.error('Init error:', err);
      }
    }
    init();
  }, []);

  // 2. Fetch Data whenever user changes
  useEffect(() => {
    if (!currentUser) return;

    loadItems();
    loadStats();
    loadMatches();
    loadUserActivity();

    if (currentUser.role === 'ADMIN') {
      loadAllClaims();
    }
  }, [currentUser]);

  async function loadItems() {
    try {
      const res = await fetch('/api/items');
      const data = await res.json();
      if (data.items) setItems(data.items);
    } catch (e) {
      console.error(e);
    }
  }

  async function loadStats() {
    try {
      const res = await fetch('/api/dashboard/stats');
      const data = await res.json();
      if (data.stats) setStats(data.stats);
    } catch (e) {
      console.error(e);
    }
  }

  async function loadMatches() {
    if (!currentUser) return;
    try {
      const res = await fetch(`/api/matches?userId=${currentUser.userId}`);
      const data = await res.json();
      if (data.matches) setMatches(data.matches);
    } catch (e) {
      console.error(e);
    }
  }

  async function loadUserActivity() {
    if (!currentUser) return;
    try {
      const [itemsRes, claimsRes] = await Promise.all([
        fetch(`/api/items/my?userId=${currentUser.userId}`).then(r => r.json()),
        fetch(`/api/claims?userId=${currentUser.userId}`).then(r => r.json())
      ]);
      if (itemsRes.items) setMyItems(itemsRes.items);
      if (claimsRes.claims) setMyClaims(claimsRes.claims);
    } catch (e) {
      console.error(e);
    }
  }

  async function loadAllClaims() {
    try {
      const res = await fetch('/api/claims');
      const data = await res.json();
      if (data.claims) setAllClaims(data.claims);
    } catch (e) {
      console.error(e);
    }
  }

  // Handle Login Success from AuthScreen
  function handleLoginSuccess(user) {
    setCurrentUser(user);
    localStorage.setItem('findit_user', JSON.stringify(user));
    setCurrentView(user.role === 'ADMIN' ? 'admin' : 'home');
    showToast(`Welcome back, ${user.fullName}!`, 'success');
  }

  // Handle Logout
  function handleLogout() {
    setCurrentUser(null);
    localStorage.removeItem('findit_user');
    showToast('Signed out successfully.', 'info');
  }



  async function handleRecalculateMatches() {
    showToast('Recalculating matches across all items...', 'info');
    try {
      await fetch('/api/matches/recalculate', { method: 'POST' });
      await loadMatches();
      showToast('Smart match calculation completed!', 'success');
    } catch (err) {
      showToast('Recalculation error', 'error');
    }
  }

  async function handleSubmitClaim(itemId, verification) {
    try {
      const res = await fetch('/api/claims', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          itemId,
          claimantId: currentUser?.userId || 2,
          verificationDetails: verification
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to submit claim');

      showToast('Ownership claim submitted for Admin review!', 'success');
      setClaimTargetItem(null);
      loadItems();
      loadUserActivity();
      loadStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  async function handleReviewClaim(claimId, action) {
    const input = prompt(`Optional ${action.toLowerCase()} notes for the student:`, '');
    if (input === null) return; // User cancelled
    const notes = input.trim();
    try {
      const res = await fetch('/api/claims/review', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          claimId,
          adminId: currentUser?.userId || 1,
          action,
          notes
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to review claim');

      showToast(`Claim #${claimId} was ${action.toLowerCase()}d!`, 'success');
      loadAllClaims();
      loadItems();
      loadStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  // ── ADMIN MODERATION: Delete & Edit Items ────────────────────────
  async function handleDeleteItem(itemId) {
    if (!window.confirm(`Are you sure you want to permanently delete item report #${itemId}? This cannot be undone.`)) {
      return;
    }

    try {
      const res = await fetch(`/api/items/${itemId}`, { method: 'DELETE' });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to delete item');

      showToast(`Item #${itemId} was deleted successfully.`, 'info');
      if (detailsTargetItem?.itemId === itemId) setDetailsTargetItem(null);
      if (editTargetItem?.itemId === itemId) setEditTargetItem(null);
      loadItems();
      loadStats();
      loadUserActivity();
      loadMatches();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  async function handleSaveEditItem(itemId, updatedData) {
    try {
      const res = await fetch(`/api/items/${itemId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedData)
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to update item');

      showToast(`Item #${itemId} updated successfully!`, 'success');
      setEditTargetItem(null);
      if (detailsTargetItem?.itemId === itemId) {
        setDetailsTargetItem(data.item);
      }
      loadItems();
      loadStats();
      loadUserActivity();
      loadMatches();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  // ── GATEKEEPER: If not logged in, render the Auth Screen ─────────
  if (!currentUser) {
    return (
      <div className="app-root">
        <div className="ambient-glow glow-top-left" />
        <div className="ambient-glow glow-bottom-right" />
        <AuthScreen onLoginSuccess={handleLoginSuccess} />
        <div className="toast-container">
          {toasts.map(t => (
            <div key={t.id} className={`toast ${t.type}`}>
              <strong>{t.type === 'success' ? '✓' : t.type === 'error' ? '✗' : 'ℹ'}</strong>
              <span>{t.message}</span>
            </div>
          ))}
        </div>
      </div>
    );
  }

  // ── AUTHENTICATED DASHBOARD ─────────────────────────────────────
  return (
    <div className="app-root">

      {/* Top Navbar */}
      <Navbar
        currentView={currentView}
        onSwitchView={setCurrentView}
        currentUser={currentUser}
        onLogout={handleLogout}
      />

      {/* Main Content Router */}
      <main className="content-wrapper">
        {(currentView === 'home' || currentView === 'lost' || currentView === 'found' || currentView === 'feed') && (
          <FeedView
            currentView={currentView === 'feed' ? 'home' : currentView}
            items={items}
            stats={stats}
            matches={matches}
            myItems={myItems}
            currentUser={currentUser}
            onSwitchView={setCurrentView}
            onOpenClaimModal={(item) => setClaimTargetItem(item)}
            onOpenDetailsModal={(item) => setDetailsTargetItem(item)}
            onEditItem={(item) => setEditTargetItem(item)}
            onDeleteItem={handleDeleteItem}
            onRefreshItems={loadItems}
          />
        )}

        {(currentView === 'report' || currentView === 'report-lost' || currentView === 'report-found') && (
          <ReportView
            currentUser={currentUser}
            initialType={currentView === 'report-found' ? 'FOUND' : 'LOST'}
            onReportSubmitted={(newItem) => {
              showToast(`Report published! Item ID: #${newItem.itemId}`, 'success');
              loadItems();
              loadUserActivity();
              loadStats();
              setCurrentView(newItem.reportType === 'FOUND' ? 'found' : 'lost');
            }}
            onCancel={() => setCurrentView('home')}
          />
        )}

        {currentView === 'matches' && (
          <MatchesView
            matches={matches}
            onRecalculate={handleRecalculateMatches}
            onOpenClaimModal={(item) => setClaimTargetItem(item)}
          />
        )}

        {currentView === 'my-items' && (
          <MyActivityView
            myItems={myItems}
            myClaims={myClaims}
            onOpenDetailsModal={(item) => setDetailsTargetItem(item)}
          />
        )}

        {currentView === 'admin' && currentUser?.role === 'ADMIN' && (
          <AdminHubView
            stats={stats}
            allClaims={allClaims}
            onReviewClaim={handleReviewClaim}
          />
        )}
      </main>

      {/* Global Figma 4-Column Footer */}
      <Footer onSwitchView={setCurrentView} currentUser={currentUser} />

      {/* Modals */}
      {claimTargetItem && (
        <ClaimModal
          item={claimTargetItem}
          currentUser={currentUser}
          onClose={() => setClaimTargetItem(null)}
          onSubmitClaim={handleSubmitClaim}
        />
      )}

      {detailsTargetItem && (
        <ItemDetailsModal
          item={detailsTargetItem}
          currentUser={currentUser}
          onClose={() => setDetailsTargetItem(null)}
          onOpenClaimModal={(item) => setClaimTargetItem(item)}
          onEditItem={(item) => setEditTargetItem(item)}
          onDeleteItem={handleDeleteItem}
        />
      )}

      {editTargetItem && (
        <EditItemModal
          item={editTargetItem}
          onClose={() => setEditTargetItem(null)}
          onSave={handleSaveEditItem}
        />
      )}

      {/* Floating Toast Alerts */}
      <div className="toast-container">
        {toasts.map(t => (
          <div key={t.id} className={`toast ${t.type}`}>
            <strong>{t.type === 'success' ? '✓' : t.type === 'error' ? '✗' : 'ℹ'}</strong>
            <span>{t.message}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
