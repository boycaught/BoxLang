
package ortus.boxlang.runtime.bifs.global.array;

import java.util.stream.IntStream;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.bifs.BoxMember;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.BoxLangType;

@BoxBIF
@BoxMember( type = BoxLangType.ARRAY )

public class ArrayReverse extends BIF {

	/**
	 * Constructor
	 */
	public ArrayReverse() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "array", Key.array )
		};
	}

	/**
	 * Returns an array with all of the elements reversed. The value in [0] within the input array will then exist in [n] in the output array, where n is
	 * the amount of elements in the array minus one.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.array The array to reverse
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		Array	actualObj	= arguments.getAsArray( Key.array );
		int		size		= actualObj.size();

		return Array.fromList( IntStream.iterate( size - 1, i -> i - 1 )
		    .limit( size )
		    .mapToObj( i -> actualObj.get( i ) )
		    .toList() );
	}

}
