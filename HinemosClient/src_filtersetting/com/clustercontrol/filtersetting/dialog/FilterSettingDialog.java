/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.fault.FilterSettingDuplicate;
import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.filtersetting.bean.FilterSettingEditMode;
import com.clustercontrol.filtersetting.composite.FilterSettingHeaderComposite;
import com.clustercontrol.filtersetting.util.GenericFilterSettingRequest;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.filtersetting.util.FilterSettingHelper;
import com.clustercontrol.util.Messages;

/**
 * フィルタ設定の作成・変更ダイアログの基本となるクラスです。
 * フィルタ分類ごとにサブクラスがあります。
 */
public abstract class FilterSettingDialog extends CommonDialog {
	private static final Log logger = LogFactory.getLog(FilterSettingDialog.class);

	private final FilterSettingEditMode mode;
	private final GenericFilterSettingResponse initialFilterSetting;
	private String managerName;
	private boolean common;

	private FilterSettingHeaderComposite cmpHeader;
	private Composite cmpFilter;

	/**
	 * @param parent 親ウィンドウ。
	 * @param mode 編集モード。
	 * @param managerName マネージャ名の初期値。
	 * @param common 共通/ユーザ選択の初期値。
	 * @param filterSetting 設定内容の初期値。
	 */
	public FilterSettingDialog(
			Shell parent,
			FilterSettingEditMode mode,
			String managerName,
			boolean common,
			GenericFilterSettingResponse filterSetting) {
		super(parent);
		this.mode = mode;
		this.managerName = managerName;
		this.common = common;
		this.initialFilterSetting = filterSetting;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 */
	@Override
	protected Point getInitialSize() {
		// 後でpackするので実サイズは変わる
		return new Point(ClusterControlPlugin.WINDOW_INIT_SIZE.width, ClusterControlPlugin.WINDOW_INIT_SIZE.height);
	}

	/**
	 * 本ダイアログ固有のコントロールを配置します。
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(getTitle());

		// レイアウト
		GridLayout lyoBase = new GridLayout(1, false);
		lyoBase.marginWidth = 10;
		lyoBase.marginHeight = 10;
		parent.setLayout(lyoBase);

		// グループ[フィルタ設定]
		Group grpUpper = new Group(parent, SWT.NONE);
		grpUpper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpUpper.setText(Messages.getString("fltset.edit_group.setting"));
		GridLayout lyoUpper = new GridLayout(1, false);
		lyoUpper.marginWidth = 5;
		lyoUpper.marginHeight = 5;
		grpUpper.setLayout(lyoUpper);

		// フィルタ設定コンポジット
		cmpHeader = new FilterSettingHeaderComposite(
				grpUpper,
				mode,
				managerName,
				common,
				initialFilterSetting.getOwnerRoleId(),
				initialFilterSetting.getFilterId(),
				initialFilterSetting.getFilterName());
		cmpHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// グループ[フィルタ条件]
		Group grpLower = new Group(parent, SWT.NONE);
		grpLower.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpLower.setText(Messages.getString("fltset.edit_group.condition"));
		GridLayout lyoLower = new GridLayout(1, false);
		lyoLower.marginWidth = 5;
		lyoLower.marginHeight = 5;
		grpLower.setLayout(lyoLower);

		// フィルタ条件コンポジット (フィルタ分類別)
		cmpFilter = createFilterComposite(grpLower, cmpHeader);
		cmpFilter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// ダイアログのサイズ調整
		adjustPosition();
	}

	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	@Override
	protected boolean action() {
		// 入力内容を元にリクエストDTOを作成
		GenericFilterSettingRequest req = FilterSettingHelper.convertToRequest(initialFilterSetting);
		switch (mode) {
		case ADD:
			managerName = cmpHeader.getManagerName();
			common = cmpHeader.isCommonFilter();
			req.setFilterId(cmpHeader.getFilterId());
			req.setFilterName(cmpHeader.getFilterName());
			req.setOwnerRoleId(cmpHeader.getOwnerRoleId());
			applyFilterConditionsTo(req);
			break;
		case MODIFY:
			common = cmpHeader.isCommonFilter();
			req.setFilterName(cmpHeader.getFilterName());
			applyFilterConditionsTo(req);
			break;
		}

		// メッセージ付加情報
		String msgAdditional = Messages.get("fltset.filter_id") + "=" + req.getFilterId();

		// API呼び出し
		ApiResultDialog resultDialog = new ApiResultDialog();
		try {
			switch (mode) {
			case ADD:
				actionAdd(managerName, common, req);
				resultDialog.addSuccess(managerName, Messages.get("message.fltset.created", msgAdditional)).show();
				return true;

			case MODIFY:
				actionModify(managerName, common, initialFilterSetting.getOwnerUserId(), req);
				resultDialog.addSuccess(managerName, Messages.get("message.fltset.changed", msgAdditional)).show();
				return true;
			}

		} catch (Exception e) {
			logger.warn("action: " + e.getClass().getSimpleName() + " " + e.getMessage() + " " + msgAdditional);
			resultDialog.addFailure(managerName, e).show();
			return false;
		}
		throw new RuntimeException("Unknown mode=" + mode);
	}

	/**
	 * ダイアログのタイトルバーへ表示する文字列を返します。
	 */
	protected abstract String getTitle();

	/**
	 * サブクラス特有のフィルタ条件入力コンポジットを生成して返します。
	 * @param parent 生成するコンポジットの親とするコンポジット。
	 * @param header 併せて配置される{@link FilterSettingHeaderComposite}のインスタンス。
	 */
	protected abstract Composite createFilterComposite(Composite parent, FilterSettingHeaderComposite header);

	/**
	 * サブクラス特有のフィルタ条件を指定された {@link FilterSettingRequest} へ反映させます。
	 */
	protected abstract void applyFilterConditionsTo(GenericFilterSettingRequest filterSettingReq);

	/**
	 * フィルタ設定の作成を行います。
	 */
	protected abstract void actionAdd(String managerName, boolean common, GenericFilterSettingRequest filterSettingReq)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingDuplicate, RestConnectFailed, HinemosUnknown;

	/**
	 * フィルタ設定の変更を行います。
	 */
	protected abstract void actionModify(String managerName, boolean common, String userId, GenericFilterSettingRequest filterSettingReq)
			throws InvalidUserPass, InvalidRole, InvalidSetting, FilterSettingNotFound, RestConnectFailed, HinemosUnknown;

}
