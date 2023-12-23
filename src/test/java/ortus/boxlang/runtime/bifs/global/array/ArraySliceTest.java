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

public class ArraySliceTest {

    static BoxRuntime  instance;
    static IBoxContext context;
    static IScope      variables;
    static Key         result = new Key( "result" );

    @BeforeAll
    public static void setUp() {
        instance  = BoxRuntime.getInstance( true );
        context   = new ScriptingBoxContext( instance.getRuntimeContext() );
        variables = context.getScopeNearby( VariablesScope.name );
    }

    @AfterAll
    public static void teardown() {
        instance.shutdown();
    }

    @BeforeEach
    public void setupEach() {
        variables.clear();
    }

    @DisplayName( "It should return a sublist starting from start and including the whole array" )
    @Test
    public void testSubListNoOffset() {
        instance.executeSource(
            """
                      nums = [ 1, 2, 3, 4, 5 ];
                      result = nums.slice( 2 );
            """,
            context );
        Array found = ( Array ) variables.dereference( result, false );
        assertThat( found.size() ).isEqualTo( 4 );
        assertThat( found.get( 0 ) ).isEqualTo( 2 );
        assertThat( found.get( 1 ) ).isEqualTo( 3 );
        assertThat( found.get( 2 ) ).isEqualTo( 4 );
        assertThat( found.get( 3 ) ).isEqualTo( 5 );
    }

    @DisplayName( "It should return a sublist starting from start and including start + offset" )
    @Test
    public void testSubListWithOffset() {
        instance.executeSource(
            """
                      nums = [ 1, 2, 3, 4, 5 ];
                      result = nums.slice( 2, 3 );
            """,
            context );
        Array found = ( Array ) variables.dereference( result, false );
        assertThat( found.size() ).isEqualTo( 3 );
        assertThat( found.get( 0 ) ).isEqualTo( 2 );
        assertThat( found.get( 1 ) ).isEqualTo( 3 );
        assertThat( found.get( 2 ) ).isEqualTo( 4 );
    }

    @DisplayName( "It should accept a negative offset" )
    @Test
    public void testNegativeOffset() {
        instance.executeSource(
            """
                      nums = [ 1, 2, 3, 4, 5 ];
                      result = nums.slice( 2, -2 );
            """,
            context );
        Array found = ( Array ) variables.dereference( result, false );
        assertThat( found.size() ).isEqualTo( 2 );
        assertThat( found.get( 0 ) ).isEqualTo( 2 );
        assertThat( found.get( 1 ) ).isEqualTo( 3 );
    }

    @DisplayName( "It should accept a negative start" )
    @Test
    public void testNegativeStart() {
        instance.executeSource(
            """
                      nums = [ 1, 2, 3, 4, 5 ];
                      result = nums.slice( -3, 1 );
            """,
            context );
        Array found = ( Array ) variables.dereference( result, false );
        assertThat( found.size() ).isEqualTo( 1 );
        assertThat( found.get( 0 ) ).isEqualTo( 2 );
    }

    @DisplayName( "It should accept a negative start and negative offset" )
    @Test
    public void testNegativeStartAndNegativeOffset() {
        instance.executeSource(
            """
                      nums = [ 1, 2, 3, 4, 5 ];
                      result = nums.slice( -3, -2 );
            """,
            context );
        Array found = ( Array ) variables.dereference( result, false );
        assertThat( found.size() ).isEqualTo( 2 );
        assertThat( found.get( 0 ) ).isEqualTo( 2 );
        assertThat( found.get( 1 ) ).isEqualTo( 3 );
    }
}
