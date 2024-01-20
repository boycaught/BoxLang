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
package ortus.boxlang.ast.statement.tag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.BoxStatement;
import ortus.boxlang.ast.Position;

/**
 * Root node for a tag/templating (program) cfm/bxm
 */
public class BoxOutput extends BoxStatement {

	private final List<BoxStatement> body;

	/**
	 * Creates an AST for a program which is represented by a list of statements
	 *
	 * @param statements list of the statements nodes
	 * @param position   position within the source code
	 * @param sourceText source code
	 *
	 * @see Position
	 * @see BoxStatement
	 */
	public BoxOutput( List<BoxStatement> statements, Position position, String sourceText ) {
		super( position, sourceText );
		this.body = statements;
		this.body.forEach( statement -> statement.setParent( this ) );
	}

	public List<BoxStatement> getBody() {
		return body;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "body", body.stream().map( BoxNode::toMap ).collect( Collectors.toList() ) );
		return map;
	}

}
