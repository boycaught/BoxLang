package ortus.boxlang.runtime.bifs.global;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;

@BoxBIF
public class Print extends BIF {

	/**
	 * Constructor
	 */
	public Print() {
		super();
		arguments = new Argument[] {
		    new Argument( true, "any", Key.message )
		};
	}

	/**
	 * Print a message with line break to the console
	 * 
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 * 
	 * @argument.message The message to print
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		System.out.print( arguments.get( Key.message ) );
		return true;
	}
}
