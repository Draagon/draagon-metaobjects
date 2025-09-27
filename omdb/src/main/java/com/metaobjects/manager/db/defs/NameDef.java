package com.metaobjects.manager.db.defs;

/**
 * Stores the name for a database entity, including schema and actual name
 * @author Doug
 */
public class NameDef {

	private String schema = null;
	private String name = null;
	
	public NameDef( String schema, String name ) {
		setSchema( schema );
		setName( name );
	}
	
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		if ( schema != null && schema.trim().length() == 0 ) schema = null;
		this.schema = schema;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if ( name == null || name.trim().length() == 0 ) {
			throw new IllegalArgumentException( "Cannot have null for the name of a DB definition" );
		}
		this.name = name;
	}
	
	/**
	 * Takes a name in the format of <schema>.<name> and breaks it into discreet components
	 * @param fullName  The full DB name
	 * @return A TableDef object with the full name broken into individual fields
	 */
	public static NameDef parseName( String fullName ) {
		
		int i = fullName.indexOf( '.' );
		if ( i < 0 ) {
			return new NameDef( null, fullName );
		} else if ( i == 0 ) {
			return new NameDef( null, fullName.substring( 1 ));
		} else {
			return new NameDef( fullName.substring( 0, i ), fullName.substring( i + 1 ));
		}
	}
	
    /*protected String [] splitName( String fn )
    {
        String s[] = new String[ 3 ];

        for( int j = 2; j >= 0; j-- ) {
          int i = fn.lastIndexOf( '.' );
          if ( i >= 0 ) {
              s[ j ] = fn.substring( i + 1 );
              fn = fn.substring( 0, i );
          }
          else {
              s[ j ] = fn;
              break;
          }
        }

        return s;
    }*/
	
	
	/**
	 * Returns the full name with a '.' appending the schema together
	 * @return full name
	 */
	public String getFullname() {
		StringBuilder sb = new StringBuilder();
		if ( getSchema() != null ) sb.append( getSchema() ).append( '.' );
		if ( getName() != null ) sb.append( getName() );		
		return sb.toString();	
	}
	
	/**
	 * Return the name of the DB entity
	 */
	public String toString() {
		return getFullname();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final NameDef other = (NameDef) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}
}
