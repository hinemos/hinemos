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

package org.dblock.log4jna.nt;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.spi.StandardLevel;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinReg;

/**
 * Append to the NT event log system.
 * 
 * <p>
 * <b>WARNING</b> This appender can only be installed and used on a Windows
 * system.
 * 
 * <p>
 * Do not forget to place jna.jar and platform.jar in the CLASSPATH.
 * </p>
 * 
 * @author <a href="mailto:cstaylor@pacbell.net">Chris Taylor</a>
 * @author <a href="mailto:jim_cakalic@na.biomerieux.com">Jim Cakalic</a>
 * @author <a href="mailto:dblock@dblock.org">Daniel Doubrovkine</a>
 * @author <a href="mailto:tony@niemira.com">Tony Niemira</a>
 * @author <a href="mailto:claudio.trajtenberg@cgtca.ca">Claudio Trajtenberg</a>
 */
@Plugin(name = "Win32EventLog", category = "Core", elementType = "appender", printObject = true)
public class Win32EventLogAppender extends AbstractAppender {

	private static final String EVENT_LOG_PATH = "SYSTEM\\CurrentControlSet\\Services\\EventLog\\";
	private static final String CATEGORY_MESSAGE_FILE = "CategoryMessageFile";
	private static final String EVENT_MESSAGE_FILE = "EventMessageFile";
	private static final int CATEGORY_COUNT = 6;
	private static final int TYPES_SUPPORTED = 7;
	private static final String DEFAULT_SOURCE = "Log4jna";
	private static final String DEFAULT_APPLICATION = "Application";
	/**
	 * 
	 */
	private String _source = null;
	private String _server = null;
	private String _application = DEFAULT_APPLICATION;
	private String _eventMessageFile = "";
	private String _categoryMessageFile = "";

	private HANDLE _handle = null;

	/**
	 * @param name The appender name Win32EventLog
	 * @param server The server for remote logging
	 * @param source The Event View Source
	 * @param application The Event View application (location)
	 * @param eventMessageFile The message file location in the file system
	 * @param categoryMessageFile The message file location in the file system
	 * @param layout A Log4j Layout
	 * @param filter A Log4j Filter
	 * @return
	 */
	@PluginFactory
	public static Win32EventLogAppender createAppender(@PluginAttribute("name") String name,
			@PluginAttribute("server") String server, @PluginAttribute("source") String source,
			@PluginAttribute("application") String application,
			@PluginAttribute("eventMessageFile") String eventMessageFile,
			@PluginAttribute("categoryMessageFile") String categoryMessageFile,
			@PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filters") Filter filter) {
		return new Win32EventLogAppender(name, server, source, application, eventMessageFile, categoryMessageFile,
				layout, filter);
	}

	/**
	 * @param name The appender name Win32EventLog
	 * @param server The server for remote logging
	 * @param source The Event View Source
	 * @param application The Event View application (location)
	 * @param eventMessageFile The message file location in the file system
	 * @param categoryMessageFile The message file location in the file system
	 * @param layout A Log4j Layout
	 * @param filter A Log4j Filter
	 */
	public Win32EventLogAppender(String name, String server, String source, String application, String eventMessageFile,
			String categoryMessageFile, Layout<? extends Serializable> layout, Filter filter) {
		super(name, filter, layout);
		if (source == null || source.length() == 0) {
			source = DEFAULT_SOURCE;
		}

		if (eventMessageFile != null) {
			Path p = Paths.get(eventMessageFile);
			if (Files.exists(p)) {
				setEventMessageFile(p.toAbsolutePath().toString());
			}
		}

		if (categoryMessageFile != null) {
			Path p = Paths.get(categoryMessageFile);
			if (Files.exists(p)) {
				setCategoryMessageFile(p.toAbsolutePath().toString());
			}
		}

		this._server = server;
		setSource(source);
		setApplication(application);
	}

	/**
	 * The <b>Source</b> option which names the source of the event. The current
	 * value of this constant is <b>Source</b>.
	 */
	public void setSource(String source) {

		if (source == null || source.length() == 0) {
			source = DEFAULT_SOURCE;
		}

		_source = source.trim();
	}

	/**
	 * @return
	 */
	public String getSource() {
		return _source;
	}

	/**
	 * The <b>Application</b> option which names the subsection of the
	 * 'Applications and Services Log'. The default value of this constant is
	 * <b>Application</b>.
	 * 
	 * @param application The Event View application (location)
	 */
	public void setApplication(String application) {

		if (application == null || application.length() == 0) {
			application = DEFAULT_APPLICATION;
		}

		_application = application.trim();
	}

	/**
	 * @return
	 */
	public String getApplication() {
		return _application;
	}

	/**
	 * 
	 */
	public void close() {
		if (_handle != null) {
			if (!Advapi32.INSTANCE.DeregisterEventSource(_handle)) {
				throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
			}
			_handle = null;
		}
	}

	/**
	 * The <b>EventMessageFile</b> option which sets file location of the Event
	 * Messages
	 * 
	 * @param eventMessageFile The message file location in the file system
	 */
	public void setEventMessageFile(String eventMessageFile) {
		_eventMessageFile = eventMessageFile.trim();
	}

	/**
	 * @return
	 */
	public String getEventMessageFile() {
		return _eventMessageFile;
	}

	/**
	 * The <b>CategoryMessageFile</b> option which sets file location of the
	 * Catagory Messages
	 * 
	 * @param categoryMessageFile The message file location in the file system
	 */
	public void setCategoryMessageFile(String categoryMessageFile) {
		_categoryMessageFile = categoryMessageFile.trim();
	}

	/**
	 * @return
	 */
	public String getCategoryMessageFile() {
		return _categoryMessageFile;
	}

