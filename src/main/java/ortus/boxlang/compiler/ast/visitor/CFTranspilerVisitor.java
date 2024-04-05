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
package ortus.boxlang.compiler.ast.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ortus.boxlang.compiler.ast.BoxBufferOutput;
import ortus.boxlang.compiler.ast.BoxClass;
import ortus.boxlang.compiler.ast.BoxExpression;
import ortus.boxlang.compiler.ast.BoxNode;
import ortus.boxlang.compiler.ast.expression.BoxAccess;
import ortus.boxlang.compiler.ast.expression.BoxArgument;
import ortus.boxlang.compiler.ast.expression.BoxArrayAccess;
import ortus.boxlang.compiler.ast.expression.BoxBooleanLiteral;
import ortus.boxlang.compiler.ast.expression.BoxDotAccess;
import ortus.boxlang.compiler.ast.expression.BoxFQN;
import ortus.boxlang.compiler.ast.expression.BoxFunctionInvocation;
import ortus.boxlang.compiler.ast.expression.BoxIdentifier;
import ortus.boxlang.compiler.ast.expression.BoxStringLiteral;
import ortus.boxlang.compiler.ast.expression.BoxUnaryOperation;
import ortus.boxlang.compiler.ast.expression.BoxUnaryOperator;
import ortus.boxlang.compiler.ast.statement.BoxAnnotation;
import ortus.boxlang.compiler.ast.statement.BoxDocumentationAnnotation;
import ortus.boxlang.compiler.ast.statement.BoxFunctionDeclaration;
import ortus.boxlang.compiler.ast.statement.BoxProperty;
import ortus.boxlang.compiler.ast.statement.component.BoxComponent;

/**
 * Pretty print BoxLang AST nodes
 */
public class CFTranspilerVisitor extends ReplacingBoxVisitor {

	private static Map<String, String>	BIFMap			= new HashMap<String, String>();
	private static Map<String, String>	identifierMap	= new HashMap<String, String>();
	private boolean						isClass			= false;

	static {
		// ENSURE ALL KEYS ARE LOWERCASE FOR EASIER MATCHING
		BIFMap.put( "serializejson", "JSONSerialize" );
		BIFMap.put( "deserializejson", "JSONDeserialize" );
		BIFMap.put( "chr", "char" );
		BIFMap.put( "asc", "ascii" );

		identifierMap.put( "cfthread", "bxthread" );
		identifierMap.put( "cfcatch", "bxcatch" );
		identifierMap.put( "cffile", "bxfile" );
		identifierMap.put( "cfftp", "bxftp" );
	}

	public CFTranspilerVisitor() {
	}

	/**
	 * Transpile Box Classes
	 * - Merge documentation into annotations
	 * - enable output
	 */
	public BoxNode visit( BoxClass node ) {
		isClass = true;
		mergeDocsIntoAnnotations( node.getAnnotations(), node.getDocumentation() );
		enableOutput( node.getAnnotations() );
		return super.visit( node );
	}

	/**
	 * Transpile UDF declarations
	 * - Merge documentation into annotations
	 * - enable output
	 */
	public BoxNode visit( BoxFunctionDeclaration node ) {
		mergeDocsIntoAnnotations( node.getAnnotations(), node.getDocumentation() );
		enableOutput( node.getAnnotations() );
		return super.visit( node );
	}

	/**
	 * Transpile Box Class properties
	 * - Merge documentation into annotations
	 */
	public BoxNode visit( BoxProperty node ) {
		mergeDocsIntoAnnotations( node.getAnnotations(), node.getDocumentation() );
		return super.visit( node );
	}

	/**
	 * Rename top level CF variables
	 */
	public BoxNode visit( BoxArrayAccess node ) {
		renameTopLevelVars( node );
		return super.visit( node );
	}

	/**
	 * Rename top level CF variables
	 */
	public BoxNode visit( BoxDotAccess node ) {
		renameTopLevelVars( node );
		return super.visit( node );
	}

