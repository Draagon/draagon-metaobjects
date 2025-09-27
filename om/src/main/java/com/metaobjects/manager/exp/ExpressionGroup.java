/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.exp;

public class ExpressionGroup extends Expression
{
  private Expression mGroup = null;

  ExpressionGroup( Expression group )
  {
    mGroup = group;
  }

  public Expression getGroup()
  {
    return mGroup;
  }

  public String toString()
  {
    return "( " + getGroup() + " )";
  }
}
