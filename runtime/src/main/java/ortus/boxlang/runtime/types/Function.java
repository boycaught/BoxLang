/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
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
package ortus.boxlang.runtime.types;

import java.util.HashMap;
import java.util.Map;

import ortus.boxlang.runtime.context.FunctionBoxContext;
import ortus.boxlang.runtime.dynamic.casters.CastAttempt;
import ortus.boxlang.runtime.dynamic.casters.GenericCaster;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;

/**
 * A BoxLang Function base class
 */
public abstract class Function implements IType {

	/**
	 * --------------------------------------------------------------------------
	 * Public Properties
	 * --------------------------------------------------------------------------
	 */

	/**
	 * The supported access levels of the function
	 */
	public enum Access {
		PRIVATE,
		PUBLIC,
		PROTECTED,
		REMOTE
	}

	/**
	 * The argument collection key which defaults to : {@code argumentCollection}
	 */
	public static Key ARGUMENT_COLLECTION = Key.of( "argumentCollection" );

	/**
	 * --------------------------------------------------------------------------
	 * Constructors
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Constructor
	 */
	protected Function() {
	}

	public abstract Key getName();

	public abstract Argument[] getArguments();

	public abstract String getReturnType();

	public abstract String getHint();

	public abstract boolean isOutput();

	public abstract Map<Key, Object> getMetadata();

	/**
	 * Return a string representation of the function
	 */
	public String asString() {
		return toString();
	}

	/**
	 * --------------------------------------------------------------------------
	 * Invokers (These are the methods that are called by the runtime)
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Call this method externally to invoke the function
	 *
	 * @param context
	 *
	 * @return
	 */
	public Object invoke( FunctionBoxContext context ) {
		// Pre/post interceptors?
		return ensureReturnType( _invoke( context ) );
	}

	/**
	 * Implement this method to invoke the actual function logic
	 *
	 * @param context
	 *
	 * @return
	 */
	public abstract Object _invoke( FunctionBoxContext context );

	/**
	 * --------------------------------------------------------------------------
	 * Arguments Helpers
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Create an arguments scope from the positional arguments
	 *
	 * @param positionalArguments The positional arguments
	 *
	 * @return The arguments scope
	 */
	public ArgumentsScope createArgumentsScope( Object[] positionalArguments ) {
		Argument[]		arguments	= getArguments();
		ArgumentsScope	scope		= new ArgumentsScope();
		// Add all incoming args to the scope, using the name if declared, otherwise using the position
		for ( int i = 0; i < positionalArguments.length; i++ ) {
			Key		name;
			Object	value	= positionalArguments[ i ];
			if ( arguments.length - 1 >= i ) {
				name	= arguments[ i ].name();
				value	= ensureArgumentType( name, value, arguments[ i ].type() );
			} else {
				name = Key.of( Integer.toString( i + 1 ) );
			}
			scope.put( name, value );
		}

		// Fill in any remaining declared arguments with default value
		if ( arguments.length > scope.size() ) {
			for ( int i = scope.size(); i < arguments.length; i++ ) {
				if ( arguments[ i ].required() && arguments[ i ].defaultValue() == null ) {
					throw new RuntimeException( "Required argument " + arguments[ i ].name() + " is missing" );
				}
				scope.put( arguments[ i ].name(),
				    ensureArgumentType( arguments[ i ].name(), arguments[ i ].defaultValue(), arguments[ i ].type() ) );
			}
		}
		return scope;
	}

