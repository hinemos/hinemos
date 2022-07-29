/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Azureオプションをver.6.0.0対応後に削除する予定のクラス
 * 
 * Hinemosプロパティがhinemos.properteisだったころの名残。
 * ver.4.1.x までは、すべてのパラメータを文字列として扱っていた。
 * 正しいデータ型か判別するためのクラス。
 * 
 */
public interface PropValidator<T> extends Serializable {
	public static class StringValidator implements PropValidator<String> {
		private static final long serialVersionUID = 1L;
		private Set<String> stringSet;
		
		public StringValidator(String... strings) {
			stringSet = new HashSet<String>(Arrays.asList(strings));
		}
		
		@Override
		public void validate(String value) throws Exception {
			if (!stringSet.contains(value.toString())) {
				throw new Exception();
			}
		}
	}
	
	public void validate(T value) throws Exception;
}
