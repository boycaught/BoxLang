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
import ourtus.boxlang.ast.Node;
import ourtus.boxlang.ast.Position;

public class BoxStringLiteral extends BoxExpr {

	private final String value;

	public String getValue() {
		return value;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	/**
	 * Terminal node StringLiteral
	 * @param value
	 * @param position
	 * @param sourceText
	 * Terminal nodes receives the Parent in the constructor
	 */
	public BoxStringLiteral(String value, Position position, String sourceText ) {
		super( position, sourceText );
		StringBuilder sb = new StringBuilder(value);
		sb.deleteCharAt(value.length() - 1);
		sb.deleteCharAt(0);
		this.value =  sb.toString();
		this.parent = parent;
	}
}
