/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openapitools.client.model.RunJobRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

/**
 * ジョブ実行確認ダイアログクラスです。
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class JobRunConfirm extends CommonDialog {

	/** ダイアログのタイトル */
	private String m_title = "";
	private String m_MessageText = "";

	/** ジョブ変数情報 */
	private RunJobRequest m_trigger = null;

	/** ジョブの待ち条件（時刻）有効無効チェックボタン */
	private Button btnJobWaitTime = null;
	/** ジョブの待ち条件（セッション開始後）有効無効チェックボタン */
	private Button btnJobWaitMinute = null;
	/** ジョブ起動コマンド置換有無　チェックボタン */
	private Button btnJobCommand = null;
	/** ジョブ起動コマンド置換有無　置き換え文字列 */
	private Text textJobCommandText = null;

	/** シェル */
	private Shell m_shell = null;

	/** ExpandBar */
	private ExpandBar m_expandBar;
	private ExpandItem m_expandItem;
	private static final Image ICON_EXPAND = AbstractUIPlugin.imageDescriptorFromPlugin(
			ClusterControlPlugin.getPluginId(), 
			"$nl$/icons/lrun_test_obj.gif").createImage();
	private static final int EXPANDBAR_HEIGHT = 26;
	private static final int EXPANDITEM_HEIGHT = 86;

	/**
	 * コンストラクタ
	 * 作成時
	 * @param parent 親シェル
	 */
	public JobRunConfirm(Shell parent) {
		super(parent);

	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();

		Label label = null;

		parent.getShell().setText(
				Messages.getString("confirmed"));
		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		/*
		 * テスト実行モード選択ダイアログ
		 */

		//メッセージ
		label = new Label(parent, SWT.LEFT | SWT.WRAP);
		label.setText(m_MessageText);
		label.setLayoutData(new RowData(590, SWT.DEFAULT));

		JobDialogUtil.getSeparator(parent);

		// テスト実行
		m_expandBar = new ExpandBar(parent, SWT.NONE);
		m_expandBar.setSpacing(1);
		m_expandBar.setBackground(ColorConstantsWrapper.lightGray());
		m_expandBar.setLayoutData(new RowData(500, SWT.DEFAULT));
		m_expandBar.addExpandListener(new ExpandListener() {
			@Override
			public void itemExpanded(ExpandEvent e) {
				m_expandItem.getControl().computeSize(SWT.DEFAULT, EXPANDITEM_HEIGHT);
				m_expandBar.setLayoutData(new RowData(SWT.DEFAULT, EXPANDITEM_HEIGHT + EXPANDBAR_HEIGHT));
				m_shell.pack();
				m_shell.setSize(new Point(650, m_shell.getSize().y));
			}
			
			@Override
			public void itemCollapsed(ExpandEvent e) {
				m_expandItem.getControl().computeSize(SWT.DEFAULT, 0);
				m_expandBar.setLayoutData(new RowData(SWT.DEFAULT, EXPANDBAR_HEIGHT));
				m_shell.pack();
				m_shell.setSize(new Point(650, m_shell.getSize().y));
			}
		});

		m_expandItem = new ExpandItem(m_expandBar, SWT.NONE);
		m_expandItem.setImage(ICON_EXPAND);
		m_expandItem.setExpanded(false);
		m_expandItem.setText(Messages.getString("message.job.118"));

		Composite selectionComposite = new Composite(m_expandBar, SWT.NONE);
		m_expandItem.setControl(selectionComposite);
		selectionComposite.setLayoutData(new RowData(500, 86));
		selectionComposite.setLayout(new GridLayout(2, false));

		//ジョブ待ち条件（時刻）
		btnJobWaitTime = new Button(selectionComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "btnJobWaitTime", btnJobWaitTime);
		btnJobWaitTime.setText(Messages.getString("message.job.119"));
		btnJobWaitTime.setLayoutData(new GridData(350, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)btnJobWaitTime.getLayoutData()).horizontalSpan = 2;
		btnJobWaitTime.setSelection(false);

		//ジョブ待ち条件（ジョブセッション開始後の時間）
		btnJobWaitMinute = new Button(selectionComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "btnJobWaitMinute", btnJobWaitMinute);
		btnJobWaitMinute.setText(Messages.getString("message.job.120"));
		btnJobWaitMinute.setLayoutData(new GridData(500, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)btnJobWaitMinute.getLayoutData()).horizontalSpan = 2;
		btnJobWaitMinute.setSelection(false);

		//起動コマンド置換
		btnJobCommand = new Button(selectionComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "jobcommand", btnJobCommand);
		btnJobCommand.setText(Messages.getString("message.job.121"));
		btnJobCommand.setLayoutData(new GridData(220, SizeConstant.SIZE_BUTTON_HEIGHT));
		btnJobCommand.setSelection(false);
		btnJobCommand.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					textJobCommandText.setEditable(true);
				} else {
					textJobCommandText.setEditable(false);
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// テキスト
		textJobCommandText = new Text(selectionComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "textJobCommandText", textJobCommandText);
		textJobCommandText.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		textJobCommandText.setEditable(false);
		this.textJobCommandText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// ExpandBarサイズ変更
		m_expandItem.setHeight(m_expandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

		// ダイアログを調整
		this.adjustDialog();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		m_shell.pack();
		m_shell.setSize(new Point(650, m_shell.getSize().y));

		// 画面中央に配置
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);
	}
	/**
	 * 更新処理
	 *
	 */
	private void update(){
		if (btnJobCommand.getSelection()) {
			if("".equals(this.textJobCommandText.getText())){
				this.textJobCommandText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				this.textJobCommandText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			this.textJobCommandText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	/**
	 * ダイアログタイトルを返します。
	 *
	 * @return ダイアログタイトル
	 */
	public String getTitleText() {
		return m_title;
	}

	/**
	 * ダイアログタイトルを設定します。
	 *
	 * @param title ダイアログタイトル
	 */
	public void setTitleText(String title) {
		m_title = title;
	}

	/**
	 * メッセージを返します。
	 *
	 * @return メッセージ
	 */
	public String getMessageText() {
		return m_MessageText;
	}

	/**
	 * メッセージを設定します。
	 *
	 * @param messagetext メッセージ
	 */
	public void setMessageText(String messagetext) {
		m_MessageText = messagetext;
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("run");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		setTriggerInfo();
		//起動コマンド取得
		if (btnJobCommand.getSelection()) {
			if (textJobCommandText.getText().length() > 0) {
			} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.3"));
			}
		}
		return result;
	}
	/**
	 * 入力データ更新処理
	 *
	 */
	private void setTriggerInfo(){
		this.m_trigger = new RunJobRequest();

		//条件関係取得
		if (btnJobWaitTime.getSelection()) {
			m_trigger.setJobWaitTime(true);
		} else {
			m_trigger.setJobWaitTime(false);
		}
		if (btnJobWaitMinute.getSelection()) {
			m_trigger.setJobWaitMinute(true);
		} else {
			m_trigger.setJobWaitMinute(false);
		}
		if (btnJobCommand.getSelection()) {
			m_trigger.setJobCommand(true);
		} else {
			m_trigger.setJobCommand(false);
		}
		if (textJobCommandText.getText().length() > 0) {
			m_trigger.setJobCommandText(textJobCommandText.getText());
		} else{
			m_trigger.setJobCommandText("");
		}
	}
	/**
	 * 入力情報を設定します。
	 *
	 * @param info 入力情報
	 */
	public void setInputData(RunJobRequest info) {
		m_trigger = info;
	}

	/**
	 * 入力情報を返します。
	 *
	 * @return 入力情報
	 */
	public RunJobRequest getInputData() {
		return m_trigger;
	}
}
