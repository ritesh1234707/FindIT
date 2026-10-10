package com.findit.service;

import com.findit.dao.ItemDAO;
import com.findit.dao.UserDAO;
import com.findit.model.Item;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gathers all statistics for both dashboards.
 * All numbers come from live database queries.
 */
public class DashboardService {

    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();

    public Map<String, Integer> getAdminStats() {
        Map<String, Integer> stats = new HashMap<>();
        try {
            int total    = itemDAO.countTotal();
            int returned = itemDAO.countByStatus(Item.ItemStatus.RETURNED);
            int rate     = total > 0 ? (returned * 100 / total) : 0;

            stats.put("totalUsers",   userDAO.countAll());
            stats.put("totalLost",    itemDAO.countOpenByType(Item.ReportType.LOST));
            stats.put("totalFound",   itemDAO.countOpenByType(Item.ReportType.FOUND));
            stats.put("returned",     returned);
            stats.put("totalItems",   total);
            stats.put("recoveryRate", rate);
        } catch (SQLException e) {
            stats.put("totalUsers", 0); stats.put("totalLost", 0);
            stats.put("totalFound", 0); stats.put("returned",  0);
            stats.put("totalItems", 0); stats.put("recoveryRate", 0);
        }
        return stats;
    }
}
