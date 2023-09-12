package ourtus.boxlang.transpiler.transformer.expression;

import com.github.javaparser.ast.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ourtus.boxlang.ast.BoxNode;
import ourtus.boxlang.ast.expression.BoxScope;
import ourtus.boxlang.transpiler.transformer.AbstractTransformer;
import ourtus.boxlang.transpiler.transformer.TransformerContext;

import java.util.HashMap;
import java.util.Map;

public class BoxScopeTransformer extends AbstractTransformer {

	Logger logger = LoggerFactory.getLogger( BoxScopeTransformer.class );

	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxScope			scope		= ( BoxScope ) node;
		String				side		= context == TransformerContext.NONE ? "" : "(" + context.toString() + ") ";
		Map<String, String>	values		= new HashMap<>() {

											{
												put( "scope", scope.getName() );
											}
										};
		String				template	= "";
		if ( context == TransformerContext.LEFT ) {
			template = "${scope}Scope";
		} else {
			template = "${scope}Scope";
		}

		Node javaExpr = parseExpression( template, values );
		logger.info( side + node.getSourceText() + " -> " + javaExpr );
		return javaExpr;
	}
}
