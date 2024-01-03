/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.runtime.types;

import java.util.Map;

import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.IReferenceable;
import ortus.boxlang.runtime.dynamic.casters.CastAttempt;
import ortus.boxlang.runtime.dynamic.casters.DoubleCaster;
import ortus.boxlang.runtime.interop.DynamicJavaInteropService;
import ortus.boxlang.runtime.scopes.IntKey;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class QueryColumn implements IReferenceable {

	private Key				name;
	private QueryColumnType	type;
	private Query			query;
	// Keep in sync if columns are added or removed
	private int				index;

	public QueryColumn( Key name, QueryColumnType type, Query query, int index ) {
		this.name	= name;
		this.type	= type;
		this.query	= query;
		this.index	= index;
	}

	public Key getName() {
		return name;
	}

	public QueryColumnType getType() {
		return type;
	}

	public Query getQuery() {
		return query;
	}

	public int getIndex() {
		return index;
	}

	// Convenience methods

	/**
	 * Set the value of a cell in this column
	 * 
	 * @param row   The row to set, 0-based index
	 * @param value The value to set
	 * 
	 * @return This QueryColumn
	 */
	public QueryColumn setCell( int row, Object value ) {
		query.validateRow( row );
		query.getData().get( row )[ index ] = value;
		return this;
	}

	/**
	 * Get the value of a cell in this column
	 * 
	 * @param row The row to get, 0-based index
	 * 
	 * @return The value of the cell
	 */
	public Object getCell( int row ) {
		return this.query.getData().get( row )[ index ];
	}

	/**
	 * Get all data in a column as a Java Object[]
	 * Data is copied, so re-assignments into the array will not be reflected in the query.
	 * Mutating a complex object in the array will be reflected in the query.
	 * 
	 * @param name column name
	 * 
	 * @return array of column data
	 */
	public Object[] getColumnData( Key name ) {
		return query.getColumnData( name );
	}

	/**
	 * Get all data in a column as an BoxLang Array
	 * Data is copied, so re-assignments into the array will not be reflected in the query.
	 * Mutating a complex object in the array will be reflected in the query.
	 * 
	 * @param name column name
	 * 
	 * @return array of column data
	 */
	public Array getColumnDataAsArray( Key name ) {
		return query.getColumnDataAsArray( name );
	}

	public static int getIntFromKey( Key key, boolean safe ) {
		Integer index;

		// If key is int, use it directly
		if ( key instanceof IntKey intKey ) {
			index = intKey.getIntValue();
		} else {
			// If key is not an int, we must attempt to cast it
			CastAttempt<Double> indexAtt = DoubleCaster.attempt( key.getName() );
			if ( !indexAtt.wasSuccessful() ) {
				if ( safe ) {
					return -1;
				}
				throw new BoxRuntimeException( String.format(
				    "Query column cannot be assigned with key %s", key.getName()
				) );
			}
			Double dIndex = indexAtt.get();
			index = dIndex.intValue();
			// Dissallow non-integer indexes foo[1.5]
			if ( index.doubleValue() != dIndex ) {
				if ( safe ) {
					return -1;
				}
				throw new BoxRuntimeException( String.format(
				    "Query column index [%s] is invalid.  Index must be an integer.", dIndex
				) );
			}
		}
		return index;
	}

	/***************************
	 * IReferencable implementation
	 ****************************/

	@Override
	public Object dereference( Key name, Boolean safe ) {
		int index = getIntFromKey( name, safe );
		return getCell( index );
	}

	@Override
	public Object dereferenceAndInvoke( IBoxContext context, Key name, Object[] positionalArguments, Boolean safe ) {
		return DynamicJavaInteropService.invoke( this, name.getName(), safe, positionalArguments );
	}

	@Override
	public Object dereferenceAndInvoke( IBoxContext context, Key name, Map<Key, Object> namedArguments, Boolean safe ) {
		return DynamicJavaInteropService.invoke( this, name.getName(), safe, namedArguments );
	}

	@Override
	public Object assign( Key name, Object value ) {
		throw new BoxRuntimeException( "You cannot assign a field on a QueryColumn." );
	}

}
