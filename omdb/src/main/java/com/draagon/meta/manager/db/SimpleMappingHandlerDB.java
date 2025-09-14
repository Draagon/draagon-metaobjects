package com.draagon.meta.manager.db;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.draagon.meta.DataTypes;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.manager.ObjectManager;
import com.draagon.meta.manager.db.defs.BaseTableDef;
import com.draagon.meta.manager.db.defs.ColumnDef;
import com.draagon.meta.manager.db.defs.IndexDef;
import com.draagon.meta.manager.db.defs.InheritenceDef;
import com.draagon.meta.manager.db.defs.NameDef;
import com.draagon.meta.manager.db.defs.SequenceDef;
import com.draagon.meta.manager.db.defs.TableDef;
import com.draagon.meta.manager.db.defs.ViewDef;

public class SimpleMappingHandlerDB implements MappingHandler {

	public final static String TRUE  = "true";
	public final static String FALSE = "false";
	
	public final static String ATTR_TABLE_DEF = "dbTableDef";
	public final static String ATTR_VIEW_DEF  = "dbViewDef";
	public final static String ATTR_COL_DEF   = "dbColDef";

	public final static String AUTO_ID  = "id";
	public final static String AUTO_LAST_ID  = "last";

	public final static String IS_INDEX     = "isIndex";
	public final static String IS_UNIQUE 	  = "isUnique";
	public final static String IS_VIEWONLY  = "isViewOnly";

	public final static String FOREIGN_KEY_REF     = "dbForeignKey";
	public final static String VIEW_REF     = "dbView";
	public final static String VIEW_SQL_REF = "dbViewSQL";
	public final static String TABLE_REF    = "dbTable";
	public final static String COL_REF      = "dbColumn";
	public final static String SEQ_REF      = "dbSequence";
	public final static String SEQ_START_REF   = "dbSeqStart";
	public final static String INHERITANCE_REF = "dbInheritance";
	
	@Override
	public ObjectMapping getCreateMapping( MetaObject mc ) {
		
		String name = getTableRef( mc );
		if ( name != null ) {
			return getTableMapping( mc );
		}
		return null;
	}

	@Override
	public ObjectMapping getReadMapping(MetaObject mc) {
		
		// Try to get a view first 
		String name = getViewRef( mc );
		if ( name != null ) {
			return getViewMapping( mc );
		}
		
		// If no view, then get a table
		name = getTableRef( mc );
		if ( name != null ) {
			return getTableMapping( mc );
		}	
		return null;
	}

	@Override
	public ObjectMapping getDeleteMapping(MetaObject mc) {
		return getCreateMapping(mc);
	}

	@Override
	public ObjectMapping getUpdateMapping(MetaObject mc) {
		return getCreateMapping(mc);
	}

	/** Get the table mapping */
	protected ObjectMappingDB getTableMapping( MetaObject mc ) {

		// Create the table definition
		TableDef t = new TableDef( NameDef.parseName( getTableRef( mc )));
		
		// Get all the possible metafields for this metaclass
		Collection<MetaField> fields = mc.getMetaFields();
		
		// Create the mapping
		ObjectMappingDB mapping = new ObjectMappingDB( t );
		
		// See if there is a referenced table
		InheritanceRef iref = getInheritanceDefinition( mc );
		if ( iref != null ) {
			
			// Load the table mapping for the super class
			ObjectMappingDB superMap = getTableMapping( iref.getSuperClass() );
			
			// Sets the parent mapping
			mapping.setSuperMapping( superMap );

			// NOTE:  Kind of shady to grab this here and later grab again, prone to bugs on implementation changes...
			String coln = getColumnRef( iref.getJoinerField() ); 
			
			// Create the inheritence definition
			InheritenceDef inheritence = new InheritenceDef( 
					coln,
					(TableDef) superMap.getDBDef(),
					(ColumnDef) superMap.getArgDef( iref.getSuperJoinerField() ),
					getColumnRef( iref.getDiscriminatorField() ),
					iref.getDiscriminatorValue() );
			
			// Add it to the current table
			t.setInheritence( inheritence );
			
			// Remove the fields found in the superclass
			fields = new ArrayList<MetaField>();
			for( MetaField mf2 : mc.getMetaFields() ) {
				boolean found = false;
				for( MetaField mf : iref.getSuperClass().getMetaFields() ) {
					if ( mf.equals( mf2 )) found = true;
				}
				if ( !found ) fields.add( mf2 );
			}
		}
		
		// Load columns
		loadColumns( fields, t, mapping );
		
		// Return the table mapping
		return mapping;
	}

