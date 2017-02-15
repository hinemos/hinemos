package com.clustercontrol.monitor.run.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 各監視の結果情報を格納するクラスです。
 * 
 * @version 6.0.0
 * @since 3.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorRunResultInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 2798625217156631572L;

	/** ファシリティID */
	private String m_facilityId;

	/** 監視可否 */
	private boolean m_monitor_flg;

	/** 収集可否 */
	private boolean m_collector_flg;

	/** 収集成功か否か */
	private Boolean m_collectorResult = Boolean.TRUE;

	/** 判定結果 */
	private Integer m_checkResult = 0;

	/** 重要度 */
	private Integer m_priority = 0;

	/** 収集項目ID */
	private String m_itemCode;

	/** 収集値表示名 */
	private String m_itemName;
	
	/** 表示名 */
	private String m_displayName;

	/** メッセージ */
	private String m_message;

	/** オリジナルメッセージ */
	private String m_messageOrg;

	/** 監視値 **/
	private Object m_value;

	/** ノード 監視結果取得時刻 */
	private Long m_nodeDate = 0l;

	/** 通知グループID */
	private String m_notifyGroupId;

	/** パターンマッチ表現（文字列監視の場合、監視結果を生じさせたパターンマッチ表現） */
	private String m_patternText;

	/** 処理タイプ **/
	private Boolean m_processType = false;

	/** 今回取得したデータ（ジョブ監視で使用） **/
	private Object m_curData = null;

	/**
	 * 監視結果の重要度を返す
	 * @return 監視結果の重要度
	 */
	public Integer getCheckResult() {
		return m_checkResult;
	}

	/**
	 * 監視結果の重要度を設定する
	 * @param 監視結果の重要度
	 */
	public void setCheckResult(Integer result) {
		m_checkResult = result;
	}

	/**
	 * ファシリティIDを返す
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return m_facilityId;
	}

	/**
	 * ファシリティIDを設定する
	 * @param ファシリティID
	 */
	public void setFacilityId(String id) {
		m_facilityId = id;
	}

	/**
	 * メッセージを返す
	 * @return メッセージ
	 */
	public String getMessage() {
		return m_message;
	}

	/**
	 * メッセージを設定する
	 * @param メッセージ
	 */
	public void setMessage(String m_message) {
		this.m_message = m_message;
	}

	/**
	 * オリジナルメッセージを返す
	 * @return オリジナルメッセージ
	 */
	public String getMessageOrg() {
		return m_messageOrg;
	}

	/**
	 * オリジナルメッセージを設定する
	 * @param オリジナルメッセージ
	 */
	public void setMessageOrg(String messageOrg) {
		m_messageOrg = messageOrg;
	}

	/**
	 * 収集衆目IDを返す
	 * @return 収集衆目ID
	 */
	public String getItemCode() {
		return m_itemCode;
	}

	/**
	 * 収集衆目IDを設定する
	 * @param 収集衆目ID
	 */
	public void setItemCode(String itemCode) {
		m_itemCode = itemCode;
	}

	/**
	 * 収集値表示名を返す
	 * @return 収集値表示名
	 */
	public String getItemName() {
		return m_itemName;
	}

	/**
	 * 収集値表示名を設定する
	 * @param 収集値表示名
	 */
	public void setItemName(String itemName) {
		m_itemName = itemName;
	}

	/**
	 * 表示名を返す
	 * @return 表示名
	 */
	public String getDisplayName() {
		return m_displayName;
	}

	/**
	 * 表示名を設定する
	 * @param 表示名
	 */
	public void setDisplayName(String displayName) {
		m_displayName = displayName;
	}

	/**
	 * 監視結果取得時刻を返す
	 * @return 監視結果取得時刻
	 */
	public Long getNodeDate() {
		return m_nodeDate;
	}

	/**
	 * 監視結果取得時刻を設定する
	 * @param 監視結果取得時刻
	 */
	public void setNodeDate(Long date) {
		m_nodeDate = date;
	}

	/**
	 * 監視可否を返す
	 * @return 監視可否
	 */
	public boolean getMonitorFlg() {
		return m_monitor_flg;
	}

	/**
	 * 監視可否を設定する
	 * @param 監視可否
	 */
	public void setMonitorFlg(boolean monitor_flg) {
		this.m_monitor_flg = monitor_flg;
	}

	/**
	 * 収集可否を返す
	 * @return 監視可否
	 */
	public boolean getCollectorFlg() {
		return m_collector_flg;
	}

	/**
	 * 収集可否を設定する
	 * @param 監視可否
	 */
	public void setCollectorFlg(boolean collector_flg) {
		this.m_collector_flg = collector_flg;
	}

	/**
	 * 重要度を返す
	 * @return 重要度
	 */
	public Integer getPriority() {
		return m_priority;
	}

	/**
	 * 重要度を設定する
	 * @param 重要度
	 */
	public void setPriority(Integer priority) {
		this.m_priority = priority;
	}

	/**
	 * 通知グループIDを返す
	 * @return 通知グループID
	 */
	public String getNotifyGroupId() {
		return m_notifyGroupId;
	}

	/**
	 * 通知グループIDを設定する
	 * @param notifyGroupId
	 */
	public void setNotifyGroupId(String notifyGroupId) {
		this.m_notifyGroupId = notifyGroupId;
	}

	/**
	 * 監視値を設定する
	 * @param m_value
	 */
	public void setValue(Double m_value) {
		this.m_value = m_value;
	}

	/**
	 * 監視値を返す(Double)
	 * @param m_value
	 */
	public Double getValue() {
		return (Double)m_value;
	}

	/**
	 * 監視値を設定する(String)
	 * @param m_value
	 */
	public void setStringValue(String m_value) {
		this.m_value = m_value;
	}

	/**
	 * 監視値を返す(String)
	 * @param m_value
	 */
	public String getStringValue() {
		return (String)m_value;
	}

	/**
	 * パターンマッチ表現（文字列監視の場合、監視結果を生じさせたパターンマッチ表現）を設定する
	 * @param patternText
	 */
	public void setPatternText(String patternText) {
		this.m_patternText = patternText;
	}

	/**
	 * パターンマッチ表現（文字列監視の場合、監視結果を生じさせたパターンマッチ表現）を返す
	 * @param m_patternText
	 */
	public String getPatternText() {
		return m_patternText;
	}

	public void setProcessType(Boolean processType) {
		this.m_processType = processType;
	}

	public Boolean getProcessType() {
		return m_processType;
	}

	/**
	 * 収集が成功したか否かのフラグ設定
	 * 
	 * @param collectorResult
	 */
	public void setCollectorResult(Boolean collectorResult) {
		this.m_collectorResult = collectorResult;
	}

	/**
	 * 収集が成功したか否か
	 * 
	 * @return
	 */
	public boolean isCollectorResult(){
		if(this.m_collectorResult == null)
			return false;
		else
			return this.m_collectorResult;
	}

	/**
	 * 今回取得したデータ（ジョブ監視で使用）を返す
	 * @return 今回取得したデータ（ジョブ監視で使用）
	 */
	public Object getCurData() {
		return m_curData;
	}

	/**
	 * 今回取得したデータ（ジョブ監視で使用）を設定する
	 * @param 今回取得したデータ（ジョブ監視で使用）
	 */
	public void setCurData(Object curData) {
		m_curData = curData;
	}
}
