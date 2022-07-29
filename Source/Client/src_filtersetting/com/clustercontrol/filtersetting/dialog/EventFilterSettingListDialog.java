/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.dialog;

import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.filtersetting.bean.FilterSettingEditMode;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.filtersetting.util.EventFilterHelper;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.Messages;

/**
 * イベント履歴のフィルタ設定一覧ダイアログです。
 */
public class EventFilterSettingListDialog extends FilterSettingListDialog {

	private MultiManagerEventDisplaySettingInfo eventDspSetting;

	public EventFilterSettingListDialog(Shell parent, MultiManagerEventDisplaySettingInfo eventDspSetting) {
		super(parent, FilterCategoryEnum.EVENT);
		this.eventDspSetting = eventDspSetting;
	}

	@Override
	protected String getTitle() {
		return Messages.getString("fltset.list_dialog.title.event");
	}

	@Override
	protected FilterSettingDialog createSettingDialogForAdd(String managerName, boolean common) {
		GenericFilterSettingResponse initial = new GenericFilterSettingResponse();
		initial.setEventFilter(EventFilterHelper.convertToResponse(EventFilterHelper.createDefaultFilter(null)));

		return new EventFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.ADD,
				managerName,
				common,
				initial,
				eventDspSetting);
	}

	@Override
	protected FilterSettingDialog createSettingDialogForModify(String managerName, boolean common, GenericFilterSettingResponse filterSetting) {
		return new EventFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.MODIFY,
				managerName,
				common,
				filterSetting,
				eventDspSetting);
	}

	@Override
	protected FilterSettingDialog createSettingDialogForCopy(String managerName, boolean common, GenericFilterSettingResponse filterSetting) {
		return new EventFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.ADD,
				managerName,
				common,
				filterSetting,
				eventDspSetting);
	}

}