	/**
	 * 
	 */
	private void registerEventSource() {
		close();

		try {
			_handle = registerEventSource(_server, _source, _application, _eventMessageFile, _categoryMessageFile);
		} catch (Exception e) {
			close();
			throw new RuntimeException("Could not register event source.", e);
		}
	}

	/**
	 * 
	 */
	public void activateOptions() {
		registerEventSource();
	}

	
	/** 
	 * {@inheritDoc}
	 */
	public void append(LogEvent event) {

		if (_handle == null) {
			registerEventSource();
		}

		String s = new String(getLayout().toByteArray(event));
		final int messageID = 0x1000;

		String[] buffer = { s };

		if (Advapi32.INSTANCE.ReportEvent(_handle, getEventLogType(event.getLevel()),
				getEventLogCategory(event.getLevel()), messageID, null, buffer.length, 0, buffer, null) == false) {
			Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
			getHandler().error("Failed to report event [" + s + "].", event, e);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public void finalize() {
		close();
	}

	/**
	 * The <code>Win32EventLogAppender</code> requires a layout. Hence, this
	 * method always returns <code>true</code>.
	 */
	public boolean requiresLayout() {
		return true;
	}

	/**
	 * @param server The server for remote logging
	 * @param source The Event View Source
	 * @param application The Event View application (location)
	 * @param eventMessageFile The message file location in the file system
	 * @param categoryMessageFile The message file location in the file system
	 * @return
	 */
	private HANDLE registerEventSource(String server, String source, String application, String eventMessageFile,
			String categoryMessageFile) {
		String applicationKeyPath = EVENT_LOG_PATH + application;
		String eventSourceKeyPath = applicationKeyPath + "\\" + source;
		if (Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, applicationKeyPath)) {
			if (Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath)) {
				setVariableKeys(eventMessageFile, categoryMessageFile, eventSourceKeyPath);
			} else {
				createAndSetAllKeys(eventMessageFile, categoryMessageFile, eventSourceKeyPath);
			}
		} else {
			createAndSetAllKeys(eventMessageFile, categoryMessageFile, eventSourceKeyPath);
		}

		HANDLE h = Advapi32.INSTANCE.RegisterEventSource(server, source);
		if (h == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}

		return h;
	}

	/**
	 * 
	 * @param eventMessageFile The message file location in the file system
	 * @param categoryMessageFile The message file location in the file system
	 * @param eventSourceKeyPath The registry path
	 */
	private void createAndSetAllKeys(String eventMessageFile, String categoryMessageFile, String eventSourceKeyPath) {
		if (Advapi32Util.registryCreateKey(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath)) {
			Advapi32Util.registrySetIntValue(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath, "TypesSupported",
					TYPES_SUPPORTED);
			Advapi32Util.registrySetIntValue(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath, "CategoryCount",
					CATEGORY_COUNT);
			setVariableKeys(eventMessageFile, categoryMessageFile, eventSourceKeyPath);
		}
	}

	/**
	 * Set the file location only if it does not exist or has changed.
	 * 
	 * @param eventMessageFile The message file location in the file system
	 * @param categoryMessageFile The message file location in the file system
	 * @param eventSourceKeyPath The registry path
	 */
	private void setVariableKeys(String eventMessageFile, String categoryMessageFile, String eventSourceKeyPath) {
		if (!Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath, EVENT_MESSAGE_FILE)
				|| !Advapi32Util
						.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath, EVENT_MESSAGE_FILE)
						.equalsIgnoreCase(eventMessageFile)) {
			Advapi32Util.registrySetStringValue(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath, EVENT_MESSAGE_FILE,
					eventMessageFile);
		}
		if (!Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath, CATEGORY_MESSAGE_FILE)
				|| !Advapi32Util
						.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath, CATEGORY_MESSAGE_FILE)
						.equalsIgnoreCase(categoryMessageFile)) {
			Advapi32Util.registrySetStringValue(WinReg.HKEY_LOCAL_MACHINE, eventSourceKeyPath, CATEGORY_MESSAGE_FILE,
					categoryMessageFile);
		}
	}

	/**
	 * Convert log4j Priority to an EventLog type. The log4j package supports 8
	 * defined priorities, but the NT EventLog only knows 3 event types of
	 * interest to us: ERROR, WARNING, and INFO.
	 * 
	 * @param level
	 *            Log4j priority.
	 * @return EventLog type.
	 */
	private static int getEventLogType(Level level) {
		int type = WinNT.EVENTLOG_SUCCESS;

		if (level.intLevel() <= Level.INFO.intLevel()) {
			type = WinNT.EVENTLOG_INFORMATION_TYPE;
			if (level.intLevel() <= Level.WARN.intLevel()) {
				type = WinNT.EVENTLOG_WARNING_TYPE;
				if (level.intLevel() <= Level.ERROR.intLevel()) {
					type = WinNT.EVENTLOG_ERROR_TYPE;
				}
			}
		}

		return type;
	}

	/**
	 * Convert log4j Priority to an EventLog category. Each category is backed
	 * by a message resource so that proper category names will be displayed in
	 * the NT Event Viewer.
	 * 
	 * @param level  Log4J priority.
	 * @return EventLog category.
	 */
	private static int getEventLogCategory(Level level) {
		StandardLevel standardLevel = StandardLevel.getStandardLevel(level.intLevel());
		switch (standardLevel) {
			case FATAL : {
				return 6;
			}
			case ERROR : {
				return 5;
			}
			case WARN : {
				return 4;
			}
			case INFO : {
				return 3;
			}
			case DEBUG : {
				return 2;
			}
			default: {
				return 1;
			}
		}
	}

}
