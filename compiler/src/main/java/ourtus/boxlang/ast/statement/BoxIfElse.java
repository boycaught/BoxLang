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
package ourtus.boxlang.ast.statement;

import ourtus.boxlang.ast.BoxStatement;
import ourtus.boxlang.ast.Position;
import ourtus.boxlang.ast.BoxExpr;

import java.util.ArrayList;
import java.util.List;

public class BoxIfElse extends BoxStatement {

	private final BoxExpr condition;
	private final List<BoxStatement> body;
	private final List<BoxStatement> elseBody;
	public BoxIfElse(BoxExpr condition,Position position, String sourceText) {
		super(position, sourceText);
		this.condition = condition;
		this.body = new ArrayList<>();
		this.elseBody = new ArrayList<>();
	}

	public BoxExpr getCondition() {
		return condition;
	}

	public List<BoxStatement> getBody() {
		return body;
	}

	public List<BoxStatement> getElseBody() {
		return elseBody;
	}
}
