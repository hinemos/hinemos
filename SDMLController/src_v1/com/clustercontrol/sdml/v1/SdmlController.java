/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.SdmlControlSettingDuplicate;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.sdml.factory.ModifySdmlControl;
import com.clustercontrol.sdml.factory.SelectSdmlControl;
import com.clustercontrol.sdml.model.SdmlControlMonitorRelation;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlControlStatus;
import com.clustercontrol.sdml.model.SdmlInitializeData;
import com.clustercontrol.sdml.util.ControlStatusUtil;
import com.clustercontrol.sdml.util.InitializeDataUtil;
import com.clustercontrol.sdml.util.SdmlControlStatusEnum;
import com.clustercontrol.sdml.util.SdmlUtil;
import com.clustercontrol.sdml.v1.bean.AutoMonitorValidInfo;
import com.clustercontrol.sdml.v1.bean.SdmlControlCode;
import com.clustercontrol.sdml.v1.bean.SdmlControlLogDTO;
import com.clustercontrol.sdml.v1.constant.InitializeKeyEnum;
import com.clustercontrol.sdml.v1.factory.ControlStatusManager;
import com.clustercontrol.sdml.v1.factory.InitializeDataManager;
import com.clustercontrol.sdml.v1.factory.SdmlMonitorIdGenerator;
import com.clustercontrol.sdml.v1.util.ControlLogUtil;
import com.clustercontrol.sdml.v1.util.SdmlMonitorUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;

/**
 * 受け取ったSDML制御ログに応じた制御を実行するメインのクラス
 */
public class SdmlController {
	private static Log logger = LogFactory.getLog(SdmlController.class);

	private String facilityId;
	private SdmlControlLogDTO logDto;
	private SdmlControlSettingInfo controlSetting;
	private SdmlControlCode controlCode;

	private InitializeDataManager initializeDataManager;
	private ControlStatusManager controlStatusManager;

	private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

