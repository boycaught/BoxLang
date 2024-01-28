
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

package ortus.boxlang.runtime.bifs.global.list;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

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
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class ListGetAtTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IScope		variables;
	static Key			result	= new Key( "result" );

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

	@DisplayName( "It can retrieve an item at a specified index" )
	@Test
	public void testCanGet() {

		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listGetAt( list, 2 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "b" );
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listGetAt( list, 1 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "a" );
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listGetAt( list, 3 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "c" );

	}

	@DisplayName( "It can us a member function to retrieve the specified item" )
	@Test
	public void testMemberFunction() {

		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listGetAt( 2 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "b" );
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listGetAt( 1 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "a" );
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listGetAt( 3 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "c" );

	}

	@DisplayName( "It will throw an error with an invalid index" )
	@Test
	public void testCanSearch() {
		assertThrows(
		    BoxRuntimeException.class,
		    () -> instance.executeStatement( "listGetAt( '1,2', 3 )" )
		);
	}

}
