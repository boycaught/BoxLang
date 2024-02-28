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
package ortus.boxlang.runtime.types.util;

import static com.google.common.truth.Truth.assertThat;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;

class BLCollectorTest {

	@Test
	void testToArray() {

		Array result = Stream.of( "string1", "string2", "string3" ).collect( BLCollector.toArray() );
		assertThat( result.size() ).isEqualTo( 3 );
		assertThat( result.get( 0 ) ).isEqualTo( "string1" );
		assertThat( result.get( 1 ) ).isEqualTo( "string2" );
		assertThat( result.get( 2 ) ).isEqualTo( "string3" );

	}

	@Test
	void testToArrayIntStream() {

		Array result = IntStream.of( 1, 2, 3, 4, 5 ).boxed().collect( BLCollector.toArray() );
		assertThat( result.size() ).isEqualTo( 5 );
		assertThat( result.get( 0 ) ).isEqualTo( 1 );
		assertThat( result.get( 1 ) ).isEqualTo( 2 );
		assertThat( result.get( 2 ) ).isEqualTo( 3 );
		assertThat( result.get( 3 ) ).isEqualTo( 4 );
		assertThat( result.get( 4 ) ).isEqualTo( 5 );

	}

	@Test
	void testToStruct() {

		IStruct result = Struct.of( "brad", "wood", "luis", "majano", "jon", "clausen" ).entrySet().stream().collect( BLCollector.toStruct() );
		assertThat( result.size() ).isEqualTo( 3 );
		assertThat( result.get( "brad" ) ).isEqualTo( "wood" );
		assertThat( result.get( "luis" ) ).isEqualTo( "majano" );
		assertThat( result.get( "jon" ) ).isEqualTo( "clausen" );

	}

	@Test
	void testToStructType() {

		IStruct result = Struct.of( "brad", "wood", "luis", "majano", "jon", "clausen" ).entrySet().stream()
		    .collect( BLCollector.toStruct( IStruct.TYPES.LINKED ) );
		assertThat( result.size() ).isEqualTo( 3 );
		assertThat( result.getType() ).isEqualTo( IStruct.TYPES.LINKED );
		assertThat( result.get( "brad" ) ).isEqualTo( "wood" );
		assertThat( result.get( "luis" ) ).isEqualTo( "majano" );
		assertThat( result.get( "jon" ) ).isEqualTo( "clausen" );

	}

}
