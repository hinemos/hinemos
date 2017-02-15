package com.clustercontrol.mbean;

import java.beans.ConstructorProperties;

public class TablePhysicalSizes {
	private long log_cc_collect_data_raw;
	private long log_cc_collect_data_string;
	private long log_cc_collect_data_tag;
	private long log_cc_collect_summary_day;
	private long log_cc_collect_summary_hour;
	private long log_cc_collect_summary_month;
	private long log_cc_event_log;
	private long log_cc_job_info;
	private long log_cc_job_param_info;
	private long log_cc_job_session;
	private long log_cc_job_session_job;
	private long log_cc_job_session_node;
	private long log_cc_job_start_job_info;
	private long log_cc_status_info;

	@ConstructorProperties({
		"log_cc_collect_data_raw",
		"log_cc_collect_data_string",
		"log_cc_collect_data_tag",
		"log_cc_collect_summary_day",
		"log_cc_collect_summary_hour", 
		"log_cc_collect_summary_month",
		"log_cc_event_log", 
		"log_cc_job_info",
		"log_cc_job_param_info",
		"log_cc_job_session",
		"log_cc_job_session",
		"log_cc_job_session_job",
		"log_cc_job_start_job_info",
		"log_cc_status_info"})
	public TablePhysicalSizes(
			long log_cc_collect_data_raw,
			long log_cc_collect_data_string,
			long log_cc_collect_data_tag,
			long log_cc_collect_summary_day,
			long log_cc_collect_summary_hour,
			long log_cc_collect_summary_month,
			long log_cc_event_log,
			long log_cc_job_info,
			long log_cc_job_param_info,
			long log_cc_job_session,
			long log_cc_job_session_job,
			long log_cc_job_session_node,
			long log_cc_job_start_job_info,
			long log_cc_status_info
			) {
			this.log_cc_collect_data_raw = log_cc_collect_data_raw;
			this.log_cc_collect_summary_day = log_cc_collect_summary_day;
			this.log_cc_collect_summary_hour = log_cc_collect_summary_hour;
			this.log_cc_collect_summary_month = log_cc_collect_summary_month;
			this.log_cc_event_log = log_cc_event_log;
			this.log_cc_job_info = log_cc_job_info;
			this.log_cc_job_param_info = log_cc_job_param_info;
			this.log_cc_job_session = log_cc_job_session;
			this.log_cc_job_session_job = log_cc_job_session_job;
			this.log_cc_job_session_node = log_cc_job_session_node;
			this.log_cc_job_start_job_info = log_cc_job_start_job_info;
			this.log_cc_status_info = log_cc_status_info;
	}

	/**
	 * @return the log_cc_collect_data_raw
	 */
	public long getLog_cc_collect_data_raw() {
		return log_cc_collect_data_raw;
	}

	/**
	 * @return the log_cc_collect_data_string
	 */
	public long getLog_cc_collect_data_string() {
		return log_cc_collect_data_string;
	}

	/**
	 * @return the log_cc_collect_data_tag
	 */
	public long getLog_cc_collect_data_tag() {
		return log_cc_collect_data_tag;
	}

	/**
	 * @return the log_cc_collect_summary_day
	 */
	public long getLog_cc_collect_summary_day() {
		return log_cc_collect_summary_day;
	}

	/**
	 * @return the log_cc_collect_summary_hour
	 */
	public long getLog_cc_collect_summary_hour() {
		return log_cc_collect_summary_hour;
	}

	/**
	 * @return the log_cc_collect_summary_month
	 */
	public long getLog_cc_collect_summary_month() {
		return log_cc_collect_summary_month;
	}

	/**
	 * @return the log_cc_event_log
	 */
	public long getLog_cc_event_log() {
		return log_cc_event_log;
	}

	/**
	 * @return the log_cc_job_info
	 */
	public long getLog_cc_job_info() {
		return log_cc_job_info;
	}

	/**
	 * @return the log_cc_job_param_info
	 */
	public long getLog_cc_job_param_info() {
		return log_cc_job_param_info;
	}

	/**
	 * @return the log_cc_job_session
	 */
	public long getLog_cc_job_session() {
		return log_cc_job_session;
	}

	/**
	 * @return the log_cc_job_session_job
	 */
	public long getLog_cc_job_session_job() {
		return log_cc_job_session_job;
	}

	/**
	 * @return the log_cc_job_session_node
	 */
	public long getLog_cc_job_session_node() {
		return log_cc_job_session_node;
	}

	/**
	 * @return the log_cc_job_start_job_info
	 */
	public long getLog_cc_job_start_job_info() {
		return log_cc_job_start_job_info;
	}

	/**
	 * @return the log_cc_status_info
	 */
	public long getLog_cc_status_info() {
		return log_cc_status_info;
	}
}