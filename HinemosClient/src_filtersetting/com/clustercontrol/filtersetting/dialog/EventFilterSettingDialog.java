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
import org.openapitools.client.model.AddEventFilterSettingRequest;
import org.openapitools.client.model.ModifyEventFilterSettingRequest;

import com.clustercontrol.fault.FilterSettingDuplicate;
import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.filtersetting.bean.EventFilterContext;
import com.clustercontrol.filtersetting.bean.FilterSettingEditMode;
import com.clustercontrol.filtersetting.composite.EventFilterComposite;
import com.clustercontrol.filtersetting.composite.FilterSettingHeaderComposite;
import com.clustercontrol.filtersetting.composite.FilterSettingHeaderComposite.CustomEventListener;
import com.clustercontrol.filtersetting.util.GenericFilterSettingRequest;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.filtersetting.util.EventFilterHelper;
import com.clustercontrol.filtersetting.util.FilterSettingRestClientWrapper;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;

/**
 * イベント履歴のフィルタ設定ダイアログです。
 */
public class EventFilterSettingDialog extends FilterSettingDialog {

	private EventFilterContext context;

	private EventFilterComposite cmpFilter;

	/**
	 * コンストラクタ。
	 * 
	 * @param parent 親ウィンドウ。
	 * @param mode 編集モード。
	 * @param managerName マネージャ名の初期値。
	 * @param common 共通/ユーザ選択の初期値。
	 * @param filterSetting 設定内容の初期値。
	 * @param eventDspSetting 拡張項目の表示設定。
	 */
	public EventFilterSettingDialog(
			Shell parent,
			FilterSettingEditMode mode,
			String managerName,
			boolean common,
			GenericFilterSettingResponse filterSetting,
			MultiManagerEventDisplaySettingInfo eventDspSetting) {
		super(parent, mode, managerName, common, filterSetting);

		context = new EventFilterContext(
				EventFilterHelper.convertToRequest(filterSetting.getEventFilter()),
				managerName,
				"",
				eventDspSetting);
	}

	@Override
	protected String getTitle() {
		return Messages.getString("fltset.dialog.title.event");
	}

	@Override
	protected Composite createFilterComposite(Composite parent, FilterSettingHeaderComposite header) {
		cmpFilter = new EventFilterComposite(parent, context);

		// フィルタ設定ヘッダ(FilterSettingHeaderComposite)の内容をフィルタ条件(EventFilterComposite)へ反映させる。
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
		filterSettingReq.setEventFilter(context.getFilter());
	}

	@Override
	protected void actionAdd(String managerName, boolean common, GenericFilterSettingRequest filterSettingReq)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate, RestConnectFailed, HinemosUnknown {

		AddEventFilterSettingRequest eventReq = GenericFilterSettingRequest.toAddEventRequest(filterSettingReq);
		FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
		if (common) {
			rest.addCommonEventFilterSetting(eventReq);
		} else {
			eventReq.setOwnerUserId(RestConnectManager.getLoginUserId(managerName));
			rest.addUserEventFilterSetting(eventReq);
		}
	}

	@Override
	protected void actionModify(String managerName, boolean common, String ownerUserId, GenericFilterSettingRequest filterSettingReq)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound, RestConnectFailed, HinemosUnknown {

		ModifyEventFilterSettingRequest eventReq = GenericFilterSettingRequest.toModifyEventRequest(filterSettingReq);
		FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
		if (common) {
			rest.modifyCommonEventFilterSetting(filterSettingReq.getFilterId(), eventReq);
		} else {
			eventReq.setOwnerUserId(ownerUserId);
			rest.modifyUserEventFilterSetting(filterSettingReq.getFilterId(), eventReq);
		}
	}

}
