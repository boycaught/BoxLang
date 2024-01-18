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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.bifs.MemberDescriptor;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.IReferenceable;
import ortus.boxlang.runtime.interop.DynamicJavaInteropService;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.services.FunctionService;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.meta.BoxMeta;
import ortus.boxlang.runtime.types.meta.GenericMeta;
import ortus.boxlang.runtime.util.LocalizationUtil;

public class DateTime implements IType, IReferenceable {

	/**
	 * --------------------------------------------------------------------------
	 * Private Properties
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Represents the wrapped ZonedDateTime object we enhance
	 */
	protected ZonedDateTime					wrapped;

	/**
	 * The format we use to represent the date time
	 * which defaults to the ODBC format: {ts '''yyyy-MM-dd HH:mm:ss'''}
	 */
	private DateTimeFormatter				formatter						= DateTimeFormatter.ofPattern( TS_FORMAT_MASK );

	/**
	 * Function service
	 */
	private static final FunctionService	functionService					= BoxRuntime.getInstance().getFunctionService();

	/**
	 * --------------------------------------------------------------------------
	 * Public Properties
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Formatters
	 */
	// This mask matches the Lucee default - @TODO ISO would be a better default - can we change this
	public static final String				TS_FORMAT_MASK					= "'{ts '''yyyy-MM-dd HH:mm:ss'''}'";
	public static final String				DEFAULT_DATE_FORMAT_MASK		= "dd-MMM-yy";
	public static final String				DEFAULT_TIME_FORMAT_MASK		= "HH:mm a";
	public static final String				DEFAULT_DATETIME_FORMAT_MASK	= "dd-MMM-yyyy HH:mm:ss";
	public static final String				ISO_DATE_TIME_MILIS_FORMAT_MASK	= "yyyy-MM-dd'T'HH:mm:ss.SSS";
	// The ODBC default format masks - no millis
	public static final String				ODBC_DATE_TIME_FORMAT_MASK		= "yyyy-MM-dd HH:mm:ss";
	public static final String				ODBC_DATE_FORMAT_MASK			= "yyyy-MM-dd";
	public static final String				ODBC_TIME_FORMAT_MASK			= "HH:mm:ss";

	public static final String				MODE_DATE						= "Date";
	public static final String				MODE_TIME						= "Time";
	public static final String				MODE_DATETIME					= "DateTime";