	/** Get the table mapping */
	protected ObjectMapping getViewMapping( MetaObject mc ) {

		// Create the view definition
		ViewDef v = new ViewDef( NameDef.parseName( getViewRef( mc )));
		
		// Add the SQL if it exists
		String sql = getViewSQL( mc );
		if ( sql != null ) {
			v.setSQL( sql );
		}
		
		// Create the mapping
		ObjectMappingDB mapping = new ObjectMappingDB( v );
		
		// Load columns
		loadColumns( mc.getMetaFields(), v, mapping );
		
		return mapping;
	}
	
	/** Load the columns for the mapping */
	protected void loadColumns( Collection<MetaField> fields, BaseTableDef table, ObjectMappingDB mapping ) {

		// Iterate through the fields and load the columns
		for( MetaField mf : fields ) {
			
			// Get the column DB name 
			String col = getColumnRef( mf );
			if ( col == null ) continue;
			
			// Make sure it's not for view's only
			if ( table instanceof TableDef ) {
				if ( TRUE.equals( getPersistenceAttribute( mf, IS_VIEWONLY ))) continue;
			}
			
			// Create the column definition
			ColumnDef colDef = new ColumnDef( col, getSQLType( mf ));
			
			// Set the length of the varchar field
			// TODO:  Length should be an attribute
			colDef.setLength( getSQLLength( mf ));
			
			// Is it a primary key?
			String key = getPersistenceAttribute( mf , ObjectManager.IS_KEY );
			if ( key != null && key.equalsIgnoreCase( TRUE )) {
				colDef.setPrimaryKey( true );
			}

			// Load extra values if this is a Table Definition
			if ( table instanceof TableDef ) {
				
				// Is it an auto column?
				String auto = getAutoGenerated( mf );
				if ( auto != null ) {
					if ( AUTO_ID.equals( auto )) {
						colDef.setAutoType( ColumnDef.AUTO_ID );
					}
					if ( AUTO_LAST_ID.equals( auto )) {
						colDef.setAutoType( ColumnDef.AUTO_LAST_ID );
					}
					else if ( ObjectManager.AUTO_CREATE.equals( auto )) {
						colDef.setAutoType( ColumnDef.AUTO_DATE_CREATE );
					}
					else if ( ObjectManager.AUTO_UPDATE.equals( auto )) {
						colDef.setAutoType( ColumnDef.AUTO_DATE_UPDATE );
					}
				}
				
				// Get the sequence if it is defined
				String seq = getSequenceRef( mf );
				if ( seq != null ) {
					int start = getSequenceStart( mf );
					SequenceDef seqDef = new SequenceDef( NameDef.parseName( seq ), start, 1 );
					colDef.setSequence( seqDef );
				}
				
				// Set if it is unique
				colDef.setUnique( isUnique( mf ));
			
				// Check if the column is an index
				if ( isIndex( mf )) {
					
					String name = table.getNameDef().getName() + "_" + col + "_index"; 
						
					IndexDef index = new IndexDef( name, col );
					
					((TableDef) table ).addIndex( index );
				}
			}
			
			// Add the column to the table
			table.addColumn( colDef );
			
			// Add the mapping entry for the column and MetaField
			mapping.addMap( colDef, mf );
		}		
	}
	
