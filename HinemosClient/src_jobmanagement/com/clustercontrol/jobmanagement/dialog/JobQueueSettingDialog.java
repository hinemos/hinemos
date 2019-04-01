/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.jobmanagement.bean.JobQueueConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobQueueSetting;

/**
 * ジョブ同時実行制御キューの設定ダイアログです。
 *
 * @version 6.2.0
 */
public class JobQueueSettingDialog extends CommonDialog {
//	private static Log log = LogFactory.getLog(JobQueueSettingDialog.class);

	private JobQueueSetting setting;

	private EditMode mode;
	public enum EditMode {
		CREATE, MODIFY, READONLY,
	};
	
	private String managerName;

	private Predicate<String> action;

	// 入力欄
	private ManagerListComposite managerComposite;
	private Text queueIdText;
	private Text queueNameText;
	private Text concurrencytext;
	private RoleIdListComposite ownerRoleIdComposite;

	/**
	 * コンストラクタです。
	 * 
	 * @param parent 親ウィンドウのShell。
	 * @param mode 編集モード。入力可能な項目が変化します。
	 * @param setting 入力欄の初期値を提供し、入力された値を受け取るオブジェクト。
	 * @param managerName マネージャの初期値です。
	 * @param action OKボタン押下後の処理です。引数は選択されたマネージャです。
	 *               この処理の前にsettingオブジェクトは入力された値で更新されます。
	 *               この処理がtrueを返した場合はダイアログを閉じ、falseを返した場合は閉じません。
	 */
	public JobQueueSettingDialog(Shell parent, EditMode mode, JobQueueSetting setting, String managerName,
			Predicate<String> action) {
		super(parent);
		this.mode = mode;
		this.setting = setting;
		this.managerName = managerName;
		this.action = action;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		Label label;
		GridData grid;
		int labelWidth = 140;

		// タイトル
		parent.getShell().setText(Messages.get("dialog.jobqueue.setting"));

		// ベースレイアウト
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		// マネージャ（ラベル）
		label = new Label(comp, SWT.LEFT);
		label.setText(Messages.get("facility.manager") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// マネージャ（テキスト）
		managerComposite = new ManagerListComposite(comp, SWT.NONE, true);
		WidgetTestUtil.setTestId(this, "managerComposite", managerComposite);
		grid = new GridData();
		grid.widthHint = 227;
		managerComposite.setLayoutData(grid);
		managerComposite.setText(managerName);
		managerComposite.addComboSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String newManagerName = managerComposite.getText();
				// オーナーロールIDの選択肢を更新
				ownerRoleIdComposite.createRoleIdList(newManagerName);
			}
		});

		// キューID（ラベル）
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.get("jobqueue.id") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// キューID（テキスト）
		queueIdText = new Text(comp, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "queueIdText", queueIdText);
		queueIdText.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		queueIdText.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		queueIdText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		queueIdText.setEnabled(true);

		// キュー名（ラベル）
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.get("jobqueue.name") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// キュー名（テキスト）
		queueNameText = new Text(comp, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "queueNameText", queueNameText);
		queueNameText.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		queueNameText.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		queueNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 同時実行可能数（ラベル）
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.get("jobqueue.concurrency") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// 同時実行可能数（テキスト）
		concurrencytext = new Text(comp, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "concurrencytext", concurrencytext);
		concurrencytext.setLayoutData(new GridData(40, SizeConstant.SIZE_TEXT_HEIGHT));
		concurrencytext.addVerifyListener(
				new NumberVerifyListener(JobQueueConstant.CONCURRENCY_MIN, JobQueueConstant.CONCURRENCY_MAX));
		concurrencytext.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		// オーナーロールID（ラベル）
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.get("owner.role.id") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// オーナーロールID（テキスト）
		ownerRoleIdComposite = new RoleIdListComposite(comp, SWT.NONE, managerName, true, Mode.OWNER_ROLE);
		WidgetTestUtil.setTestId(this, "ownerRoleIdComposite", ownerRoleIdComposite);
		grid = new GridData();
		grid.widthHint = 227;
		ownerRoleIdComposite.setLayoutData(grid);

		// 表示調整
		adjustPosition(450);

		// 初期値を表示
		queueIdText.setText(setting.getQueueId());
		queueNameText.setText(setting.getName());
		concurrencytext.setText(Objects.toString(setting.getConcurrency(), ""));
		ownerRoleIdComposite.setText(setting.getOwnerRoleId());

		// 入力可能/不可能を設定
		switch (mode) {
		case CREATE:
			// すべて入力可能
			break;
		case MODIFY:
			managerComposite.setEnabled(false);
			queueIdText.setEnabled(false);
			ownerRoleIdComposite.setEnabled(false);
			break;
		case READONLY:
			managerComposite.setEnabled(false);
			queueIdText.setEnabled(false);
			queueNameText.setEnabled(false);
			concurrencytext.setEnabled(false);
			ownerRoleIdComposite.setEnabled(false);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			break;
		}
		
		update();
	}

	@Override
	protected String getOkButtonText() {
		return Messages.get("register");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.get("cancel");
	}

	@Override
	protected boolean action() {
		setting.setQueueId(queueIdText.getText());
		setting.setName(queueNameText.getText());
		setting.setOwnerRoleId(ownerRoleIdComposite.getText());
		
		String concurrency = concurrencytext.getText();
		if (StringUtils.isEmpty(concurrency)) {
			setting.setConcurrency(null);
		} else {
			setting.setConcurrency(Integer.parseInt(concurrency));
		}

		return action.test(managerComposite.getText());
	}

	private void markRequired(Text text) {
		Color bgColor;
		if (StringUtils.isBlank(text.getText())) {
			bgColor = RequiredFieldColorConstant.COLOR_REQUIRED;
		} else {
			bgColor = RequiredFieldColorConstant.COLOR_UNREQUIRED;
		}
		text.setBackground(bgColor);
	}
	
	private void update() {
		markRequired(queueIdText);
		markRequired(queueNameText);
		markRequired(concurrencytext);
	}
}
