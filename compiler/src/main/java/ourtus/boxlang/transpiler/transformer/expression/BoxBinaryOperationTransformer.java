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
package ourtus.boxlang.transpiler.transformer.expression;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ourtus.boxlang.ast.BoxNode;
import ourtus.boxlang.ast.expression.BoxBinaryOperation;
import ourtus.boxlang.ast.expression.BoxBinaryOperator;
import ourtus.boxlang.transpiler.BoxLangTranspiler;
import ourtus.boxlang.transpiler.transformer.AbstractTransformer;
import ourtus.boxlang.transpiler.transformer.BoxAssignmentTransformer;
import ourtus.boxlang.transpiler.transformer.TransformerContext;

import java.util.HashMap;
import java.util.Map;

public class BoxBinaryOperationTransformer extends AbstractTransformer {
	Logger logger = LoggerFactory.getLogger( BoxBinaryOperationTransformer.class );
	@Override
	public Node transform(BoxNode node, TransformerContext context) throws IllegalStateException {
		logger.info(node.getSourceText() );
		BoxBinaryOperation operation = (BoxBinaryOperation)node;
		Expression left = (Expression) resolveScope(BoxLangTranspiler.transform(operation.getLeft()));
		Expression right = (Expression) resolveScope(BoxLangTranspiler.transform(operation.getRight()));

		Map<String, String> values = new HashMap<>() {{
			put("left", left.toString());
			put("right", right.toString());

		}};

		String template = "";
		if (operation.getOperator() == BoxBinaryOperator.Concat) {
			template = "Concat.invoke(${left},${right})";
		} else if (operation.getOperator() == BoxBinaryOperator.Plus) {
			template = "Plus.invoke(${left},${right})";
		} else if (operation.getOperator() == BoxBinaryOperator.Minus) {
			template = "Minus.invoke(${left},${right})";
		} else if (operation.getOperator() == BoxBinaryOperator.Star) {
			template = "Multiply.invoke(${left},${right})";
		} else if (operation.getOperator() == BoxBinaryOperator.Slash) {
			template = "Divide.invoke(${left},${right})";
		} else if (operation.getOperator() == BoxBinaryOperator.Contains) {
			template = "Contains.contains(${left},${right})";
		}

		return parseExpression(template,values);
	}

	private Node resolveScope(Node expr) {
		if(expr instanceof NameExpr) {
			String id = expr.toString();
			String template = "context.scopeFindNearby(Key.of(\"${id}\"))";
			Map<String, String> values = new HashMap<>() {{
				put("id", id.toString());
			}};
			return  parseExpression(template,values);

		}
		return expr;
	}
}
