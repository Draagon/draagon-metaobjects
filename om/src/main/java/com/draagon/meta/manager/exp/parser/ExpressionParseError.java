package com.draagon.meta.manager.exp.parser;

import com.draagon.meta.MetaException;

@SuppressWarnings("serial")
public class ExpressionParseError extends MetaException
{
  private String msgKey;
  private int start;
  private int end;
  private String arg1;

  /**
   * Specifies the error message key and the start and end line
   */
  public ExpressionParseError( String msgKey,int start, int end )
  {
    this( msgKey, start, end, null );
  }

  /**
   * Specifies the error message key, start, end, and an argument
   */
  public ExpressionParseError( String msgKey, int start, int end, String arg1 )
  {
    this.msgKey = msgKey;
    this.start = start;
    this.end = end;
    this.arg1 = arg1;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public String getMessageKey() {
    return msgKey;
  }

  public String getArg1() {
    return arg1;
  }

  public String getMessage() {
    return toString();
  }

  public String toString()
  {
    StringBuffer b = new StringBuffer( msgKey );
    if ( arg1 != null ) {
      b.append( '(' );
      b.append( arg1 );
      b.append( ')' );
    }
    b.append( " from line " );
    b.append( start );
    b.append( " to " );
    b.append( end );

    return b.toString();
  }
}
