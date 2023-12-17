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
package ortus.boxlang.transpiler;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.BoxScript;
import ortus.boxlang.ast.BoxStatement;
import ortus.boxlang.ast.expression.BoxArgument;
import ortus.boxlang.ast.expression.BoxArrayAccess;
import ortus.boxlang.ast.expression.BoxArrayLiteral;
import ortus.boxlang.ast.expression.BoxAssignment;
import ortus.boxlang.ast.expression.BoxBinaryOperation;
import ortus.boxlang.ast.expression.BoxBooleanLiteral;
import ortus.boxlang.ast.expression.BoxComparisonOperation;
import ortus.boxlang.ast.expression.BoxDecimalLiteral;
import ortus.boxlang.ast.expression.BoxDotAccess;
import ortus.boxlang.ast.expression.BoxExpressionInvocation;
import ortus.boxlang.ast.expression.BoxFQN;
import ortus.boxlang.ast.expression.BoxFunctionInvocation;
import ortus.boxlang.ast.expression.BoxIdentifier;
import ortus.boxlang.ast.expression.BoxIntegerLiteral;
import ortus.boxlang.ast.expression.BoxLambda;
import ortus.boxlang.ast.expression.BoxMethodInvocation;
import ortus.boxlang.ast.expression.BoxNegateOperation;
import ortus.boxlang.ast.expression.BoxNewOperation;
import ortus.boxlang.ast.expression.BoxNull;
import ortus.boxlang.ast.expression.BoxParenthesis;
import ortus.boxlang.ast.expression.BoxScope;
import ortus.boxlang.ast.expression.BoxStringConcat;
import ortus.boxlang.ast.expression.BoxStringInterpolation;
import ortus.boxlang.ast.expression.BoxStringLiteral;
import ortus.boxlang.ast.expression.BoxStructLiteral;
import ortus.boxlang.ast.expression.BoxTernaryOperation;
import ortus.boxlang.ast.expression.BoxUnaryOperation;
import ortus.boxlang.ast.statement.BoxArgumentDeclaration;
import ortus.boxlang.ast.statement.BoxAssert;
import ortus.boxlang.ast.statement.BoxBreak;
import ortus.boxlang.ast.statement.BoxContinue;
import ortus.boxlang.ast.statement.BoxDo;
import ortus.boxlang.ast.statement.BoxExpression;
import ortus.boxlang.ast.statement.BoxForIn;
import ortus.boxlang.ast.statement.BoxForIndex;
import ortus.boxlang.ast.statement.BoxFunctionDeclaration;
import ortus.boxlang.ast.statement.BoxIfElse;
import ortus.boxlang.ast.statement.BoxImport;
import ortus.boxlang.ast.statement.BoxInclude;
import ortus.boxlang.ast.statement.BoxRethrow;
import ortus.boxlang.ast.statement.BoxReturn;
import ortus.boxlang.ast.statement.BoxSwitch;
import ortus.boxlang.ast.statement.BoxThrow;
import ortus.boxlang.ast.statement.BoxTry;
import ortus.boxlang.ast.statement.BoxWhile;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.runnables.BoxTemplate;
import ortus.boxlang.runtime.runnables.compiler.JavaSourceString;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.exceptions.ApplicationException;
import ortus.boxlang.transpiler.transformer.Transformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;
import ortus.boxlang.transpiler.transformer.expression.BoxAccessTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxArgumentTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxArrayLiteralTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxAssignmentTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxBinaryOperationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxBooleanLiteralTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxComparisonOperationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxDecimalLiteralTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxExpressionInvocationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxFQNTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxFunctionInvocationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxIdentifierTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxIntegerLiteralTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxLambdaTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxMethodInvocationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxNegateOperationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxNewOperationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxNullTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxParenthesisTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxScopeTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxStringConcatTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxStringInterpolationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxStringLiteralTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxStructLiteralTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxTernaryOperationTransformer;
import ortus.boxlang.transpiler.transformer.expression.BoxUnaryOperationTransformer;
import ortus.boxlang.transpiler.transformer.indexer.CrossReference;
import ortus.boxlang.transpiler.transformer.indexer.IndexPrettyPrinterVisitor;
import ortus.boxlang.transpiler.transformer.statement.BoxArgumentDeclarationTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxAssertTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxBreakTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxContinueTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxDoTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxExpressionTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxForInTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxForIndexTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxFunctionDeclarationTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxIfElseTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxImportTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxIncludeTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxRethrowTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxReturnTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxScriptTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxSwitchTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxThrowTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxTryTransformer;
import ortus.boxlang.transpiler.transformer.statement.BoxWhileTransformer;

