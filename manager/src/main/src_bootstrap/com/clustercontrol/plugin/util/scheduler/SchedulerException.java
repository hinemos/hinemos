package com.clustercontrol.plugin.util.scheduler;

public class SchedulerException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SchedulerException() {
		super();
	}

	public SchedulerException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SchedulerException(String message, Throwable cause) {
		super(message, cause);
	}

	public SchedulerException(String message) {
		super(message);
	}

	public SchedulerException(Throwable cause) {
		super(cause);
	}
}