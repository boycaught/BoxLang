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
package ortus.boxlang.compiler.ast.statement.component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ortus.boxlang.compiler.ast.BoxStatement;
import ortus.boxlang.compiler.ast.Position;

/**
 * An island of script code within a template
 */
public class BoxTemplateIsland extends BoxStatement {

	private List<BoxStatement> statements;

	/**
	 * Creates an AST for a block of statements
	 *
	 * @param statements list of the statements nodes
	 * @param position   position within the source code
	 * @param sourceText source code
	 *
	 * @see Position
	 * @see BoxStatement
	 */
	public BoxTemplateIsland( List<BoxStatement> statements, Position position, String sourceText ) {
		super( position, sourceText );
		setStatements( statements );
	}

	public List<BoxStatement> getStatements() {
		return statements;
	}

	void setStatements( List<BoxStatement> statements ) {
		replaceChildren( this.statements, statements );
		this.statements = statements;
		this.statements.forEach( arg -> arg.setParent( this ) );
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "statements", statements.stream().map( s -> s.toMap() ).collect( Collectors.toList() ) );
		return map;
	}
}
