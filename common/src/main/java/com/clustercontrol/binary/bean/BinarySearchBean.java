/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

/**
 * バイナリ検索用Bean.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinarySearchBean {

	/** 検索種別 */
	private BinaryConstant.SearchType searchType;

	/** 検索種別エラーの分類 */
	private BinaryConstant.SearchError searchError;

	/** 0x抜きの16進数文字列(検索種別がHEX以外の場合はnull) */
	private String onlyHexString;

	/** 検索種別 */
	public BinaryConstant.SearchType getSearchType() {
		return searchType;
	}

	/** 検索種別 */
	public void setSearchType(BinaryConstant.SearchType searchType) {
		this.searchType = searchType;
	}

	/** 検索種別エラーの分類 */
	public BinaryConstant.SearchError getSearchError() {
		return searchError;
	}

	/** 検索種別エラーの分類 */
	public void setSearchError(BinaryConstant.SearchError searchError) {
		this.searchError = searchError;
	}

	/** 0x抜きの16進数文字列(検索種別がHEX以外の場合はnull) */
	public String getOnlyHexString() {
		return onlyHexString;
	}

	/** 0x抜きの16進数文字列(検索種別がHEX以外の場合はnull) */
	public void setOnlyHexString(String onlyHexString) {
		this.onlyHexString = onlyHexString;
	}
}
