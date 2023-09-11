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
package ourtus.boxlang.transpiler.transformer.statement;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ourtus.boxlang.ast.BoxNode;
import ourtus.boxlang.ast.BoxStatement;
import ourtus.boxlang.ast.statement.BoxIfElse;
import ourtus.boxlang.transpiler.BoxLangTranspiler;
import ourtus.boxlang.transpiler.transformer.AbstractTransformer;
import ourtus.boxlang.transpiler.transformer.TransformerContext;
import ourtus.boxlang.transpiler.transformer.expression.BoxParenthesisTransformer;

import java.util.HashMap;
import java.util.Map;

public class BoxIfElseTransformer extends AbstractTransformer {
	Logger logger = LoggerFactory.getLogger( BoxParenthesisTransformer.class );
	@Override
	public Node transform(BoxNode node, TransformerContext context) throws IllegalStateException {
		BoxIfElse ifElse = (BoxIfElse) node;
		Expression condition =  (Expression) BoxLangTranspiler.transform(ifElse.getCondition(),TransformerContext.RIGHT);

		String template = "if(  ${condition}  ) {}";
		if(requiresBooleanCaster(ifElse.getCondition())) {
			template = "if( BooleanCaster.cast( ${condition} ) ) {}";
		}
		Map<String, String> values = new HashMap<>() {{
			put("condition", condition.toString());
		}};

		IfStmt javaIfStmt = (IfStmt) parseStatement(template,values);
		BlockStmt thenBlock = new BlockStmt();
		BlockStmt elseBlock = new BlockStmt();
		for(BoxStatement statement : ifElse.getThenBody()) {
			thenBlock.getStatements().add((Statement) BoxLangTranspiler.transform(statement));
		}
		for(BoxStatement statement : ifElse.getElseBody()) {
			elseBlock.getStatements().add((Statement) BoxLangTranspiler.transform(statement));
		}

		javaIfStmt.setThenStmt(thenBlock);
		if(elseBlock.getStatements().isNonEmpty() ) {
			if(elseBlock.getStatements().size() > 1)
				javaIfStmt.setElseStmt(elseBlock);
			else
				javaIfStmt.setElseStmt(elseBlock.getStatement(0));
		}
		return javaIfStmt;

	}

}