	/**
	 * メイン処理
	 * 
	 * @param facilityId
	 * @param logDto
	 * @return
	 * @throws HinemosUnknown
	 *             ※OutputBasicInfoの作成でエラーが出た場合は通知できないのでthrowする
	 */
	public List<OutputBasicInfo> run(String facilityId, SdmlControlLogDTO logDto) throws HinemosUnknown {
		logger.debug("run() : start. log=" + logDto.getOrgLogLine());
		List<OutputBasicInfo> rtn = new ArrayList<>();
		this.facilityId = facilityId;
		this.logDto = logDto;

		try {
			// アプリケーションIDからSDML制御設定を取得
			this.controlSetting = new SelectSdmlControl().getSdmlControlSettingInfo(logDto.getApplicationId(),
					ObjectPrivilegeMode.NONE);
		} catch (Exception e) {
			// 制御ログ中のアプリケーションIDで設定が取得できなかった場合
			logger.warn("run() : There is no Control setting. applicationId(log)=" + logDto.getApplicationId());
			// 通知先がないためログだけ出力して終了する
			return rtn;
		}
		if (!checkFacilityId()) {
			logger.debug("run() ; This facilityId is out of scope. facilityId=" + this.facilityId);
			// ファシリティIDがスコープに含まれていなければ終了
			return rtn;
		}
		if (!this.controlSetting.getValidFlg()) {
			logger.debug("run() ; This Control setting is invalid. applicationId=" + this.controlSetting.getApplicationId());
			// 通常は設定無効の場合にエージェントから送られてくることはないがコントローラ側でもはじいておく
			return rtn;
		}
		this.initializeDataManager = new InitializeDataManager(this.controlSetting.getApplicationId(), this.facilityId);
		this.controlStatusManager = new ControlStatusManager(this.controlSetting.getApplicationId(), this.facilityId);

		// 収集処理
		if (this.controlSetting.getControlLogCollectFlg().booleanValue()) {
			ControlLogUtil.collectControlLog(this.controlSetting, this.facilityId, this.logDto);
		}

		// メイン処理
		this.controlCode = new SdmlControlCode(logDto.getControlCode());
		if (this.controlCode.getMainCode() == null) {
			// nullの場合
			logger.warn("run() : This code is not supported. controlCode=" + logDto.getControlCode());
			if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
						MessageConstant.SDML_MSG_LOG_NOT_SUPPORTED.getMessage(), logDto.getOrgLogLine(),
						NotifyTriggerType.SDML_CONTROL_ABNORMAL));
			}
			return rtn;
		}
		// 制御コードに応じた処理
		switch (this.controlCode.getMainCode()) {
		case Initialize:
			rtn = initialize();
			break;
		case Start:
			rtn = start();
			break;
		case Stop:
			rtn = stop();
			break;
		case Error:
			rtn = error();
			break;
		case Warning:
			rtn = warning();
			break;
		case Info:
			rtn = info();
			break;
		default:
			// 到達しない
		}

		return rtn;
	}

	/**
	 * SDML制御コード:Initialize
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> initialize() throws HinemosUnknown {
		logger.debug(
				"initialize() : applicationId=" + controlSetting.getApplicationId() + ", facilityId=" + facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();

		// サブコードによって分岐
		switch (this.controlCode.getSubCode()) {
		case Begin:
			rtn = initializeBegin();
			break;
		case Set:
			rtn = initializeSet();
			break;
		case End:
			rtn = initializeEnd();
			break;
		default:
			// nullの場合はここにくる（ここには到達しない）
			logger.error("initialize() : subcode is null.");
		}
		return rtn;
	}

	/**
	 * Initialize_Beginの処理
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> initializeBegin() {
		logger.info("initializeBegin() : applicationId=" + controlSetting.getApplicationId() + ", facilityId="
				+ facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();

		SdmlControlStatus status = controlStatusManager.getSdmlControlStatus();
		if (!controlStatusManager.checkStatus(status, this.controlCode)) {
			// Initialize_Beginだけは正常な順番で来ないことも許容されるため、ログだけ出して続行する
			// 例）SIGKILLなどによりStopまで出力されないまま再起動され、Initialize_Beginから開始される、など
			logger.info("initializeBegin() : Start over from Initialize.");
			// 蓄積情報の削除
			InitializeDataUtil.clear(controlSetting.getApplicationId(), facilityId);
		}

		// ステータス更新
		ControlStatusUtil
				.refresh(controlStatusManager.updateByInitializeBegin(status, this.controlCode, this.logDto.getTime()));
		return rtn;
	}

	/**
	 * Initialize_Setの処理
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> initializeSet() throws HinemosUnknown {
		logger.debug(
				"initializeSet() : applicationId=" + controlSetting.getApplicationId() + ", facilityId=" + facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();

		SdmlControlStatus status = controlStatusManager.getSdmlControlStatus();
		if (!controlStatusManager.checkStatus(status, this.controlCode)) {
			if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
						MessageConstant.SDML_MSG_LOG_OUT_OF_ORDER.getMessage(), logDto.getOrgLogLine(),
						NotifyTriggerType.SDML_CONTROL_ABNORMAL));
			}
			rtn.addAll(forcedUpdateToWaiting(status));
			return rtn;
		}

		try {
			// Key,Valueの登録
			InitializeDataUtil.add(initializeDataManager.createInitializeData(this.logDto.getMessage()));

			// ステータス更新
			ControlStatusUtil.refresh(controlStatusManager.update(status, this.controlCode));
		} catch (HinemosUnknown e) {
			// Keyが揃っていない場合はEndで判定するのでここではログだけ出力
			logger.warn("initializeSet() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		return rtn;
	}

	/**
	 * Initialize_Endの処理
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> initializeEnd() throws HinemosUnknown {
		logger.info(
				"initializeEnd() : applicationId=" + controlSetting.getApplicationId() + ", facilityId=" + facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();

		SdmlControlStatus status = controlStatusManager.getSdmlControlStatus();
		if (!controlStatusManager.checkStatus(status, this.controlCode)) {
			if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
						MessageConstant.SDML_MSG_LOG_OUT_OF_ORDER.getMessage(), logDto.getOrgLogLine(),
						NotifyTriggerType.SDML_CONTROL_ABNORMAL));
			}
			rtn.addAll(forcedUpdateToWaiting(status));
			return rtn;
		}

		boolean firstFlg = false;
		List<SdmlControlMonitorRelation> relationList = new SelectSdmlControl()
				.getSdmlControlMonitorRelation(this.controlSetting.getApplicationId(), this.facilityId);
		if (relationList == null || relationList.isEmpty()) {
			// アプリケーションIDとファシリティIDに紐づく自動作成監視の関連情報がない＝初回
			firstFlg = true;
		}

		// Keyが揃っているか確認
		Map<String, SdmlInitializeData> initializeDataMap = initializeDataManager.getInitializeDataMap();
		if (!initializeDataManager.checkInitializeDataAvailable(initializeDataMap)) {
			logger.warn("initializeEnd() : initialize data is not available.");
			if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
						MessageConstant.SDML_MSG_INITIALIZE_DATA_NOT_AVAILABLE.getMessage(), "",
						NotifyTriggerType.SDML_CONTROL_ABNORMAL));
			}
			rtn.addAll(forcedUpdateToWaiting(status));
			return rtn;
		}

		// 監視か収集が有効な監視種別のリストを取得（どちらも無効の場合は監視設定を作成しない）
		List<AutoMonitorValidInfo> enableMonitorList = SdmlMonitorUtil.getEnableMonitorList(initializeDataMap);

		// 監視の登録用にログインユーザを管理者ユーザーに設定
		String user = UserIdConstant.HINEMOS;
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, user);
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);

		MonitorSettingControllerBean controllerBean = new MonitorSettingControllerBean();
		List<String> createSuccessList = new ArrayList<>();
		List<String> updateSuccessList = new ArrayList<>();
		for (AutoMonitorValidInfo validInfo : enableMonitorList) {

			// 監視項目IDを生成する
			validInfo.setMonitorId(Singletons.get(SdmlMonitorIdGenerator.class).generateMonitorId(
					controlSetting.getApplicationId(), validInfo.getSdmlMonitorTypeId(), validInfo.getSubType()));

			SdmlControlMonitorRelation monitorRelation = null;
			// 初回でない場合はSDML監視種別に応じた自動作成監視の関連情報を取得
			if (!firstFlg) {
				for (SdmlControlMonitorRelation rel : relationList) {
					if (validInfo.equalTo(rel)) {
						monitorRelation = rel;
						break;
					}
				}
			}

			if (!firstFlg && monitorRelation != null) {
				// 初回ではない かつ 対応した関連情報がある（＝前回は監視収集が無効ではない）場合は更新
				try {
					MonitorInfo info = controllerBean.getMonitor(monitorRelation.getMonitorId());
					info = SdmlMonitorUtil.updateMonitorInfo(validInfo, controlSetting, initializeDataMap, info);
					controllerBean.modifyMonitor(info);

					// オブジェクト権限
					SdmlMonitorUtil.setObjectPlivilege(controlSetting, info);

					// SdmlControlMonitorRelationの有効・無効を更新
					monitorRelation.setMonitorFlg(validInfo.isMonitorFlg());
					monitorRelation.setCollectorFlg(validInfo.isCollectorFlg());

					updateSuccessList.add(info.getMonitorId());
					// 成功したら次へ
					continue;

				} catch (InvalidSetting | InvalidRole | UsedObjectPrivilege | PrivilegeDuplicate | HinemosUnknown e) {
					logger.warn(
							"initializeEnd() : update failed. " + e.getClass().getSimpleName() + ", " + e.getMessage(),
							e);
					if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
						String sdmlMessage = MessageConstant.SDML_MSG_UPDATE_FAILED.getMessage();
						rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
								sdmlMessage,
								createMessageOrg(sdmlMessage, monitorRelation.getMonitorId(), e),
								NotifyTriggerType.SDML_CONTROL_UPDATE_MONITOR));
					}
					rtn.addAll(forcedUpdateToWaiting(status));
					return rtn;
				} catch (MonitorNotFound e) {
					logger.warn(
							"initializeEnd() : update failed. " + e.getClass().getSimpleName() + ", " + e.getMessage());
					// 設定が存在しない場合は関連情報を削除して新規作成に進む
					new ModifySdmlControl().deleteOnlyControlMonitorRelation(monitorRelation);
					monitorRelation = null;
				}
			}

			// 更新ではない場合は新規作成
			try {
				MonitorInfo info = SdmlMonitorUtil.createMonitorInfo(validInfo, controlSetting, initializeDataMap,
						facilityId);
				controllerBean.addMonitor(info);

				// オブジェクト権限
				SdmlMonitorUtil.setObjectPlivilege(controlSetting, info);

				// SdmlControlMonitorRelation作成
				monitorRelation = new SdmlControlMonitorRelation(controlSetting.getApplicationId(), facilityId,
						info.getMonitorId());
				monitorRelation.setSdmlMonitorTypeId(validInfo.getSdmlMonitorTypeId());
				monitorRelation.setSubType(validInfo.getSubType());
				monitorRelation.setMonitorFlg(validInfo.isMonitorFlg());
				monitorRelation.setCollectorFlg(validInfo.isCollectorFlg());
				new ModifySdmlControl().addSdmlControlMonitorRelation(monitorRelation);

				createSuccessList.add(info.getMonitorId());
			} catch (MonitorIdInvalid | MonitorDuplicate | SdmlControlSettingDuplicate | InvalidSetting | InvalidRole
					| UsedObjectPrivilege | PrivilegeDuplicate | HinemosUnknown e) {
				logger.warn("initializeEnd() : create failed. " + e.getClass().getSimpleName() + ", " + e.getMessage(),
						e);
				if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
					String sdmlMessage = MessageConstant.SDML_MSG_CREATE_FAILED.getMessage();
					rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
							sdmlMessage,
							createMessageOrg(sdmlMessage, validInfo.getMonitorId(), e),
							NotifyTriggerType.SDML_CONTROL_CREATE_MONITOR));
				}
				rtn.addAll(forcedUpdateToWaiting(status));
				return rtn;
			}
		}
		// 初回ではない場合、監視も収集も無効に指定された登録済みの監視設定は全て無効とする（削除はしない）
		if (!firstFlg) {
			for (SdmlControlMonitorRelation monitorRelation : relationList) {
				boolean exists = false;
				for (AutoMonitorValidInfo validInfo : enableMonitorList) {
					if (validInfo.equalTo(monitorRelation)) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					logger.debug(
							"initializeEnd() : Not in targets to enable. monitorId=" + monitorRelation.getMonitorId());
					monitorRelation.setMonitorFlg(false);
					monitorRelation.setCollectorFlg(false);
				}
			}
		}
		// 通知
		if (!createSuccessList.isEmpty()) {
			if (isNotified(this.controlSetting.getAutoCreateSuccessPriority())) {
				String sdmlMessage = MessageConstant.SDML_MSG_CREATE_SUCCESS.getMessage();
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoCreateSuccessPriority(),
						sdmlMessage,
						createMessageOrg(sdmlMessage, String.join(",", createSuccessList), null),
						NotifyTriggerType.SDML_CONTROL_CREATE_MONITOR));
			}
		}
		if (!updateSuccessList.isEmpty()) {
			if (isNotified(this.controlSetting.getAutoUpdateSuccessPriority())) {
				String sdmlMessage = MessageConstant.SDML_MSG_UPDATE_SUCCESS.getMessage();
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoUpdateSuccessPriority(),
						sdmlMessage,
						createMessageOrg(sdmlMessage, String.join(",", updateSuccessList), null),
						NotifyTriggerType.SDML_CONTROL_UPDATE_MONITOR));
			}
		}

		// 初回/更新に関わらずInfoが送られてくる間隔を更新する
		try {
			status.setInternalCheckInterval(
					Integer.parseInt(initializeDataMap.get(InitializeKeyEnum.InfoInterval.name()).getValue()));
		} catch (NumberFormatException e) {
			// parseIntの失敗で発生する（Hinemosロギング側で型のチェックはするが制御ログを意図的に編集すれば発生の可能性はある）
			logger.error("initializeEnd() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			rtn.addAll(forcedUpdateToWaiting(status));
			return rtn;
		}

		// Initializeが完了したら蓄積情報は削除する
		InitializeDataUtil.clear(controlSetting.getApplicationId(), facilityId);
		// ステータス更新
		ControlStatusUtil.refresh(controlStatusManager.update(status, this.controlCode));

		return rtn;
	}

	/**
	 * SDML制御コード:Start
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> start() throws HinemosUnknown {
		logger.info("start() : applicationId=" + controlSetting.getApplicationId() + ", facilityId=" + facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();

		SdmlControlStatus status = controlStatusManager.getSdmlControlStatus();
		if (!controlStatusManager.checkStatus(status, this.controlCode)) {
			if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
						MessageConstant.SDML_MSG_LOG_OUT_OF_ORDER.getMessage(), logDto.getOrgLogLine(),
						NotifyTriggerType.SDML_CONTROL_ABNORMAL));
			}
			rtn.addAll(forcedUpdateToWaiting(status));
			return rtn;
		}

		List<String> monitorIdList = new ArrayList<String>();
		// アプリケーションIDとファシリティIDに紐づく監視項目IDのリストを取得
		List<SdmlControlMonitorRelation> relationList = new SelectSdmlControl()
				.getSdmlControlMonitorRelation(this.controlSetting.getApplicationId(), this.facilityId);
		for (SdmlControlMonitorRelation relation : relationList) {
			if (relation.getMonitorFlg() || relation.getCollectorFlg()) {
				// 監視か収集が有効な場合のみ通知に含める
				monitorIdList.add(relation.getMonitorId());
			}
		}
		String ids = String.join(",", monitorIdList);
		try {
			// 監視の登録用にログインユーザを管理者ユーザーに設定
			String user = UserIdConstant.HINEMOS;
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, user);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);

			MonitorSettingControllerBean controllerBean = new MonitorSettingControllerBean();
			for (SdmlControlMonitorRelation relation : relationList) {
				MonitorInfo info = controllerBean.getMonitor(relation.getMonitorId());
				// 監視・収集の有効/無効設定
				info.setMonitorFlg(relation.getMonitorFlg());
				info.setCollectorFlg(relation.getCollectorFlg());
				// 将来予測・変化量も変わってしまうためsetStatusMonitorは使わない
				controllerBean.modifyMonitor(info);
			}
			// 通知
			if (isNotified(this.controlSetting.getAutoEnableSuccessPriority())) {
				String sdmlMessage = MessageConstant.SDML_MSG_ENABLE_SUCCESS.getMessage();
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoEnableSuccessPriority(),
						sdmlMessage,
						createMessageOrg(sdmlMessage, ids, null),
						NotifyTriggerType.SDML_CONTROL_ENABLE_MONITOR));
			}

			// ステータス更新
			ControlStatusUtil.refresh(controlStatusManager.update(status, this.controlCode));

		} catch (MonitorNotFound | InvalidSetting | InvalidRole | HinemosUnknown e) {
			logger.warn("start() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
				String sdmlMessage = MessageConstant.SDML_MSG_ENABLE_FAILED.getMessage();
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
						sdmlMessage,
						createMessageOrg(sdmlMessage, ids, e),
						NotifyTriggerType.SDML_CONTROL_ENABLE_MONITOR));
			}
			rtn.addAll(forcedUpdateToWaiting(status));
		}
		return rtn;
	}

	/**
	 * SDML制御コード:Stop
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> stop() throws HinemosUnknown {
		logger.info("stop() : applicationId=" + controlSetting.getApplicationId() + ", facilityId=" + facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();

		SdmlControlStatus status = controlStatusManager.getSdmlControlStatus();
		if (!controlStatusManager.checkStatus(status, this.controlCode)) {
			if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
				rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
						MessageConstant.SDML_MSG_LOG_OUT_OF_ORDER.getMessage(), logDto.getOrgLogLine(),
						NotifyTriggerType.SDML_CONTROL_ABNORMAL));
			}
			// 蓄積情報が残っている可能性があるため削除
			InitializeDataUtil.clear(controlSetting.getApplicationId(), facilityId);
			// 状態のチェックでNGだった場合も停止処理は必要のため継続する
		} else {
			// 正常の場合は起動から停止までの時間をチェックする
			if (controlSetting.getEarlyStopThresholdSecond() > 0
					&& isNotified(controlSetting.getEarlyStopNotifyPriority())) {
				if (controlStatusManager.checkIsEarlyStop(status,
						controlSetting.getEarlyStopThresholdSecond().longValue(), logDto.getTime())) {
					// 起動から停止までが指定した時間より短い場合は通知
					String startDateStr = "";
					String stopDateStr = "";
					try {
						SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
						format.setTimeZone(HinemosTime.getTimeZone());
						startDateStr = format.format(new Date(status.getApplicationStartupDate()));
						stopDateStr = format.format(new Date(logDto.getTime()));
					} catch (Exception e) {
						// チェックは通っているのでフォーマットの異常がない限り到達しない
						logger.error("stop() : Date format is invalid. format=" + DATE_FORMAT);
					}
					rtn.add(createOutputBasicInfo(controlSetting.getEarlyStopNotifyPriority(),
							MessageConstant.SDML_MSG_EARLY_STOP
									.getMessage(controlSetting.getEarlyStopThresholdSecond().toString()),
							MessageConstant.SDML_MSG_START_DATE.getMessage(startDateStr) + "\n"
									+ MessageConstant.SDML_MSG_STOP_DATE.getMessage(stopDateStr),
							NotifyTriggerType.SDML_CONTROL_EARLY_STOP));
				}
			}
		}

		// 停止処理
		rtn.addAll(stopMonitor());

		// 停止するので機能障害検知は停止する
		if (status != null) {
			status.setInternalCheckInterval(null);
		}

		// ステータス更新（Stopの場合は待機に遷移するのでforceUpdateは不要）
		ControlStatusUtil.refresh(controlStatusManager.update(status, this.controlCode));

		return rtn;
	}

	/**
	 * 停止の共通処理<br>
	 * ・Stopを受け取った場合<br>
	 * ・Errorを受け取った場合<br>
	 * ・その他異常が発生した場合<br>
	 * 
	 * @return
	 * @throws HinemosUnknown
	 */
	private List<OutputBasicInfo> stopMonitor() throws HinemosUnknown {
		List<OutputBasicInfo> rtn = new ArrayList<>();

		// 登録されている自動作成監視の関連情報を取得
		List<SdmlControlMonitorRelation> relationList = new SelectSdmlControl()
				.getSdmlControlMonitorRelation(this.controlSetting.getApplicationId(), this.facilityId);
		if (relationList == null || relationList.isEmpty()) {
			// 関連情報がなければ終了
			logger.debug("stopMonitor() : SdmlControlMonitorRelation is emtpy.");
			return rtn;
		}

		// 監視の設定用にログインユーザを管理者ユーザーに設定
		String user = UserIdConstant.HINEMOS;
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, user);
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);

		boolean deleteFlg = this.controlSetting.getAutoMonitorDeleteFlg().booleanValue();
		if (deleteFlg) {
			// 監視設定の削除が有効の場合は削除する
			// 削除の場合は手動で削除した場合などを考慮し、1件ずつ削除する
			// ※NotFoundが発生しても既に削除されているなら問題ないので関連情報を削除し次の処理に進む
			List<String> successList = new ArrayList<>();
			ModifySdmlControl modifier = new ModifySdmlControl();
			for (SdmlControlMonitorRelation relation : relationList) {
				String monitorId = relation.getMonitorId();
				try {
					// 自動作成された監視設定と関連情報を削除
					modifier.deleteAutoCreatedMonitor(relation);
					successList.add(monitorId);
				} catch (MonitorNotFound | InvalidRole | InvalidSetting | HinemosUnknown e) {
					logger.warn("stopMonitor() : delete failed. monitorId=" + monitorId + ", "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
						String sdmlMessage = MessageConstant.SDML_MSG_DELETE_FAILED.getMessage();
						rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
								sdmlMessage,
								createMessageOrg(sdmlMessage, monitorId, e),
								NotifyTriggerType.SDML_CONTROL_DISABLE_MONITOR));
					}
				}
			}
			if (!successList.isEmpty()) {
				// 通知
				if (isNotified(this.controlSetting.getAutoDisableSuccessPriority())) {
					String sdmlMessage = MessageConstant.SDML_MSG_DELETE_SUCCESS.getMessage();
					rtn.add(createOutputBasicInfo(this.controlSetting.getAutoDisableSuccessPriority(),
							sdmlMessage,
							createMessageOrg(sdmlMessage, String.join(",", successList), null),
							NotifyTriggerType.SDML_CONTROL_DISABLE_MONITOR));
				}
			}

		} else {
			// 監視設定の削除が無効の場合は監視と収集を無効化する
			List<String> successList = new ArrayList<>();
			MonitorSettingControllerBean controllerBean = new MonitorSettingControllerBean();
			for (SdmlControlMonitorRelation relation : relationList) {
				String monitorId = relation.getMonitorId();
				try {
					MonitorInfo info = controllerBean.getMonitor(monitorId);
					if (!info.getMonitorFlg() && !info.getCollectorFlg()) {
						// すでに監視も収集も無効ならスキップ
						continue;
					}
					// 監視と収集を無効化する（変化量と将来予測は手動で変更しない限り有効にならないので無視する）
					info.setMonitorFlg(false);
					info.setCollectorFlg(false);
					controllerBean.modifyMonitor(info);
					successList.add(monitorId);

				} catch (MonitorNotFound | InvalidRole | InvalidSetting | HinemosUnknown e) {
					logger.warn("stopMonitor() : disable failed. monitorId=" + monitorId + ", "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					// 理由が異なる可能性もあるため一件ずつ通知する
					if (isNotified(this.controlSetting.getAutoControlFailedPriority())) {
						String sdmlMessage = MessageConstant.SDML_MSG_DISABLE_FAILED.getMessage();
						rtn.add(createOutputBasicInfo(this.controlSetting.getAutoControlFailedPriority(),
								sdmlMessage,
								createMessageOrg(sdmlMessage, monitorId, e),
								NotifyTriggerType.SDML_CONTROL_DISABLE_MONITOR));
					}
				}
			}
			if (!successList.isEmpty()) {
				// 通知
				if (isNotified(this.controlSetting.getAutoDisableSuccessPriority())) {
					String sdmlMessage = MessageConstant.SDML_MSG_DISABLE_SUCCESS.getMessage();
					rtn.add(createOutputBasicInfo(this.controlSetting.getAutoDisableSuccessPriority(),
							sdmlMessage,
							createMessageOrg(sdmlMessage, String.join(",", successList), null),
							NotifyTriggerType.SDML_CONTROL_DISABLE_MONITOR));
				}
			}
		}
		return rtn;
	}

	/**
	 * SDML制御コード:Error
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> error() throws HinemosUnknown {
		logger.info("error() : applicationId=" + controlSetting.getApplicationId() + ", facilityId=" + facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();
		// 通知
		rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_CRITICAL,
				MessageConstant.SDML_MSG_LOG_ERROR.getMessage(logDto.getMessage()), logDto.getOrgLogLine(),
				NotifyTriggerType.SDML_CONTROL_ABNORMAL));

		// ステータスを強制的に待機に更新
		SdmlControlStatus status = controlStatusManager.getSdmlControlStatus();
		rtn.addAll(forcedUpdateToWaiting(status));
		return rtn;
	}

	/**
	 * SDML制御コード:Warning
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> warning() throws HinemosUnknown {
		logger.info("warning() : applicationId=" + controlSetting.getApplicationId() + ", facilityId=" + facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();
		// 通知
		rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_WARNING,
				MessageConstant.SDML_MSG_LOG_WARNING.getMessage(logDto.getMessage()), logDto.getOrgLogLine(),
				NotifyTriggerType.SDML_CONTROL_ABNORMAL));

		// ステータス更新
		SdmlControlStatus status = controlStatusManager.getSdmlControlStatus();
		if (status == null) {
			// Initialize_Beginが来る前に来た場合は待機に更新
			ControlStatusUtil.refresh(
					controlStatusManager.forcedUpdate(null, this.controlCode, SdmlControlStatusEnum.Waiting));
		} else {
			ControlStatusUtil.refresh(controlStatusManager.update(status, this.controlCode));
		}
		return rtn;
	}

	/**
	 * SDML制御コード:Info
	 * 
	 * @return
	 */
	private List<OutputBasicInfo> info() {
		logger.info("info() : applicationId=" + controlSetting.getApplicationId() + ", facilityId=" + facilityId);
		List<OutputBasicInfo> rtn = new ArrayList<>();

		// ステータス更新
		SdmlControlStatus status = controlStatusManager.getSdmlControlStatus();
		if (status == null) {
			// Initialize_Beginが来る前に来た場合は待機に更新
			ControlStatusUtil.refresh(
					controlStatusManager.forcedUpdate(null, this.controlCode, SdmlControlStatusEnum.Waiting));
		} else {
			ControlStatusUtil.refresh(controlStatusManager.update(status, this.controlCode));
		}
		return rtn;
	}

	/**
	 * ファシリティIDがSDML制御設定に紐づくかどうかチェック.<br>
	 * <br>
	 * 実際に読み取り処理を実行したAgentに紐づくFacilityIDが設定に紐づくFacilityIDかどうかをチェックする.<br>
	 * 同一IPのAgentに対して複数FacilityIDを設定した場合を考慮<br>
	 * 
	 * @return true:紐づく、false:紐づかない
	 */
	private boolean checkFacilityId() {
		// 設定に紐づくファシリティID一覧を取得.
		List<String> facilityIdList = FacilitySelector.getFacilityIdList(this.controlSetting.getFacilityId(),
				this.controlSetting.getOwnerRoleId(), 0, false, false);
		if (facilityIdList == null || facilityIdList.isEmpty()) {
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("checkFacilityId() : targetFacilityId=" + this.facilityId + ", facilityIdList="
					+ String.join(",", facilityIdList));
		}
		// 設定に紐づくファシリティIDに含まれてるかどうかチェック.
		return facilityIdList.contains(this.facilityId);
	}

	/**
	 * 通知情報作成
	 */
	private OutputBasicInfo createOutputBasicInfo(int priority, String message, String messageOrg, NotifyTriggerType notifyTriggerType)
			throws HinemosUnknown {
		return SdmlUtil.createOutputBasicInfo(this.controlSetting, this.facilityId, priority, message, messageOrg, notifyTriggerType);
	}

	/**
	 * ステータスを強制的に待機に戻し、必要な処理を実行する
	 * 
	 * @return
	 * @throws HinemosUnknown
	 */
	private List<OutputBasicInfo> forcedUpdateToWaiting(SdmlControlStatus status) throws HinemosUnknown {
		List<OutputBasicInfo> rtn = new ArrayList<>();
		// 蓄積情報を削除
		InitializeDataUtil.clear(controlSetting.getApplicationId(), facilityId);

		// 停止処理
		rtn.addAll(stopMonitor());

		// 停止するので機能障害検知が動作しないようにしておく（有効化直後はstatusがnullになる）
		if (status != null) {
			status.setInternalCheckInterval(null);
		}

		// 強制的に待機に更新
		ControlStatusUtil
				.refresh(controlStatusManager.forcedUpdate(status, controlCode, SdmlControlStatusEnum.Waiting));

		return rtn;
	}

	private boolean isNotified(Integer priority) {
		if (priority == null || priority == PriorityConstant.TYPE_NONE || this.controlSetting.getNotifyGroupId() == null
				|| this.controlSetting.getNotifyGroupId().isEmpty()) {
			return false;
		}
		// 要通知の場合はtrue
		return true;
	}
	
	private String createMessageOrg(String sdmlMessage, String monitorId, Exception e) {
		String messageOrg = sdmlMessage + "\n"
				+ MessageConstant.SDML_CONTROL_LOG.getMessage() + " : " + logDto.getOrgLogLine().trim() + "\n"
				+ "Monitor ID : " + monitorId;
		if (e != null) {
			messageOrg += "\n" + "Error Message : " + e.getMessage();
		}
		return messageOrg;
	}

}