	protected int getSQLType( MetaField mf ) {
		switch( mf.getDataType() )
		{
		case BOOLEAN: return Types.BIT;
		case BYTE: return Types.TINYINT;
		case SHORT: return Types.SMALLINT;
		case INT: return Types.INTEGER;
		case DATE:  return Types.TIMESTAMP;
		case LONG: return Types.BIGINT;
		case FLOAT: return Types.FLOAT;
		case DOUBLE: return Types.DOUBLE;
		case STRING: return Types.VARCHAR;
		case OBJECT: return Types.BLOB;
		default: throw new IllegalArgumentException( "Unable to get SQL type for MetaField [" + mf + "] with type (" + mf.getDataType() + ")" );
		}
	}

	protected int getSQLLength( MetaField mf ) {
		// TODO:  Support length on metafield as validator or attribute
		switch( mf.getDataType() )
		{
			case BOOLEAN: return 1;
			case BYTE: return 2;
			case SHORT: return 4;
			case INT:
			case DATE:
			case LONG:
			case FLOAT:
			case DOUBLE: return 8;
			case STRING: return 50;
			case OBJECT: return 100;
			default: throw new IllegalArgumentException( "Unable to get SQL type for MetaField [" + mf + "] with type (" + mf.getDataType() + ")" );
		}
	}

	/**
	 * Returns the inheritance definition for a given MetaClass or returns null if none exists 
	 * @param mc The MetaClass to retrieve the inheritance definition for
	 * @return The inheritance definition or null
	 */
	public InheritanceRef getInheritanceDefinition( MetaObject mc ) {

		InheritanceRef def = (InheritanceRef) mc.getCacheValue( INHERITANCE_REF );
		if ( def == null ) {
			if ( !mc.hasMetaAttr( INHERITANCE_REF )) return null;

			Properties props = (Properties) mc.getMetaAttr( INHERITANCE_REF ).getValue();
			if ( props == null ) return null;

			def = new InheritanceRef( mc, props );
		}

		return def;
	}

	/**
	 * Returns whether the metafield is an auto id and is set prior to creation or update
	 */
	protected String getAutoGenerated( MetaField mf ) {
		return getPersistenceAttribute( mf, ObjectManager.AUTO );
	}


	/**
	 * Retrieves the fields of a MetaClass which are persistable
	 */
	/*public Collection<MetaField> getTableFields( MetaClass mc )
    {
        final String KEY = "getTableFields()";

        ArrayList<MetaField> fields = (ArrayList<MetaField>) mc.getCacheValue( KEY );

        if ( fields == null )
        {
	        fields = new ArrayList<MetaField>();

	        for( Iterator i = mc.getMetaFields().iterator(); i.hasNext(); )
	        {
	            MetaField f = (MetaField) i.next();
	            if ( isReadableField( f ) && !isViewOnly( f )) fields.add( f );
	        }

	        mc.setCacheValue( KEY, fields );
        }

        return fields;
    }*/


    /**
     * Retrieves the foreign keys defined in the specified MetaClass
     */
  /*public Collection<ForeignKeyDef> getForeignKeys( MetaClass mc )
  {
    List<ForeignKeyDef> fKeys = new ArrayList<ForeignKeyDef>();

    for( MetaField mf : mc.getMetaFields() )
    {
      String fkey = getPersistenceAttribute( mf, FOREIGN_KEY_REF );
      if ( fkey != null )
      {
        int i = fkey.indexOf( "->" );
        if ( i <= 0 )
          throw new IllegalArgumentException( "Invalid Format for " + FOREIGN_KEY_REF + " parameter on MetaField [" + mf + "], no '->' found" );

        String packageName = mc.getPackage();
        String foreignClassStr = fkey.substring( 0, i );
        String foreignFieldStr = fkey.substring( i + 2 );
        MetaClass foreignClass = null;

          if ( foreignClassStr.length() > 0 )
          {
            // Try to find it with the name prepended if not fully qualified
            try {
              if ( foreignClassStr.indexOf( MetaClass.SEPARATOR ) < 0 && packageName.length() > 0 )
                foreignClass = MetaClass.forName( packageName + MetaClass.SEPARATOR + foreignClassStr );
            }
            catch( MetaClassNotFoundException e ) {
              log.debug( "Could not find ForeignKey MetaClass [" + packageName + MetaClass.SEPARATOR + foreignClassStr + "], assuming fully qualified" );
            }

            if ( foreignClass == null ) {
               foreignClass = MetaClass.forName( foreignClassStr );
            }
          }

          // REST HERE
          fKeys.add( new ForeignKeyDef( mf, foreignClass, foreignFieldStr ));
      }
    }

    return fKeys;
  }*/


