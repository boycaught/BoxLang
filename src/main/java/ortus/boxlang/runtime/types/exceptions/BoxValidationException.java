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
package ortus.boxlang.runtime.types.exceptions;

import ortus.boxlang.runtime.components.Attribute;
import ortus.boxlang.runtime.components.Component;

/**
 * Configuration exceptions within BoxLang
 */
public class BoxValidationException extends BoxRuntimeException {

	/**
	 * Constructor
	 *
	 * @param message The message
	 */
	public BoxValidationException( String message ) {
		this( message, null );
	}

	/**
	 * Constructor
	 *
	 * @param message The message
	 */
	public BoxValidationException( Component component, Attribute attribute, String message ) {
		this( "Attribute [" + attribute.name().getName() + "] for component [" + component.getName().getName() + "] " + message, null );
	}

	/**
	 * Constructor
	 *
	 * @param message The message
	 * @param cause   The cause
	 */
	public BoxValidationException( String message, Throwable cause ) {
		super( message, "BoxValidationException", cause );
	}

}
