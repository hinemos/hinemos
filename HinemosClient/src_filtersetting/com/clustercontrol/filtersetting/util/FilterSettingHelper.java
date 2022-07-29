/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.EventFilterSettingResponse;
import org.openapitools.client.model.FilterSettingSummaryResponse;
import org.openapitools.client.model.JobHistoryFilterSettingResponse;
import org.openapitools.client.model.StatusFilterSettingResponse;

import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ManagerTag;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * フィルタ設定に関する共通の(特定フィルタ分類に依らない)ヘルパーです。
 */
public class FilterSettingHelper {
	private static final Log logger = LogFactory.getLog(FilterSettingHelper.class);

	/**
	 * ResponseDTO から RequestDTO へ変換して返します。
	 */
	public static GenericFilterSettingRequest convertToRequest(GenericFilterSettingResponse res) {
		GenericFilterSettingRequest req = new GenericFilterSettingRequest();
		try {
			RestClientBeanUtil.convertBean(res, req);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert CommonFilterSettingResponse to CommonFilterSettingRequest.\n"
					+ "res=" + res.toString() + "\nreq=" + req.toString());
		}
		return req;
	}

	/**
	 * フィルタ設定のオブジェクト権限用IDを、クライアント表示向けの文字列に変換して返します。
	 */
	// マネージャ側でフィルタ設定のobjectIdのフォーマットが変更になった場合には、併せて対応が必要。
	// - HinemosCommonへobjectId関連のロジックを移動して共有することも考慮したが、
	//   今後のメンテナンスにおいてコンポーネント内に閉じた修正であってもHinemosCommonの更新になるのは避けたかった。
	public static String formatObjectIdForDisplay(String objectId) {
		try {
			FilterCategoryEnum category = FilterCategoryEnum.fromCode(Integer.parseInt(objectId.substring(0, 4), 10));
			String filterId = objectId.substring(4); 
			
			return filterId
					+ " ("
					+ HinemosMessage.replace(category.getLabel())
					+ ")";

		} catch (Exception e) {
			logger.warn("formatObjectIdForDisplay: " + e.getClass().getName() + "," + e.getMessage());
			// 表示上のことなので何か問題があってもそれらしい文字列(現在はオブジェクトIDそのまま)を返すに留める
			return objectId;
		}
	}

	/**
	 * フィルタ設定の概要情報をもとに、フィルタ設定の詳細情報をマネージャから取得します。<br/>
	 * 本メソッドにより、共通フィルタ設定/ユーザフィルタ設定の違いによるAPIの呼び分けなどを省力化できます。
	 */
	public static GenericFilterSettingResponse fetchFilterSetting(ManagerTag<FilterSettingSummaryResponse> summary)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound, RestConnectFailed, HinemosUnknown {
		GenericFilterSettingResponse fs = null;
		FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(summary.managerName);
		FilterCategoryEnum cat = FilterCategoryEnum.valueOf(summary.data.getFilterCategory().name());
		if (summary.data.getCommon().booleanValue()) {
			switch (cat) {
			case EVENT:
				EventFilterSettingResponse er = rest.getCommonEventFilterSetting(summary.data.getFilterId());
				fs = GenericFilterSettingResponse.fromEventResponse(er);
				break;
			case STATUS:
				StatusFilterSettingResponse sr = rest.getCommonStatusFilterSetting(summary.data.getFilterId());
				fs = GenericFilterSettingResponse.fromStatusResponse(sr);
				break;
			case JOB_HISTORY:
				JobHistoryFilterSettingResponse jr = rest.getCommonJobHistoryFilterSetting(summary.data.getFilterId());
				fs = GenericFilterSettingResponse.fromJobHistoryResponse(jr);
				break;
			}
		} else {
			switch (cat) {
			case EVENT:
				EventFilterSettingResponse er = rest.getUserEventFilterSetting(summary.data.getFilterId(), summary.data.getOwnerUserId());
				fs = GenericFilterSettingResponse.fromEventResponse(er);
				break;
			case STATUS:
				StatusFilterSettingResponse sr = rest.getUserStatusFilterSetting(summary.data.getFilterId(), summary.data.getOwnerUserId());
				fs = GenericFilterSettingResponse.fromStatusResponse(sr);
				break;
			case JOB_HISTORY:
				JobHistoryFilterSettingResponse jr = rest.getUserJobHistoryFilterSetting(summary.data.getFilterId(), summary.data.getOwnerUserId());
				fs = GenericFilterSettingResponse.fromJobHistoryResponse(jr);
				break;
			}
		}
		return fs;
	}

}
