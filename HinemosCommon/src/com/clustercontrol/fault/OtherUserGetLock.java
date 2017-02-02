package com.clustercontrol.fault;

public class OtherUserGetLock extends HinemosException {

	private static final long serialVersionUID = 6861250010147043583L;

	/**
	 * コンストラクタ
	 */
	public OtherUserGetLock() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public OtherUserGetLock(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public OtherUserGetLock(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public OtherUserGetLock(String messages, Throwable e) {
		super(messages, e);
	}
}
