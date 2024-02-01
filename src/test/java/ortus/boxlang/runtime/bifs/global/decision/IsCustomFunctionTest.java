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

package ortus.boxlang.runtime.bifs.global.decision;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class IsCustomFunctionTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IScope		variables;

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@AfterAll
	public static void teardown() {
		instance.shutdown();
	}

	@BeforeEach
	public void setupEach() {
		variables.clear();
	}

	@DisplayName( "It detects custom functions" )
	@Test
	public void testTrueConditions() {
		instance.executeSource(
		    """
		    closure = isCustomFunction( function(){} );
		    arrowFunction = isCustomFunction( () => {} );

		    myFunc = function() {};
		    functionReference = isCustomFunction( myFunc );
		       """,
		    context
		);
		assertThat( ( Boolean ) variables.get( Key.of( "closure" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "arrowFunction" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "functionReference" ) ) ).isTrue();
	}

	@DisplayName( "It returns false for non-custom functions" )
	@Test
	public void testFalseConditions() {
		instance.executeSource(
		    """
		    anInteger = isCustomFunction( 123 );
		    aString = isCustomFunction( "abc" );
		       """,
		    context );
		assertThat( ( Boolean ) variables.get( Key.of( "anInteger" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "aString" ) ) ).isFalse();
	}

	@Disabled( "Lucee's results do not match our own; determine the proper behavior" )
	@DisplayName( "It supports Lucee's type parameter" )
	@Test
	public void testTypeParameter() {
		instance.executeSource(
		    """
		    isLambdaAUDFType = isCustomFunction( () => {}, "udf" );
		    isLambdaAClosureType = isCustomFunction( () => {}, "closure" );
		    isLambdaALambdaType = isCustomFunction( () => {}, "lambda" );

		    function myUDF(){};
		    isUDFaLambdaType = isCustomFunction( myUDF, "lambda" );
		    isUDFaClosureType = isCustomFunction( myUDF, "closure" );
		    isUDFaUDFType = isCustomFunction( myUDF, "udf" );

		    isClosureaLambdaType = isCustomFunction( function(){}, "lambda" );
		    isClosureaUDFType = isCustomFunction( function(){}, "udf" );
		    isClosureaClosureType = isCustomFunction( function(){}, "closure" );
		       """,
		    context );
		assertThat( ( Boolean ) variables.get( Key.of( "isLambdaAUDFType" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "isLambdaAClosureType" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "isLambdaALambdaType" ) ) ).isTrue();

		assertThat( ( Boolean ) variables.get( Key.of( "isUDFaLambdaType" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "isUDFaClosureType" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "isUDFaUDFType" ) ) ).isTrue();

		assertThat( ( Boolean ) variables.get( Key.of( "isClosureaLambdaType" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "isClosureaUDFType" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "isClosureaClosureType" ) ) ).isTrue();
	}

	@DisplayName( "It validates the type parameter" )
	@Test
	public void testTypeParameterValidation() {
		assertThrows( Throwable.class, () -> {
			instance.executeSource(
			    """
			    result = isCustomFunction( () => {}, "brad" );
			    """,
			    context );
		} );
	}
}
