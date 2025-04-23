/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.binary.bean.BinaryQueryInfo;
import com.clustercontrol.binary.factory.BinaryHubController;
import com.clustercontrol.binary.session.BinaryControllerBean;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.collect.session.CollectControllerBean;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.BinaryRecordNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.PerfFileNotFound;
import com.clustercontrol.hub.bean.StringData;
import com.clustercontrol.hub.bean.StringQueryInfo;
import com.clustercontrol.hub.bean.StringQueryResult;
import com.clustercontrol.hub.bean.Tag;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.performance.bean.CollectMasterInfo;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.session.PerformanceCollectMasterControllerBean;
import com.clustercontrol.performance.session.PerformanceControllerBean;
import com.clustercontrol.performance.util.code.CollectorItemTreeItem;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.rest.RestConstant;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemAdminPrivilege;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.endpoint.collect.dto.AddCollectMasterRequest;
import com.clustercontrol.rest.endpoint.collect.dto.ArrayListInfoResponse;
import com.clustercontrol.rest.endpoint.collect.dto.BinaryDataResponse;
import com.clustercontrol.rest.endpoint.collect.dto.BinaryQueryResultResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectDataInfoResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectDataResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectKeyInfoRequest;
import com.clustercontrol.rest.endpoint.collect.dto.CollectKeyInfoResponseP1;
import com.clustercontrol.rest.endpoint.collect.dto.CollectKeyMapForAnalyticsResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectKeyResponseP1;
import com.clustercontrol.rest.endpoint.collect.dto.CollectMasterInfoResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectorItemCodeMstDataResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectorItemCodeMstInfoResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectorItemCodeMstMapResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectorItemCodeMstResponseP1;
import com.clustercontrol.rest.endpoint.collect.dto.CollectorItemInfoResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CollectorItemTreeItemResponse;
import com.clustercontrol.rest.endpoint.collect.dto.CreatePerfFileRequest;
import com.clustercontrol.rest.endpoint.collect.dto.CreatePerfFileResponse;
import com.clustercontrol.rest.endpoint.collect.dto.DownloadBinaryRecordRequest;
import com.clustercontrol.rest.endpoint.collect.dto.DownloadBinaryRecordsKeyRequest;
import com.clustercontrol.rest.endpoint.collect.dto.DownloadBinaryRecordsRequest;
import com.clustercontrol.rest.endpoint.collect.dto.GetCoefficientsRequest;
import com.clustercontrol.rest.endpoint.collect.dto.GetCoefficientsResponse;
import com.clustercontrol.rest.endpoint.collect.dto.GetCollectDataResponse;
import com.clustercontrol.rest.endpoint.collect.dto.GetCollectIdResponse;
import com.clustercontrol.rest.endpoint.collect.dto.GetItemCodeListResponse;
import com.clustercontrol.rest.endpoint.collect.dto.QueryCollectBinaryDataRequest;
import com.clustercontrol.rest.endpoint.collect.dto.QueryCollectStringDataRequest;
import com.clustercontrol.rest.endpoint.collect.dto.RunSummaryLogcountRequest;
import com.clustercontrol.rest.endpoint.collect.dto.StringDataResponse;
import com.clustercontrol.rest.endpoint.collect.dto.StringQueryResultResponse;
import com.clustercontrol.rest.endpoint.collect.dto.TagResponse;
import com.clustercontrol.rest.endpoint.collect.dto.emuntype.SummaryTypeEnum;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestDownloadFile;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileType;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

@Path("/collect")
@RestLogFunc(name = LogFuncName.Collector)
public class CollectRestEndpoints {

	private static Log m_log = LogFactory.getLog( CollectRestEndpoints.class );

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "collect";

	private static boolean debug = false;//debug用のフラグ。

