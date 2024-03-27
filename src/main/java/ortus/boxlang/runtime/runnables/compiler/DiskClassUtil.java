/**
 * [BoxLang]
 * <p>
 * Copyright [2023] [Ortus Solutions, Corp]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ortus.boxlang.runtime.runnables.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.util.JSONUtil;

/**
 * Contains some utilities for working with non-class files in the class generation dir
 */
public class DiskClassUtil {

	/**
	 * The location of the disk store
	 */
	private Path diskStore;

	/**
	 * Constructor
	 *
	 * @param diskStore disk store location path
	 */
	public DiskClassUtil( Path diskStore ) {
		this.diskStore = diskStore;
	}

	/**
	 * Check if a JSON exists on disk
	 *
	 * @param name class name
	 *
	 * @return true if JSON exists on disk
	 */
	public boolean hasLineNumbers( String name ) {
		return generateDiskpath( name, "json" ).toFile().exists();
	}

	private Path generateDiskpath( String name, String extension ) {
		return Paths.get( diskStore.toString(), name.replace( ".", File.separator ) + "." + extension );
	}

	public void writeLineNumbers( String fqn, String lineNumberJSON ) {
		if ( lineNumberJSON == null ) {
			return;
		}

		writeBytes( fqn, "json", lineNumberJSON.getBytes() );
	}

	/**
	 * Read line numbers.
	 *
	 * @returns array of maps. Null if not found.
	 */
	@SuppressWarnings( "unchecked" )
	public SourceMap readLineNumbers( String fqn ) {
		if ( !hasLineNumbers( fqn ) ) {
			return null;
		}
		Path diskPath = generateDiskpath( fqn, "json" );
		try {
			String json = new String( Files.readAllBytes( diskPath ) );
			return JSONUtil.fromJSON( SourceMap.class, json );
		} catch ( IOException e ) {
			throw new BoxRuntimeException( "Unable to read line number JSON file from disk", e );
		}
	}

	/**
	 * This is really just for debugging purposes. It's not used in production.
	 *
	 * @param fqn        The fully qualified name of the class
	 * @param javaSource The Java source code
	 */
	public void writeJavaSource( String fqn, String javaSource ) {
		writeBytes( fqn, "java", javaSource.getBytes() );
	}

	/**
	 * Write a file to the directory configured for BoxLang
	 *
	 * @param fqn        The fully qualified name of the class
	 * @param extension  The extension of the file
	 * @param javaSource The Java source code
	 */
	public void writeBytes( String fqn, String extension, byte[] bytes ) {
		Path diskPath = generateDiskpath( fqn, extension );
		diskPath.toFile().getParentFile().mkdirs();
		try {
			Files.write( diskPath, bytes );
		} catch ( IOException e ) {
			throw new BoxRuntimeException( "Unable to write Java Sourece file to disk", e );
		}
	}

}
