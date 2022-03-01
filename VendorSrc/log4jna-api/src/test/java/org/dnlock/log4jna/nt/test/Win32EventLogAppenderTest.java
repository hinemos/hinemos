/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License.  You may obtain a copy 
 * of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dnlock.log4jna.nt.test;

import static org.junit.Assert.*;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.dblock.log4jna.nt.Win32EventLogAppender;
import org.junit.Before;
import org.junit.Test;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;
import com.sun.jna.platform.win32.Advapi32Util.EventLogType;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;

/**
 * Test case for {@link Win32EventLogAppender}.
 * 
 * @author Curt Arnold
 * @author <a href="mailto:dblock@dblock.org">Daniel Doubrovkine</a>
 * @author <a href="mailto:tony@niemira.com">Tony Niemira</a>
 * @author <a href="mailto:claudio.trajtenberg@cgtca.ca">Claudio Trajtenberg</a>
 * 
 */
public class Win32EventLogAppenderTest {

	/**
	 * This must exist in the windows registry.
	 * <p>
	 * Key:
	 * </p>
	 * <p>
	 * <code>HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\EventLog\Application\Log4jnaTest</code>
	 * </p>
	 * <p>
	 * With entries:
	 * </p>
	 * 
	 * <pre>
	 * Name: <code>TypesSupported</code> 
	 * Type: <code>REG_DWORD</code> 
	 * Data: <code>0x7</code>
	 * 
	 * Name: <code>CategoryCount</code> 
	 * Type: <code>REG_DWORD</code> 
	 * Data: <code>0x6</code>
	 * </pre>
	 */
	private static final String TEST_LOGGER_NAME = "Log4jnaTest";

	/**
	 * The dll file location from the <code>src/test/resources</code> folder.
	 */
	private static String _eventLogAppenderDLL = Paths.get("src/test/resources/Win32EventlogAppender.dll")
			.toAbsolutePath().toString();

	/**
	 * Class under test
	 */
	private Win32EventLogAppender _eventLogAppender = null;

	/**
	 * 
	 */
	@Before
	public void setUp() {
		String source = null;
		String log = getClass().getName();
		Layout<? extends Serializable> layout = PatternLayout.newBuilder()
				.withPattern(PatternLayout.TTCC_CONVERSION_PATTERN).build();

		Filter filter = null;
		_eventLogAppender = Win32EventLogAppender.createAppender("appenderName", null, source, log, _eventLogAppenderDLL, _eventLogAppenderDLL, layout, filter);
		_eventLogAppender.setSource(TEST_LOGGER_NAME);
		_eventLogAppender.setApplication("Application");
//		_eventLogAppender.setCategoryMessageFile(_eventLogAppenderDLL);
//		_eventLogAppender.setEventMessageFile(_eventLogAppenderDLL);
	}

	/**
	 * Test case for {@link Win32EventLogAppender#append(LogEvent)} with debug
	 * level.
	 */
	@Test
	public void testDebugEvent() {
		String message = "log4jna debug message @ " + Kernel32.INSTANCE.GetTickCount();
		_eventLogAppender.append(asLogEvent(message, Level.DEBUG));
		expectEvent(message, Level.DEBUG, EventLogType.Informational);
	}

	/**
	 * Test case for {@link Win32EventLogAppender#append(LogEvent)} with info
	 * level.
	 */
	@Test
	public void testInfoEvent() {
		String message = "log4jna info message @ " + Kernel32.INSTANCE.GetTickCount();
		_eventLogAppender.append(asLogEvent(message, Level.INFO));
		expectEvent(message, Level.INFO, EventLogType.Informational);
	}

	/**
	 * Test case for {@link Win32EventLogAppender#append(LogEvent)} with warn
	 * level.
	 */
	@Test
	public void testWarnEvent() {
		String message = "log4jna warn message @ " + Kernel32.INSTANCE.GetTickCount();
		_eventLogAppender.append(asLogEvent(message, Level.WARN));
		expectEvent(message, Level.WARN, EventLogType.Warning);
	}

	/**
	 * Test case for {@link Win32EventLogAppender#append(LogEvent)} with fatal level.
	 */
	@Test
	public void testFatalEvent() {
		String message = "log4jna fatal message @ " + Kernel32.INSTANCE.GetTickCount();
		_eventLogAppender.append(asLogEvent(message, Level.FATAL));
		expectEvent(message, Level.FATAL, EventLogType.Error);
	}

	public void donttestRegistryValues() {
		String eventSourceKeyPath = "SYSTEM\\CurrentControlSet\\Services\\EventLog\\"
				+ _eventLogAppender.getApplication() + "\\" + _eventLogAppender.getSource();

		String eventMessageFileInRegistry = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
				eventSourceKeyPath, "EventMessageFile");

		Path eventMessageFileGiven = Paths.get(_eventLogAppenderDLL);
		assertEquals(eventMessageFileInRegistry, eventMessageFileGiven.toString());

		String categoryMessageFileInRegistry = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
				eventSourceKeyPath, "CategoryMessageFile");

		Path categoryMessageFileGiven = Paths.get(_eventLogAppenderDLL);
		assertEquals(categoryMessageFileInRegistry, categoryMessageFileGiven.toString());
	}

	private LogEvent asLogEvent(String message, Level level) {
		return new Log4jLogEvent.Builder().setLoggerName(TEST_LOGGER_NAME).setMarker(null)
				.setLoggerFqcn(_eventLogAppender.getClass().getName()).setLevel(level)
				.setMessage(new SimpleMessage(message)).setTimeMillis(System.currentTimeMillis()).build();
	}

	/*
	 * public void testException() { String message =
	 * "log4jna exception message @ " + Kernel32.INSTANCE.GetTickCount();
	 * _logger.debug(message, new Exception("testing exception"));
	 * expectEvent(message, Level.DEBUG, EventLogType.Informational); }
	 */

	private void expectEvent(String message, Level level, EventLogType eventLogType) {
		EventLogIterator iter = new EventLogIterator(null, TEST_LOGGER_NAME, WinNT.EVENTLOG_BACKWARDS_READ);
		try {
			assertTrue(iter.hasNext());
			EventLogRecord record = iter.next();
			assertEquals(TEST_LOGGER_NAME, record.getSource());

			assertEquals(eventLogType, record.getType());
			assertEquals(1, record.getRecord().NumStrings.intValue());
			assertNull(record.getData());

			// The full message includes a level and the full class name
			String fullMessage = level + " " + TEST_LOGGER_NAME + " " + "[]" + " - " + message;

			// The event message has the location tacked on the front
			StringBuilder eventMessage = new StringBuilder();
			for (int i = 0; i < record.getStrings().length; i++) {
				eventMessage.append(record.getStrings()[i].trim());
			}

			int levelMarker = eventMessage.indexOf(level.toString());
			assertTrue("missing level marker in '" + eventMessage + "'", levelMarker >= 0);
			String eventMessageWithoutLocation = eventMessage.substring(levelMarker);

			assertEquals(fullMessage, eventMessageWithoutLocation);
		} finally {
			iter.close();
		}
	}
}
