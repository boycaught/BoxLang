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
package ortus.boxlang.runtime.testing;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class ExamplesTest {

	@DisplayName( "It can execute Phase1 example" )
	@Test
	public void testItCanExecutePhase1() throws Throwable {
		Phase1.main( new String[] {
		} );
	}

	@DisplayName( "It can execute Phase1 try/catch example" )
	@Test
	public void testItCanExecutePhase1TryCatch() throws Throwable {
		Phase1TryCatch.main( new String[] {
		} );
	}

	@DisplayName( "It can execute Phase1 switch example" )
	@Test
	public void testItCanExecutePhase1Switch() throws Throwable {
		Phase1Switch.main( new String[] {
		} );
	}

	@DisplayName( "It can execute Phase2 UDF example" )
	@Test
	public void testItCanExecutePhase2UDF() throws Throwable {
		Phase2UDF.main( new String[] {
		} );
	}

	@DisplayName( "It can execute Phase2 Closure example" )
	@Test
	public void testItCanExecutePhase2Closure() throws Throwable {
		Phase2Closure.main( new String[] {
		} );
	}

	@DisplayName( "It can execute Phase2 Lambda example" )
	@Test
	public void testItCanExecutePhase2Lambda() throws Throwable {
		Phase2Lambda.main( new String[] {
		} );
	}

}