/**
 * BoxLang AST to Java AST transpiler
 * The registry maps a AST node to the corresponding Transformer Java class instance.
 * Each transformer implements the logic to convert the BoxLang AST nodes into Java
 * AST nodes.
 *
 */
public class JavaTranspiler extends Transpiler {

	static Logger								logger			= LoggerFactory.getLogger( JavaTranspiler.class );

	private static HashMap<Class, Transformer>	registry		= new HashMap<>();
	private List<Statement>						statements		= new ArrayList<>();
	private List<CrossReference>				crossReferences	= new ArrayList<>();
	private Map<Key, CompilationUnit>			UDFcallables	= new HashMap<Key, CompilationUnit>();
	private List<CompilationUnit>				callables		= new ArrayList<>();

	public JavaTranspiler() {
		registry.put( BoxScript.class, new BoxScriptTransformer( this ) );
		registry.put( BoxExpression.class, new BoxExpressionTransformer( this ) );

		// Expressions
		registry.put( BoxIdentifier.class, new BoxIdentifierTransformer( this ) );
		registry.put( BoxScope.class, new BoxScopeTransformer( this ) );
		// Literals
		registry.put( BoxStringLiteral.class, new BoxStringLiteralTransformer( this ) );
		registry.put( BoxIntegerLiteral.class, new BoxIntegerLiteralTransformer( this ) );
		registry.put( BoxBooleanLiteral.class, new BoxBooleanLiteralTransformer( this ) );
		registry.put( BoxDecimalLiteral.class, new BoxDecimalLiteralTransformer( this ) );
		registry.put( BoxStringInterpolation.class, new BoxStringInterpolationTransformer( this ) );
		registry.put( BoxStringConcat.class, new BoxStringConcatTransformer( this ) );
		registry.put( BoxArgument.class, new BoxArgumentTransformer( this ) );
		registry.put( BoxFQN.class, new BoxFQNTransformer( this ) );

		registry.put( BoxParenthesis.class, new BoxParenthesisTransformer( this ) );
		registry.put( BoxBinaryOperation.class, new BoxBinaryOperationTransformer( this ) );
		registry.put( BoxTernaryOperation.class, new BoxTernaryOperationTransformer( this ) );
		registry.put( BoxNegateOperation.class, new BoxNegateOperationTransformer( this ) );
		registry.put( BoxComparisonOperation.class, new BoxComparisonOperationTransformer( this ) );
		registry.put( BoxUnaryOperation.class, new BoxUnaryOperationTransformer( this ) );

		// All access nodes use the same base transformer
		registry.put( BoxDotAccess.class, new BoxAccessTransformer( this ) );
		registry.put( BoxArrayAccess.class, new BoxAccessTransformer( this ) );

		registry.put( BoxMethodInvocation.class, new BoxMethodInvocationTransformer( this ) );
		registry.put( BoxFunctionInvocation.class, new BoxFunctionInvocationTransformer( this ) );
		registry.put( BoxIfElse.class, new BoxIfElseTransformer( this ) );
		registry.put( BoxWhile.class, new BoxWhileTransformer( this ) );
		registry.put( BoxDo.class, new BoxDoTransformer( this ) );
		registry.put( BoxSwitch.class, new BoxSwitchTransformer( this ) );
		registry.put( BoxBreak.class, new BoxBreakTransformer( this ) );
		registry.put( BoxContinue.class, new BoxContinueTransformer( this ) );
		registry.put( BoxForIn.class, new BoxForInTransformer( this ) );
		registry.put( BoxForIndex.class, new BoxForIndexTransformer( this ) );
		registry.put( BoxAssert.class, new BoxAssertTransformer( this ) );
		registry.put( BoxTry.class, new BoxTryTransformer( this ) );
		registry.put( BoxThrow.class, new BoxThrowTransformer( this ) );
		registry.put( BoxNewOperation.class, new BoxNewOperationTransformer( this ) );
		registry.put( BoxFunctionDeclaration.class, new BoxFunctionDeclarationTransformer( this ) );
		registry.put( BoxArgumentDeclaration.class, new BoxArgumentDeclarationTransformer( this ) );
		registry.put( BoxReturn.class, new BoxReturnTransformer( this ) );
		registry.put( BoxRethrow.class, new BoxRethrowTransformer( this ) );
		registry.put( BoxImport.class, new BoxImportTransformer( this ) );
		registry.put( BoxArrayLiteral.class, new BoxArrayLiteralTransformer( this ) );
		registry.put( BoxStructLiteral.class, new BoxStructLiteralTransformer( this ) );
		registry.put( BoxAssignment.class, new BoxAssignmentTransformer( this ) );
		registry.put( BoxNull.class, new BoxNullTransformer( this ) );
		registry.put( BoxLambda.class, new BoxLambdaTransformer( this ) );
		registry.put( BoxInclude.class, new BoxIncludeTransformer( this ) );
		registry.put( BoxExpressionInvocation.class, new BoxExpressionInvocationTransformer( this ) );
	}

