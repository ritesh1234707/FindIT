package com.findit.service;

import com.findit.dao.ItemDAO;
import com.findit.model.Item;
import com.findit.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Business logic for creating and retrieving item reports. */
public class ItemService {

    private final ItemDAO         itemDAO         = new ItemDAO();
    private final MatchingService matchingService = new MatchingService();

    /**
     * Creates a new LOST or FOUND report and triggers match generation.
     */
    public Item reportItem(int userId, String title, String category, String color, String brand,
                            String description, String location, LocalDate eventDate,
                            Item.ReportType type) {

        String err = ValidationUtil.validateItemReport(title, category, location,
                         eventDate != null ? eventDate.toString() : null);
        if (err != null) throw new IllegalArgumentException(err);

        try {
            Item item = new Item(userId, title.trim(), category, color, brand,
                                 description, location.trim(), eventDate, type);
            int id = itemDAO.save(item);
            item.setItemId(id);

            // Run smart matching in background so the UI stays fast
            new Thread(() -> matchingService.generateMatchesForItem(item)).start();

            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save report: " + e.getMessage(), e);
        }
    }

    public List<Item> getAll(String reportType, String search, String category, String status) {
        try { return itemDAO.findAll(reportType, search, category, status); }
        catch (SQLException e) { throw new RuntimeException("Failed to load items.", e); }
    }

    public List<Item> getMyReports(int userId) {
        try { return itemDAO.findByUserId(userId); }
        catch (SQLException e) { throw new RuntimeException("Failed to load your reports.", e); }
    }

    public Optional<Item> getById(int itemId) {
        try { return itemDAO.findById(itemId); }
        catch (SQLException e) { throw new RuntimeException("Failed to find item.", e); }
    }

    public List<Item> getRecentOpen(int limit) {
        try { return itemDAO.findRecentOpen(limit); }
        catch (SQLException e) { return List.of(); }
    }

    public void updateStatus(int itemId, Item.ItemStatus status) {
        try { itemDAO.updateStatus(itemId, status); }
        catch (SQLException e) { throw new RuntimeException("Failed to update status.", e); }
    }

    // ── Stat helpers ──────────────────────────────────────────────────────

    public int countOpenLost()          { try { return itemDAO.countOpenByType(Item.ReportType.LOST);  } catch (SQLException e) { return 0; } }
    public int countOpenFound()         { try { return itemDAO.countOpenByType(Item.ReportType.FOUND); } catch (SQLException e) { return 0; } }
    public int countReturned()          { try { return itemDAO.countByStatus(Item.ItemStatus.RETURNED); } catch (SQLException e) { return 0; } }
    public int countMyActive(int uid)   { try { return itemDAO.countActiveByUser(uid); }               catch (SQLException e) { return 0; } }
    public int countTotal()             { try { return itemDAO.countTotal(); }                          catch (SQLException e) { return 0; } }
}