	/**
	 * Rename some common CF built-in functions like chr() to char()
	 * 
	 * Replace
	 * structKeyExists( struct, key )
	 * with
	 * !isNull( struct[ key ] )
	 */
	public BoxNode visit( BoxFunctionInvocation node ) {
		String name = node.getName().toLowerCase();
		if ( BIFMap.containsKey( name ) ) {
			node.setName( BIFMap.get( name ) );
		}

		if ( name.equalsIgnoreCase( "structKeyExists" ) && node.getArguments().size() == 2 ) {
			BoxUnaryOperation newNode = new BoxUnaryOperation(
			    new BoxFunctionInvocation(
			        "isNull",
			        List.of(
			            new BoxArgument(
			                new BoxArrayAccess(
			                    node.getArguments().get( 0 ).getValue(),
			                    true,
			                    node.getArguments().get( 1 ).getValue(),
			                    null,
			                    null ),
			                null,
			                null
			            )
			        ),
			        null,
			        null
			    ),
			    BoxUnaryOperator.Not,
			    null,
			    null
			);
			return super.visit( newNode );
		}
		return super.visit( node );
	}

	/**
	 * Rename enablecfoutputonly attribute on cfsetting tag
	 */
	public BoxNode visit( BoxComponent node ) {
		// if we get more of these, create a config map above
		if ( node.getName().equalsIgnoreCase( "setting" ) ) {
			var attrs = node.getAttributes();
			// If there is one called enablecfoutputonly, rename to enableoutputonly. Loop over and modify in place
			for ( BoxAnnotation attr : attrs ) {
				if ( attr.getKey().getValue().equalsIgnoreCase( "enablecfoutputonly" ) ) {
					attr.getKey().setValue( "enableoutputonly" );
				}
			}

		}
		return super.visit( node );
	}

	/**
	 * CF reads documentation comment lines such as
	 * 
	 * @foo bar
	 *      as an actual "annotation" for classes and functions and properties.
	 *      We'll need to merge these in manually as BL keeps them separate.
	 * 
	 * @param annotations   The annotations for the node
	 * @param documentation The documentation for the node
	 * 
	 */
	private void mergeDocsIntoAnnotations( List<BoxAnnotation> annotations, List<BoxDocumentationAnnotation> documentation ) {
		Set<String> existingAnnotations = annotations.stream().map( BoxAnnotation::getKey ).map( BoxFQN::getValue ).map( k -> k.toLowerCase() )
		    .collect( Collectors.toSet() );
		for ( BoxDocumentationAnnotation doc : documentation ) {
			// Don't override existing annotations, and don't copy hint
			if ( !doc.getKey().getValue().equalsIgnoreCase( "hint" ) && !existingAnnotations.contains( doc.getKey().getValue().toLowerCase() ) ) {
				annotations.add(
				    new BoxAnnotation(
				        new BoxFQN( doc.getKey().getValue(), null, null ),
				        doc.getValue(),
				        null,
				        null
				    )
				);
			}
		}
	}

	/**
	 * Remove empty output nodes from script (because in BoxLang, classes are only script, so the original CF may have been tags)
	 */
	public BoxNode visit( BoxBufferOutput node ) {
		if ( isClass ) {
			BoxExpression expr = node.getExpression();
			// only contains white space
			if ( expr instanceof BoxStringLiteral str && str.getValue().trim().isEmpty() ) {
				return null;
			}
		}
		return super.visit( node );
	}

	/**
	 * Add output annotation and set to true if it doesn't exist
	 * 
	 * @param annotations The annotations for the node
	 */
	private void enableOutput( List<BoxAnnotation> annotations ) {
		if ( !annotations.stream().anyMatch( a -> a.getKey().getValue().equalsIgnoreCase( "output" ) ) ) {
			// @output true
			annotations.add(
			    new BoxAnnotation(
			        new BoxFQN( "output", null, null ),
			        new BoxBooleanLiteral( true, null, null ),
			        null,
			        null )
			);
		}
	}

	/**
	 * Rename some common CF variables like cfthread.foo to bxthread.foo
	 * or cfthread[ "foo" ] to bxthread[ "foo" ]
	 * 
	 * @param boxAccess The access node to rename
	 */
	private void renameTopLevelVars( BoxAccess boxAccess ) {
		if ( boxAccess.getContext() instanceof BoxIdentifier id ) {
			String name = id.getName().toLowerCase();
			if ( BIFMap.containsKey( name ) ) {
				id.setName( BIFMap.get( name ) );
			}
		}
	}

}
