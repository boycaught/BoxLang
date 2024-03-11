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
package ortus.boxlang.runtime.components.jdbc;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ortus.boxlang.runtime.components.Attribute;
import ortus.boxlang.runtime.components.BoxComponent;
import ortus.boxlang.runtime.components.Component;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.validation.Validator;

@BoxComponent( allowsBody = true )
public class Transaction extends Component {

	Logger log = LoggerFactory.getLogger( Transaction.class );

	/**
	 * Constructor
	 */
	public Transaction() {
		super();
		declaredAttributes = new Attribute[] {
		    new Attribute( Key.action, "string", Set.of(
		        Validator.valueOneOf(
		            "begin",
		            "commit",
		            "rollback",
		            "setsavepoint"
		        )
		    ) ),
		    new Attribute( Key.isolation, "string", Set.of(
		        Validator.valueOneOf(
		            "read_uncommitted",
		            "read_committed",
		            "repeatable_read",
		            "serializable"
		        )
		    ) ),
		    new Attribute( Key.savepoint, "string", Set.of() ),
		    new Attribute( Key.type, "boolean", false, Set.of(
		        Validator.TYPE
		    ) )
		};
	}

	/**
	 *
	 *
	 * @param context        The context in which the Component is being invoked
	 * @param attributes     The attributes to the Component
	 * @param body           The body of the Component
	 * @param executionState The execution state of the Component
	 *
	 */
	public BodyResult _invoke( IBoxContext context, IStruct attributes, ComponentBody body, IStruct executionState ) {
		BodyResult bodyResult = processBody( context, body );
		// IF there was a return statement inside our body, we early exit now
		if ( bodyResult.isEarlyExit() ) {
			return bodyResult;
		}
		return DEFAULT_RETURN;
	}
}
