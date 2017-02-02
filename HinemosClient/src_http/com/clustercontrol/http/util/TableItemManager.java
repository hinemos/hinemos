/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.http.util;

import java.util.LinkedList;
import java.util.List;

/**
 * テーブルアイテムの判定情報を管理するクラス<BR>
 * シングルトン。
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class TableItemManager<T> {

	/** 順序を管理する判定情報のリスト。 */
	private List<T> m_orderList = null;

	/**
	 * コンストラクタ。<BR>
	 * 初期処理を行います。
	 */
	public TableItemManager() {
		this(new LinkedList<T>());
	}

	public TableItemManager(List<T> orderList) {
		initialize(orderList);
	}

	public void initialize(List<T> orderList){
		this.m_orderList = new LinkedList<T>(orderList);
	}

	/**
	 * 全ての判定情報の配列を返します。
	 * <p>
	 * 順序の番号順に整列した配列を返します。
	 * 
	 * @return 判定情報一覧
	 */
	public Object[] get() {
		return m_orderList.toArray();
	}

	/**
	 * 全ての判定情報のリストを返します。
	 * <p>
	 * 順序の番号順に整列した配列を返します。
	 * 
	 * @return 判定情報一覧
	 */
	public List<T> getTableItemInfoList() {
		return m_orderList;
	}

	/**
	 * 引数で指定した判定情報を追加します。
	 * <p>
	 * 
	 * @param info 判定情報
	 * @return 成功した場合、<code> true </code>
	 */
	public boolean add(T info) {
		this.m_orderList.add(info);
		return true;
	}

	/**
	 * 引数で指定した判定情報を変更します。
	 * <p>
	 * 
	 * @param info 判定情報
	 * @return 成功した場合、<code> true </code>
	 */
	public boolean modify(T oldItem, T newItem) {
		if(!this.m_orderList.contains(oldItem)){
			return false;
		}

		this.m_orderList.set(m_orderList.indexOf(oldItem), newItem);
		return true;
	}

	/**
	 * 引数で指定した判定情報を削除します。
	 * <p>
	 * 
	 * @param info 判定情報
	 * @return 成功した場合、<code> true </code>
	 */
	public boolean delete(T info) {
		if(!this.m_orderList.contains(info)){
			return false;
		}

		this.m_orderList.remove(info);

		return true;
	}

	/**
	 * 引数で指定した判定情報の順序をひとつ上げます。
	 * 
	 * @param info 判定情報ー
	 * @return 成功した場合、<code> true </code>
	 * 
	 * @see #change(int, int)
	 */
	public boolean upOrder(T info) {
		if(!this.m_orderList.contains(info)){
			return false;
		}

		int oldOrder = this.m_orderList.indexOf(info);
		int newOrder = oldOrder - 1;
		if (newOrder < 0) {
			return false;
		}

		return this.change(oldOrder, newOrder);
	}

	/**
	 * 引数で指定した判定情報の順序をひとつ下げます。
	 * 
	 * @param info 判定情報
	 * @return 成功した場合、<code> true </code>
	 * 
	 * @see #change(int, int)
	 */
	public boolean downOrder(T info) {
		if(!this.m_orderList.contains(info)){
			return false;
		}

		int oldOrder = this.m_orderList.indexOf(info);
		int newOrder = oldOrder + 1;
		if (newOrder >= this.m_orderList.size()) {
			return false;
		}

		return this.change(oldOrder, newOrder);
	}

	/**
	 * 引数で指定した順序の判定情報同士の順序を入れ替えます。
	 * <p>
	 * 
	 * @param index1 判定情報１のindex
	 * @param index2 判定情報２のindex
	 * @return 正常に終了した場合、<code> true </code>
	 */
	private boolean change(int index1, int index2) {
		T info1 = this.m_orderList.get(index1);
		T info2 = this.m_orderList.get(index2);

		// リストの位置を入れ替えます。
		this.m_orderList.set(index1, info2);
		this.m_orderList.set(index2, info1);

		return true;
	}

	/**
	 * 引数で指定した判定情報のindexを返します。
	 * <p>
	 * 
	 * @param info 判定情報
	 * @return 判定情報のindex
	 */
	public int indexOf(T info) {
		return this.m_orderList.indexOf(info);
	}
}
