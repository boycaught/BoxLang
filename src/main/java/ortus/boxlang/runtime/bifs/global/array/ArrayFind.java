package ortus.boxlang.runtime.bifs.global.array;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.ArrayCaster;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.Array;

public class ArrayFind extends BIF {

	private final static Key	array	= Key.of( "array" );
	private final static Key	value	= Key.of( "value " );

	/**
	 * Constructor
	 */
	public ArrayFind() {
		super();
		arguments = new Argument[] {
		    new Argument( true, "any", array ),
		    new Argument( true, "any", value )
		};
	}

	/**
	 * Return int position of value in array, case sensitive
	 * 
	 * @param context
	 * @param arguments Argument scope defining the array.
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		Array	actualArray	= ArrayCaster.cast( arguments.dereference( array, false ) );
		Object	value		= arguments.dereference( this.value, false );

		return ArrayContains._invoke( actualArray, value );
	}

}
