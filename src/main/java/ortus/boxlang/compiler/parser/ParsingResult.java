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
package ortus.boxlang.compiler.parser;

import java.util.List;

import ortus.boxlang.compiler.ast.BoxNode;
import ortus.boxlang.compiler.ast.Issue;

/**
 * The results returned when parsing code.
 */
public class ParsingResult {

	private BoxNode				root;
	private final List<Issue>	issues;

	/**
	 * General constructor.
	 *
	 * @param root   the AST, or null if it wasn't created.
	 * @param issues a list of encountered parsing problems
	 */
	public ParsingResult( BoxNode root, List<Issue> issues ) {
		this.root	= root;
		this.issues	= issues;
	}

	public BoxNode getRoot() {
		return root;
	}

	public List<Issue> getIssues() {
		return issues;
	}

	/**
	 * Returns if parsing was successful
	 *
	 * @return true if no errors of any kind were encountered.
	 */
	public boolean isCorrect() {
		return this.issues.isEmpty();
	}
}
