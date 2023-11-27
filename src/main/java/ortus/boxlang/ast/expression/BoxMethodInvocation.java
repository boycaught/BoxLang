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
package ortus.boxlang.ast.expression;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.Position;
import ortus.boxlang.ast.ReferenceByName;

/**
 * AST Node representing a method invocation like:
 * <code>object.method(1,"a")</code>
 */
public class BoxMethodInvocation extends BoxExpr {

	private final ReferenceByName	name;

	private final List<BoxArgument>	arguments;
	private final BoxExpr			obj;

	public final List<BoxArgument> getArguments() {
		return arguments;
	}

	/**
	 * Creates the AST node
	 *
	 * @param name       name of the method
	 * @param obj        object
	 * @param arguments  list of BoxArgument representing the arguments
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 *
	 * @see BoxArgument
	 */
	public BoxMethodInvocation( String name, BoxExpr obj, List<BoxArgument> arguments, Position position, String sourceText ) {
		super( position, sourceText );
		this.name	= new ReferenceByName( name );
		this.obj	= obj;
		this.obj.setParent( this );
		this.arguments = Collections.unmodifiableList( arguments );
		this.arguments.forEach( arg -> arg.setParent( this ) );
	}

	public ReferenceByName getName() {
		return name;
	}

	public BoxExpr getObj() {
		return obj;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "obj", obj.toMap() );
		map.put( "name", name );
		map.put( "arguments", arguments.stream().map( BoxExpr::toMap ).collect( Collectors.toList() ) );
		return map;
	}
}
