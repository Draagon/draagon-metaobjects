package com.draagon.meta.constraint;

/**
 * Policy for placement constraints defining whether a child can be placed under a parent.
 */
public enum PlacementPolicy {
    /**
     * Child can be placed under parent
     */
    ALLOWED,
    
    /**
     * Child cannot be placed under parent
     */
    FORBIDDEN
}
