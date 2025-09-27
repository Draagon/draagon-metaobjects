/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.columnar;

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