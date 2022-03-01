/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.sdml.model.SdmlControlStatus;
import com.clustercontrol.sdml.model.SdmlControlStatusPK;
import com.clustercontrol.sdml.util.ControlStatusUtil;
import com.clustercontrol.sdml.util.QueryUtil;
import com.clustercontrol.sdml.util.SdmlControlStatusEnum;
import com.clustercontrol.sdml.v1.bean.SdmlControlCode;
import com.clustercontrol.sdml.v1.bean.SdmlControlCode.MainCode;
import com.clustercontrol.sdml.v1.bean.SdmlControlCode.SubCode;
import com.clustercontrol.util.HinemosTime;

public class ControlStatusManager {
	private static Log logger = LogFactory.getLog(ControlStatusManager.class);

	private String applicationId;
	private String facilityId;

	public ControlStatusManager(String applicationId, String facilityId) {
		this.applicationId = applicationId;
		this.facilityId = facilityId;
	}

	/**
	 * アプリケーションIDとファシリティIDに応じた自動制御の状態を取得する
	 * 
	 * @return
	 */
	public SdmlControlStatus getSdmlControlStatus() {
		SdmlControlStatus rtn = null;
		SdmlControlStatus src = null;
		try {
			src = QueryUtil.getSdmlControlStatusPK(new SdmlControlStatusPK(applicationId, facilityId));
		} catch (SdmlControlSettingNotFound e) {
			// 初回以外は通常到達しない
			logger.debug("getSdmlControlStatus() : control status is not found. applicationId=" + applicationId
					+ ", facilityId=" + facilityId);
			return rtn;
		}
		// 新規オブジェクトに値をコピーして返却する
		rtn = new SdmlControlStatus(src.getApplicationId(), src.getFacilityId());
		ControlStatusUtil.copy(src, rtn);
		return rtn;
	}

	/**
	 * 現在の状態に対して次の状態に正常に遷移してよい制御コードかどうかチェックする
	 * 
	 * @param statusEntity
	 * @param controlCode
	 * @return true:正常 false:異常
	 */
	public boolean checkStatus(SdmlControlStatus statusEntity, SdmlControlCode controlCode) {
		if (statusEntity == null) {
			if (controlCode.getMainCode() == MainCode.Initialize && controlCode.getSubCode() == SubCode.Begin) {
				// 初回の場合はステータスがないのでOKとする
				return true;
			} else {
				// 制御コードが"Initialize_Begin"以外の場合はEntityが存在しなければいけない
				// 通常到達しない
				logger.error("checkStatus() : control status is null. applicationId=" + applicationId + ", facilityId="
						+ facilityId + ", controlCode=" + controlCode.getFullCode());
				return false;
			}
		}
		SdmlControlStatusEnum status = SdmlControlStatusEnum.valueOf(statusEntity.getStatus());
		switch (controlCode.getMainCode()) {
		case Initialize:
			switch (controlCode.getSubCode()) {
			case Begin:
				if (status == SdmlControlStatusEnum.Waiting) {
					return true;
				}
				break;
			case Set:
				// "End"と同じ
			case End:
				if (status == SdmlControlStatusEnum.Initializing) {
					return true;
				}
				break;

			default:
				break;
			}
			break;
		case Start:
			if (status == SdmlControlStatusEnum.BeforeStart) {
				return true;
			}
			break;
		case Stop:
			if (status == SdmlControlStatusEnum.Monitoring) {
				return true;
			}
			break;

		default:
			break;
		}
		if (controlCode.getMainCode() != MainCode.Initialize || controlCode.getSubCode() != SubCode.Begin) {
			// Initialize_Begin以外でここに到達するのは順番に異常がある場合
			logger.warn("checkStatus() : current code is out of order. applicationId=" + applicationId + ", facilityId="
					+ facilityId + ", lastCode=" + statusEntity.getLastControlCode() + ", currentCode="
					+ controlCode.getFullCode());
		}
		return false;
	}

	/**
	 * 開始（Initialize_Begin）から停止（Stop）までの時間が指定された時間より短いかチェックする<br>
	 * ※判定の対象はコントローラの受信日時ではなく、ログが出力された日時<br>
	 * ※通常到達しないエラーの場合は内部都合なので通知はしないようにfalseとする
	 * 
	 * @param threshold
	 * @param stopLogDate
	 * @return true:開始～停止が閾値より短い false:開始～停止が閾値より長い
	 */
	public boolean checkIsEarlyStop(SdmlControlStatus statusEntity, Long threshold, Long stopLogDate) {
		if (statusEntity == null) {
			// 通常到達しない
			logger.error("checkEarlyStop() : control status is null. applicationId=" + applicationId + ", facilityId="
					+ facilityId);
			return false;
		}
		if (statusEntity.getApplicationStartupDate() == null || stopLogDate == null) {
			// 通常到達しない
			logger.error("checkEarlyStop() : target date is null. applicationId=" + applicationId + ", facilityId="
					+ facilityId);
			return false;
		}
		Long diff = stopLogDate - statusEntity.getApplicationStartupDate();
		// 終了日時と開始日時の差をチェック
		if (diff < 0) {
			// 通常到達しない
			logger.error("checkEarlyStop() : Stop log before Initialize log. applicationId=" + applicationId
					+ ", facilityId=" + facilityId);
			return false;
		}
		if (diff == 0) {
			// 閾値が0の場合はチェックしない（＝必ず閾値が0以上）ので差が0の場合は必ずtrueとする
			return true;
		}
		if (diff / 1000 >= threshold) {
			// 閾値より長ければfalse（閾値と差が同値の場合もfalse）
			return false;
		}
		return true;
	}

