package com.clustercontrol.monitor.run.model;

/**
 * 監視情報の判定情報を保持するクラス<BR>
 * <p>
 * @version 3.0.0
 * @since 2.1.0
 */
public class MonitorJudgementInfo implements java.io.Serializable {
	private static final long serialVersionUID = 2684510388370616270L;

	private String message;
	private String monitorId;
	private Integer priority;
	// Truth
	private Integer truthValue; 
	// String
	private Boolean caseSensitivityFlg;
	private String description;
	private String pattern;
	private Boolean processType;
	private Boolean validFlg;
	// Numeric
	private Double thresholdLowerLimit;
	private Double thresholdUpperLimit;


	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	
	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getTruthValue() {
		return this.truthValue;
	}

	public void setTruthValue(Integer truthValue) {
		this.truthValue = truthValue;
	}

	public Boolean getCaseSensitivityFlg() {
		return this.caseSensitivityFlg;
	}
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getPattern() {
		return this.pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Boolean getProcessType() {
		return this.processType;
	}
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}

	public Boolean getValidFlg() {
		return this.validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public Double getThresholdLowerLimit() {
		return this.thresholdLowerLimit;
	}
	public void setThresholdLowerLimit(Double thresholdLowerLimit) {
		this.thresholdLowerLimit = thresholdLowerLimit;
	}

	public Double getThresholdUpperLimit() {
		return this.thresholdUpperLimit;
	}
	public void setThresholdUpperLimit(Double thresholdUpperLimit) {
		this.thresholdUpperLimit = thresholdUpperLimit;
	}

}