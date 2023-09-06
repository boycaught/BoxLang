package ortus.boxlang.runtime.functions.global;

import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.functions.BIF;

public class Println extends BIF {

	public static Object invoke( IBoxContext context, String message ) throws RuntimeException {
		System.out.println( message );
		return true;
	}
}
