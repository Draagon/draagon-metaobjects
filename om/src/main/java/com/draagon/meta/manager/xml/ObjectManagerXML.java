/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.xml;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.manager.*;
import com.draagon.meta.manager.exp.Expression;
import com.draagon.meta.manager.exp.Range;
import com.draagon.meta.manager.exp.SortOrder;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// import javax.servlet.*;
//import org.xml.sax.InputSource;
//import org.xml.sax.ErrorHandler;
//import org.xml.sax.SAXParseException;
//import org.xml.sax.SAXNotRecognizedException;
//import org.xml.sax.SAXNotSupportedException;

/**
 * The Object Manager Base is able to add, update, delete,
 * and retrieve objects of those types from a datastore.
 */
public class ObjectManagerXML extends ObjectManager
{
  private static final Logger log = LoggerFactory.getLogger(ObjectManagerXML.class);

  private Map<MetaObject,List<Object>> tables = new Hashtable<MetaObject,List<Object>>();
  private String mRoot = null;

  public ObjectManagerXML()
  {
  }

  public void setLocation( String root )
  {
        mRoot = root;
    }

    public String getLocation()
    {
        return mRoot;
    }


    ///////////////////////////////////////////////////////
    // CONNECTION HANDLING METHODS
    //

  /**
   * Retrieves a connection object representing the datastore
   */
  public ObjectConnection getConnection()
    throws MetaDataException
    {
        return new ObjectConnectionXML( tables );
    }

  public void releaseConnection( ObjectConnection oc ) 
  	throws MetaDataException
  	{
	  oc.close();
  	}


    ///////////////////////////////////////////////////////
    // PERSISTENCE METHODS
    //

	/** Is this a createable class */        
	public boolean isCreateableClass( MetaObject mc ) {
            return true;
        }

	/** Is this a readable class */
	public boolean isReadableClass( MetaObject mc ) {
            return true;
        }

	/** Gets the update mapping to the DB */
	public boolean isUpdateableClass( MetaObject mc ) {
            return true;
        }

	/** Gets the delete mapping to the DB */
	public boolean isDeleteableClass( MetaObject mc ) {
            return true;
        }
        
  	//protected MappingHandler getDefaultMappingHandler() {
  	//	return new SimpleMappingHandlerXML();
  	//}
  
  /**
   * Retrieves the fields of a MetaClass which are persistable
   */
  @SuppressWarnings("unchecked")
  public Collection<MetaField> getWriteableFields( MetaObject mc )
  {
      final String KEY = "getWriteableFields()";

      ArrayList<MetaField> fields = (ArrayList<MetaField>) mc.getCacheValue( KEY );

      if ( fields == null )
      {
	        fields = new ArrayList<MetaField>();

	        for( Iterator i = mc.getMetaFields().iterator(); i.hasNext(); )
	        {
	            MetaField f = (MetaField) i.next();
	            if ( isWriteableField( f )) fields.add( f );
	        }

	        mc.setCacheValue( KEY, fields );
      }

  /*if ( fields.size() == 0 )
    throw new MetaException( "No persistable fields for MetaClass [" + mc + "]" );*/

      return fields;
  }
  

  /**
   * Retrieves the fields of a MetaClass which are persistable
   */
  @SuppressWarnings("unchecked")
	public Collection<MetaField> getReadableFields( MetaObject mc )
  {
      final String KEY = "getReadableFields()";

      ArrayList<MetaField> fields = (ArrayList<MetaField>) mc.getCacheValue( KEY );

      if ( fields == null )
      {
	        fields = new ArrayList<MetaField>();

	        for( Iterator i = mc.getMetaFields().iterator(); i.hasNext(); )
	        {
	            MetaField f = (MetaField) i.next();
	            if ( isReadableField( f )) fields.add( f );
	        }

	        mc.setCacheValue( KEY, fields );
      }

      /*if ( fields.size() == 0 )
          throw new MetaException( "No persistable fields for MetaClass [" + mc + "]" );*/

      return fields;
  }

