package ortus.boxlang.transpiler.transformer.expression;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NameExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.expression.BoxFQN;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;
import ortus.boxlang.transpiler.transformer.statement.BoxThrowTransformer;

public class BoxFQNTransformer extends AbstractTransformer {

	Logger logger = LoggerFactory.getLogger( BoxThrowTransformer.class );

	/**
	 * Transform a fully qualified name
	 *
	 * @param node    a BoxFQN instance
	 * @param context transformation context
	 *
	 * @return Generates a throw
	 *
	 * @throws IllegalStateException
	 */
	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxFQN boxFQN = ( BoxFQN ) node;
		return new NameExpr( boxFQN.getValue() );
	}
}