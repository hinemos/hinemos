/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.util;

import java.util.HashSet;
import java.util.Set;

public class StringEscaper {
	private Set<Character> escapeTargetCharacterSet;
	private Character escapeCharacter = '\\';

	public StringEscaper() {
		this.escapeTargetCharacterSet = new HashSet<Character>();
	}

	public StringEscaper(String escapeTaregtStr) {
		this(escapeTaregtStr.toCharArray());
	}

	public StringEscaper(char[] escapeTaregtCharArray) {
		this();
		for (final char c : escapeTaregtCharArray) {
			this.add(c);
		}
	}

	public boolean add(char escapeTargetChar) {
		return this.add(Character.valueOf(escapeTargetChar));
	}

	public boolean add(Character escapeTargetCharacter) {
		return escapeTargetCharacterSet.add(escapeTargetCharacter);
	}

	public boolean needsEscape(char c) {
		return this.needsEscape(Character.valueOf(c));
	}

	public boolean needsEscape(Character character) {
		return this.escapeTargetCharacterSet.contains(character);
	}

	public String escapeString(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		StringBuilder builder = new StringBuilder();
		for (final char c : str.toCharArray()) {
			if (this.needsEscape(c)) {
				builder.append(this.escapeCharacter);
			}
			builder.append(c);
		}
		return builder.toString();
	}
}
