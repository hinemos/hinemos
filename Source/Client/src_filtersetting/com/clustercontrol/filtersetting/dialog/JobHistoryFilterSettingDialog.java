/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.dialog;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.AddJobHistoryFilterSettingRequest;
import org.openapitools.client.model.ModifyJobHistoryFilterSettingRequest;

import com.clustercontrol.fault.FilterSettingDuplicate;
import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.filtersetting.bean.FilterSettingEditMode;
import com.clustercontrol.filtersetting.bean.JobHistoryFilterContext;
import com.clustercontrol.filtersetting.composite.FilterSettingHeaderComposite;
import com.clustercontrol.filtersetting.composite.JobHistoryFilterComposite;
import com.clustercontrol.filtersetting.util.GenericFilterSettingRequest;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.filtersetting.util.FilterSettingRestClientWrapper;
import com.clustercontrol.filtersetting.util.JobHistoryFilterHelper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;

/**
 * ジョブ実行履歴のフィルタ設定ダイアログです。
 */
public class JobHistoryFilterSettingDialog extends FilterSettingDialog {

	private JobHistoryFilterContext context;

	private JobHistoryFilterComposite cmpFilter;

	/**
	 * コンストラクタ。
	 * 
	 * @param parent 親ウィンドウ。
	 * @param mode 編集モード。
	 * @param managerName マネージャ名の初期値。
	 * @param common 共通/ユーザ選択の初期値。
	 * @param filterSetting 設定内容の初期値。
	 */
	public JobHistoryFilterSettingDialog(
			Shell parent,
			FilterSettingEditMode mode,
			String managerName,
			boolean common,
			GenericFilterSettingResponse filterSetting) {
		super(parent, mode, managerName, common, filterSetting);

		context = new JobHistoryFilterContext(
				JobHistoryFilterHelper.convertToRequest(filterSetting.getJobHistoryFilter()),
				managerName);
	}

	@Override
	protected String getTitle() {
		return Messages.getString("fltset.dialog.title.job_history");
	}

	@Override
	protected Composite createFilterComposite(Composite parent, FilterSettingHeaderComposite header) {
		cmpFilter = new JobHistoryFilterComposite(parent, context);
		cmpFilter.readContext();
		return cmpFilter;
	}

	@Override
	protected void applyFilterConditionsTo(GenericFilterSettingRequest filterSettingReq) {
		cmpFilter.writeContext();
		filterSettingReq.setJobHistoryFilter(context.getFilter());
	}

	@Override
	protected void actionAdd(String managerName, boolean common, GenericFilterSettingRequest filterSettingReq)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate, RestConnectFailed, HinemosUnknown {

		AddJobHistoryFilterSettingRequest jobHistoryReq = GenericFilterSettingRequest.toAddJobHistoryRequest(filterSettingReq);
		FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
		if (common) {
			rest.addCommonJobHistoryFilterSetting(jobHistoryReq);
		} else {
			jobHistoryReq.setOwnerUserId(RestConnectManager.getLoginUserId(managerName));
			rest.addUserJobHistoryFilterSetting(jobHistoryReq);
		}
	}

	@Override
	protected void actionModify(String managerName, boolean common, String ownerUserId, GenericFilterSettingRequest filterSettingReq)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound, RestConnectFailed, HinemosUnknown {

		ModifyJobHistoryFilterSettingRequest jobHistoryReq = GenericFilterSettingRequest.toModifyJobHistoryRequest(filterSettingReq);
		FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
		if (common) {
			rest.modifyCommonJobHistoryFilterSetting(filterSettingReq.getFilterId(), jobHistoryReq);
		} else {
			jobHistoryReq.setOwnerUserId(ownerUserId);
			rest.modifyUserJobHistoryFilterSetting(filterSettingReq.getFilterId(), jobHistoryReq);
		}
	}

}
