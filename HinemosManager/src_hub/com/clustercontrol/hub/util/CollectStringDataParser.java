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

package com.clustercontrol.hub.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.clustercontrol.hub.bean.ValueType;
import com.clustercontrol.hub.model.CollectDataTag;
import com.clustercontrol.hub.model.CollectDataTagPK;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.LogFormatKey;

/**
 * 文字列収集値に、ログフォーマットで抽出したタグ情報を付加するクラス。
 *
 */
public class CollectStringDataParser {
	
	private static final Log log = LogFactory.getLog(CollectStringDataParser.class);
	
	public final LogFormat format;
	
	public final Pattern timestampPattern;
	public final DateTimeFormatter timestampFormatter;
	public final Locale timestampLocale = Locale.US;
	
	public final Map<String, Pattern> keywordPatternMap;
	
	public final static String KEY_TIMESTAMP_IN_LOG = "TIMESTAMP_IN_LOG";
	public final static String KEY_TIMESTAMP_RECIEVED = "TIMESTAMP_RECIEVED";
	
	public CollectStringDataParser(LogFormat format) {
		this.format = format;
		
		Pattern timestampPattern = null;
		try {
			timestampPattern = format.getTimestampRegex() == null ? null : Pattern.compile(format.getTimestampRegex());
		} catch (PatternSyntaxException e) {
			log.warn(String.format("invalid regex : ignored format.timestampRegex = %s", format.getTimestampRegex()));
		} finally {
			this.timestampPattern = timestampPattern;
		}
		
		DateTimeFormatter timestampFormatter = null;
		try {
			//空白文字は、IllegalArgumentと認識されるため、nullに置き換える。
			timestampFormatter = (format.getTimestampFormat() == null || format.getTimestampFormat().equals("")) ? null : DateTimeFormat.forPattern(format.getTimestampFormat()).withLocale(timestampLocale).withDefaultYear(0);
		} catch (IllegalArgumentException e) {
			log.warn(String.format("invalid format : ignored format.timestampFormat  = %s", format.getTimestampFormat()));
		} finally {
			this.timestampFormatter = timestampFormatter == null ? DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss.SSS").withLocale(timestampLocale).withDefaultYear(0): timestampFormatter;
		}
		
		Map<String, Pattern> keywordPatternMap = new TreeMap<String, Pattern>();
		for (LogFormatKey keyword : format.getKeyPatternList()) {
			try {
				Pattern keywordPattern = keyword.getPattern() == null ? null : Pattern.compile(keyword.getPattern());
				keywordPatternMap.put(keyword.getKey(), keywordPattern);
			} catch (PatternSyntaxException e) {
				log.warn(String.format("invalid regex : ignored keyword.regex = %s", keyword.getPattern()));
			}
		}
		
		this.keywordPatternMap = Collections.unmodifiableMap(keywordPatternMap);
	}
	
	/**
	 * 指定された文字列収集値に、ログフォーマットに従って抽出されたタグ情報を負荷する。
	 * 
	 * @param data
	 * @return
	 */
	public CollectStringData parse(CollectStringData data) {
		Map<String, CollectDataTag> tagMap = new HashMap<>();
		for (CollectDataTag tag: data.getTagList()) {
			tagMap.put(tag.getKey(), tag);
		}
		
		if (isNullOrZeroLength(format.getTimestampRegex()) && isNullOrZeroLength(format.getTimestampFormat())) {
			// do nothing, use currentTimeMillis as timestamp
		} else {
			Matcher m = timestampPattern.matcher(data.getValue());
			if (m.find() && m.groupCount() > 0) {
				String timestampStr = m.group(1);
				
				try {
					DateTime datetime = timestampFormatter.parseDateTime(timestampStr);
					
					if (datetime.year().get() == 0) {
						// for messages without year, like syslog
						
						DateTime now = new DateTime();
						DateTimeFormatter timestampFormatterWithCurrentYear = timestampFormatter.withDefaultYear(now.year().get());
						DateTimeFormatter timestampFormatterWithLastYear = timestampFormatter.withDefaultYear(now.year().get() - 1);
						
						datetime = timestampFormatterWithCurrentYear.parseDateTime(timestampStr);
						if (datetime.getMillis() - now.getMillis() > 1000 * 60 * 60 * 24 * 7) {
							// treat messages as end of year (threshold : 1 week)
							datetime = timestampFormatterWithLastYear.parseDateTime(timestampStr);
						}
					}
					
					tagMap.put(KEY_TIMESTAMP_IN_LOG, new CollectDataTag(new CollectDataTagPK(data.getCollectId(), data.getDataId(), KEY_TIMESTAMP_IN_LOG), ValueType.number, Long.toString(datetime.getMillis())));
				} catch (IllegalArgumentException e) {
					log.warn(String.format("invalid timestamp string : format = %s, string = %s", format.getTimestampRegex(), timestampStr));
				}
			}
		}
		
		for (LogFormatKey keyword : format.getKeyPatternList()) {
			Pattern p = keywordPatternMap.get(keyword.getKey());
			if (null == p) {
				log.debug(String.format("Pattern is null keyword : pattern=%s", keyword.getPattern()));
				continue;
			}
			
			Matcher m = p.matcher(data.getValue());
			String matchedStr = null;
			switch (keyword.getKeyType()) {
			case parsing:
				if (m.find() && m.groupCount() > 0) {
					matchedStr = m.group(1);
				}
				break;
			case fixed:
				if (m.find()) {
					matchedStr = keyword.getValue();
				}
				break;
			}
			
			if (matchedStr != null && keyword.getValueType() == ValueType.string) {
				tagMap.put(keyword.getKey(), new CollectDataTag(new CollectDataTagPK(data.getCollectId(), data.getDataId(), keyword.getKey()), keyword.getValueType(), matchedStr));
			}else if (matchedStr != null && keyword.getValueType() != ValueType.string) {
				tagMap.put(keyword.getKey(), new CollectDataTag(new CollectDataTagPK(data.getCollectId(), data.getDataId(), keyword.getKey()), keyword.getValueType(), matchedStr));
				
				switch(keyword.getValueType()) {
				case number:
					try {
						new BigDecimal(matchedStr);
					} catch(NumberFormatException e) {
						log.warn(String.format("not match number format : value=%s, source=%s, pattern=%s", matchedStr, data.getValue(), p.pattern()));
					}
					break;
				case bool:
					if (!"true".equalsIgnoreCase(matchedStr) || !"false".equalsIgnoreCase(matchedStr)) {
						log.warn(String.format("not match boolean type : value=%s, source=%s, pattern=%s", matchedStr, data.getValue(), p.pattern()));
					}
					break;
				default:
					log.warn(String.format("unexpected value type : type=%s, value=source=%s, pattern=%s", keyword.getValueType().name(), data.getValue(), p.pattern()));
					break;
				}
			}
		}
		
		data.setTagList(new ArrayList<>(tagMap.values()));
		return data;
	}
	
	private boolean isNullOrZeroLength(String str) {
		return str == null || "".equals(str);
	}
}
