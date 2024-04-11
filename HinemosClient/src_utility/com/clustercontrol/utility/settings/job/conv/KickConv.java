/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.job.conv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openapitools.client.model.JobFileCheckResponse;
import org.openapitools.client.model.JobFileCheckResponse.EventTypeEnum;
import org.openapitools.client.model.JobFileCheckResponse.ModifyTypeEnum;
import org.openapitools.client.model.JobKickResponse;
import org.openapitools.client.model.JobLinkExpInfoResponse;
import org.openapitools.client.model.JobLinkRcvResponse;
import org.openapitools.client.model.JobRuntimeParamDetailResponse;
import org.openapitools.client.model.JobRuntimeParamResponse;
import org.openapitools.client.model.JobRuntimeParamResponse.ParamTypeEnum;
import org.openapitools.client.model.JobScheduleResponse;
import org.openapitools.client.model.JobScheduleResponse.ScheduleTypeEnum;
import org.openapitools.client.model.JobScheduleResponse.SessionPremakeEveryXHourEnum;
import org.openapitools.client.model.JobScheduleResponse.SessionPremakeScheduleTypeEnum;

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.jobmanagement.bean.SessionPremakeScheduleType;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.AbstractConvertor;
import com.clustercontrol.utility.settings.job.xml.FileCheckData;
import com.clustercontrol.utility.settings.job.xml.FileCheckInfo;
import com.clustercontrol.utility.settings.job.xml.JobLinkExpInfo;
import com.clustercontrol.utility.settings.job.xml.JobLinkRcvData;
import com.clustercontrol.utility.settings.job.xml.JobLinkRcvInfo;
import com.clustercontrol.utility.settings.job.xml.JobRuntimeDetailInfos;
import com.clustercontrol.utility.settings.job.xml.JobRuntimeInfos;
import com.clustercontrol.utility.settings.job.xml.ManualInfo;
import com.clustercontrol.utility.settings.job.xml.ScheduleData;
import com.clustercontrol.utility.settings.job.xml.ScheduleInfo;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.StringUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * スケジュール定義情報をXMLのBeanとHinemosのDTOの相互変換を行います。<BR>
 *
 * @version 6.0.0
 * @since 1.0.0
 * 
 * 
 * 
 */
public class KickConv extends AbstractConvertor {

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	/** スキーマタイプ */
	private static final String schemaType=VersionUtil.getSchemaProperty("JOB.KICK.SCHEMATYPE");
	/** スキーマバージョン */
	private static final String schemaVersion=VersionUtil.getSchemaProperty("JOB.KICK.SCHEMAVERSION");
	/** スキーマレビジョン */
	private static final String schemaRevision =VersionUtil.getSchemaProperty("JOB.KICK.SCHEMAREVISION");
	
	/** メッセージ定義 入力必須項目が正しくありません。*/
	private static String MESSAGE_ESSENTIALVALUEINVALID = Messages.getString("SettingTools.EssentialValueInvalid");
	
	/**
	 * コンストラクタ
	 */
	public KickConv() {
		super.schemaType = KickConv.schemaType;
		super.schemaVersion = KickConv.schemaVersion;
		super.schemaRevision = KickConv.schemaRevision;
	}

	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	public com.clustercontrol.utility.settings.job.xml.SchemaInfo getSchemaVersion() {

		com.clustercontrol.utility.settings.job.xml.SchemaInfo schema = new com.clustercontrol.utility.settings.job.xml.SchemaInfo();
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);

