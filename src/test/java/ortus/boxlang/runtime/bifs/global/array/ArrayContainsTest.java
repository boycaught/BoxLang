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

public class ArrayContainsTest {

	static BoxRuntime	instance;
	IBoxContext			context;
	IScope				variables;
	static Key			result	= new Key( "result" );

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

	@DisplayName( "It can search" )
	@Test
	public void testCanSearch() {

		instance.executeSource(
		    """
		    arr = [ 'a', 'b', 'c' ];
		    result = arrayContains( arr, 'b' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 2 );

		instance.executeSource(
		    """
		    arr = [ 'a', 'b', 'c' ];
		    result = arrayContains( arr, 'B' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );
	}

	@DisplayName( "Will throw an error if a UDF is used as the search" )
	@Test
	public void testCanSearchUDF() {
		assertThrows(
		    BoxRuntimeException.class,
		    () -> instance.executeSource(
		        """
		        arr = [ 'a', 'b', 'c' ];
		        result = arrayContains( arr, i->i=="b" );
		        """,
		        context )
		);
	}

	@DisplayName( "It can search member" )
	@Test
	public void testCanSearchMember() {

		instance.executeSource(
		    """
		    arr = [ 'a', 'b', 'c' ];
		    result = arr.contains( 'b' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 2 );

		instance.executeSource(
		    """
		    arr = [ 'a', 'b', 'c' ];
		    result = arr.contains( 'B' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );
	}

}