	public static final Struct				commonFormatters				= new Struct(
	    new HashMap<String, DateTimeFormatter>() {

		    {
			    put( "fullDateTime", DateTimeFormatter.ofLocalizedDateTime( FormatStyle.FULL, FormatStyle.FULL ) );
			    put( "longDateTime", DateTimeFormatter.ofLocalizedDateTime( FormatStyle.LONG, FormatStyle.LONG ) );
			    put( "mediumDateTime", DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM, FormatStyle.MEDIUM ) );
			    put( "shortDateTime", DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM, FormatStyle.MEDIUM ) );
			    put( "ISODateTime", DateTimeFormatter.ISO_DATE_TIME );
			    put( "ISO8601DateTime", DateTimeFormatter.ISO_OFFSET_DATE_TIME );
			    put( "ODBCDateTime", DateTimeFormatter.ofPattern( ODBC_DATE_TIME_FORMAT_MASK ) );
			    put( "fullDate", DateTimeFormatter.ofLocalizedDate( FormatStyle.FULL ) );
			    put( "longDate", DateTimeFormatter.ofLocalizedDate( FormatStyle.LONG ) );
			    put( "mediumDate", DateTimeFormatter.ofLocalizedDate( FormatStyle.MEDIUM ) );
			    put( "shortDate", DateTimeFormatter.ofLocalizedDate( FormatStyle.SHORT ) );
			    put( "ISODate", DateTimeFormatter.ISO_DATE );
			    put( "ISO8601Date", DateTimeFormatter.ISO_DATE );
			    put( "ODBCDate", DateTimeFormatter.ofPattern( ODBC_DATE_FORMAT_MASK ) );
			    put( "fullTime", DateTimeFormatter.ofLocalizedTime( FormatStyle.FULL ) );
			    put( "longTime", DateTimeFormatter.ofLocalizedTime( FormatStyle.LONG ) );
			    put( "mediumTime", DateTimeFormatter.ofLocalizedTime( FormatStyle.MEDIUM ) );
			    put( "shortTime", DateTimeFormatter.ofLocalizedTime( FormatStyle.SHORT ) );
			    put( "ISOTime", DateTimeFormatter.ISO_TIME );
			    put( "ISO8601Time", DateTimeFormatter.ISO_TIME );
			    put( "ODBCTime", DateTimeFormatter.ofPattern( ODBC_TIME_FORMAT_MASK ) );
		    }
	    }
	);

	/**
	 * Metadata object
	 */
	public BoxMeta							$bx;

	/**
	 * --------------------------------------------------------------------------
	 * Constructors
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Constructor to create default DateTime representing the current instance and the default timezone
	 */
	public DateTime() {
		this( ZoneId.systemDefault() );
	}

	/**
	 * Constructor to create DateTime with a timezone
	 *
	 * @param zoneId The timezone to use
	 */
	public DateTime( ZoneId zoneId ) {
		this( ZonedDateTime.of( LocalDateTime.now(), zoneId ) );
	}

	/**
	 * Constructor to create DateTime from a ZonedDateTime object
	 *
	 * @param dateTime A zoned date time object
	 */
	public DateTime( ZonedDateTime dateTime ) {
		this.wrapped = dateTime;
	}

	/**
	 * Constructor to create DateTime from a Instant
	 *
	 * @param dateTime A zoned date time object
	 */
	public DateTime( Instant instant ) {
		this.wrapped = ZonedDateTime.ofInstant( instant, ZoneId.systemDefault() );
	}

	/**
	 * Constructor to create DateTime from a time string and a mask
	 *
	 * @param dateTime - a string representing the date and time
	 * @param mask     - a string representing the mask
	 */
	public DateTime( String dateTime, String mask ) {
		this( dateTime, mask, ZoneId.systemDefault() );
	}

	/**
	 * Constructor to create DateTime from a time string and a mask
	 *
	 * @param dateTime - a string representing the date and time
	 * @param mask     - a string representing the mask
	 */
	public DateTime( String dateTime, String mask, ZoneId timezone ) {
		ZonedDateTime parsed = null;
		// try parsing if it fails then our time does not contain timezone info so we fall back to a local zoned date
		try {
			parsed = ZonedDateTime.parse( dateTime, getFormatter( mask ) );
		} catch ( java.time.format.DateTimeParseException e ) {
			// First fallback - it has a time without a zone
			try {
				parsed = ZonedDateTime.of( LocalDateTime.parse( dateTime, getFormatter( mask ) ), timezone );
				// Second fallback - it is only a date and we need to supply a time
			} catch ( java.time.format.DateTimeParseException x ) {
				try {
					parsed = ZonedDateTime.of( LocalDateTime.of( LocalDate.parse( dateTime, getFormatter( mask ) ), LocalTime.MIN ), timezone );
					// last fallback - this is a time only value
				} catch ( java.time.format.DateTimeParseException z ) {
					parsed = ZonedDateTime.of( LocalDate.MIN, LocalTime.parse( dateTime, getFormatter( mask ) ), timezone );
				}
			} catch ( Exception x ) {
				throw new BoxRuntimeException(
				    String.format(
				        "The the date time value of [%s] could not be parsed as a valid date or datetime",
				        dateTime
				    ), x );
			}
		} catch ( Exception e ) {
			throw new BoxRuntimeException(
			    String.format(
			        "The the date time value of [%s] could not be parsed using the mask [%s]",
			        dateTime,
			        mask
			    ), e );
		}
		this.wrapped = parsed;
	}

	/**
	 * Constructor to create DateTime from a string, using the system locale and timezone
	 *
	 * @param dateTime - a string representing the date and time
	 */
	public DateTime( String dateTime ) {
		this( dateTime, ZoneId.systemDefault() );
	}

	/**
	 * Constructor to create DateTime from a string with a specified timezone, using the system locale
	 *
	 * @param dateTime - a string representing the date and time
	 * @param timezone - the timezone string
	 */
	public DateTime( String dateTime, ZoneId timezone ) {
		this( dateTime, Locale.getDefault(), timezone );
	}

	/**
	 * Constructor to create DateTime from a datetime string from a specific locale and timezone
	 *
	 * @param dateTime - a string representing the date and time
	 * @param locale   - a locale object used to assist in parsing the string
	 * @param timezone The timezone to assign to the string, if an offset or zone is not provided in the value
	 */
	public DateTime( String dateTime, Locale locale, ZoneId timezone ) {
		ZonedDateTime parsed = null;
		this.formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME.withLocale( locale );
		// try parsing if it fails then our time does not contain timezone info so we fall back to a local zoned date
		try {
			parsed = ZonedDateTime.parse( dateTime, LocalizationUtil.getLocaleZonedDateTimeParsers( locale ) );
		} catch ( java.time.format.DateTimeParseException e ) {
			// First fallback - it has a time without a zone
			try {
				parsed = ZonedDateTime.of( LocalDateTime.parse( dateTime, LocalizationUtil.getLocaleDateTimeParsers( locale ) ),
				    timezone );
				// Second fallback - it is only a date and we need to supply a time
			} catch ( java.time.format.DateTimeParseException x ) {
				try {
					parsed = ZonedDateTime.of(
					    LocalDateTime.of( LocalDate.parse( dateTime, LocalizationUtil.getLocaleDateParsers( locale ) ), LocalTime.MIN ),
					    timezone );
					// last fallback - this is a time only value
				} catch ( java.time.format.DateTimeParseException z ) {
					parsed = ZonedDateTime.of( LocalDate.MIN, LocalTime.parse( dateTime, LocalizationUtil.getLocaleTimeParsers( locale ) ),
					    ZoneId.systemDefault() );
				}
			} catch ( Exception x ) {
				throw new BoxRuntimeException(
				    String.format(
				        "The the date time value of [%s] could not be parsed as a valid date or datetimea locale of [%s]",
				        dateTime,
				        locale.getDisplayName()
				    ), x );
			}
		} catch ( Exception e ) {
			throw new BoxRuntimeException(
			    String.format(
			        "The the date time value of [%s] could not be parsed with a locale of [%s]",
			        dateTime,
			        locale.getDisplayName()
			    ), e );
		}
		this.wrapped = parsed;
	}

	/**
	 * Constructor to create DateTime from a numerics through millisecond
	 *
	 * @param year         The year
	 * @param month        The month
	 * @param day          The day
	 * @param hour         The hour
	 * @param minute       The minute
	 * @param second       The second
	 * @param milliseconds The milliseconds
	 * @param timezone     The timezone
	 */
	public DateTime(
	    Integer year,
	    Integer month,
	    Integer day,
	    Integer hour,
	    Integer minute,
	    Integer second,
	    Integer milliseconds,
	    String timezone ) {
		this(
		    ZonedDateTime.of(
		        year,
		        month,
		        day,
		        hour,
		        minute,
		        second,
		        milliseconds * 1000000,
		        ( timezone != null ) ? ZoneId.of( timezone ) : ZoneId.systemDefault()
		    )
		);
	}

	/**
	 * Constructor to create DateTime from a numerics through day in the default timezone
	 *
	 * @param year  The year
	 * @param month The month
	 * @param day   The day
	 */
	public DateTime(
	    Integer year,
	    Integer month,
	    Integer day ) {
		this( year, month, day, null );
	}

	/**
	 * Constructor to create DateTime from a numerics through day with a timezone
	 *
	 * @param year     The year
	 * @param month    The month
	 * @param day      The day
	 * @param timezone The timezone
	 */
	public DateTime(
	    Integer year,
	    Integer month,
	    Integer day,
	    String timezone ) {
		this(
		    ZonedDateTime.of(
		        year,
		        month,
		        day,
		        0,
		        0,
		        0,
		        0,
		        ( timezone != null ) ? ZoneId.of( timezone ) : ZoneId.systemDefault()
		    )
		);
	}

	/**
	 * --------------------------------------------------------------------------
	 * Convenience methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Returns a DateTime formatter from a pattern passed in
	 *
	 * @param pattern the pattern to use
	 *
	 * @return the DateTimeFormatter object with the pattern
	 */
	private static DateTimeFormatter getFormatter( String pattern ) {
		return DateTimeFormatter.ofPattern( pattern );
	}

	/**
	 * Chainable member function to set the format and return the object
	 *
	 * @param mask the formatting mask to use
	 *
	 * @return
	 */
	public DateTime setFormat( String mask ) {
		this.formatter = DateTimeFormatter.ofPattern( mask );
		return this;
	}

	/**
	 * Alternate format setter which accepts a DateTimeFormatter object
	 *
	 * @param formatter A DateTimeFormatter instance
	 *
	 * @return
	 */
	public DateTime setFormat( DateTimeFormatter formatter ) {
		this.formatter = formatter;
		return this;
	}

	/**
	 * --------------------------------------------------------------------------
	 * IType Interface Methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Interface method to return the string representation
	 **/
	public String asString() {
		return toString();
	}

	/**
	 * Represent as string, or throw exception if not possible
	 *
	 * @return The string representation
	 */
	public BoxMeta getBoxMeta() {
		if ( this.$bx == null ) {
			this.$bx = new GenericMeta( this );
		}
		return this.$bx;
	}

	/**
	 * --------------------------------------------------------------------------
	 * Type Helper Methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Returns the hashcode of the wrapped object
	 */
	@Override
	public int hashCode() {
		return Objects.hash( wrapped, formatter );
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param obj The reference object with which to compare.
	 *
	 * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
	 */
	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null || getClass() != obj.getClass() )
			return false;
		DateTime other = ( DateTime ) obj;
		return Objects.equals( wrapped, other.wrapped ) &&
		    Objects.equals( formatter, other.formatter );
	}

	/**
	 * Returns the datetime representation as a string
	 **/
	@Override
	public String toString() {
		return this.formatter.format( this.wrapped );
	}

	/*
	 * Clones this object to produce a new object
	 */
	public DateTime clone() {
		return clone( this.wrapped.getZone() );
	}

	/**
	 * Determines whether the year of this object is a leap year
	 *
	 * @return boolean
	 */
	public Boolean isLeapYear() {
		return Year.isLeap( this.wrapped.getYear() );
	}

	/**
	 * Clones this object to produce a new object
	 *
	 * @param timezone the string timezone to cast the clone to
	 */
	public DateTime clone( ZoneId timezone ) {
		return new DateTime( ZonedDateTime.ofInstant( this.wrapped.toInstant(), timezone != null ? timezone : this.wrapped.getZone() ) );
	}

	/**
	 * Returns the date time representation as a string in the specified format mask
	 *
	 * @param mask the formatting mask to use
	 *
	 * @return the date time representation as a string in the specified format mask
	 */
	public String format( String mask ) {
		return this.format( Locale.getDefault(), mask );
	}

	/**
	 * Returns the date time representation as a string with the provided formatter
	 *
	 * @param formatter The DateTimeFormatter instance
	 *
	 * @return the date time representation as a string in the specified format mask
	 */
	public String format( DateTimeFormatter formatter ) {
		return this.wrapped.format( formatter );
	}

	/**
	 * Returns the date time representation as a string in the specified locale
	 *
	 * @param mask the formatting mask to use
	 *
	 * @return the date time representation as a string in the specified format mask
	 */
	public String format( Locale locale, String mask ) {
		if ( mask == null ) {
			return this.wrapped.format( DateTimeFormatter.ofLocalizedDateTime( FormatStyle.LONG, FormatStyle.LONG ).withLocale( locale ) );
		} else {
			return this.format( getDateTimeFormatter( mask ).withLocale( locale ) );
		}
	}

	/**
	 * Returns the date time representation as a string in the specified format mask
	 *
	 * @return
	 */
	public String toISOString() {
		this.formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		return toString();
	}

	/**
	 * Returns this date time as an instant
	 *
	 * @return An instant representing this date time
	 */
	public Instant toInstant() {
		return this.wrapped.toInstant();
	}

	/**
	 * Returns this date time in epoch time ( seconds )
	 *
	 * @return The epoch time in seconds
	 */
	public Long toEpoch() {
		return this.wrapped.toEpochSecond();
	}

	/**
	 * Returns this date time in epoch milliseconds
	 *
	 * @return The epoch time in milliseconds
	 */
	public Long toEpochMillis() {
		return this.wrapped.toInstant().toEpochMilli();
	}

	/**
	 * Allows the date object to be modified by a convention of unit ( datepart ) and quantity. Supports the DateAdd BIF
	 *
	 * @param unit     - an abbreviation for a time unit
	 *                 d/y - day
	 *                 m - month
	 *                 yyyy - year
	 *                 w - weekdays
	 *                 ww - weeks
	 *                 h - hours
	 *                 n - minutes
	 *                 s - seconds
	 *                 l - milliseconds
	 * @param quantity a positive or negative quantity of the unit to modify the DateTime
	 *
	 * @return the DateTime instance
	 */
	public DateTime modify( String unit, Long quantity ) {
		switch ( unit ) {
			case "d" :
			case "y" :
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusDays( quantity ) : wrapped.minusDays( Math.abs( quantity ) );
				break;
			case "yyyy" :
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusYears( quantity ) : wrapped.minusYears( Math.abs( quantity ) );
				break;
			case "q" :
				Long multiplier = 3l;
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusMonths( quantity * multiplier ) : wrapped.minusMonths( Math.abs( quantity ) * multiplier );
				break;
			case "m" :
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusMonths( quantity ) : wrapped.minusMonths( Math.abs( quantity ) );
				break;
			case "w" :
				Integer dayOfWeek = wrapped.getDayOfWeek().getValue();
				switch ( dayOfWeek ) {
					case 5 :
						quantity = quantity + 2l;
						break;
					case 6 :
						quantity = quantity + 1l;
				}
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusDays( quantity ) : wrapped.minusDays( Math.abs( quantity ) );
				break;
			case "ww" :
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusWeeks( quantity ) : wrapped.minusWeeks( Math.abs( quantity ) );
				break;
			case "h" :
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusHours( quantity ) : wrapped.minusHours( Math.abs( quantity ) );
				break;
			case "n" :
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusMinutes( quantity ) : wrapped.minusMinutes( Math.abs( quantity ) );
				break;
			case "s" :
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plusSeconds( quantity ) : wrapped.minusSeconds( Math.abs( quantity ) );
				break;
			case "l" :
				wrapped = Long.signum( quantity ) == 1 ? wrapped.plus( quantity, ChronoUnit.MILLIS ) : wrapped.minus( Math.abs( quantity ), ChronoUnit.MILLIS );
				break;
		}
		return this;
	}

	/**
	 * Sets the timezone of the current wrapped date time
	 *
	 * @param timeZone The string representation of the timezone; e.g. "America/New_York", "UTC", "Asia/Tokyo" etc.
	 *
	 * @return The new DateTime object with the timezone set
	 */
	public DateTime setTimezone( String timeZone ) {
		return setTimezone( ZoneId.of( timeZone ) );
	}

	/**
	 * Sets the timezone of the current wrapped date time using a ZoneId object
	 *
	 * @param zoneId The ZoneId object to use
	 *
	 * @return The new DateTime object with the timezone set
	 */
	public DateTime setTimezone( ZoneId zoneId ) {
		this.wrapped = wrapped.withZoneSameLocal( zoneId );
		return this;
	}

	/**
	 * Get's the original wrapped ZonedDateTime object
	 *
	 * @return The original wrapped ZonedDateTime object
	 */
	public ZonedDateTime getWrapped() {
		return this.wrapped;
	}

	/**
	 * Parses a locale from a string
	 */
	public static Locale getParsedLocale( String locale ) {
		Locale localeObj = null;
		if ( locale != null ) {
			var		localeParts	= locale.split( "-|_| " );
			String	ISOLang		= localeParts[ 0 ];
			String	ISOCountry	= null;
			if ( localeParts.length > 1 ) {
				ISOCountry = localeParts[ 1 ];
			}
			localeObj = ISOCountry == null ? new Locale( ISOLang ) : new Locale( ISOLang, ISOCountry );
		} else {
			localeObj = Locale.getDefault();
		}
		return localeObj;
	}

	/**
	 * --------------------------------------------------------------------------
	 * IReferenceable Interface Methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Assign a value to a key
	 *
	 * @param key   The key to assign
	 * @param value The value to assign
	 */
	@Override
	public Object assign( IBoxContext context, Key key, Object value ) {
		DynamicJavaInteropService.setField( this, key.getName().toLowerCase(), value );
		return this;
	}

	/**
	 * Dereference this object by a key and return the value, or throw exception
	 *
	 * @param key  The key to dereference
	 * @param safe Whether to throw an exception if the key is not found
	 *
	 * @return The requested object
	 */
	@Override
	public Object dereference( IBoxContext context, Key key, Boolean safe ) {
		try {
			return DynamicJavaInteropService.getField( this, key.getName().toLowerCase() ).get();
		} catch ( NoSuchElementException e ) {
			throw new BoxRuntimeException(
			    String.format(
			        "The property [%s] does not exist or is not public in the class [%s].",
			        key.getName(),
			        this.getClass().getSimpleName()
			    )
			);
		}
	}

	/**
	 * Dereference this object by a key and invoke the result as an invokable (UDF, java method) using positional arguments
	 *
	 * @param name                The key to dereference
	 * @param positionalArguments The positional arguments to pass to the invokable
	 * @param safe                Whether to throw an exception if the key is not found
	 *
	 * @return The requested object
	 */
	public Object dereferenceAndInvoke( IBoxContext context, Key name, Object[] positionalArguments, Boolean safe ) {
		MemberDescriptor memberDescriptor = functionService.getMemberMethod( name, BoxLangType.DATETIME );
		if ( memberDescriptor != null ) {
			return memberDescriptor.invoke( context, this, positionalArguments );
		}

		if ( DynamicJavaInteropService.hasMethodNoCase( this.getClass(), name.getName() ) ) {
			return DynamicJavaInteropService.invoke( this, name.getName(), safe, positionalArguments );
		} else if ( DynamicJavaInteropService.hasMethodNoCase( this.wrapped.getClass(), name.getName() ) ) {
			return DynamicJavaInteropService.invoke( this.wrapped, name.getName(), safe, positionalArguments );
		} else if ( DynamicJavaInteropService.hasMethodNoCase( this.getClass(), "get" + name.getName() ) ) {
			return DynamicJavaInteropService.invoke( this.wrapped, "get" + name.getName(), safe, positionalArguments );
		} else {
			throw new BoxRuntimeException(
			    String.format(
			        "The method [%s] is not present in the [%s] object",
			        name.getName(),
			        this.getClass().getSimpleName()
			    )
			);
		}
	}

	/**
	 * Dereference this object by a key and invoke the result as an invokable (UDF, java method)
	 *
	 * @param name           The name of the key to dereference, which becomes the method name
	 * @param namedArguments The arguments to pass to the invokable
	 * @param safe           If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested return value or null
	 */
	public Object dereferenceAndInvoke( IBoxContext context, Key name, Map<Key, Object> namedArguments, Boolean safe ) {

		MemberDescriptor memberDescriptor = functionService.getMemberMethod( name, BoxLangType.DATETIME );
		if ( memberDescriptor != null ) {
			return memberDescriptor.invoke( context, this, namedArguments );
		}
		if ( DynamicJavaInteropService.hasMethodNoCase( this.getClass(), name.getName() ) ) {
			return DynamicJavaInteropService.invoke( this, name.getName(), safe, namedArguments );
			// no args - just pass through to the wrapped methods
		} else if ( DynamicJavaInteropService.hasMethodNoCase( this.wrapped.getClass(), name.getName() ) ) {
			return DynamicJavaInteropService.invoke( this.wrapped, name.getName(), safe );
		} else if ( DynamicJavaInteropService.hasMethodNoCase( this.getClass(), "get" + name.getName() ) ) {
			return DynamicJavaInteropService.invoke( this.wrapped, "get" + name.getName(), safe );
		} else {
			throw new BoxRuntimeException(
			    String.format(
			        "The method [%s] is not present in the [%s] object",
			        name.getName(),
			        this.getClass().getSimpleName()
			    )
			);
		}
	}

	/**
	 * Convienience method to create a formatter with a specific pattern - will look up an equivalent known DateTime formatter
	 *
	 * @param mask
	 *             q
	 *
	 * @return
	 */
	private static DateTimeFormatter getDateTimeFormatter( String mask ) {
		Key formatKey = Key.of( mask + MODE_DATETIME );
		return DateTime.commonFormatters.containsKey( formatKey )
		    ? ( DateTimeFormatter ) DateTime.commonFormatters.get( formatKey )
		    : DateTimeFormatter.ofPattern( mask );
	}
}
