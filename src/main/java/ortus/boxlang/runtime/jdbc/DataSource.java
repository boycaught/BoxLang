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
package ortus.boxlang.runtime.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

/**
 * Encapsulates a datasource configuration and connection pool, providing methods for executing queries (transactionally or single) on the datasource.
 * <p>
 * <strong>Warning:</strong> Datasource configuration is currently case-sensitive. This will be fixed in a future release. Refer to the
 * {@link <a href="https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby">HikariCP
 * configuration docs</a>} for a list of valid configuration properties:
 *
 * @TODO:
 *        <ul>
 *        <li>Move all JDBC classes to a JDBC module to allow a leaner, lighter-weight BoxLang Core.
 *        <li>Implement parameterized queries with PreparedStatement.</li>
 *        <li>Allow setting isolation levels, connection timeouts, and other ad-hoc connection settings at query time</li>
 *        <li>Potentially re-enable Driver-based configuration for constructing a JDBC URL from individual driver/host/port/username/password
 *        properties.</li>
 *        <li>Add support for case-insensitive keys in the properties struct. Java.util.Properties is case-sensitive, so using this method to
 *        configure HikariConfig with dynamic configuration means that a connection attempt with `JDBCurl: 'jdbc:derby:foo'` will fail with a
 *        '"jdbcUrl" propertty is required"' message.</li>
 *        <li>Return Query values by default from the execute methods.
 *        <li>
 *        <li>Handle multiple return types in the `execute()` and `executeTransactionally()` methods.
 *        <ul>
 *        <li>"query": returns a query object</li>
 *        <li>"array_of_entity": returns an array of ORM entities (requires dbtype to be "hql")</li>
 *        <li>"array": returns an array of structs</li>
 *        <li>"struct": returns a struct of structs (requires columnkey to be defined).</li>
 *        </ul>
 *        </li>
 *        <li>Add support for return values in the `executeTransactionally()` methods.
 *        <li>
 */
public class DataSource {

	/**
	 * Underlying HikariDataSource object, used in connection pooling.
	 */
	private HikariDataSource hikariDataSource;

	/**
	 * Configure and initialize a new DataSourceRecord object from a struct of properties.
	 *
	 * @see https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby
	 *
	 * @param properties Struct of properties for configuring the datasource. Be aware that the struct keys are case-sensitive and must match the Hikari
	 *                   configuration property names. (We'll be adding support for case-insensitive keys in the near future.)
	 *
	 * @return
	 */
	public DataSource( IStruct config ) {
		Properties properties = new Properties();
		config.forEach( ( key, value ) -> properties.setProperty( key.getName(), ( String ) value ) );

		HikariConfig hikariConfig = new HikariConfig( properties );
		this.hikariDataSource = new HikariDataSource( hikariConfig );

	}

	/**
	 * Get a connection to the configured datasource.
	 *
	 * @return A JDBC connection to the configured datasource.
	 *
	 * @throws SQLException if connection could not be established.
	 */
	public Connection getConnection() {
		try {
			return hikariDataSource.getConnection();
		} catch ( SQLException e ) {
			// @TODO: Recast as BoxSQLException?
			throw new BoxRuntimeException( "Unable to open connection:", e );
		}
	}

	/**
	 * Shut down the datasource, including the connection pool and all connections.
	 *
	 * @return This DataSource object, which is now shut down and useless for any further operations.
	 */
	public DataSource shutdown() {
		hikariDataSource.close();
		return this;
	}

	/**
	 * Execute a query on the connection, using a connection from the connection pool which is autoclosed upon query completion.
	 *
	 * @param query The SQL query to execute.
	 *
	 * @return An array of Structs, each representing a row of the result set (if any). If there are no results (say, for an UPDATE statement), an empty
	 *         array is returned.
	 */
	public Struct[] execute( String query ) {
		try ( Connection conn = getConnection(); ) {
			return execute( query, conn );
		} catch ( SQLException e ) {
			throw new BoxRuntimeException( "Unable to close connection:", e );
		}
	}

	/**
	 * Execute a query on the connection, using the provided connection.
	 * <p>
	 * Note the connection passed in is NOT closed automatically. It is up to the caller to close the connection when they are done with it. If you want
	 * an automanaged, i.e. autoclosed connection, use the <code>execute(String)</code> method.
	 *
	 * @param query The SQL query to execute.
	 *
	 * @return An array of Structs, each representing a row of the result set (if any). If there are no results (say, for an UPDATE statement), an empty
	 *         array is returned.
	 */
	public Struct[] execute( String query, Connection conn ) {
		try ( Statement stmt = conn.createStatement() ) {
			// @TODO: Implement parameterized queries with PreparedStatement.
			boolean hasResult = stmt.execute( query );

			if ( hasResult ) {
				// Move to an abstract result processing method which looks at the query `returnType` option
				ResultSet		resultSet	= stmt.getResultSet();
				List<IStruct>	result		= new ArrayList<>();
				while ( resultSet.next() ) {
					IStruct				row			= new Struct();
					ResultSetMetaData	metaData	= resultSet.getMetaData();
					int					columnCount	= metaData.getColumnCount();
					for ( int i = 1; i <= columnCount; i++ ) {
						String	columnName	= metaData.getColumnName( i );
						Object	columnValue	= resultSet.getObject( i );
						row.put( columnName, columnValue );
					}
					result.add( row );
				}
				return result.toArray( new Struct[ 0 ] );
			} else {
				return new Struct[ 0 ];
			}
		} catch ( SQLException e ) {
			throw new BoxRuntimeException( "Unable to execute query:", e );
		}
	}

	/**
	 * Begin a transaction on the connection. (i.e. acquire a transaction object for further operations)
	 */
	public void executeTransactionally( String[] query ) {
		try ( Connection conn = getConnection() ) {
			executeTransactionally( query, conn );
		} catch ( SQLException e ) {
			throw new BoxRuntimeException( "Unable to close connection:", e );
		}
	}

	/**
	 * Execute a series of statements in a transaction.
	 * <p>
	 * Note the connection passed in is NOT closed automatically. It is up to the caller to close the connection when they are done with it. If you want
	 * an automanaged, i.e. autoclosed transaction, use the <code>executeTransactionally(String[])</code> method.
	 *
	 * @param query An array of SQL statements to execute in the transaction.
	 * @param conn  The connection to execute the transaction on. A connection is required - use <code>executeTransactionally(String[])</code> if you
	 *              don't wish to provide one.
	 */
	public void executeTransactionally( String[] query, Connection conn ) {
		try {
			conn.setAutoCommit( false );
			for ( String sql : query ) {
				try ( var stmt = conn.createStatement() ) {
					// @TODO: Flip between this for vanilla SQL and PreparedStatement for parameterized queries.
					stmt.execute( sql );

					// @TODO: Process the ResultSet, i.e. stmt.getResultSet()
					// ResultSet rs = stmt.getResultSet();
				}
			}
			conn.commit();
		} catch ( SQLException e ) {
			BoxRuntimeException bre = new BoxRuntimeException( "Error in transaction", e );
			// @TODO: Rolling back the transaction is a good idea... right?
			try {
				conn.rollback();
			} catch ( SQLException e2 ) {
				// keep our original exception as the "cause" so we're not obscuring upstream exceptions.
				bre = new BoxRuntimeException( "Error rolling back transaction", bre );
			}
			throw bre;
		} finally {
			try {
				conn.setAutoCommit( true );
			} catch ( SQLException e ) {
				throw new BoxRuntimeException( "Unable to re-enable autoCommit:", e );
			}
		}
	}

}
