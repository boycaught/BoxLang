/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ortus.boxlang.runtime.bifs.global.binary;

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

public class BitShTest {

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

	@DisplayName( "Bitwise shift-left operation with positive integers" )
	@Test
	public void testBitwiseShlnWithPositiveIntegers() {
		instance.executeSource( "result = bitShln(5, 1);", context );
		assertThat( variables.get( result ) ).isEqualTo( 10 );
	}

	@DisplayName( "Bitwise shift-left operation with negative integers" )
	@Test
	public void testBitwiseShlnWithNegativeIntegers() {
		instance.executeSource( "result = bitShln(-5, 2);", context );
		assertThat( variables.get( result ) ).isEqualTo( -20 );
	}

	@DisplayName( "Bitwise shift-left operation with zero" )
	@Test
	public void testBitwiseShlnWithZero() {
		instance.executeSource( "result = bitShln(0, 5);", context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );
	}

	@DisplayName( "Bitwise shift-left operation with large integers" )
	@Test
	public void testBitwiseShlnWithLargeIntegers() {
		instance.executeSource( "result = bitShln(123456789, 2);", context );
		assertThat( variables.get( result ) ).isEqualTo( 493827156 );
	}

	// Test bitwise shift-right operations
	@DisplayName( "Bitwise shift-right operation with positive integers" )
	@Test
	public void testBitwiseShrnWithPositiveIntegers() {
		instance.executeSource( "result = bitShrn(5, 1);", context );
		assertThat( variables.get( result ) ).isEqualTo( 2 );
	}

	@DisplayName( "Bitwise shift-right operation with negative integers" )
	@Test
	public void testBitwiseShrnWithNegativeIntegers() {
		instance.executeSource( "result = bitShrn(-5, 2);", context );
		assertThat( variables.get( result ) ).isEqualTo( 1073741822 );
	}

	@DisplayName( "Bitwise shift-right operation with zero" )
	@Test
	public void testBitwiseShrnWithZero() {
		instance.executeSource( "result = bitShrn(0, 5);", context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );
	}

	@DisplayName( "Bitwise shift-right operation with large integers" )
	@Test
	public void testBitwiseShrnWithLargeIntegers() {
		instance.executeSource( "result = bitShrn(123456789, 2);", context );
		assertThat( variables.get( result ) ).isEqualTo( 30864197 );
	}

}
