/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.exp;

public class Range
{
  private int mStart = 0;
  private int mEnd = 0;

  public Range( int start, int end )
  {
    setStart( start );
    setEnd( end );
  }

  public int getStart()
  {
    return mStart;
  }

  public void setStart( int start )
  {
    if ( start < 1 ) start = 1;
    mStart = start;
  }

  public int getEnd()
  {
    return mEnd;
  }

  public void setEnd( int end )
  {
    if ( end < 1 ) end = 1;
    mEnd = end;
  }

  public String toString()
  {
    return "FROM " + getStart() + " TO " + getEnd();
  }
}
