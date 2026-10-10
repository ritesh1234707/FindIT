package com.findit.model;

import java.time.LocalDateTime;

/**
 * Model class representing a student's ownership claim on a found item.
 *
 * Workflow: Student submits → PENDING → Admin reviews → APPROVED / REJECTED
 */
public class Claim {

    private int          claimId;
    private int          itemId;
    private int          claimantId;
    private String       claimantName;         // Joined for display
    private String       itemTitle;            // Joined for display
    private String       verificationDetails;  // Private proof of ownership
    private ClaimStatus  status;
    private Integer      reviewedBy;           // Admin user_id (nullable)
    private String       reviewerName;         // Joined for display
    private String       reviewNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ClaimStatus {
        PENDING, APPROVED, REJECTED, WITHDRAWN;

        public String display() {
            return switch (this) {
                case PENDING   -> "Pending Review";
                case APPROVED  -> "Approved";
                case REJECTED  -> "Rejected";
                case WITHDRAWN -> "Withdrawn";
            };
        }
    }

    // ── Constructors ──────────────────────────────────────────────────────

    public Claim() {}

    public Claim(int itemId, int claimantId, String verificationDetails) {
        this.itemId              = itemId;
        this.claimantId          = claimantId;
        this.verificationDetails = verificationDetails;
        this.status              = ClaimStatus.PENDING;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int          getClaimId()           { return claimId; }
    public void         setClaimId(int id)     { this.claimId = id; }

    public int          getItemId()            { return itemId; }
    public void         setItemId(int id)      { this.itemId = id; }

    public int          getClaimantId()        { return claimantId; }
    public void         setClaimantId(int id)  { this.claimantId = id; }

    public String       getClaimantName()           { return claimantName; }
    public void         setClaimantName(String n)   { this.claimantName = n; }

    public String       getItemTitle()              { return itemTitle; }
    public void         setItemTitle(String t)      { this.itemTitle = t; }

    public String       getVerificationDetails()            { return verificationDetails; }
    public void         setVerificationDetails(String v)    { this.verificationDetails = v; }

    public ClaimStatus  getStatus()              { return status; }
    public void         setStatus(ClaimStatus s) { this.status = s; }

    public Integer      getReviewedBy()           { return reviewedBy; }
    public void         setReviewedBy(Integer id) { this.reviewedBy = id; }

    public String       getReviewerName()           { return reviewerName; }
    public void         setReviewerName(String n)   { this.reviewerName = n; }

    public String       getReviewNotes()             { return reviewNotes; }
    public void         setReviewNotes(String notes) { this.reviewNotes = notes; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime t)  { this.updatedAt = t; }

    // ── Helpers ───────────────────────────────────────────────────────────

    public boolean isPending()  { return status == ClaimStatus.PENDING; }
    public boolean isApproved() { return status == ClaimStatus.APPROVED; }
}
