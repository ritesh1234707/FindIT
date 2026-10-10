/**
 * FindIT — Campus Lost & Found System
 * Frontend Application Controller
 */

(function () {
    'use strict';

    const API_BASE = '/api';

    // Application State
    const state = {
        currentUser: null,
        users: [],
        currentView: 'feed',
        items: [],
        matches: [],
        claims: [],
        stats: null,
        filters: {
            search: '',
            type: 'ALL',
            category: 'ALL',
            status: 'OPEN'
        }
    };

    // ── API Helper ───────────────────────────────────────────────────────────
    async function apiRequest(endpoint, options = {}) {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...(options.headers || {})
            },
            ...options
        };

        try {
            const res = await fetch(`${API_BASE}${endpoint}`, config);
            const data = await res.json();
            if (!res.ok) {
                throw new Error(data.error || `HTTP error ${res.status}`);
            }
            return data;
        } catch (err) {
            console.error(`[API Error] ${endpoint}:`, err);
            showToast(err.message, 'error');
            throw err;
        }
    }

    // ── Toast Notification ───────────────────────────────────────────────────
    function showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        const icon = type === 'success' ? '✓' : type === 'error' ? '✗' : 'ℹ';
        toast.innerHTML = `<strong>${icon}</strong> <span>${escapeHtml(message)}</span>`;

        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(20px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    }

    // ── View Switching ───────────────────────────────────────────────────────
    function switchView(viewName) {
        state.currentView = viewName;

        // Update nav buttons
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.view === viewName);
        });

        // Update view panels
        document.querySelectorAll('.view-panel').forEach(panel => {
            panel.classList.toggle('active', panel.id === `view-${viewName}`);
        });

        // Trigger view-specific loads
        if (viewName === 'feed')     loadItems();
        if (viewName === 'matches')  loadMatches();
        if (viewName === 'my-items') loadMyActivity();
        if (viewName === 'admin')    loadAdminData();
    }

    // ── Authentication & User Session ────────────────────────────────────────
    async function initUsers() {
        try {
            const data = await apiRequest('/auth/users');
            state.users = data.users || [];

            // Populate the Quick Demo Switcher dropdown
            const select = document.getElementById('quick-user-select');
            select.innerHTML = '';
            state.users.forEach(u => {
                const opt = document.createElement('option');
                opt.value = u.userId;
                opt.textContent = `${u.fullName} (${u.role})`;
                select.appendChild(opt);
            });

            // Restore saved user or default to first student (Ritesh)
            const savedId = localStorage.getItem('findit_userId');
            const found = state.users.find(u => u.userId == savedId) || state.users[1] || state.users[0];
            setCurrentUser(found);

            select.addEventListener('change', (e) => {
                const selected = state.users.find(u => u.userId == e.target.value);
                if (selected) {
                    setCurrentUser(selected);
                    showToast(`Switched user to: ${selected.fullName}`, 'success');
                }
            });
        } catch (err) {
            console.warn('Could not load user list:', err);
        }
    }

    function setCurrentUser(user) {
        state.currentUser = user;
        localStorage.setItem('findit_userId', user.userId);

        // Update select value
        const select = document.getElementById('quick-user-select');
        if (select) select.value = user.userId;

        // Update navbar badge
        document.getElementById('nav-user-name').textContent = user.fullName;
        const roleBadge = document.getElementById('nav-user-role');
        roleBadge.textContent = user.role;
        roleBadge.className = `user-role-badge ${user.role.toLowerCase()}`;

        const avatar = document.getElementById('nav-user-avatar');
        avatar.textContent = (user.fullName || 'U').charAt(0).toUpperCase();

        // Admin nav button visibility / styling
        const adminBtn = document.getElementById('admin-nav-btn');
        if (adminBtn) {
            adminBtn.style.display = (user.role === 'ADMIN') ? 'flex' : 'none';
        }

        // Refresh views
        if (state.currentView === 'my-items') loadMyActivity();
        if (state.currentView === 'matches')  loadMatches();
        if (state.currentView === 'admin' && user.role !== 'ADMIN') switchView('feed');
        updateGlobalStats();
    }

    // ── Global Stats Ticker ──────────────────────────────────────────────────
    async function updateGlobalStats() {
        try {
            const data = await apiRequest('/dashboard/stats');
            state.stats = data.stats;
            if (state.stats) {
                document.getElementById('ticker-active-items').textContent =
                    (state.stats.totalLost || 0) + (state.stats.totalFound || 0);
                document.getElementById('ticker-recovery-rate').textContent =
                    `${state.stats.recoveryRate || 0}%`;
            }
        } catch (e) { /* ignore */ }
    }

    // ── VIEW 1: ITEMS FEED ───────────────────────────────────────────────────
    async function loadItems() {
        const grid = document.getElementById('items-grid');
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 40px;">Loading feed items...</div>';

        const params = new URLSearchParams();
        if (state.filters.type !== 'ALL') params.set('type', state.filters.type);
        if (state.filters.category !== 'ALL') params.set('category', state.filters.category);
        if (state.filters.status !== 'ALL') params.set('status', state.filters.status);
        if (state.filters.search.trim()) params.set('search', state.filters.search.trim());

        try {
            const res = await apiRequest(`/items?${params.toString()}`);
            state.items = res.items || [];
            renderItems(state.items);
            updateGlobalStats();
        } catch (err) {
            grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--accent-rose); padding: 40px;">Could not load items: ${err.message}</div>`;
        }
    }

    function renderItems(items) {
        const grid = document.getElementById('items-grid');
        const countLabel = document.getElementById('items-count-label');
        countLabel.textContent = `Showing ${items.length} ${items.length === 1 ? 'item' : 'items'}`;

        if (!items.length) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 60px 20px; color: var(--text-muted);">
                    <div style="font-size: 40px; margin-bottom: 12px;">🔍</div>
                    <h3>No items match your filters</h3>
                    <p style="font-size: 13.5px; margin-top: 6px;">Try adjusting your search keyword or category filter.</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = items.map(item => {
            const isLost = item.reportType === 'LOST';
            const badgeClass = isLost ? 'badge-lost' : 'badge-found';
            const badgeText = isLost ? 'LOST ITEM' : 'FOUND ITEM';
            const statusClass = (item.status || 'OPEN').toLowerCase();

            const canClaim = !isLost && item.status === 'OPEN' && item.userId !== state.currentUser?.userId;

            return `
                <div class="item-card glass-panel" data-id="${item.itemId}">
                    <div>
                        <div class="card-header">
                            <span class="badge ${badgeClass}">${badgeText}</span>
                            <span class="badge-status ${statusClass}">${formatStatus(item.status)}</span>
                        </div>
                        <h3 class="card-title">${escapeHtml(item.title)}</h3>
                        <div class="card-tags">
                            <span class="tag">${escapeHtml(item.category || 'General')}</span>
                            ${item.color ? `<span class="tag">${escapeHtml(item.color)}</span>` : ''}
                            ${item.brand ? `<span class="tag">${escapeHtml(item.brand)}</span>` : ''}
                        </div>
                        <p class="card-desc">${escapeHtml(item.description || 'No additional details provided.')}</p>
                    </div>
                    <div class="card-footer">
                        <div class="card-location">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                            <span>${escapeHtml(item.location || 'Campus')}</span>
                        </div>
                        <div class="card-actions">
                            ${canClaim ? `
                                <button class="btn btn-sm btn-primary" onclick="window.app.openClaimModal(${item.itemId})">
                                    Claim Item
                                </button>
                            ` : ''}
                            <button class="btn btn-sm btn-secondary" onclick="window.app.viewItemDetails(${item.itemId})">
                                Details
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    // ── VIEW 2: REPORT FORM & LIVE PREVIEW ────────────────────────────────────
    function initReportForm() {
        const form = document.getElementById('report-form');
        const titleInput = document.getElementById('report-title');
        const catSelect  = document.getElementById('report-category');
        const colorInput = document.getElementById('report-color');
        const brandInput = document.getElementById('report-brand');
        const dateInput  = document.getElementById('report-date');
        const locInput   = document.getElementById('report-location');
        const descInput  = document.getElementById('report-desc');

        // Set default date to today
        dateInput.value = new Date().toISOString().split('T')[0];

        // Type toggle radio clicks
        const toggleLost = document.getElementById('toggle-lost');
        const toggleFound = document.getElementById('toggle-found');

        toggleLost.addEventListener('click', () => {
            toggleLost.classList.add('active');
            toggleFound.classList.remove('active');
            toggleLost.querySelector('input').checked = true;
            updatePreview();
        });
        toggleFound.addEventListener('click', () => {
            toggleFound.classList.add('active');
            toggleLost.classList.remove('active');
            toggleFound.querySelector('input').checked = true;
            updatePreview();
        });

        // Realtime preview updates
        [titleInput, catSelect, colorInput, brandInput, dateInput, locInput, descInput].forEach(el => {
            el.addEventListener('input', updatePreview);
        });

        function updatePreview() {
            const isLost = form.reportType.value === 'LOST';
            const badge = document.getElementById('prev-badge');
            badge.className = `badge ${isLost ? 'badge-lost' : 'badge-found'}`;
            badge.textContent = isLost ? 'LOST ITEM' : 'FOUND ITEM';

            document.getElementById('prev-title').textContent = titleInput.value.trim() || 'Item Title Preview';
            document.getElementById('prev-category').textContent = catSelect.value || 'Category';
            document.getElementById('prev-color').textContent = colorInput.value.trim() || 'Color';
            document.getElementById('prev-brand').textContent = brandInput.value.trim() || 'Brand';
            document.getElementById('prev-desc').textContent = descInput.value.trim() || 'Your description will preview here in real-time as you type...';
            document.getElementById('prev-loc').textContent = locInput.value.trim() || 'Campus Location';
            document.getElementById('prev-date').textContent = dateInput.value || 'Today';
        }

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btn-submit-report');
            btn.disabled = true;
            btn.querySelector('span').textContent = 'Submitting...';

            try {
                const payload = {
                    userId: state.currentUser ? state.currentUser.userId : 1,
                    title: titleInput.value.trim(),
                    category: catSelect.value,
                    color: colorInput.value.trim() || null,
                    brand: brandInput.value.trim() || null,
                    description: descInput.value.trim(),
                    location: locInput.value.trim(),
                    eventDate: dateInput.value,
                    reportType: form.reportType.value
                };

                const res = await apiRequest('/items', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });

                showToast(`Report published successfully! Item ID: #${res.item.itemId}`, 'success');
                form.reset();
                dateInput.value = new Date().toISOString().split('T')[0];
                updatePreview();

                // Switch to Feed to see the new item
                switchView('feed');
            } catch (err) {
                /* Handled by apiRequest toast */
            } finally {
                btn.disabled = false;
                btn.querySelector('span').textContent = 'Publish Report';
            }
        });
    }

    // ── VIEW 3: SMART MATCHES ────────────────────────────────────────────────
    async function loadMatches() {
        const container = document.getElementById('matches-container');
        container.innerHTML = '<div style="text-align: center; color: var(--text-muted); padding: 40px;">Evaluating heuristic match scores...</div>';

        const userId = state.currentUser ? state.currentUser.userId : 2;
        try {
            const res = await apiRequest(`/matches?userId=${userId}`);
            state.matches = res.matches || [];
            renderMatches(state.matches);
        } catch (err) {
            container.innerHTML = `<div style="text-align: center; color: var(--accent-rose); padding: 40px;">Could not load matches: ${err.message}</div>`;
        }
    }

    function renderMatches(matches) {
        const container = document.getElementById('matches-container');
        if (!matches.length) {
            container.innerHTML = `
                <div style="text-align: center; padding: 60px 20px; color: var(--text-muted);" class="glass-panel">
                    <div style="font-size: 40px; margin-bottom: 12px;">⚡</div>
                    <h3>No High-Confidence Matches Found Yet</h3>
                    <p style="font-size: 13.5px; margin-top: 6px;">As students report more items, our matching algorithm compares categories, names, colors, locations, and dates.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = matches.map(m => {
            const scoreColor = m.matchScore >= 80 ? 'var(--accent-emerald)' : m.matchScore >= 50 ? 'var(--accent-cyan)' : 'var(--accent-amber)';

            return `
                <div class="match-card glass-panel">
                    <div class="match-item-pane">
                        <span class="pane-role lost">Lost Report (#${m.lostItemId})</span>
                        <h4 class="match-item-title">${escapeHtml(m.lostItemTitle)}</h4>
                        <span class="match-item-meta">Reported by student</span>
                    </div>

                    <div class="match-score-gauge">
                        <div class="gauge-score" style="color: ${scoreColor}">${m.matchScore}%</div>
                        <div class="gauge-label">Match Confidence</div>
                    </div>

                    <div class="match-item-pane">
                        <span class="pane-role found">Found Item (#${m.foundItemId})</span>
                        <h4 class="match-item-title">${escapeHtml(m.foundItemTitle)}</h4>
                        <span class="match-item-meta">Reported in campus</span>
                    </div>

                    <div style="display: flex; justify-content: flex-end;">
                        <button class="btn btn-sm btn-primary" onclick="window.app.openClaimModal(${m.foundItemId})">
                            Claim Found
                        </button>
                    </div>

                    <div class="match-reasons-snippet">
                        <strong>Match Factor Breakdown:</strong> ${escapeHtml(m.matchingReasons || 'Category and keyword correlation')}
                    </div>
                </div>
            `;
        }).join('');
    }

    // ── VIEW 4: MY ACTIVITY & CLAIMS ─────────────────────────────────────────
    async function loadMyActivity() {
        const userId = state.currentUser ? state.currentUser.userId : 2;

        try {
            const [itemsRes, claimsRes] = await Promise.all([
                apiRequest(`/items/my?userId=${userId}`),
                apiRequest(`/claims?userId=${userId}`)
            ]);

            const myItems = itemsRes.items || [];
            const myClaims = claimsRes.claims || [];

            document.getElementById('count-my-reports').textContent = myItems.length;
            document.getElementById('count-my-claims').textContent = myClaims.length;

            // Render My Reports
            const reportsGrid = document.getElementById('my-reports-list');
            if (!myItems.length) {
                reportsGrid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-muted);">You have not reported any lost or found items yet.</div>';
            } else {
                reportsGrid.innerHTML = myItems.map(item => `
                    <div class="item-card glass-panel">
                        <div>
                            <div class="card-header">
                                <span class="badge ${item.reportType === 'LOST' ? 'badge-lost' : 'badge-found'}">${item.reportType}</span>
                                <span class="badge-status ${item.status.toLowerCase()}">${formatStatus(item.status)}</span>
                            </div>
                            <h3 class="card-title">${escapeHtml(item.title)}</h3>
                            <p class="card-desc">${escapeHtml(item.description || '')}</p>
                        </div>
                        <div class="card-footer">
                            <span class="card-location">${escapeHtml(item.location)}</span>
                            <button class="btn btn-sm btn-secondary" onclick="window.app.viewItemDetails(${item.itemId})">Details</button>
                        </div>
                    </div>
                `).join('');
            }

            // Render My Claims
            const claimsList = document.getElementById('my-claims-list');
            if (!myClaims.length) {
                claimsList.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--text-muted);" class="glass-panel">You have not submitted any ownership claims yet.</div>';
            } else {
                claimsList.innerHTML = myClaims.map(c => `
                    <div class="claim-card glass-panel">
                        <div class="claim-info">
                            <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 6px;">
                                <h4>Claim #${c.claimId} on Item #${c.itemId}</h4>
                                <span class="badge-status ${c.status.toLowerCase()}">${c.status}</span>
                            </div>
                            <div class="claim-proof">
                                <strong>Your Verification:</strong> ${escapeHtml(c.verificationDetails)}
                            </div>
                            ${c.adminNotes ? `<div style="font-size: 12.5px; color: var(--accent-amber); margin-top: 6px;"><strong>Admin Response:</strong> ${escapeHtml(c.adminNotes)}</div>` : ''}
                        </div>
                    </div>
                `).join('');
            }
        } catch (err) {
            console.error('Error loading my activity:', err);
        }
    }

    // ── VIEW 5: ADMIN HUB ────────────────────────────────────────────────────
    async function loadAdminData() {
        try {
            const [statsRes, claimsRes] = await Promise.all([
                apiRequest('/dashboard/stats'),
                apiRequest('/claims')
            ]);

            const stats = statsRes.stats || {};
            document.getElementById('stat-total-users').textContent = stats.totalUsers || 0;
            document.getElementById('stat-total-lost').textContent  = stats.totalLost || 0;
            document.getElementById('stat-total-found').textContent = stats.totalFound || 0;
            document.getElementById('stat-returned').textContent    = stats.returned || 0;

            const rate = stats.recoveryRate || 0;
            document.getElementById('admin-rate-fill').style.width = `${rate}%`;
            document.getElementById('admin-rate-label').textContent = `${rate}%`;

            // Claims Review Table
            const tbody = document.getElementById('admin-claims-tbody');
            const claims = claimsRes.claims || [];

            if (!claims.length) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 30px; color: var(--text-muted);">No claims in the system.</td></tr>';
                return;
            }

            tbody.innerHTML = claims.map(c => {
                const isPending = c.status === 'PENDING';
                return `
                    <tr>
                        <td><strong>#${c.claimId}</strong></td>
                        <td>Item #${c.itemId}</td>
                        <td>Claimant #${c.claimantId}</td>
                        <td style="max-width: 280px; font-size: 12.5px;">${escapeHtml(c.verificationDetails)}</td>
                        <td><span class="badge-status ${c.status.toLowerCase()}">${c.status}</span></td>
                        <td>
                            ${isPending ? `
                                <div style="display: flex; gap: 6px;">
                                    <button class="btn btn-sm btn-success" onclick="window.app.reviewClaim(${c.claimId}, 'APPROVE')">Approve</button>
                                    <button class="btn btn-sm btn-danger" onclick="window.app.reviewClaim(${c.claimId}, 'REJECT')">Reject</button>
                                </div>
                            ` : `<span style="font-size: 12px; color: var(--text-muted);">Resolved</span>`}
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            console.error('Error loading admin data:', err);
        }
    }

    // ── MODALS & ACTIONS ─────────────────────────────────────────────────────
    function openClaimModal(itemId) {
        const item = state.items.find(i => i.itemId == itemId);
        document.getElementById('claim-item-id').value = itemId;

        const summary = document.getElementById('modal-claim-item-summary');
        if (item) {
            summary.innerHTML = `
                <strong>Claiming Item:</strong> ${escapeHtml(item.title)} (${escapeHtml(item.category)})<br>
                <small style="color: var(--text-secondary)">Found near: ${escapeHtml(item.location)}</small>
            `;
        } else {
            summary.innerHTML = `<strong>Claiming Item #${itemId}</strong>`;
        }

        document.getElementById('claim-verification').value = '';
        document.getElementById('modal-claim').classList.add('active');
    }

    function closeModal(modalId) {
        document.getElementById(modalId).classList.remove('active');
    }

    async function submitClaimForm(e) {
        e.preventDefault();
        const itemId = parseInt(document.getElementById('claim-item-id').value);
        const verification = document.getElementById('claim-verification').value.trim();

        if (!verification) {
            showToast('Please provide verification details.', 'error');
            return;
        }

        try {
            await apiRequest('/claims', {
                method: 'POST',
                body: JSON.stringify({
                    itemId: itemId,
                    claimantId: state.currentUser ? state.currentUser.userId : 2,
                    verificationDetails: verification
                })
            });

            showToast('Claim submitted for Admin review!', 'success');
            closeModal('modal-claim');
            loadItems();
        } catch (err) {
            /* Handled */
        }
    }

    async function reviewClaim(claimId, action) {
        const notes = prompt(`Optional ${action.toLowerCase()} notes for claimant:`) || '';

        try {
            await apiRequest('/claims/review', {
                method: 'POST',
                body: JSON.stringify({
                    claimId: claimId,
                    adminId: state.currentUser ? state.currentUser.userId : 1,
                    action: action,
                    notes: notes
                })
            });

            showToast(`Claim #${claimId} was ${action.toLowerCase()}d!`, 'success');
            loadAdminData();
            updateGlobalStats();
        } catch (err) {
            /* Handled */
        }
    }

    async function viewItemDetails(itemId) {
        try {
            const res = await apiRequest(`/items/${itemId}`);
            const item = res.item;
            if (!item) return;

            document.getElementById('details-modal-title').textContent = item.title;
            const body = document.getElementById('details-modal-body');
            body.innerHTML = `
                <div style="display: flex; gap: 8px; margin-bottom: 14px;">
                    <span class="badge ${item.reportType === 'LOST' ? 'badge-lost' : 'badge-found'}">${item.reportType}</span>
                    <span class="badge-status ${item.status.toLowerCase()}">${formatStatus(item.status)}</span>
                    <span class="tag">${escapeHtml(item.category)}</span>
                </div>
                <p style="margin-bottom: 14px; font-size: 14px;">${escapeHtml(item.description || 'No description provided.')}</p>
                <div style="background: rgba(0,0,0,0.25); padding: 14px; border-radius: var(--radius-md); font-size: 13px; line-height: 1.8;">
                    <div><strong>Color:</strong> ${escapeHtml(item.color || 'Not specified')}</div>
                    <div><strong>Brand:</strong> ${escapeHtml(item.brand || 'Not specified')}</div>
                    <div><strong>Location:</strong> ${escapeHtml(item.location || 'Campus')}</div>
                    <div><strong>Event Date:</strong> ${item.eventDate || 'N/A'}</div>
                </div>
            `;

            const footer = document.getElementById('details-modal-footer');
            const canClaim = item.reportType === 'FOUND' && item.status === 'OPEN' && item.userId !== state.currentUser?.userId;

            footer.innerHTML = `
                <button type="button" class="btn btn-secondary" onclick="window.app.closeModal('modal-details')">Close</button>
                ${canClaim ? `
                    <button type="button" class="btn btn-primary" onclick="window.app.closeModal('modal-details'); window.app.openClaimModal(${item.itemId})">
                        Claim Item
                    </button>
                ` : ''}
            `;

            document.getElementById('modal-details').classList.add('active');
        } catch (err) {
            /* Handled */
        }
    }

    // ── Helper Utilities ────────────────────────────────────────────────────
    function formatStatus(status) {
        if (!status) return 'Available';
        return status.replace('_', ' ');
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    // ── Initial Setup & Listeners ───────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', () => {
        // Nav button clicks
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.addEventListener('click', () => switchView(btn.dataset.view));
        });

        // Brand home click
        document.getElementById('brand-home-btn').addEventListener('click', () => switchView('feed'));

        // Feed Filter inputs
        document.getElementById('filter-search').addEventListener('input', (e) => {
            state.filters.search = e.target.value;
            loadItems();
        });

        document.querySelectorAll('.type-filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.type-filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                state.filters.type = btn.dataset.type;
                loadItems();
            });
        });

        document.getElementById('filter-category').addEventListener('change', (e) => {
            state.filters.category = e.target.value;
            loadItems();
        });

        document.getElementById('filter-status').addEventListener('change', (e) => {
            state.filters.status = e.target.value;
            loadItems();
        });

        // Recalculate matches button
        document.getElementById('btn-recalculate-matches').addEventListener('click', async () => {
            showToast('Recalculating matches across campus...', 'info');
            try {
                await apiRequest('/matches/recalculate', { method: 'POST' });
                showToast('Matching complete!', 'success');
                loadMatches();
            } catch (err) {}
        });

        // Activity tab pill switcher
        document.querySelectorAll('.tab-pill-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.tab-pill-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                const targetTab = btn.dataset.tab;
                document.querySelectorAll('.activity-tab-content').forEach(c => {
                    c.classList.toggle('active', c.id === `tab-${targetTab}`);
                });
            });
        });

        // Claim form submit
        document.getElementById('claim-form').addEventListener('submit', submitClaimForm);

        // Report Form setup
        initReportForm();

        // Initialize Users & Default Feed
        initUsers();
        loadItems();
    });

    // Global exports for inline HTML event handlers
    window.app = {
        switchView,
        openClaimModal,
        closeModal,
        viewItemDetails,
        reviewClaim
    };

})();
