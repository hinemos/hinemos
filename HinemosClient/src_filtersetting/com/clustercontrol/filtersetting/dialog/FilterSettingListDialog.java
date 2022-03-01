/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.EventFilterSettingResponse;
import org.openapitools.client.model.FilterSettingSummariesResponse;
import org.openapitools.client.model.FilterSettingSummaryResponse;
import org.openapitools.client.model.JobHistoryFilterSettingResponse;
import org.openapitools.client.model.RoleInfoResponseP1;
import org.openapitools.client.model.StatusFilterSettingResponse;
import org.openapitools.client.model.UserInfoResponseP3;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.dialog.ObjectPrivilegeEditDialog;
import com.clustercontrol.accesscontrol.dialog.ObjectPrivilegeListDialog;
import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.filtersetting.util.FilterSettingHelper;
import com.clustercontrol.filtersetting.util.FilterSettingRestClientWrapper;
import com.clustercontrol.filtersetting.util.GenericFilterSettingResponse;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.ManagerTag;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * フィルタ設定一覧ダイアログの基本となるクラスです。
 * フィルタ分類ごとにサブクラスがあります。
 */
public abstract class FilterSettingListDialog extends CommonDialog {
	private static Log logger = LogFactory.getLog(FilterSettingListDialog.class);

	/** コピー時のフィルタID("%s"にオリジナルが入る) */
	private static final String COPIED_FILTER_ID = "%s";

	/** フィルタ分類 */
	private final FilterCategoryEnum category;

	/** 接続マネージャのうちいずれかで管理者ならtrue */
	private final boolean belongingAdmins;

	// コントロール
	private TabFolder tbfList;
	private TabItem tabCommon;
	private TabItem tabUser;

	private Composite cmpCommonTab;
	private Table tblCommon;
	private CommonTableViewer ctvCommon;
	private Text txtCommonSearch;
	private Button btnAllUsers;
	private Label lblCommonCount;

	private Composite cmpUserTab;
	private Table tblUser;
	private CommonTableViewer ctvUser;
	private Text txtUserSearch;
	private Label lblUserCount;

	private Composite cmpButtons;
	private Button btnAdd;
	private Button btnModify;
	private Button btnCopy;
	private Button btnDelete;
	private Button btnObjPriv;

