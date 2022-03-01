/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

public class WinActorLogParser implements RpaLogParser {
	private static Log m_log = LogFactory.getLog(WinActorLogParser.class);
	
	// WinActor7の日付パターン
	private SimpleDateFormat dateFormat7 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");
	// WinActor6の日付パターン
	private SimpleDateFormat dateFormat6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// WinActor7の日付パターン正規表現
	private Pattern dateRegExPattern7 = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}\\+[0-9]{2}:[0-9]{2}");
	// WinActor6の日付パターン正規表現
	private Pattern dateRegExPattern6 = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}");
	// WinActorのシナリオ名パターン正規表現
	private Pattern scenarioNamePattern = Pattern.compile("シナリオ「(.*\\.ums[0-9])」");
	// ログ重要度とHinemos重要度とのマッピング(WinActor7のみ)
	private Map<String, Priority> priorityMap = new HashMap<>();

	public WinActorLogParser() {
		// ログ重要度とHinemos重要度とのマッピングを定義
		priorityMap.put("ERROR", Priority.CRITICAL);
		priorityMap.put("INFO", Priority.INFO);
	}
	
	@Override
	public RpaLogParseResult parse(String logText) throws RpaLogParseException {
		m_log.debug("parse() logText = " + logText);
		RpaLogParseResult result = new RpaLogParseResult();

		// 日付を解析
		String dateEx = "";
		int version = 7;
		Matcher matcher7 = dateRegExPattern7.matcher(logText);
		Matcher matcher6 = dateRegExPattern6.matcher(logText);

		// 日付文字列を抽出(バージョン判定も行う)
		if (matcher7.find()) {
			dateEx = matcher7.group();
		} else if(matcher6.find()){
			version = 6;
			dateEx = matcher6.group();
		} else {
			throw new RpaLogParseException("parse() logTime match failure. logText = " + logText);
		}
		m_log.debug("parse() dateEx = " +  dateEx);

		// 日付をパース
		Date date;
		try {
			if (version == 7) {
				date = dateFormat7.parse(dateEx);
			} else {
				date = dateFormat6.parse(dateEx);
			}
			m_log.debug("parse() date = " +  date.getTime());
		} catch (ParseException e) {
			throw new RpaLogParseException("parse() invalid date expression. dateEx = " + dateEx + "WinActor version = " + version + " logText = " + logText);
		}

		// 出力時刻を取得
		result.setLogTime(date.getTime());
		// スペースで区切って重要度とメッセージ部分を解析する。
		String[] splitText = logText.split(" ");

		// 3番目が重要度部分(version 7の場合)
		if (version == 7){
			String priorityEx = splitText[2];
			if (priorityMap.containsKey(priorityEx)){
				result.setPriority(priorityMap.get(priorityEx));
			} else {
				throw new RpaLogParseException("parse() invalid priority expression. priorityEx = " + priorityEx + "WinActor version = " + version + " logText = " + logText);				
			}
		} else {
			// version 6は重要度なし
			result.setPriority(Priority.NONE);
		}

		// それ以降はメッセージ部分
		int messageidx = 3;
		if (version == 6) {
			messageidx = 2;
		}

		m_log.debug("splitText=" + Arrays.toString(splitText) + ", messageIdx=" + messageidx);
		String message = String.join(" ", Arrays.copyOfRange(splitText, messageidx, splitText.length));
		result.setLogMessage(message.trim());
		result.setIdentifyString(parseIdentifyString(message));

		return result;
	}
	
	private String parseIdentifyString(String message) {
		String scenarioName = null;
		// シナリオ名をシナリオ識別文字列として取得
		Matcher matcher = scenarioNamePattern.matcher(message);

		// メッセージにシナリオ名が含まれないケースも有るため、マッチしなくてもExceptionはthrowしない
		if(matcher.find()) {
			scenarioName = matcher.group(1);
		}

		return scenarioName;
	}

}
