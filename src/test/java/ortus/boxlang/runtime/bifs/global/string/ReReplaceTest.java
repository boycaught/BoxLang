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

package ortus.boxlang.runtime.bifs.global.string;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class ReReplaceTest {

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

	@Test
	public void testReplaceOnce() {
		instance.executeSource(
		    """
		    result = ReReplace( "test 123!", "[^a-z0-9]", '', "once" );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "test123!" );
	}

	@Test
	public void testReplaceAll() {
		instance.executeSource(
		    """
		    result = ReReplace( "test 123!", "[^a-z0-9]", '', "all" );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "test123" );
	}

	@Test
	public void testReplaceOnceMember() {
		instance.executeSource(
		    """
		    result = "test 123!".ReReplace( "[^a-z0-9]", '', "once" );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "test123!" );
	}

	@Test
	public void testReplaceAllMember() {
		instance.executeSource(
		    """
		    result = "test 123!".ReReplace( "[^a-z0-9]", '', "all" );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "test123" );
	}

}
