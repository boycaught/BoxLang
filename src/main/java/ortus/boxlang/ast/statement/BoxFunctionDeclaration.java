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
package ortus.boxlang.ast.statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ortus.boxlang.ast.BoxStatement;
import ortus.boxlang.ast.Position;

/**
 * AST Node representing a function definition
 */
public class BoxFunctionDeclaration extends BoxStatement {

	private final BoxAccessModifier				accessModifier;
	private final String						name;
	private final List<BoxArgumentDeclaration>	args;
	private final BoxReturnType					type;
	private final List<BoxStatement>			body;
	private final List<BoxStatement>			annotations;

	/**
	 * Creates the AST node
	 *
	 * @param accessModifier public,private etc
	 * @param name           name of the function
	 * @param type           return type
	 * @param args           List of arguments
	 * @param body           body of the function
	 * @param position       position of the statement in the source code
	 * @param sourceText     source code that originated the Node
	 *
	 * @see BoxAccessModifier
	 * @see BoxArgumentDeclaration
	 */

	public BoxFunctionDeclaration( BoxAccessModifier accessModifier, String name, BoxReturnType type, List<BoxArgumentDeclaration> args,
	    List<BoxStatement> body, Position position,
	    String sourceText ) {
		super( position, sourceText );
		this.accessModifier	= accessModifier;
		this.name			= name;
		this.type			= type;
		this.type.setParent( this );
		// TODO populate
		this.annotations	= new ArrayList<>();
		this.args			= Collections.unmodifiableList( args );
		this.body			= Collections.unmodifiableList( body );
		this.args.forEach( arg -> arg.setParent( this ) );
		this.body.forEach( stmt -> stmt.setParent( this ) );
	}

	public BoxAccessModifier getAccessModifier() {
		return accessModifier;
	}

	public String getName() {
		return name;
	}

	public BoxReturnType getType() {
		return type;
	}

	public List<BoxArgumentDeclaration> getArgs() {
		return args;
	}

	public List<BoxStatement> getBody() {
		return body;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "accessModifier", accessModifier.toString() );
		map.put( "name", name );
		map.put( "type", type.toMap() );
		map.put( "args", args.stream().map( BoxArgumentDeclaration::toMap ).collect( java.util.stream.Collectors.toList() ) );
		map.put( "body", body.stream().map( BoxStatement::toMap ).collect( java.util.stream.Collectors.toList() ) );
		return map;
	}
}
