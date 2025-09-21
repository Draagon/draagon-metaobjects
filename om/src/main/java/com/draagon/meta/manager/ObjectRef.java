/*
 * Created on Aug 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.draagon.meta.manager;

import java.util.ArrayList;
import java.util.Collection;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.MetaDataUtil;

import com.draagon.meta.object.MetaObject;

/**
 * @author dmealing
 */
public class ObjectRef {

  private final static String OBJECTREF_PREFIX = "objectref://";
  private final static String OBJECTREF_SEP = "/";

  private String mRef = null;
  private MetaObject mMetaClass = null;
  private String [] mIds = null;

  public ObjectRef( String ref ) {

    if ( ref == null || ref.length() == 0 || !ref.startsWith( OBJECTREF_PREFIX ))
      throw new IllegalArgumentException( "Invalid object reference [" + ref + "]" );

    int i = ref.indexOf( OBJECTREF_SEP, OBJECTREF_PREFIX.length() + 1 );
    if ( i < 0 )
      throw new IllegalArgumentException( "Invalid object reference [" + ref + "]" );

    MetaObject mc = MetaDataUtil.findMetaObjectByName( ref.substring( OBJECTREF_PREFIX.length(), i ), ObjectRef.class);

    i += OBJECTREF_SEP.length();
    ArrayList<String> al = new ArrayList<String>();

    while( true )
    {
      int j = ref.indexOf( OBJECTREF_SEP, i );
      if ( j > 0 ) {
        al.add( ref.substring( i, j ));
        i = j + OBJECTREF_SEP.length();
      }
      else {
        if ( ref.substring( i ).trim().length() > 0 )
          al.add( ref.substring( i ).trim() );

        break;
      }
    }

    String [] ids = (String []) al.toArray( new String[ al.size() ]);

    verifyIds( mc, ids );

    mRef = ref;
    mMetaClass = mc;
    mIds = ids;
  }

  public ObjectRef( MetaObject mc, String [] ids ) {
    verifyIds( mc, ids );
    mMetaClass = mc;
    mIds = ids;
  }

  protected void verifyIds( MetaObject mc, String [] ids ) {
    for( int i = 0; i < ids.length; i++ ) {
      if ( ids[ i ] == null || ids[ i ].length() == 0
          || ids[ i ].indexOf( OBJECTREF_PREFIX ) >=0
          || ids[ i ].indexOf( OBJECTREF_SEP ) >= 0 )
        throw new IllegalArgumentException( "Invalid object reference id [" + ids[ i ] + "] at index (" + i + ") for MetaClass [" + mc + "]" );
    }
  }

  public String [] getIds() {
    return mIds;
  }

  public MetaObject getMetaClass() {
    return mMetaClass;
  }

  public String toString() {
    if ( mRef != null ) return mRef;

    StringBuilder b = new StringBuilder();
    b.append( OBJECTREF_PREFIX );
    b.append( getMetaClass().getName() );
    b.append( OBJECTREF_SEP );
    for( int i = 0; i < mIds.length; i++ ) {
      if ( i > 0 ) b.append( OBJECTREF_SEP );
      b.append( mIds[ i ]);
    }

    mRef = b.toString();
    return mRef;
  }

  public int hashCode()
  {
    return toString().hashCode();
  }

  public boolean equals( Object o ) {
    if ( o instanceof String || o instanceof ObjectRef ) {
      if ( o.toString().equals( toString() )) return true;
    }
    return false;
  }
}
