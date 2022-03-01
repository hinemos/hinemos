/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ジョブの終了状態に関する情報を保持するクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobEndStatusInfo implements Serializable, Comparable<JobEndStatusInfo> {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 5256607875379422805L;

	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobEndStatusInfo.class );

	/** 終了状態の種別 */
	private Integer type = 0;

	/** 終了状態の終了値 */
	private Integer value = 0;

	/** 終了値範囲(開始) */
	private Integer startRangeValue = 0;

	/** 終了値範囲(終了) */
	private Integer endRangeValue = 0;

	/**
	 * 終了値範囲(終了)を返す
	 * @return 終了値範囲(終了)
	 */
	public Integer getEndRangeValue() {
		return endRangeValue;
	}

	/**
	 * 終了値範囲(終了)を設定する。<BR>
	 * @param endRangeValue 終了値範囲(終了)
	 */
	public void setEndRangeValue(Integer endRangeValue) {
		this.endRangeValue = endRangeValue;
	}

	/**
	 * 終了値範囲(開始)を返す。<BR>
	 * @return 終了値範囲(開始)
	 */
	public Integer getStartRangeValue() {
		return startRangeValue;
	}

	/**
	 * 終了値範囲(開始)を設定する。<BR>
	 * @param startRangeValue 終了値範囲(開始)
	 */
	public void setStartRangeValue(Integer startRangeValue) {
		this.startRangeValue = startRangeValue;
	}

	/**
	 * 終了状態の種別を返す。<BR>
	 * @return 終了状態の種別
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * 終了状態の種別を設定する。<BR>
	 * @param type 終了状態の種別
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * 終了状態の終了値を返す。<BR>
	 * @return 終了状態の終了値
	 */
	public Integer getValue() {
		return value;
	}

	/**
	 * 終了状態の終了値を設定する。<BR>
	 * @param value 終了状態の終了値
	 */
	public void setValue(Integer value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((endRangeValue == null) ? 0 : endRangeValue.hashCode());
		result = prime
				* result
				+ ((startRangeValue == null) ? 0 : startRangeValue
						.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobEndStatusInfo)) {
			return false;
		}
		JobEndStatusInfo o1 = this;
		JobEndStatusInfo o2 = (JobEndStatusInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getType(), o2.getType()) &&
				equalsSub(o1.getValue(), o2.getValue()) &&
				equalsSub(o1.getStartRangeValue(), o2.getStartRangeValue()) &&
				equalsSub(o1.getEndRangeValue(), o2.getEndRangeValue());

		if (!ret) {
			m_log.debug("type = " + equalsSub(o1.getType(), o2.getType()));
			m_log.debug("value = " + equalsSub(o1.getValue(), o2.getValue()));
			m_log.debug("startRange = " + equalsSub(o1.getStartRangeValue(), o2.getStartRangeValue()));
			m_log.debug("endRange = " + equalsSub(o1.getEndRangeValue(), o2.getEndRangeValue()));
		}
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		
		if (o1 == null)
			return false;
		
		return o1.equals(o2);
	}

	@Override
	public int compareTo(JobEndStatusInfo o) {
		return this.getType() - o.getType();
	}

	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}
	/**
	 * 単体テスト
	 */
	public static void testEquals(){

		System.out.println("=== JobEndStatusInfo の単体テスト ===");

		System.out.println("*** 全部一致 ***");
		JobEndStatusInfo info1 = new JobEndStatusInfo();
		info1.setType(0);
		info1.setValue(0);
		info1.setStartRangeValue(0);
		info1.setEndRangeValue(0);

		JobEndStatusInfo info2 = new JobEndStatusInfo();
		info2.setType(0);
		info2.setValue(0);
		info2.setStartRangeValue(0);
		info2.setEndRangeValue(0);

		judge(true,info1.equals(info2));

		System.out.println("*** 「終了状態の種別」のみ違う ***");
		info2 = new JobEndStatusInfo();
		info2.setType(1);
		info2.setValue(0);
		info2.setStartRangeValue(0);
		info2.setEndRangeValue(0);

		judge(false,info1.equals(info2));

		System.out.println("*** 「終了状態の終了値」のみ違う ***");
		info2 = new JobEndStatusInfo();
		info2.setType(0);
		info2.setValue(1);
		info2.setStartRangeValue(0);
		info2.setEndRangeValue(0);

		judge(false,info1.equals(info2));

		System.out.println("*** 「終了値範囲(開始)」のみ違う ***");
		info2 = new JobEndStatusInfo();
		info2.setType(0);
		info2.setValue(0);
		info2.setStartRangeValue(1);
		info2.setEndRangeValue(0);

		judge(false,info1.equals(info2));

		System.out.println("*** 「終了値範囲(終了)」のみ違う ***");
		info2 = new JobEndStatusInfo();
		info2.setType(0);
		info2.setValue(0);
		info2.setStartRangeValue(0);
		info2.setEndRangeValue(1);

		judge(false,info1.equals(info2));
	}

	public static JobEndStatusInfo createSampleInfo() {
		JobEndStatusInfo info = new JobEndStatusInfo();
		info.setType(0);
		info.setValue(0);
		info.setStartRangeValue(0);
		info.setEndRangeValue(0);
		return info;
	}
	/**
	 * 単体テストの結果が正しいものか判断する
	 * @param judge
	 * @param result
	 */
	private static void judge(boolean judge, boolean result){

		System.out.println("expect : " + judge);
		System.out.print("result : " + result);
		String ret = "NG";
		if (judge == result) {
			ret = "OK";
		}
		System.out.println("    is ...  " + ret);
	}
}