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
package ortus.boxlang.transpiler.transformer.expression;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.expression.BoxArrayAccess;
import ortus.boxlang.ast.expression.BoxScope;
import ortus.boxlang.ast.expression.BoxStringLiteral;
import ortus.boxlang.transpiler.JavaTranspiler;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Transform a BoxArrayAccess Node the equivalent Java Parser AST nodes
 */
public class BoxArrayAccessTransformer extends AbstractTransformer {

	Logger logger = LoggerFactory.getLogger( BoxArrayAccessTransformer.class );

	public BoxArrayAccessTransformer( JavaTranspiler transpiler ) {
		super( transpiler );
	}

	/**
	 * Transform BoxArrayAccess argument
	 *
	 * @param node    a BoxArrayAccess instance
	 * @param context transformation context
	 *
	 * @return generates a Java Parser Expression accessing the scope
	 *
	 * @throws IllegalStateException
	 *
	 * @see BoxArrayAccess
	 */
	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxArrayAccess	expr	= ( BoxArrayAccess ) node;
		String			side	= context == TransformerContext.NONE ? "" : "(" + context.toString() + ") ";
		logger.info( side + node.getSourceText() );
		/* Case variables['x'] */
		if ( expr.getIndex() instanceof BoxStringLiteral ) {
			Expression			scope		= ( Expression ) transpiler.transform( expr.getContext(), context );
			StringLiteralExpr	variable	= ( StringLiteralExpr ) transpiler.transform( expr.getIndex() );

			Map<String, String>	values		= new HashMap<>() {

												{
													put( "scope", scope.toString() );
													put( "variable", variable.toString() );
												}
											};

			String				template;

			if ( context == TransformerContext.LEFT ) {
				template = """
				            			${scope}.assign(Key.of(${variable}))
				           """;

			} else if ( expr.getContext() instanceof BoxScope ) {
				// template = "context.getScopeNearby( Key.of( \"${scope}\" ) ).get( Key.of( ${variable} ) )";
				template = "${scope}.dereference( Key.of( ${variable}) , false )";

			} else {
				template = """
				           Referencer.get(
				             context.scopeFindNearby( Key.of( "${scope}" ), context.getDefaultAssignmentScope() ).value(),
				             Key.of( ${variable} ),
				             false
				           )
				           """;
			}

			Node javaNode = parseExpression( template, values );
			// logger.info(side + node.getSourceText() + " -> " + javaNode);
			addIndex( javaNode, node );
			return javaNode;
		}
		throw new IllegalStateException( "Not implemented" );
	}
}
