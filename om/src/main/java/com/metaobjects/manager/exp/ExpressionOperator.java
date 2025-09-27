/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.exp;

public class ExpressionOperator extends Expression
{
  public final static int AND = 0;
  public final static int OR  = 1;

  private Expression mExpA = null;
  private Expression mExpB = null;
  private int mOper = AND;

  ExpressionOperator( Expression expA, Expression expB, int oper )
  {
    mExpA = expA;
    mExpB = expB;
    mOper = oper;
  }

  public Expression getExpressionA()
  {
    return mExpA;
  }

  public Expression getExpressionB()
  {
    return mExpB;
  }

  public int getOperator()
  {
    return mOper;
  }

  public String toString()
  {
    String c = " AND ";
    if ( getOperator() == OR ) c = " OR ";
    return getExpressionA() + c + getExpressionB();
  }
}
