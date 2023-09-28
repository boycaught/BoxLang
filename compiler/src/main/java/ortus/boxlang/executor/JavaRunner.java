/**
 * [BoxLang]
 * <p>
 * Copyright [2023] [Ortus Solutions, Corp]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package ortus.boxlang.executor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.stmt.Statement;

/**
 * Dynamic in memory Java executor
 */
public class JavaRunner {

	final static String	fqn			= "ortus.boxlang.test.TestClass";
	String				template	= """
	                                                                         package ortus.boxlang.test;

	                                                                         import ortus.boxlang.runtime.BoxRuntime;
	                                                                         import ortus.boxlang.runtime.context.*;

	                                                                         // BoxLang Auto Imports
	                                                                         import ortus.boxlang.runtime.runnables.BoxTemplate;
	                                                                         import ortus.boxlang.runtime.dynamic.Referencer;
	                                                                         import ortus.boxlang.runtime.interop.DynamicObject;
	                                                                         import ortus.boxlang.runtime.loader.ClassLocator;
	                                  import ortus.boxlang.runtime.loader.ImportDefinition;
	                                                                         import ortus.boxlang.runtime.operators.*;
	                                                                         import ortus.boxlang.runtime.scopes.Key;
	                                                                         import ortus.boxlang.runtime.scopes.IScope;
	                                                                         import ortus.boxlang.runtime.dynamic.casters.*;

	                                                                         import java.nio.file.Path;
	                                                                         import java.nio.file.Paths;
	                                                                         import java.time.LocalDateTime;
	                                                                         import java.util.List;

	                                                                         public class TestClass extends BoxTemplate {

	                                                                          	private static TestClass instance;

	                                  	private static final List<ImportDefinition>	imports			= List.of();
	                                                                          	private static final Path					path			= Paths.get( "" );
	                                                                          	private static final long					compileVersion	= 1L;
	                                                                          	private static final LocalDateTime			compiledOn		= LocalDateTime.parse( "2023-09-27T10:15:30" );
	                                                                          	private static final Object					ast				= null;

	                                                                          	public TestClass() {
	                                                                          	}

	                                                                          	public static synchronized TestClass getInstance() {
	                                                                          		if ( instance == null ) {
	                                                                          			instance = new TestClass();
	                                                                          		}
	                                                                          		return instance;
	                                                                          	}
	                                                                          	/**
	                                                                          		* Each template must implement the invoke() method which executes the template
	                                                                          		*
	                                                                          		* @param context The execution context requesting the execution
	                                                                          		*/
	                                                                          	public void _invoke( IBoxContext context ) {
	                                                                          		// Reference to the variables scope
	                                                                          		IScope variablesScope = context.getScopeNearby( Key.of( "variables" ) );
	                                                                          		ClassLocator JavaLoader = ClassLocator.getInstance();
	                                                                          		IBoxContext			catchContext = null;
	                                                                          		${javaCode};
	                                                                          		String result = variablesScope.toString();
	                                                                          		System.out.println(result);
	                                                                          		if(catchContext != null) {
	                                                                          			System.out.println(catchContext);
	                                                                          		}

	                                                                          	}

	                                                                          	// ITemplateRunnable implementation methods

	                                                                          	/**
	                                                                          	 * The version of the BoxLang runtime
	                                                                          	*/
	                                                                          	public long getRunnableCompileVersion() {
	                                                                         	 	return TestClass.compileVersion;
	                                                                          	}

	                                                                          	/**
	                                                                          	 * The date the template was compiled
	                                                                          	*/
	                                                                          	public LocalDateTime getRunnableCompiledOn() {
	                                                                         	 	return TestClass.compiledOn;
	                                                                          	}

	                                                                          	/**
	                                                                          	 * The AST (abstract syntax tree) of the runnable
	                                                                          	*/
	                                                                          	public Object getRunnableAST() {
	                                                                        	  	return TestClass.ast;
	                                                                          	}

	                                                                          	/**
	                                                                          	 * The path to the template
	                                                                          	*/
	                                                                          	public Path getRunnablePath() {
	                                                                        	  	return TestClass.path;
	                                                                          	}

	                                                                          	public static void main(String[] args) {
	                                                                          		BoxRuntime rt = BoxRuntime.getInstance();

	                                                                          		try {
	                                                                          			rt.executeTemplate( TestClass.getInstance() );
	                                                                          		} catch ( Throwable e ) {
	                                                                          			e.printStackTrace();
	                                                                          			System.exit( 1 );
	                                                                          		}

	                                                                          		// Bye bye! Ciao Bella!
	                                                                          		rt.shutdown();


	                                                                          	}
	                                                                         }
	                                                                         """;

	Logger				logger		= LoggerFactory.getLogger( JavaRunner.class );

	private String makeClass( String javaCode ) {
		Map<String, String>	values	= new HashMap<>() {

										{
											put( "javaCode", javaCode );
										}
									};

		StringSubstitutor	sub		= new StringSubstitutor( values );
		return sub.replace( template );
	}

	public void runExpression( String expression ) {
		run(
		    makeClass( expression )
		);
	}

	public void run( List<Statement> statements ) {
		String javaCode = statements.stream().map( it -> it.toString() )
		    .collect( Collectors.joining( "\n" ) );
		run(
		    makeClass( javaCode )
		);
	}

	private void run( String javaClass ) {
		System.out.println( javaClass );
		try {
			JavaCompiler						compiler	= ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject>	diagnostics	= new DiagnosticCollector<>();
			JavaMemoryManager					manager		= new JavaMemoryManager( compiler.getStandardFileManager( null, null, null ) );

			String								javaRT		= System.getProperty( "java.class.path" );
			String								boxRT		= "/home/madytyoo/IdeaProjects/boxlang1/runtime/build/classes/java/main";
			String								compRT		= "/home/madytyoo/IdeaProjects/boxlang1/compiler/build/classes/java/main";

			List<JavaFileObject>				sourceFiles	= Collections.singletonList( new JavaSourceString( fqn, javaClass ) );
			List<String>						options		= new ArrayList<>() {

																{
																	add( "-g" );
																	add( "-cp" );
																	add( javaRT + File.pathSeparator + boxRT + File.pathSeparator + File.pathSeparator
																	    + compRT );

																}
															};
			JavaCompiler.CompilationTask		task		= compiler.getTask( null, manager, diagnostics, options, null, sourceFiles );
			boolean								result		= task.call();

			if ( !result ) {
				diagnostics.getDiagnostics()
				    .forEach( d -> logger.error( String.valueOf( d ) ) );
				throw new RuntimeException( "Compiler Error" );
			} else {
				JavaDynamicClassLoader	classLoader	= new JavaDynamicClassLoader(
				    new URL[] {
				        new File( boxRT ).toURI().toURL()
				    },
				    this.getClass().getClassLoader(),
				    manager );

				// JavaDynamicClassLoader classLoader = (JavaDynamicClassLoader) manager.getClassLoader( null );
				// classLoader.defineClass(fqn);
				Class					cls			= Class.forName( fqn, true, classLoader );
				Method					meth		= cls.getMethod( "main", String[].class );
				String[]				params		= null; // init params accordingly
				meth.invoke( null, ( Object ) params );

			}
		} catch ( ClassNotFoundException e ) {
			throw new RuntimeException( e );
		} catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		} catch ( InvocationTargetException e ) {
			throw new RuntimeException( e );
		} catch ( NoSuchMethodException e ) {
			throw new RuntimeException( e );
		} catch ( MalformedURLException e ) {
			throw new RuntimeException( e );
		}
	}
}
