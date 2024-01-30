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
package ortus.boxlang.debugger.event;

import ortus.boxlang.debugger.ISendable;

public class Event implements ISendable {

	public String	type	= "event";
	public String	event;

	public Event( String event ) {
		this.event = event;
	}

	/**
	 * Gets the type of the debug protocl message. Always "event".
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Gets the name of the event
	 */
	@Override
	public String getName() {
		return event;
	}
}
