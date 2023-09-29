/**
 * [BoxLang]
 *
 * Copytype [2023] [Ortus Solutions, Corp]
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
package ortus.boxlang.runtime.dynamic.casters;

import java.math.BigDecimal;
import java.util.List;

import ortus.boxlang.runtime.types.NullValue;
import ortus.boxlang.runtime.types.exceptions.ApplicationException;

/**
 * I handle casting anything
 */
public class GenericCaster {

	/**
	 * Tests to see if the value can be cast
	 * Returns a {@code CastAttempt<T>} which will contain the result if casting was
	 * was successfull, or can be interogated to proceed otherwise.
	 * If the cast type was "null" or "void" the CastAttempt will contain a NullValue() instance.
	 * If the input value is null and the type is "any", the CastAttempt will contain a NullValue() instance.
	 *
	 * @param object The value to cast
	 * @param oType  The type to cast to
	 * @param strict True to throw exception when casting non-null value to null/void
	 *
	 * @return A CastAttempt, which contains the casted value, if successful
	 */
	public static CastAttempt<Object> attempt( Object object, Object oType, boolean strict ) {
		String type = StringCaster.cast( oType ).toLowerCase();

		// Represent legit null values in a NullValue instance
		if ( type.equalsIgnoreCase( "null" ) || type.equalsIgnoreCase( "void" ) ) {
			if ( strict && object != null ) {
				throw new ApplicationException(
				    String.format( "Cannot cast type [%s] to %s.", object.getClass().getName(), type )
				);
			}
			return CastAttempt.ofNullable( new NullValue() );
		}

		// Represent legit null values in a NullValue instance
		if ( type.equalsIgnoreCase( "any" ) && object == null ) {
			return CastAttempt.ofNullable( new NullValue() );
		}

		return CastAttempt.ofNullable( cast( object, type, false ) );
	}

	/**
	 * Tests to see if the value can be cast
	 * Returns a {@code CastAttempt<T>} which will contain the result if casting was
	 * was successfull, or can be interogated to proceed otherwise.
	 * If the cast type was "null" or "void" the CastAttempt will contain a NullValue() instance.
	 * If the input value is null and the type is "any", the CastAttempt will contain a NullValue() instance.
	 *
	 * @param object The value to cast
	 * @param oType  The type to cast to
	 *
	 * @return A CastAttempt, which contains the casted value, if successful
	 */
	public static CastAttempt<Object> attempt( Object object, Object oType ) {
		return attempt( object, oType, false );
	}

	/**
	 * Used to cast anything, throwing exception if we fail
	 *
	 * @param object The value to cast
	 * @param oType  The type to cast to
	 *
	 * @return The value
	 */
	public static Object cast( Object object, Object oType ) {
		return cast( object, oType, true );
	}

	/**
	 * Used to cast anything. Note, when fail is set to false, it is not possible to differentiate between
	 * a failed cast and a successful cast to type "null" or "void". The same ambiguity exists for an input
	 * of null and a type of "any". For these cases, use the attempt() method and check the optional
	 * for a NullValue() instance.
	 *
	 * @param object The value to cast
	 * @param oType  The type to cast to
	 * @param fail   True to throw exception when failing.
	 *
	 * @return The value, or null when cannot be cast or if the type was "null" or "void"
	 */
	public static Object cast( Object object, Object oType, Boolean fail ) {
		String type = StringCaster.cast( oType ).toLowerCase();

		if ( type.equals( "null" ) || type.equals( "void" ) ) {
			return null;
		}

		if ( type.equals( "any" ) ) {
			return object;
		}

		// Handle arrays like int[]
		if ( type.endsWith( "[]" ) ) {

			Object[] incomingList;

			if ( object.getClass().isArray() ) {
				incomingList = ( Object[] ) object;
			} else if ( object instanceof List ) {
				incomingList = ( ( List<?> ) object ).toArray();
			} else {
				throw new ApplicationException(
				    String.format( "You asked for type %s, but input %s cannot be cast to an array.", type,
				        object.getClass().getName() )
				);
			}

			String		newType	= type.substring( 0, type.length() - 2 );
			Object[]	result	= ( Object[] ) java.lang.reflect.Array.newInstance( getClassFromType( newType ),
			    incomingList.length );

			for ( int i = incomingList.length - 1; i >= 0; i-- ) {
				result[ i ] = GenericCaster.cast( incomingList[ i ], newType, fail );
			}
			return result;

		}

		if ( type.equals( "string" ) ) {
			return StringCaster.cast( object, fail );
		}
		if ( type.equals( "double" ) || type.equals( "numeric" ) ) {
			return DoubleCaster.cast( object, fail );
		}
		if ( type.equals( "boolean" ) ) {
			return BooleanCaster.cast( object, fail );
		}
		if ( type.equals( "bigdecimal" ) ) {
			return BigDecimalCaster.cast( object, fail );
		}
		if ( type.equals( "char" ) ) {
			return CharacterCaster.cast( object, fail );
		}
		if ( type.equals( "byte" ) ) {
			return ByteCaster.cast( object, fail );
		}
		if ( type.equals( "int" ) || type.equals( "integer" ) ) {
			return IntegerCaster.cast( object, fail );
		}
		if ( type.equals( "long" ) ) {
			return LongCaster.cast( object, fail );
		}
		if ( type.equals( "short" ) ) {
			return ShortCaster.cast( object, fail );
		}
		if ( type.equals( "float" ) ) {
			return FloatCaster.cast( object, fail );
		}

		throw new ApplicationException(
		    String.format( "Invalid cast type [%s]", type )
		);
	}

	public static Class<?> getClassFromType( String type ) {

		if ( type.equals( "string" ) ) {
			return String.class;
		}
		if ( type.equals( "double" ) ) {
			return Double.class;
		}
		if ( type.equals( "boolean" ) ) {
			return Boolean.class;
		}
		if ( type.equals( "bigdecimal" ) ) {
			return BigDecimal.class;
		}
		if ( type.equals( "char" ) ) {
			return Character.class;
		}
		if ( type.equals( "byte" ) ) {
			return Byte.class;
		}
		if ( type.equals( "int" ) ) {
			return Integer.class;
		}
		if ( type.equals( "long" ) ) {
			return Long.class;
		}
		if ( type.equals( "short" ) ) {
			return Short.class;
		}
		if ( type.equals( "float" ) ) {
			return Float.class;
		}
		throw new ApplicationException(
		    String.format( "Invalid cast type [%s]", type )
		);
	}
}
