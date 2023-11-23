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
 * AST Node representing a unary operator
 */
public class BoxUnaryOperation extends BoxExpr {

	private final BoxExpr			expr;
	private final BoxUnaryOperator	operator;

	/**
	 *
	 * @param expr       expression
	 * @param operator   operator to apply
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 */
	public BoxUnaryOperation( BoxExpr expr, BoxUnaryOperator operator, Position position, String sourceText ) {
		super( position, sourceText );
		this.expr = expr;
		this.expr.setParent( this );
		this.operator = operator;
	}

	public BoxExpr getExpr() {
		return expr;
	}

	public BoxUnaryOperator getOperator() {
		return operator;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "expr", expr.toMap() );
		map.put( "operator", operator.toString() );
		return map;
	}

}
