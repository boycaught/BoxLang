package ortus.boxlang.runtime.bifs.global.math;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.DoubleCaster;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class RandRange extends BIF {

    private final static Key number1   = Key.of( "number1" );
    private final static Key number2   = Key.of( "number2" );
    private final static Key algorithm = Key.of( "algorithm" );

    public static Argument[] arguments = new Argument[] {
        new Argument( true, "numeric", number1 ),
        new Argument( true, "numeric", number2 ),
        new Argument( algorithm )
    };

    /**
     * 
     * Return a random int between number1 and number 2
     * 
     * @param context
     * @param number1 A numeric value that represents the range minimum
     * @param number2 A numeric value that represents the range maximum (not inclusive)
     * 
     * @return
     */
    public static Object invoke( IBoxContext context, ArgumentsScope arguments ) {
        if ( arguments.containsKey( algorithm ) && arguments.dereference( algorithm, false ) != null ) {
            throw new BoxRuntimeException( "The algorithm argument has not yet been implemented" );
        }

        Double numA = DoubleCaster.cast( arguments.dereference( number1, false ) );
        Double numB = DoubleCaster.cast( arguments.dereference( number2, false ) );

        return ( int ) ( numA + Rand.invoke( context, new ArgumentsScope() ) * ( numB - numA ) );
    }

}
