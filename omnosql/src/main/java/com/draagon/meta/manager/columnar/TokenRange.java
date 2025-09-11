/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.columnar;

/**
 * Represents a token range for partitioned data distribution
 */
public class TokenRange {
    private final long startToken;
    private final long endToken;
    
    public TokenRange(long startToken, long endToken) {
        this.startToken = startToken;
        this.endToken = endToken;
    }
    
    public long getStartToken() {
        return startToken;
    }
    
    public long getEndToken() {
        return endToken;
    }
    
    public boolean contains(long token) {
        if (startToken <= endToken) {
            return token >= startToken && token <= endToken;
        } else {
            // Wrap-around case
            return token >= startToken || token <= endToken;
        }
    }
    
    @Override
    public String toString() {
        return String.format("TokenRange{start=%d, end=%d}", startToken, endToken);
    }
}