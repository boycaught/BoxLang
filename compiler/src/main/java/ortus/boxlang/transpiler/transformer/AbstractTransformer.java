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
package ortus.boxlang.transpiler.transformer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.commons.text.StringSubstitutor;
import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.expression.BoxBinaryOperation;
import ortus.boxlang.ast.expression.BoxBinaryOperator;
import ortus.boxlang.ast.expression.BoxNegateOperation;
import ortus.boxlang.transpiler.transformer.indexer.BoxLangCrossReferencer;
import ortus.boxlang.transpiler.transformer.indexer.BoxLangCrossReferencerDefault;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Transformer class
 * Implements common functionality used by all the transformer sub classes
 */
public abstract class AbstractTransformer implements Transformer {

	protected static JavaParser				javaParser		= new JavaParser();
	protected static BoxLangCrossReferencer	crossReferencer	= new BoxLangCrossReferencerDefault();

	@Override
	public abstract Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException;

	public Node transform( BoxNode node ) throws IllegalStateException {
		return this.transform( node, TransformerContext.NONE );
	}

	/**
	 * Returns the Java Parser AST nodes for the given template
	 *
	 * @param template a string template with the expression to parse
	 * @param values   a map of values to be replaced in the template
	 *
	 * @return the Java Parser AST representation of the expression
	 */
	protected Node parseExpression( String template, Map<String, String> values ) {
		StringSubstitutor		sub		= new StringSubstitutor( values );
		String					code	= sub.replace( template );
		ParseResult<Expression>	result	= javaParser.parseExpression( code );
		if ( !result.isSuccessful() ) {
			System.out.println( code );
			throw new IllegalStateException( result.toString() );
		}
		return result.getResult().get();
	}

	/**
	 * Returns the Java Parser AST for the given template
	 *
	 * @param template a string template with the statement to parse
	 * @param values   a map of values to be replaced in the template
	 *
	 * @return the Java Parser AST representation of the statement
	 */
	protected Node parseStatement( String template, Map<String, String> values ) {
		StringSubstitutor		sub		= new StringSubstitutor( values );
		String					code	= sub.replace( template );
		ParseResult<Statement>	result	= javaParser.parseStatement( code );
		if ( !result.isSuccessful() ) {
			throw new IllegalStateException( result.toString() );
		}
		return result.getResult().get();
	}

	/**
	 * Returns the appropriate template code to access the scope
	 *
	 * @param expr    expression to be solved
	 * @param context transformation context LEFT or RIGHT indicating the side of the expression
	 *
	 * @return
	 */
	protected Node resolveScope( Node expr, TransformerContext context ) {
		if ( expr instanceof NameExpr ) {
			String				id			= expr.toString();
			String				template	= switch ( context ) {
												case RIGHT -> "context.scopeFindNearby(Key.of(\"${id}\")).value()";
												default -> "context.scopeFindNearby(Key.of(\"${id}\"))";
											}

			;
			Map<String, String>	values		= new HashMap<>() {

												{
													put( "id", id.toString() );
												}
											};
			return parseExpression( template, values );

		}
		return expr;
	}

	/**
	 * Detects if a statement requires a BooleanCaster
	 *
	 * @param condition the expression to evaluate
	 *
	 * @return true if the BooleanCaster is required
	 */
	protected boolean requiresBooleanCaster( BoxExpr condition ) {
		if ( condition instanceof BoxBinaryOperation ) {
			BoxBinaryOperation op = ( BoxBinaryOperation ) condition;
			if ( op.getOperator() == BoxBinaryOperator.Or )
				return false;
			if ( op.getOperator() == BoxBinaryOperator.And )
				return false;
		}
		if ( condition instanceof BoxNegateOperation ) {
			return false;
		}
		return true;
	}

	/**
	 * Add cross-reference index entry
	 *
	 * @param javaNode Java Parser Node
	 * @param boxNode  BoxLang Node
	 *
	 * @return the Java Parser Node
	 */
	protected Node addIndex( Node javaNode, BoxNode boxNode ) {
		if ( crossReferencer != null ) {
			crossReferencer.storeReference( javaNode, boxNode );
		}
		return javaNode;
	}

}