    /**
     * Returns whether the metafield is persistable or not
     */
    public boolean isReadableField( MetaField mf )
    {
      return true;
    }

    /**
     * Returns whether the metafield is persistable or not
     */
    public boolean isWriteableField( MetaField mf )
    {
      if ( isReadOnly( mf )) return false;
      return true;
    }

    protected String getNextFieldId( ObjectConnection c, MetaObject mc, MetaField f ) throws MetaDataException
    {
        long id = 0;
        List<Object> list = getObjectsFromTable( c, mc );

        for( Object o : list )
        {
            Long l = f.getLong( o );
            if ( l == null ) l = Long.valueOf( 1 );

            if ( l.longValue() > id ) id = l.longValue();
        }

        return "" + ( id + 1 );
    }

  public void handleAutoField( ObjectConnection c, MetaObject mc, MetaField mf, Object obj, Object auto, int state, int action )
    throws MetaDataException
  {
    if ( "id".equals( auto ) && action == CREATE && state == AUTO_PRIOR ) {
      mf.setString( obj, getNextFieldId( c, mc, mf ));
    }

    super.handleAutoField( c, mc, mf, obj, auto, state, action );
  }


  /**
   * Gets the object by the id; throws exception if it did not exist
   */
  public Object getObjectByRef( ObjectConnection c, String refStr )
    throws MetaDataException
    {
        ObjectRef ref = getObjectRef( refStr );
        MetaObject mc = ref.getMetaClass();

        for( Object o : getObjectsFromTable( c, mc )) {
            if ( getObjectRef( o ).equals( ref )) return o;
        }

        throw new ObjectNotFoundException( "Object of class [" + mc + "] with reference [" + ref + "] was not found" );
    }

  /**
   * Get all objects of the specified kind from the datastore
   */
  public Collection<Object> getObjects( ObjectConnection c, MetaObject mc, QueryOptions options )
    throws MetaDataException
    {
        Collection<Object> objs = getObjectsFromTable( c, mc );

        // Get the fields to copy
        Collection<MetaField> fields = options.getFields();
        if ( fields == null || fields.size() == 0 ) {
          //if ( options.getWriteableOnly() )
          //  fields = getWriteableFields( mc );
          //else
            fields = getReadableFields( mc );
        }

        // Copy all the objects and only the specified fields
        Collection<Object> results = new ArrayList<Object>();
        for( Object o1 : objs )
        {
          Object o2 = mc.newInstance();

          for( MetaField f : fields ) {
            f.setObject( o2, f.getObject( o1 ));
          }

          results.add( o2 );
        }


        // Handle Expressions, Sorting, Ranging, and Options
        Expression exp = options.getExpression();
        SortOrder sort = options.getSortOrder();
        Range range = options.getRange();

        if ( exp != null )
            results = filterObjects( results, exp );

        if ( sort != null )
            results = sortObjects( results, sort );

        if ( range != null )
            results = clipObjects( results, range );

        if( options.isDistinct() )
            results = distinctObjects( results );

        return results;
    }

  /**
   * Load the specified object from the XML source
   */
  public void loadObject( ObjectConnection c, Object obj )
    throws MetaDataException
    {
        String ref = getObjectRef( obj ).toString();

        Object o = getObjectByRef( c, ref );

        for( MetaField mf : getReadableFields( MetaDataRegistry.findMetaObject( obj )))
        {
          if ( !isPrimaryKey( mf ))
            mf.setObject( obj, mf.getObject( o ));
        }
    }

  /**
   * Add the specified object to the datastore
   */
  public void createObject( ObjectConnection c, Object obj )
    throws MetaDataException
    {
        MetaObject mc = MetaDataRegistry.findMetaObject( obj );
        List<Object> list = getObjectsFromTable( c, mc );

        if ( !isCreateableClass( mc ))
          throw new MetaDataException( "Object of class [" + mc + "] is not persistable" );

        prePersistence( c, mc, obj, CREATE );

        list.add( obj );

        postPersistence( c, mc, obj, CREATE );
    }