	/**
	 * 
	 * ファシリティIDをキーとし、収集IDが格納されているCollectKeyResponseのリストを取得します
	 *
	 * CollectRead権限が必要
	 *
	 * @param itemCode 収集項目コード
	 * @param displayName 表示名(リソース監視)
	 * @param facilityIdList ファシリティIDのリスト
	 * @return ファシリティIDをキーとし、収集IDが格納されているCollectKeyResponseのリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/key/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCollectId")
	@RestLog(action = LogAction.Get, target = LogTarget.Key, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetCollectIdResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectId(@Context Request request, @Context UriInfo uriInfo,
			@RestValidateObject(notNull=true) @QueryParam(value = "itemName") String itemName, 
			@RestValidateObject(notNull=true) @QueryParam(value = "displayName") String displayName, 
			@QueryParam(value = "size") String sizeStr, 
			@PathParam(value = "monitorId") String monitorId, 
			@RestValidateObject(notNull=true) @ArrayTypeParam @QueryParam(value = "facilityIds") String facilityIds) 
					throws InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call getCollectId");
		Integer size = RestCommonConverter.convertInteger(MessageConstant.SIZE.getMessage(), sizeStr, false, 1, null);

		CommonValidator.validateString(MessageConstant.FACILITY_ID.getMessage(), facilityIds, true, 1, Integer.MAX_VALUE);
		CommonValidator.validateNull(MessageConstant.COLLECTION_ITEM_NAME.getMessage(), itemName);
		CommonValidator.validateNull(MessageConstant.COLLECTION_DISPLAY_NAME.getMessage(), displayName);

		List<CollectKeyResponseP1> dtoResList = new ArrayList<>();

		List<String> facilityIdList = new ArrayList<>();
		if(facilityIds != null && !facilityIds.isEmpty()) {
			facilityIdList = Arrays.asList(facilityIds.split(","));
		}

		//debugがtrueでサンプルデータ、falseで実データを取得
		if(debug){
			for (String facilityId : facilityIdList) {
				int id = 0;
				id += itemName.hashCode();
				id *= 37;
				if (displayName != null) {
					id += displayName.hashCode();
				}
				id *= 37;
				id += facilityId.hashCode();

				CollectKeyResponseP1 dtoRes = new CollectKeyResponseP1();
				dtoRes.setFacilityId(facilityId);
				dtoRes.setId(id);
				dtoResList.add(dtoRes);
			}
		} else {
			for(String facilityId : facilityIdList){
				m_log.debug("itemName:" + itemName + ", displayName:"+displayName + ", monitorId:" + monitorId + ", facilityId:" + facilityId);
				try {
					Integer id = new CollectControllerBean().getCollectId(itemName, displayName, monitorId, facilityId);
					CollectKeyResponseP1 dtoRes = new CollectKeyResponseP1();
					dtoRes.setFacilityId(facilityId);
					dtoRes.setId(id);
					dtoResList.add(dtoRes);
				} catch (Exception e) {
					m_log.debug(e.getClass().getName() + ", itemName:" + itemName + ", displayName:"+displayName + 
							", monitorId:" + monitorId + ", facilityId:" + facilityId);
				}
			}
		}
		List<CollectKeyResponseP1> dtoResListZap = new ArrayList<>();
		if( size != null ){
			int recCount = 0;
			for (CollectKeyResponseP1 rec : dtoResList) {
				dtoResListZap.add(rec);
				recCount++;
				if( recCount >= size ){
					break;
				}
			}
		}else{
			dtoResListZap = dtoResList;
		}

		RestLanguageConverter.convertMessages(dtoResListZap);

		GetCollectIdResponse dtoRes = new GetCollectIdResponse();
		dtoRes.setKeyList(dtoResListZap);
		dtoRes.setTotal(dtoResList.size());

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 
	 * 収集IDをキーとし、収集IDと時間とデータが格納されているリストを取得します
	 *
	 * CollectRead権限が必要
	 *
	 * @param ids 収集IDのリスト
	 * @param summaryType サマリタイプ
	 * @param fromTime 取得するデータの時間(起点)
	 * @param toTime 取得するデータの時間(終点)
	 * @return 収集IDをキーとし、収集IDと時間とデータが格納されているリスト
	 * @throws HinemosDbTimeout
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */

