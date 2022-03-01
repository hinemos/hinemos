/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import org.openapitools.client.model.JobResourceInfoResponse;

import com.clustercontrol.util.Messages;

/**
 * リソース制御ジョブのアクション種別の定数定義、変換クラス
 */
public class ResourceJobActionMessage {

	/** パワーオン */
	public static final String STRING_POWERON = Messages.getString("command.poweron");
	/** パワーオフ */
	public static final String STRING_POWEROFF = Messages.getString("command.poweroff");
	/** 再起動 */
	public static final String STRING_REBOOT = Messages.getString("command.reboot");
	/** サスペンド */
	public static final String STRING_SUSPEND = Messages.getString("command.suspend");
	/** スナップショット */
	public static final String STRING_SNAPSHOT = Messages.getString("command.snapshot");
	/** アタッチ */
	public static final String STRING_ATTACH = Messages.getString("command.attach");
	/** デタッチ */
	public static final String STRING_DETACH = Messages.getString("command.detach");

	/**
	 * EnumからStringに変換する<BR>変換できなかった場合は空文字を返す
	 * @param type
	 * @return
	 */
	public static String typeEnumToString(JobResourceInfoResponse.ResourceActionEnum type) {
		switch (type) {
		case POWERON:
			return STRING_POWERON;
		case POWEROFF:
			return STRING_POWEROFF;
		case REBOOT:
			return STRING_REBOOT;
		case SUSPEND:
			return STRING_SUSPEND;
		case SNAPSHOT:
			return STRING_SNAPSHOT;
		case ATTACH:
			return STRING_ATTACH;
		case DETACH:
			return STRING_DETACH;
		}
		return "";
	}

	/**
	 * StringからEnumに変換する<BR>変換できなかった場合はnullを返す
	 * @param type
	 * @return
	 */
	public static JobResourceInfoResponse.ResourceActionEnum typeStringToEnum(String type) {
		if (STRING_POWERON.equals(type)) {
			return JobResourceInfoResponse.ResourceActionEnum.POWERON;
		} else if (STRING_POWEROFF.equals(type)) {
			return JobResourceInfoResponse.ResourceActionEnum.POWEROFF;
		} else if (STRING_REBOOT.equals(type)) {
			return JobResourceInfoResponse.ResourceActionEnum.REBOOT;
		} else if (STRING_SUSPEND.equals(type)) {
			return JobResourceInfoResponse.ResourceActionEnum.SUSPEND;
		} else if (STRING_SNAPSHOT.equals(type)) {
			return JobResourceInfoResponse.ResourceActionEnum.SNAPSHOT;
		} else if (STRING_ATTACH.equals(type)) {
			return JobResourceInfoResponse.ResourceActionEnum.ATTACH;
		} else if (STRING_DETACH.equals(type)) {
			return JobResourceInfoResponse.ResourceActionEnum.DETACH;
		}
		return null;
	}
}
