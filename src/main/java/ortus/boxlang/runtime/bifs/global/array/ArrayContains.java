package ortus.boxlang.runtime.bifs.global.array;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.BooleanCaster;
import ortus.boxlang.runtime.operators.Compare;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.Function;

// @BoxMember( "Array", "contains", "array" )
// @BoxMember( "Array", "find", "array" )
// @BoxIgnoreBIF()
public class ArrayContains extends BIF {

	/**
	 * Constructor
	 */
	public ArrayContains() {
		super();
		arguments = new Argument[] {
		    new Argument( true, "array", Key.array ),
		    new Argument( true, "any", Key.value )
		};
	}

	/**
	 * Return int position of value in array, case sensitive
	 * 
	 * @param context
	 * @param arguments Argument scope defining the array.
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		Array	actualArray	= arguments.getAsArray( Key.array );
		Object	value		= arguments.get( Key.value );

		if ( value instanceof Function callback ) {
			for ( int i = 0; i < actualArray.size(); i++ ) {
				if ( BooleanCaster.cast(
				    // Invoke Function
				    context.invokeFunction(
				        // Function object
				        actualArray.get( i ),
				        new Object[] {
				            // Pass this array value
				            actualArray.get( i ),
				            // current loop index
				            i + 1,
				            // full original array
				            actualArray }
				    )
				) ) {
					return i + 1;
				}
			}
			return 0;
		}
		return ArrayContains._invoke( actualArray, value );
	}

	/**
	 * Return int position of value in array, case sensitive
	 * 
	 * @param context
	 * @param arguments Argument scope defining the array.
	 */
	public static int _invoke( Array array, Object value ) {
		for ( int i = 0; i < array.size(); i++ ) {
			if ( Compare.invoke( array.get( i ), value, true ) == 0 ) {
				return i + 1;
			}
		}
		return 0;
	}

}
