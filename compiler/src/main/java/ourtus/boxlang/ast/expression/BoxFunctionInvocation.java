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
package ourtus.boxlang.ast.expression;

import ourtus.boxlang.ast.BoxExpr;
import ourtus.boxlang.ast.Position;
import ourtus.boxlang.ast.ReferenceByName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoxFunctionInvocation extends BoxExpr {

	private final ReferenceByName name;

	public ReferenceByName getName() {
		return name;
	}

	private final List<BoxArgument> arguments;

	public List<BoxArgument> getArguments() {
		return arguments;
	}

	/**
	 * Function invocation i.e. create(x)
	 * @param name
	 * @param arguments
	 * @param position
	 * @param sourceText
	 */
	public BoxFunctionInvocation(String name, List<BoxArgument> arguments, Position position, String sourceText ) {
		super( position, sourceText );
		this.name      = new ReferenceByName( name );
		this.arguments = Collections.unmodifiableList(arguments);
		this.arguments.stream().forEach( arg -> arg.setParent(this));
	}
}
