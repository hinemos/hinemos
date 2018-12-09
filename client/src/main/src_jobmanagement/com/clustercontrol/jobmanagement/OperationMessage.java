/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement;

import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.util.Messages;


/**
 * ジョブの操作種別を定数として定義するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class OperationMessage {

	/** 開始[即時] */
	public static final String STRING_START_AT_ONCE = Messages.getString("job.start.at.once");
	/** 開始[中断解除] */
	public static final String STRING_START_SUSPEND = Messages.getString("job.start.release.suspend");
	/** 開始[スキップ解除] */
	public static final String STRING_START_SKIP = Messages.getString("job.start.release.skip");
	/** 開始[保留解除] */
	public static final String STRING_START_WAIT = Messages.getString("job.start.release.reserve");

	/** 停止[コマンド] */
	public static final String STRING_STOP_AT_ONCE = Messages.getString("job.stop.at.once");
	/** 停止[中断] */
	public static final String STRING_STOP_SUSPEND = Messages.getString("job.stop.suspend");
	/** 停止[スキップ] */
	public static final String STRING_STOP_SKIP = Messages.getString("job.stop.skip");
	/** 停止[保留] */
	public static final String STRING_STOP_WAIT = Messages.getString("job.stop.reserve");
	/** 停止[状態変更] */
	public static final String STRING_STOP_MAINTENANCE = Messages.getString("job.stop.maintenance");
	/** 停止[状態指定] */
	public static final String STRING_STOP_SET_END_VALUE = Messages.getString("job.stop.set.end.value");
	/** 停止[強制] */
	public static final String STRING_STOP_FORCE = Messages.getString("job.stop.force");

	/**
	 * 種別から文字列に変換する。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type){
		if(type == OperationConstant.TYPE_START_AT_ONCE){
			return STRING_START_AT_ONCE;
		}
		else if(type == OperationConstant.TYPE_START_SUSPEND){
			return STRING_START_SUSPEND;
		}
		else if(type == OperationConstant.TYPE_START_SKIP){
			return STRING_START_SKIP;
		}
		else if(type == OperationConstant.TYPE_START_WAIT){
			return STRING_START_WAIT;
		}
		else if(type == OperationConstant.TYPE_STOP_AT_ONCE){
			return STRING_STOP_AT_ONCE;
		}
		else if(type == OperationConstant.TYPE_STOP_SUSPEND){
			return STRING_STOP_SUSPEND;
		}
		else if(type == OperationConstant.TYPE_STOP_SKIP){
			return STRING_STOP_SKIP;
		}
		else if(type == OperationConstant.TYPE_STOP_WAIT){
			return STRING_STOP_WAIT;
		}
		else if(type == OperationConstant.TYPE_STOP_MAINTENANCE){
			return STRING_STOP_MAINTENANCE;
		}
		else if(type == OperationConstant.TYPE_STOP_SET_END_VALUE){
			return STRING_STOP_SET_END_VALUE;
		}
		else if(type == OperationConstant.TYPE_STOP_FORCE){
			return STRING_STOP_FORCE;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換する。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string){
		if(string.equals(STRING_START_AT_ONCE)){
			return OperationConstant.TYPE_START_AT_ONCE;
		}
		else if(string.equals(STRING_START_SUSPEND)){
			return OperationConstant.TYPE_START_SUSPEND;
		}
		else if(string.equals(STRING_START_SKIP)){
			return OperationConstant.TYPE_START_SKIP;
		}
		else if(string.equals(STRING_START_WAIT)){
			return OperationConstant.TYPE_START_WAIT;
		}
		else if(string.equals(STRING_STOP_AT_ONCE)){
			return OperationConstant.TYPE_STOP_AT_ONCE;
		}
		else if(string.equals(STRING_STOP_SUSPEND)){
			return OperationConstant.TYPE_STOP_SUSPEND;
		}
		else if(string.equals(STRING_STOP_SKIP)){
			return OperationConstant.TYPE_STOP_SKIP;
		}
		else if(string.equals(STRING_STOP_WAIT)){
			return OperationConstant.TYPE_STOP_WAIT;
		}
		else if(string.equals(STRING_STOP_MAINTENANCE)){
			return OperationConstant.TYPE_STOP_MAINTENANCE;
		}
		else if(string.equals(STRING_STOP_SET_END_VALUE)){
			return OperationConstant.TYPE_STOP_SET_END_VALUE;
		}
		else if(string.equals(STRING_STOP_FORCE)){
			return OperationConstant.TYPE_STOP_FORCE;
		}
		return -1;
	}
}