	public FilterSettingListDialog(Shell parent, FilterCategoryEnum category) {
		super(parent);
		this.category = category;

		// アクティブなマネージャから所属ロールを取得して、ADMINISTRATORSが1つでもあるかどうかを探す
		boolean belongingAdmins = false;
		for (String managerName : RestConnectManager.getActiveManagerNameList()) {
			try {
				AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
				UserInfoResponseP3 res = wrapper.getOwnerRoleIdList();
				for (RoleInfoResponseP1 role : res.getRoleList()) {
					if (RoleIdConstant.ADMINISTRATORS.equals(role.getRoleId())) {
						belongingAdmins = true;
						break;
					}
				}
			} catch (Exception e) {
				// 情報が取れなくても一部コントロールが無効になるだけなので、エラー表示だけして握り潰す
				logger.warn("ctor: " + e.getMessage());
				logger.debug("Exception detail", e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						ApiResultDialog.formatMessage(
								managerName,
								Messages.getString("message.error.get.available_role")));
			}
		}
		this.belongingAdmins = belongingAdmins;
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
		GridData gd; // 汎用変数
		Locale locale = Locale.getDefault();

		// タイトル
		this.getShell().setText(getTitle());

		// レイアウト
		GridLayout lyoBase = new GridLayout(2, false);
		lyoBase.marginWidth = 10;
		lyoBase.marginHeight = 10;
		parent.setLayout(lyoBase);

		// タブフォルダー
		tbfList = new TabFolder(parent, SWT.NONE);
		tbfList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout lyoList = new GridLayout(1, false);
		lyoList.marginWidth = 0;
		lyoList.marginHeight = 0;
		tbfList.setLayout(lyoList);

		// 共通タブ
		cmpCommonTab = new Composite(tbfList, SWT.NONE);
		cmpCommonTab.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cmpCommonTab.setBackground(parent.getBackground());  // タブの中は白くてみにくいので
		GridLayout lyoCommonTab = new GridLayout(5, false);
		lyoCommonTab.marginWidth = 2;
		lyoCommonTab.marginHeight = 2;
		cmpCommonTab.setLayout(lyoCommonTab);

		// 共通フィルタ設定一覧
		tblCommon = new Table(cmpCommonTab, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1);
		gd.heightHint = 300;  // 表の高さを確保するために指定する (ユーザフィルタ側は追随してくる模様)
		tblCommon.setLayoutData(gd);
		tblCommon.setHeaderVisible(true);
		tblCommon.setLinesVisible(true);

		ArrayList<TableColumnInfo> commonColumns = new ArrayList<TableColumnInfo>();
		commonColumns.add(new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.MANAGER_NAME, 120, SWT.LEFT));
		commonColumns.add(new TableColumnInfo(Messages.getString("fltset.filter_id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		commonColumns.add(new TableColumnInfo(Messages.getString("fltset.filter_name", locale), TableColumnInfo.NONE, 300, SWT.LEFT));
		commonColumns.add(new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));

		ctvCommon = new CommonTableViewer(tblCommon);
		ctvCommon.createTableColumn(commonColumns, 0, 1, 1);

		// 共通フィルタ設定の検索窓
		Label lblCommonSearch = new Label(cmpCommonTab, SWT.NONE);
		lblCommonSearch.setText(Messages.getString("search") + " : ");

		txtCommonSearch = new Text(cmpCommonTab, SWT.BORDER);
		txtCommonSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtCommonSearch.setMessage(Messages.get("message.fltset.search_hint"));

		Button btnDummy = new Button(cmpCommonTab, SWT.CHECK);			// ユーザフィルタ設定とレイアウトを合わせるためのダミー。
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);	// ただし、ダイアログの幅を変更すると、なぜかずれてしまう模様。
		gd.exclude = true;
		btnDummy.setLayoutData(gd);
		btnDummy.setText(Messages.getString("fltset.owner.user.show_all"));
		btnDummy.setVisible(false);

		// 共通フィルタ設定の件数表示
		lblCommonCount = new Label(cmpCommonTab, SWT.NONE);
		lblCommonCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		String countPlaceHolder = Messages.getString("number.of.display.list") + " : 999";
		lblCommonCount.setText(countPlaceHolder);

		tabCommon = new TabItem(tbfList, SWT.NONE);
		tabCommon.setText(Messages.getString("fltset.owner.common"));
		tabCommon.setControl(cmpCommonTab);

		// ユーザタブ
		cmpUserTab = new Composite(tbfList, SWT.NONE);
		cmpUserTab.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cmpUserTab.setBackground(parent.getBackground());  // タブの中は白くて見にくいので
		GridLayout lyoUserTab = new GridLayout(5, false);
		lyoUserTab.marginWidth = 2;
		lyoUserTab.marginHeight = 2;
		cmpUserTab.setLayout(lyoUserTab);

		// ユーザフィルタ設定一覧
		tblUser = new Table(cmpUserTab, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		tblUser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
		tblUser.setHeaderVisible(true);
		tblUser.setLinesVisible(true);

		ArrayList<TableColumnInfo> userColumns = new ArrayList<TableColumnInfo>();
		userColumns.add(new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.MANAGER_NAME, 120, SWT.LEFT));
		userColumns.add(new TableColumnInfo(Messages.getString("fltset.filter_id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		userColumns.add(new TableColumnInfo(Messages.getString("fltset.filter_name", locale), TableColumnInfo.NONE, 300, SWT.LEFT));
		userColumns.add(new TableColumnInfo(Messages.getString("user.id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));

		ctvUser = new CommonTableViewer(tblUser);
		ctvUser.createTableColumn(userColumns, 0, 1, 1);

		// ユーザフィルタ設定の検索窓
		Label lblUserSearch = new Label(cmpUserTab, SWT.NONE);
		lblUserSearch.setText(Messages.getString("search") + " : ");

		txtUserSearch = new Text(cmpUserTab, SWT.BORDER);
		txtUserSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtUserSearch.setMessage(Messages.get("message.fltset.search_hint"));

		// 全ユーザ表示チェックボックス (管理者のみ)
		btnAllUsers = new Button(cmpUserTab, SWT.CHECK);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.exclude = !belongingAdmins;
		btnAllUsers.setLayoutData(gd);
		btnAllUsers.setText(Messages.getString("fltset.owner.user.show_all"));
		btnAllUsers.setVisible(belongingAdmins);

		// ユーザフィルタ設定の件数表示
		lblUserCount = new Label(cmpUserTab, SWT.NONE);
		lblUserCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUserCount.setText(countPlaceHolder);

		tabUser = new TabItem(tbfList, SWT.NONE);
		tabUser.setText(Messages.getString("fltset.owner.user"));
		tabUser.setControl(cmpUserTab);

		// ボタン配置用コンポジット
		cmpButtons = new Composite(parent, SWT.NONE);
		cmpButtons.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		GridLayout lyoButtons = new GridLayout(1, false);
		lyoButtons.marginWidth = 5;
		lyoButtons.marginHeight = 22;
		cmpButtons.setLayout(lyoButtons);

		// 追加ボタン
		btnAdd = new Button(cmpButtons, SWT.NONE);
		btnAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAdd.setText(Messages.getString("add"));

		// 変更ボタン
		btnModify = new Button(cmpButtons, SWT.NONE);
		btnModify.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnModify.setText(Messages.getString("modify"));

		// コピーボタン
		btnCopy = new Button(cmpButtons, SWT.NONE);
		btnCopy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnCopy.setText(Messages.getString("copy"));

		// 削除ボタン
		btnDelete = new Button(cmpButtons, SWT.NONE);
		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnDelete.setText(Messages.getString("delete"));

		// spacer
		new Label(cmpButtons, SWT.NONE);

		// オブジェクト権限ボタン
		btnObjPriv = new Button(cmpButtons, SWT.NONE);
		btnObjPriv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnObjPriv.setText(Messages.getString("object.privilege"));

		// イベントリスナを登録
		registerEventListeners();

		// OKボタンを隠す
		getButton(IDialogConstants.OK_ID).setVisible(false);

		// ダイアログのサイズ調整
		adjustPosition();

		// 一覧最新化
		refreshList();
	}

	// 長いので別メソッドへ切り出した。
	private void registerEventListeners() {
		tbfList.addSelectionListener(new SelectionAdapter() {
			// タブ切替「後」に呼ばれる
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 検索テキストを同期
				if (isCommonSelected()) {
					txtCommonSearch.setText(txtUserSearch.getText());
				} else {
					txtUserSearch.setText(txtCommonSearch.getText());
				}
				// 一覧を更新
				refreshList();
			}
		});

		IDoubleClickListener listenerTableDoubleClick = new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				btnModify.notifyListeners(SWT.Selection, null);
			}
		};
		ctvCommon.addDoubleClickListener(listenerTableDoubleClick);
		ctvUser.addDoubleClickListener(listenerTableDoubleClick);

		ctvCommon.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				changeButtonsState();
			}
		});

		ctvUser.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				changeButtonsState();
			}
		});

		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilterSettingDialog dlg = createSettingDialogForAdd(
						RestConnectManager.getActiveManagerNameList().get(0), // リスト先頭のマネージャを初期値に
						isCommonSelected());
				dlg.open();
				refreshList();
			}
		});

		btnModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ManagerTag<GenericFilterSettingResponse> mtg = fetchSelectedFilterSettingDetail();
				if (mtg == null) return;

				FilterSettingDialog dlg = createSettingDialogForModify(
						mtg.managerName,
						mtg.data.getCommon().booleanValue(),
						mtg.data);
				dlg.open();
				refreshList();
			}
		});

		btnCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ManagerTag<GenericFilterSettingResponse> mtg = fetchSelectedFilterSettingDetail();
				if (mtg == null) return;

				// フィルタIDを加工
				mtg.data.setFilterId(String.format(COPIED_FILTER_ID, mtg.data.getFilterId()));

				FilterSettingDialog dlg = createSettingDialogForCopy(
						mtg.managerName,
						mtg.data.getCommon().booleanValue(),
						mtg.data);
				dlg.open();
				refreshList();
			}
		});

		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				deleteFilterSettings();
			}
		});

		btnObjPriv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 選択項目をObjectBeanへ変換
				List<ObjectBean> objectBeans = new ArrayList<>();
				String ownerRoleId = null;
				for (ManagerTag<FilterSettingSummaryResponse> mtg : getSelectedFilterSettingSummaries()) {
					// ボタンの有効下制御をしているのでありえないが、念のためユーザフィルタ設定が混入していたら除去する
					if (!mtg.data.getCommon().booleanValue()) {
						continue;
					}

					ObjectBean bean = new ObjectBean(
							mtg.managerName,
							HinemosModuleConstant.FILTER_SETTING,
							mtg.data.getObjectId(),
							FilterSettingHelper.formatObjectIdForDisplay(mtg.data.getObjectId()));
					objectBeans.add(bean);

					ownerRoleId = mtg.data.getOwnerRoleId();
				}

				// ObjectBeanの数に合わせて処理分岐
				int result;
				int size = objectBeans.size();
				if (size == 0) {
					// ボタンの有効下制御をしているのでありえないが、念のため対象非選択なら何もしない
					return;
				} else if (size == 1) {
					// 単一選択：オブジェクト権限一覧を表示する
					ObjectBean bean = objectBeans.get(0);
					ObjectPrivilegeListDialog dialog = new ObjectPrivilegeListDialog(
							getShell(),
							bean.getManagerName(),
							bean.getObjectId(),
							HinemosModuleConstant.FILTER_SETTING,
							ownerRoleId);
					dialog.setObjectIdForDisplay(bean.getObjectIdForDisplay());
					result = dialog.open();
				} else {
					// 複数選択：オブジェクト権限編集を表示する
					ObjectPrivilegeEditDialog dialog = new ObjectPrivilegeEditDialog(
							getShell(),
							objectBeans,
							null,
							null);
					result = dialog.open();
				}
				// オブジェクト権限登録を行った場合は一覧を更新する
				if (result == IDialogConstants.OK_ID) {
					refreshList();
				}
			}
		});

		btnAllUsers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshList();
			}
		});

		TraverseListener listenerSearchTextTraverse = new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				Text text = (Text) e.widget;
				switch (e.detail) {
				case (SWT.TRAVERSE_ESCAPE):
					text.setText("");
					text.setFocus();
					e.doit = false; // ダイアログを閉じないように
					break;
				case (SWT.TRAVERSE_RETURN):
					text.selectAll();
					refreshList();
					break;
				default:
					break;
				}
			}
		};

		FocusListener listenerSearchTextFocus = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				refreshList();
			}
		};

		txtCommonSearch.addTraverseListener(listenerSearchTextTraverse);
		txtCommonSearch.addFocusListener(listenerSearchTextFocus);
		txtUserSearch.addTraverseListener(listenerSearchTextTraverse);
		txtUserSearch.addFocusListener(listenerSearchTextFocus);
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("close");
	}

	/**
	 * 共通フィルタ一覧が選択されている場合は true を、ユーザフィルタ一覧が選択されている場合は false を返します。
	 */
	public boolean isCommonSelected() {
		TabItem[] selection = tbfList.getSelection();
		return selection.length > 0 && selection[0] == tabCommon;
	}

	/**
	 * 一覧表示を最新にします。
	 */
	public void refreshList() {
		List<ManagerTag<FilterSettingSummaryResponse>> list = new ArrayList<>();
		ApiResultDialog resultDialog = new ApiResultDialog();
		boolean common = isCommonSelected();
		String pattern = common ? txtCommonSearch.getText() : txtUserSearch.getText();

		// マネージャから一覧情報を取得
		for (String managerName : RestConnectManager.getActiveManagerNameList()) {
			try {
				FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
				FilterSettingSummariesResponse res;
				if (isCommonSelected()) {
					try {
						res = rest.getCommonFilterSettingSummaries(category, pattern);
					} catch (InvalidRole ignored) {
						// ユーザフィルタに関してはシステム権限なしで使えるので、
						// 共通フィルタ設定の読み取りで権限エラーが起きたとしてもエラーは出さずに無視する
						continue;
					}
				} else {
					if (btnAllUsers.getSelection()) {
						res = rest.getAllUserFilterSettingSummaries(category, pattern);
					} else {
						res = rest.getUserFilterSettingSummaries(category, pattern,
								RestConnectManager.getLoginUserId(managerName));
					}
				}
				list.addAll(ManagerTag.listFrom(managerName, res.getSummaries()));
			} catch (Exception e) {
				resultDialog.addFailure(managerName, e);
			}
		}
		resultDialog.show();

		// 一覧表示
		ArrayList<List<Object>> inputRows = new ArrayList<>();
		for (ManagerTag<FilterSettingSummaryResponse> it : list) {
			List<Object> inputCols = new ArrayList<>();
			inputCols.add(it.managerName);
			inputCols.add(it.data.getFilterId());
			inputCols.add(it.data.getFilterName());
			if (common) {
				inputCols.add(it.data.getOwnerRoleId());
			} else {
				inputCols.add(it.data.getOwnerUserId());
			}
			inputCols.add(it);
			inputRows.add(inputCols);
		}

		// ヒット件数表示
		String countText = Messages.getString("number.of.display.list") + " : " + inputRows.size();

		if (common) {
			ctvCommon.setInput(inputRows);
			lblCommonCount.setText(countText);
		} else {
			ctvUser.setInput(inputRows);
			lblUserCount.setText(countText);
		}

		// ボタン制御
		changeButtonsState();
	}

	/**
	 * 一覧上で選択されているフィルタ設定の概要情報を取得します。
	 */
	private List<ManagerTag<FilterSettingSummaryResponse>> getSelectedFilterSettingSummaries() {
		Table table = isCommonSelected() ? tblCommon : tblUser;
		TableItem[] selection = table.getSelection();
		List<ManagerTag<FilterSettingSummaryResponse>> rtn = new ArrayList<>();
		for (TableItem tableItem : selection) {
			@SuppressWarnings("unchecked")
			List<Object> row = (List<Object>) tableItem.getData();
			@SuppressWarnings("unchecked")
			ManagerTag<FilterSettingSummaryResponse> mtg =
					(ManagerTag<FilterSettingSummaryResponse>) row.get(row.size() - 1);
			rtn.add(mtg);
		}
		return rtn;
	}

	/**
	 * 一覧上で単一選択されているフィルタ設定の詳細情報を取得します。
	 * 非選択または複数選択の場合はnullを返します。
	 * 何らかのエラーが発生した場合は、エラーダイアログを表示した後、nullを返します。
	 */
	private ManagerTag<GenericFilterSettingResponse> fetchSelectedFilterSettingDetail() {
		List<ManagerTag<FilterSettingSummaryResponse>> mtgs = getSelectedFilterSettingSummaries();
		if (mtgs.size() != 1) return null;
		ManagerTag<FilterSettingSummaryResponse> mtg = mtgs.get(0);

		GenericFilterSettingResponse fs = null;
		try {
			FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(mtg.managerName);
			if (mtg.data.getCommon().booleanValue()) {
				switch (category) {
				case EVENT:
					EventFilterSettingResponse er = rest.getCommonEventFilterSetting(mtg.data.getFilterId());
					fs = GenericFilterSettingResponse.fromEventResponse(er);
					break;
				case STATUS:
					StatusFilterSettingResponse sr = rest.getCommonStatusFilterSetting(mtg.data.getFilterId());
					fs = GenericFilterSettingResponse.fromStatusResponse(sr);
					break;
				case JOB_HISTORY:
					JobHistoryFilterSettingResponse jr = rest.getCommonJobHistoryFilterSetting(mtg.data.getFilterId());
					fs = GenericFilterSettingResponse.fromJobHistoryResponse(jr);
					break;
				}
			} else {
				switch (category) {
				case EVENT:
					EventFilterSettingResponse er = rest.getUserEventFilterSetting(mtg.data.getFilterId(), mtg.data.getOwnerUserId());
					fs = GenericFilterSettingResponse.fromEventResponse(er);
					break;
				case STATUS:
					StatusFilterSettingResponse sr = rest.getUserStatusFilterSetting(mtg.data.getFilterId(), mtg.data.getOwnerUserId());
					fs = GenericFilterSettingResponse.fromStatusResponse(sr);
					break;
				case JOB_HISTORY:
					JobHistoryFilterSettingResponse jr = rest.getUserJobHistoryFilterSetting(mtg.data.getFilterId(), mtg.data.getOwnerUserId());
					fs = GenericFilterSettingResponse.fromJobHistoryResponse(jr);
					break;
				}
			}
		} catch (Exception e) {
			new ApiResultDialog().addFailure(mtg.managerName, e);
			if (e instanceof FilterSettingNotFound) {
				// フィルタ設定が削除されていたりする可能性があるので一覧を更新する
				refreshList();
			}
			return null;
		}

		return new ManagerTag<GenericFilterSettingResponse>(mtg.managerName, fs);
	}

	/**
	 * 一覧の選択状態に合わせて、操作ボタンの状態を変更します。
	 */
	private void changeButtonsState() {
		Table table = isCommonSelected() ? tblCommon : tblUser;
		TableItem[] selection = table.getSelection();
		if (selection.length == 0) {
			btnAdd.setEnabled(true);
			btnModify.setEnabled(false);
			btnCopy.setEnabled(false);
			btnDelete.setEnabled(false);
			btnObjPriv.setEnabled(false);
		} else if (selection.length == 1) {
			btnAdd.setEnabled(true);
			btnModify.setEnabled(true);
			btnCopy.setEnabled(true);
			btnDelete.setEnabled(true);
			btnObjPriv.setEnabled(isCommonSelected());
		} else {
			btnAdd.setEnabled(true);
			btnModify.setEnabled(false);
			btnCopy.setEnabled(false);
			btnDelete.setEnabled(true);
			btnObjPriv.setEnabled(isCommonSelected());
		}
	}

	/**
	 * 一覧で選択されているフィルタ設定を削除します。
	 * 現在のところ、GUIデザイン的に、共通フィルタ設定とユーザフィルタ設定を同時に選択することは不可能ですが、
	 * 同時選択しても動作する実装になっています(いるはずです)。
	 */
	private void deleteFilterSettings() {
		List<ManagerTag<FilterSettingSummaryResponse>> mtgs = getSelectedFilterSettingSummaries();
		if (mtgs.size() == 0) return;

		// マネージャごとのリストへ変換
		SortedMap<String, List<FilterSettingSummaryResponse>> map = ManagerTag.listToMap(mtgs);

		// 削除確認
		if (!deleteFilterSettings_confirm(map)) return;

		// 準備
		ApiResultDialog resultDialog = new ApiResultDialog();
		String prefix = Messages.get("fltset.filter_id") + "=";

		// マネージャでループ
		for (Entry<String, List<FilterSettingSummaryResponse>> iterManager : map.entrySet()) {
			String managerName = iterManager.getKey();
			List<FilterSettingSummaryResponse> filterSummaries = iterManager.getValue();

			// フィルタIDを共通/ユーザ別に仕分け
			List<String> cmnFilterIds = new ArrayList<>();
			Map<String, List<String>> usrFilterIdsMap = new HashMap<>();
			deleteFilterSettings_divideFilterIds(filterSummaries, cmnFilterIds, usrFilterIdsMap);

			// 削除実行 - 共通フィルタ設定
			FilterSettingRestClientWrapper rest = FilterSettingRestClientWrapper.getWrapper(managerName);
			if (cmnFilterIds.size() > 0) {
				String ids = StringUtils.join(cmnFilterIds, ",");
				try {
					switch (category) {
					case EVENT:
						rest.deleteCommonEventFilterSetting(ids);
						break;
					case STATUS:
						rest.deleteCommonStatusFilterSetting(ids);
						break;
					case JOB_HISTORY:
						rest.deleteCommonJobHistoryFilterSetting(ids);
						break;
					}
					resultDialog.addSuccess(managerName, Messages.get("message.fltset.deleted", prefix + ids));
				} catch (Exception e) {
					logger.warn("deleteFilterSettings: " + e.getClass().getSimpleName() + ", message=" + e.getMessage()
							+ ", manager=" + managerName + ", filterIds=[" + ids + "]");
					resultDialog.addFailure(managerName, e);
				}
			}
			// 削除実行 - ユーザフィルタ設定
			for (Entry<String, List<String>> iterUser : usrFilterIdsMap.entrySet()) {
				String userId = iterUser.getKey();
				String ids = StringUtils.join(iterUser.getValue(), ",");
				try {
					switch (category) {
					case EVENT:
						rest.deleteUserEventFilterSetting(ids, userId);
						break;
					case STATUS:
						rest.deleteUserStatusFilterSetting(ids, userId);
						break;
					case JOB_HISTORY:
						rest.deleteUserJobHistoryFilterSetting(ids, userId);
						break;
					}
					resultDialog.addSuccess(managerName, Messages.get("message.fltset.deleted", prefix + ids));
				} catch (Exception e) {
					logger.warn("deleteFilterSettings: " + e.getClass().getSimpleName() + ", message=" + e.getMessage()
							+ ", manager=" + managerName + ", filterIds=[" + ids + "], userId=" + userId);
					resultDialog.addFailure(managerName, e);
				}
			}
		}

		// 結果表示
		resultDialog.show();
		refreshList();
	}

	/**
	 * 削除確認ダイアログを表示します。
	 * ユーザが削除実行を選んだ場合は true を返します。
	 */
	private boolean deleteFilterSettings_confirm(SortedMap<String, List<FilterSettingSummaryResponse>> map) {
		// 削除対象を下記フォーマットにする
		//     ----------------------
		//     マネージャ1 : id1,id2
		//     マネージャ2 : id3,id4,id5
		//     ----------------------
		StringBuilder target = new StringBuilder();
		for (Entry<String, List<FilterSettingSummaryResponse>> entry : map.entrySet()) {
			target.append(entry.getKey()).append(" : ");
			String delim = "";
			for (FilterSettingSummaryResponse it : entry.getValue()) {
				target.append(delim).append(it.getFilterId());
				delim = ",";
			}
			target.append("\n");
		}

		// 削除確認
		String msg = Messages.get("message.fltset.confirm_to_delete") + "\n" + target.toString();
		if (MessageDialog.openConfirm(null, Messages.get("confirmed"), msg)) {
			return true; // 実行
		}
		return false;  // キャンセル
	}

	/**
	 * フィルタ設定の概要情報リスト filterSummaries から、フィルタIDをAPIの呼び出し単位(共通/ユーザ)で仕分けして、
	 * 結果を受け取るコレクション cmnFilterIds, usrFilterIdsMap へセットします。
	 */
	private void deleteFilterSettings_divideFilterIds(List<FilterSettingSummaryResponse> filterSummaries,
			List<String> cmnFilterIds, Map<String, List<String>> usrFilterIdsMap) {
		for (FilterSettingSummaryResponse summ : filterSummaries) {
			if (summ.getCommon().booleanValue()) {
				cmnFilterIds.add(summ.getFilterId());
			} else {
				List<String> usrFilterIds = usrFilterIdsMap.get(summ.getOwnerUserId());
				if (usrFilterIds == null) {
					usrFilterIds = new ArrayList<>();
					usrFilterIdsMap.put(summ.getOwnerUserId(), usrFilterIds);
				}
				usrFilterIds.add(summ.getFilterId());
			}
		}
	}

	/**
	 * タイトルバーへ表示する文字列を返します。
	 */
	protected abstract String getTitle();

	/**
	 * フィルタ設定を作成するための設定ダイアログを生成します。
	 */
	protected abstract FilterSettingDialog createSettingDialogForAdd(String managerName, boolean common);

	/**
	 * フィルタ設定を変更するための設定ダイアログを生成します。
	 */
	protected abstract FilterSettingDialog createSettingDialogForModify(String managerName, boolean common, GenericFilterSettingResponse filterSetting);

	/**
	 * フィルタ設定をコピー生成するための設定ダイアログを生成します。
	 */
	protected abstract FilterSettingDialog createSettingDialogForCopy(String managerName, boolean common, GenericFilterSettingResponse filterSetting);

}
