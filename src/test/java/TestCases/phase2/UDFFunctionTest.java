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
package TestCases.phase2;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.UDF;

@Disabled
public class UDFFunctionTest {

	static BoxRuntime	instance;
	IBoxContext	context;
	IScope		variables;
	static Key			result	= new Key( "result" );
	static Key			foo		= new Key( "foo" );

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
	}

	@AfterAll
	public static void teardown() {
		instance.shutdown();
	}

	@BeforeEach
	public void setupEach() {
		context		= new ScriptingBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@DisplayName( "basic UDF" )
	@Test
	public void testBasicUDF() {

		instance.executeSource(
		    """
		       function foo() {
		    	return "my func";
		    }
		    result = foo();
		    """,
		    context );
		assertThat( variables.dereference( result, false ) ).isEqualTo( "my func" );
		assertThat( variables.dereference( foo, false ) instanceof UDF ).isEqualTo( true );

		Struct meta = ( ( UDF ) variables.dereference( foo, false ) ).getMetaData();

		assertThat( meta.dereference( Key.of( "name" ), false ) ).isEqualTo( "foo" );
		// Defaults
		assertThat( meta.dereference( Key.of( "hint" ), false ) ).isEqualTo( "" );
		assertThat( meta.dereference( Key.of( "output" ), false ) ).isEqualTo( true );
		assertThat( meta.dereference( Key.of( "returnType" ), false ) ).isEqualTo( "any" );
		assertThat( meta.dereference( Key.of( "access" ), false ) ).isEqualTo( "public" );

	}

	@DisplayName( "argument defaults" )
	@Test
	public void testArgumentDefaults() {

		instance.executeSource(
		    """
		         function foo( param1, param2="param2 default", param3, param4="param4 default" ) {
		    // Return entire arguments scope
		      	return arguments;
		      }
		      result = foo( 'Brad' );
		      """,
		    context );
		assertThat( variables.dereference( result, false ) instanceof ArgumentsScope ).isEqualTo( true );
		ArgumentsScope args = ( ArgumentsScope ) variables.dereference( result, false );

		assertThat( args.dereference( Key.of( "param1" ), false ) ).isEqualTo( null );
		assertThat( args.dereference( Key.of( "param2" ), false ) ).isEqualTo( "param2 default" );
		assertThat( args.dereference( Key.of( "param3" ), false ) ).isEqualTo( null );
		assertThat( args.dereference( Key.of( "param4" ), false ) ).isEqualTo( "param4 default" );

		assertThat( args.dereference( Key.of( "1" ), false ) ).isEqualTo( null );
		assertThat( args.dereference( Key.of( "2" ), false ) ).isEqualTo( "param2 default" );
		assertThat( args.dereference( Key.of( "3" ), false ) ).isEqualTo( null );
		assertThat( args.dereference( Key.of( "4" ), false ) ).isEqualTo( "param4 default" );

	}

	@DisplayName( "return argument" )
	@Test
	public void testReturnArgument() {

		instance.executeSource(
		    """
		       function foo( param ) {
		    	return param;
		    }
		    result = foo( 'Brad' );
		    """,
		    context );
		assertThat( variables.dereference( result, false ) ).isEqualTo( "Brad" );
		assertThat( variables.dereference( foo, false ) instanceof UDF ).isEqualTo( true );

	}

	@DisplayName( "UDF metadata" )
	@Test
	public void testUDFMetadata() {

		instance.executeSource(
		    """
		    public String function foo(
		    	required string param1 hint="My param",
		    	numeric param2=42 luis="majano"
		    ) hint="my UDF" output=false brad="wood" {
		      return "value";
		    }
		    result = foo();
		        """,
		    context );
		assertThat( variables.dereference( result, false ) ).isEqualTo( "value" );
		Struct meta = ( ( UDF ) variables.dereference( foo, false ) ).getMetaData();

		assertThat( meta.dereference( Key.of( "name" ), false ) ).isEqualTo( "foo" );
		assertThat( meta.dereference( Key.of( "hint" ), false ) ).isEqualTo( "my UDF" );
		assertThat( meta.dereference( Key.of( "output" ), false ) ).isEqualTo( false );
		assertThat( meta.dereference( Key.of( "brad" ), false ) ).isEqualTo( "wood" );
		assertThat( meta.dereference( Key.of( "returnType" ), false ) ).isEqualTo( "String" );
		assertThat( meta.dereference( Key.of( "access" ), false ) ).isEqualTo( "public" );

		Array args = ( ( Array ) meta.dereference( Key.of( "parameters" ), false ) );
		assertThat( args.size() ).isEqualTo( 2 );

		Struct param1 = ( Struct ) args.get( 0 );
		assertThat( param1.dereference( Key.of( "name" ), false ) ).isEqualTo( "param1" );
		assertThat( param1.dereference( Key.of( "hint" ), false ) ).isEqualTo( "My param" );
		assertThat( param1.dereference( Key.of( "required" ), false ) ).isEqualTo( true );
		assertThat( param1.dereference( Key.of( "type" ), false ) ).isEqualTo( "any" );

		Struct param2 = ( Struct ) args.get( 1 );
		assertThat( param2.dereference( Key.of( "name" ), false ) ).isEqualTo( "param2" );
		assertThat( param2.dereference( Key.of( "luis" ), false ) ).isEqualTo( "majano" );
		assertThat( param2.dereference( Key.of( "hint" ), false ) ).isEqualTo( "" );
		assertThat( param2.dereference( Key.of( "required" ), false ) ).isEqualTo( false );
		assertThat( param2.dereference( Key.of( "type" ), false ) ).isEqualTo( "numeric" );

	}

	@DisplayName( "UDF metadata javadoc" )
	@Test
	public void testUDFMetadataJavadoc() {

		instance.executeSource(
		    """
		       /**
		       * my UDF
		       *
		       * @param1.hint My param
		       * @param1.required true
		       *
		       * @param1.type string
		       * @param2.default 42
		       * @param2.type numeric
		       * @param2.luis majano
		       */
		       public String function foo( param1, param2 ) output=false brad="wood" {
		       	return "value";
		       }
		    result = foo();
		         """,
		    context );
		assertThat( variables.dereference( result, false ) ).isEqualTo( "value" );
		Struct meta = ( ( UDF ) variables.dereference( foo, false ) ).getMetaData();

		assertThat( meta.dereference( Key.of( "name" ), false ) ).isEqualTo( "foo" );
		assertThat( meta.dereference( Key.of( "hint" ), false ) ).isEqualTo( "my UDF" );
		assertThat( meta.dereference( Key.of( "output" ), false ) ).isEqualTo( false );
		assertThat( meta.dereference( Key.of( "brad" ), false ) ).isEqualTo( "wood" );
		assertThat( meta.dereference( Key.of( "returnType" ), false ) ).isEqualTo( "String" );
		assertThat( meta.dereference( Key.of( "access" ), false ) ).isEqualTo( "public" );

		Array args = ( ( Array ) meta.dereference( Key.of( "parameters" ), false ) );
		assertThat( args.size() ).isEqualTo( 2 );

		Struct param1 = ( Struct ) args.get( 0 );
		assertThat( param1.dereference( Key.of( "name" ), false ) ).isEqualTo( "param1" );
		assertThat( param1.dereference( Key.of( "hint" ), false ) ).isEqualTo( "My param" );
		assertThat( param1.dereference( Key.of( "required" ), false ) ).isEqualTo( true );
		assertThat( param1.dereference( Key.of( "type" ), false ) ).isEqualTo( "any" );

		Struct param2 = ( Struct ) args.get( 1 );
		assertThat( param2.dereference( Key.of( "name" ), false ) ).isEqualTo( "param2" );
		assertThat( param2.dereference( Key.of( "luis" ), false ) ).isEqualTo( "majano" );
		assertThat( param2.dereference( Key.of( "hint" ), false ) ).isEqualTo( "" );
		assertThat( param2.dereference( Key.of( "required" ), false ) ).isEqualTo( false );
		assertThat( param2.dereference( Key.of( "type" ), false ) ).isEqualTo( "numeric" );

	}

	@DisplayName( "UDF metadata javadoc alt" )
	@Test
	public void testUDFMetadataJavadocAlt() {

		instance.executeSource(
		    """
		      /**
		       * my UDF
		       *
		       * @param1 My param
		       * @param1.required
		       *
		       * @param1.type string
		       * @param2.default 42
		       * @param2.type numeric
		       * @param2.luis majano
		       */
		       public String function foo( param1, param2 ) output=false brad="wood" {
		         return "value";
		       }
		    result = foo();
		          """,
		    context );
		assertThat( variables.dereference( result, false ) ).isEqualTo( "value" );
		Struct meta = ( ( UDF ) variables.dereference( foo, false ) ).getMetaData();

		assertThat( meta.dereference( Key.of( "name" ), false ) ).isEqualTo( "foo" );
		assertThat( meta.dereference( Key.of( "hint" ), false ) ).isEqualTo( "my UDF" );
		assertThat( meta.dereference( Key.of( "output" ), false ) ).isEqualTo( false );
		assertThat( meta.dereference( Key.of( "brad" ), false ) ).isEqualTo( "wood" );
		assertThat( meta.dereference( Key.of( "returnType" ), false ) ).isEqualTo( "String" );
		assertThat( meta.dereference( Key.of( "access" ), false ) ).isEqualTo( "public" );

		Array args = ( ( Array ) meta.dereference( Key.of( "parameters" ), false ) );
		assertThat( args.size() ).isEqualTo( 2 );

		Struct param1 = ( Struct ) args.get( 0 );
		assertThat( param1.dereference( Key.of( "name" ), false ) ).isEqualTo( "param1" );
		assertThat( param1.dereference( Key.of( "hint" ), false ) ).isEqualTo( "My param" );
		assertThat( param1.dereference( Key.of( "required" ), false ) ).isEqualTo( true );
		assertThat( param1.dereference( Key.of( "type" ), false ) ).isEqualTo( "any" );

		Struct param2 = ( Struct ) args.get( 1 );
		assertThat( param2.dereference( Key.of( "name" ), false ) ).isEqualTo( "param2" );
		assertThat( param2.dereference( Key.of( "luis" ), false ) ).isEqualTo( "majano" );
		assertThat( param2.dereference( Key.of( "hint" ), false ) ).isEqualTo( "" );
		assertThat( param2.dereference( Key.of( "required" ), false ) ).isEqualTo( false );
		assertThat( param2.dereference( Key.of( "type" ), false ) ).isEqualTo( "numeric" );

	}

	@DisplayName( "positional arguments" )
	@Test
	public void testPositionalArguments() {

		instance.executeSource(
		    """
		    result = {};
		       function foo( param1, param2 ) {
		       	result = arguments;
		       }
		       foo( 'value1', 'value2', "value3", "value4" );
		       """,
		    context );

		ArgumentsScope argsScope = ( ArgumentsScope ) variables.dereference( Key.of( "result" ), false );
		assertThat( argsScope.size() ).isEqualTo( 4 );
		Object[] args = argsScope.asArray();
		assertThat( args[ 0 ] ).isEqualTo( "value1" );
		assertThat( args[ 1 ] ).isEqualTo( "value2" );
		assertThat( args[ 2 ] ).isEqualTo( "value3" );
		assertThat( args[ 3 ] ).isEqualTo( "value4" );

		List<String> keys = argsScope.getKeys();
		assertThat( keys.get( 0 ) ).isEqualTo( "param1" );
		assertThat( keys.get( 1 ) ).isEqualTo( "param2" );
		assertThat( keys.get( 2 ) ).isEqualTo( "3" );
		assertThat( keys.get( 3 ) ).isEqualTo( "4" );

	}

	@DisplayName( "named arguments equals" )
	@Test
	public void testNamedArgumentsEquals() {

		instance.executeSource(
		    """
		    result = {};
		       function foo( param1, param2 ) {
		       	result = arguments;
		       }
		       foo( param1='value1', param2='value2', param3="value3", param4="value4" );
		       """,
		    context );

		ArgumentsScope argsScope = ( ArgumentsScope ) variables.dereference( Key.of( "result" ), false );
		assertThat( argsScope.size() ).isEqualTo( 4 );
		Object[] args = argsScope.asArray();
		assertThat( args[ 0 ] ).isEqualTo( "value1" );
		assertThat( args[ 1 ] ).isEqualTo( "value2" );
		assertThat( args[ 2 ] ).isEqualTo( "value3" );
		assertThat( args[ 3 ] ).isEqualTo( "value4" );

		List<String> keys = argsScope.getKeys();
		assertThat( keys.get( 0 ) ).isEqualTo( "param1" );
		assertThat( keys.get( 1 ) ).isEqualTo( "param2" );
		assertThat( keys.get( 2 ) ).isEqualTo( "param3" );
		assertThat( keys.get( 3 ) ).isEqualTo( "param4" );

	}

	@DisplayName( "named arguments colon" )
	@Test
	public void testNamedArgumentsColon() {

		instance.executeSource(
		    """
		    result = {};
		       function foo( param1, param2 ) {
		       	result = arguments;
		       }
		       foo( param1:'value1', param2 : 'value2', param3 :"value3", param4: "value4" );
		       """,
		    context );

		ArgumentsScope argsScope = ( ArgumentsScope ) variables.dereference( Key.of( "result" ), false );
		assertThat( argsScope.size() ).isEqualTo( 4 );
		Object[] args = argsScope.asArray();
		assertThat( args[ 0 ] ).isEqualTo( "value1" );
		assertThat( args[ 1 ] ).isEqualTo( "value2" );
		assertThat( args[ 2 ] ).isEqualTo( "value3" );
		assertThat( args[ 3 ] ).isEqualTo( "value4" );

		List<String> keys = argsScope.getKeys();
		assertThat( keys.get( 0 ) ).isEqualTo( "param1" );
		assertThat( keys.get( 1 ) ).isEqualTo( "param2" );
		assertThat( keys.get( 2 ) ).isEqualTo( "param3" );
		assertThat( keys.get( 3 ) ).isEqualTo( "param4" );

	}

	@DisplayName( "argument collection" )
	@Test
	public void testArgumentCollection() {

		instance.executeSource(
		    """
		        result = {};
		           function foo( param1, param2 ) {
		           	result = arguments;
		           }
		           foo(
		    	argumentCollection={
		    		param1:'value1',
		    		param2 : 'value2',
		    		param3 :"value3",
		    		param4: "value4"
		    	}
		    );
		      """,
		    context );

		ArgumentsScope argsScope = ( ArgumentsScope ) variables.dereference( Key.of( "result" ), false );
		assertThat( argsScope.size() ).isEqualTo( 4 );
		Object[] args = argsScope.asArray();
		assertThat( args[ 0 ] ).isEqualTo( "value1" );
		assertThat( args[ 1 ] ).isEqualTo( "value2" );
		assertThat( args[ 2 ] ).isEqualTo( "value3" );
		assertThat( args[ 3 ] ).isEqualTo( "value4" );

		List<String> keys = argsScope.getKeys();
		assertThat( keys.get( 0 ) ).isEqualTo( "param1" );
		assertThat( keys.get( 1 ) ).isEqualTo( "param2" );
		assertThat( keys.get( 2 ) ).isEqualTo( "param3" );
		assertThat( keys.get( 3 ) ).isEqualTo( "param4" );

	}

}