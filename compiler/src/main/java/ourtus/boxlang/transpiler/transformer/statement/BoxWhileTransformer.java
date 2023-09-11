package ourtus.boxlang.transpiler.transformer.statement;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ourtus.boxlang.ast.BoxNode;
import ourtus.boxlang.ast.BoxStatement;
import ourtus.boxlang.ast.statement.BoxIfElse;
import ourtus.boxlang.ast.statement.BoxWhile;
import ourtus.boxlang.transpiler.BoxLangTranspiler;
import ourtus.boxlang.transpiler.transformer.AbstractTransformer;
import ourtus.boxlang.transpiler.transformer.TransformerContext;
import ourtus.boxlang.transpiler.transformer.expression.BoxParenthesisTransformer;

import java.util.HashMap;
import java.util.Map;

public class BoxWhileTransformer extends AbstractTransformer {
	Logger logger = LoggerFactory.getLogger( BoxParenthesisTransformer.class );
	@Override
	public Node transform(BoxNode node, TransformerContext context) throws IllegalStateException {
		BoxWhile boxWhile = (BoxWhile) node;
		Expression condition =  (Expression) BoxLangTranspiler.transform(boxWhile.getCondition(),TransformerContext.RIGHT);

		String template = "while(  ${condition}  ) {}";
		if(requiresBooleanCaster(boxWhile.getCondition())) {
			template = "while( BooleanCaster.cast( ${condition} ) ) {}";
		}
		Map<String, String> values = new HashMap<>() {{
			put("condition", condition.toString());
		}};
		WhileStmt javaWhile = (WhileStmt) parseStatement(template,values);
		BlockStmt body = new BlockStmt();
		for(BoxStatement statement : boxWhile.getBody()) {
			body.getStatements().add((Statement) BoxLangTranspiler.transform(statement));
		}
		javaWhile.setBody(body);
		return javaWhile;
	}
}
