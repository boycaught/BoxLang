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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;

import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.statement.BoxForIn;
import ortus.boxlang.transpiler.JavaTranspiler;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;

/**
 * Transform a BoxForIn Node the equivalent Java Parser AST nodes
 */
public class BoxForInTransformer extends AbstractTransformer {

	Logger logger = LoggerFactory.getLogger( BoxForIndexTransformer.class );

	public BoxForInTransformer( JavaTranspiler transpiler ) {
		super( transpiler );
	}

	/**
	 * Transform a collection for statement
	 *
	 * @param node    a BoxForIn instance
	 * @param context transformation context
	 *
	 * @return a Java Parser Block statement with an iterator and a while loop
	 *
	 * @throws IllegalStateException
	 */
	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		BoxForIn			boxFor		= ( BoxForIn ) node;
		Node				variable	= transpiler.transform( boxFor.getVariable() );
		Node				collection	= transpiler.transform( boxFor.getExpression() );

		BlockStmt			stmt		= new BlockStmt();
		Map<String, String>	values		= new HashMap<>() {

											{
												put( "variable", variable.toString() );
												put( "collection", collection.toString() );
												put( "contextName", transpiler.peekContextName() );
											}
										};

		String				template1	= """
		                                  	Iterator ${variable} = CollectionCaster.cast( ${collection} ).iterator();
		                                  """;
		// TODO: This isn't correct and needs reworked. Also, support "for( var x in y )"too
		String				template2	= """
		                                  	while( ${variable}.hasNext() ) {
		                                  		${collection}.put( Key.of( "${variable}" ), ${variable}.next() );
		                                  	}
		                                  """;
		WhileStmt			whileStmt	= ( WhileStmt ) parseStatement( template2, values );
		stmt.addStatement( ( Statement ) parseStatement( template1, values ) );
		boxFor.getBody().forEach( it -> {
			whileStmt.getBody().asBlockStmt().addStatement( ( Statement ) transpiler.transform( it ) );
		} );
		stmt.addStatement( whileStmt );
		logger.info( node.getSourceText() + " -> " + stmt );
		addIndex( stmt, node );
		return stmt;
	}
}
