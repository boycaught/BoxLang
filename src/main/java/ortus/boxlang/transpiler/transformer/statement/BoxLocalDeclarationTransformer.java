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
package ortus.boxlang.transpiler.transformer.statement;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.expression.BoxIdentifier;
import ortus.boxlang.ast.statement.BoxLocalDeclaration;
import ortus.boxlang.transpiler.JavaTranspiler;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;

import java.util.HashMap;
import java.util.Map;

public class BoxLocalDeclarationTransformer extends AbstractTransformer {

	Logger logger = LoggerFactory.getLogger( BoxLocalDeclarationTransformer.class );

	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxLocalDeclaration	declaration	= ( BoxLocalDeclaration ) node;
		BlockStmt			stmt		= new BlockStmt();
		if ( declaration.getExpression() != null ) {
			Expression expr = ( Expression ) JavaTranspiler.transform( declaration.getExpression(), TransformerContext.RIGHT );
			;
			declaration.getIdentifiers().stream().forEach( it -> {
				BoxIdentifier		variable	= ( BoxIdentifier ) it;
				Map<String, String>	values		= new HashMap<>() {

													{
														put( "variable", variable.getName() );
														put( "expr", expr.toString() );
													}
												};

				String				template	= """
				                                  context.getScopeNearby( LocalScope.name )
				                                  	.assign(Key.of( "${variable}" ),
				                                  	${expr});
				                                  """;

				Node				javaStmt	= parseStatement( template, values );
				logger.info( node.getSourceText() + " -> " + javaStmt );
				stmt.getStatements().add( ( Statement ) javaStmt );

			} );

		}

		return stmt;
	}
}
