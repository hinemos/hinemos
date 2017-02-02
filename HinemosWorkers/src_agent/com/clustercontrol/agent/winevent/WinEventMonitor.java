/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent.winevent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.CalendarWSUtil;
import com.clustercontrol.agent.util.MonitorStringUtil;
import com.clustercontrol.bean.PluginConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.winevent.bean.WinEventConstant;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.WinEventCheckInfo;
import com.sun.jna.platform.win32.Win32Exception;

public class WinEventMonitor {

	private static Log m_log = LogFactory.getLog(WinEventMonitor.class);
	
	public static final int _collectorType = PluginConstant.TYPE_WINEVENT_MONITOR;

	public static final String runPath = Agent.getAgentHome() + "var\\run\\";
	public static final String PREFIX = "winevent-";
	public static final String POSTFIX_BOOKMARK = "-bookmark";
	
	// Windowsでファイル名として無効な文字「*、|、\、:、"、<、>、?、/」 
		public static final String INVALID_FILE_CHARACTER = "[*|\\\\:\"<>?/]";
	
	private WinEventReader winEventReader;
	
	// イベントログ名とブックマークファイル名のマップ
	private Map<String, String> bookmarkFileNameMap = new HashMap<String, String>();
	
	// イベント取得クエリ一回あたりのタイムアウト
	private static final String TIMEOUT_KEY = "monitor.winevent.timeout.per.query";
	private static long timeout = 30000;
	
	// イベント取得クエリ一回あたりのバッファ
	private static final String BUFFER_LENGTH_KEY = "monitor.winevent.buffer.per.query";
	private static int bufferLength = 100000;

	//オリジナルメッセージのサイズ上限（Byte）
	public static final String MESSAGE_LENGTH = "monitor.winevent.message.length";
	public static int messageLength = 1024;

	// イベント取得クエリ一回あたいの最大取得件数
	private static final String MAX_EVENTS_KEY = "monitor.winevent.maxevents.per.query";
	private static int maxEvents = 1000;

	// イベントメッセージの文字の一時置換用文字
	private static String RETURN_CHAR_REPLACE_KEY = "monitor.winevent.return.char.replace";
	private static String GT_CHAR_REPLACE_KEY = "monitor.winevent.gt.char.replace";
	private static String LT_CHAR_REPLACE_KEY = "monitor.winevent.lt.char.replace";
	private static final String TMP_RETURN_CODE = "#n;";
	private static final String TMP_GT_CODE = "#gt;";
	private static final String TMP_LT_CODE = "#lt;";
	private static String tmpReturnCode = TMP_RETURN_CODE;	// 改行
	private static String tmpGtCode = TMP_GT_CODE;			// ">"
	private static String tmpLtCode = TMP_LT_CODE;			// "<"
	
	// Windowsイベント監視設定
	private MonitorInfo monitorInfo;

	private RunInstructionInfo runInstructionInfo = null;

	private static String targetProperty;
	
	//監視の終了時刻
	private Date lastMonitorDate = HinemosTime.getDateInstance();;
	
	
	public WinEventMonitor(final MonitorInfo monitorInfo, final RunInstructionInfo runInstructionInfo) {
		this.monitorInfo = monitorInfo;
		this.runInstructionInfo = runInstructionInfo;

		String key = "";
		if (runInstructionInfo == null) {
			// 監視ジョブ以外
			key = monitorInfo.getMonitorId();
		} else {
			// 監視ジョブ
			key = runInstructionInfo.getSessionId()
					+ runInstructionInfo.getJobunitId()
					+ runInstructionInfo.getJobId()
					+ runInstructionInfo.getFacilityId()
					+ monitorInfo.getMonitorId();
		}
		winEventReader = new WinEventReader();
		for(String logName : monitorInfo.getWinEventCheckInfo().getLogName()) {
			String bookmarkFileName = runPath + PREFIX + key + "-" + logName.replaceAll(INVALID_FILE_CHARACTER, "") + POSTFIX_BOOKMARK + ".xml";
			bookmarkFileNameMap.put(logName, bookmarkFileName);
			try {
				if(!new File(bookmarkFileName).exists()) {
					winEventReader.updateBookmark(bookmarkFileName, logName);
				}
			} catch (Win32Exception e) {
				m_log.warn("Failed to init create bookmark file " + bookmarkFileName + ", " + e.getMessage());
				sendMessage(PriorityConstant.TYPE_CRITICAL, MessageConstant.MESSAGE_WINEVENT_FAILED_TO_CREATE_BOOKMARK.getMessage(), "Failed to init create bookmark file:" + key + "," + bookmarkFileName);
			} catch (IOException e) {
				m_log.warn("Failed to init create bookmark file " + bookmarkFileName + ", " + e.getMessage());
				sendMessage(PriorityConstant.TYPE_CRITICAL, MessageConstant.MESSAGE_WINEVENT_FAILED_TO_CREATE_BOOKMARK.getMessage(), "Failed to init create bookmark file:" + key + "," + bookmarkFileName);
			}
		}
		
		init();
	}
	
