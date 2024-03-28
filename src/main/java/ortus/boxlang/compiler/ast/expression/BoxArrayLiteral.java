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
package ortus.boxlang.compiler.ast.expression;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ortus.boxlang.compiler.ast.BoxExpr;
import ortus.boxlang.compiler.ast.Position;

/**
 * AST Node representing an array literal.
 * An array literal is surrounded by square braces []
 * and contains zero or more comma-delimited expressions.
 * Example
 * 
 * <pre>
 * []
 * [1,2,3]
 * ["foo","bar"]
 * [
 * [1,2],
 * [3,4],
 * "brad"
 * ]
 * </pre>
 */
public class BoxArrayLiteral extends BoxExpr {

	private final List<BoxExpr> values;

	/**
	 * Creates the AST node for an anonymous argument
	 * 
	 * @param values     initialization values
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 */
	public BoxArrayLiteral( List<BoxExpr> values, Position position, String sourceText ) {
		super( position, sourceText );
		this.values = Collections.unmodifiableList( values );
		this.values.forEach( arg -> arg.setParent( this ) );
	}

	public List<BoxExpr> getValues() {
		return values;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "values", values.stream().map( BoxExpr::toMap ).collect( Collectors.toList() ) );
		return map;
	}
}
