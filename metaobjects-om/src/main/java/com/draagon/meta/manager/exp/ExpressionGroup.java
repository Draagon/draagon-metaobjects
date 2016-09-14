/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.exp;

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
