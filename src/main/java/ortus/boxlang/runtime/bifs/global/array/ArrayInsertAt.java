package ortus.boxlang.runtime.bifs.global.array;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.ArrayCaster;
import ortus.boxlang.runtime.dynamic.casters.IntegerCaster;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.Array;

public class ArrayInsertAt extends BIF {

	private final static Key	array		= Key.of( "array" );
	private final static Key	position	= Key.of( "position" );
	private final static Key	value		= Key.of( "value" );

	/**
	 * Constructor
	 */
	public ArrayInsertAt() {
		super();
		arguments = new Argument[] {
		    new Argument( true, "any", array ),
		    new Argument( true, "any", position ),
		    new Argument( true, "any", value )
		};
	}

	/**
	 * Append a value to an array
	 *
	 * @param context
	 * @param arguments Argument scope defining the array and value to append.
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		Array	actualArray	= ArrayCaster.cast( arguments.dereference( array, false ) );
		int		position	= IntegerCaster.cast( arguments.dereference( this.position, false ) );
		actualArray.add( position - 1, arguments.dereference( value, false ) );
		return actualArray;
	}

}