	private static void init() {
		// タイムアウトを設定する
		String timeoutStr = AgentProperties.getProperty(TIMEOUT_KEY);
		try{
			timeout = Long.parseLong(timeoutStr);
		} catch (NumberFormatException e){
			m_log.info("collecor.winevent.timeout uses " + timeout + ". (" + timeoutStr + " is invalid)");
		}

		// 実行結果(XML)を格納するバッファサイズを設定する
		String bufferLengthStr = AgentProperties.getProperty(BUFFER_LENGTH_KEY);
		try{
			bufferLength = Integer.parseInt(bufferLengthStr);
		} catch (NumberFormatException e){
			m_log.info("collecor.winevent.buffer uses " + bufferLength + ". (" + bufferLengthStr + " is invalid)");
		}
		
		// イベントの最大取得件数を設定する
		String maxEventsStr = AgentProperties.getProperty(MAX_EVENTS_KEY);
		try{
			maxEvents = Integer.parseInt(maxEventsStr);
		} catch (NumberFormatException e){
			m_log.info("collector.winevent.maxevents uses " + maxEvents + ". (" + maxEventsStr + " is invalid)");
		}

		// 1行のメッセージ上限を定める
		String messageLengthStr = AgentProperties.getProperty(MESSAGE_LENGTH);
		try {
			messageLength = Integer.parseInt(messageLengthStr);
		} catch (NumberFormatException e) {
			m_log.info("monitor.message.length uses " + messageLength + ". (" + messageLengthStr + " is not collect)");
		}
		
		// イベントメッセージの文字の一時置換用文字を設定する
		tmpReturnCode = AgentProperties.getProperty(RETURN_CHAR_REPLACE_KEY);
		if(tmpReturnCode == null){
			tmpReturnCode = TMP_RETURN_CODE;
			m_log.info("collector.winevent.return.char.replace uses " + tmpReturnCode + ". ");
		}
		tmpGtCode = AgentProperties.getProperty(GT_CHAR_REPLACE_KEY);
		if(tmpGtCode == null){
			tmpGtCode = TMP_GT_CODE;
			m_log.info("collector.winevent.gt.char.replace uses " + tmpGtCode + ". ");
		}
		tmpLtCode = AgentProperties.getProperty(LT_CHAR_REPLACE_KEY);
		if(tmpLtCode == null){
			tmpLtCode = TMP_GT_CODE;
			m_log.info("collector.winevent.lt.char.replace uses " + tmpLtCode + ". ");
		}
	}
	
