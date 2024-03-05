package TestCases.debugger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.jr.ob.JSONObjectException;

import ortus.boxlang.debugger.AdapterProtocolMessageReader;
import ortus.boxlang.debugger.IAdapterProtocolMessage;
import ortus.boxlang.runtime.types.util.JSONUtil;

public class DebugMessages {

	public static interface TriConsumer<A, B, C> {

		public void accept( A a, B b, C c );
	}

	public static <A, B, C> TriConsumer<byte[], ByteArrayInputStream, AdapterProtocolMessageReader> readMessageStep( List<Map<String, Object>> messages ) {
		return ( a, b, reader ) -> {
			try {
				IAdapterProtocolMessage	message	= reader.read();
				int						i		= 0;

				while ( message != null ) {
					if ( i >= messages.size() ) {
						messages.add( message.getRawMessageData() );
					}
					i++;
					message = reader.read();
				}

			} catch ( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
	}

	public static <A, B, C> TriConsumer<byte[], ByteArrayInputStream, AdapterProtocolMessageReader> delayStep( long delay ) {
		return ( a, b, reader ) -> {
			try {
				Thread.sleep( delay );
			} catch ( InterruptedException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
	}

	public static <A, B, C> TriConsumer<byte[], ByteArrayInputStream, AdapterProtocolMessageReader> sendMessageStep( Map<String, Object> map ) {
		return ( byteArray, inputStream, reader ) -> {

			// clear buffer
			for ( int i = 0; i < byteArray.length; i++ ) {
				byteArray[ i ] = 0;
			}

			String jsonMessage;
			try {
				jsonMessage = JSONUtil.getJSONBuilder().asString( map );

				// @formatter:off
				// prettier-ignore
				String protocolMessage = String.format("""
Content-Length: %d
				
%s""", jsonMessage.getBytes().length, jsonMessage );
				byte[] messageBytes = protocolMessage.getBytes();

				// write message to byte array
				int offset = byteArray.length - messageBytes.length - 1;
				for ( int i = 0; i < messageBytes.length; i++ ) {
					byteArray[ i + offset ] = messageBytes[ i ];
				}

				inputStream.reset();
				inputStream.skip( offset );

			} catch ( JSONObjectException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch ( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
	}

	public static Map<String, Object> getInitRequest( int seq ) {
		Map<String, Object> initializeRequest = new HashMap<String, Object>();
		initializeRequest.put( "command", "initialize" );
		initializeRequest.put( "type", "request" );
		initializeRequest.put( "seq", seq );
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put( "clientID", "vscode" );
		arguments.put( "clientName", "Visual Studio Code" );
		arguments.put( "adapterID", "boxlang" );
		arguments.put( "pathFormat", "path" );
		arguments.put( "linesStartAt1", true );
		arguments.put( "columnsStartAt1", true );
		arguments.put( "supportsVariableType", true );
		arguments.put( "supportsVariablePaging", true );
		arguments.put( "supportsRunInTerminalRequest", true );
		arguments.put( "locale", "en" );
		arguments.put( "supportsProgressReporting", true );
		arguments.put( "supportsInvalidatedEvent", true );
		arguments.put( "supportsMemoryReferences", true );
		arguments.put( "supportsArgsCanBeInterpretedByShell", true );
		arguments.put( "supportsMemoryEvent", true );
		arguments.put( "supportsStartDebuggingRequest", true );
		initializeRequest.put( "arguments", arguments );

		return initializeRequest;
	}

	public static Map<String, Object> getLaunchRequest( int seq ) {
		Map<String, Object> launchRequest = new HashMap<String, Object>();
		launchRequest.put( "command", "launch" );
		launchRequest.put( "type", "request" );
		launchRequest.put( "seq", seq );
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put( "type", "boxlang" );
		arguments.put( "request", "launch" );
		arguments.put( "name", "Run BoxLang Program" );
		arguments.put( "program", Paths.get( "src/test/java/TestCases/debugger/main.cfs" ).toAbsolutePath().toString() );
		arguments.put( "__configurationTarget", 6 );
		arguments.put( "__sessionId", "0e92688e-8cc0-47d6-ad83-74978ec3798f" );
		launchRequest.put( "arguments", arguments );

		return launchRequest;
	}

	public static Map<String, Object> getSetBreakpointsRequest( int seq ) {
		Map<String, Object> request = new HashMap<String, Object>();
		request.put( "command", "setBreakpoints" );
		request.put( "type", "request" );
		request.put( "seq", seq );

		Map<String, Object> arguments = new HashMap<String, Object>();
		request.put( "arguments", arguments );

		Map<String, Object> source = new HashMap<String, Object>();
		arguments.put( "source", source );
		source.put( "name", "main.cfs" );
		source.put( "path", Paths.get( "src/test/java/TestCases/debugger/main.cfs" ).toAbsolutePath().toString() );

		arguments.put( "lines", new int[] { 4 } );
		Map<String, Object> lineMap = new HashMap<String, Object>();
		lineMap.put( "line", 4 );
		arguments.put( "breakpoints", new Map[] { lineMap } );

		return request;
	}
	
	/*
	 * {"command":"configurationDone","type":"request","seq":6}
	 */
	public static Map<String, Object> getConfigurationDoneRequest( int seq ) {
		Map<String, Object> request = new HashMap<String, Object>();
		request.put( "command", "configurationDone" );
		request.put( "type", "request" );
		request.put( "seq", seq );

		return request;
	}
}
