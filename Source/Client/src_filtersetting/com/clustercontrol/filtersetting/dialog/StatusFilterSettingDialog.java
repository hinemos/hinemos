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
import org.openapitools.client.model.AddStatusFilterSettingRequest;
import org.openapitools.client.model.ModifyStatusFilterSettingRequest;

import com.clustercontrol.fault.FilterSettingDuplicate;
import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.filtersetting.bean.FilterSettingEditMode;
import com.clustercontrol.filtersetting.bean.StatusFilterContext;
import com.clustercontrol.filtersetting.composite.FilterSettingHeaderComposite;
import com.clustercontrol.filtersetting.composite.FilterSettingHeaderComposite.CustomEventListener;
import com.clustercontrol.filtersetting.composite.StatusFilterComposite;
import com.clustercontrol.filtersetting.util.GenericFilterSettingRequest;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.filtersetting.util.FilterSettingRestClientWrapper;
import com.clustercontrol.filtersetting.util.StatusFilterHelper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;

/**
 * ステータス通知結果のフィルタ設定ダイアログです。
 */
public class StatusFilterSettingDialog extends FilterSettingDialog {

	private StatusFilterContext context;

	private StatusFilterComposite cmpFilter;

	/**
	 * コンストラクタ。
	 * 
	 * @param parent 親ウィンドウ。
	 * @param mode 編集モード。
	 * @param managerName マネージャ名の初期値。
	 * @param common 共通/ユーザ選択の初期値。
	 * @param filterSetting 設定内容の初期値。
	 */
	public StatusFilterSettingDialog(
			Shell parent,
			FilterSettingEditMode mode,
			String managerName,
			boolean common,
			GenericFilterSettingResponse filterSetting) {
		super(parent, mode, managerName, common, filterSetting);

		context = new StatusFilterContext(
				StatusFilterHelper.convertToRequest(filterSetting.getStatusFilter()),
				managerName,
				"");
	}

	@Override
	protected String getTitle() {
		return Messages.getString("fltset.dialog.title.status");
	}

	@Override
	protected Composite createFilterComposite(Composite parent, FilterSettingHeaderComposite header) {
		cmpFilter = new StatusFilterComposite(parent, context);

		// フィルタ設定ヘッダ(FilterSettingHeaderComposite)の内容をフィルタ条件(StatusFilterComposite)へ反映させる。
		cmpFilter.setOwner(header.getManagerName(), header.isCommonFilter(), header.getOwnerRoleId());

		// setOwnerの効果を取り込むため、表示更新はこの位置
		cmpFilter.readContext();

		// これ以後、フィルタ設定ヘッダでの変更が自動でフィルタ条件へ連携されるようにする。
		header.addCustomEventListener(new CustomEventListener() {
			@Override
			public void onOwnerChanged(String managerName, boolean common, String ownerRoleId) {
				cmpFilter.setOwner(managerName, common, ownerRoleId);
			}
		});

		return cmpFilter;
	}

	@Override
	protected void applyFilterConditionsTo(GenericFilterSettingRequest filterSettingReq) {
		cmpFilter.writeContext();
		filterSettingReq.setStatusFilter(context.getFilter());
	}

	@Override
	protected void actionAdd(String managerName, boolean common, GenericFilterSettingRequest filterSettingReq)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate, RestConnectFailed, HinemosUnknown {

		AddStatusFilterSettingRequest statusReq = GenericFilterSettingRequest.toAddStatusRequest(filterSettingReq);
		FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
		if (common) {
			rest.addCommonStatusFilterSetting(statusReq);
		} else {
			statusReq.setOwnerUserId(RestConnectManager.getLoginUserId(managerName));
			rest.addUserStatusFilterSetting(statusReq);
		}
	}

	@Override
	protected void actionModify(String managerName, boolean common, String ownerUserId, GenericFilterSettingRequest filterSettingReq)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound, RestConnectFailed, HinemosUnknown {

		ModifyStatusFilterSettingRequest statusReq = GenericFilterSettingRequest.toModifyStatusRequest(filterSettingReq);
		FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
		if (common) {
			rest.modifyCommonStatusFilterSetting(filterSettingReq.getFilterId(), statusReq);
		} else {
			statusReq.setOwnerUserId(ownerUserId);
			rest.modifyUserStatusFilterSetting(filterSettingReq.getFilterId(), statusReq);
		}
	}

}
