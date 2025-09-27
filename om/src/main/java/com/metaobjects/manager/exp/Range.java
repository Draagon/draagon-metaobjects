/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.exp;

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
