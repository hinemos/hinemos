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
import com.clustercontrol.filtersetting.util.StatusFilterHelper;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.Messages;

/**
 * ステータス通知結果のフィルタ設定一覧ダイアログです。
 */
public class StatusFilterSettingListDialog extends FilterSettingListDialog {

	public StatusFilterSettingListDialog(Shell parent) {
		super(parent, FilterCategoryEnum.STATUS);
	}

	@Override
	protected String getTitle() {
		return Messages.getString("fltset.list_dialog.title.status");
	}

	@Override
	protected FilterSettingDialog createSettingDialogForAdd(String managerName, boolean common) {
		GenericFilterSettingResponse initial = new GenericFilterSettingResponse();
		initial.setStatusFilter(StatusFilterHelper.convertToResponse(StatusFilterHelper.createDefaultFilter(null)));

		return new StatusFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.ADD,
				managerName,
				common,
				initial);
	}

	@Override
	protected FilterSettingDialog createSettingDialogForModify(String managerName, boolean common, GenericFilterSettingResponse filterSetting) {
		return new StatusFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.MODIFY,
				managerName,
				common,
				filterSetting);
	}

	@Override
	protected FilterSettingDialog createSettingDialogForCopy(String managerName, boolean common, GenericFilterSettingResponse filterSetting) {
		return new StatusFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.ADD,
				managerName,
				common,
				filterSetting);
	}

}
