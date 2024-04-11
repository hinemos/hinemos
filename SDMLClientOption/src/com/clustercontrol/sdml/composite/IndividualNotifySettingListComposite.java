/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;
import org.openapitools.client.model.SdmlMonitorNotifyRelationResponse;
import org.openapitools.client.model.SdmlMonitorTypeMasterResponse;

import com.clustercontrol.sdml.action.GetIndividualNotifySettingTableDefine;
import com.clustercontrol.sdml.dialog.IndividualNotifySettingCreateDialog;
import com.clustercontrol.sdml.util.SdmlMonitorTypeUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

public class IndividualNotifySettingListComposite extends Composite {
	/** マネージャ */
	private String managerName = null;
	/** オーナーロールID */
	private String ownerRoleId = null;
	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;
	/** 自動作成監視用の通知関連情報 */
	private Map<String, SdmlMonitorNotifyRelationResponse> monitorNotifyRelationMap = null;
	/** 追加 ボタン */
	private Button addButton = null;
	/** 変更 ボタン */
	private Button modifyButton = null;
	/** 削除 ボタン */
	private Button deleteButton = null;
	/** コピー ボタン */
	private Button copyButton = null;

	/** SDML監視種別マップ <id, SdmlMonitorTypeMasterResponse> */
	private Map<String, SdmlMonitorTypeMasterResponse> sdmlMonitorTypeMap = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 * @param style
	 * @param managerName
	 */
	public IndividualNotifySettingListComposite(Composite parent, int style, String managerName, String ownerRoleId) {
		super(parent, style);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
		initialize();
		this.sdmlMonitorTypeMap = SdmlMonitorTypeUtil.getMap(managerName);
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridData gridData = null;
		GridLayout layout = null;

		layout = new GridLayout(10, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);

		Table table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetIndividualNotifySettingTableDefine.get(),
				GetIndividualNotifySettingTableDefine.SORT_COLUMN_INDEX,
				GetIndividualNotifySettingTableDefine.SORT_ORDER);
		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				SdmlMonitorNotifyRelationResponse info = getSelectedItem();
				if (info != null) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					IndividualNotifySettingCreateDialog dialog = new IndividualNotifySettingCreateDialog(shell,
							getAvailableSdmlMonitorTypeMap(info), info, managerName, ownerRoleId);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = getTableViewer().getTable();
						monitorNotifyRelationMap.put(dialog.getSdmlMonitorTypeId(), dialog.getInputData());
						update();
						table.setSelection(table.getSelectionIndex());
					}
				}
			}
		});

		// 操作ボタン用Composite
		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, false));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		buttonComposite.setLayoutData(gridData);

		// 追加ボタン
		this.addButton = this.createButton(buttonComposite, Messages.getString("add"));
		this.addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				IndividualNotifySettingCreateDialog dialog = new IndividualNotifySettingCreateDialog(shell,
						getAvailableSdmlMonitorTypeMap(null), managerName, ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					monitorNotifyRelationMap.put(dialog.getSdmlMonitorTypeId(), dialog.getInputData());
					update();
				}
			}
		});

		// 変更ボタン
		this.modifyButton = this.createButton(buttonComposite, Messages.getString("modify"));
		this.modifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SdmlMonitorNotifyRelationResponse target = getSelectedItem();
				if (target != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					String beforeId = target.getSdmlMonitorTypeId();
					IndividualNotifySettingCreateDialog dialog = new IndividualNotifySettingCreateDialog(shell,
							getAvailableSdmlMonitorTypeMap(target), target, managerName, ownerRoleId);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = getTableViewer().getTable();
						int selectIndex = table.getSelectionIndex();
						monitorNotifyRelationMap.remove(beforeId);
						monitorNotifyRelationMap.put(dialog.getSdmlMonitorTypeId(), dialog.getInputData());
						update();
						table.setSelection(selectIndex);
					}
				} else {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.sdml.param.choose"));
				}
			}
		});

		// 削除ボタン
		this.deleteButton = this.createButton(buttonComposite, Messages.getString("delete"));
		this.deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SdmlMonitorNotifyRelationResponse target = getSelectedItem();
				if (target != null) {
					String displayName = "";
					if (target.getSdmlMonitorTypeId() != null) {
						SdmlMonitorTypeMasterResponse type = sdmlMonitorTypeMap.get(target.getSdmlMonitorTypeId());
						if (type != null) {
							displayName = HinemosMessage.replace(type.getSdmlMonitorType());
						}
					}

					String[] args = { displayName, Messages.getString("delete") };
					if (MessageDialog.openConfirm(null, Messages.getString("confirmed"),
							Messages.getString("message.sdml.param.action.confirm", args))) {
						monitorNotifyRelationMap.remove(target.getSdmlMonitorTypeId());
						update();
					}
				} else {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.sdml.param.choose"));
				}
			}
		});

		// コピーボタン
		this.copyButton = this.createButton(buttonComposite, Messages.getString("copy"));
		this.copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SdmlMonitorNotifyRelationResponse target = getSelectedItem();
				if (target != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					IndividualNotifySettingCreateDialog dialog = new IndividualNotifySettingCreateDialog(shell,
							getAvailableSdmlMonitorTypeMap(null), target, managerName, ownerRoleId);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = getTableViewer().getTable();
						int selectIndex = table.getSelectionIndex();
						monitorNotifyRelationMap.put(dialog.getSdmlMonitorTypeId(), dialog.getInputData());
						update();
						table.setSelection(selectIndex);
					}
				} else {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.sdml.param.choose"));
				}
			}
		});
	}

	/**
	 * 引数で受け取った情報から各項目に設定します
	 * 
	 * @param info
	 */
	public void setInputData(SdmlControlSettingInfoResponse info) {
		// 初期化
		this.monitorNotifyRelationMap = new HashMap<>();
		if (info != null && info.getSdmlMonitorNotifyRelationList() != null
				&& !info.getSdmlMonitorNotifyRelationList().isEmpty()) {
			// 設定
			for (SdmlMonitorNotifyRelationResponse relationInfo : info.getSdmlMonitorNotifyRelationList()) {
				this.monitorNotifyRelationMap.put(relationInfo.getSdmlMonitorTypeId(), relationInfo);
			}
			// テーブル更新
			update();
		}
	}

	/**
	 * このコンポジットが利用するテーブルビューアーを返します。
	 *
	 * @return テーブルビューアー
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * 現在選択されているアイテムを返します。
	 * <p>
	 * 選択されていない場合は、<code>null</code>を返します。
	 *
	 * @return 選択アイテム
	 */
	public SdmlMonitorNotifyRelationResponse getSelectedItem() {
		StructuredSelection selection = (StructuredSelection) this.tableViewer.getSelection();
		if (selection == null || selection.getFirstElement() == null) {
			return null;
		} else {
			ArrayList<?> list = (ArrayList<?>) selection.getFirstElement();
			// 識別用のIDで判定
			String id = (String) list.get(2);
			return this.monitorNotifyRelationMap.get(id);
		}
	}

	/**
	 * 共通の設定がされたボタンを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param label
	 *            ボタンに表示するテキスト
	 * @return ボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);
		button.setText(label);

		return button;
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update() {
		// テーブル更新
		ArrayList<Object> listAll = new ArrayList<Object>();
		for (SdmlMonitorNotifyRelationResponse info : this.monitorNotifyRelationMap.values()) {
			ArrayList<Object> list = new ArrayList<Object>();

			// SDML監視種別（変換する）
			SdmlMonitorTypeMasterResponse type = sdmlMonitorTypeMap.get(info.getSdmlMonitorTypeId());
			if (type != null) {
				list.add(HinemosMessage.replace(type.getSdmlMonitorType()));
			} else {
				list.add("");
			}

			// 通知ID
			list.add(getNotifyIdText(info.getNotifyRelationList()));

			// 選択したアイテムの識別用にIDをセットする（表示はしない）
			list.add(info.getSdmlMonitorTypeId());
			listAll.add(list);
		}
		this.tableViewer.setInput(listAll);
	}

	/**
	 * 引数で受け取った情報に入力値を設定します
	 * 
	 * @param info
	 */
	public void createInputData(SdmlControlSettingInfoResponse info) {
		if (info != null) {
			if (this.monitorNotifyRelationMap != null && !this.monitorNotifyRelationMap.isEmpty()) {
				// 登録前にアプリケーションIDを設定
				for (SdmlMonitorNotifyRelationResponse relationInfo : this.monitorNotifyRelationMap.values()) {
					relationInfo.setApplicationId(info.getApplicationId());
				}
				info.setSdmlMonitorNotifyRelationList(new ArrayList<>(this.monitorNotifyRelationMap.values()));
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		tableViewer.getTable().setEnabled(enabled);
		addButton.setEnabled(enabled);
		modifyButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
		copyButton.setEnabled(enabled);
	}

	/**
	 * テーブルに登録されていないSDML監視種別のみ返却する
	 * 
	 * @param selected
	 *            変更の場合は選択済みの項目も選択肢に含める必要がある
	 * @return
	 */
	private Map<String, SdmlMonitorTypeMasterResponse> getAvailableSdmlMonitorTypeMap(
			SdmlMonitorNotifyRelationResponse selected) {
		Map<String, SdmlMonitorTypeMasterResponse> rtnMap = new HashMap<>();
		for (Map.Entry<String, SdmlMonitorTypeMasterResponse> entry : sdmlMonitorTypeMap.entrySet()) {
			if (selected != null && selected.getSdmlMonitorTypeId().equals(entry.getKey())) {
				// 選択済みの項目がある場合はセット
				rtnMap.put(entry.getKey(), entry.getValue());
				continue;
			}
			boolean exists = false;
			for (String key : monitorNotifyRelationMap.keySet()) {
				if (entry.getKey().equals(key)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				// まだ登録されていないSDML監視種別のみセット
				rtnMap.put(entry.getKey(), entry.getValue());
			}
		}
		return rtnMap;
	}

	private String getNotifyIdText(List<NotifyRelationInfoResponse> relList) {
		if (relList == null || relList.isEmpty()) {
			return "";
		}
		// 表示用に通知IDを連結する
		List<String> idList = new ArrayList<>();
		for (NotifyRelationInfoResponse rel : relList) {
			idList.add(rel.getNotifyId());
		}
		return String.join(",", idList);
	}

	public void reflect(String managerName, String ownerRoleId) {
		this.managerName = managerName;
		this.sdmlMonitorTypeMap = SdmlMonitorTypeUtil.getMap(managerName);
		this.ownerRoleId = ownerRoleId;
		// すでに設定されている通知IDは一括でクリアする
		if (this.monitorNotifyRelationMap != null && !this.monitorNotifyRelationMap.isEmpty()) {
			for (SdmlMonitorNotifyRelationResponse relation : this.monitorNotifyRelationMap.values()) {
				relation.setNotifyRelationList(new ArrayList<>());
			}
			update();
		}
	}
}
