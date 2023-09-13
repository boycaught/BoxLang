package ortus.boxlang.runtime.context;

import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Lambda;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.KeyNotFoundException;
import ortus.boxlang.runtime.types.exceptions.ScopeNotFoundException;

/**
 * This context represents the execution of a Lambda. Lambdas are a simpler form of a Function which,
 * unlike UDFs, do not track things like return type, output, etc. Lambdas retain NO reference to the
 * context in which they were created, and do not search scopes outside their local and arguments scope.
 */
public class LambdaBoxContext extends FunctionBoxContext {

	/**
	 * Creates a new execution context with a bounded function instance and parent context
	 *
	 * @param parent   The parent context
	 * @param function The Lambda being invoked with this context
	 */
	public LambdaBoxContext( IBoxContext parent, Lambda function ) {
		this( parent, function, new ArgumentsScope() );
	}

	/**
	 * Creates a new execution context with a bounded function instance and parent context and arguments scope
	 *
	 * @param parent         The parent context
	 * @param function       The Lambda being invoked with this context
	 * @param argumentsScope The arguments scope for this context
	 */
	public LambdaBoxContext( IBoxContext parent, Lambda function, ArgumentsScope argumentsScope ) {
		super( parent, function, argumentsScope );
		if ( parent == null ) {
			throw new IllegalArgumentException( "Parent context cannot be null for LambdaBoxContext" );
		}
	}

	/**
	 * Search for a variable in "nearby" scopes
	 */
	public ScopeSearchResult scopeFindNearby( Key key, IScope defaultScope, boolean shallow ) {

		Object result = localScope.getRaw( key );
		// Null means not found
		if ( result != null ) {
			// Unwrap the value now in case it was really actually null for real
			return new ScopeSearchResult( localScope, Struct.unWrapNull( result ) );
		}

		result = argumentsScope.getRaw( key );
		// Null means not found
		if ( result != null ) {
			// Unwrap the value now in case it was really actually null for real
			return new ScopeSearchResult( argumentsScope, Struct.unWrapNull( result ) );
		}

		// Lambdas don't look anywhere else!
		throw new KeyNotFoundException(
		    String.format( "The requested key [%s] was not located in any scope or it's undefined", key.getName() )
		);

	}

	/**
	 * Look for a scope by name
	 */
	public IScope getScope( Key name ) throws ScopeNotFoundException {
		// Lambda's are a "dead end". They do not "see" anything outside of their local and arguments scopes
		throw new ScopeNotFoundException(
		    String.format( "The requested scope name [%s] was not located in any context", name.getName() )
		);
	}

	/**
	 * Look for a "nearby" scope by name
	 */
	public IScope getScopeNearby( Key name ) throws ScopeNotFoundException {
		// Check the scopes I know about
		if ( name.equals( localScope.getName() ) ) {
			return localScope;
		}
		if ( name.equals( argumentsScope.getName() ) ) {
			return argumentsScope;
		}

		// Lambdas don't look anywhere else!
		throw new ScopeNotFoundException(
		    String.format( "The requested scope name [%s] was not located in any context", name.getName() )
		);

	}

	/**
	 * Returns the function being invoked with this context, cast as a Lambda
	 */
	@Override
	public Lambda getFunction() {
		return ( Lambda ) function;
	}

}
