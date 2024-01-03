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

package ortus.boxlang.runtime.bifs.global.array;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.Array;

public class ArrayPopTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IScope		variables;
	static Key			result	= new Key( "result" );

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

	@DisplayName( "It can pop an item off the array" )
	@Test
	public void testPop() {

		instance.executeSource(
		    """
		    a = [ "apple", "pear", "watermelon" ];
		       result = ArrayPop( a );
		       """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "watermelon" );
		assertThat( ( ( Array ) variables.get( "a" ) ).size() ).isEqualTo( 2 );
	}

	@DisplayName( "It can return a default value" )
	@Test
	public void testDefaultValue() {

		instance.executeSource(
		    """
		    a = [];
		       result = ArrayPop( a, "what" );
		       """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "what" );
		assertThat( ( ( Array ) variables.get( "a" ) ).size() ).isEqualTo( 0 );
	}

	@DisplayName( "It be invoked as a member function" )
	@Test
	public void testMemberInvocation() {

		instance.executeSource(
		    """
		    a = [ "apple", "pear", "watermelon" ];
		       result = a.pop();
		       """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "watermelon" );
		assertThat( ( ( Array ) variables.get( "a" ) ).size() ).isEqualTo( 2 );
	}

}
