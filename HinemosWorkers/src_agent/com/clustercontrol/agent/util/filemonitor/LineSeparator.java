/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util.filemonitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.clustercontrol.logfile.bean.LogfileLineSeparatorConstant;

/**
 * ファイルから読み込んだログを行分解するクラス。
 * 
 */
public class LineSeparator {
	// Logger
	private static Logger log = Logger.getLogger(LineSeparator.class);
	
	public final String fileReturnCode;
	public final Pattern startRegexPattern;
	public final Pattern endRegexPattern;
	
	public final int CR = 13;
	public final int LF = 10;
	
	
	public LineSeparator(String fileReturnCode, String startRegexString, String endRegexString) {
		this.fileReturnCode = fileReturnCode;

		Pattern startRegexPattern = null;
		try {
			startRegexPattern = isNullOrZeroLength(startRegexString) ? null : Pattern.compile(startRegexString);
		} catch (PatternSyntaxException e) {
			log.warn(String.format("invalid regex : ignored format.separator.startRegex = %s", startRegexString));
		} finally {
			this.startRegexPattern = startRegexPattern;
		}
		
		Pattern endRegexPattern = null;
		try {
			endRegexPattern = isNullOrZeroLength(endRegexString) ? null : Pattern.compile(endRegexString);
		} catch (PatternSyntaxException e) {
			log.warn(String.format("invalid regex : ignored format.separator.endRegex = %s", endRegexString));
		} finally {
			this.endRegexPattern = endRegexPattern;
		}
	}
	
	public int search(CharSequence cbuf) {
		if (startRegexPattern == null &&
				endRegexPattern == null) {
			return searchByLineSeparator(cbuf);
		}
		
		if (startRegexPattern != null) {
			return searchByStartRegex(cbuf);
		}
		
		if (endRegexPattern != null) {
			return searchByEndRegex(cbuf);
		}
		
		log.debug(String.format("separator definition is illegal"));
		
		return -1;
	}
	
	private int searchByLineSeparator(CharSequence cbuf) {
		if (LogfileLineSeparatorConstant.CRLF.equals(fileReturnCode)) {
			for (int i = 0; i < cbuf.length() - 1; i++) {
				if (cbuf.charAt(i) == '\r' && cbuf.charAt(i + 1) == '\n') {
					return i + 2;
				}
			}
		} else {
			byte lineSeparator;
			if (LogfileLineSeparatorConstant.CR.equals(fileReturnCode)) {
				lineSeparator = '\r';
			} else if (LogfileLineSeparatorConstant.LF.equals(fileReturnCode)) {
				lineSeparator = '\n';
			} else {
				log.warn(String.format("unknown line separator: %s", fileReturnCode));
				return -1;
			}
			for (int i = 0; i < cbuf.length(); i++) {
				if (cbuf.charAt(i) == lineSeparator) {
					return i + 1;
				}
			}
		}
		return -1;
	}
	
	private int searchByStartRegex(CharSequence str) {
		Matcher matcher = startRegexPattern.matcher(str);
		while (matcher.find()) {
			int index = matcher.start();
			if (index > 0) {
				return index;
			}
		}
		return -1;
	}
	
	private int searchByEndRegex(CharSequence str) {
		Matcher matcher = endRegexPattern.matcher(str);
		if (matcher.find()) {
			int index = matcher.end();
			return index;
		}
		return -1;
	}
	
	private boolean isNullOrZeroLength(CharSequence str) {
		return str == null || str.length() == 0;
	}

}