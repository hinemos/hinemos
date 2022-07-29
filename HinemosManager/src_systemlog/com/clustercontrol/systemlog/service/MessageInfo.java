/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.service;

/**
 * syslogメッセージ情報を保持するクラス<BR>
 * メッセージのホスト名、発生日時、メッセージを格納します。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class MessageInfo {

	/** ホスト名。 */
    private String hostName;

	/** 発生日時。 */
    private Long generationDate;

	/** メッセージ。 */
    private String message;


	/**
	 * 発生日時を取得します。<BR>
	 * 
	 * @return 発生日時
	 */
	public Long getGenerationDate() {
        return generationDate;
	}

	/**
	 * 発生日時を設定します。<BR>
	 * 
	 * @param generationDate 発生日時
	 */
	public void setGenerationDate(Long generationDate) {
        this.generationDate = generationDate;
	}

	/**
	 * ホスト名を取得します。<BR>
	 * 
	 * @return ホスト名
	 */
	public String getHostName() {
        return hostName;
	}

	/**
	 * ホスト名を設定します。<BR>
	 * 
	 * @param hostName ホスト名
	 */
	public void setHostName(String hostName) {
        this.hostName = hostName;
	}

	/**
	 * メッセージを取得します。<BR>
	 * 
	 * @return メッセージ
	 */
	public String getMessage() {
        return message;
	}

	/**
	 * メッセージを設定します。<BR>
	 * 
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
        this.message = message;
	}
}
