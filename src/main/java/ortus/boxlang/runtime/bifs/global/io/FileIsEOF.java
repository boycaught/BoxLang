
package ortus.boxlang.runtime.bifs.global.io;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.File;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF

public class FileIsEOF extends BIF {

	/**
	 * Constructor
	 */
	public FileIsEOF() {
		super();
		// Uncomment and define declare argument to this BIF
		declaredArguments = new Argument[] {
		    new Argument( true, "any", Key.file )
		};
	}

	/**
	 * Describe what the invocation of your bif function does
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.foo Describe any expected arguments
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		if ( arguments.get( Key.file ) instanceof File ) {
			File file = ( File ) arguments.get( Key.file );
			return file.isEOF();
		} else {
			throw new BoxRuntimeException( "The file [" + arguments.getAsString( Key.file ) + "] is not an open file stream." );
		}
	}

}
