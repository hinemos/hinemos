package com.clustercontrol.agent.log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.clustercontrol.ws.monitor.LogfileCheckInfo;

/**
 * ファイルから読み込んだログを行文化宇するクラス。
 * 
 */
public class LineSeparator {
	// Logger
	private static Logger log = Logger.getLogger(LineSeparator.class);
	
	public final LogfileCheckInfo info;
	public final Pattern startRegexPattern;
	public final Pattern endRegexPattern;
	
	public final int CR = 13;
	public final int LF = 10;
	
	public LineSeparator(LogfileCheckInfo info) {
		this.info = info;
		
		Pattern startRegexPattern = null;
		try {
			startRegexPattern = isNullOrZeroLength(info.getPatternHead()) ? null : Pattern.compile(info.getPatternHead());
		} catch (PatternSyntaxException e) {
			log.warn(String.format("invalid regex : ignored format.separator.startRegex = %s", info.getPatternHead()));
		} finally {
			this.startRegexPattern = startRegexPattern;
		}
		
		Pattern endRegexPattern = null;
		try {
			endRegexPattern = isNullOrZeroLength(info.getPatternTail()) ? null : Pattern.compile(info.getPatternTail());
		} catch (PatternSyntaxException e) {
			log.warn(String.format("invalid regex : ignored format.separator.endRegex = %s", info.getPatternTail()));
		} finally {
			this.endRegexPattern = endRegexPattern;
		}
	}
	
	public int search(CharSequence cbuf, int maxBytes) {
		if (startRegexPattern == null &&
				endRegexPattern == null) {
			int pos = searchByLineSeparator(cbuf);
			return smallerOfPosAndMaxBytes(pos, maxBytes, cbuf);
		}
		
		if (startRegexPattern != null) {
			int pos = searchByStartRegex(cbuf);
			return smallerOfPosAndMaxBytes(pos, maxBytes, cbuf);
		}
		
		if (endRegexPattern != null) {
			int pos = searchByEndRegex(cbuf);
			return smallerOfPosAndMaxBytes(pos, maxBytes, cbuf);
		}
		
		log.debug(String.format("separator definition is illegal : %s", info));
		
		return -1;
	}
	
	private int searchByLineSeparator(CharSequence cbuf) {
		if ("CRLF".equals(info.getFileReturnCode())) {
			for (int i = 0; i < cbuf.length() - 1; i++) {
				if (cbuf.charAt(i) == '\r' && cbuf.charAt(i + 1) == '\n') {
					return i + 2;
				}
			}
		} else {
			byte lineSeparator;
			if ("CR".equals(info.getFileReturnCode())) {
				lineSeparator = '\r';
			} else if ("LF".equals(info.getFileReturnCode())) {
				lineSeparator = '\n';
			} else {
				log.warn(String.format("unknown line separator: %s", info.getFileReturnCode()));
				return -1;
			}
			for (int i = 0; i < cbuf.length(); i++) {
				if (cbuf.charAt(i) == lineSeparator)
					return i + 1;
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
	
	private int smallerOfPosAndMaxBytes(int pos, int maxBytes, CharSequence cbuf) {
		int ret = pos;
		if(maxBytes != -1) {
			if(pos == -1) {
				if(maxBytes <= cbuf.length()) {
					ret = maxBytes;
				}
			} else {
				if (maxBytes < pos) {
					ret = maxBytes;
				}
			}
		}
		return ret;
	}
}