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
package ortus.boxlang.transpiler.transformer.statement.component;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.UnknownType;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.statement.component.BoxOutput;
import ortus.boxlang.transpiler.JavaTranspiler;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;

public class BoxOutputTransformer extends AbstractTransformer {

	public BoxOutputTransformer( JavaTranspiler transpiler ) {
		super( transpiler );
	}

	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxOutput	boxOutput			= ( BoxOutput ) node;
		BoxExpr		query				= boxOutput.getQuery();
		BoxExpr		group				= boxOutput.getGroup();
		BoxExpr		groupCaseSensitive	= boxOutput.getGroupCaseSensitive();
		BoxExpr		startRow			= boxOutput.getStartRow();
		BoxExpr		maxRows				= boxOutput.getMaxRows();
		// TODO: This needs to be handled in the context somehow
		BoxExpr		encodeFor			= boxOutput.getEncodeFor();

		if ( query != null ) {
			BlockStmt	jBody				= new BlockStmt();
			String		lambdaContextName	= "lambdaContext" + transpiler.incrementAndGetLambdaContextCounter();
			transpiler.pushContextName( lambdaContextName );
			for ( BoxNode statement : boxOutput.getBody() ) {
				jBody.getStatements().add( ( Statement ) transpiler.transform( statement ) );
			}
			transpiler.popContextName();

			LambdaExpr lambda = new LambdaExpr();
			lambda.setParameters( new NodeList<>(
			    new Parameter( new UnknownType(), lambdaContextName ) ) );
			lambda.setBody( jBody );

			Statement jStatement = new ExpressionStmt(
			    new MethodCallExpr(
			        new NameExpr( "QueryOutputUtil" ),
			        "doLoop",
			        new NodeList<>(
			            new NameExpr( transpiler.peekContextName() ),
			            ( Expression ) transpiler.transform( query ),
			            group == null ? new NullLiteralExpr() : ( Expression ) transpiler.transform( group ),
			            groupCaseSensitive == null ? new NullLiteralExpr() : ( Expression ) transpiler.transform( groupCaseSensitive ),
			            startRow == null ? new NullLiteralExpr() : ( Expression ) transpiler.transform( startRow ),
			            maxRows == null ? new NullLiteralExpr() : ( Expression ) transpiler.transform( maxRows ),
			            lambda )
			    )
			);

			addIndex( jStatement, node );
			logger.debug( "{} -> {}", node.getSourceText(), jStatement );
			return jStatement;
		}
		BlockStmt jBody = new BlockStmt();
		for ( BoxNode statement : boxOutput.getBody() ) {
			jBody.getStatements().add( ( Statement ) transpiler.transform( statement ) );
		}
		addIndex( jBody, node );
		logger.debug( "{} -> {}", node.getSourceText(), jBody );
		return jBody;
	}
}