  /**
   * Update the specified object in the datastore
   */
  public void updateObject( ObjectConnection c, Object obj )
    throws MetaDataException
  {
    MetaObject mc = MetaDataRegistry.findMetaObject( obj );
    List<Object> list = getObjectsFromTable( c, mc );

    int i = list.indexOf( obj );
    if ( i < 0 )
      throw new MetaDataException( "Object [" + obj + "] did not exist in table for class [" + mc + "]" );

    if ( !isUpdateableClass( mc ))
      throw new MetaDataException( "Object of class [" + mc + "] is not persistable" );

    prePersistence( c, mc, obj, UPDATE );

    list.set( i, obj );

    postPersistence( c, mc, obj, UPDATE );
  }

  /**
   * Delete the specified object from the datastore
   */
  public void deleteObject( ObjectConnection c, Object obj )
    throws MetaDataException
  {
    MetaObject mc = MetaDataRegistry.findMetaObject( obj );
    List<Object> list = getObjectsFromTable( c, mc );

    int i = list.indexOf( obj );
    if ( i < 0 )
      throw new MetaDataException( "Object [" + obj + "] did not exist in table for class [" + mc + "]" );

    if ( !isDeleteableClass( mc ))
      throw new MetaDataException( "Object of class [" + mc + "] is not persistable" );

    prePersistence( c, mc, obj, DELETE );

    list.remove( i );

    postPersistence( c, mc, obj, DELETE );
  }


  /**
   * Stores the specified object by adding, updating, or deleting
   */
  public void storeObject( ObjectConnection c, Object obj )
    throws MetaDataException
    {
        StateAwareMetaObject pmc = getStatefulMetaClass( obj );

        if ( pmc.isNew( obj )) createObject( c, obj );
        else if ( pmc.isDeleted( obj )) deleteObject( c, obj );
        else if ( pmc.isModified( obj )) updateObject( c, obj );
    }


    ///////////////////////////////////////////////////////
    // PRIVATE METHODS
    //

    @SuppressWarnings("unchecked")
	private synchronized List<Object> getObjectsFromTable( ObjectConnection c, MetaObject mc )
        throws MetaDataException
    {
        Map<MetaObject,List<Object>> map = (Map<MetaObject,List<Object>>) c.getDatastoreConnection();

        ArrayList<Object> tmp = (ArrayList<Object>) map.get( mc );
        if ( tmp == null )
        {
            if ( isReadableClass( mc ))
                return loadObjectsFromFile( c, mc );

            tmp = new ArrayList<Object>();
            map.put( mc, tmp );
        }

        return tmp;
    }

    protected String getFileRef( MetaObject mc )
    {
        try {
            return (String) mc.getMetaAttr( "fileRef" ).getValueAsString();
        }
        catch( MetaDataNotFoundException e ) { }

        return mc.getShortName() + ".xml";
    }

    protected String getNameRef( MetaObject mc )
    {
        try {
            return (String) mc.getMetaAttr( "nameRef" ).getValue();
        }
        catch( MetaDataNotFoundException e ) { }

        return mc.getShortName();
    }

    protected String getFieldRef( MetaField mf )
    {
        try {
            return (String) mf.getMetaAttr( "nameRef" ).getValue();
        }
        catch( MetaDataNotFoundException e ) { }

        return mf.getName();
    }

    private InputStream getSourceStream( MetaObject mc )
        throws MetaDataException
    {
        String file = "";
        if ( getLocation() != null ) file += getLocation();
        file = file + getFileRef( mc );

        InputStream is = getClass().getClassLoader().getResourceAsStream( file );
        if ( is == null )
        {
            log.error( "Meta XML file [" + file + "] does not exist" );
            throw new MetaDataException( "The Meta XML item file [" + file + "] was not found" );
        }

        return is;
    }

