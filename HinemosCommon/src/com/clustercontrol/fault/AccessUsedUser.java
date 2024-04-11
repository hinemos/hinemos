package com.clustercontrol.fault;

public class AccessUsedUser extends HinemosUsed {

	private static final long serialVersionUID = 1L;

	/**
	 * AccessUsedUserコンストラクタ
	 * @param messages
	 * @param e
	 */
	public AccessUsedUser(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * AccessUsedUserコンストラクタ
	 * @param messages
	 */
	public AccessUsedUser(String messages) {
		super(messages);
	}

	/**
	 * AccessUsedUserコンストラクタ
	 * @param e
	 */
	public AccessUsedUser(Throwable e) {
		super(e);
	}


}
