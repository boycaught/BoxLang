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

public class BoxMethodInvocation extends BoxExpr {

	private final ReferenceByName name;

	private final List<BoxArgument> arguments;
	private final BoxExpr obj;

	public final List<BoxArgument> getArguments() {
		return arguments;
	}

	/**
	 * Method Invocation i.e. object.method(1,2)
	 * @param name
	 * @param obj
	 * @param arguments
	 * @param position
	 * @param sourceText
	 */
	public BoxMethodInvocation(String name,BoxExpr obj,List<BoxArgument> arguments,Position position, String sourceText ) {
		super( position, sourceText );
		this.name      = new ReferenceByName( name );
		this.obj       = obj;
		this.obj.setParent(this);
		this.arguments = Collections.unmodifiableList(arguments);
		this.arguments.forEach(arg -> arg.setParent(this));
	}

	public ReferenceByName getName() {
		return name;
	}

	public BoxExpr getObj() {
		return obj;
	}
}
