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
package ortus.boxlang.runtime.types;

import ortus.boxlang.runtime.context.FunctionBoxContext;
import ortus.boxlang.runtime.scopes.Key;

public class SampleUDF extends UDF {

    Object returnVal = null;

    public SampleUDF( Access access, Key name, String returnType, Argument[] arguments, String hint, boolean output,
        Object returnVal ) {
        super( access, name, returnType, arguments, hint, output );
        this.returnVal = returnVal;
    }

    @Override
    public Object invoke( FunctionBoxContext context ) {
        return ensureReturnType( returnVal );
    }
}