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
package ortus.boxlang.compiler.ast.expression;

import java.util.Map;

import ortus.boxlang.compiler.ast.BoxExpression;
import ortus.boxlang.compiler.ast.Position;
import ortus.boxlang.compiler.ast.visitor.VoidBoxVisitor;

/**
 * AST Node representing a Ternary operator
 */
public class BoxTernaryOperation extends BoxExpression {

	private BoxExpression	condition;
	private BoxExpression	whenTrue;
	private BoxExpression	whenFalse;

	/**
	 * Creates the AST node
	 *
	 * @param condition  expression to evaluate
	 * @param whenTrue   executed when the condition is true
	 * @param whenFalse  executed when the condition is false
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 */
	public BoxTernaryOperation( BoxExpression condition, BoxExpression whenTrue, BoxExpression whenFalse, Position position, String sourceText ) {
		super( position, sourceText );
		setCondition( condition );
		setWhenTrue( whenTrue );
		setWhenFalse( whenFalse );
	}

	public BoxExpression getCondition() {
		return condition;
	}

	public BoxExpression getWhenTrue() {
		return whenTrue;
	}

	public BoxExpression getWhenFalse() {
		return whenFalse;
	}

	void setCondition( BoxExpression condition ) {
		replaceChildren( this.condition, condition );
		this.condition = condition;
		this.condition.setParent( this );
	}

	void setWhenTrue( BoxExpression whenTrue ) {
		replaceChildren( this.whenTrue, whenTrue );
		this.whenTrue = whenTrue;
		this.whenTrue.setParent( this );
	}

	void setWhenFalse( BoxExpression whenFalse ) {
		replaceChildren( this.whenFalse, whenFalse );
		this.whenFalse = whenFalse;
		this.whenFalse.setParent( this );
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "condition", condition.toMap() );
		map.put( "whenTrue", whenTrue.toMap() );
		map.put( "whenFalse", whenFalse.toMap() );
		return map;
	}

	public void accept( VoidBoxVisitor v ) {
		v.visit( this );
	}
}
