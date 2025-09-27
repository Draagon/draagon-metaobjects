/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager;

import java.util.Collection;

import com.metaobjects.field.MetaField;
import com.metaobjects.manager.exp.Expression;
import com.metaobjects.manager.exp.Range;
import com.metaobjects.manager.exp.SortOrder;

/**
 * Provides a mechanism to pass options into object requests
 *  for the ObjectManagers.
 */
public class QueryOptions
{
  private boolean mDistinct      = false;
  private Expression mExp        = null;
  private SortOrder mOrder       = null;
  private Range mRange           = null;
  //private boolean mWriteableOnly = false;
  private Collection<MetaField> mFields      = null;
  private boolean mWithLock		 = false;

  public QueryOptions()
  {
  }

  public QueryOptions( Expression exp )
  {
    this( exp, null );
  }

  public QueryOptions( Expression exp, SortOrder order )
  {
    this( exp, order, null );
    //mExp = exp;
    //mOrder = order;
  }

  //public QueryOptions( SortOrder order )
  //{
  //  mOrder = order;
  //}

  public QueryOptions( Expression exp, SortOrder order, Range range )
  {
    setExpression( exp );
    setSortOrder( order );
    setRange( range );
  }

  //public QueryOptions( SortOrder order, Range range )
  //{
  //  mOrder = order;
  //  mRange = range;
  //}

  //public QueryOptions( Range range )
  //{
  //  mRange = range;
  //}

  //public QueryOptions( Expression exp, Range range )
  //{
  //  mExp = exp;
  //  mRange = range;
  //}

  public void setExpression( Expression exp )
  {
    mExp = exp;
  }

  public Expression getExpression()
  {
    return mExp;
  }

  public void setSortOrder( SortOrder order )
  {
    mOrder = order;
  }

  public SortOrder getSortOrder()
  {
    return mOrder;
  }

  public void setRange( int start, int end )
  {
    mRange = new Range( start, end );
  }

  public void setRange( Range range )
  {
    mRange = range;
  }

  public Range getRange()
  {
    return mRange;
  }

  public void setDistinct( boolean distinct )
  {
    mDistinct = distinct;
  }

  public boolean isDistinct()
  {
    return mDistinct;
  }

  public void setFields( Collection<MetaField> fields )
  {
    mFields = fields;
  }

  public Collection<MetaField> getFields()
  {
    return mFields;
  }

  //public void setWriteableOnly( boolean writeableOnly ) {
  //  mWriteableOnly = writeableOnly;
  //}

  //public boolean getWriteableOnly() {
  //  return mWriteableOnly;
  //}

  public String toString()
  {
    return "Options{ EXP: " + getExpression() + "; ORDER: " + getSortOrder() + "; RANGE: " + getRange() + " }";
  }

  /** If the records read should be locked from updates for this transaction */
  public boolean withLock() {
	return mWithLock;
  }
	
  /** Whether to lock the records being read from updates for this transaction */
  public void setWithLock(boolean withLock) {
	this.mWithLock = withLock;
  }
}
