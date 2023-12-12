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

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.Statement;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.expression.BoxBinaryOperation;
import ortus.boxlang.ast.expression.BoxBinaryOperator;
import ortus.boxlang.ast.expression.BoxComparisonOperation;
import ortus.boxlang.ast.expression.BoxStringLiteral;
import ortus.boxlang.ast.expression.BoxUnaryOperation;
import ortus.boxlang.ast.expression.BoxUnaryOperator;
import ortus.boxlang.runtime.config.util.PlaceholderHelper;
import ortus.boxlang.runtime.types.exceptions.ApplicationException;
import ortus.boxlang.transpiler.Transpiler;
import ortus.boxlang.transpiler.transformer.indexer.BoxLangCrossReferencer;
import ortus.boxlang.transpiler.transformer.indexer.BoxLangCrossReferencerDefault;

/**
 * Abstract Transformer class
 * Implements common functionality used by all the transformer sub classes
 */
public abstract class AbstractTransformer implements Transformer {

	protected Transpiler					transpiler;
	protected static JavaParser				javaParser		= new JavaParser();
	protected static BoxLangCrossReferencer	crossReferencer	= new BoxLangCrossReferencerDefault();

	public AbstractTransformer( Transpiler transpiler ) {
		this.transpiler = transpiler;
	}

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
		String code = PlaceholderHelper.resolve( template, values );
		try {
			ParseResult<Expression> result = javaParser.parseExpression( code );
			if ( !result.isSuccessful() ) {
				// System.out.println( code );
				throw new IllegalStateException( result.toString() );
			}
			return result.getResult().get();
		} catch ( Throwable e ) {
			throw new RuntimeException( "Error parsing expression: " + code, e );
		}

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
		String					code	= PlaceholderHelper.resolve( template, values );
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
			String	id	= expr.toString();
			String	template;
			if ( transpiler.matchesImport( id ) ) {
				template = switch ( context ) {
					case INIT -> throw new ApplicationException( "You cannot assign a variable with the same name as an import: [" + id + "]" );
					case RIGHT -> "classLocator.load( ${contextName}, \"${id}\", imports )";
					default -> "classLocator.load( ${contextName}, \"${id}\", imports )";
				};
			} else {
				template = switch ( context ) {
					case INIT -> "${contextName}.scopeFindNearby(Key.of(\"${id}\"), ${contextName}.getDefaultAssignmentScope()).scope().assign(Key.of(\"${id}\"))";
					case SAFE -> "${contextName}.scopeFindNearby(Key.of(\"${id}\"), ${contextName}.getDefaultAssignmentScope()).value()";
					case RIGHT -> "${contextName}.scopeFindNearby(Key.of(\"${id}\"),null).value()";
					default -> "${contextName}.scopeFindNearby(Key.of(\"${id}\"),null).value()";
				};
			}

			Map<String, String> values = new HashMap<>() {

				{
					put( "id", id.toString() );
					put( "contextName", transpiler.peekContextName() );
				}
			};
			return parseExpression( template, values );

		}
		return expr;
	}

	/**
	 * Create a Key instance out of any expression. May optimize requests for the same key more than once in a template
	 *
	 * @param expr expression to be turned into a Key. May be a literal, or expression
	 *
	 * @return
	 */
	protected Node createKey( BoxExpr expr ) {
		// TODO: optimize for the same key more than once in a template
		NameExpr		nameExpr		= new NameExpr( "Key" );
		MethodCallExpr	methodCallExpr	= new MethodCallExpr( nameExpr, "of" );
		methodCallExpr.addArgument( ( Expression ) transpiler.transform( expr ) );

		return methodCallExpr;
	}

	/**
	 * Create a Key instance from a string literal. May optimize requests for the same key more than once in a template
	 *
	 * @param expr expression to be turned into a Key. May be a literal, or expression
	 *
	 * @return
	 */
	protected Node createKey( String expr ) {
		return createKey( new BoxStringLiteral( expr, null, expr ) );
	}

	/**
	 * Detects if a statement requires a BooleanCaster
	 *
	 * @param condition the expression to evaluate
	 *
	 * @return true if the BooleanCaster is required
	 */
	protected boolean requiresBooleanCaster( BoxExpr condition ) {
		if ( condition instanceof BoxBinaryOperation op ) {
			if ( op.getOperator() == BoxBinaryOperator.Or )
				return false;
			if ( op.getOperator() == BoxBinaryOperator.And )
				return false;
			if ( op.getOperator() == BoxBinaryOperator.Contains )
				return false;
			if ( op.getOperator() == BoxBinaryOperator.InstanceOf )
				return false;
			if ( op.getOperator() == BoxBinaryOperator.NotContains )
				return false;
			if ( op.getOperator() == BoxBinaryOperator.Xor )
				return false;
		}
		if ( condition instanceof BoxUnaryOperation op ) {
			if ( op.getOperator() == BoxUnaryOperator.Not )
				return false;
		}
		if ( condition instanceof BoxComparisonOperation op ) {
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
