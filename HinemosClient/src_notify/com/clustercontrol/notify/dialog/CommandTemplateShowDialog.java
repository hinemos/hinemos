/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.CommandTemplateResponse;

import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.util.Messages;

/**
 * コマンド通知テンプレートダイアログクラス<BR>
 *
 */
public class CommandTemplateShowDialog extends CommonDialog {
	/** カラム数（項目） */
	private static final int WIDTH_LABEL = 5;

	/** カラム数（設定） */
	private static final int WIDTH_TEXT = 10;
	
	/** manager name */
	private String managerName;
	/** 表示するコマンド通知テンプレートの情報 */
	private CommandTemplateResponse commandTemplate;

	private TextWithParameterComposite txtManager;
	private TextWithParameterComposite txtTemplateId;
	private TextWithParameterComposite txtDescription;
	private TextWithParameterComposite txtOwnerRoleId;
	private TextWithParameterComposite txtCommand;
	
	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public CommandTemplateShowDialog(Shell parent, String managerName, CommandTemplateResponse commandTemplate) {
		super(parent);
		this.managerName = managerName;
		this.commandTemplate = commandTemplate;
	}

	// ----- instance メソッド ----- //

	/**
	 * 親のクラスから呼ばれ、コマンド通知テンプレートのダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 */
	protected void customizeDialog(Composite parent) {
		final Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.command.template"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * コマンド通知テンプレート
		 */

		// ラベル（マネージャID）
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("word.manager"));

		txtManager = new TextWithParameterComposite(parent, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtManager.setLayoutData(gridData);
		txtManager.setEnabled(false);

		// ラベル（テンプレートID）
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("command.template.id"));

		txtTemplateId = new TextWithParameterComposite(parent, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtTemplateId.setLayoutData(gridData);
		txtTemplateId.setEnabled(false);

		// ラベル（説明）
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description"));

		txtDescription = new TextWithParameterComposite(parent, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtDescription.setLayoutData(gridData);
		txtDescription.setEnabled(false);

		// ラベル（オーナーロールID）
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("owner.role.id"));

		txtOwnerRoleId = new TextWithParameterComposite(parent, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtOwnerRoleId.setLayoutData(gridData);
		txtOwnerRoleId.setEnabled(false);

		// ラベル（コマンド）
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("command"));

		txtCommand = new TextWithParameterComposite(parent, SWT.BORDER | SWT.MULTI);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		txtCommand.setLayoutData(gridData);
		txtCommand.setEnabled(false);

		setInputData(managerName, commandTemplate);
		// サイズを最適化
		shell.pack();
		shell.setSize(new Point(540, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}
 
	private void setInputData(String managerName, CommandTemplateResponse commandTemplate) {
		txtManager.setText(managerName);
		txtTemplateId.setText(commandTemplate.getCommandTemplateId());
		txtDescription.setText(commandTemplate.getDescription());
		txtOwnerRoleId.setText(commandTemplate.getOwnerRoleId());
		txtCommand.setText(commandTemplate.getCommand());
	}

	/**
	 * ボタンを生成します。<BR>
	 *
	 * @param parent ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		this.createButton(parent, IDialogConstants.CANCEL_ID,
				Messages.getString("ok"), false);
	}
}
