package com.findit.service;

import com.findit.dao.ClaimDAO;
import com.findit.dao.ItemDAO;
import com.findit.model.Claim;
import com.findit.model.Item;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for claim submission and admin review.
 *
 * Claim workflow:
 *   Student submits claim → PENDING, item → CLAIM_PENDING
 *   Admin approves        → APPROVED, item → CLAIMED
 *   Admin rejects         → REJECTED, item → OPEN  (back to available)
 *   Admin confirms return → item → RETURNED
 */
public class ClaimService {

    private final ClaimDAO claimDAO = new ClaimDAO();
    private final ItemDAO  itemDAO  = new ItemDAO();

    public Claim submitClaim(int itemId, int claimantId, String verificationDetails) {
        if (verificationDetails == null || verificationDetails.isBlank()) {
            throw new IllegalArgumentException("Please provide ownership verification details.");
        }
        try {
            Item item = itemDAO.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));

            if (!item.isFound())
                throw new IllegalArgumentException("You can only claim FOUND items.");
            if (item.getUserId() == claimantId)
                throw new IllegalArgumentException("You cannot claim an item you reported yourself.");
            if (item.getStatus() != Item.ItemStatus.OPEN)
                throw new IllegalArgumentException("This item is not currently available for claims.");
            if (claimDAO.hasActiveClaim(itemId, claimantId))
                throw new IllegalArgumentException("You already have a pending claim for this item.");

            Claim claim = new Claim(itemId, claimantId, verificationDetails.trim());
            int id = claimDAO.save(claim);
            claim.setClaimId(id);
            itemDAO.updateStatus(itemId, Item.ItemStatus.CLAIM_PENDING);
            return claim;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to submit claim: " + e.getMessage(), e);
        }
    }

    public void approveClaim(int claimId, int adminId, String notes) {
        try {
            Claim claim = claimDAO.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found."));
            if (!claim.isPending())
                throw new IllegalArgumentException("Claim is not in PENDING status.");
            claimDAO.updateStatus(claimId, Claim.ClaimStatus.APPROVED, adminId, notes);
            itemDAO.updateStatus(claim.getItemId(), Item.ItemStatus.CLAIMED);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to approve claim: " + e.getMessage(), e);
        }
    }

    public void rejectClaim(int claimId, int adminId, String notes) {
        try {
            Claim claim = claimDAO.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found."));
            if (!claim.isPending())
                throw new IllegalArgumentException("Claim is not in PENDING status.");
            claimDAO.updateStatus(claimId, Claim.ClaimStatus.REJECTED, adminId, notes);
            itemDAO.updateStatus(claim.getItemId(), Item.ItemStatus.OPEN); // reopen
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reject claim: " + e.getMessage(), e);
        }
    }

    public void confirmReturn(int itemId) {
        try { itemDAO.updateStatus(itemId, Item.ItemStatus.RETURNED); }
        catch (SQLException e) { throw new RuntimeException("Failed to confirm return.", e); }
    }

    public List<Claim> getMyClaims(int userId) {
        try { return claimDAO.findByClaimantId(userId); }
        catch (SQLException e) { throw new RuntimeException("Failed to load claims.", e); }
    }

    public List<Claim> getPendingClaims() {
        try { return claimDAO.findPending(); }
        catch (SQLException e) { throw new RuntimeException("Failed to load pending claims.", e); }
    }

    public List<Claim> getAllClaims() {
        try { return claimDAO.findAll(); }
        catch (SQLException e) { throw new RuntimeException("Failed to load all claims.", e); }
    }

    public int countPending()             { try { return claimDAO.countPending();        } catch (SQLException e) { return 0; } }
    public int countMyPending(int userId) { try { return claimDAO.countPendingByUser(userId); } catch (SQLException e) { return 0; } }
}
