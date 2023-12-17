package ortus.boxlang.transpiler.transformer.statement;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;

import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.statement.BoxThrow;
import ortus.boxlang.transpiler.JavaTranspiler;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;

public class BoxThrowTransformer extends AbstractTransformer {

	public BoxThrowTransformer( JavaTranspiler transpiler ) {
		super( transpiler );
	}

	/**
	 * Transform a throw statement
	 *
	 * @param node    a BoxThrow instance
	 * @param context transformation context
	 *
	 * @return Generates a throw
	 *
	 * @throws IllegalStateException
	 */
	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxThrow			boxAssert	= ( BoxThrow ) node;
		Expression			expr		= ( Expression ) transpiler.transform( boxAssert.getExpression(), TransformerContext.RIGHT );
		Map<String, String>	values		= new HashMap<>() {

											{
												put( "expr", expr.toString() );
												put( "contextName", transpiler.peekContextName() );

											}
										};
		String				template	= "ExceptionUtil.throwException(${expr});";
		Node				javaStmt	= parseStatement( template, values );
		logger.info( node.getSourceText() + " -> " + javaStmt );
		addIndex( javaStmt, node );
		return javaStmt;

	}
}