	@GET
	@Path("/data")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCollectData")
	@RestLog(action = LogAction.Get, target = LogTarget.Data, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetCollectDataResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectData (@Context Request request, @Context UriInfo uriInfo,
			@QueryParam(value = "size") String sizeStr,
			@RestValidateObject(notNull=true) @ArrayTypeParam @QueryParam(value = "idList") String ids, 
			@RestValidateObject(notNull=true) @QueryParam(value = "summaryType") String summaryType, 
			@RestValidateObject(notNull=true) @QueryParam(value = "fromTime") String fromTime, 
			@RestValidateObject(notNull=true) @QueryParam(value = "toTime") String toTime) 
			throws HinemosDbTimeout, InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCollectData()");

		Integer size = RestCommonConverter.convertInteger(MessageConstant.SIZE.getMessage(), sizeStr, false, 1, null);
		CommonValidator.validateString(MessageConstant.COLLECTOR_ID.getMessage(), ids, true, 1, Integer.MAX_VALUE);
		CommonValidator.validateNull(MessageConstant.SUMMARYTYPE.getMessage(), summaryType);
		CommonValidator.validateNull(MessageConstant.GENERATION_DATE_FROM.getMessage(), fromTime);
		CommonValidator.validateNull(MessageConstant.GENERATION_DATE_TO.getMessage(), toTime);
		SummaryTypeEnum summaryTypeEnum = RestCommonConverter.convertEnum(MessageConstant.SUMMARYTYPE.getMessage(),
				summaryType, SummaryTypeEnum.values());
		
		List<CollectDataResponse> dtoResList = new ArrayList<>();

		List<String> idList = new ArrayList<>();
		if(ids != null && !ids.isEmpty()) {
			idList = Arrays.asList(ids.split(","));
		}
		
		List<Integer> tmpIdList = new ArrayList<>();
		for(String tmpStr:idList){
			 tmpIdList.add(Integer.parseInt(tmpStr));
		}
		if(m_log.isDebugEnabled()){
			m_log.debug("debug tmpIdList"+ tmpIdList.toString());
		}

		HashMap<Integer, ArrayListInfoResponse> map = new HashMap<>();

		long start = HinemosTime.currentTimeMillis();
		//デバッグ用
		if(debug){
			int count = 0;
			int span = 60 * 1000;
				switch (summaryTypeEnum.getCode()) {
				case SummaryTypeConstant.TYPE_RAW :
					span *= 5; break; // 5分
				case SummaryTypeConstant.TYPE_AVG_HOUR: 
					span *= 12; break; // 1時間
				case SummaryTypeConstant.TYPE_MIN_HOUR: 
					span *= 12; break; // 1時間
				case SummaryTypeConstant.TYPE_MAX_HOUR: 
					span *= 12; break; // 1時間
				case SummaryTypeConstant.TYPE_AVG_DAY : 
					span *= 12 * 24; break; // 1日
				case SummaryTypeConstant.TYPE_MIN_DAY : 
					span *= 12 * 24; break; // 1日
				case SummaryTypeConstant.TYPE_MAX_DAY : 
					span *= 12 * 24; break; // 1日
				case SummaryTypeConstant.TYPE_AVG_MONTH : 
					span *= 12 * 24 * 30; break; // 1ヶ月
				case SummaryTypeConstant.TYPE_MIN_MONTH : 
					span *= 12 * 24 * 30; break; // 1ヶ月
				case SummaryTypeConstant.TYPE_MAX_MONTH : 
					span *= 12 * 24 * 30; break; // 1ヶ月
				default :
					break;
				}
			for (Integer id : tmpIdList) {
				ArrayListInfoResponse list = new ArrayListInfoResponse();
				// spanごとにランダムデータが入るように。
				long fromTimeLong = RestCommonConverter.convertDTStringToHinemosTime(fromTime, MessageConstant.GENERATION_DATE_FROM.getMessage());
				long toTimeLong = RestCommonConverter.convertDTStringToHinemosTime(toTime, MessageConstant.GENERATION_DATE_TO.getMessage());
				long time = fromTimeLong / span * span; // 分以下を切り捨て
				while (true) {
					time += span;
					if (toTimeLong < time) {
						break;
					}
					double tmp = (Math.random()*10);
					CollectDataInfoResponse data = new CollectDataInfoResponse();
					data.setTime(RestCommonConverter.convertHinemosTimeToDTString(time));
					data.setValue((float)tmp);
					list.getList1().add(data);
					count ++;
					m_log.debug("id:" + id + ", time:" + time + ", value:" + tmp);
				}
				map.put(id, list);
			}
			try {
				Thread.sleep(count / 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			CollectControllerBean controller = new CollectControllerBean();
			long fromTimeLong = RestCommonConverter.convertDTStringToHinemosTime(fromTime, MessageConstant.GENERATION_DATE_FROM.getMessage());
			long toTimeLong = RestCommonConverter.convertDTStringToHinemosTime(toTime, MessageConstant.GENERATION_DATE_TO.getMessage());
			
			//欲しいサマリデータ、または収集データ(raw)のタイプでスイッチ
			switch(summaryTypeEnum.getCode()){
				case SummaryTypeConstant.TYPE_AVG_HOUR: {	
					ArrayListInfoResponse list;
					List<SummaryHour> summaryList = controller.getSummaryHourList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryHour summary : summaryList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getAvg());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
						}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_HOUR: {	
					ArrayListInfoResponse list;
					List<SummaryHour> summaryList = controller.getSummaryHourList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryHour summary : summaryList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getMin());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_HOUR: {	
					ArrayListInfoResponse list;
					List<SummaryHour> summaryHourList = controller.getSummaryHourList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryHour summary : summaryHourList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getMax());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_AVG_DAY: {
					ArrayListInfoResponse list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryDay summary : summaryList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getAvg());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_DAY: {
					ArrayListInfoResponse list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryDay summary : summaryList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getMin());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_DAY: {
					ArrayListInfoResponse list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryDay summary : summaryList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getMax());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_AVG_MONTH: {
					ArrayListInfoResponse list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryMonth summary : summaryList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getAvg());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_MONTH: {
					ArrayListInfoResponse list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryMonth summary : summaryList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getMin());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_MONTH: {
					ArrayListInfoResponse list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(tmpIdList, fromTimeLong, toTimeLong);
					for (SummaryMonth summary : summaryList) {
						CollectDataInfoResponse data = new CollectDataInfoResponse();
						data.setCollectorId(summary.getId().getCollectorid());//CollectDataPK
						data.setTime(RestCommonConverter.convertHinemosTimeToDTString(summary.getTime()));
						data.setValue(summary.getMax());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getCollectorId()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getCollectorId(), list);
						}
						map.get(data.getCollectorId()).getList1().add(data);
					}
					break;
				}
				default: { // defaultはRAWとする
					ArrayListInfoResponse list;
					List<com.clustercontrol.collect.model.CollectData> dataList = controller.getCollectDataList(tmpIdList, fromTimeLong, toTimeLong);
					for(com.clustercontrol.collect.model.CollectData data : dataList){
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfoResponse();
							map.put(data.getId().getCollectorid(), list);
						}
						CollectDataInfoResponse collectData = new CollectDataInfoResponse();
						RestBeanUtil.convertBeanNoInvalid(data, collectData);
						collectData.setCollectorId(data.getId().getCollectorid());
						collectData.setTime(RestCommonConverter.convertHinemosTimeToDTString(data.getId().getTime()));
						map.get(data.getCollectorId()).getList1().add(collectData);
					}
					break;
				}
			}

