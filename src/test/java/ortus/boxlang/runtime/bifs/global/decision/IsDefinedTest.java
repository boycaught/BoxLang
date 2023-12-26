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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

@Disabled( "Unimplemented" )
public class IsDefinedTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IScope		variables;

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
		context		= new ScriptingBoxContext( instance.getRuntimeContext() );
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

	@DisplayName( "It detects binary values" )
	@Test
	public void testTrueConditions() {
		instance.executeSource(
		    """
		       var result = true;
		    variableName = "result";
		    variables.foo = "bar";

		    stringVarName     = isDefined( "result" );
		    variableReference = isDefined( variableName );
		    localReference    = isDefined( "local.result" );
		    variableScope     = isDefined( "variables.foo" );
		       """,
		    context );
		assertThat( ( Boolean ) variables.dereference( Key.of( "stringVarName" ), false ) ).isTrue();
		assertThat( ( Boolean ) variables.dereference( Key.of( "variableReference" ), false ) ).isTrue();
		assertThat( ( Boolean ) variables.dereference( Key.of( "localReference" ), false ) ).isTrue();
		assertThat( ( Boolean ) variables.dereference( Key.of( "variableScope" ), false ) ).isTrue();
	}

	@DisplayName( "It returns false for non-binary values" )
	@Test
	public void testFalseConditions() {
		instance.executeSource(
		    """
		    variableName = "result";
		    variables.foo = "bar";

		    stringVarName     = isDefined( "doesntexist" );
		    variableReference = isDefined( variableName );
		    localReference    = isDefined( "local.result" );
		    variableScope     = isDefined( "variables.foo" );
		       """,
		    context );
		assertThat( ( Boolean ) variables.dereference( Key.of( "stringVarName" ), false ) ).isFalse();
		assertThat( ( Boolean ) variables.dereference( Key.of( "variableReference" ), false ) ).isFalse();
		assertThat( ( Boolean ) variables.dereference( Key.of( "localReference" ), false ) ).isFalse();
		assertThat( ( Boolean ) variables.dereference( Key.of( "variableScope" ), false ) ).isFalse();
	}

}
