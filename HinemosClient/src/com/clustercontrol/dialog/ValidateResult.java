/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.dialog;

import com.clustercontrol.util.Messages;

/**
 * 入力チェックの結果のダイアログクラス<BR>
 * 
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
	
	/**
	 * invalidに設定したオブジェクトを返します。
	 * 
	 * @param idMessageKey IDプロパティへ設定する文字列のメッセージキー。
	 * @param contentMessageKey messageプロパティへ設定する文字列のメッセージキー。
	 * @return パラメータをもとにinvalidとして生成したオブジェクト。
	 */
	public static ValidateResult of(String idMessageKey, String contentMessageKey) {
		ValidateResult instance = new ValidateResult();
		instance.setID(Messages.getString(idMessageKey));
		instance.setMessage(Messages.getString(contentMessageKey));
		return instance;
	}

	/**
	 * メッセージを元にinvalidに設定したオブジェクトを返します。
	 * 
	 * @param idMessage IDプロパティへ設定する文字列。
	 * @param contentMessage messageプロパティへ設定する文字列。
	 * @return パラメータをもとにinvalidとして生成したオブジェクト。
	 */
	public static ValidateResult messageOf(String idMessage, String contentMessage) {
		ValidateResult instance = new ValidateResult();
		instance.setValid(false);
		instance.setID(idMessage);
		instance.setMessage(contentMessage);
		return instance;
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
