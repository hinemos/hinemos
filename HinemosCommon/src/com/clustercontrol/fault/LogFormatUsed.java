package com.clustercontrol.fault;

public class LogFormatUsed extends HinemosException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4477541039576179801L;


	private String logFormatId = null;

	/**
	 * LogformatNotFoundコンストラクタ
	 */
	public LogFormatUsed() {
		super();
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param messages
	 */
	public LogFormatUsed(String messages) {
		super(messages);
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param e
	 */
	public LogFormatUsed(Throwable e) {
		super(e);
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public LogFormatUsed(String messages, Throwable e) {
		super(messages, e);
	}

	public String getLogFormatId() {
		return logFormatId;
	}

	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}

}
