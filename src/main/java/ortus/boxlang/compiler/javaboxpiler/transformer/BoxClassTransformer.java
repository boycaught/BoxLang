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
package ortus.boxlang.compiler.javaboxpiler.transformer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.Statement;

import ortus.boxlang.compiler.ast.BoxClass;
import ortus.boxlang.compiler.ast.BoxExpression;
import ortus.boxlang.compiler.ast.BoxNode;
import ortus.boxlang.compiler.ast.BoxStatement;
import ortus.boxlang.compiler.ast.Source;
import ortus.boxlang.compiler.ast.SourceFile;
import ortus.boxlang.compiler.ast.expression.BoxBooleanLiteral;
import ortus.boxlang.compiler.ast.expression.BoxFQN;
import ortus.boxlang.compiler.ast.expression.BoxIntegerLiteral;
import ortus.boxlang.compiler.ast.expression.BoxNull;
import ortus.boxlang.compiler.ast.expression.BoxStringLiteral;
import ortus.boxlang.compiler.ast.statement.BoxAnnotation;
import ortus.boxlang.compiler.ast.statement.BoxImport;
import ortus.boxlang.compiler.ast.statement.BoxProperty;
import ortus.boxlang.compiler.javaboxpiler.JavaTranspiler;
import ortus.boxlang.runtime.config.util.PlaceholderHelper;
import ortus.boxlang.runtime.dynamic.casters.BooleanCaster;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class BoxClassTransformer extends AbstractTransformer {

	// @formatter:off
	private final String template = """
		package ${packageName};


		// BoxLang Auto Imports
		import ortus.boxlang.runtime.BoxRuntime;
		import ortus.boxlang.runtime.components.Component;
		import ortus.boxlang.runtime.context.*;
		import ortus.boxlang.runtime.context.ClassBoxContext;
		import ortus.boxlang.runtime.context.FunctionBoxContext;
		import ortus.boxlang.runtime.dynamic.casters.*;
		import ortus.boxlang.runtime.dynamic.ExpressionInterpreter;
		import ortus.boxlang.runtime.dynamic.IReferenceable;
		import ortus.boxlang.runtime.dynamic.Referencer;
		import ortus.boxlang.runtime.interop.DynamicObject;
		import ortus.boxlang.runtime.interop.DynamicObject;
		import ortus.boxlang.runtime.loader.ClassLocator;
		import ortus.boxlang.runtime.loader.ImportDefinition;
		import ortus.boxlang.runtime.operators.*;
		import ortus.boxlang.runtime.runnables.BoxScript;
		import ortus.boxlang.runtime.runnables.BoxTemplate;
		import ortus.boxlang.runtime.runnables.IClassRunnable;
		import ortus.boxlang.runtime.runnables.BoxClassSupport;
		import ortus.boxlang.runtime.scopes.*;
		import ortus.boxlang.runtime.scopes.Key;
		import ortus.boxlang.runtime.types.*;
		import ortus.boxlang.runtime.types.util.*;
		import ortus.boxlang.runtime.types.exceptions.*;
		import ortus.boxlang.runtime.types.exceptions.ExceptionUtil;
		import ortus.boxlang.runtime.types.meta.BoxMeta;
		import ortus.boxlang.runtime.types.meta.ClassMeta;
		import ortus.boxlang.runtime.types.Property;
		import ortus.boxlang.runtime.util.*;
		import ortus.boxlang.web.scopes.*;
		import ortus.boxlang.compiler.parser.BoxSourceType;

		// Java Imports
		import java.nio.file.Path;
		import java.nio.file.Paths;
		import java.time.LocalDateTime;
		import java.util.ArrayList;
		import java.util.Collections;
		import java.util.HashMap;
		import java.util.Iterator;
		import java.util.LinkedHashMap;
		import java.util.LinkedHashMap;
		import java.util.List;
		import java.util.Map;
		import java.util.Optional;

		public class ${className} implements IClassRunnable, IReferenceable, IType {

			private static final List<ImportDefinition>	imports			= List.of();
			private static final Path					path			= Paths.get( "${fileFolderPath}" );
			private static final BoxSourceType			sourceType		= BoxSourceType.${sourceType};
			private static final long					compileVersion	= ${compileVersion};
			private static final LocalDateTime			compiledOn		= ${compiledOnTimestamp};
			private static final Object					ast				= null;
			public static final Key[]					keys			= new Key[] {};

			/**
			 * Metadata object
			 */
			public BoxMeta						$bx;

			/**
			 * Cached lookup of the output annotation
			 */
			private Boolean			canOutput			= null;

			private final static IStruct	annotations;
			private final static IStruct	documentation;
			// replace Object with record/class to represent a property
			private final static Map<Key,Property>	properties;
			private final static Map<Key,Property>	getterLookup=null;
			private final static Map<Key,Property>	setterLookup=null;

			private VariablesScope variablesScope = new ClassVariablesScope(this);
			private ThisScope thisScope = new ThisScope();
			private Key name = ${boxClassName};
			private IClassRunnable _super = null;
			private IClassRunnable child = null;

			public ${className}() {
			}

			public Map<Key,Property> getGetterLookup() {
				return getterLookup;
			}
			public Map<Key,Property> getSetterLookup() {
				return setterLookup;
			}

			public BoxMeta _getbx() {
				return this.$bx;
			}

			public void _setbx( BoxMeta bx ) {
				this.$bx = bx;
			}

			public void pseudoConstructor( IBoxContext context ) {
				BoxClassSupport.pseudoConstructor( this, context );
			}

			public void _pseudoConstructor( IBoxContext context ) {
				ClassLocator classLocator = ClassLocator.getInstance();
			}

			// ITemplateRunnable implementation methods

			public long getRunnableCompileVersion() {
				return ${className}.compileVersion;
			}

			public LocalDateTime getRunnableCompiledOn() {
				return ${className}.compiledOn;
			}

			public Object getRunnableAST() {
				return ${className}.ast;
			}

			public Path getRunnablePath() {
				return ${className}.path;
			}

			public BoxSourceType getSourceType() {
				return sourceType;
			}

			public List<ImportDefinition> getImports() {
				return imports;
			}

			public VariablesScope getVariablesScope() {
				return variablesScope;
			}

			public ThisScope getThisScope() {
				return thisScope;
			}

			public IStruct getAnnotations() {
				return annotations;
			}

			public IStruct getDocumentation() {
				return documentation;
			}

			public Key getName() {
				return this.name;
			}

			public Map<Key,Property> getProperties() {
				return this.properties;
			}

			public BoxMeta getBoxMeta() {
				return BoxClassSupport.getBoxMeta( this );
			}

			public String asString() {
				return BoxClassSupport.asString( this );
			}

			public boolean canOutput() {				
				return BoxClassSupport.canOutput( this );
			}
			
			public Boolean getCanOutput() {
				return this.canOutput;
			}

			public void setCanOutput( Boolean canOutput ) {
				this.canOutput = canOutput;
			}

			public IClassRunnable getSuper() {
				return this._super;
			}

			public void setSuper( IClassRunnable _super ) {
				BoxClassSupport.setSuper( this, _super );
			}
			
			public void _setSuper( IClassRunnable _super ) {
				this._super = _super;
			}

			public IClassRunnable getChild() {
				return this.child;
			}

			public void setChild( IClassRunnable child ) {
				this.child = child;
			}

			public IClassRunnable getBottomClass() {
				return BoxClassSupport.getBottomClass( this );
			}

			/**
			 * --------------------------------------------------------------------------
			 * IReferenceable Interface Methods
			 * --------------------------------------------------------------------------
			 */

			public Object assign( IBoxContext context, Key key, Object value ) {				
				return BoxClassSupport.assign( this, context, key, value );
			}

			public Object dereference( IBoxContext context, Key key, Boolean safe ) {
				return BoxClassSupport.dereference( this, context, key, safe );
			}

			public Object dereferenceAndInvoke( IBoxContext context, Key name, Object[] positionalArguments, Boolean safe ) {
				return BoxClassSupport.dereferenceAndInvoke( this, context, name, positionalArguments, safe );
			}

			public Object dereferenceAndInvoke( IBoxContext context, Key name, Map<Key, Object> namedArguments, Boolean safe ) {
					return BoxClassSupport.dereferenceAndInvoke( this, context, name, namedArguments, safe );
			}

			public IStruct getMetaData() {
				return BoxClassSupport.getMetaData( this );
			}

		}
	""";
	// @formatter:on

	/**
	 * Constructor
	 *
	 * @param transpiler parent transpiler
	 */
	public BoxClassTransformer( JavaTranspiler transpiler ) {
		super( transpiler );
	}

	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {

		BoxClass	boxClass		= ( BoxClass ) node;
		Source		source			= boxClass.getPosition().getSource();
		String		packageName		= transpiler.getProperty( "packageName" );
		String		boxPackageName	= transpiler.getProperty( "boxPackageName" );
		String		className		= transpiler.getProperty( "classname" );
		String		fileName		= source instanceof SourceFile file && file.getFile() != null ? file.getFile().getName() : "unknown";
		String		filePath		= source instanceof SourceFile file && file.getFile() != null ? file.getFile().getAbsolutePath()
		    : "unknown";
		String		boxClassName	= boxPackageName + "." + fileName.replace( ".bx", "" ).replace( ".cfc", "" );
		String		sourceType		= transpiler.getProperty( "sourceType" );

		// trim leading . if exists
		if ( boxClassName.startsWith( "." ) ) {
			boxClassName = boxClassName.substring( 1 );
		}

		Map<String, String>				values	= Map.ofEntries(
		    Map.entry( "packagename", packageName ),
		    Map.entry( "boxPackageName", boxPackageName ),
		    Map.entry( "className", className ),
		    Map.entry( "fileName", fileName ),
		    Map.entry( "sourceType", sourceType ),
		    Map.entry( "fileFolderPath", filePath.replaceAll( "\\\\", "\\\\\\\\" ) ),
		    Map.entry( "compiledOnTimestamp", transpiler.getDateTime( LocalDateTime.now() ) ),
		    Map.entry( "compileVersion", "1L" ),
		    Map.entry( "boxClassName", createKey( boxClassName ).toString() )
		);
		String							code	= PlaceholderHelper.resolve( template, values );
		ParseResult<CompilationUnit>	result;

		try {
			result = javaParser.parse( code );
		} catch ( Exception e ) {
			// Temp debugging to see generated Java code
			throw new BoxRuntimeException( code, e );
		}
		if ( !result.isSuccessful() ) {
			// Temp debugging to see generated Java code
			throw new BoxRuntimeException(
			    "Error parsing class" + packageName + "." + className + ". The message received was:" + result.toString() + "\n" + code );
		}

		CompilationUnit		entryPoint				= result.getResult().get();

		MethodDeclaration	pseudoConstructorMethod	= entryPoint.findCompilationUnit().orElseThrow()
		    .getClassByName( className ).orElseThrow()
		    .getMethodsByName( "_pseudoConstructor" ).get( 0 );

		FieldDeclaration	imports					= entryPoint.findCompilationUnit().orElseThrow()
		    .getClassByName( className ).orElseThrow()
		    .getFieldByName( "imports" ).orElseThrow();

		FieldDeclaration	keys					= entryPoint.findCompilationUnit().orElseThrow()
		    .getClassByName( className ).orElseThrow()
		    .getFieldByName( "keys" ).orElseThrow();

		/* Transform the annotations creating the initialization value */
		Expression			annotationStruct		= transformAnnotations( boxClass.getAnnotations() );
		result.getResult().orElseThrow().getType( 0 ).getFieldByName( "annotations" ).orElseThrow().getVariable( 0 ).setInitializer( annotationStruct );

		/* Transform the documentation creating the initialization value */
		Expression documentationStruct = transformDocumentation( boxClass.getDocumentation() );
		result.getResult().orElseThrow().getType( 0 ).getFieldByName( "documentation" ).orElseThrow().getVariable( 0 )
		    .setInitializer( documentationStruct );

		List<Expression> propertyStructs = transformProperties( boxClass.getProperties() );
		result.getResult().orElseThrow().getType( 0 ).getFieldByName( "properties" ).orElseThrow().getVariable( 0 )
		    .setInitializer( propertyStructs.get( 0 ) );
		result.getResult().orElseThrow().getType( 0 ).getFieldByName( "getterLookup" ).orElseThrow().getVariable( 0 )
		    .setInitializer( propertyStructs.get( 1 ) );
		result.getResult().orElseThrow().getType( 0 ).getFieldByName( "setterLookup" ).orElseThrow().getVariable( 0 )
		    .setInitializer( propertyStructs.get( 2 ) );

		transpiler.pushContextName( "context" );
		var pseudoConstructorBody = pseudoConstructorMethod.getBody().orElseThrow();

		// Add imports
		for ( BoxImport statement : boxClass.getImports() ) {
			Node			javaASTNode	= transpiler.transform( statement );
			// For import statements, we add an argument to the constructor of the static List of imports
			MethodCallExpr	imp			= ( MethodCallExpr ) imports.getVariable( 0 ).getInitializer().orElseThrow();
			imp.getArguments().add( ( MethodCallExpr ) javaASTNode );
		}
		// Add body
		for ( BoxStatement statement : boxClass.getBody() ) {

			Node javaASTNode = transpiler.transform( statement );

			// These get left behind from UDF declarations
			if ( javaASTNode instanceof EmptyStmt ) {
				continue;
			}

			// Java block get each statement in their block added
			if ( javaASTNode instanceof BlockStmt ) {
				BlockStmt stmt = ( BlockStmt ) javaASTNode;
				stmt.getStatements().forEach( it -> {
					pseudoConstructorBody.addStatement( it );
					// statements.add( it );
				} );
			} else if ( statement instanceof BoxImport ) {
				// For import statements, we add an argument to the constructor of the static List of imports
				MethodCallExpr imp = ( MethodCallExpr ) imports.getVariable( 0 ).getInitializer().orElseThrow();
				imp.getArguments().add( ( MethodCallExpr ) javaASTNode );
			} else {
				// All other statements are added to the _invoke() method
				pseudoConstructorBody.addStatement( ( Statement ) javaASTNode );
				// statements.add( ( Statement ) javaASTNode );
			}
		}
		// loop over UDF registrations and add them to the _invoke() method
		( ( JavaTranspiler ) transpiler ).getUDFDeclarations().forEach( it -> {
			pseudoConstructorBody.addStatement( 0, it );
		} );

		// Add the keys to the static keys array
		ArrayCreationExpr keysImp = ( ArrayCreationExpr ) keys.getVariable( 0 ).getInitializer().orElseThrow();
		for ( Map.Entry<String, BoxExpression> entry : transpiler.getKeys().entrySet() ) {
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

		transpiler.popContextName();

		return entryPoint;
	}

	/**
	 * Transforms a collection of properties into a Map
	 *
	 * @param properties list of properties
	 *
	 * @return an Expression node
	 */
	private List<Expression> transformProperties( List<BoxProperty> properties ) {
		List<Expression>	members			= new ArrayList<Expression>();
		List<Expression>	getterLookup	= new ArrayList<Expression>();
		List<Expression>	setterLookup	= new ArrayList<Expression>();
		properties.forEach( prop -> {
			Expression			documentationStruct		= transformDocumentation( prop.getDocumentation() );
			/*
			 * normalize annotations to allow for
			 * property String userName;
			 */
			List<BoxAnnotation>	finalAnnotations		= new ArrayList<BoxAnnotation>();
			var					annotations				= prop.getAnnotations();
			int					namePosition			= annotations.stream().map( BoxAnnotation::getKey ).map( BoxFQN::getValue ).map( String::toLowerCase )
			    .collect( java.util.stream.Collectors.toList() ).indexOf( "name" );
			int					typePosition			= annotations.stream().map( BoxAnnotation::getKey ).map( BoxFQN::getValue ).map( String::toLowerCase )
			    .collect( java.util.stream.Collectors.toList() ).indexOf( "type" );
			int					defaultPosition			= annotations.stream().map( BoxAnnotation::getKey ).map( BoxFQN::getValue ).map( String::toLowerCase )
			    .collect( java.util.stream.Collectors.toList() ).indexOf( "default" );
			int					numberOfNonValuedKeys	= ( int ) annotations.stream().map( BoxAnnotation::getValue ).filter( it -> it == null ).count();
			List<BoxAnnotation>	nonValuedKeys			= annotations.stream().filter( it -> it.getValue() == null )
			    .collect( java.util.stream.Collectors.toList() );
			BoxAnnotation		nameAnnotation			= null;
			BoxAnnotation		typeAnnotation			= null;
			BoxAnnotation		defaultAnnotation		= null;

			if ( namePosition > -1 )
				nameAnnotation = annotations.get( namePosition );
			if ( typePosition > -1 )
				typeAnnotation = annotations.get( typePosition );
			if ( defaultPosition > -1 )
				defaultAnnotation = annotations.get( defaultPosition );
			/*
			 * If there is no name, if there is more than one nonvalued keys and no type, use the first nonvalued key
			 * as the type and second nonvalued key as the name. Otherwise, if there are more than one non-valued key, use the first as the name.
			 */
			if ( namePosition == -1 ) {
				if ( numberOfNonValuedKeys > 1 && typePosition == -1 ) {
					typeAnnotation	= new BoxAnnotation( new BoxFQN( "type", null, null ),
					    new BoxStringLiteral( nonValuedKeys.get( 0 ).getKey().getValue(), null, null ), null,
					    null );
					nameAnnotation	= new BoxAnnotation( new BoxFQN( "name", null, null ),
					    new BoxStringLiteral( nonValuedKeys.get( 1 ).getKey().getValue(), null, null ), null,
					    null );
					finalAnnotations.add( nameAnnotation );
					finalAnnotations.add( typeAnnotation );
					annotations.remove( nonValuedKeys.get( 0 ) );
					annotations.remove( nonValuedKeys.get( 1 ) );
				} else if ( numberOfNonValuedKeys > 0 ) {
					nameAnnotation = new BoxAnnotation( new BoxFQN( "name", null, null ),
					    new BoxStringLiteral( nonValuedKeys.get( 0 ).getKey().getValue(), null, null ), null,
					    null );
					finalAnnotations.add( nameAnnotation );
					annotations.remove( nonValuedKeys.get( 0 ) );
				} else {
					throw new BoxRuntimeException( "Property [" + prop.getSourceText() + "] has no name" );
				}
			}
			// add type with value of any if not present
			if ( typeAnnotation == null ) {
				typeAnnotation = new BoxAnnotation( new BoxFQN( "type", null, null ), new BoxStringLiteral( "any", null, null ), null,
				    null );
				finalAnnotations.add( typeAnnotation );
			}
			// add default with value of null if not present
			if ( defaultPosition == -1 ) {
				defaultAnnotation = new BoxAnnotation( new BoxFQN( "default", null, null ), new BoxNull( null, null ), null,
				    null );
				finalAnnotations.add( defaultAnnotation );
			}
			// add remaining annotations
			finalAnnotations.addAll( annotations );

			Expression	annotationStruct	= transformAnnotations( finalAnnotations );
			/* Process default value */
			String		init				= "null";
			if ( defaultAnnotation.getValue() != null ) {
				Node initExpr = transpiler.transform( defaultAnnotation.getValue() );
				init = initExpr.toString();
			}
			// name and type must be simple values
			String	name;
			String	type;
			if ( nameAnnotation.getValue() instanceof BoxStringLiteral namelit ) {
				name = namelit.getValue().trim();
				if ( name.isEmpty() )
					throw new BoxRuntimeException( "Property [" + prop.getSourceText() + "] name cannot be empty" );
			} else {
				throw new BoxRuntimeException( "Property [" + prop.getSourceText() + "] name must be a simple value" );
			}
			if ( typeAnnotation.getValue() instanceof BoxStringLiteral typelit ) {
				type = typelit.getValue().trim();
				if ( type.isEmpty() )
					throw new BoxRuntimeException( "Property [" + prop.getSourceText() + "] type cannot be empty" );
			} else {
				throw new BoxRuntimeException( "Property [" + prop.getSourceText() + "] type must be a simple value" );
			}
			Expression						jNameKey	= ( Expression ) createKey( name );
			Expression						jGetNameKey	= ( Expression ) createKey( "get" + name );
			Expression						jSetNameKey	= ( Expression ) createKey( "set" + name );
			LinkedHashMap<String, String>	values		= new LinkedHashMap<>();
			values.put( "type", type );
			values.put( "name", jNameKey.toString() );
			values.put( "init", init );
			values.put( "annotations", annotationStruct.toString() );
			values.put( "documentation", documentationStruct.toString() );
			String		template	= """
			                          				new Property( ${name}, "${type}", ${init}, ${annotations} ,${documentation} )
			                          """;
			Expression	javaExpr	= ( Expression ) parseExpression( template, values );
			// logger.atTrace().log( "{} -> {}", prop.getSourceText(), javaExpr );

			members.add( jNameKey );
			members.add( javaExpr );

			// Check if getter key annotation is defined in finalAnnotations and false. I don't love this as annotations can technically be any literal
			boolean getter = !finalAnnotations.stream()
			    .anyMatch( it -> it.getKey().getValue().equalsIgnoreCase( "getter" ) && !BooleanCaster.cast( getBoxExprAsString( it.getValue() ) ) );
			if ( getter ) {
				getterLookup.add( jGetNameKey );
				getterLookup.add( ( Expression ) parseExpression( "properties.get( ${name} )", values ) );
			}
			// Check if setter key annotation is defined in finalAnnotations and false. I don't love this as annotations can technically be any literal
			boolean setter = !finalAnnotations.stream()
			    .anyMatch( it -> it.getKey().getValue().equalsIgnoreCase( "setter" ) && !BooleanCaster.cast( getBoxExprAsString( it.getValue() ) ) );
			if ( setter ) {
				setterLookup.add( jSetNameKey );
				setterLookup.add( ( Expression ) parseExpression( "properties.get( ${name} )", values ) );
			}
		} );
		if ( members.isEmpty() ) {
			Expression	emptyMap	= ( Expression ) parseExpression( "MapHelper.LinkedHashMapOfProperties()", new HashMap<>() );
			Expression	emptyMap2	= ( Expression ) parseExpression( "MapHelper.HashMapOfProperties()", new HashMap<>() );
			return List.of( emptyMap, emptyMap2, emptyMap2 );
		} else {
			MethodCallExpr	propertiesStruct	= ( MethodCallExpr ) parseExpression( "MapHelper.LinkedHashMapOfProperties()", new HashMap<>() );
			MethodCallExpr	getterStruct		= ( MethodCallExpr ) parseExpression( "MapHelper.HashMapOfProperties()", new HashMap<>() );
			MethodCallExpr	setterStruct		= ( MethodCallExpr ) parseExpression( "MapHelper.HashMapOfProperties()", new HashMap<>() );
			propertiesStruct.getArguments().addAll( members );
			getterStruct.getArguments().addAll( getterLookup );
			setterStruct.getArguments().addAll( setterLookup );
			return List.of( propertiesStruct, getterStruct, setterStruct );
		}
	}

	/**
	 * Janky workaround to extract value from a literal expression.
	 *
	 * @param expr the expression to extract the value from
	 *
	 * @return the value as a string
	 */
	private String getBoxExprAsString( BoxExpression expr ) {
		if ( expr == null ) {
			return "";
		}
		if ( expr instanceof BoxStringLiteral str ) {
			return str.getValue();
		}
		if ( expr instanceof BoxBooleanLiteral bool ) {
			return bool.getValue() ? "true" : "false";
		} else {
			throw new BoxRuntimeException( "Unsupported BoxExpr type: " + expr.getClass().getSimpleName() );
		}
	}

}