	/**
	 * Utility method to transform a node
	 *
	 * @param node a BoxLang AST Node
	 *
	 * @return a JavaParser AST Node
	 *
	 * @throws IllegalStateException
	 */
	public Node transform( BoxNode node ) throws IllegalStateException {
		return this.transform( node, TransformerContext.NONE );
	}

	/**
	 * Utility method to transform a node with a transformation context
	 *
	 * @param node    a BoxLang AST Node
	 * @param context transformation context
	 *
	 * @return
	 *
	 * @throws IllegalStateException
	 *
	 * @see TransformerContext
	 */
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		Transformer transformer = registry.get( node.getClass() );
		if ( transformer != null ) {
			Node javaNode = transformer.transform( node, context );
			// logger.info(transformer.getClass().getSimpleName() + " : " + node.getSourceText() + " -> " + javaNode );
			return javaNode;
		}
		throw new IllegalStateException( "unsupported: " + node.getClass().getSimpleName() + " : " + node.getSourceText() );
	}

	public List<Statement> getStatements() {
		return statements;
	}

	/**
	 * Cross reference
	 *
	 * @return the list of references between the source and the generated code
	 */

	public List<CrossReference> getCrossReferences() {
		return crossReferences;
	}

	/**
	 * Write a class bytecode
	 *
	 * @param cu         java compilation unit
	 * @param outputPath output directory
	 * @param classPath  classpath
	 *
	 * @throws IllegalStateException in the compilation fails
	 */
	public String compileJava( CompilationUnit cu, String outputPath, List<String> classPath ) throws IllegalStateException {
		JavaCompiler						compiler		= ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject>	diagnostics		= new DiagnosticCollector<>();
		String								pkg				= cu.getPackageDeclaration().orElseThrow().getName().toString();
		String								name			= cu.getType( 0 ).getName().asString();
		String								fqn				= pkg + "." + name;
		List<JavaFileObject>				sourceFiles		= Collections.singletonList( new JavaSourceString( fqn, cu.toString() ) );

		Writer								output			= null;

		ArrayList<String>					classPathList	= new ArrayList<>();
		classPathList.add( System.getProperty( "java.class.path" ) );
		classPathList.addAll( classPath );
		classPathList.add( outputPath );
		String compilerClassPath = classPathList
		    .stream()
		    .map( it -> {
			    return it;
		    } )
		    .collect( Collectors.joining( File.pathSeparator ) );
		;
		StandardJavaFileManager			stdFileManager	= compiler.getStandardFileManager( null, null, null );

		List<String>					options			= new ArrayList<>(
		    List.of( "-g",
		        "-cp",
		        compilerClassPath,
		        "-d",
		        outputPath
		    )
		);

		JavaCompiler.CompilationTask	task			= compiler.getTask( output, stdFileManager, diagnostics, options, null, sourceFiles );
		boolean							result			= task.call();

		if ( !result ) {
			diagnostics.getDiagnostics()
			    .forEach( d -> logger.error( String.valueOf( d ) ) );
			throw new IllegalStateException( "Compiler Error" );
		}
		try {
			stdFileManager.close();
		} catch ( Exception e ) {
			throw new IllegalStateException( "Compiler Error" );
		}

		return fqn;
	}

	/**
	 * Runa a java class
	 *
	 * @param fqn
	 * @param classPath
	 *
	 * @throws ClassNotFoundException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 */
	public void runJavaClass( String fqn, List<String> classPath )
	    throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		List<URL> finalClassPath = new ArrayList<>();
		for ( String path : classPath ) {
			try {
				finalClassPath.add( new File( path ).toURI().toURL() );

			} catch ( MalformedURLException e ) {
				throw new RuntimeException( e );
			}
		}

		try {
			URL[]			classLoaderClassPath	= finalClassPath.toArray( new URL[ 0 ] );
			URLClassLoader	classLoader				= new URLClassLoader(
			    classLoaderClassPath,
			    this.getClass().getClassLoader()
			);
			Class			boxClass				= Class.forName( fqn, true, classLoader );
			Method			method					= boxClass.getDeclaredMethod( "getInstance" );
			Object			instance				= method.invoke( boxClass );

			// Runtime
			BoxRuntime		rt						= BoxRuntime.getInstance();

			rt.executeTemplate( ( BoxTemplate ) instance );

			rt.shutdown();

		} catch ( Throwable e ) {
			throw e;
		}

	}

	/**
	 * Transpile a BoxLang AST into a Java Parser AST
	 *
	 * @return a Java Parser TranspiledCode representing the equivalent Java code
	 *
	 * @throws IllegalStateException
	 *
	 *
	 * @see TranspiledCode
	 */
	@Override
	public TranspiledCode transpile( BoxNode node ) throws ApplicationException {

		BoxScript			source			= ( BoxScript ) node;
		CompilationUnit		entryPoint		= ( CompilationUnit ) transform( source );

		String				className		= getProperty( "classname" );

		MethodDeclaration	invokeMethod	= entryPoint.findCompilationUnit().orElseThrow()
		    .getClassByName( className ).orElseThrow()
		    .getMethodsByName( "_invoke" ).get( 0 );

		FieldDeclaration	imports			= entryPoint.findCompilationUnit().orElseThrow()
		    .getClassByName( className ).orElseThrow()
		    .getFieldByName( "imports" ).orElseThrow();

		FieldDeclaration	keys			= entryPoint.findCompilationUnit().orElseThrow()
		    .getClassByName( className ).orElseThrow()
		    .getFieldByName( "keys" ).orElseThrow();

		pushContextName( "context" );
		// Track if the latest BL AST node we encountered was a returnable expression
		boolean lastStatementIsReturnable = false;
		for ( BoxStatement statement : source.getStatements() ) {
			// Expressions are returnable
			lastStatementIsReturnable = statement instanceof BoxExpression;

			Node javaASTNode = transform( statement );
			// For Function declarations, we add the transformed function itself as a compilation unit
			// and also hoist the declaration itself to the top of the _invoke() method.
			if ( statement instanceof BoxFunctionDeclaration BoxFunc ) {
				// a function declaration generate
				getUDFcallables().put( Key.of( BoxFunc.getName() ), ( CompilationUnit ) javaASTNode );
				Node registrer = transform( statement, TransformerContext.REGISTER );
				invokeMethod.getBody().orElseThrow().addStatement( 0, ( Statement ) registrer );

			} else {
				// Java block get each statement in their block added
				if ( javaASTNode instanceof BlockStmt ) {
					BlockStmt stmt = ( BlockStmt ) javaASTNode;
					stmt.getStatements().forEach( it -> {
						invokeMethod.getBody().get().addStatement( it );
						statements.add( it );
					} );
				} else if ( statement instanceof BoxImport ) {
					// For import statements, we add an argument to the constructor of the static List of imports
					MethodCallExpr imp = ( MethodCallExpr ) imports.getVariable( 0 ).getInitializer().orElseThrow();
					imp.getArguments().add( ( MethodCallExpr ) javaASTNode );
				} else {
					// All other statements are added to the _invoke() method
					invokeMethod.getBody().orElseThrow().addStatement( ( Statement ) javaASTNode );
					statements.add( ( Statement ) javaASTNode );
				}
			}
		}

		// Add the keys to the static keys array
		ArrayCreationExpr keysImp = ( ArrayCreationExpr ) keys.getVariable( 0 ).getInitializer().orElseThrow();
		for ( Map.Entry<String, BoxExpr> entry : getKeys().entrySet() ) {
			MethodCallExpr methodCallExpr = new MethodCallExpr( new NameExpr( "Key" ), "of" );
			if ( entry.getValue() instanceof BoxStringLiteral str ) {
				methodCallExpr.addArgument( new StringLiteralExpr( str.getValue() ) );
			} else if ( entry.getValue() instanceof BoxIntegerLiteral id ) {
				methodCallExpr.addArgument( new IntegerLiteralExpr( id.getValue() ) );
			} else {
				throw new IllegalStateException( "Unsupported key type: " + entry.getValue().getClass().getSimpleName() );
			}
			keysImp.getInitializer().get().getValues().add( methodCallExpr );
		}

		popContextName();

		// Only try to return a value if the class has a return type for the _invoke() method...
		if ( ! ( invokeMethod.getType() instanceof com.github.javaparser.ast.type.VoidType ) ) {
			int			lastIndex	= invokeMethod.getBody().get().getStatements().size() - 1;
			Statement	last		= invokeMethod.getBody().get().getStatements().get( lastIndex );
			// ... and the last BL AST node was a returnable expression and the last Java AST node is an expression statement
			if ( lastStatementIsReturnable && last instanceof ExpressionStmt stmt ) {
				invokeMethod.getBody().get().getStatements().remove( lastIndex );
				invokeMethod.getBody().get().getStatements().add( new ReturnStmt( stmt.getExpression() ) );
			} else {
				// If our base class requires a return value and we have none, then add a statement to return null.
				invokeMethod.getBody().orElseThrow().addStatement( new ReturnStmt( new NullLiteralExpr() ) );
			}

		}

		IndexPrettyPrinterVisitor visitor = new IndexPrettyPrinterVisitor( new DefaultPrinterConfiguration() );
		entryPoint.accept( visitor, null );
		this.crossReferences.addAll( visitor.getCrossReferences() );

		List<CompilationUnit> allCallables = getCallables();
		allCallables.addAll( getUDFcallables().values() );
		return new TranspiledCode( entryPoint, allCallables );
	}

	/**
	 * Get the list of compilation units that represent the callable functions
	 * 
	 * @return the list of compilation units
	 */
	public List<CompilationUnit> getCallables() {
		return callables;
	}

	/**
	 * Get the list of compilation units that represent the callable functions
	 * 
	 * @return the list of compilation units
	 */
	public Map<Key, CompilationUnit> getUDFcallables() {
		return UDFcallables;
	}

}
