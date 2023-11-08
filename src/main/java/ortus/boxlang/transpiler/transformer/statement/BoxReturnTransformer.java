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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.statement.BoxReturn;
import ortus.boxlang.transpiler.JavaTranspiler;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Transform a Return Statement in the equivalent Java Parser AST nodes
 */
public class BoxReturnTransformer extends AbstractTransformer {

	Logger logger = LoggerFactory.getLogger( BoxFunctionDeclarationTransformer.class );

	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxReturn			boxReturn	= ( BoxReturn ) node;

		String				template	= "return;";
		Map<String, String>	values		= new HashMap<>();
		if ( boxReturn.getExpression() != null ) {
			Expression expr = ( Expression ) JavaTranspiler.transform( boxReturn.getExpression(), TransformerContext.RIGHT );
			values.put( "expr", expr.toString() );
			template = "return ${expr};";
		}

		Node javaStmt = parseStatement( template, values );
		logger.info( node.getSourceText() + " -> " + javaStmt );
		addIndex( javaStmt, node );
		return javaStmt;
	}
}
