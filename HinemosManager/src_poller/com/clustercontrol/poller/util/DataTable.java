/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.poller.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * キーとオブジェクトのマッピングを保持するクラス
 * 
 * @version 3.0.0
 * @since 2.0.0
 */
public final class DataTable implements Serializable {

	private final static Log log = LogFactory.getLog(DataTable.class);
	private static final long serialVersionUID = 1L;

	// キーと値オブジェクトのマッピングを保持するマップ
	private final ConcurrentHashMap<String, TableEntry> m_hm =
			new ConcurrentHashMap<String,TableEntry>();

	/**
	 * 新しい空のテーブルを作成します。
	 */
	public DataTable(){
	}

	/**
	 * 指定された値と指定されたキーをこのテーブルに関連付けます。
	 * 
	 * @param key 指定される値が関連付けられるキー
	 * @param date 指定される値に関連付けられる時刻
	 * @param value 指定されるキーに関連付けられる値
	 */
	public void putValue(String key, long date, Serializable value) {
		m_hm.put(key, new TableEntry(key, date, value));
	}
	
	/**
	 * エントリをこのテーブルに関連付けます。
	 * 
	 * @param entry 登録するエントリ
	 */
	public void putValue(TableEntry entry) {
		m_hm.put(entry.getKey(), entry);
	}
	
	/**
	 * エントリをこのテーブルに関連付けます。
	 * @param entries 登録するエントリの集合
	 */
	public void putValue(Collection<TableEntry> entries) {
		for (TableEntry entry : entries) {
			m_hm.put(entry.getKey(), entry);
		}
	}

	/**
	 * テーブル内のエントリをこのテーブルに関連付けます。
	 * 
	 * @param table 追加登録するテーブル
	 */
	public void putAll(DataTable table) {
		m_hm.putAll(table.m_hm);
	}

	/**
	 * 指定されたキーにマップされている値を返します。
	 * 
	 * @param key 関連付けられた値が返されるキー
	 * @return 指定されたキーにマッピングしている値オブジェクト。
	 */
	public TableEntry getValue(String key){
		TableEntry entry = m_hm.get(key);
		return entry;
	}
	
	/**
	 * 指定されたキーと一致・あるいは指定したキーで始まるデータが存在する場合trueを返す
	 * @param key
	 * @return 
	 */
	public boolean containStartWith(String key) {
		if (log.isDebugEnabled()) {
			log.debug("containStartWith() : key = " + key + "");
			log.debug("containStartWith() targetDataTable");
			log.debug(this.toString());
		}
		// 一致する場合はHashMap.containsKeyを使ったほうが圧倒的に高速なのでまずはこれで調べる
		if (m_hm.containsKey(key)) {
			return true;
		} else {
			//　一致しない場合は、指定したキーで始まるものが存在するかを調べる
			for (final String registeredKey : m_hm.keySet()) {
				if (registeredKey.startsWith(key)) {
					return true;
				}
			}
		}
		return false;
	}

	// 値が取れているか RESPONSE_NOT_FOUNDならOK(Windowsの場合など、一部の値がとれない場合があるため)
	public boolean isNoneError() {
		for (TableEntry entry : m_hm.values()) {
			if (entry.getErrorType().equals(TableEntry.ErrorType.NO_ERROR) || entry.getErrorType().equals(TableEntry.ErrorType.RESPONSE_NOT_FOUND)) {
				continue;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * キー（文字列）にマッピングされている値のうち、
	 * 指定された接頭辞で始まるキーにマッピングされている値のセットを返します。
	 * 
	 * @param prefix 接頭辞
	 * @return 指定された接頭辞で始まるキーで取得できる値のセット
	 * 値を保持しているが指定の接頭辞で始まるキーのものが存在しない場合は空のセットを返す
	 * 値をまったく保持していない場合は、nullを返す
	 */
	public Set<TableEntry> getValueSetStartWith(String prefix) {
		if (log.isDebugEnabled()) {
			log.debug("getValueSetStartWith() : prefix = " + prefix + "");
			log.debug("getValueSetStartWith() targetDataTable");
			log.debug(this.toString());
		}
		if (m_hm.size() == 0) {
			return null;
		}
		final Set<TableEntry> set = new HashSet<TableEntry>();
		// キーとされているフルのOIDを取得しそのフルOIDの文字列の先頭部分が、
		// 引数指定のOIDである場合は、その値を戻りのセットに格納する
		for (final java.util.Map.Entry<String,TableEntry> entry : m_hm.entrySet()) {
			if (entry.getKey().startsWith(prefix + ".")) {
				set.add(entry.getValue());
			}
		}
		return set;
	}

	/**
	 * キーのセットを返します。
	 * @return キーのセット
	 */
	public Set<String> keySet(){
		return this.m_hm.keySet();
	}

	/**
	 * 全てのマッピングをマップから削除します。
	 */
	public void clear(){
		m_hm.clear();
	}
	
	public String toString() {
		return m_hm.toString();
	}
}
