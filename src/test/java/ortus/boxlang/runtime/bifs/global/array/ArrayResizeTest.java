
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
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.IStruct;

public class ArrayResizeTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IStruct		variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
		context		= new ScriptingBoxContext( instance.getRuntimeContext() );
		variables	= ( IStruct ) context.getScopeNearby( VariablesScope.name );
	}

	@AfterAll
	public static void teardown() {
		instance.shutdown();
	}

	@BeforeEach
	public void setupEach() {
		variables.clear();
	}

	@DisplayName( "It should grow an array using nulls" )
	@Test
	public void testGrow() {
		instance.executeSource(
		    """
		    nums = [];
		    result = ArrayResize( nums, 3 );
		      """,
		    context );
		Array arr = variables.getAsArray( Key.of( "nums" ) );
		assertThat( variables.get( result ) ).isEqualTo( true );
		assertThat( arr.size() ).isEqualTo( 3 );
		assertThat( arr.get( 0 ) ).isNull();
		assertThat( arr.get( 1 ) ).isNull();
		assertThat( arr.get( 2 ) ).isNull();
	}

	@DisplayName( "It should not grow an array that is already bigger than the provided size" )
	@Test
	public void testBigger() {
		instance.executeSource(
		    """
		    nums = [ 1, 2, 3, 4, 5 ];
		    result = ArrayResize( nums, 3 );
		      """,
		    context );
		Array arr = variables.getAsArray( Key.of( "nums" ) );
		assertThat( variables.get( result ) ).isEqualTo( true );
		assertThat( arr.size() ).isEqualTo( 5 );
		assertThat( arr.get( 0 ) ).isEqualTo( 1 );
		assertThat( arr.get( 1 ) ).isEqualTo( 2 );
		assertThat( arr.get( 2 ) ).isEqualTo( 3 );
		assertThat( arr.get( 3 ) ).isEqualTo( 4 );
		assertThat( arr.get( 4 ) ).isEqualTo( 5 );
	}

	@DisplayName( "It should allow member invocation" )
	@Test
	public void testMemberInvocation() {
		instance.executeSource(
		    """
		    nums = [];
		    result = nums.resize( 3 );
		      """,
		    context );
		Array arr = variables.getAsArray( Key.of( "nums" ) );
		assertThat( variables.get( result ) ).isEqualTo( true );
		assertThat( arr.size() ).isEqualTo( 3 );
		assertThat( arr.get( 0 ) ).isNull();
		assertThat( arr.get( 1 ) ).isNull();
		assertThat( arr.get( 2 ) ).isNull();
	}

}
