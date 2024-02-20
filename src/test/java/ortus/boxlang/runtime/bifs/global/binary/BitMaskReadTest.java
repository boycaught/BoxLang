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
package ortus.boxlang.runtime.bifs.global.binary;

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
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class BitMaskReadTest {

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

	@DisplayName( "Bitwise Mask Read operation with positive integers" )
	@Test
	public void testBitwiseMaskReadWithPositiveIntegers() {
		instance.executeSource( "result = bitMaskRead(15, 1, 3);", context );
		assertThat( variables.get( result ) ).isEqualTo( 7 );
	}

	@DisplayName( "Bitwise Mask Read operation with negative integers" )
	@Test
	public void testBitwiseMaskReadWithNegativeIntegers() {
		instance.executeSource( "result = bitMaskRead(-5, 1, 2);", context );
		assertThat( variables.get( result ) ).isEqualTo( 1 );
	}

	@DisplayName( "Bitwise Mask Read operation with zero" )
	@Test
	public void testBitwiseMaskReadWithZero() {
		instance.executeSource( "result = bitMaskRead(0, 2, 4);", context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );
	}

	@DisplayName( "Bitwise Mask Read operation with large integers" )
	@Test
	public void testBitwiseMaskReadWithLargeIntegers() {
		instance.executeSource( "result = bitMaskRead(123456789, 4, 10);", context );
		assertThat( variables.get( result ) ).isEqualTo( 209 );
	}

	@DisplayName( "Bitwise Mask Read operation with invalid length" )
	@Test
	public void testBitwiseMaskReadWithInvalidLength() {
		assertThrows( BoxRuntimeException.class, () -> instance.executeSource( "result = bitMaskRead(5, 2, 32);", context ) );
	}

	@DisplayName( "Bitwise Mask Read operation with invalid start" )
	@Test
	public void testBitwiseMaskReadWithInvalidStart() {
		assertThrows( BoxRuntimeException.class, () -> instance.executeSource( "result = bitMaskRead(5, 32, 3);", context ) );
	}
}