	/**
	 * Create an arguments scope from the named arguments
	 *
	 * @param namedArguments The named arguments
	 *
	 * @return The arguments scope
	 */
	public ArgumentsScope createArgumentsScope( Map<Key, Object> namedArguments ) {
		Argument[]		arguments	= getArguments();
		ArgumentsScope	scope		= new ArgumentsScope();

		// If argumentCollection exists, add it
		if ( namedArguments.containsKey( ARGUMENT_COLLECTION )
		    && namedArguments.get( ARGUMENT_COLLECTION ) instanceof Map<?, ?> ) {
			@SuppressWarnings( "unchecked" )
			Map<Key, Object> argumentCollection = ( Map<Key, Object> ) namedArguments.get( ARGUMENT_COLLECTION );
			scope.putAll( argumentCollection );
			namedArguments.remove( ARGUMENT_COLLECTION );
		}

		// Put all remaining incoming args
		scope.putAll( namedArguments );

		// For all declared args
		for ( Argument argument : arguments ) {
			// If they aren't here, add their default value (if defined)
			if ( !scope.containsKey( argument.name() ) ) {
				if ( argument.required() && argument.defaultValue() == null ) {
					throw new RuntimeException( "Required argument " + argument.name() + " is missing" );
				}
				// Make sure the default value is valid
				scope.put( argument.name(), ensureArgumentType( argument.name(), argument.defaultValue(), argument.type() ) );
				// If they are here, confirm their types
			} else {
				scope.put( argument.name(),
				    ensureArgumentType( argument.name(), scope.get( argument.name() ), argument.type() ) );
			}
		}
		return scope;
	}

	/**
	 * Create an arguments scope from no arguments
	 *
	 * @return The arguments scope
	 */
	public ArgumentsScope createArgumentsScope() {
		return createArgumentsScope( new Object[] {} );
	}

	/**
	 * Ensure the argument is the correct type
	 *
	 * @param name  The name of the argument
	 * @param value The value of the argument
	 * @param type  The type of the argument
	 *
	 * @return The value of the argument
	 *
	 * @throws RuntimeException if the argument is not the correct type
	 */
	protected Object ensureArgumentType( Key name, Object value, String type ) {
		CastAttempt<Object> typeCheck = GenericCaster.attempt( value, type, true );
		if ( !typeCheck.wasSuccessful() ) {
			throw new RuntimeException(
			    String.format( "Argument [%s] with a type of [%s] does not match the declared type of [%s]",
			        name.getName(), value.getClass().getName(), type )
			);
		}
		// Should we actually return the casted value??? Not CFML Compat!
		return value;
	}

	/**
	 * Ensure the return value of the function is the correct type
	 *
	 * @param value
	 *
	 * @return
	 */
	protected Object ensureReturnType( Object value ) {
		CastAttempt<Object> typeCheck = GenericCaster.attempt( value, getReturnType(), true );
		if ( !typeCheck.wasSuccessful() ) {
			throw new RuntimeException(
			    String.format( "The return value of the function [%s] does not match the declared type of [%s]",
			        value.getClass().getName(), getReturnType() )
			);
		}
		// Should we actually return the casted value??? Not CFML Compat!
		return value;
	}

	/**
	 * Represents an argument to a function
	 *
	 * @param required     Whether the argument is required
	 * @param type         The type of the argument
	 * @param name         The name of the argument
	 * @param defaultValue The default value of the argument
	 *
	 */
	public record Argument( boolean required, String type, Key name, Object defaultValue, String hint, Map<Key, Object> metadata ) {

		public Argument( Key name ) {
			this( false, "any", name, null, "", new HashMap<>() );
		}

		public Argument( boolean required, String type, Key name ) {
			this( required, type, name, null, "", new HashMap<>() );
		}

		public Argument( boolean required, String type, Key name, Object defaultValue ) {
			this( required, type, name, defaultValue, "", new HashMap<>() );
		}

		public Argument( boolean required, String type, Key name, Object defaultValue, String hint ) {
			this( required, type, name, defaultValue, hint, new HashMap<>() );
		}

		public Argument( boolean required, String type, Key name, Object defaultValue, String hint, Map<Key, Object> metadata ) {
			this.required		= required;
			this.type			= type;
			this.name			= name;
			this.defaultValue	= defaultValue;
			this.hint			= hint;
			this.metadata		= metadata;
		}

	}
}
