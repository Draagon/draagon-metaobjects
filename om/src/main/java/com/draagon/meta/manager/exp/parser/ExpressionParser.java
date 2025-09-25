package com.draagon.meta.manager.exp.parser;

import java.text.DateFormat;

import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.DateField;
import com.draagon.meta.field.IntegerField;
import com.draagon.meta.field.LongField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.manager.exp.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionParser
{
  private static final Logger log = LoggerFactory.getLogger(ExpressionParser.class);
  private static ExpressionParser instance = null;

  protected class ExpResult
  {
    private Expression exp;
    private int end;

    public ExpResult( Expression exp, int end ) {
      this.exp = exp;
      this.end = end;
    }

    public Expression getExp() {
      return exp;
    }

    public int getEnd() {
      return end;
    }
  }

  public static ExpressionParser getInstance() {
    if ( instance == null )
      instance = new ExpressionParser();
    return instance;
  }

  public Expression parse( MetaObject mc, String expressionString ) throws ExpressionParseError
  {
    return parse( mc, expressionString.toLowerCase(), 0, 0 ).getExp();
  }

  protected final static int ACTION_AND = 1;
  protected final static int ACTION_OR  = 2;

  protected ExpResult parse( MetaObject mc, String str, int n, int scope ) throws ExpressionParseError
  {
    Expression a = null;

    int x = n;

    int action = 0;

    while( x < str.length() )
    {
      int c = str.charAt( x );

      if ( c == '(' ) {
        ExpResult r = parse( mc, str, x + 1, scope + 1 );
        Expression b = r.getExp();

        if ( a == null ) a = b;
        else if ( action == ACTION_AND ) a = a.and( b );
        else if ( action == ACTION_OR ) a = a.or( b );
        else throw new ExpressionParseError( "error.parse.unexpected", x, r.getEnd() );
        action = 0;

        x = r.getEnd() + 1;
        n = x;
      }
      else if ( c == ')' ) {
        if ( a == null ) throw new ExpressionParseError( "error.parse.empty", n, x );
        return new ExpResult( a.group(), x );
      }
      else if ( c == ' ' ) {
        x++;
      }
      else if ( c == 'a' && isAnd( str, x )){
        action = ACTION_AND;
        x += 3;
        if ( a == null ) throw new ExpressionParseError( "error.parse.unexpected", x, x+3 );
        n = x;
      }
      else if ( c == 'o' && isOr( str, x )){
        action = ACTION_OR;
        x += 2;
        if ( a == null ) throw new ExpressionParseError( "error.parse.unexpected", x, x+2 );
        n = x;
      }
      else {
        if ( a != null && action == 0 )
          throw new ExpressionParseError( "error.parse.noexp", n, x );

        ExpResult r = parseExpression( mc, str, x );
        Expression b = r.getExp();

        if ( a == null ) a = b;
        else if ( action == ACTION_AND )  a = a.and( b );
        else if ( action == ACTION_OR ) a = a.or( b );
        else throw new ExpressionParseError( "error.parse.unexpected", x, r.getEnd() );
        action = 0;

        //if ( str.charAt( r.getEnd() ) != ')' )
        x = r.getEnd();

        log.debug("END: [{}] {}", str.charAt(x), str.substring(x));

        n = x;
      }
    }

    if ( scope > 0 )
      throw new ExpressionParseError( "error.parse.noclosing", n, x );

    if ( a == null )
      throw new ExpressionParseError( "error.parse.empty", n, x );

    return new ExpResult( a, x );
  }

  protected ExpResult parseExpression( MetaObject mc, String str, int n ) throws ExpressionParseError
  {
    int x = n;

    String type = null;
    String name = null;

    MetaField field = null;

    boolean quoted = false;

    while( x < str.length() )
    {
      char c = str.charAt( x );

      if ( !quoted && c == '(' ) {
        throw new ExpressionParseError( "error.parse.unexpected", n, x );
      }
      else if ( !quoted && ( c == ' ' || c == ')' || c == '!'
        || c == '<' || c == '>' || c == '=' ))
      {
        if ( field == null && name != null )
        {
          if ( !mc.hasMetaField( name ))
            throw new ExpressionParseError( "error.parse.nofield", n, x, name );
          else
            field = mc.getMetaField( name );

          name = null;

          n = x;
        }
        else if ( name != null )
        {
          if ( "NULL".equals( name )) name = null;
          return buildExpresion( field, type, name, n, x );
        }

        if ( c != ' ' && c != ')' )
        {
          if ( type == null ) type = "" + c;
          else type += c;
        }
        x++;
      }
      else if ( c == '\'' ) {
        if ( field == null )
          throw new ExpressionParseError( "error.parse.unexpected", n, x, "'" );

        x++;

        if ( !quoted )
          quoted = true;
        else
          return buildExpresion( field, type, name, n, x );
      }
      else {
        if ( name == null ) name = "" + c;
        else name += c;
        x++;
      }
    }

    if ( field != null && name != null && type != null ) {
      if ( "NULL".equals( name )) name = null;
      return buildExpresion( field, type, name, n, x );
    }

    throw new ExpressionParseError( "error.parse.unknown", n, x );
  }

  protected ExpResult buildExpresion( MetaField mf, String type, String name, int n, int x ) throws ExpressionParseError
  {
    String field = mf.getName();
    Object value = null;

    // The value could be NULL
    if ( name != null ) {
      try {
        switch( mf.getDataType() ) {
        case STRING: value = name; break;
        case DOUBLE: value = Double.valueOf( name ); break;
        case FLOAT: value = Float.valueOf( name ); break;
        case LONG: value = Long.valueOf( name ); break;
        case INT: value = Integer.valueOf( name ); break;
        case SHORT: value = Short.valueOf( name ); break;
        case BYTE: value = Byte.valueOf( name ); break;
        case BOOLEAN: value = Boolean.valueOf( name ); break;
        case DATE:
        	{
        		value = DateFormat.getDateTimeInstance().parse( name ); break;
        	}
        // TODO: Add support for other types
        default: value = name; break;
        }
      }
      catch( Exception e ) {
        throw new ExpressionParseError( "error.parse.badvalue", n, x, name );
      }
    }

    Expression exp = null;

    if ( type == null ) throw new ExpressionParseError( "error.parse.notype", n, x );
    else if ( type.equals( ">" )) exp = new Expression( field, value, Expression.GREATER );
    else if ( type.equals( "<" )) exp = new Expression( field, value, Expression.LESSER );
    else if ( type.equals( "=" )) exp = new Expression( field, value, Expression.EQUAL );
    else if ( type.equals( ">=" )) exp = new Expression( field, value, Expression.EQUAL_GREATER );
    else if ( type.equals( "<=" )) exp = new Expression( field, value, Expression.EQUAL_LESSER );
    else if ( type.equals( "!=" ) || type.equals( "<>" )) exp = new Expression( field, value, Expression.NOT_EQUAL );
    else throw new ExpressionParseError( "error.parse.typeunknown", n, x, type );
    // else if ( type.equals( "!!" )) exp = new Expression( field, value, Expression.CONTAIN );

    return new ExpResult( exp, x );
  }

  protected boolean isAnd( String str, int x ) {
    if (( str.length() - x ) < 4 ) return false;
    if ( str.substring( x, x+3 ).equals( "and" )
        && ( str.charAt( x+3 ) == ' ' ||
            str.charAt( x+3 ) == '(' )) return true;
    return false;
  }

  protected boolean isOr( String str, int x ) {
    if (( str.length() - x ) < 3 ) return false;
    if ( str.substring( x, x+2 ).equals( "or" )
        && ( str.charAt( x+2 ) == ' ' ||
            str.charAt( x+2 ) == '(' )) return true;
    return false;
  }

  public static void main( String [] args ) throws ExpressionParseError
  {
    MetaObject mc = new PojoMetaObject("test");
    MetaField a = new LongField("id");
    MetaField b = new StringField("name");
    MetaField c = new IntegerField("value");
    MetaField d = new DateField("time");
    mc.addMetaField( a );
    mc.addMetaField( b );
    mc.addMetaField( c );
    mc.addMetaField( d );

    Expression exp = ExpressionParser.getInstance().parse( mc, "( time > = '10/12/2006 14:55' and ( ( id = 5 or name = 'test me!' ) and value <> 20))" );
    log.debug("EXPRESSION: {}", exp);
  }
}
