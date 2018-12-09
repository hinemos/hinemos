/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.run.bean.MonitorTypeMessage;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * ログ件数監視[集計]ダイアログクラス<BR>
 *
 * @version 6.1.0
 */
public class SummaryLogcountDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( SummaryLogcountDialog.class );

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 4;

	/** カラム数（値）。 */
	public static final int WIDTH_VALUE = 2;

	/** 監視設定ID */
	private Text m_textMonitorInfo = null;

	/** 収集開始日時 */
	private Long m_startDate = -1l;
	private Text m_startDateText = null;
	private Button m_startDateButton = null;

	/** マネージャ名 */
	private String m_managerName = "";

	/** 監視設定ID */
	private String m_monitorId = "";

	private Shell m_shell = null;

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public SummaryLogcountDialog(Shell parent, String managerName, String monitorId) {
		super(parent);
		this.m_monitorId = monitorId;
		this.m_managerName = managerName;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();

		// タイトル
		m_shell.setText(Messages.getString("dialog.monitor.logcount.summary"));

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
		 * 監視設定ID
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.id") + " : ");
		// ラベル
		this.m_textMonitorInfo = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_textMonitorInfo", m_textMonitorInfo);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textMonitorInfo.setLayoutData(gridData);

		/*
		 * 収集開始日時
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("collect.start.time") + " : ");
		//テキスト
		m_startDateText = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "timefrom", m_startDateText);
		gridData = new GridData();
		gridData.horizontalSpan = 9;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_startDateText.setLayoutData(gridData);
		//日時ダイアログからの入力しか受け付けません
		m_startDateText.setEditable(false);
		m_startDateText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 追加ボタン
		m_startDateButton = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "timefrom", m_startDateButton);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_startDateButton.setLayoutData(gridData);
		m_startDateButton.setText(Messages.getString("calendar.button"));
		m_startDateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(m_shell);
				if (m_startDateText.getText().length() > 0) {
					Date date = new Date(m_startDate);
					dialog.setDate(date);
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					//取得した日時をLong型で保持
					m_startDate = dialog.getDate().getTime();
					//ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					m_startDateText.setText(tmp);
					update();
				}
			}
		});

		// ダイアログを調整
		this.adjustDialog();
		//ダイアログにカレンダ詳細情報反映
		this.reflectData();
		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;
		String[] args = { m_monitorId, m_managerName };
		// 確認ダイアログ
		if (!MessageDialog.openQuestion(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.monitor.96", args))) {
			return result;
		}
		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(m_managerName);
			wrapper.runSummaryLogcount(m_monitorId, m_startDate);
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.monitor.95", args));
			result = true;
		} catch (InvalidRole_Exception e) { 
			MessageDialog.openInformation(null, Messages.getString("message"), 
					Messages.getString("message.accesscontrol.16")); 
		} catch (Exception e) { 
			m_log.warn("run(), " + e.getMessage(), e); 
			MessageDialog.openError( 
					null, 
					Messages.getString("failed"), 
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage())); 
		}
		return result;
	}

	/**
	 * ダイアログに監視設定情報を反映します。
	 *
	 * @param detailList
	 * @throws HinemosUnknown 
	 */
	private void reflectData() {

		// 監視設定
		MonitorInfo monitorInfo = null;
		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(this.m_managerName);
			monitorInfo = wrapper.getMonitor(this.m_monitorId);
		} catch (com.clustercontrol.ws.monitor.InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("reflectData() getMonitor, " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		this.m_textMonitorInfo.setText(getMonitorIdLabel(monitorInfo));

		//カレンダ情報取得
		m_startDate = null;
		m_startDateText.setText("");

		this.update();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		m_shell.pack();
		m_shell.setSize(new Point(600, m_shell.getSize().y));

		// 画面中央に配置
		Display calAdjustDisplay = m_shell.getDisplay();
		m_shell.setLocation((calAdjustDisplay.getBounds().width - m_shell.getSize().x) / 2,
				(calAdjustDisplay.getBounds().height - m_shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		// 有効期間（開始）のインデックス
		if("".equals(this.m_startDateText.getText())){
			this.m_startDateText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_startDateText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 無効な入力値をチェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see #createInputData()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		if(m_startDateText.getText().length() > 0){
			Date dateTimeFrom;
			try {
				dateTimeFrom = (TimezoneUtil.getSimpleDateFormat()).parse(m_startDateText.getText());
				m_startDate = dateTimeFrom.getTime();
			} catch (ParseException e) {
				m_log.warn("createCalendarInfo : " + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * 収集開始日時を返す
	 * @return 収集開始日時
	 */
	public Long getStartDate() {
		return this.m_startDate;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 監視設定文字列を取得する
	 * 
	 * @param monitorInfo 監視情報
	 * @return 監視設定文字列
	 */
	private String getMonitorIdLabel(MonitorInfo monitorInfo) {
		String pluginName = "";
		if (monitorInfo == null || monitorInfo.getMonitorTypeId() == null) {
			m_log.warn("monitorInfo=" + monitorInfo);
			pluginName = "";
			return null;
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGCOUNT)) {
			pluginName = Messages.getString("logcount.monitor");
		} else {
			pluginName = monitorInfo.getMonitorTypeId();
		}

		return String.format("%s (%s[%s])", 
				monitorInfo.getMonitorId(),
				pluginName, 
				MonitorTypeMessage.typeToString(monitorInfo.getMonitorType()));
	}
}
