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
package ortus.boxlang.runtime.config.segments;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import ortus.boxlang.runtime.types.Struct;

/**
 * A BoxLang cache configuration
 */
public class CacheConfig {

	/**
	 * The name of the cache engine
	 */
	@JsonProperty( "name" )
	private String	name		= "default";

	/**
	 * The default cache engine type
	 */
	@JsonProperty( "type" )
	private String	type		= "Caffeine";

	/**
	 * The properties for the cache engine
	 */
	@JsonProperty( "properties" )
	private Struct	properties	= new Struct();

	public String getType() {
		return type;
	}

	public void setType( String type ) {
		this.type = type;
	}

	public Struct getProperties() {
		return properties;
	}

	public void setProperties( Struct properties ) {
		this.properties = properties;
	}
}
