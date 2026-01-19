package com.ticketmaster.ticketing_engine.util;

/**
 * <h1>User Roles (RBAC)</h1>
 * <p>
 * Defines the authority levels available in the system.
 * These enums are used by Spring Security to restrict access to sensitive endpoints.
 * </p>
 * <h2>Hierarchy:</h2>
 * <ul>
 * <li><b>USER:</b> The default role for customers. Can view events and buy tickets.</li>
 * <li><b>ADMIN:</b> The management role. Can create events, delete users, and view system health.</li>
 * </ul>
 */
public enum Role {
    /**
     * Standard customer access.
     * <p>
     * Granted automatically upon registration.
     * </p>
     */
    USER,
    /**
     * Administrator access.
     * <p>
     * Has full control over the Event Inventory and User Management.
     * <b>Security Note:</b> There is no public registration for Admins.
     * This role must be assigned manually in the database or via a secure seed script.
     * </p>
     */
    ADMIN,
}
