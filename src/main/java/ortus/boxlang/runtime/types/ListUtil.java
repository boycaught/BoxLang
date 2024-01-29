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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.ArrayCaster;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

/**
 * I handle basic list operations. I assume 1-based indexes. All casting to strings must be done prior to calling these methods.
 */
public class ListUtil {

	public static final String DEFAULT_DELIMITER = ",";

	/**
	 * Turns a list into a string
	 *
	 * @param list      The list to turn into a string
	 * @param delimiter The delimiter to use
	 *
	 * @return The string representation
	 */
	public static String asString( Array list, String delimiter ) {
		return list.stream()
		    .map( StringCaster::cast )
		    .collect( Collectors.joining( delimiter ) );
	}

	/**
	 * Turns a string in to an Array
	 *
	 * @param string    The string to turn into a list
	 * @param delimiter The delimiter to use
	 *
	 * @return The Java List representation
	 */
	public static Array asList( String list, String delimiter ) {
		return asList( list, delimiter, false, false );
	}

	/**
	 * Creates an array from a delimited list
	 *
	 * @param list           The string lists
	 * @param delimiter      The delimiter(s) of the list
	 * @param includeEmpty   Whether to include empty items in the result array
	 * @param wholeDelimiter Whether the delimiter contains multiple characters which should be matched. Otherwise all characters in the delimiter are
	 *                       treated as separate delimiters
	 *
	 * @return
	 */
	public static Array asList(
	    String list,
	    String delimiter,
	    Boolean includeEmpty,
	    Boolean wholeDelimiter ) {

		String[] result = null;
		if ( wholeDelimiter ) {
			if ( includeEmpty ) {
				result = StringUtils.splitByWholeSeparatorPreserveAllTokens( list, delimiter );
			} else {
				result = StringUtils.splitByWholeSeparator( list, delimiter );
			}
		} else if ( includeEmpty ) {
			result = StringUtils.splitPreserveAllTokens( list, delimiter );
		} else {
			result = StringUtils.split( list, delimiter );
		}
		return new Array( result );
	}

	/**
	 * Find the index of a value in a list
	 *
	 * @param list      The list to search
	 * @param value     The value to search for
	 * @param delimiter The delimiter to use
	 *
	 * @return The (1-based) index of the value or 0 if not found
	 */
	public static int indexOf( String list, String value, String delimiter ) {
		return asList( list, delimiter ).findIndex( value, true );
	}

	/**
	 * Find the index of a value in a list case insensitive
	 *
	 * @param list      The list to search
	 * @param value     The value to search for
	 * @param delimiter The delimiter to use
	 *
	 * @return The (1-based) index of the value or 0 if not found
	 */
	public static int indexOfNoCase( String list, String value, String delimiter ) {
		return asList( list.toLowerCase(), delimiter ).findIndex( value, false );
	}

	/**
	 * Determine if a value is in a list
	 *
	 * @param list      The list to search
	 * @param value     The value to search for
	 * @param delimiter The delimiter to use
	 *
	 * @return True if the value is in the list
	 */
	public static Boolean contains( String list, String value, String delimiter ) {
		return indexOf( list, value, delimiter ) > 0;
	}

	/**
	 * Determine if a value is in a list
	 *
	 * @param list      The list to search
	 * @param value     The value to search for
	 * @param delimiter The delimiter to use
	 *
	 * @return True if the value is in the list
	 */
	public static Boolean containsNoCase( String list, String value, String delimiter ) {
		return indexOfNoCase( list, value, delimiter ) > 0;
	}

	/**
	 * Get an item at a specific (1-based) index
	 *
	 * @param list      The list to search
	 * @param index     The index to get
	 * @param delimiter The delimiter to use
	 *
	 * @return The value at the index if found
	 */
	public static String getAt( String list, int index, String delimiter ) {
		return StringCaster.cast( asList( list, delimiter ).getAt( index ) );
	}

	/**
	 * Set an item at a specific (1-based) index
	 *
	 * @param list      The list to set into
	 * @param index     The index to set
	 * @param delimiter The delimiter to use
	 *
	 * @return The new list
	 */
	public static String setAt( String list, int index, String value, String delimiter ) {
		return asString( asList( list, delimiter ).setAt( index, value ), delimiter );
	}

	/**
	 * Append an item to the end of a list
	 *
	 * @param list      The list to append to
	 * @param value     The value to append
	 * @param delimiter The delimiter to use
	 *
	 * @return The new list
	 */
	public static String append( String list, String value, String delimiter ) {
		Array jList = asList( list, delimiter );
		jList.add( value );
		return asString( jList, delimiter );
	}

	/**
	 * Prepend an item to the beginning of a list
	 *
	 * @param list      The list to prepend to
	 * @param value     The value to prepend
	 * @param delimiter The delimiter to use
	 *
	 * @return The new list
	 */
	public static String prepend( String list, String value, String delimiter ) {
		Array jList = asList( list, delimiter );
		jList.add( 0, value );
		return asString( jList, delimiter );
	}

	/**
	 * Insert an item at a specific (1-based) index
	 *
	 * @param list      The list to insert into
	 * @param index     The index to insert at
	 * @param value     The value to insert
	 * @param delimiter The delimiter to use
	 *
	 * @return The new list
	 */
	public static String insertAt( String list, int index, String value, String delimiter ) {
		Array jList = asList( list, delimiter );
		// Throw if index is out of bounds
		if ( index < 1 || index > jList.size() + 1 ) {
			throw new BoxRuntimeException( "Index out of bounds for list with " + jList.size() + " elements." );
		}
		jList.add( index - 1, value );
		return asString( jList, delimiter );
	}

	/**
	 * Remove an item at a specific (1-based) index
	 *
	 * @param list      The list to remove from
	 * @param index     The index to remove
	 * @param delimiter The delimiter to use
	 *
	 * @return The new list
	 */
	public static String deleteAt( String list, int index, String delimiter ) {
		Array jList = asList( list, delimiter );
		// Throw if index is out of bounds
		if ( index < 1 || index > jList.size() ) {
			throw new BoxRuntimeException( "Index out of bounds for list with " + jList.size() + " elements." );
		}
		jList.remove( index - 1 );
		return asString( jList, delimiter );
	}

	/**
	 * Method to filter an list with a function callback and context
	 *
	 * @param array           The array object to filter
	 * @param callback        The callback Function object
	 * @param callbackContext The context in which to execute the callback
	 * @param parallel        Whether to process the filter in parallel
	 * @param maxThreads      Optional max threads for parallel execution
	 *
	 * @return A filtered array
	 */
	public static Array filter(
	    Array array,
	    Function callback,
	    IBoxContext callbackContext,
	    Boolean parallel,
	    Integer maxThreads ) {

		IntPredicate	test	= idx -> ( boolean ) callbackContext.invokeFunction( callback,
		    new Object[] { array.get( idx ), idx + 1, array } );

		ForkJoinPool	pool	= null;
		if ( parallel ) {
			pool = new ForkJoinPool( maxThreads );
		}

		return ArrayCaster.cast(
		    pool == null
		        ? array.intStream()
		            .filter( test )
		            .mapToObj( array::get )
		            .toArray()

		        : CompletableFuture.supplyAsync(
		            () -> array.intStream().parallel().filter( test ).mapToObj( array::get ),
		            pool
		        ).join().toArray()
		);
	}

}
