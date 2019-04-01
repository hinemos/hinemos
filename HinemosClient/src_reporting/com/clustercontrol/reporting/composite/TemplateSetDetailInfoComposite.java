/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite;

import java.util.ArrayList;
import java.util.Iterator;

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
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.reporting.dialog.TemplateSetDetailDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.reporting.TemplateSetDetailInfo;

/**
 * テンプレートセット詳細情報グループのコンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>値取得の成功時</dd>
 *  <dd>　テンプレートセット詳細情報一覧コンポジット</dd>
 *  <dd>　「追加」ボタン</dd>
 *  <dd>　「変更」ボタン</dd>
 *  <dd>　「削除」ボタン</dd>
 *  <dd>　「コピー」ボタン</dd>
 *  <dd>　「上へ」ボタン</dd>
 *  <dd>　「下へ」ボタン</dd>
 *  <dd>値取得の失敗時</dd>
 *  <dd>　「重要度」 コンボボックス</dd>
 * </dl>
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateSetDetailInfoComposite extends Composite {

	/** テンプレートセット詳細情報一覧 コンポジット。 */
	private TemplateSetDetailListComposite templateSetDetailListComposite = null;

	/** 追加 ボタン。 */
	private Button templateSetDetailInfoAddButton = null;

	/** 変更 ボタン。 */
	private Button templateSetDetailInfoModifyButton = null;

	/** 削除 ボタン。 */
	private Button templateSetDetailInfoDeleteButton = null;

	/** コピー ボタン。 */
	private Button templateSetDetailInfoCopyButton = null;

	/** 上へ ボタン。 */
	private Button templateSetDetailInfoUpButton = null;

	/** 下へ ボタン。 */
	private Button templateSetDetailInfoDownButton = null;

	/** マネージャ名 */
	private String m_managerName = null;
	
	public void setManagerName(String managerName) {
		m_managerName = managerName;
		templateSetDetailListComposite.setManagerName(managerName);
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 * @param tableDefine テンプレートセット詳細情報一覧のテーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.run.action.GetStringFilterTableDefine
	 * @see #initialize(ArrayList)
	 */
	public TemplateSetDetailInfoComposite(Composite parent, int style, String managerName){
		super(parent, style);
		this.m_managerName = managerName;
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(){

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * テンプレートセット詳細情報一覧
		 */
		this.templateSetDetailListComposite = new TemplateSetDetailListComposite(this, SWT.BORDER, this.m_managerName);
		WidgetTestUtil.setTestId(this, "list", templateSetDetailListComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 13;
		this.templateSetDetailListComposite.setLayoutData(gridData);
		/*
		 * 操作ボタン
		 */
		Composite templateSetDetailInfoButtonComposite = new Composite(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "button", templateSetDetailInfoButtonComposite);
		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		templateSetDetailInfoButtonComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		templateSetDetailInfoButtonComposite.setLayoutData(gridData);

		// 追加ボタン
		this.templateSetDetailInfoAddButton = this.createButton(templateSetDetailInfoButtonComposite, Messages.getString("add"));
		WidgetTestUtil.setTestId(this, "add", templateSetDetailInfoAddButton);
		this.templateSetDetailInfoAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				TemplateSetDetailDialog dialog = new TemplateSetDetailDialog(shell, templateSetDetailListComposite.getManagerName(), templateSetDetailListComposite.getOwnerRoleId());
				if (dialog.open() == IDialogConstants.OK_ID) {
					templateSetDetailListComposite.getDetailList().add(dialog.getInputData());
					templateSetDetailListComposite.update();
				}
			}
		});

		// 変更ボタン
		this.templateSetDetailInfoModifyButton = this.createButton(templateSetDetailInfoButtonComposite, Messages.getString("modify"));
		WidgetTestUtil.setTestId(this, "modify", templateSetDetailInfoModifyButton);
		this.templateSetDetailInfoModifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = templateSetDetailListComposite.getSelection();
				if (order != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					TemplateSetDetailDialog dialog = new TemplateSetDetailDialog(shell, templateSetDetailListComposite.getManagerName(), templateSetDetailListComposite.getDetailList().get(order - 1), templateSetDetailListComposite.getOwnerRoleId());
					if (dialog.open() == IDialogConstants.OK_ID) {
						templateSetDetailListComposite.getDetailList().remove(templateSetDetailListComposite.getDetailList().get(order - 1));
						templateSetDetailListComposite.getDetailList().add(order - 1,dialog.getInputData());
						templateSetDetailListComposite.setSelection();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 削除ボタン
		this.templateSetDetailInfoDeleteButton = this.createButton(templateSetDetailInfoButtonComposite, Messages.getString("delete"));
		WidgetTestUtil.setTestId(this, "delete", templateSetDetailInfoDeleteButton);
		this.templateSetDetailInfoDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = templateSetDetailListComposite.getSelection();
				if (order != null) {
					templateSetDetailListComposite.getDetailList().remove(order - 1);
					templateSetDetailListComposite.update();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// コピーボタン
		this.templateSetDetailInfoCopyButton = this.createButton(templateSetDetailInfoButtonComposite, Messages.getString("copy"));
		WidgetTestUtil.setTestId(this, "copy", templateSetDetailInfoCopyButton);
		this.templateSetDetailInfoCopyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = templateSetDetailListComposite.getSelection();
				if (order != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					TemplateSetDetailDialog dialog = new TemplateSetDetailDialog(shell, templateSetDetailListComposite.getManagerName(), templateSetDetailListComposite.getDetailList().get(order - 1), templateSetDetailListComposite.getOwnerRoleId());
					if (dialog.open() == IDialogConstants.OK_ID) {
						templateSetDetailListComposite.getDetailList().add(dialog.getInputData());
						templateSetDetailListComposite.setSelection();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 上へボタン
		this.templateSetDetailInfoUpButton = this.createButton(templateSetDetailInfoButtonComposite, Messages.getString("up"));
		WidgetTestUtil.setTestId(this, "up", templateSetDetailInfoUpButton);
		this.templateSetDetailInfoUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = templateSetDetailListComposite.getSelection();
				if (order != null) {
					templateSetDetailListComposite.up();
					templateSetDetailListComposite.update();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 下へボタン
		this.templateSetDetailInfoDownButton = this.createButton(templateSetDetailInfoButtonComposite, Messages.getString("down"));
		WidgetTestUtil.setTestId(this, "down", templateSetDetailInfoDownButton);
		this.templateSetDetailInfoDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = templateSetDetailListComposite.getSelection();
				if (order != null) {
					templateSetDetailListComposite.down();
					templateSetDetailListComposite.update();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});
	}

	/**
	 *
	 * @return
	 */
	public ArrayList<TemplateSetDetailInfo> getDetailList(){
		return this.templateSetDetailListComposite.getDetailList();
	}
	/**
	 * カレンダ詳細情報をコンポジット内リストに反映させる
	 * @param detailList
	 */
	public void setDetailList(ArrayList<TemplateSetDetailInfo> detailList){
		if (detailList != null) {
			this.templateSetDetailListComposite.setDetailList(detailList);
		}
		this.update();
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.templateSetDetailListComposite.setEnabled(enabled);
		this.templateSetDetailInfoAddButton.setEnabled(enabled);
		this.templateSetDetailInfoModifyButton.setEnabled(enabled);
		this.templateSetDetailInfoDeleteButton.setEnabled(enabled);
		this.templateSetDetailInfoCopyButton.setEnabled(enabled);
		this.templateSetDetailInfoUpButton.setEnabled(enabled);
		this.templateSetDetailInfoDownButton.setEnabled(enabled);
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param label ボタンに表示するテキスト
	 * @return ボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button templateSetDetailInfoCommonButton = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, templateSetDetailInfoCommonButton);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		templateSetDetailInfoCommonButton.setLayoutData(gridData);

		templateSetDetailInfoCommonButton.setText(label);

		return templateSetDetailInfoCommonButton;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.templateSetDetailListComposite.setOwnerRoleId(ownerRoleId);
	}

	public void changeOwnerRoleId(String ownerRoleId) {

		if (ownerRoleId == null
				|| !ownerRoleId.equals(this.templateSetDetailListComposite.getOwnerRoleId())) {
			
			Iterator<TemplateSetDetailInfo> iter = templateSetDetailListComposite.getDetailList().iterator();
			while (iter.hasNext()) {
				TemplateSetDetailInfo composite = iter.next();
				composite.setTemplateSetId(null);
			}
		}
		setOwnerRoleId(ownerRoleId);
	}
}
