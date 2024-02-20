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
package ortus.boxlang.runtime.components.net;

import java.util.Set;

import ortus.boxlang.runtime.components.Attribute;
import ortus.boxlang.runtime.components.BoxComponent;
import ortus.boxlang.runtime.components.Component;
import ortus.boxlang.runtime.components.validators.Validator;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.ExpressionInterpreter;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.BoxValidationException;

@BoxComponent( allowsBody = true )
public class HTTP extends Component {

	/**
	 * --------------------------------------------------------------------------
	 * Constructor(s)
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Required by SLI
	 */
	public HTTP() {
	}

	/**
	 * Constructor
	 *
	 * @param name The name of the component
	 */
	public HTTP( Key name ) {
		super( name );
		declaredAttributes = new Attribute[] {
		    new Attribute( Key.URL, "string", Set.of(
		        Validator.REQUIRED,
		        Validator.NON_EMPTY,
		        ( cxt, comp, attr, attrs ) -> {
			        if ( !attrs.getAsString( attr.name() ).startsWith( "http" ) ) {
				        throw new BoxValidationException( comp, attr, "must start with 'http://' or 'https://'" );
			        }
		        }
		    ) ),
		    new Attribute( Key.result, "string" )
			/*
			 * TODO:
			 * port
			 * method
			 * proxyserver
			 * proxyport
			 * proxyuser
			 * proxypassword
			 * username
			 * password
			 * useragent
			 * charset
			 * resolveurl
			 * throwonerror
			 * redirect
			 * timeout
			 * getasbinary
			 * delimiter
			 * name
			 * columns
			 * firstrowasheaders
			 * textqualifier
			 * file
			 * multipart
			 * clientcertpassword
			 * clientcert
			 * path
			 * compression
			 * authType
			 * domain
			 * workstation
			 * cachedWithin
			 * encodeURL
			 */
		};
	}

	/**
	 * I make an HTTP call
	 *
	 * @param context        The context in which the BIF is being invoked
	 * @param attributes     The attributes to the BIF
	 * @param body           The body of the BIF
	 * @param executionState The execution state of the BIF
	 *
	 */
	public BodyResult _invoke( IBoxContext context, IStruct attributes, ComponentBody body, IStruct executionState ) {
		executionState.put( Key.HTTPParams, new Array() );

		BodyResult bodyResult = processBody( context, body );
		// IF there was a return statement inside our body, we early exit now
		if ( bodyResult.isEarlyExit() ) {
			return bodyResult;
		}

		String	variableName	= StringCaster.cast( attributes.getOrDefault( Key.result, "cfhttp" ) );
		String	theURL			= StringCaster.cast( attributes.dereference( context, Key.URL, false ) );
		Struct	HTTPResult		= new Struct();

		System.out.println( "Make HTTP call to: " + theURL );
		System.out.println( "Using the following HTTP Params: " );
		System.out.println( executionState.getAsArray( Key.HTTPParams ).asString() );

		HTTPResult.put( Key.statusCode, 200 );
		HTTPResult.put( Key.statusText, "OK" );
		HTTPResult.put( Key.fileContent, "This is the response text" );

		// Set the result back into the page
		ExpressionInterpreter.setVariable( context, variableName, HTTPResult );

		return DEFAULT_RETURN;
	}
}
