package ortus.boxlang.transpiler.transformer.expression;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.expression.BoxAssignmentExpression;
import ortus.boxlang.transpiler.JavaTranspiler;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.Transformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;

import java.util.HashMap;
import java.util.Map;

public class BoxAssignmentExpressionTransformer extends AbstractTransformer {

	Logger logger = LoggerFactory.getLogger( BoxAssignmentExpressionTransformer.class );

	public BoxAssignmentExpressionTransformer( JavaTranspiler transpiler ) {
		super( transpiler );
	}

	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxAssignmentExpression	assignment	= ( BoxAssignmentExpression ) node;

		Expression				left		= ( Expression ) transpiler.transform( assignment.getLeft(), TransformerContext.LEFT );
		Expression				right		= ( Expression ) transpiler.transform( assignment.getRight() );

		if ( left instanceof MethodCallExpr javaExpr && javaExpr.getName().asString().equalsIgnoreCase( "assign" ) ) {
			javaExpr.getArguments().add( right );
			logger.info( "{} -> {}", node.getSourceText(), javaExpr );
			return javaExpr;
		}

		if ( left instanceof NameExpr ) {
			Map<String, String>	values		= Map.of( "id", left.toString() );
			String				template	= """
			                                  context.scopeFindNearby(
			                                  	Key.of( ${id} ),
			                                  	context.getDefaultAssignmentScope()).scope().assign()
			                                  """;

			MethodCallExpr		javaExpr	= ( MethodCallExpr ) parseExpression( template, values );
			javaExpr.getArguments().add( right );
			logger.info( "{} -> {}", node.getSourceText(), javaExpr );
			return javaExpr;

		}
		return left;
	}
}