    /*private boolean isViewOnly( MetaField mf )
    {
	    try {
	      if ( "true".equals( mf.getMetaAttr( IS_VIEWONLY ).getValue())) return true;
	    } catch( MetaAttributeNotFoundException e ) {}
	    return false;
    }*/

	/** Get the persistence attribute */
    private String getPersistenceAttribute( MetaData md, String ref ) {
    	if ( !md.hasMetaAttr( ref )) return null;
		Object r = md.getMetaAttr( ref ).getValue();
		if ( r != null ) return r.toString();
		return null;
	}
    
    /**
     * Retrieves the table name from the MetaClass
     *
     * @return Returns the table name
     * @throws MetaDataException An exception is thrown if the object is not persistable
     */
    protected String getViewRef( MetaObject mc )
    {
      return getPersistenceAttribute( mc, VIEW_REF );
    }

	/**
     * Retrieves the SQL generation for the view
     *
     * @return Returns the SQL to create the view
     * @throws MetaDataException An exception is thrown if the object is not persistable
     */
    protected String getViewSQL( MetaObject mc )
    {
      return getPersistenceAttribute( mc, VIEW_SQL_REF );
    }

    /**
     * Retrieves the table name from the MetaClass
     *
     * @return Returns the table name
     * @throws MetaDataException An exception is thrown if the object is not persistable
     */
    protected String getTableRef( MetaObject mc )
    {
      return getPersistenceAttribute( mc, TABLE_REF );
    }

    /**
     * Retrieves the table column from the MetaField
     *
     * @return Returns the column name
     * @throws MetaDataException An exception is thrown if the column is not persistable
     */
    protected String getColumnRef( MetaField mf )
    {
      return getPersistenceAttribute( mf, COL_REF );
    }
    
    /**
     * Retrieves the sequence name for the MetaField
     *
     * @return Returns the sequence name
     * @throws MetaDataException An exception is thrown if the object is not persistable
     */
    public String getSequenceRef( MetaField mf )
    {
      return getPersistenceAttribute( mf, SEQ_REF );
    }

    /**
     * Retrieves the sequence name for the MetaField
     *
     * @return Returns the sequence name
     * @throws MetaDataException An exception is thrown if the object is not persistable
     */
    public int getSequenceStart( MetaField mf )
    {
      int start = 1;
      try {
        start = Integer.parseInt( getPersistenceAttribute( mf, SEQ_START_REF ));
      } catch( Exception e ) {}
      if ( start < 1 ) start = 1;
      return start;
    }

    
    /**
     * Determines whether the MetaField is a key
     */
    public boolean isIndex( MetaField mf )
    {
      try {
        if ( TRUE.equals( mf.getMetaAttr( IS_INDEX ).getValue())) return true;
      } catch( MetaAttributeNotFoundException e ) {}
      return false;
    }

    /**
     * Determines whether the MetaField is a key
     */
    public boolean isUnique( MetaField mf )
    {
      try {
        if ( TRUE.equals( mf.getMetaAttr( IS_UNIQUE ).getValue())) return true;
      } catch( MetaAttributeNotFoundException e ) {}
      return false;
    }
}
