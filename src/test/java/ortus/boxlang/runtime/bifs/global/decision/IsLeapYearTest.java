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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class IsLeapYearTest {

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

	@DisplayName( "It detects leap years" )
	@Test
	public void testTrueConditions() {
		instance.executeSource(
		    """
		    // returns true in both engines
		    anInteger          = isLeapYear( 2024 );
		    aStringInteger     = isLeapYear( "2024" );
		    aFloat             = isLeapYear( .2024 );
		    anotherFloat       = isLeapYear( 20.24 );
		    aTwoDigitInteger   = isLeapYear( 20 );
		    aStringFloat       = isLeapYear( "20.24" );
		       """,
		    context );
		assertThat( ( Boolean ) variables.get( Key.of( "anInteger" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "aStringInteger" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "aFloat" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "anotherFloat" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "aTwoDigitInteger" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "aStringFloat" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "aStringFloat" ) ) ).isTrue();
	}

	@Test
	public void testCenturyTurns() {
		instance.executeSource(
		    """
		    sixteenhundred     = isLeapYear( 1600 );
		    nineteenhundred    = isLeapYear( 1900 );
		    twothousand        = isLeapYear( 2000 );
		    """,
		    context );
		assertThat( ( Boolean ) variables.get( Key.of( "sixteenhundred" ) ) ).isTrue();
		assertThat( ( Boolean ) variables.get( Key.of( "nineteenhundred" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "twothousand" ) ) ).isTrue();
	}

	@DisplayName( "It returns false for non-leap years" )
	@Test
	public void testFalseConditions() {
		instance.executeSource(
		    """
		    // returns false in both engines
		    anIntegerNonLeapYear = isLeapYear( 2023 );
		    aStringNonLeapYear = isLeapYear( "2021" );
		       """,
		    context );
		assertThat( ( Boolean ) variables.get( Key.of( "anIntegerNonLeapYear" ) ) ).isFalse();
		assertThat( ( Boolean ) variables.get( Key.of( "aStringNonLeapYear" ) ) ).isFalse();
	}

	@DisplayName( "It throws on non-numeric values" )
	@Test
	public void testException() {
		// throws in ACF and Lucee
		assertThrows( Throwable.class, () -> instance.executeSource( "isLeapYear( 'brad2024' );", context ) );
	}

}
