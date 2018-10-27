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

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.AbstractConvertor;
import com.clustercontrol.utility.settings.job.xml.FileCheckData;
import com.clustercontrol.utility.settings.job.xml.FileCheckInfo;
import com.clustercontrol.utility.settings.job.xml.JobRuntimeDetailInfos;
import com.clustercontrol.utility.settings.job.xml.JobRuntimeInfos;
import com.clustercontrol.utility.settings.job.xml.ManualInfo;
import com.clustercontrol.utility.settings.job.xml.ScheduleData;
import com.clustercontrol.utility.settings.job.xml.ScheduleInfo;
import com.clustercontrol.utility.util.StringUtil;
import com.clustercontrol.ws.jobmanagement.JobFileCheck;
import com.clustercontrol.ws.jobmanagement.JobKick;
import com.clustercontrol.ws.jobmanagement.JobRuntimeParam;
import com.clustercontrol.ws.jobmanagement.JobRuntimeParamDetail;
import com.clustercontrol.ws.jobmanagement.JobSchedule;

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

	/** スキーマタイプ */
	private static final String schemaType="H";
	/** スキーマバージョン */
	private static final String schemaVersion="1";
	/** スキーマレビジョン */
	private static final String schemaRevision ="1";
	
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
	 */
	public JobSchedule scheduleXml2Dto(ScheduleInfo schedule) {
		
		ScheduleData data = schedule.getScheduleData();

		// 投入用データの作成
		JobSchedule info = new JobSchedule();
		info.setType(JobTriggerTypeConstant.TYPE_SCHEDULE);
		
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
		info.setScheduleType(data.getScheduleType());
		// 曜日
		info.setWeek(data.getWeek());
		// 時
		if(data.getHour() == -1){
			info.setHour(null);
		} else {
			info.setHour(data.getHour());
		}
		// 分
		info.setMinute(data.getMinute());
		// X分から
		info.setFromXminutes(data.getFromXminutes());
		// X分ごとに繰り返し実行
		info.setEveryXminutes(data.getEveryXminutes());
		
		// ジョブ変数
		List<JobRuntimeParam> paramlist = createRuntimeListFrom(schedule.getJobRuntimeInfos());
		info.getJobRuntimeParamList().addAll(paramlist);

		return info;
	}

	/**
	 * Managerで利用されているスケジュールデータをXMLのBeanにマッピングします。
	 * @param jobSchedule マネージャで利用されいる形式のスケジュールデータ
	 * @return 出力用XMLのBean
	 */
	public ScheduleInfo scheduleDto2Xml(JobSchedule jobSchedule) {

		// scheduleXML : XMLバイディング用のデータ
		// dataXML : XMLバイディング用の追加データ
		ScheduleInfo scheduleXML = new ScheduleInfo();
		scheduleXML.setId(jobSchedule.getId());
		scheduleXML.setName(jobSchedule.getName());
		//scheduleXML.setType(jobSchedule.getType());

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
		scheduleXML.setValidFlg(jobSchedule.isValid());
		
		// 日付データの投入
		ScheduleData dataXML = new ScheduleData();
		dataXML.setScheduleType(jobSchedule.getScheduleType());
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
		if (jobSchedule.getEveryXminutes() != null) {
			dataXML.setEveryXminutes(jobSchedule.getEveryXminutes());
		} else {
			// everyXminutesはrequiredの為、仮の値(0)を入力
			dataXML.setEveryXminutes(0);
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
	 */
	public JobFileCheck fileCheckXml2Dto(FileCheckInfo fileCheck) {

		FileCheckData data = fileCheck.getFileCheckData();
		
		// 投入用データの作成
		JobFileCheck info = new JobFileCheck();
		info.setType(JobTriggerTypeConstant.TYPE_FILECHECK);

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
		// イベント種別
		info.setEventType(data.getEventType());
		// 変更種別
		info.setModifyType(data.getModifyType());

		// ジョブ変数
		List<JobRuntimeParam> paramlist = createRuntimeListFrom(fileCheck.getJobRuntimeInfos());
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
	public FileCheckInfo fileCheckDto2Xml(JobFileCheck jobFileCheck) {

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
		fileCheckXML.setValidFlg(jobFileCheck.isValid());

		FileCheckData dataXML = new FileCheckData();
		dataXML.setFacilityId(jobFileCheck.getFacilityId());
		dataXML.setDirectory(jobFileCheck.getDirectory());
		dataXML.setFileName(jobFileCheck.getFileName());
		dataXML.setEventType(jobFileCheck.getEventType());
		if (jobFileCheck.getModifyType() != null) {
			dataXML.setModifyType(jobFileCheck.getModifyType());
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
	public ManualInfo manualDto2Xml(JobKick dto) {

		ManualInfo manualXML = new ManualInfo();
		manualXML.setId(dto.getId());
		manualXML.setName(dto.getName());
		manualXML.setJobId(dto.getJobId());
		manualXML.setJobunitId(dto.getJobunitId());
		manualXML.setOwnerRoleId(dto.getOwnerRoleId());
		//manualXML.setType(2);

		// カレンダIDがNULLでなければ投入
		//manualXML.setCalId(Objects.isNull(dto.getCalendarId()) ? "" : dto.getCalendarId());

		// 有効/無効
		manualXML.setValidFlg(dto.isValid());

		// ジョブ変数
		List<JobRuntimeInfos> runtimes = getJobRuntimeInfosFromManagerDto(dto.getJobRuntimeParamList());
		manualXML.setJobRuntimeInfos(runtimes.toArray(new JobRuntimeInfos[0]));

		return manualXML;
	}

	/**
	 * マニュアル実行契機のインポート用データを作成して返す.
	 * @param xml エクスポートしたXMLファイルから取得したデータ
	 * @return インポート用のDTO
	 */
	public JobKick manualXml2Dto(ManualInfo src) {
		// 入力データチェック
		if (!isValidManualInfoIds(src)){
			return null;
		}
		
		JobKick dest = new JobKick();
		dest.setId(src.getId());
		dest.setName(src.getName());
		dest.setJobId(src.getJobId());
		dest.setJobunitId(src.getJobunitId());
		dest.setOwnerRoleId(src.getOwnerRoleId());
		//dest.setType(src.getType());
		dest.setType(JobTriggerTypeConstant.TYPE_MANUAL);
		//dest.setCalendarId(src.getCalId());
		dest.setValid(src.getValidFlg());

		// ジョブ変数リスト生成
		List<JobRuntimeParam> paramlist = createRuntimeListFrom(src.getJobRuntimeInfos());
		dest.getJobRuntimeParamList().addAll(paramlist);
		
		return dest;
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
	private List<JobRuntimeInfos> getJobRuntimeInfosFromManagerDto(List<JobRuntimeParam> paramlist) {
		// ジョブ変数
		List<JobRuntimeInfos> runtimes = new ArrayList<>();
		for(JobRuntimeParam e : paramlist){
			JobRuntimeInfos info = new JobRuntimeInfos();
			info.setParamId(e.getParamId());
			info.setParamType(e.getParamType());
			if (e.getValue() == null){
				info.setDefaultValue("");
			}else{
				info.setDefaultValue(e.getValue());
			}
			info.setDescription(e.getDescription());
			info.setRequiredFlg(e.isRequiredFlg());

			// ジョブ変数の詳細
			int no = 0;
			List<JobRuntimeDetailInfos> runtimedetails = new ArrayList<>();
			for(JobRuntimeParamDetail detail : e.getJobRuntimeParamDetailList()){
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
	 */
	private List<JobRuntimeParam> createRuntimeListFrom(JobRuntimeInfos[] runtimeinfos){
		List<JobRuntimeParam> dtolist = new ArrayList<>();
		// ジョブ変数
		for(JobRuntimeInfos xmldata : runtimeinfos) {
			JobRuntimeParam dto = new JobRuntimeParam();
			dto.setParamId(xmldata.getParamId());
			dto.setParamType(xmldata.getParamType());
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
				JobRuntimeParamDetail dtodetail = new JobRuntimeParamDetail();
				dtodetail.setParamValue(detailinfo.getParamValue());
				dtodetail.setDescription(detailinfo.getDescription());
				dto.getJobRuntimeParamDetailList().add(dtodetail);
			}
			dtolist.add(dto);
		}
		return dtolist;
	}
}
