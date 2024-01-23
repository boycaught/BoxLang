/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ortus.boxlang.runtime.bifs.global.decision;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.DateTimeCaster;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.DateTime;

@BoxBIF
public class IsDate extends BIF {

	/**
	 * Constructor
	 */
	public IsDate() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "any", Key.value ),
		};
	}

	/**
	 * Determine whether a given value is a date object or a date string.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.value Value to test for date-ness
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		Object value = arguments.get( Key.value );
		if ( value instanceof DateTime ) {
			return true;
		} else if ( value instanceof String ) {
			return DateTimeCaster.attempt( value ).wasSuccessful();
		}
		return false;
	}

}