	/**
	 * スレッドの動作メソッド<BR>
	 * 
	 */
	public void run() {
		m_log.debug("run WinEventMonitorThread");
		
		long start = HinemosTime.currentTimeMillis(); // 計測開始
			
		// 監視設定無効時はイベントログを取得しない
//		if (runInstructionInfo == null && !monitorInfo.isMonitorFlg()) {
//			m_log.debug("WinEventMonitorThread run is skipped because of monitor flg");
//			for(Map.Entry<String, String> entry : bookmarkFileNameMap.entrySet()) {
//				String logName = entry.getKey();
//				try {
//					winEventReader.updateBookmark(entry.getValue(), logName);
//				} catch (Win32Exception e) {
//					m_log.warn("Failed to update bookmark file (monitor disabled)" + bookmarkFileNameMap.get(logName) + ", " + e.getMessage());
//				} catch (IOException e) {
//					m_log.warn("Failed to update bookmark file (monitor disabled)" + bookmarkFileNameMap.get(logName) + ", " + e.getMessage());
//				}
//			}
//			return;
//		}
		// カレンダによる非稼動時はイベントログを取得しない
		if (runInstructionInfo == null && monitorInfo.getCalendar() != null && ! CalendarWSUtil.isRun(monitorInfo.getCalendar())) {
			m_log.debug("WinEventMonitorThread run is skipped because of calendar settings");
			
			for(Map.Entry<String, String> entry : bookmarkFileNameMap.entrySet()) {
				String logName = entry.getKey();
				try {
					winEventReader.updateBookmark(entry.getValue(), logName);
				} catch (Win32Exception e) {
					m_log.warn("Failed to update bookmark file (calendar non-operating)" + bookmarkFileNameMap.get(logName) + ", " + e.getMessage());
				} catch (IOException e) {
					m_log.warn("Failed to update bookmark file (calendar non-operating)" + bookmarkFileNameMap.get(logName) + ", " + e.getMessage());
				}
			}
			return;
		}
		
		
		
		for(Map.Entry<String, String> entry : bookmarkFileNameMap.entrySet()) {
			String logName = entry.getKey();
			try {
				long queryStart = HinemosTime.currentTimeMillis();
				
				String query = createQuery(monitorInfo.getWinEventCheckInfo(), logName);
				m_log.debug("createQuery =" + query);
				m_log.trace("query creating time : " + (HinemosTime.currentTimeMillis() - queryStart));
			
				long readStart = HinemosTime.currentTimeMillis();
				
				String readEventResult;
				// maxEventsずつイベントログの取得、パース、パターンマッチ、通知情報の送信を行う
				while((readEventResult = winEventReader.readEventLog(entry.getValue(), query, maxEvents, timeout, logName)) != null) {
			
					// バッファのあふれをチェック
					int eventLogLen = readEventResult.toString().getBytes(Charset.forName("MS932")).length;
					m_log.debug("eventLogLen : " + eventLogLen);
					if(eventLogLen > bufferLength) {
						sendMessage(PriorityConstant.TYPE_CRITICAL, MessageConstant.MESSAGE_WINEVENT_FAILED_TO_MONITOR_EVENTLOG_EXECUTION_STRING_MAXIMUM_VALUE.getMessage(), "read event log length=" + eventLogLen);
						m_log.error("Discard part of eventLog : readEventResult=" + readEventResult + " eventLogLen=" + eventLogLen);
						continue;
					}
			
					String formattedEventLog = "<Events><root>" + readEventResult + "</root></Events>";
					m_log.debug("formattedEventLog : " + (formattedEventLog));

					// 実行結果をパースしてEventLogRecordクラスに格納
					ArrayList<EventLogRecord> eventlogs = parseEventXML(new ByteArrayInputStream(formattedEventLog.getBytes()));
					Collections.reverse(eventlogs);

					// 監視設定をもとにパターンマッチし、通知情報をマネージャに送信する
					for(EventLogRecord eventlog : eventlogs){
						m_log.debug("Event : " + eventlog);
						// 監視設定をもとにパターンマッチする
						MonitorStringUtil.patternMatch(formatLine(eventlog.toString()), monitorInfo, runInstructionInfo, eventlog.getTimeCreated(), null);
					}
				}
				m_log.trace("event log reading time : " + (HinemosTime.currentTimeMillis() - readStart));
			} catch (Win32Exception e){
				m_log.warn("failed executing monitor for " + monitorInfo.getMonitorId() + " logName is " + logName + ", " + e.getMessage());
				
				Date now = HinemosTime.getDateInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				sdf.setTimeZone(HinemosTime.getTimeZone());
				sendMessage(PriorityConstant.TYPE_CRITICAL, 
						MessageConstant.MESSAGE_WINEVENT_EVENTS_LOST.getMessage(new String[]{sdf.format(lastMonitorDate), sdf.format(now), logName}), 
						"Events may have been lost");
				try {
					winEventReader.updateBookmark(entry.getValue(), logName);
				} catch (Win32Exception ex) {
					m_log.warn("Failed to update bookmark file " + bookmarkFileNameMap.get(logName) + ", " + ex.getMessage());
				} catch (IOException ex) {
					m_log.warn("Failed to update bookmark file " + bookmarkFileNameMap.get(logName) + ", " + ex.getMessage());
				}
			
			} catch (IOException e) {
				m_log.warn("failed executing monitor for " + monitorInfo.getMonitorId() + " logName is " + logName + ", " + e.getMessage());
			} catch (RuntimeException e){
				m_log.warn("failed executing monitor for " + monitorInfo.getMonitorId() + " logName is " + logName, e);

			//ブックマークファイルは更新しない。
			//何度もExceptionが生じた場合は無限ループとなるが、環境障害 or 不具合の可能性が高いため、勝手にスキップするのはそもそもナンセンス。
			}
		}
		m_log.trace("total running time : " + (HinemosTime.currentTimeMillis() - start));
		
		//監視の終了時刻を保存しておく
		lastMonitorDate = HinemosTime.getDateInstance();
	}
	