	/**
	 * 制御コードに応じてステータスを更新する
	 * 
	 * @param statusEntity
	 * @param controlCode
	 * @return
	 */
	public SdmlControlStatus update(SdmlControlStatus statusEntity, SdmlControlCode controlCode) {
		return update(statusEntity, controlCode, null);
	}

	/**
	 * 制御コードに応じてステータスを更新する<br>
	 * Initialize_Beginの場合のみこちらを使う
	 * 
	 * @param statusEntity
	 * @param controlCode
	 * @param initializeBeginLogDate
	 * @return
	 */
	public SdmlControlStatus updateByInitializeBegin(SdmlControlStatus statusEntity, SdmlControlCode controlCode,
			Long initializeBeginLogDate) {
		return update(statusEntity, controlCode, initializeBeginLogDate);
	}

	private SdmlControlStatus update(SdmlControlStatus statusEntity, SdmlControlCode controlCode,
			Long initializeBeginLogDate) {
		if (statusEntity == null) {
			if (controlCode.getMainCode() == MainCode.Initialize && controlCode.getSubCode() == SubCode.Begin) {
				// 初回の場合はステータスがないので作成
				statusEntity = new SdmlControlStatus(applicationId, facilityId);
			} else {
				// 制御コードが"Initialize_Begin"以外の場合はEntityが存在しなければいけない
				// 事前にチェックもしているので通常到達しない
				logger.error("update() : control status is null. applicationId=" + applicationId + ", facilityId="
						+ facilityId + ", controlCode=" + controlCode.getFullCode());
				return null;
			}
		}
		// 共通
		statusEntity.setLastUpdateDate(HinemosTime.currentTimeMillis());
		statusEntity.setLastControlCode(controlCode.getFullCode());
		// 制御コードに応じて状態を設定
		switch (controlCode.getMainCode()) {
		case Initialize:
			switch (controlCode.getSubCode()) {
			case Begin:
				statusEntity.setStatus(SdmlControlStatusEnum.Initializing.getValue());
				// Initialize_Beginの場合のみログの出力日時をアプリケーションの起動日時として保存する
				statusEntity.setApplicationStartupDate(initializeBeginLogDate);
				break;
			case Set:
				// ステータスは更新しない
				break;
			case End:
				statusEntity.setStatus(SdmlControlStatusEnum.BeforeStart.getValue());
				break;
			default:
				// ここには到達しない
				break;
			}
			break;
		case Start:
			statusEntity.setStatus(SdmlControlStatusEnum.Monitoring.getValue());
			break;
		case Stop:
			statusEntity.setStatus(SdmlControlStatusEnum.Waiting.getValue());
			break;
		case Error:
			statusEntity.setStatus(SdmlControlStatusEnum.Waiting.getValue());
			break;
		case Warning:
			// ステータスは更新しない
			break;
		case Info:
			// ステータスは更新しない
			break;
		default:
			// ここには到達しない
			break;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("update() : updated. applicationId=" + applicationId + ", facilityId=" + facilityId
					+ ", currentStatus=" + SdmlControlStatusEnum.valueOf(statusEntity.getStatus()));
		}
		return statusEntity;
	}

	/**
	 * 指定した状態に強制的に変更する
	 * 
	 * @param statusEntity
	 * @param controlCode
	 * @param status
	 * @return
	 */
	public SdmlControlStatus forcedUpdate(SdmlControlStatus statusEntity, SdmlControlCode controlCode,
			SdmlControlStatusEnum status) {
		if (statusEntity == null) {
			// ない場合は作成（初回からInitialize_Begin以外が来る可能性を考慮）
			statusEntity = new SdmlControlStatus(applicationId, facilityId);
		}
		statusEntity.setLastUpdateDate(HinemosTime.currentTimeMillis());
		statusEntity.setLastControlCode(controlCode.getFullCode());
		statusEntity.setStatus(status.getValue());

		logger.info("forcedUpdate() : updated to " + status.name() + ". applicationId=" + applicationId
				+ ", facilityId=" + facilityId);
		return statusEntity;
	}
}
