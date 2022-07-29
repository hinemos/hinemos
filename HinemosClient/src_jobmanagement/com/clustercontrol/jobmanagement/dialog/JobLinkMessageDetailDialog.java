/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.JobLinkExpInfoResponse;
import org.openapitools.client.model.JobLinkMessageResponse.PriorityEnum;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.TextAreaDialog;
import com.clustercontrol.jobmanagement.action.GetJobLinkExpTableDefine;
import com.clustercontrol.jobmanagement.composite.JobLinkMessageComposite.JobLinkMessageResponseEx;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 受信ジョブ連携メッセージ詳細ダイアログクラス<BR>
 *
 */
public class JobLinkMessageDetailDialog extends CommonDialog {

	/** 値を保持するオブジェクト。 */
	private JobLinkMessageResponseEx m_info = null;

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param info
	 *            受信ジョブ連携メッセージ情報
	 */
	public JobLinkMessageDetailDialog(Shell parent, JobLinkMessageResponseEx info) {
		super(parent);
		this.m_info = info;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.joblinkmessage.detail"));

		// 変数として利用されるラベル
		Label label = null;

		// レイアウト
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		GridData gridData = new GridData();

		/** マネージャ名 */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("facility.manager") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textManagerName = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textManagerName.setLayoutData(gridData);
		if (m_info.getManagerName() != null) {
			textManagerName.setText(m_info.getManagerName());
		}

		/** ジョブ連携メッセージID */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("joblink.message.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textJoblinkMessageId = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textJoblinkMessageId.setLayoutData(gridData);
		if (m_info.getJoblinkMessageId() != null) {
			textJoblinkMessageId.setText(m_info.getJoblinkMessageId());
		}

		/** 送信元ファシリティID */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("source.facility.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textSrcFacilityId = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textSrcFacilityId.setLayoutData(gridData);
		if (m_info.getFacilityId() != null) {
			textSrcFacilityId.setText(m_info.getFacilityId());
		}

		/** 送信元ファシリティ名 */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("source.scope") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textSrcFacilityName = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textSrcFacilityName.setLayoutData(gridData);
		if (m_info.getFacilityName() != null) {
			textSrcFacilityName.setText(m_info.getFacilityName());
		}

		/** 送信元IPアドレス */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("source.ip.address") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textSrcIpAddress = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textSrcIpAddress.setLayoutData(gridData);
		if (m_info.getIpAddress() != null) {
			textSrcIpAddress.setText(m_info.getIpAddress());
		}

		/** 監視詳細 */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("monitor.detail.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textMonitorDetailId = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textMonitorDetailId.setLayoutData(gridData);
		if (m_info.getMonitorDetailId() != null) {
			textMonitorDetailId.setText(m_info.getMonitorDetailId());
		}

		/** アプリケーション */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("application") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textApplication = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textApplication.setLayoutData(gridData);
		if (m_info.getApplication() != null) {
			textApplication.setText(m_info.getApplication());
		}

		/** 重要度 */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("priority") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textPriority = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textPriority.setLayoutData(gridData);
		if (m_info.getPriority() == PriorityEnum.INFO) {
			textPriority.setText(PriorityMessage.STRING_INFO);
			textPriority.setBackground(PriorityColorConstant.COLOR_INFO);
		} else if (m_info.getPriority() == PriorityEnum.WARNING) {
			textPriority.setText(PriorityMessage.STRING_WARNING);
			textPriority.setBackground(PriorityColorConstant.COLOR_WARNING);
		} else if (m_info.getPriority() == PriorityEnum.CRITICAL) {
			textPriority.setText(PriorityMessage.STRING_CRITICAL);
			textPriority.setBackground(PriorityColorConstant.COLOR_CRITICAL);
		} else if (m_info.getPriority() == PriorityEnum.UNKNOWN) {
			textPriority.setText(PriorityMessage.STRING_UNKNOWN);
			textPriority.setBackground(PriorityColorConstant.COLOR_UNKNOWN);
		}

		/** メッセージ */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("message") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textMessage = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		textMessage.setLayoutData(new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT));
		if (m_info.getMessage() != null) {
			textMessage.setText(m_info.getMessage());
		}
		Button btnMessage = new Button(parent, SWT.NONE);
		btnMessage.setLayoutData(new GridData(30, SizeConstant.SIZE_BUTTON_HEIGHT));
		btnMessage.setText(Messages.getString("..."));
		btnMessage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				//テキストエリアダイアログを表示する
				TextAreaDialog dialog;
				dialog = new TextAreaDialog(shell, Messages.getString("message"), false);
				if (m_info.getMessage() != null) {
					dialog.setText(m_info.getMessage());
				}
				dialog.open();
			}
		});

		/** オリジナルメッセージ */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("message.org") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textMessageOrg = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		textMessageOrg.setLayoutData(new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT));
		if (m_info.getMessageOrg() != null) {
			textMessageOrg.setText(m_info.getMessageOrg());
		}
		Button btnMessageOrg = new Button(parent, SWT.NONE);
		btnMessageOrg.setLayoutData(new GridData(30, SizeConstant.SIZE_BUTTON_HEIGHT));
		btnMessageOrg.setText(Messages.getString("..."));
		btnMessageOrg.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				//テキストエリアダイアログを表示する
				TextAreaDialog dialog;
				dialog = new TextAreaDialog(shell, Messages.getString("message.org"), false);
				if (m_info.getMessageOrg() != null) {
					dialog.setText(m_info.getMessageOrg());
				}
				dialog.open();
			}
		});

		/** 拡張情報 */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("extended.info") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Table expTable = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		expTable.setHeaderVisible(true);
		expTable.setLinesVisible(true);
		gridData = new GridData(300, 50);
		gridData.horizontalSpan = 2;
		gridData.verticalAlignment = SWT.TOP;
		expTable.setLayoutData(gridData);
		CommonTableViewer expTableViewer = new CommonTableViewer(expTable);
		expTableViewer.createTableColumn(GetJobLinkExpTableDefine.get(),
				GetJobLinkExpTableDefine.SORT_COLUMN_INDEX, GetJobLinkExpTableDefine.SORT_ORDER);

		List<JobLinkExpInfoResponse> expList = m_info.getJobLinkExpInfo();
		if (expList != null) {
			ArrayList<Object> tableData = new ArrayList<Object>();
			for (int i = 0; i < expList.size(); i++) {
				JobLinkExpInfoResponse info = expList.get(i);
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(info.getKey());
				tableLineData.add(info.getValue());
				tableData.add(tableLineData);
			}
			expTableViewer.setInput(tableData);
		}

		// 検索結果の日時はyyyy/MM/dd HH:mm:ss.SSSのフォーマット
		SimpleDateFormat dateFormat = new SimpleDateFormat(JobRestClientWrapper.DATETIME_FORMAT);
		dateFormat.setTimeZone(TimezoneUtil.getTimeZone());
		
		/** 送信日時 */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("send.time") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textSendDate = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textSendDate.setLayoutData(gridData);
		if (m_info.getSendDateTime() != null) {
			textSendDate.setText(dateFormat.format(m_info.getSendDateTime()));
		}

		/** 受信日時 */
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("receive.time") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		Text textAcceptDate = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		textAcceptDate.setLayoutData(gridData);
		if (m_info.getAcceptDateTime() != null) {
			textAcceptDate.setText(dateFormat.format(m_info.getAcceptDateTime()));
		}

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * ボタンを作成します。
	 * 
	 * @param parent
	 *            ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// 閉じるボタン
		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("ok"), false);
	}
}
