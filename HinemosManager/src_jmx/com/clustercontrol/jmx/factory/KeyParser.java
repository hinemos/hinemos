/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.factory;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * JMX によるアクセスで項目を識別するために使用するキーの配列を、使用順に記述した文字列か作成する。
 * 解析対象となる文字列のは、以下。
 * 
 * 1. キーは、「`」 で囲われる。
 * 2. キーとキーは、「,」 で区分けする。
 * 3. 文字列の配列は、「[]」 で囲う。
 * 4. 配列内の文字列は、キー同様、「`」 で囲う。
 * 
 * ex.
 * System.out.println(KeyParser.parseKeys("`aaaaa`, `bbbbb`, `cccccc`, [`11111`, `22222`], `ddddddd`");
 * 
 * > [aaaaa, bbbbb, cccccc, [11111, 22222], ddddddd]
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class KeyParser {
	private static final int in_quotation = 1;
	private static final int end_quotation = 2;
	private static final int completed_key = 3;

	private static final char quotation = '`';
	private static final char separator = ',';
	private static final char leftArray = '[';
	private static final char rightArray = ']';
	private static final char space = ' ';

	/**
	 * キーの流れを記述した文字列の解析を行う。
	 * 
	 * @param key 解析対象の文字列
	 * @return 解析したキーの配列
	 * @throws Exception
	 */
	public static Object[] parseKeys(String key) throws Exception {
		List<Object> keys = new ArrayList<>();
		if (key == null) {
			return keys.toArray(new Object[keys.size()]);
		}

		StringCharacterIterator charIter = new StringCharacterIterator(key);
		boolean result = true;

		char c = charIter.current();
		if (c == StringCharacterIterator.DONE)
			return new Object[0];

		end_loop:
		do {
			switch (c) {
			case quotation:
				keys.add(parseQuataion(charIter, null));
				break;
			case leftArray:
				List<Object> arrayKey = new ArrayList<>();

				boolean endArray = false;
				end_array_loop:
				while ((c = charIter.next()) != StringCharacterIterator.DONE) {
					switch (c) {
					case quotation:
						arrayKey.add(parseQuataion(charIter, rightArray));
						break;
					case space:
						break;
					case rightArray:
						while ((c = charIter.next()) != StringCharacterIterator.DONE) {
							switch (c) {
							case space:
								break;
							case separator:
								endArray = true;
								break end_array_loop;
							default:
								break end_array_loop;
							}
						}
						endArray = true;
						break end_array_loop;
					default:
						break end_array_loop;
					}
				}

				if (!endArray) {
					result = false;
					break end_loop;
				}

				keys.add(arrayKey);
				break;
			case space:
				break;
			default:
				result = false;
				break end_loop;
			}
		} while ((c = charIter.next()) != StringCharacterIterator.DONE);

		if (!result)
			throw new Exception("fial to parse a keys string. " + key);

		return keys.toArray(new Object[keys.size()]);
	}

	private static Object parseQuataion(StringCharacterIterator charIter, Character end) throws Exception {
		StringBuilder buffer = null;
		StringBuilder b = new StringBuilder();

		int status = in_quotation;

		char c;
		end_quatation:
		while ((c = charIter.next()) != StringCharacterIterator.DONE) {
			if (status == end_quotation && end != null && c == end) {
				charIter.setIndex(charIter.getIndex() - 1);
				break;
			}

			switch (c) {
			case quotation:
				switch(status) {
				case end_quotation:
					status = in_quotation;
					b.append(buffer);
					b.append(c);
					break;
				case in_quotation:
					status = end_quotation;
					buffer = new StringBuilder();
					buffer.append(c);
					break;
				}
				break;
			case separator:
				switch(status) {
				case end_quotation:
					status = completed_key;
					break end_quatation;
				case in_quotation:
					b.append(c);
					break;
				}
				break;
			case space:
				switch(status) {
				case end_quotation:
					buffer.append(c);
					break;
				default: // in_quotation
					b.append(c);
					break;
				}
				break;
			default:
				switch(status) {
				case in_quotation:
					b.append(c);
					break;
				case end_quotation:
					status = in_quotation;
					b.append(buffer);
					b.append(c);
					break;
				default:
					break;
				}
				break;
			}
		}

		if (status != completed_key && status != end_quotation)
			throw new Exception("fial to parse a keys string. " + charIter.toString());

		return b.toString();
	}
}
