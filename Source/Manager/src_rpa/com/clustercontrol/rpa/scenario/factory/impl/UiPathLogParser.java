/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.rpa.scenario.factory.RpaLogParseException;
import com.clustercontrol.rpa.scenario.factory.RpaLogParseResult;
import com.clustercontrol.rpa.scenario.factory.RpaLogParseResult.Priority;
import com.clustercontrol.rpa.scenario.factory.RpaLogParser;

public class UiPathLogParser implements RpaLogParser {
	private static Log m_log = LogFactory.getLog(UiPathLogParser.class);
	
	// UiPathの日付パターン
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	// UiPathの日付パターン正規表現
	private Pattern dateRegExPattern = Pattern.compile("\"timeStamp\"\\s*:\\s*\"([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]*\\+[0-9]{2}:[0-9]{2})\"");
	// UiPathの重要度パターン正規表現
	private Pattern priorityRegExPattern = Pattern.compile("\"level\"\\s*:\\s*\"(.*?)\"");
	// UiPathのメッセージパターン正規表現
	private Pattern logMessageRegExPattern = Pattern.compile("\"message\"\\s*:\\s*\"(.*?)(?<!\\\\)\"");
	// UiPathのシナリオ名パターン正規表現
	private Pattern scenarioNamePattern = Pattern.compile("\"processName\"\\s*:\\s*\"(.*?)\"");
	// ログ重要度とHinemos重要度とのマッピング
	private Map<String, Priority> priorityMap = new HashMap<>();

	public UiPathLogParser() {
		// ログ重要度とHinemos重要度とのマッピングを定義
		// https://docs.uipath.com/robot/docs/logging-levels#logging-levels-in-uipath
		priorityMap.put("Critical", Priority.CRITICAL);
		priorityMap.put("Error", Priority.CRITICAL);
		priorityMap.put("Warning", Priority.WARNING);
		priorityMap.put("Information", Priority.INFO);
		priorityMap.put("Trace", Priority.INFO);
		priorityMap.put("Verbose", Priority.INFO);		
	}
	
	@Override
	public RpaLogParseResult parse(String logText) throws RpaLogParseException {
		m_log.debug("parse() logText = " + logText);
		RpaLogParseResult result = new RpaLogParseResult();
		
		// 出力時刻、重要度、メッセージ、識別文字列をパース
		result.setLogTime(parseLogTime(logText));
		result.setPriority(parsePriority(logText));
		result.setLogMessage(parseMessage(logText));		
		result.setIdentifyString(parseScenarioName(logText));

		return result;
	}

	// 出力時刻をパース
	private long parseLogTime(String logText) throws RpaLogParseException {
		m_log.debug("parseLogTime() logText = " + logText);
		String dateEx = "";
		Matcher matcher = dateRegExPattern.matcher(logText);
		
		// 日付文字列を抽出(バージョン判定も行う)
		if (matcher.find()) {
			dateEx = matcher.group(1);
		} else {
			throw new RpaLogParseException("parseLogTime() logDate match failure. logText = " + logText);
		}
		m_log.debug("parseLogTime() dateEx = " +  dateEx);
		
		// 秒が小数点以下4桁以上まで出力されているため、パースするため3桁(msec)に丸める。
		Matcher secMatcher = Pattern.compile("([0-9]\\.[0-9]*)\\+").matcher(dateEx);
		secMatcher.find();
		String secStr = secMatcher.group(1);
		BigDecimal secBD = new BigDecimal(secStr);
		BigDecimal roundSecBD = secBD.setScale(3, RoundingMode.HALF_UP);
		String roundDateEx = secMatcher.replaceFirst(roundSecBD.toPlainString() + "+");

		// 日付をlong型にパース
		try {
			Date date = dateFormat.parse(roundDateEx);
			m_log.debug("parseLogTime() date = " +  date.getTime());
			return date.getTime();
		} catch (ParseException e) {
			throw new RpaLogParseException("parseLogTime() invalid date expression. dateEx = " + dateEx + " logText = " + logText);
		}		
	}

	// 重要度をパース
	private Priority parsePriority(String logText) throws RpaLogParseException {
		m_log.debug("parsePriority() logText = " + logText);
		String priorityEx = "";

		// 重要度文字列を抽出
		Matcher matcher = priorityRegExPattern.matcher(logText);
		if (matcher.find()) {
			priorityEx = matcher.group(1);
		} else {
			throw new RpaLogParseException("parsePriority() priority match failure. logText = " + logText);
		}
		
		if (priorityMap.containsKey(priorityEx)) {
			return priorityMap.get(priorityEx);
		} else {
			throw new RpaLogParseException("parsePriority() invalid priority expression. priorityEx = " + priorityEx + " logText = " + logText);
		}
	}

	// メッセージをパース
	private String parseMessage(String logText) throws RpaLogParseException {
		Matcher matcher = logMessageRegExPattern.matcher(logText);
		if(matcher.find()) {
			return matcher.group(1);
		} else {
			throw new RpaLogParseException("parseMessage() message match failure. logText = " + logText);
		}
	}
	
	// 識別文字列をパース
	private String parseScenarioName(String logText) throws RpaLogParseException {
		String scenarioName = null;
		Matcher matcher = scenarioNamePattern.matcher(logText);
		if(matcher.find()) {
			scenarioName = matcher.group(1);
		} else {
			throw new RpaLogParseException("parseScenarioName() scenario name match failure. logText = " + logText);
		}
		return scenarioName;
	}


}
