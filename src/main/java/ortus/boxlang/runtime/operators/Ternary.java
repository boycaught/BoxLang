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
package ortus.boxlang.runtime.operators;

import java.util.function.Function;

import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.BooleanCaster;

/**
 * Performs logical ternary operator
 * condition ? ifTrue : ifFalse
 */
public class Ternary implements IOperator {

	/**
	 *
	 * @param condition Boollean to evaluate
	 * @param ifTrue    Value to use if condition is true
	 * @param ifFalse   Value to use if condition is false
	 *
	 * @return The result of the ternary operation
	 */
	public static Object invoke( IBoxContext context, Object condition, Function<IBoxContext, Object> ifTrue, Function<IBoxContext, Object> ifFalse ) {
		return BooleanCaster.cast( condition ) ? ifTrue.apply( context ) : ifFalse.apply( context );
	}

}
