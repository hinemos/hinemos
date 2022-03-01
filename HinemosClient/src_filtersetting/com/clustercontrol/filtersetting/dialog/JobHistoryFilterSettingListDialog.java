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
import com.clustercontrol.filtersetting.util.JobHistoryFilterHelper;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.Messages;

/**
 * ジョブ実行履歴のフィルタ設定一覧ダイアログです。
 */
public class JobHistoryFilterSettingListDialog extends FilterSettingListDialog {

	public JobHistoryFilterSettingListDialog(Shell parent) {
		super(parent, FilterCategoryEnum.JOB_HISTORY);
	}

	@Override
	protected String getTitle() {
		return Messages.getString("fltset.list_dialog.title.job_history");
	}

	@Override
	protected FilterSettingDialog createSettingDialogForAdd(String managerName, boolean common) {
		GenericFilterSettingResponse initial = new GenericFilterSettingResponse();
		initial.setJobHistoryFilter(JobHistoryFilterHelper.convertToResponse(JobHistoryFilterHelper.createDefaultFilter()));

		return new JobHistoryFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.ADD,
				managerName,
				common,
				initial);
	}

	@Override
	protected FilterSettingDialog createSettingDialogForModify(String managerName, boolean common, GenericFilterSettingResponse filterSetting) {
		return new JobHistoryFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.MODIFY,
				managerName,
				common,
				filterSetting);
	}

	@Override
	protected FilterSettingDialog createSettingDialogForCopy(String managerName, boolean common, GenericFilterSettingResponse filterSetting) {
		return new JobHistoryFilterSettingDialog(
				getShell(),
				FilterSettingEditMode.ADD,
				managerName,
				common,
				filterSetting);
	}

}
