/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.msgfilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MsgFilterClientOptionManager {
	private static Log logger = LogFactory.getLog(MsgFilterClientOptionManager.class);

	private static final MsgFilterClientOptionManager instance = new MsgFilterClientOptionManager();

	/** オプション管理用 */
	private IMsgFilterClientOption msgFilterClientOption = null;

	/**
	 * コンストラクタ<BR>
	 * getInstance()を利用すること
	 */
	private MsgFilterClientOptionManager() {
	}

	/**
	 * インスタンス取得
	 * 
	 * @return
	 */
	public static MsgFilterClientOptionManager getInstance() {
		return instance;
	}

	
	public IMsgFilterClientOption getMsgFilterClientOption() {
		return msgFilterClientOption;
	}

	public void setMsgFilterClientOption(IMsgFilterClientOption option) {
		logger.info("setMsgFilterClientOption() : " + option.getUrl());
		this.msgFilterClientOption = option;
	}
}
