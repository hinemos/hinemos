package com.clustercontrol.fault;

public class LogFormatDuplicate extends HinemosException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4477541039576179801L;


	private String logFormatId = null;

	/**
	 * LogformatNotFoundコンストラクタ
	 */
	public LogFormatDuplicate() {
		super();
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param messages
	 */
	public LogFormatDuplicate(String messages) {
		super(messages);
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param e
	 */
	public LogFormatDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public LogFormatDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getLogFormatId() {
		return logFormatId;
	}

	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}

}
