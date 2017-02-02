package com.clustercontrol.fault;

public class InvalidSetting extends HinemosException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7222304609607635089L;

	/**
	 * InvalidSettingのコンストラクタ
	 */
	public InvalidSetting() {
		super();
	}

	/**
	 * InvalidSettingのコンストラクタ
	 * @param messages
	 */
	public InvalidSetting(String messages) {
		super(messages);
	}

	/**
	 * InvalidSettingのコンストラクタ
	 * @param e
	 */
	public InvalidSetting(Throwable e) {
		super(e);
	}

	/**
	 * InvalidSettingのコンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidSetting(String messages, Throwable e) {
		super(messages, e);
	}

}
