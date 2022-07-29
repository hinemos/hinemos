/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.openapitools.client.model.NodeConfigCustomInfoResponse;
import org.openapitools.client.model.NodeConfigSettingInfoResponse;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.repository.dialog.NodeCustomInfoCreateDialog;
import com.clustercontrol.util.Messages;

/**
 * ユーザ任意情報一覧コンポジットクラス<BR>
 * <p>
 * <dl>
 * <dt>コンポジット</dt>
 * <dd>「ユーザ任意情報一覧」 フィールド</dd>
 * <dd>「追加」 ボタン</dd>
 * <dd>「変更」 ボタン</dd>
 * <dd>「削除」 ボタン</dd>
 * <dd>「コピー」 ボタン</dd>
 * </dl>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeCustomComposite extends Composite {

	/** ユーザ任意情報一覧 コンポジット. */
	private NodeCustomListComposite m_infoList = null;

	/** 追加 ボタン. */
	private Button m_buttonAdd = null;
	/** 変更 ボタン. */
	private Button m_buttonModify = null;
	/** 削除 ボタン. */
	private Button m_buttonDelete = null;
	/** コピー ボタン. */
	private Button m_buttonCopy = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeCustomComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		this.setLayout(new GridLayout(2, false));

		/*
		 * ユーザ任意情報一覧
		 */
		Composite listComposite = new Composite(this, SWT.NONE);
		listComposite.setLayout(new GridLayout(1, false));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		listComposite.setLayoutData(gridData);
		this.m_infoList = new NodeCustomListComposite(listComposite, SWT.BORDER);
		this.m_infoList.setLayoutData(gridData);

		/*
		 * 操作ボタン
		 */
		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, false));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		buttonComposite.setLayoutData(gridData);

		// 追加ボタン
		this.m_buttonAdd = this.createButton(buttonComposite, Messages.getString("add"));
		this.m_buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				NodeCustomInfoCreateDialog dialog = new NodeCustomInfoCreateDialog(shell, m_infoList.getCustomIdList());
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_infoList.getNodeConfigCustomInfoMap().put(dialog.getCustomId(), dialog.getInputData());
					m_infoList.update();
				}
			}
		});

		// 変更ボタン
		this.m_buttonModify = this.createButton(buttonComposite, Messages.getString("modify"));
		this.m_buttonModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_infoList.getTableViewer().getTable().getSelectionIndex();
				if (order >= 0) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					NodeConfigCustomInfoResponse modifyInfo = m_infoList.getFilterItem();
					String beforeId = modifyInfo.getSettingCustomId();
					NodeCustomInfoCreateDialog dialog = new NodeCustomInfoCreateDialog(shell, modifyInfo,
							m_infoList.getCustomIdList(), false);
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_infoList.getNodeConfigCustomInfoMap().remove(beforeId);
						m_infoList.getNodeConfigCustomInfoMap().put(dialog.getCustomId(), dialog.getInputData());
						m_infoList.setSelection();
						m_infoList.update();
					}
				} else {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 削除ボタン
		this.m_buttonDelete = this.createButton(buttonComposite, Messages.getString("delete"));
		this.m_buttonDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NodeConfigCustomInfoResponse deleteInfo = m_infoList.getFilterItem();

				if (deleteInfo != null) {
					String deleteId = m_infoList.getFilterItem().getSettingCustomId();
					if (deleteId == null) {
						deleteId = "";
					}

					String[] args = { deleteId };
					if (MessageDialog.openConfirm(null, Messages.getString("confirmed"),
							Messages.getString("message.monitor.31", args))) {
						m_infoList.getNodeConfigCustomInfoMap().remove(deleteId);
						m_infoList.update();
					}
				} else {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// コピーボタン
		this.m_buttonCopy = this.createButton(buttonComposite, Messages.getString("copy"));
		this.m_buttonCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_infoList.getTableViewer().getTable().getSelectionIndex();
				if (order >= 0) {

					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					NodeConfigCustomInfoResponse copyInfo = m_infoList.getFilterItem();
					NodeCustomInfoCreateDialog dialog = new NodeCustomInfoCreateDialog(shell, copyInfo,
							m_infoList.getCustomIdList(), true);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = m_infoList.getTableViewer().getTable();
						int selectIndex = table.getSelectionIndex();
						m_infoList.getNodeConfigCustomInfoMap().put(dialog.getCustomId(), dialog.getInputData());
						m_infoList.update();
						table.setSelection(selectIndex);
					}
				} else {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info
	 *            設定値として用いる監視情報
	 */
	public void setInputData(NodeConfigSettingInfoResponse info) {

		this.m_infoList.setInputData(info);
		// 必須項目を明示
		this.update();
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info
	 *            入力値を設定する監視情報
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.monitor.run.composite.StringValueListComposite#createInputData(MonitorInfo)
	 */
	public ValidateResult createInputData(NodeConfigSettingInfoResponse info) {

		// 文字列監視判定情報
		ValidateResult validateResult = m_infoList.createInputData(info);
		if (validateResult != null) {
			return validateResult;
		}

		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_infoList.setEnabled(enabled);
		this.m_buttonAdd.setEnabled(enabled);
		this.m_buttonModify.setEnabled(enabled);
		this.m_buttonDelete.setEnabled(enabled);
		this.m_buttonCopy.setEnabled(enabled);
	}

	/**
	 * ボタンを返します。
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
		gridData.horizontalSpan = 1;
		gridData.widthHint = 90;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id
	 *            ID
	 * @param message
	 *            メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	/**
	 * 入力したユーザ任意情報を取得します。
	 */
	public ArrayList<NodeConfigCustomInfoResponse> getNodeConfigCustomInfoList() {
		ArrayList<NodeConfigCustomInfoResponse> returnList = new ArrayList<NodeConfigCustomInfoResponse>();
		returnList.addAll(this.m_infoList.getNodeConfigCustomInfoMap().values());
		return returnList;
	}

	/**
	 * 有効なユーザ任意情報が1件以上存在するかチェックします。
	 */
	public boolean isValid() {
		if (this.m_infoList.getNodeConfigCustomInfoMap() == null
				|| this.m_infoList.getNodeConfigCustomInfoMap().isEmpty()) {
			return false;
		}

		boolean valid = false;
		for (NodeConfigCustomInfoResponse customInfo : this.m_infoList.getNodeConfigCustomInfoMap().values()) {
			if (customInfo.getValidFlg()) {
				valid = true;
				break;
			}
		}
		return valid;
	}
}