			// mapのKey毎データをDTOクラスに格納
			List<Integer> tmp = new ArrayList<Integer>(map.keySet());
			for(Integer id : tmp){
				CollectDataResponse dtoRes = new CollectDataResponse();
				dtoRes.setCollectId(id);
				dtoRes.setArrayListInfoResponse(map.get(id));
				dtoResList.add(dtoRes);
			}
		}

		List<CollectDataResponse> dtoResListZap = new ArrayList<>();
		if( size != null ){
			int recCount = 0;
			for (CollectDataResponse rec : dtoResList) {
				dtoResListZap.add(rec);
				recCount++;
				if( recCount >= size ){
					break;
				}
			}
		}else{
			dtoResListZap = dtoResList;
		}
		
		if (m_log.isInfoEnabled()) { // debug
			int dateSize = 0;
			for (Map.Entry<Integer, ArrayListInfoResponse> entry : map.entrySet()) {
				dateSize += entry.getValue().size();
			}
			long difftime = HinemosTime.currentTimeMillis() - start;
			if (difftime > 5 * 1000) {
				m_log.info("getCollectData end   size=" + dateSize + ", " + difftime + "ms"); // debug
			}
		}
		RestLanguageConverter.convertMessages(dtoResList);

		GetCollectDataResponse dtoRes = new GetCollectDataResponse();
		dtoRes.setCollectDataList(dtoResListZap);
		dtoRes.setTotal(dtoResList.size());

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 収集項目コードのリストを取得します
	 *
	 * CollectRead権限が必要
	 *
	 * @param facilityIdList ファシリティIDリスト
	 * @return 収集項目コードのリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 * @throws InvalidSetting 
	 */
	@GET
	@Path("/key")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetItemCodeList")
	@RestLog(action = LogAction.Get, target = LogTarget.Key, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetItemCodeListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItemCodeList (@Context Request request, @Context UriInfo uriInfo, 
			@ArrayTypeParam @QueryParam(value = "facilityIds") String facilityIds, @QueryParam(value = "size") String sizeStr)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.info("call getItemCodeList()");
		Integer size = RestCommonConverter.convertInteger(MessageConstant.SIZE.getMessage(), sizeStr, false, 1, null);

		List<String> facilityIdList = new ArrayList<>();
		if(facilityIds != null && !facilityIds.isEmpty()) {
			facilityIdList = Arrays.asList(facilityIds.split(","));
		}
		
		List<CollectKeyInfoPK> infoResList = new CollectControllerBean().getItemCode(facilityIdList);

		List<CollectKeyInfoResponseP1> dtoResList = new ArrayList<>();
		int recCount = 0;
		for(CollectKeyInfoPK info : infoResList){
			CollectKeyInfoResponseP1 dtoRec = new CollectKeyInfoResponseP1();
			RestBeanUtil.convertBeanNoInvalid(info, dtoRec);
			dtoRec.setItemNameTransrate(info.getItemName());
			dtoResList.add(dtoRec);
			recCount++;
			if (size != null && recCount >= size) {
				break;
			}
		}

		RestLanguageConverter.convertMessages(dtoResList);
		GetItemCodeListResponse dtoRes = new GetItemCodeListResponse();
		dtoRes.setKeyInfoList(dtoResList);
		dtoRes.setTotal(infoResList.size());
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 性能実績のDLデータを作成する。
	 * 指定したファシリティIDがスコープの場合は、配下の全てのノードに対して1ファイルずつCSVファイルを作成する。
	 * 本メソッドが終了した時点で、Hinemosマネージャ上にファイルが作成され、そのファイルパスのリストを返却する
	 *
	 * CollectRead権限が必要
	 *
	 *
	 * @param facilityidList
	 * @param summaryType
	 * @param item_codeList
	 * @param header
	 * @param archive
	 * @return ファイルパスのリスト(CreatePerfFileResponse)
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting 
	 */
	@POST
	@Path("/data_create")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CreatePerfFile")
	@RestLog(action = LogAction.Create, target = LogTarget.Data, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Collect, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CreatePerfFileResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPerfFile(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "createPerfFileBody", content = @Content(schema = @Schema(implementation = CreatePerfFileRequest.class))) String requestBody)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call createPerfFile()");

		CreatePerfFileRequest dtoReq = 
				RestObjectMapperWrapper.convertJsonToObject(requestBody, CreatePerfFileRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<CollectKeyInfoPK> collectKeyInfoList = new ArrayList<>();
		for(CollectKeyInfoRequest tmp : dtoReq.getCollectKeyInfoList()){
			CollectKeyInfoPK collectKeyInfoPK = new CollectKeyInfoPK();
			RestBeanUtil.convertBeanNoInvalid(tmp, collectKeyInfoPK);
			collectKeyInfoList.add(collectKeyInfoPK);
		}
		
		List<String> ret = new CollectControllerBean().createPerfFile(
					dtoReq.getFacilityNameMap(),
					dtoReq.getFacilityList() , 
					collectKeyInfoList,
					dtoReq.getSummaryType().getCode(), 
					dtoReq.getLocaleStr(), 
					dtoReq.getHeader(), 
					dtoReq.getDefaultDateStr()
					);

		CreatePerfFileResponse dtoRes = new CreatePerfFileResponse();
		dtoRes.setFileList(ret);

		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 性能データのファイルをDLする
	 *
	 * CollectRead権限が必要
	 *
	 * @param filepath
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/data_download/{fileName}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadPerfFile")
	@RestLog(action = LogAction.Download, target = LogTarget.Data, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Collect, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response downloadPerfFile(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "fileName") String fileName) 
			throws InvalidUserPass, InvalidRole, PerfFileNotFound, HinemosUnknown{
		m_log.info("call downloadPerfFile()");

		String exportDirectory = HinemosPropertyDefault.performance_export_dir.getStringValue();
		File file = new File(exportDirectory + fileName);
		if(!file.exists()) {
			String msg = String.format("file is not found : %s", exportDirectory + fileName);
			m_log.info(msg);
			throw new PerfFileNotFound(msg);
		}

		RestDownloadFile restDownloadFile = new RestDownloadFile(file, fileName);
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(restDownloadFile.getTempFile());

		return Response.ok(stream).header("Content-Disposition", "filename=\"" + restDownloadFile.getFileName() + "\"").build();

	}

	/**
	 * 収集項目コードを取得します。
	 * 
	 * @return 収集項目コードリスト
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/itemCodeMst")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCollectItemCodeMasterList")
	@RestLog(action = LogAction.Get, target = LogTarget.ItemCodeMst, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorItemCodeMstInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectItemCodeMasterList(@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getCollectItemCodeMasterList()");

		List<CollectorItemCodeMstData> collectorItemCodeMstData = 
				new PerformanceCollectMasterControllerBean().getCollectItemCodeMasterList();

		CollectorItemCodeMstInfoResponse dtoRes = new CollectorItemCodeMstInfoResponse();
		List<CollectorItemCodeMstInfoResponse> dtoResList = new ArrayList<>();

		for(CollectorItemCodeMstData tmp : collectorItemCodeMstData){
			CollectorItemCodeMstDataResponse collectorItemCodeMstDataResponse = new CollectorItemCodeMstDataResponse();
			RestBeanUtil.convertBeanNoInvalid(tmp, collectorItemCodeMstDataResponse);
			collectorItemCodeMstDataResponse.setItemNameTransrate(tmp.getItemName());
			collectorItemCodeMstDataResponse.setMeasureTransrate(tmp.getMeasure());
			dtoRes.getCollectorItemCodeMstData().add(collectorItemCodeMstDataResponse);
		}

		RestLanguageConverter.convertMessages(dtoRes);
		dtoResList.add(dtoRes);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 以下の条件に一致する収集値キーの一覧を取得します。
	 *　　オーナーロールIDが参照可能
	 *　　数値監視
	 *　　指定されたファシリティIDもしくはその配下のノードに一致する
	 *　※サイレント監視で使用する。
	 *　※権限はMonitorSettingのREAD権限
	 * 
	 * @param facilityId　ファシリティID
	 * @param ownerRoleId　オーナーロールID
	 * @return Map(名称, 収集値キーリスト)
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting 
	 */
	@GET
	@Path("/key_mapKeyItemName/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCollectKeyMapForAnalytics")
	@RestLog(action = LogAction.Get, target = LogTarget.Key, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectKeyMapForAnalyticsResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectKeyMapForAnalytics( 
			@PathParam(value = "facilityId") String facilityId, @Context Request request,@Context UriInfo uriInfo,
			@RestValidateObject(notNull=true) @QueryParam(value = "ownerRoleId")String ownerRoleId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getCollectKeyMapForAnalytics()");

		CommonValidator.validateNull(MessageConstant.OWNER_ROLE_ID.getMessage(), ownerRoleId);
		
		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		Map<String, CollectKeyInfo> collectKeyMapForAnalytics = new CollectControllerBean().getCollectKeyMapForAnalytics(facilityId, ownerRoleId);

		Map<String, CollectKeyInfoResponseP1> map = new ConcurrentHashMap<>();
		for(Map.Entry<String, CollectKeyInfo> entry:collectKeyMapForAnalytics.entrySet()){
			CollectKeyInfoResponseP1 collectKeyInfoResponseP1 = new CollectKeyInfoResponseP1();
			collectKeyInfoResponseP1.setDisplayName(entry.getValue().getDisplayName());
			collectKeyInfoResponseP1.setFacilityId(entry.getValue().getFacilityid());
			collectKeyInfoResponseP1.setItemName(entry.getValue().getItemName());
			collectKeyInfoResponseP1.setItemNameTransrate(entry.getValue().getItemName());
			collectKeyInfoResponseP1.setMonitorId(entry.getValue().getMonitorId());
			map.put(entry.getKey(),collectKeyInfoResponseP1);
		}

		CollectKeyMapForAnalyticsResponse dtoRes = new CollectKeyMapForAnalyticsResponse();
		dtoRes.setCollectKeyMapForAnalytics(map);
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 回帰方程式の係数を取得する(将来予測監視)
	 *    
	 * @param monitorInfo	監視設定
	 * @param facilityId	ファシリティID
	 * @param displayName	DisplayName
	 * @param itemName		ItemName
	 * @return 回帰方程式の係数
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	@POST
	@Path("/dataCache_coefficients_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCoefficients")
	@RestLog(action = LogAction.Get, target = LogTarget.DataCache, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetCoefficientsResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCoefficients(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getCoefficientsBody", 
			content = @Content(schema = @Schema(implementation = GetCoefficientsRequest.class))) 
			String requestBody) throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown{
		m_log.info("call getCoefficients()");

		GetCoefficientsRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetCoefficientsRequest.class);

		GetCoefficientsResponse dtoRes = new GetCoefficientsResponse();
		dtoRes.setCoefficients(MonitorCollectDataCache.getCoefficients(dtoReq.getMonitorId(), dtoReq.getFacilityId(), dtoReq.getDisplayName(), dtoReq.getItemName()));
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 収集項目コードの一覧を取得します
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return 収集項目IDをキーとしCollectorItemTreeItemが格納されているHashMap
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	@GET
	@Path("/itemCodeMst_map")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetItemCodeMap")
	@RestLog(action = LogAction.Get, target = LogTarget.ItemCodeMst, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorItemCodeMstMapResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItemCodeMap(@Context Request request, @Context UriInfo uriInfo) 
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getItemCodeMap");

		CollectorItemCodeMstMapResponse dtoRes = new CollectorItemCodeMstMapResponse();
		Map<String, CollectorItemTreeItemResponse>  itemCodeMap = new ConcurrentHashMap<String, CollectorItemTreeItemResponse>();

		Map<String, CollectorItemTreeItem> collectorItemTreeItemMap = new PerformanceControllerBean().getItemCodeMap();
		for(Map.Entry<String, CollectorItemTreeItem> tmp : collectorItemTreeItemMap.entrySet()){
			CollectorItemTreeItemResponse collectorItemTreeItemResponse = new CollectorItemTreeItemResponse();
			RestBeanUtil.convertBeanNoInvalid(tmp.getValue(), collectorItemTreeItemResponse);
			itemCodeMap.put(tmp.getKey(), collectorItemTreeItemResponse);
		}

		dtoRes.setItemCodeMap(itemCodeMap);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定のファシリティで収集可能な項目のリストを返します
	 * デバイス別の収集項目があり、ノード毎に登録されているデバイス情報が異なるため、
	 * 取得可能な収集項目はファシリティ毎に異なる。
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @param facilityId ファシリティID
	 * @return 指定のファシリティで収集可能な項目のリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/itemCodeMst_availableItem")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAvailableCollectorItemList")
	@RestLog(action = LogAction.Get, target = LogTarget.ItemCodeMst, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorItemCodeMstResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAvailableCollectorItemList(@Context Request request, @Context UriInfo uriInfo,
			@QueryParam(value = "facilityId") String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getAvailableCollectorItemList");

		CollectorItemCodeMstResponseP1 dtoRes = new CollectorItemCodeMstResponseP1();

		List<CollectorItemInfoResponse> collectorItemInfoResponselist = new ArrayList<>();
		List<CollectorItemInfo> collectorItemInfoList = new PerformanceControllerBean().getAvailableCollectorItemList(facilityId);

		for(CollectorItemInfo tmp : collectorItemInfoList){
			CollectorItemInfoResponse collectorItemInfoResponse = new CollectorItemInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(tmp,collectorItemInfoResponse);
			collectorItemInfoResponselist.add(collectorItemInfoResponse);
		}
		dtoRes.setAvailableCollectorItemList(collectorItemInfoResponselist);
		
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログ件数監視の過去分集計を行う。
	 *
	 * MonitorSettingModify権限が必要
	 *
	 * @param monitorId 監視設定ID
	 * @param startDate 収集開始日時
	 * @throws MonitorNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	@POST
	@Path("/dataRaw_logCount")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RunSummaryLogcount")
	@RestLog(action = LogAction.LogCount, target = LogTarget.DataRaw, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response runSummaryLogcount(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "runSummaryLogcountBody", 
			content = @Content(schema = @Schema(implementation = RunSummaryLogcountRequest.class))) 
			String requestBody) 
					throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call runSummaryLogcount");

		RunSummaryLogcountRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				RunSummaryLogcountRequest.class);
		dtoReq.correlationCheck();

		String monitorId =  dtoReq.getMonitorId();
		Long startDate = RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getStartDate(), MessageConstant.COLLECT_START_DATETIME .getMessage());
		new MonitorSettingControllerBean().runSummaryLogcount(monitorId, startDate);
		
		return Response.status(Status.OK).build();
	}

	/**
	 * 文字列収集情報を検索する。
	 * 
	 * @param query
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws HinemosDbTimeout
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/dataString_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "QueryCollectStringData")
	@RestLog(action = LogAction.Get, target = LogTarget.DataString, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StringQueryResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response queryCollectStringData(
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "queryCollectStringDataBody", 
			content = @Content(schema = @Schema(implementation = QueryCollectStringDataRequest.class))) 
			String requestBody)
					throws InvalidUserPass, InvalidRole, HinemosUnknown, HinemosDbTimeout, InvalidSetting {
		m_log.info("call queryCollectStringData");
		m_log.debug("requestBody=" + requestBody);
		
		QueryCollectStringDataRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				QueryCollectStringDataRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		StringQueryInfo queryInfo = new StringQueryInfo();
		RestBeanUtil.convertBeanNoInvalid(dtoReq, queryInfo);
		if (dtoReq.getOperator() == QueryCollectStringDataRequest.OperatorEnum.AND ) {
			queryInfo.setOperator(StringQueryInfo.Operator.AND);
		} else {
			queryInfo.setOperator(StringQueryInfo.Operator.OR);
		}
		m_log.debug("queryInfo=" + queryInfo);
		
		StringQueryResultResponse dtoRes = new StringQueryResultResponse();
		StringQueryResult result = new HubControllerBean().queryCollectStringData(queryInfo);
		dtoRes.setCount(result.getCount());
		dtoRes.setOffset(result.getOffset());
		dtoRes.setSize(result.getSize());
		dtoRes.setTime(result.getTime());

		//個別に格納 StringDataResponse
		List<StringDataResponse> stringDataResponseList = new ArrayList<StringDataResponse>();
		//個別に格納 TagResponse
		if(result.getDataList() != null){
			for(StringData tmpStringData:result.getDataList()){
				StringDataResponse stringDataResponse = new StringDataResponse();
				stringDataResponse.setCollectId(tmpStringData.getPrimaryKey().getCollectId());
				stringDataResponse.setDataId(tmpStringData.getPrimaryKey().getDataId());
				RestBeanUtil.convertBeanNoInvalid(tmpStringData, stringDataResponse);
				if(tmpStringData.getTagList() != null){
					List<TagResponse> tagResponseList = new ArrayList<TagResponse>();
					for(Tag tmpTag: tmpStringData.getTagList()){
						TagResponse tagResponse = new TagResponse();
						RestBeanUtil.convertBeanNoInvalid(tmpTag, tagResponse);
						tagResponseList.add(tagResponse);
					}
					stringDataResponse.setTagList(tagResponseList);
				}

				stringDataResponseList.add(stringDataResponse);
			}
		}
		dtoRes.setDataList(stringDataResponseList);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * バイナリ収集情報を検索する.
	 * 
	 * @param query
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws HinemosDbTimeout
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/dataBinary_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "QueryCollectBinaryData")
	@RestLog(action = LogAction.Get, target = LogTarget.DataBinary, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BinaryQueryResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response queryCollectBinaryData(
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "queryCollectBinaryDataBody", 
			content = @Content(schema = @Schema(implementation = QueryCollectBinaryDataRequest.class))) 
			String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, HinemosDbTimeout, InvalidSetting {
		m_log.info("call queryCollectBinaryData");

		QueryCollectBinaryDataRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				QueryCollectBinaryDataRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		BinaryQueryInfo infoReq = new BinaryQueryInfo();
		RestBeanUtil.convertBeanNoInvalid(dtoReq, infoReq);
		if (dtoReq.getOperator() == QueryCollectBinaryDataRequest.OperatorEnum.AND ) {
			infoReq.setOperator(StringQueryInfo.Operator.AND);
		} else {
			infoReq.setOperator(StringQueryInfo.Operator.OR);
		}
		
		BinaryQueryResultResponse dtoRes = new BinaryQueryResultResponse();
		StringQueryResult result = new BinaryHubController().queryCollectBinaryData(infoReq);
		dtoRes.setCount(result.getCount());
		dtoRes.setOffset(result.getOffset());
		dtoRes.setSize(result.getSize());
		dtoRes.setTime(result.getTime());

		// BinaryDataResponse、TagResponseは個別に格納
		List<BinaryDataResponse> binaryDataResponseList = new ArrayList<>();
		if(result.getDataList() != null){
			for(StringData tmpStringData:result.getDataList()){
				BinaryDataResponse binaryDataResponse = new BinaryDataResponse();
				binaryDataResponse.setCollectId(tmpStringData.getPrimaryKey().getCollectId());
				binaryDataResponse.setDataId(tmpStringData.getPrimaryKey().getDataId());
				RestBeanUtil.convertBeanNoInvalid(tmpStringData, binaryDataResponse);
				if(tmpStringData.getTagList() != null){
					List<TagResponse> tagResponseList = new ArrayList<TagResponse>();
					for(Tag tmpTag: tmpStringData.getTagList()){
						TagResponse tagResponse = new TagResponse();
						RestBeanUtil.convertBeanNoInvalid(tmpTag, tagResponse);
						tagResponseList.add(tagResponse);
					}
					binaryDataResponse.setTagList(tagResponseList);
				}

				binaryDataResponseList.add(binaryDataResponse);
			}
		}

		dtoRes.setDataList(binaryDataResponseList);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 収集項目マスタデータを一括で登録します。
	 * @param collectMasterInfo
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting 
	 */
	@POST
	@Path("/master")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCollectMaster")
	@RestLog(action = LogAction.Add, target = LogTarget.Master, type = LogType.UPDATE )
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectMasterInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCollectMaster(
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addCollectMasterBody", 
			content = @Content(schema = @Schema(implementation = AddCollectMasterRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addCollectMaster");

		AddCollectMasterRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddCollectMasterRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CollectMasterInfo collectMasterInfo = new CollectMasterInfo();
		RestBeanUtil.convertBean(dtoReq, collectMasterInfo);

		CollectMasterInfo infoRes = new PerformanceCollectMasterControllerBean().addCollectMaster(collectMasterInfo);
		
		CollectMasterInfoResponse dtoRes = new CollectMasterInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 収集項目のマスタ情報を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@DELETE
	@Path("/master")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCollectMasterAll")
	@RestLog(action = LogAction.Delete, target = LogTarget.Master, type = LogType.UPDATE )
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
	@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectMasterInfoResponse.class)), description = "response"),
	@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
	@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
	@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") }) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteCollectMasterAll(@Context Request request, @Context UriInfo uriInfo) 
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call deleteCollectMasterAll");

		CollectMasterInfo infoRes = new PerformanceCollectMasterControllerBean().deleteCollectMasterAll();
		CollectMasterInfoResponse dtoRes = new CollectMasterInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 収集マスタ情報を一括で取得します。
	 * 
	 * @return 収集マスタ情報
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/master")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCollectMasterInfo")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE )
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectMasterInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectMasterInfo(@Context Request request, @Context UriInfo uriInfo) 
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getCollectMasterInfo");

		CollectMasterInfo collectMasterInfo = new PerformanceCollectMasterControllerBean().getCollectMasterInfo();
		CollectMasterInfoResponse dtoRes = new CollectMasterInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(collectMasterInfo, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * DB取得したバイナリファイルをクライアント送信用に返却.
	 * 
	 * @param recordTime 表示レコード時刻
	 * @param recordKey レコードキー
	 * @throws HinemosException
	 */
	@POST
	@Path("/dataBinary_download")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadBinaryRecord")
	@RestLog(action = LogAction.Download, target = LogTarget.DataBinary, type = LogType.UPDATE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadBinaryRecord(
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "downloadBinaryRecordBody", 
			content = @Content(schema = @Schema(implementation = DownloadBinaryRecordRequest.class))) 
			String requestBody)
					throws BinaryRecordNotFound, HinemosDbTimeout, HinemosUnknown, InvalidRole, InvalidSetting, InvalidUserPass {
		m_log.info("call downloadBinaryRecord");

		DownloadBinaryRecordRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				DownloadBinaryRecordRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CollectStringDataPK primaryKey = new CollectStringDataPK();
		primaryKey.setCollectId(dtoReq.getRecords().get(0).getCollectId());
		primaryKey.setDataId(dtoReq.getRecords().get(0).getDataId());

		BinaryQueryInfo infoReq = new BinaryQueryInfo();
		RestBeanUtil.convertBeanNoInvalid(dtoReq.getQueryCollectBinaryDataRequest().get(0), infoReq);
		
		RestDownloadFile restDownloadFile = new BinaryControllerBean().downloadBinaryRecord(infoReq,
					primaryKey,dtoReq.getFilename(), createTempDir());
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(restDownloadFile.getTempFile(), 
				restDownloadFile.getTempFile().getParentFile());

		return Response.status(Status.OK).entity(stream)
				.header("Content-Disposition", "filename=" + restDownloadFile.getFileName()).build();
	}

	/**
	 * DB取得したバイナリファイルをzipファイルにまとめてクライアント送信用に返却.
	 * 
	 * @param recordTime 表示レコード時刻
	 * @param recordKey レコードキー
	 * @return
	 * @throws HinemosException
	 */
	@POST
	@Path("/dataBinaries_zip_download")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadBinaryRecords")
	@RestLog(action = LogAction.Download, target = LogTarget.DataBinary, type = LogType.UPDATE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadBinaryRecords(
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "downloadBinaryRecordsBody", 
			content = @Content(schema = @Schema(implementation = DownloadBinaryRecordsRequest.class))) 
			String requestBody)
					throws BinaryRecordNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosDbTimeout, HinemosUnknown {
		m_log.info("call downloadBinaryRecords");
		
		DownloadBinaryRecordsRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				DownloadBinaryRecordsRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		if (dtoReq.getFilename() == null || dtoReq.getFilename().length() == 0) {
			SimpleDateFormat sdf = new SimpleDateFormat(RestConstant.DOWNLOAD_FILE_TIMESTAMP);
			dtoReq.setFilename(RestConstant.DOWNLOAD_FILE_NAME_PREFIX_BINARY
					+ sdf.format(HinemosTime.currentTimeMillis()) + RestConstant.DOWNLOAD_FILE_ZIP_EXTENSION);
		}

		// ファイル統合用にダウンロード条件リストの順序をレコードキーで整列.
		Collections.sort(dtoReq.getRecords(), new Comparator<DownloadBinaryRecordsKeyRequest>() {
			@Override
			public int compare(DownloadBinaryRecordsKeyRequest info1, DownloadBinaryRecordsKeyRequest info2) {
				// ファイル種別で比較した結果を返却.
				return info1.getRecordKey().compareTo(info2.getRecordKey());
			}
		});

		ArrayList<String> intoZipList = new ArrayList<String>();
		BinaryControllerBean controller = new BinaryControllerBean();
		RestDownloadFile restDownloadFile = null;
		String tempDir = createTempDir();
		for (int countRecords = 0; countRecords < dtoReq.getRecords().size(); countRecords++) {
			// マネージャーに一時ファイルを出力.
			CollectStringDataPK pk = new CollectStringDataPK();
			pk.setCollectId( dtoReq.getRecords().get(countRecords).getCollectId() );
			pk.setDataId( dtoReq.getRecords().get(countRecords).getDataId() );

			BinaryQueryInfo queryInfo = new BinaryQueryInfo();
			RestBeanUtil.convertBeanNoInvalid(dtoReq.getQueryCollectBinaryDataRequest().get(countRecords), queryInfo);
			intoZipList = controller.createTmpRecords(queryInfo, pk, intoZipList, tempDir);
		}
		restDownloadFile = controller.createZipHandler(intoZipList, tempDir, dtoReq.getFilename());

		// ダウンロード処理
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(restDownloadFile.getTempFile(),
				restDownloadFile.getTempFile().getParentFile());

		return Response.status(Status.OK).entity(stream)
				.header("Content-Disposition", "filename=" + restDownloadFile.getFileName()).build();
	}
	
	// ダウンロード用の一時ディレクトリを作成する
	private String createTempDir() throws HinemosUnknown {
		String tempDir = null;
		try {
			tempDir = RestTempFileUtil.createTempDirectory(RestTempFileType.BINARY).toString();
		} catch (IOException e) {
			m_log.warn("failed to create temporary dir. " + e.getMessage());
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return tempDir;
	}
}