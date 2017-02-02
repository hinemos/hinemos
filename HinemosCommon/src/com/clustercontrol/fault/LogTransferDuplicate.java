package com.clustercontrol.fault;

public class LogTransferDuplicate extends HinemosException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -744631319212377520L;
	private String transferId = null;

	/**
	 * LogTransferNotFoundコンストラクタ
	 */
	public LogTransferDuplicate() {
		super();
	}

	/**
	 * LogTransferNotFoundコンストラクタ
	 * @param messages
	 */
	public LogTransferDuplicate(String messages) {
		super(messages);
	}

	/**
	 * LogTransferNotFoundコンストラクタ
	 * @param e
	 */
	public LogTransferDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * LogTransferNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public LogTransferDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getTransferId() {
		return transferId;
	}

	public void setTransferId(String transferId) {
		this.transferId = transferId;
	}
}
