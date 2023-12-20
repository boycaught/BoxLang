/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.compiler;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import ortus.boxlang.ast.BoxScript;
import ortus.boxlang.ast.expression.BoxClosure;
import ortus.boxlang.ast.statement.BoxArgumentDeclaration;
import ortus.boxlang.ast.statement.BoxExpression;
import ortus.boxlang.parser.BoxCFParser;
import ortus.boxlang.parser.BoxParser;
import ortus.boxlang.parser.ParsingResult;
import ortus.boxlang.transpiler.JavaTranspiler;

public class TestClosure extends TestBase {

	private Node transformClosure( String expression ) throws IOException {
		BoxParser		parser	= new BoxParser();
		ParsingResult	result	= parser.parseExpression( expression );
		assertTrue( result.isCorrect() );

		JavaTranspiler transpiler = new JavaTranspiler();
		transpiler.setProperty( "packageName", "ortus.test" );
		transpiler.setProperty( "classname", "MyClosure" );
		transpiler.pushContextName( "context" );
		transpiler.transform( result.getRoot() );
		return transpiler.getCallables().get( 0 );
	}

	@Test
	public void testClosureParameters() throws IOException {
		String			code	= """
		                          ( required string param1='default' key='value' ) key='value' => { return param1 }
		                           """;
		BoxCFParser		parser	= new BoxCFParser();
		ParsingResult	result	= parser.parse( code );
		assertTrue( result.isCorrect() );
		BoxScript script = ( BoxScript ) result.getRoot();
		script.getStatements().forEach( stmt -> {
			stmt.walk().forEach( it -> {
				if ( it instanceof BoxExpression exp && exp.getExpression() instanceof BoxClosure closure ) {
					Assertions.assertEquals( 1, closure.getArgs().size() );
					Assertions.assertEquals( 1, closure.getAnnotations().size() );

					BoxArgumentDeclaration arg;
					Assertions.assertEquals( 1, closure.getArgs().get( 0 ).getAnnotations().size() );

				}
			} );
		} );

		CompilationUnit javaAST = ( CompilationUnit ) transformClosure( code );
		System.out.println( javaAST.toString() );
	}

	@Test
	public void testClosureAsAnonymous() throws IOException {
		String			code	= """
		                          	function( required string param1='default' key='value' ) key='value' { return param1; }
		                          """;
		BoxCFParser		parser	= new BoxCFParser();
		ParsingResult	result	= parser.parse( code );
		assertTrue( result.isCorrect() );
		BoxScript script = ( BoxScript ) result.getRoot();
		script.getStatements().forEach( stmt -> {
			stmt.walk().forEach( it -> {
				if ( it instanceof BoxExpression exp && exp.getExpression() instanceof BoxClosure closure ) {
					Assertions.assertEquals( 1, closure.getArgs().size() );
					Assertions.assertEquals( 1, closure.getAnnotations().size() );

					BoxArgumentDeclaration arg;
					Assertions.assertEquals( 1, closure.getArgs().get( 0 ).getAnnotations().size() );

				}
			} );
		} );

		CompilationUnit javaAST = ( CompilationUnit ) transformClosure( code );
		System.out.println( javaAST.toString() );
	}

	@Test
	public void testClosureReturn() throws IOException {
		String			code	= """
		                          	() => "my func";
		                          """;
		BoxCFParser		parser	= new BoxCFParser();
		ParsingResult	result	= parser.parse( code );
		assertTrue( result.isCorrect() );

		CompilationUnit javaAST = ( CompilationUnit ) transformClosure( code );
		System.out.println( javaAST.toString() );
	}

}
