/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.exp;

public class SortOrder
{
  public final static int NONE = 0;
  public final static int ASC = 1;
  public final static int DESC = 2;

  private int mOrder = ASC;
  private String mField = null;

  private SortOrder mNextSort = null;

  public SortOrder( String field )
  {
    setField( field );
  }

  public SortOrder( String field, int order )
  {
    setOrder( order );
    setField( field );
  }

  public int getOrder()
  {
    return mOrder;
  }

  public void setOrder( int order )
  {
    if ( order < ASC ) order = ASC;
    if ( order > DESC ) order = DESC;
    mOrder = order;
  }

  public String getField()
  {
    return mField;
  }

  public void setField( String field )
  {
    mField = field;
  }

  public void addNextSort( SortOrder sort )
  {
	if ( mNextSort != null ) mNextSort.addNextSort( sort );
	else mNextSort = sort;
  }

  public SortOrder getNext()
  {
    return mNextSort;
  }

  public boolean hasNext()
  {
    if ( mNextSort == null ) return false;
    return true;
  }

  public String toString()
  {
    String c = " ASC";
    if ( getOrder() == DESC ) c = " DESC";
    if ( getNext() == null )
      return getField() + c;
    else
      return getField() + c + ", " + getNext();
  }
}