	private String createQuery(WinEventCheckInfo checkInfo, String logName){
		m_log.debug("createQuery() start creating query for EvtQuery");
		
		String query = "<QueryList><Query><Select Path='" + logName + "'>*[System[";
			
		// ソース
		if(checkInfo.getSource() != null && checkInfo.getSource().size() != 0){
			query += "Provider[";
			StringBuffer sourceStr = new StringBuffer();
			for(String sourceName : checkInfo.getSource()){
				sourceStr.append("@Name='");
				sourceStr.append(sourceName);
				sourceStr.append("' or ");
			}
			query += sourceStr.toString();
			query = query.replaceFirst(" or \\z", "");	// 末尾の"or"を削除
			query += "] and ";
		}
			
		// レベル
		query += "(";
		query += checkInfo.isLevelCritical() ? "Level=" + WinEventConstant.CRITICAL_LEVEL + " or " : "" ;
		query += checkInfo.isLevelWarning() ? "Level=" + WinEventConstant.WARNING_LEVEL + " or " : "" ;
		query += checkInfo.isLevelVerbose() ? "Level=" + WinEventConstant.VERBOSE_LEVEL + " or " : "" ;
		query += checkInfo.isLevelError() ? "Level=" + WinEventConstant.ERROR_LEVEL + " or " : "" ;
		query += checkInfo.isLevelInformational() ? "Level=" + WinEventConstant.INFORMATION_LEVEL0 
				+ " or Level=" + WinEventConstant.INFORMATION_LEVEL4 + " or " : "" ;
			
		query = query.replaceFirst(" or \\z", "");	// 末尾の"or"を削除
		query += ") and ";
		
		// イベントID
		if(checkInfo.getEventId() != null && checkInfo.getEventId().size() != 0){
			query += "(";
			StringBuffer sourceStr = new StringBuffer();
			for(Integer id : checkInfo.getEventId()){
				sourceStr.append("EventID=" + id + " or ");
			}
			query += sourceStr.toString();
			query = query.replaceFirst(" or \\z", "");	// 末尾の"or"を削除
			query += ") and ";
		}

		// タスクの分類
		if(checkInfo.getCategory() != null && checkInfo.getCategory().size() != 0){
			query += "(";
			StringBuffer sourceStr = new StringBuffer();
			for(Integer category : checkInfo.getCategory()){
				sourceStr.append("Task=" + category + " or ");
			}
			query += sourceStr.toString();
			query = query.replaceFirst(" or \\z", "");	// 末尾の"or"を削除
			query += ") and ";
		}
			
		// キーワード
		if(checkInfo.getKeywords() != null && checkInfo.getKeywords().size() != 0){
			long targetKeyword = 0l;
			for(Long keyword : checkInfo.getKeywords()){
				targetKeyword += keyword.longValue();
			}
			query += "(band(Keywords," + targetKeyword + "))";
		}
			
		query = query.replaceFirst(" and \\z", "");	
			
		query += "]]</Select></Query></QueryList>";
		
		return query;
	}
	
	
	/**
	 * イベントXMLをStAXでパースしてEventLogRecordのリストに変換する
	 * @param eventXmlStream
	 * @return EventLogRecordのリスト
	 */
	private ArrayList<EventLogRecord> parseEventXML(InputStream eventXmlStream) {
		ArrayList<EventLogRecord> eventlogs = new ArrayList<EventLogRecord>();
		
		try {
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			/**
			 * OpenJDK7/OracleJDK7にて"]"が2回続くと、以降の文字が複数の要素に分割されてしまい、通常の処理では最初の要素のみ扱うようになる。
			 * サードパーティのXMLパーサでは事象が発生しないため、OpenJDK7/OracleJDK7の実装によるものになるが、仕様/不具合として明確化できない。
			 * 以下関連URLにあるパラメタを使用して、複数要素を合体させて、扱うように対応する
			 * 
			 * 関連URL
			 * http://docs.oracle.com/javase/jp/6/api/javax/xml/stream/XMLStreamReader.html#next()
			 */
			String xmlCoalescingKey = "javax.xml.stream.isCoalescing";// TODO JREが変更された場合は、この変数が変更されていないかチェックすること。
			if(m_log.isDebugEnabled()){
				m_log.debug(xmlCoalescingKey + " = true");
			}
			xmlif.setProperty(xmlCoalescingKey, true); 
			XMLStreamReader xmlr = xmlif.createXMLStreamReader(eventXmlStream);
			
			while (xmlr.hasNext()) {
				switch (xmlr.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					m_log.trace("EventType : XMLStreamConstants.START_ELEMENT");

					String localName = xmlr.getLocalName();
					m_log.trace("local name : " + localName);

					if("Event".equals(localName)){
						EventLogRecord eventlog = new EventLogRecord();
						eventlogs.add(eventlog);
						m_log.debug("create new EventLogRecord");
					} else {
						String attrLocalName = null;
						String attrValue = null;
						
						if(xmlr.getAttributeCount() != 0){
							attrLocalName = xmlr.getAttributeLocalName(0);
							attrValue = xmlr.getAttributeValue(0);
							m_log.trace("attribute local name : " + attrLocalName);
							m_log.trace("attribute local value : " + attrValue);
						}
						
						if("Provider".equals(localName)){
							if("Name".equals(attrLocalName)){
								m_log.trace("target value : " + attrValue);
								
								EventLogRecord eventlog = eventlogs.get(eventlogs.size() - 1);
								eventlog.setProviderName(attrValue);
								m_log.debug("set ProviderName : " + eventlog.getProviderName());
							}
						}
						// Get-WinEvent用/wevtutil.exe用
						else if("TimeCreated".equals(localName) && "SystemTime".equals(attrLocalName)){
							m_log.trace("target value : " + attrValue);
							
							// "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"とするとSの部分でパースに失敗するため、秒までで切り捨てる。
							String formatedDateString = attrValue.replaceAll("\\..*Z", "");
							m_log.trace("formatted target value : " + formatedDateString);
							DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
							sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
							
							EventLogRecord eventlog = eventlogs.get(eventlogs.size() - 1);;
							try {
								eventlog.setTimeCreated(sdf.parse(formatedDateString));
							} catch (ParseException e) {
								// do nothing
								m_log.error("set TimeCreated Error", e);
							}
							m_log.debug("set TimeCreated : " + eventlog.getTimeCreated());
						}
						// Get-EventLog用
						if("TimeGenerated".equals(localName) && "SystemTime".equals(attrLocalName)){
							m_log.trace("target value : " + attrValue);
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
							sdf.setTimeZone(HinemosTime.getTimeZone());

							EventLogRecord eventlog = eventlogs.get(eventlogs.size() - 1);;
							try {
								eventlog.setTimeCreated(sdf.parse(attrValue));
							} catch (ParseException e) {
								// do nothing
								m_log.error("set TimeCreated Error", e);
							}
							m_log.debug("set TimeCreated : " + eventlog.getTimeCreated());
						}
						else{
							targetProperty = localName;
							m_log.trace("target property : " + targetProperty);
						}
					}
					
					break;
				case XMLStreamConstants.SPACE:
				case XMLStreamConstants.CHARACTERS:
					m_log.trace("EventType : XMLStreamConstants.CHARACTERS, length=" + xmlr.getTextLength());
					if(targetProperty != null){
						try{
							EventLogRecord eventlog = eventlogs.get(eventlogs.size() - 1);;
							if("EventID".equals(targetProperty)){
								eventlog.setId(Integer.parseInt(new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength())));
								m_log.debug("set EventID : " + eventlog.getId());
							}
							// Get-WinEvent用/wevtutil.exe用
							else if("Level".equals(targetProperty)){
								if(eventlog.getLevel() == WinEventConstant.UNDEFINED){
									eventlog.setLevel(Integer.parseInt(new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength())));
									m_log.debug("set Level : " + eventlog.getLevel());
								}
							}
							else if("Task".equals(targetProperty)){
								if(eventlog.getTask() == WinEventConstant.UNDEFINED){
									eventlog.setTask(Integer.parseInt(new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength())));
									m_log.debug("set Task : " + eventlog.getTask());
								}
							}
							else if("Keywords".equals(targetProperty)){
								// TODO パースに失敗するのでいったん外す（例：0x8080000000000000）
								//eventlog.setKeywords(Long.decode(new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength())));
								//m_log.debug("set Keywords : " + eventlog.getKeywords());
							}
							else if("EventRecordId".equals(targetProperty)){
								eventlog.setRecordId(Long.parseLong(new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength())));
								m_log.debug("set RecordId : " + eventlog.getRecordId());
							}
							else if("Channel".equals(targetProperty)){
								eventlog.setLogName(new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength()));
								m_log.debug("set LogName : " + eventlog.getLogName());
							}
							else if("Computer".equals(targetProperty)){
								eventlog.setMachineName(new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength()));
								m_log.debug("set MachineName : " + eventlog.getMachineName());
							}
							else if("Message".equals(targetProperty)){
								String message = new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
								message = message.replaceAll(tmpReturnCode, "\r\n");
								message = message.replaceAll(tmpLtCode, "<");
								message = message.replaceAll(tmpGtCode, ">");
								eventlog.setMessage(message);
								m_log.debug("set Message : " + eventlog.getMessage());
							}
							else if("Data".equals(targetProperty)){
								String data = new String(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
								eventlog.getData().add(data);
								m_log.debug("set Data : " + data);
							}
							else {
								m_log.debug("unknown target property : " + targetProperty);
							}
						} catch (NumberFormatException e){
							m_log.debug("number parse error", e);
						}
					}
					targetProperty = null;
					break;
				default: // スルー
					break;
				}
				xmlr.next();
			}
			xmlr.close();
		} catch (XMLStreamException e) {
			m_log.warn("parseEvent() xmlstream error", e);
		}
		
		return eventlogs;
		
	}
	
	public MonitorInfo getMonitorInfo() {
		return monitorInfo;
	}

	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	
	/**
	 * 通知をマネージャに送信する。
	 * @param priority
	 * @param message
	 * @param messageOrg
	 */
	private void sendMessage(int priority, String message, String messageOrg) {
		WinEventMonitorManager.sendMessage(priority, message, messageOrg, monitorInfo.getMonitorId(), runInstructionInfo);
	}

	/**
	 * 監視文字列を整形する
	 * @param line
	 * @return formatted line
	 */
	private String formatLine(String line){
		// CR-LFの場合は\rが残ってしまうので、ここで削除する。
		line = line.replace("\r", "");

		// 長さが上限値を超える場合は切り捨てる
		if (line.length() > messageLength) {
			m_log.info("log line is too long");
			line = line.substring(0, messageLength);
		}
		return line;
	}
}
