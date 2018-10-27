/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.dialog;

/**
 * 入力チェックの結果のダイアログクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ValidateResult {

	// ----- instance フィールド ----- //

	private boolean isValid = true;

	private String id = null;

	private String message = null;

	// ----- コンストラクタ ----- //

	public ValidateResult() {
		this.initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * このオブジェクトを初期化します。
	 */
	public void initialize() {
		this.setValid(true);
		this.setID("");
		this.setMessage("");
	}

	/**
	 * @return isValid を戻します。
	 */
	public boolean isValid() {
		return this.isValid;
	}

	/**
	 * @param isValid
	 *            isValid を設定。
	 */
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	/**
	 * @return id を戻します。
	 */
	public String getID() {
		return this.id;
	}

	/**
	 * @param id
	 *            id を設定。
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * @return message を戻します。
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @param message
	 *            message を設定。
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
