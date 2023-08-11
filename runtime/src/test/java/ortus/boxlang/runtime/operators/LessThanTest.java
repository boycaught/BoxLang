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
package ortus.boxlang.runtime.operators;

import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LessThanTest {

	@DisplayName( "It can compare strings" )
	@Test
	void testItCanCompareStrings() {
		assertThat( LessThan.invoke( "A", "B" ) ).isTrue();
	}

	@DisplayName( "It can compare numbers" )
	@Test
	void testItCanCompareNumbers() {
		assertThat( LessThan.invoke( 1, 2 ) ).isTrue();
		assertThat( LessThan.invoke( 1.5, 1.7 ) ).isTrue();
	}

	@DisplayName( "It can compare strings as numbers" )
	@Test
	void testItCanCompareStringsAsNumbers() {
		assertThat( LessThan.invoke( "1", "2" ) ).isTrue();
		assertThat( LessThan.invoke( "1.5", "1.7" ) ).isTrue();
		assertThat( LessThan.invoke( "1.5000", "1.7" ) ).isTrue();
	}

}
