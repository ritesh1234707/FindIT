package com.findit.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing a Lost or Found item report.
 *
 * OOP Concepts:
 * - Encapsulation with private fields
 * - Nested enums for type-safe status and type values
 * - Overloaded constructors for creation vs retrieval scenarios
 */
public class Item {

    private int          itemId;
    private int          userId;
    private String       reporterName;   // Joined from users table for display
    private String       title;
    private String       category;
    private String       color;
    private String       brand;
    private String       description;
    private String       location;
    private LocalDate    eventDate;      // When the item was lost or found
    private ReportType   reportType;     // LOST or FOUND
    private ItemStatus   status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Nested Enums ─────────────────────────────────────────────────────

    public enum ReportType {
        LOST, FOUND;

        public String display() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    public enum ItemStatus {
        OPEN, CLAIM_PENDING, CLAIMED, RETURNED, CLOSED;

        public String display() {
            return switch (this) {
                case OPEN          -> "Open";
                case CLAIM_PENDING -> "Claim Pending";
                case CLAIMED       -> "Claimed";
                case RETURNED      -> "Returned";
                case CLOSED        -> "Closed";
            };
        }
    }

    // ── Constructors ──────────────────────────────────────────────────────

    public Item() {}

    /** For creating a new report */
    public Item(int userId, String title, String category, String color, String brand,
                String description, String location, LocalDate eventDate, ReportType reportType) {
        this.userId      = userId;
        this.title       = title;
        this.category    = category;
        this.color       = color;
        this.brand       = brand;
        this.description = description;
        this.location    = location;
        this.eventDate   = eventDate;
        this.reportType  = reportType;
        this.status      = ItemStatus.OPEN;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int          getItemId()            { return itemId; }
    public void         setItemId(int id)      { this.itemId = id; }

    public int          getUserId()            { return userId; }
    public void         setUserId(int id)      { this.userId = id; }

    public String       getReporterName()           { return reporterName; }
    public void         setReporterName(String n)   { this.reporterName = n; }

    public String       getTitle()             { return title; }
    public void         setTitle(String t)     { this.title = t; }

    public String       getCategory()          { return category; }
    public void         setCategory(String c)  { this.category = c; }

    public String       getColor()             { return color; }
    public void         setColor(String c)     { this.color = c; }

    public String       getBrand()             { return brand; }
    public void         setBrand(String b)     { this.brand = b; }

    public String       getDescription()       { return description; }
    public void         setDescription(String d) { this.description = d; }

    public String       getLocation()          { return location; }
    public void         setLocation(String l)  { this.location = l; }

    public LocalDate    getEventDate()          { return eventDate; }
    public void         setEventDate(LocalDate d) { this.eventDate = d; }

    public ReportType   getReportType()            { return reportType; }
    public void         setReportType(ReportType t){ this.reportType = t; }

    public ItemStatus   getStatus()             { return status; }
    public void         setStatus(ItemStatus s) { this.status = s; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime t)  { this.updatedAt = t; }

    // ── Convenience helpers ───────────────────────────────────────────────

    public boolean isLost()   { return reportType == ReportType.LOST; }
    public boolean isFound()  { return reportType == ReportType.FOUND; }
    public boolean isOpen()   { return status == ItemStatus.OPEN; }

    @Override
    public String toString() {
        return "Item{id=" + itemId + ", title='" + title + "', type=" + reportType + ", status=" + status + "}";
    }
}