    @SuppressWarnings("unchecked")
	private List<Object> loadObjectsFromFile( ObjectConnection c, MetaObject mc )
        throws MetaDataException
    {
        ///////////////////////////////////////////////////////////
        // Load the XML document
        Document doc = null;

        Map<MetaObject,List<Object>> map = (Map<MetaObject,List<Object>>) c.getDatastoreConnection();

        InputStream is = null;
        try {
          is = getSourceStream( mc );
          doc = XMLUtil.loadFromStream( is );
        }
        catch( IOException e )
        {
          log.error( "Meta XML file for MetaClass [" + mc + "] cannot be parsed: " + e.getMessage() );
          throw new MetaDataException( "The Meta XML file for MetaClass [" + mc + "] was not parsable", e );
        }
        finally {
          try { if ( is != null ) is.close(); }
          catch( IOException e ) {}
        }

        /////////////////////////////////////////////////////
        // Parse the XML

        try
        {
            // Look for the <items> element
            NodeList itemdocList = doc.getElementsByTagName( "objects" );
            if ( itemdocList == null || itemdocList.getLength() == 0 )
                throw new MetaDataException( "The root 'objects' element was not found" );

            Element itemdocElement = (Element) itemdocList.item( 0 );

            Collection objects = parseObjects( c, mc, itemdocElement );

            List<Object> tmp = new ArrayList<Object>();

            // Place the resulting objects into the map
            for( Iterator i = objects.iterator(); i.hasNext(); )
                tmp.add( i.next() );

            // Add the objects to the map
            map.put( mc, tmp );

            return tmp;
        }
        catch( SAXException e )
        {
            throw new MetaDataException( "Unable to load Objects for MetaClass [" + mc + "]", e );
        }
    }

    private List<Object> parseObjects( ObjectConnection c, MetaObject mc, Element element )
        throws MetaDataException, SAXException
    {
        String nameRef = getNameRef( mc );

        NodeList objectList = element.getElementsByTagName( nameRef );
        List<Object> objects = new ArrayList<Object>();

        for( int i = 0; i < objectList.getLength(); i++ )
        {
            Element e = (Element) objectList.item( i );

            Object o = getNewObject( mc );

            for( MetaField mf : getReadableFields( mc ))
            {
                String fieldRef = getFieldRef( mf );

                NodeList list = e.getElementsByTagName( fieldRef );

                if ( list.getLength() > 0 )
                {
                    Element fe = (Element) list.item( 0 );
                    String value = "";
                    Node nv = fe.getFirstChild();
                    if ( nv != null ) value = nv.getNodeValue();

                    mf.setString( o, value );
                }
                else
                    mf.setString( o, null );
            }

            if ( mc instanceof StateAwareMetaObject )
            {
                // It was pulled from the database, so it doesn't need to be flagged as modified
                ((StateAwareMetaObject) mc ).setModified( o, false );

                // It is also no longer a new item
                ((StateAwareMetaObject) mc ).setNew( o, false );
            }

            // Add the object to the returned vector
            objects.add( o );
        }

        return objects;
    }


    ///////////////////////////////////////////////////////
    // OBJECT QUERY LANGUAGE METHODS
    //

    public int execute( ObjectConnection c, String query, Collection<?> arguments ) throws MetaDataException
    {
    	throw new UnsupportedOperationException( "execute is not support by the XML manager" );
    }

    public Collection<?> executeQuery( ObjectConnection c, String query, Collection<?> arguments ) throws MetaDataException
    {
    	throw new UnsupportedOperationException( "executeQuery is not support by the XML manager" );
    }


    ///////////////////////////////////////////////////////
    // TO STRING METHOD
    //

    public String toString()
    {
        return "MetaManXML";
    }
}
