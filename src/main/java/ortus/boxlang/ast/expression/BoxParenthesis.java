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

import java.util.Map;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.Position;

/**
 * AST Node representing and expression within parenthesis
 */
public class BoxParenthesis extends BoxExpr {

	private final BoxExpr expression;

	public BoxExpr getExpression() {
		return expression;
	}

	/**
	 * Creates the AST node
	 *
	 * @param expression expression within the parenthesis
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 */
	public BoxParenthesis( BoxExpr expression, Position position, String sourceText ) {
		super( position, sourceText );
		this.expression = expression;
		this.expression.setParent( this );
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "expression", expression.toMap() );
		return map;
	}
}
