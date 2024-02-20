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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class IsBooleanTest {

	static BoxRuntime	instance;
	IBoxContext			context;
	IScope				variables;

	@BeforeAll
	public static void setUp() {
		instance = BoxRuntime.getInstance( true );
	}

	@AfterAll
	public static void teardown() {

	}

	@BeforeEach
	public void setupEach() {
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@DisplayName( "It detects boolean values" )
	@Test
	public void testTrueConditions() {
		instance.executeSource(
		    """
		    trueValue        = isBoolean( true );
		    falseValue       = isBoolean( false );

		    stringTrue  = isBoolean( 'true' );
		    stringFalse = isBoolean( 'false' );

		    yes         = isBoolean( "yes" );
		    no          = isBoolean( "no" );

		    float       = isBoolean( 1.1 );
		    zero        = isBoolean( 0 );
		    twentythree = isBoolean( 23 );
		    """,
		    context );
		assertThat( ( Boolean ) variables.get( Key.of( "trueValue" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "falseValue" ) ) ).isTrue();

		assertThat( ( Boolean ) variables.get( Key.of( "stringTrue" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "stringFalse" ) ) ).isTrue();

		assertThat( ( Boolean ) variables.get( Key.of( "yes" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "no" ) ) ).isTrue();

		assertThat( ( Boolean ) variables.get( Key.of( "float" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "zero" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "twentythree" ) ) ).isTrue();
	}

	@DisplayName( "It returns false for non-boolean values" )
	@Test
	public void testFalseConditions() {
		instance.executeSource(
		    """
		    array       = isBoolean( [ true, false ] );

		    string      = isBoolean( "randomstring" );
		    struct      = isBoolean( {} );
		    """,
		    context );
		assertThat( ( Boolean ) variables.get( Key.of( "array" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "string" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "struct" ) ) ).isFalse();
	}

}
