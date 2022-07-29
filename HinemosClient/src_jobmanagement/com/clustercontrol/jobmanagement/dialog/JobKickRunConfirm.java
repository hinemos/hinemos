/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openapitools.client.model.JobKickResponse;
import org.openapitools.client.model.JobRuntimeParamResponse;
import org.openapitools.client.model.JobRuntimeParamRunRequest;
import org.openapitools.client.model.RunJobRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetJobKick;
import com.clustercontrol.jobmanagement.composite.JobKickInputParamComposite;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ実行契機 直接実行確認ダイアログクラスです。
 *
 * @version 5.1.0
 */
public class JobKickRunConfirm extends CommonDialog {

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

	/** マネージャ名 */
	private String m_managerName = null;

	/** 実行契機ID */
	private String m_jobkickId = null;

	/** ランタイムジョブ変数入力用Compositeリスト */
	private List<JobKickInputParamComposite> m_jobKickInputParamCompositeList 
		= new ArrayList<>();

	/** ExpandBar */
	private ExpandBar m_expandBar;
	private ExpandItem m_expandItem;
	private Group m_runJobParamGroup;
	private Point m_size;
	private static final Image ICON_EXPAND = AbstractUIPlugin.imageDescriptorFromPlugin(
			ClusterControlPlugin.getPluginId(), 
			"$nl$/icons/lrun_test_obj.gif").createImage();
	private static final int EXPANDBAR_HEIGHT = 26;
	private static final int EXPANDITEM_HEIGHT = 86;
	private static final int SCROLL_WIDTH = 20;

	/**
	 * コンストラクタ
	 * 作成時
	 * @param parent 親シェル
	 */
	public JobKickRunConfirm(Shell parent, String managerName, String jobkickId) {
		super(parent);
		this.m_managerName = managerName;
		this.m_jobkickId = jobkickId;
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
		layout.wrap = false;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		/*
		 * テスト実行モード選択ダイアログ
		 */

		// メッセージ（ラベル）
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
				m_expandBar.setLayoutData(new RowData(SWT.DEFAULT, EXPANDITEM_HEIGHT + EXPANDBAR_HEIGHT));
				m_shell.setSize(m_size.x, m_size.y + 86);
				Point areaSize = getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				getScrolledComposite().setMinSize(areaSize.x, areaSize.y);
				getScrolledComposite().setMinWidth(areaSize.x - SCROLL_WIDTH);
			}
			