		return schema;
	}
	
	/**
	 * XMLのBeanからHinemosのBeanに変換しします。
	 * @param schedule XMLのBean
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public JobScheduleResponse scheduleXml2Dto(ScheduleInfo schedule) throws InvalidSetting, HinemosUnknown {
		
		ScheduleData data = schedule.getScheduleData();

		// 投入用データの作成
		JobScheduleResponse info = new JobScheduleResponse();
		
		// ID
		if (!StringUtil.isNullOrEmpty(schedule.getId())) {
			info.setId(schedule.getId());
		} else {
			log.warn(String.format("%s(ScheduleId) : %s", MESSAGE_ESSENTIALVALUEINVALID, schedule.getId()));
			return null;
		}
		// 名前
		if (!StringUtil.isNullOrEmpty(schedule.getName())) {
			info.setName(schedule.getName());
		} else {
			log.warn(String.format("%s(ScheduleName) : %s", MESSAGE_ESSENTIALVALUEINVALID, schedule.getId()));
			return null;
		}
		// ジョブユニットID
		if (!StringUtil.isNullOrEmpty(schedule.getJobunitId())) {
			info.setJobunitId(schedule.getJobunitId());
		} else {
			log.warn(String.format("%s(JobunitId) : %s", MESSAGE_ESSENTIALVALUEINVALID, schedule.getId()));
			return null;
		}
		// ジョブID
		if (!StringUtil.isNullOrEmpty(schedule.getJobId())) {
			info.setJobId(schedule.getJobId());
		} else {
			log.warn(String.format("%s(JobId) : %s", MESSAGE_ESSENTIALVALUEINVALID, schedule.getId()));
			return null;
		}
		// オーナーロールID
		if (!StringUtil.isNullOrEmpty(schedule.getOwnerRoleId())) {
			info.setOwnerRoleId(schedule.getOwnerRoleId());
		} else {
			log.warn(String.format("%s(OwnerRoleId) : %s", MESSAGE_ESSENTIALVALUEINVALID, schedule.getId()));
			return null;
		}
		// カレンダーID
		if (schedule.getCalId().length() > 0) {
			info.setCalendarId(schedule.getCalId());
		}else{
			//カレンダーは0でもＯＫ
		}
		// 有効・無効フラグ
		info.setValid(schedule.getValidFlg());

		// スケジュール種別
		ScheduleTypeEnum scheduleTypeEnum = OpenApiEnumConverter.integerToEnum(data.getScheduleType(), JobScheduleResponse.ScheduleTypeEnum.class);
		info.setScheduleType(scheduleTypeEnum);
		// 曜日
		if(data.getScheduleType() == ScheduleConstant.TYPE_WEEK){
			info.setWeek((int)data.getWeek());
		} else {
			info.setWeek(null);
		}
		// 時
		if((data.getScheduleType() == ScheduleConstant.TYPE_DAY || data.getScheduleType() == ScheduleConstant.TYPE_WEEK
				|| data.getScheduleType() == ScheduleConstant.TYPE_INTERVAL) 
				&& data.getHour() != -1){
			info.setHour(data.getHour());
		} else {
			info.setHour(null);
		}
		// 分
		if(data.getScheduleType() == ScheduleConstant.TYPE_DAY || data.getScheduleType() == ScheduleConstant.TYPE_WEEK
				|| data.getScheduleType() == ScheduleConstant.TYPE_INTERVAL){
			info.setMinute((int)data.getMinute());
		} else {
			info.setMinute(null);
		}
		// X分から
		if(data.getScheduleType() == ScheduleConstant.TYPE_REPEAT){
			info.setFromXminutes(data.getFromXminutes());
		} else {
			info.setFromXminutes(null);
		}
		// X分ごとに繰り返し実行
		if(data.getScheduleType() == ScheduleConstant.TYPE_REPEAT){
			info.setEveryXminutes(data.getEveryXminutes_Hour());
		} else if(data.getScheduleType() == ScheduleConstant.TYPE_INTERVAL){
			info.setEveryXminutes(data.getEveryXminutes_Interval());
		} else {
			info.setEveryXminutes(null);
		}
		
		// ジョブセッション事前生成有効・無効フラグ
		if(data.hasSessionPremakeFlg()){
			info.setSessionPremakeFlg(data.getSessionPremakeFlg());
		}
		
		// 事前生成スケジュール種別
		if(data.hasSessionPremakeScheduleType()){
			SessionPremakeScheduleTypeEnum sessionPremakeScheduleEnum = OpenApiEnumConverter.integerToEnum(
					data.getSessionPremakeScheduleType(), SessionPremakeScheduleTypeEnum.class);
			info.setSessionPremakeScheduleType(sessionPremakeScheduleEnum);
		}
		
		// 曜日
		if(data.hasSessionPremakeWeek() && 
				data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_EVERY_WEEK){
			info.setSessionPremakeWeek(data.getSessionPremakeWeek());
		}
		
		// 時
		if(data.hasSessionPremakeHour() && 
				data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_EVERY_DAY
				|| data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_EVERY_WEEK
				|| data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_TIME){
			info.setSessionPremakeHour(data.getSessionPremakeHour());
		}
		
		// 分
		if(data.hasSessionPremakeMinute() && 
				data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_EVERY_DAY
				|| data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_EVERY_WEEK
				|| data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_TIME){
			info.setSessionPremakeMinute(data.getSessionPremakeMinute());
		}
		
		// x時間毎に繰り返し
		if(data.hasSessionPremakeEveryXHour() && 
				data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_TIME){
			info.setSessionPremakeEveryXHour(OpenApiEnumConverter.integerToEnum(
					data.getSessionPremakeEveryXHour(), SessionPremakeEveryXHourEnum.class));
		}
		
		// 日時（から）
		if(data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_DATETIME){
			info.setSessionPremakeDate(data.getSessionPremakeDate());
		}
		
		// 日時（実行分まで）
		if(data.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_DATETIME){
			info.setSessionPremakeToDate(data.getSessionPremakeToDate());
		}
		
		// 事前生成完了時にINTERNALイベント出力の有効/無効フラグ
		if(data.hasSessionPremakeInternalFlg()){
			info.setSessionPremakeInternalFlg(data.getSessionPremakeInternalFlg());
		}
		
		// ジョブ変数
		List<JobRuntimeParamResponse> paramlist = createRuntimeListFrom(schedule.getJobRuntimeInfos());
		info.getJobRuntimeParamList().addAll(paramlist);

		return info;
	}

	/**
	 * Managerで利用されているスケジュールデータをXMLのBeanにマッピングします。
	 * @param jobSchedule マネージャで利用されいる形式のスケジュールデータ
	 * @return 出力用XMLのBean
	 */
	public ScheduleInfo scheduleDto2Xml(JobScheduleResponse jobSchedule) {

		// scheduleXML : XMLバイディング用のデータ
		// dataXML : XMLバイディング用の追加データ
		ScheduleInfo scheduleXML = new ScheduleInfo();
		scheduleXML.setId(jobSchedule.getId());
		scheduleXML.setName(jobSchedule.getName());

		scheduleXML.setJobunitId(jobSchedule.getJobunitId());
		scheduleXML.setJobId(jobSchedule.getJobId());
		scheduleXML.setOwnerRoleId(jobSchedule.getOwnerRoleId());

		// カレンダIDがNULLでなければ投入
		if (null != jobSchedule.getCalendarId()) {
			scheduleXML.setCalId(jobSchedule.getCalendarId());
		} else {
			scheduleXML.setCalId("");
		}

		// 有効/無効
		scheduleXML.setValidFlg(jobSchedule.getValid());
		
		// 日付データの投入
		ScheduleData dataXML = new ScheduleData();
		
		if(jobSchedule.getScheduleType()==null){
			dataXML.setScheduleType(ScheduleConstant.TYPE_DAY);
		} else {
			int scheduleTypeInt = OpenApiEnumConverter.enumToInteger(jobSchedule.getScheduleType());
			dataXML.setScheduleType(scheduleTypeInt);
		}

		if (jobSchedule.getWeek() != null) {
			dataXML.setWeek(jobSchedule.getWeek());
		} else {
			// weekはrequiredの為、仮の値(日曜日)を入力
			dataXML.setWeek(DayOfWeekConstant.TYPE_SUNDAY);
		}
		if (jobSchedule.getHour() != null) {
			dataXML.setHour(jobSchedule.getHour());
		} else {
			// hourがnull(*)の場合、-1を入力
			dataXML.setHour(-1);
		}
		if (jobSchedule.getMinute() != null) {
			dataXML.setMinute(jobSchedule.getMinute());
		} else {
			// minuteはrequiredの為、仮の値(0)を入力
			dataXML.setMinute(0);
		}
		if (jobSchedule.getFromXminutes() != null) {
			dataXML.setFromXminutes(jobSchedule.getFromXminutes());
		} else {
			// fromXminutesはrequiredの為、仮の値(0)を入力
			dataXML.setFromXminutes(0);
		}
		// everyXminutesはrequiredの為、一旦 仮の値(0)をデフォルトとして設定した後、必要に応じて値を変更
		dataXML.setEveryXminutes_Hour(0);
		dataXML.setEveryXminutes_Interval(0);
		if (jobSchedule.getEveryXminutes() != null) {
			if(OpenApiEnumConverter.enumToInteger(jobSchedule.getScheduleType()) == ScheduleConstant.TYPE_REPEAT){
				dataXML.setEveryXminutes_Hour(jobSchedule.getEveryXminutes());
			} else if(OpenApiEnumConverter.enumToInteger(jobSchedule.getScheduleType()) == ScheduleConstant.TYPE_INTERVAL){
				dataXML.setEveryXminutes_Interval(jobSchedule.getEveryXminutes());
			}
		}
		
		// ジョブセッション事前生成有効・無効フラグ
		if (jobSchedule.getSessionPremakeFlg() != null){
			dataXML.setSessionPremakeFlg(jobSchedule.getSessionPremakeFlg());
		} else {
			// nullの場合、仮の値(無効)を入力
			dataXML.setSessionPremakeFlg(false);
		}
		
		// 事前生成スケジュール種別
		if (jobSchedule.getSessionPremakeScheduleType() != null){
			dataXML.setSessionPremakeScheduleType(OpenApiEnumConverter.enumToInteger(
					jobSchedule.getSessionPremakeScheduleType()));
		}
		
		// 曜日
		if (jobSchedule.getSessionPremakeWeek() != null){
			dataXML.setSessionPremakeWeek(jobSchedule.getSessionPremakeWeek());
		}
		
		// 時
		if (jobSchedule.getSessionPremakeHour() != null){
			dataXML.setSessionPremakeHour(jobSchedule.getSessionPremakeHour());
		}
		
		// 分
		if (jobSchedule.getSessionPremakeMinute() != null){
			dataXML.setSessionPremakeMinute(jobSchedule.getSessionPremakeMinute());
		}
		
		// x時間毎に繰り返し
		if (jobSchedule.getSessionPremakeEveryXHour() != null){
			dataXML.setSessionPremakeEveryXHour(OpenApiEnumConverter.enumToInteger(
					jobSchedule.getSessionPremakeEveryXHour()));
		}
		
		// 日時（から）
		dataXML.setSessionPremakeDate(jobSchedule.getSessionPremakeDate());
		
		// 日時（実行分まで）
		dataXML.setSessionPremakeToDate(jobSchedule.getSessionPremakeToDate());
		
		// 事前生成完了時にINTERNALイベント出力の有効/無効フラグ
		if (jobSchedule.getSessionPremakeInternalFlg() != null){
			dataXML.setSessionPremakeInternalFlg(jobSchedule.getSessionPremakeInternalFlg());
		}
		
		scheduleXML.setScheduleData(dataXML);

		// ジョブ変数
		List<JobRuntimeInfos> runtimes = getJobRuntimeInfosFromManagerDto(jobSchedule.getJobRuntimeParamList());
		scheduleXML.setJobRuntimeInfos(runtimes.toArray(new JobRuntimeInfos[0]));

		return scheduleXML;
	}
	
	/**
	 * XMLのBeanからHinemosManagerのBeanに変換しします。
	 * @param fileCheck XMLのBean
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public JobFileCheckResponse fileCheckXml2Dto(FileCheckInfo fileCheck) throws InvalidSetting, HinemosUnknown {

		FileCheckData data = fileCheck.getFileCheckData();
		
		// 投入用データの作成
		JobFileCheckResponse info = new JobFileCheckResponse();

		// ID
		if (!StringUtil.isNullOrEmpty(fileCheck.getId())) {
			info.setId(fileCheck.getId());
		} else {
			log.warn(String.format("%s(ScheduleId) : %s", MESSAGE_ESSENTIALVALUEINVALID, fileCheck.getId()));
			return null;
		}
		// 名前
		if (!StringUtil.isNullOrEmpty(fileCheck.getName())) {
			info.setName(fileCheck.getName());
		} else {
			log.warn(String.format("%s(ScheduleName) : %s", MESSAGE_ESSENTIALVALUEINVALID, fileCheck.getId()));
			return null;
		}
		// ジョブユニットID
		if (!StringUtil.isNullOrEmpty(fileCheck.getJobunitId())) {
			info.setJobunitId(fileCheck.getJobunitId());
		} else {
			log.warn(String.format("%s(JobunitId) : %s", MESSAGE_ESSENTIALVALUEINVALID, fileCheck.getId()));
			return null;
		}
		// ジョブID
		if (!StringUtil.isNullOrEmpty(fileCheck.getJobId())) {
			info.setJobId(fileCheck.getJobId());
		} else {
			log.warn(String.format("%s(JobId) : %s", MESSAGE_ESSENTIALVALUEINVALID, fileCheck.getId()));
			return null;
		}
		// オーナーロールID
		if (!StringUtil.isNullOrEmpty(fileCheck.getOwnerRoleId())) {
			info.setOwnerRoleId(fileCheck.getOwnerRoleId());
		} else {
			log.warn(String.format("%s(OwnerRoleId) : %s", MESSAGE_ESSENTIALVALUEINVALID, fileCheck.getId()));
			return null;
		}
		// カレンダーID
		if (fileCheck.getCalId().length() > 0) {
			info.setCalendarId(fileCheck.getCalId());
		} else {
			// カレンダーはなくてもＯＫ
		}
		// 有効・無効フラグ
		info.setValid(fileCheck.getValidFlg());

		// ファシリティID
		info.setFacilityId(data.getFacilityId());
		// ディレクトリ
		info.setDirectory(data.getDirectory());
		// ファイル名
		info.setFileName(data.getFileName());
		// ファイルが使用されている場合の判定の持ち越しの有効/無効フラグ
		info.setCarryOverJudgmentFlg(data.getCarryOverJudgmentFlg());
		
		// イベント種別
		EventTypeEnum eventTypeEnum = OpenApiEnumConverter.integerToEnum(data.getEventType(), EventTypeEnum.class);
		info.setEventType(eventTypeEnum);
		// 変更種別
		if(data.getEventType() == FileCheckConstant.TYPE_MODIFY){
			ModifyTypeEnum modifyTypeEnum = OpenApiEnumConverter.integerToEnum(data.getModifyType(), ModifyTypeEnum.class); 
			info.setModifyType(modifyTypeEnum);
		} else {
			info.setModifyType(null);
		}

		// ジョブ変数
		List<JobRuntimeParamResponse> paramlist = createRuntimeListFrom(fileCheck.getJobRuntimeInfos());
		info.getJobRuntimeParamList().addAll(paramlist);

		return info;
	}

	/**
	 * MGRで利用されているスケジュールデータをXMLのBeanにマッピングします。
	 * @param scheduleMgr マネージャで利用されいる形式のスケジュールデータ
	 * 
	 */
	/**
	 * Managerで利用されているファイルチェックデータをXMLのBeanにマッピングします。
	 * @param jobSchedule マネージャで利用されいる形式のファイルチェックデータ
	 * @return 出力用XMLのBean
	 */
	public FileCheckInfo fileCheckDto2Xml(JobFileCheckResponse jobFileCheck) {

		//fileCheckXML 	: XMLバイディング用のデータ
		//dataXML		: XMLバイディング用の追加データ

		FileCheckInfo fileCheckXML = new FileCheckInfo();
		fileCheckXML.setId(jobFileCheck.getId());
		fileCheckXML.setName(jobFileCheck.getName());
		fileCheckXML.setJobId(jobFileCheck.getJobId());
		fileCheckXML.setJobunitId(jobFileCheck.getJobunitId());
		fileCheckXML.setOwnerRoleId(jobFileCheck.getOwnerRoleId());

		//カレンダIDがNULLでなければ投入
		if (jobFileCheck.getCalendarId() != null) {
			fileCheckXML.setCalId(jobFileCheck.getCalendarId());
		} else {
			fileCheckXML.setCalId("");
		}

		// 有効/無効
		fileCheckXML.setValidFlg(jobFileCheck.getValid());

		FileCheckData dataXML = new FileCheckData();
		dataXML.setFacilityId(jobFileCheck.getFacilityId());
		dataXML.setDirectory(jobFileCheck.getDirectory());
		dataXML.setFileName(jobFileCheck.getFileName());
		dataXML.setCarryOverJudgmentFlg(jobFileCheck.getCarryOverJudgmentFlg());
		
		int eventTypeInt = OpenApiEnumConverter.enumToInteger(jobFileCheck.getEventType());
		dataXML.setEventType(eventTypeInt);
		if (jobFileCheck.getModifyType() != null) {
			int modifyTypeInt = OpenApiEnumConverter.enumToInteger(jobFileCheck.getModifyType());
			dataXML.setModifyType(modifyTypeInt);
		} else {
			//modifyTypeはrequiredの為、仮の値(作成)を入力
			dataXML.setModifyType(FileCheckConstant.TYPE_CREATE);
		}
		
		fileCheckXML.setFileCheckData(dataXML);

		// ジョブ変数
		List<JobRuntimeInfos> runtimes = getJobRuntimeInfosFromManagerDto(jobFileCheck.getJobRuntimeParamList());
		fileCheckXML.setJobRuntimeInfos(runtimes.toArray(new JobRuntimeInfos[0]));

		return fileCheckXML;
	}

	/**
	 * マニュアル実行契機のXML出力データを作成して返す.
	 * @param dto Managerから受取ったマニュアル実行契機のDTO
	 * @return XML出力用のデータ
	 */
	public ManualInfo manualDto2Xml(JobKickResponse dto) {

		ManualInfo manualXML = new ManualInfo();
		manualXML.setId(dto.getId());
		manualXML.setName(dto.getName());
		manualXML.setJobId(dto.getJobId());
		manualXML.setJobunitId(dto.getJobunitId());
		manualXML.setOwnerRoleId(dto.getOwnerRoleId());

		// 有効/無効
		manualXML.setValidFlg(dto.getValid());

		// ジョブ変数
		List<JobRuntimeInfos> runtimes = getJobRuntimeInfosFromManagerDto(dto.getJobRuntimeParamList());
		manualXML.setJobRuntimeInfos(runtimes.toArray(new JobRuntimeInfos[0]));

		return manualXML;
	}

	/**
	 * マニュアル実行契機のインポート用データを作成して返す.
	 * @param xml エクスポートしたXMLファイルから取得したデータ
	 * @return インポート用のDTO
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public JobKickResponse manualXml2Dto(ManualInfo src) throws InvalidSetting, HinemosUnknown {
		// 入力データチェック
		if (!isValidManualInfoIds(src)){
			return null;
		}
		
		JobKickResponse dest = new JobKickResponse();
		dest.setId(src.getId());
		dest.setName(src.getName());
		dest.setJobId(src.getJobId());
		dest.setJobunitId(src.getJobunitId());
		dest.setOwnerRoleId(src.getOwnerRoleId());
		dest.setType(JobKickResponse.TypeEnum.MANUAL);
		dest.setValid(src.getValidFlg());

		// ジョブ変数リスト生成
		List<JobRuntimeParamResponse> paramlist = createRuntimeListFrom(src.getJobRuntimeInfos());
		dest.getJobRuntimeParamList().addAll(paramlist);
		
		return dest;
	}
	
	/**
	 * XMLのBeanからHinemosManagerのBeanに変換しします。
	 * @param jobLinkRcv XMLのBean
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public JobLinkRcvResponse jobLinkRcvXml2Dto(JobLinkRcvInfo jobLinkRcv) throws InvalidSetting, HinemosUnknown {

		JobLinkRcvData data = jobLinkRcv.getJobLinkRcvData();
		
		// 投入用データの作成
		JobLinkRcvResponse info = new JobLinkRcvResponse();

		// ID
		if (!StringUtil.isNullOrEmpty(jobLinkRcv.getId())) {
			info.setId(jobLinkRcv.getId());
		} else {
			log.warn(String.format("%s(ScheduleId) : %s", MESSAGE_ESSENTIALVALUEINVALID, jobLinkRcv.getId()));
			return null;
		}
		// 名前
		if (!StringUtil.isNullOrEmpty(jobLinkRcv.getName())) {
			info.setName(jobLinkRcv.getName());
		} else {
			log.warn(String.format("%s(ScheduleName) : %s", MESSAGE_ESSENTIALVALUEINVALID, jobLinkRcv.getId()));
			return null;
		}
		// ジョブユニットID
		if (!StringUtil.isNullOrEmpty(jobLinkRcv.getJobunitId())) {
			info.setJobunitId(jobLinkRcv.getJobunitId());
		} else {
			log.warn(String.format("%s(JobunitId) : %s", MESSAGE_ESSENTIALVALUEINVALID, jobLinkRcv.getId()));
			return null;
		}
		// ジョブID
		if (!StringUtil.isNullOrEmpty(jobLinkRcv.getJobId())) {
			info.setJobId(jobLinkRcv.getJobId());
		} else {
			log.warn(String.format("%s(JobId) : %s", MESSAGE_ESSENTIALVALUEINVALID, jobLinkRcv.getId()));
			return null;
		}
		// オーナーロールID
		if (!StringUtil.isNullOrEmpty(jobLinkRcv.getOwnerRoleId())) {
			info.setOwnerRoleId(jobLinkRcv.getOwnerRoleId());
		} else {
			log.warn(String.format("%s(OwnerRoleId) : %s", MESSAGE_ESSENTIALVALUEINVALID, jobLinkRcv.getId()));
			return null;
		}
		// カレンダーID
		if (jobLinkRcv.getCalId().length() > 0) {
			info.setCalendarId(jobLinkRcv.getCalId());
		} else {
			// カレンダーはなくてもＯＫ
		}
		// 有効・無効フラグ
		info.setValid(jobLinkRcv.getValidFlg());

		// 送信元ファシリティID
		info.setFacilityId(data.getFacilityId());
		// ジョブ連携メッセージID
		info.setJoblinkMessageId(data.getJoblinkMessageId());
		// 重要度（情報）
		info.setInfoValidFlg(data.getInfoValidFlg());
		// 重要度（警告）
		info.setWarnValidFlg(data.getWarnValidFlg());
		// 重要度（危険）
		info.setCriticalValidFlg(data.getCriticalValidFlg());
		// 重要度（不明）
		info.setUnknownValidFlg(data.getUnknownValidFlg());
		// アプリケーションフラグ
		info.setApplicationFlg(data.getApplicationFlg());
		// アプリケーション
		info.setApplication(data.getApplication());
		// 監視詳細フラグ
		info.setMonitorDetailIdFlg(data.getMonitorDetailIdFlg());
		// 監視詳細
		info.setMonitorDetailId(data.getMonitorDetailId());
		// メッセージフラグ
		info.setMessageFlg(data.getMessageFlg());
		// メッセージ
		info.setMessage(data.getMessage());
		// 拡張情報フラグ
		info.setExpFlg(data.getExpFlg());
		
		// 拡張情報
		List<JobLinkExpInfoResponse> jobLinkExpList = new ArrayList<>();
		for(JobLinkExpInfo jobLinkExpInfo : data.getJobLinkExpInfo()) {
			JobLinkExpInfoResponse jobLinkExp = new JobLinkExpInfoResponse();
			jobLinkExp.setKey(jobLinkExpInfo.getKey());
			jobLinkExp.setValue(jobLinkExpInfo.getValue());
			jobLinkExpList.add(jobLinkExp);
		}
		info.setJobLinkExpList(jobLinkExpList);

		// ジョブ変数
		List<JobRuntimeParamResponse> paramlist = createRuntimeListFrom(jobLinkRcv.getJobRuntimeInfos());
		info.getJobRuntimeParamList().addAll(paramlist);

		return info;
	}

	/**
	 * ジョブ連携受信実行契機のXML出力データを作成して返す.
	 * @param dto Managerから受取ったマニュアル実行契機のDTO
	 * @return XML出力用のデータ
	 */
	public JobLinkRcvInfo jobLinkRcvDto2Xml(JobKickResponse dto) {

		JobLinkRcvInfo jobLinkRcvXML = new JobLinkRcvInfo();
		// ID
		jobLinkRcvXML.setId(dto.getId());
		// 名前
		jobLinkRcvXML.setName(dto.getName());
		// ジョブID
		jobLinkRcvXML.setJobId(dto.getJobId());
		// ジョブユニットID
		jobLinkRcvXML.setJobunitId(dto.getJobunitId());
		// オーナーロールID
		jobLinkRcvXML.setOwnerRoleId(dto.getOwnerRoleId());

		//カレンダIDがNULLでなければ投入
		if (dto.getCalendarId() != null) {
			jobLinkRcvXML.setCalId(dto.getCalendarId());
		} else {
			jobLinkRcvXML.setCalId("");
		}

		// 有効/無効
		jobLinkRcvXML.setValidFlg(dto.getValid());

		JobLinkRcvData dataXML = new JobLinkRcvData();
		// 送信元ファシリティID
		dataXML.setFacilityId(dto.getFacilityId());
		// ジョブ連携メッセージID
		dataXML.setJoblinkMessageId(dto.getJoblinkMessageId());
		// 重要度（情報）
		dataXML.setInfoValidFlg(dto.getInfoValidFlg());
		// 重要度（警告）
		dataXML.setWarnValidFlg(dto.getWarnValidFlg());
		// 重要度（危険）
		dataXML.setCriticalValidFlg(dto.getCriticalValidFlg());
		// 重要度（不明）
		dataXML.setUnknownValidFlg(dto.getUnknownValidFlg());
		// アプリケーションフラグ
		dataXML.setApplicationFlg(dto.getApplicationFlg());
		// アプリケーション
		dataXML.setApplication(dto.getApplication());
		// 監視詳細フラグ
		dataXML.setMonitorDetailIdFlg(dto.getMonitorDetailIdFlg());
		// 監視詳細
		dataXML.setMonitorDetailId(dto.getMonitorDetailId());
		// メッセージフラグ
		dataXML.setMessageFlg(dto.getMessageFlg());
		// メッセージ
		dataXML.setMessage(dto.getMessage());
		// 拡張情報フラグ
		dataXML.setExpFlg(dto.getExpFlg());

		// 拡張情報
		List<JobLinkExpInfo> jobLinkExpList = new ArrayList<>();
		for(JobLinkExpInfoResponse jobLinkExpInfo : dto.getJobLinkExpList()) {
			JobLinkExpInfo jobLinkExp = new JobLinkExpInfo();
			jobLinkExp.setKey(jobLinkExpInfo.getKey());
			jobLinkExp.setValue(jobLinkExpInfo.getValue());
			jobLinkExpList.add(jobLinkExp);
		}
		dataXML.setJobLinkExpInfo(jobLinkExpList.toArray(new JobLinkExpInfo[0]));

		jobLinkRcvXML.setJobLinkRcvData(dataXML);

		// ジョブ変数
		List<JobRuntimeInfos> runtimes = getJobRuntimeInfosFromManagerDto(dto.getJobRuntimeParamList());
		jobLinkRcvXML.setJobRuntimeInfos(runtimes.toArray(new JobRuntimeInfos[0]));

		return jobLinkRcvXML;
	}
	
	/**
	 * ManualInfoのIDチェックを行う.
	 * @param src
	 * @return 不正なデータが存在する場合はfalseを返却する.
	 */
	private boolean isValidManualInfoIds(ManualInfo src) {

		// ID
		if (StringUtil.isNullOrEmpty(src.getId())){
			log.warn(String.format("%s(Id) : %s", MESSAGE_ESSENTIALVALUEINVALID, src.getId()));
			return false;
		}
		// 名前
		if (StringUtil.isNullOrEmpty(src.getName())){
			log.warn(String.format("%s(Name) : %s", MESSAGE_ESSENTIALVALUEINVALID, src.getName()));
			return false;
		}
		// ジョブユニットID
		if (StringUtil.isNullOrEmpty(src.getJobunitId())){
			log.warn(String.format("%s(JobunitId) : %s", MESSAGE_ESSENTIALVALUEINVALID, src.getJobunitId()));
			return false;
		}
		// ジョブID
		if (StringUtil.isNullOrEmpty(src.getJobId())){
			log.warn(String.format("%s(JobId) : %s", MESSAGE_ESSENTIALVALUEINVALID, src.getJobId()));
			return false;
		}
		// オーナーロールID
		if (StringUtil.isNullOrEmpty(src.getOwnerRoleId())){
			log.warn(String.format("%s(OwnerRoleId) : %s", MESSAGE_ESSENTIALVALUEINVALID, src.getOwnerRoleId()));
			return false;
		}
		
		return true;
	}
	

	/**
	 * Managerから受取ったDTOのジョブ変数リストをXML出力データで使用する形式に変換して返します.
	 * @param paramlist ジョブ変数のリスト
	 * @return
	 */
	private List<JobRuntimeInfos> getJobRuntimeInfosFromManagerDto(List<JobRuntimeParamResponse> paramlist) {
		// ジョブ変数
		List<JobRuntimeInfos> runtimes = new ArrayList<>();
		for(JobRuntimeParamResponse e : paramlist){
			JobRuntimeInfos info = new JobRuntimeInfos();
			info.setParamId(e.getParamId());
			int paramTypeInt = OpenApiEnumConverter.enumToInteger(e.getParamType());
			info.setParamType(paramTypeInt);
			if (e.getValue() == null){
				info.setDefaultValue("");
			}else{
				info.setDefaultValue(e.getValue());
			}
			info.setDescription(e.getDescription());
			info.setRequiredFlg(e.getRequiredFlg());

			// ジョブ変数の詳細
			int no = 0;
			List<JobRuntimeDetailInfos> runtimedetails = new ArrayList<>();
			for(JobRuntimeParamDetailResponse detail : e.getJobRuntimeParamDetailList()){
				JobRuntimeDetailInfos detailinfo = new JobRuntimeDetailInfos();
				detailinfo.setOrderNo(++no);
				detailinfo.setParamValue(detail.getParamValue());
				detailinfo.setDescription(detail.getDescription());
				runtimedetails.add(detailinfo);
			}
			info.setJobRuntimeDetailInfos(runtimedetails.toArray(new JobRuntimeDetailInfos[0]));
			
			runtimes.add(info);
		}
		return runtimes;
	}
	
	/**
	 * インポート用DTOのジョブ変数リストを作成して返します.
	 * @param runtimeinfos XML側のジョブ変数のリスト
	 * @return インポート用DTO
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private List<JobRuntimeParamResponse> createRuntimeListFrom(JobRuntimeInfos[] runtimeinfos) throws InvalidSetting, HinemosUnknown{
		List<JobRuntimeParamResponse> dtolist = new ArrayList<>();
		// ジョブ変数
		for(JobRuntimeInfos xmldata : runtimeinfos) {
			JobRuntimeParamResponse dto = new JobRuntimeParamResponse();
			dto.setParamId(xmldata.getParamId());
			ParamTypeEnum paramTypeEnum = OpenApiEnumConverter.integerToEnum(xmldata.getParamType(), ParamTypeEnum.class);
			dto.setParamType(paramTypeEnum);
			if (xmldata.getDefaultValue() == null || xmldata.getDefaultValue().isEmpty()){
				dto.setValue(null);
			}else{
				dto.setValue(xmldata.getDefaultValue());
			}
			dto.setDescription(xmldata.getDescription());
			dto.setRequiredFlg(xmldata.getRequiredFlg());
			// ジョブ変数詳細
			JobRuntimeDetailInfos[] detailinfos = xmldata.getJobRuntimeDetailInfos();
			
			try {
				Arrays.sort(
						detailinfos,
					new Comparator<JobRuntimeDetailInfos>() {
						@Override
						public int compare(JobRuntimeDetailInfos obj1, JobRuntimeDetailInfos obj2) {
							return obj1.getOrderNo() - obj2.getOrderNo();
						}
					});
			}
			catch (Exception e) {
			}
			
			for (JobRuntimeDetailInfos detailinfo : detailinfos) {
				JobRuntimeParamDetailResponse dtodetail = new JobRuntimeParamDetailResponse();
				dtodetail.setParamValue(detailinfo.getParamValue());
				dtodetail.setDescription(detailinfo.getDescription());
				dto.getJobRuntimeParamDetailList().add(dtodetail);
			}
			dtolist.add(dto);
		}
		return dtolist;
	}
}
