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
package ortus.boxlang.runtime.components.system;

import java.util.Optional;
import java.util.Set;

import ortus.boxlang.runtime.components.Attribute;
import ortus.boxlang.runtime.components.BoxComponent;
import ortus.boxlang.runtime.components.Component;
import ortus.boxlang.runtime.components.validators.Validator;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;

@BoxComponent
public class Dump extends Component {

	/**
	 * --------------------------------------------------------------------------
	 * Constructor(s)
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Required by SLI
	 */
	public Dump() {
	}

	public Dump( Key name ) {
		super( name );
		declaredAttributes = new Attribute[] {
		    new Attribute( Key.var, "any", Set.of( Validator.REQUIRED ) )
		};
	}

	/**
	 * I create a representation of objects
	 *
	 * @param context        The context in which the BIF is being invoked
	 * @param attributes     The attributes to the BIF
	 * @param body           The body of the BIF
	 * @param executionState The execution state of the BIF
	 *
	 */
	public Optional<Object> _invoke( IBoxContext context, IStruct attributes, ComponentBody body, IStruct executionState ) {
		Object theVar = attributes.get( Key.var );
		runtime.getFunctionService().getGlobalFunction( Key.dump ).invoke( context, new Object[] { theVar }, false, Key.dump );
		return DEFAULT_RETURN;
	}
}
