/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.infra.util;

import org.eclipse.swt.widgets.Combo;
import org.openapitools.client.model.InfraManagementInfoResponse.AbnormalPriorityCheckEnum;
import org.openapitools.client.model.InfraManagementInfoResponse.AbnormalPriorityRunEnum;
import org.openapitools.client.model.InfraManagementInfoResponse.NormalPriorityCheckEnum;
import org.openapitools.client.model.InfraManagementInfoResponse.NormalPriorityRunEnum;
import org.openapitools.client.model.InfraManagementInfoResponse.StartPriorityEnum;

import com.clustercontrol.bean.PriorityMessage;

/**
 * 通知の重要度の Enum と文字列を変換するクラス
 */
public class NotiryPriorityConverter {

	/**
	 * [開始]の重要度について Enum -> 文字列の変換
	 */
	public static void setSelectStartPriority(Combo combo, StartPriorityEnum priority) {
		String select = "";

		if (StartPriorityEnum.CRITICAL.equals(priority)) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (StartPriorityEnum.UNKNOWN.equals(priority)) {
			select = PriorityMessage.STRING_UNKNOWN;
		} else if (StartPriorityEnum.WARNING.equals(priority)) {
			select = PriorityMessage.STRING_WARNING;
		} else if (StartPriorityEnum.INFO.equals(priority)) {
			select = PriorityMessage.STRING_INFO;
		} else if (StartPriorityEnum.NONE.equals(priority)) {
			select = PriorityMessage.STRING_NONE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * [開始]の重要度について 文字列 -> Enumの変換
	 */
	public static StartPriorityEnum getSelectStartPriority(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return StartPriorityEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return StartPriorityEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return StartPriorityEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return StartPriorityEnum.UNKNOWN;
		} else if (select.equals(PriorityMessage.STRING_NONE)) {
			return StartPriorityEnum.NONE;
		}

		return null;
	}

	/**
	 * [実行正常]の重要度について Enum -> 文字列の変換
	 */
	public static void setSelectNormalPriorityRun(Combo combo, NormalPriorityRunEnum priority) {
		String select = "";

		if (NormalPriorityRunEnum.CRITICAL.equals(priority)) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (NormalPriorityRunEnum.UNKNOWN.equals(priority)) {
			select = PriorityMessage.STRING_UNKNOWN;
		} else if (NormalPriorityRunEnum.WARNING.equals(priority)) {
			select = PriorityMessage.STRING_WARNING;
		} else if (NormalPriorityRunEnum.INFO.equals(priority)) {
			select = PriorityMessage.STRING_INFO;
		} else if (NormalPriorityRunEnum.NONE.equals(priority)) {
			select = PriorityMessage.STRING_NONE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * [実行正常]の重要度について 文字列 -> Enumの変換
	 */
	public static NormalPriorityRunEnum getSelectNormalPriorityRun(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return NormalPriorityRunEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return NormalPriorityRunEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return NormalPriorityRunEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return NormalPriorityRunEnum.UNKNOWN;
		} else if (select.equals(PriorityMessage.STRING_NONE)) {
			return NormalPriorityRunEnum.NONE;
		}

		return null;
	}

	/**
	 * [実行異常]の重要度について Enum -> 文字列の変換
	 */
	public static void setSelectAbnormalPriorityRun(Combo combo, AbnormalPriorityRunEnum priority) {
		String select = "";

		if (AbnormalPriorityRunEnum.CRITICAL.equals(priority)) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (AbnormalPriorityRunEnum.UNKNOWN.equals(priority)) {
			select = PriorityMessage.STRING_UNKNOWN;
		} else if (AbnormalPriorityRunEnum.WARNING.equals(priority)) {
			select = PriorityMessage.STRING_WARNING;
		} else if (AbnormalPriorityRunEnum.INFO.equals(priority)) {
			select = PriorityMessage.STRING_INFO;
		} else if (AbnormalPriorityRunEnum.NONE.equals(priority)) {
			select = PriorityMessage.STRING_NONE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * [実行異常]の重要度について 文字列 -> Enumの変換
	 */
	public static AbnormalPriorityRunEnum getSelectAbnormalPriorityRun(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return AbnormalPriorityRunEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return AbnormalPriorityRunEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return AbnormalPriorityRunEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return AbnormalPriorityRunEnum.UNKNOWN;
		} else if (select.equals(PriorityMessage.STRING_NONE)) {
			return AbnormalPriorityRunEnum.NONE;
		}

		return null;
	}

	/**
	 * [チェック正常]の重要度について Enum -> 文字列の変換
	 */
	public static void setSelectNormalPriorityCheck(Combo combo, NormalPriorityCheckEnum priority) {
		String select = "";

		if (NormalPriorityCheckEnum.CRITICAL.equals(priority)) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (NormalPriorityCheckEnum.UNKNOWN.equals(priority)) {
			select = PriorityMessage.STRING_UNKNOWN;
		} else if (NormalPriorityCheckEnum.WARNING.equals(priority)) {
			select = PriorityMessage.STRING_WARNING;
		} else if (NormalPriorityCheckEnum.INFO.equals(priority)) {
			select = PriorityMessage.STRING_INFO;
		} else if (NormalPriorityCheckEnum.NONE.equals(priority)) {
			select = PriorityMessage.STRING_NONE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * [チェック正常]の重要度について 文字列 -> Enumの変換
	 */
	public static NormalPriorityCheckEnum getSelectNormalPriorityCheck(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return NormalPriorityCheckEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return NormalPriorityCheckEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return NormalPriorityCheckEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return NormalPriorityCheckEnum.UNKNOWN;
		} else if (select.equals(PriorityMessage.STRING_NONE)) {
			return NormalPriorityCheckEnum.NONE;
		}

		return null;
	}

	/**
	 * [チェック異常]の重要度について Enum -> 文字列の変換
	 */
	public static void setSelectAbnormalPriorityCheck(Combo combo, AbnormalPriorityCheckEnum priority) {
		String select = "";

		if (AbnormalPriorityCheckEnum.CRITICAL.equals(priority)) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (AbnormalPriorityCheckEnum.UNKNOWN.equals(priority)) {
			select = PriorityMessage.STRING_UNKNOWN;
		} else if (AbnormalPriorityCheckEnum.WARNING.equals(priority)) {
			select = PriorityMessage.STRING_WARNING;
		} else if (AbnormalPriorityCheckEnum.INFO.equals(priority)) {
			select = PriorityMessage.STRING_INFO;
		} else if (AbnormalPriorityCheckEnum.NONE.equals(priority)) {
			select = PriorityMessage.STRING_NONE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * [チェック異常]の重要度について 文字列 -> Enumの変換
	 */
	public static AbnormalPriorityCheckEnum getSelectAbnormalPriorityCheck(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return AbnormalPriorityCheckEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return AbnormalPriorityCheckEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return AbnormalPriorityCheckEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return AbnormalPriorityCheckEnum.UNKNOWN;
		} else if (select.equals(PriorityMessage.STRING_NONE)) {
			return AbnormalPriorityCheckEnum.NONE;
		}

		return null;
	}
}