			@Override
			public void itemCollapsed(ExpandEvent e) {
				m_expandBar.setLayoutData(new RowData(SWT.DEFAULT, EXPANDBAR_HEIGHT));
				m_shell.setSize(m_size);
				Point areaSize = getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				getScrolledComposite().setMinSize(areaSize.x, areaSize.y - (EXPANDITEM_HEIGHT + EXPANDBAR_HEIGHT));
				getScrolledComposite().setMinWidth(areaSize.x - SCROLL_WIDTH);
			}
		});

		m_expandItem = new ExpandItem(m_expandBar, SWT.NONE);
		m_expandItem.setImage(ICON_EXPAND);
		m_expandItem.setExpanded(false);
		m_expandItem.setText(Messages.getString("message.job.118"));

		Composite runJobComposite = new Composite(m_expandBar, SWT.NONE);
		m_expandItem.setControl(runJobComposite);
		runJobComposite.setLayoutData(new RowData());
		runJobComposite.setLayout(new GridLayout(2, false));

		//ジョブ待ち条件（時刻）（チェックボックス）
		btnJobWaitTime = new Button(runJobComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "btnJobWaitTime", btnJobWaitTime);
		btnJobWaitTime.setText(Messages.getString("message.job.119"));
		btnJobWaitTime.setLayoutData(new GridData(250, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)btnJobWaitTime.getLayoutData()).horizontalSpan = 2;
		btnJobWaitTime.setSelection(false);

		//ジョブ待ち条件（ジョブセッション開始後の時間）（チェックボックス）
		btnJobWaitMinute = new Button(runJobComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "btnJobWaitMinute", btnJobWaitMinute);
		btnJobWaitMinute.setText(Messages.getString("message.job.120"));
		btnJobWaitMinute.setLayoutData(new GridData(400, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)btnJobWaitMinute.getLayoutData()).horizontalSpan = 2;
		btnJobWaitMinute.setSelection(false);

		//起動コマンド置換（チェックボックス）
		btnJobCommand = new Button(runJobComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "btnJobCommand", btnJobCommand);
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

		//起動コマンド置換（テキスト）
		textJobCommandText = new Text(runJobComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "textJobCommandText", textJobCommandText);
		textJobCommandText.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		textJobCommandText.setEditable(false);
		this.textJobCommandText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		JobDialogUtil.getSeparator(parent);

		// ExpandBarサイズ変更
		m_expandItem.setHeight(m_expandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

		// ランタイムジョブ変数（Group）
		m_runJobParamGroup = new Group(parent, SWT.NONE);
		m_runJobParamGroup.setText(Messages.getString("jobkick.runtimejob.param"));
		m_runJobParamGroup.setLayoutData(new RowData());
		m_runJobParamGroup.setLayout(new RowLayout(SWT.VERTICAL));

		// ラインタイムジョブ変数入力用Composite生成
		createDetailComposite(m_runJobParamGroup);

		// 更新処理
		update();

		// ダイアログエリアの調整
		adjustDialog();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		m_shell.pack();
		m_shell.setSize(new Point(650, (m_shell.getSize().y>500)?500:m_shell.getSize().y));
		m_size = m_shell.getSize();
		// 画面中央に配置
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);
	}

	/**
	 * ランタイムジョブ変数入力用Composite生成
	 * 
	 * @param parent 親Composite
	 */
	private void createDetailComposite(Composite parent) {

		// ジョブ実行契機よりランタイムジョブ変数情報を取得
		JobKickResponse jobKick = GetJobKick.getJobKick(this.m_managerName, this.m_jobkickId);
		if (jobKick != null
				&& jobKick.getJobRuntimeParamList() != null) {
			for (JobRuntimeParamResponse jobRuntimeParam : jobKick.getJobRuntimeParamList()) {
				JobKickInputParamComposite composite 
					= new JobKickInputParamComposite(parent, SWT.NONE, jobRuntimeParam);
				composite.setLayoutData(new RowData());
				this.m_jobKickInputParamCompositeList.add(composite);
			}
		}
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
		
		// ラインタイムジョブ変数入力用Composite
		for (JobKickInputParamComposite paramComposite 
				: this.m_jobKickInputParamCompositeList) {
			paramComposite.update();
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

		// ランタイムジョブ変数用Composite
		for (JobKickInputParamComposite paramComposite 
				: this.m_jobKickInputParamCompositeList) {
			result = paramComposite.validateInputParam();
			if (result != null) {
				return result;
			}
		}
		// ランタイムジョブ変数設定
		setInputParam();

		return result;
	}

	/**
	 * 入力データ更新処理
	 *
	 */
	private void setTriggerInfo(){
		this.m_trigger = new RunJobRequest();

		//条件関係取得
		if (this.btnJobWaitTime.getSelection()) {
			this.m_trigger.setJobWaitTime(true);
		} else {
			this.m_trigger.setJobWaitTime(false);
		}
		if (this.btnJobWaitMinute.getSelection()) {
			this.m_trigger.setJobWaitMinute(true);
		} else {
			this.m_trigger.setJobWaitMinute(false);
		}
		if (this.btnJobCommand.getSelection()) {
			this.m_trigger.setJobCommand(true);
		} else {
			this.m_trigger.setJobCommand(false);
		}
		if (this.textJobCommandText.getText().length() > 0) {
			this.m_trigger.setJobCommandText(this.textJobCommandText.getText());
		} else{
			this.m_trigger.setJobCommandText("");
		}
	}

	/**
	 * 入力データ更新処理（ラインタイムジョブ変数）
	 * 
	 */
	private void setInputParam() {
		if (this.m_jobKickInputParamCompositeList != null) {
			for (JobKickInputParamComposite paramComposite 
					: this.m_jobKickInputParamCompositeList){
				if (paramComposite.getParamId() != null 
						&& paramComposite.getValue() != null) {
					JobRuntimeParamRunRequest jobRuntimeParam = new JobRuntimeParamRunRequest();
					jobRuntimeParam.setParamId(paramComposite.getParamId());
					jobRuntimeParam.setValue(paramComposite.getValue());
					this.m_trigger.getJobRuntimeParamList().add(jobRuntimeParam);
				}
			}
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
