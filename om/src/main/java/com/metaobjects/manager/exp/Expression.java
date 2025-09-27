/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.exp;

import java.util.Date;

public class Expression
{
    public final static int EQUAL           = 0;
    public final static int NOT_EQUAL       = 1;
    public final static int GREATER         = 2;
    public final static int LESSER          = 3;
    public final static int EQUAL_GREATER   = 4;
    public final static int EQUAL_LESSER    = 5;

    public final static int CONTAIN         = 6;
    public final static int NOT_CONTAIN     = 7;
    public final static int START_WITH      = 8;
    public final static int NOT_START_WITH  = 9;
    public final static int END_WITH        = 10;
    public final static int NOT_END_WITH    = 11;
    public final static int EQUALS_IGNORE_CASE = 12;

    ///**
    // * @deprecated Replaced with CONTAIN
    // */
    //public final static int CONTAINS        = CONTAIN;

    private boolean mSpecial = false;
    private String field = null;
    private Object value = null;
    private int condition = EQUAL;

    protected Expression()
    {
        mSpecial = true;
    }

    public Expression( final String field, final Object value )
    {
        this( field, value, EQUAL );
    }

    public Expression( final String field, final Object value, final int condition )
    {
      setField( field );
      setValue( value );
      setCondition( condition );
    }

    public boolean isSpecial()
    {
        return mSpecial;
    }

    public int getCondition()
    {
        return condition;
    }

    public void setCondition( final int condition )
    {
        //if ( condition < EQUAL ) condition = EQUAL;
        //if ( condition > ENDS_WITH ) condition = ENDS_WITH;
        this.condition = condition;
    }

    public String getField()
    {
        return field;
    }

    public void setField( final String field )
    {
        this.field = field;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue( final Object value )
    {
      if ( value == null && condition != EQUAL && condition != NOT_EQUAL )
        throw new IllegalArgumentException( "A null value is only acceptable for EQUAL or NOT_EQUAL conditions" );

      this.value = value;
    }

    public Expression and( final Expression exp )
    {
        return new ExpressionOperator( this, exp, ExpressionOperator.AND );
    }

    public Expression or( final Expression exp )
    {
        return new ExpressionOperator( this, exp, ExpressionOperator.OR );
    }

    public Expression group()
    {
        return new ExpressionGroup( this );
    }

    public final static String condStr( int condition )
    {
        switch( condition )
        {
            case EQUAL:           return "=";
            case NOT_EQUAL:       return "!=";
            case GREATER:         return ">";
            case LESSER:          return "<";
            case EQUAL_GREATER:   return ">=";
            case EQUAL_LESSER:    return "<=";
            case CONTAIN:         return "C";
            case NOT_CONTAIN:     return "!C";
            case START_WITH:      return "S";
            case NOT_START_WITH:  return "!S";
            case END_WITH:        return "E";
            case NOT_END_WITH:    return "!E";
            case EQUALS_IGNORE_CASE:    return "(=)";
        }

        return "?" + condition + "?";
    }

    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	
        sb.append( getField() ).append( " " ).append( condStr( getCondition() )).append( " " );
        if ( getValue() == null ) {
        	sb.append( "NULL" );
        }
        if ( getValue() instanceof String
            || getValue() instanceof Date ) {
        	sb.append( "'" ).append( getValue() ).append( "'" );
        }
        else sb.append( getValue() );

        return sb.toString();
    }
}
