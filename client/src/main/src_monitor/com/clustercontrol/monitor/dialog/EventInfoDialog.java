/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.dialog;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.Property;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.util.EventDataPropertyUtil;
import com.clustercontrol.monitor.util.MonitorEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.viewer.PropertySheet;
import com.clustercontrol.ws.monitor.EventDataInfo;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;

/**
 * 監視[イベントの詳細]ダイアログクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class EventInfoDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( EventInfoDialog.class );

	/** 選択されたアイテム。 */
	private List<?> m_list = null;

	/** プロパティシート。 */
	private PropertySheet propertySheet = null;

	/**
	 * プロパティ
	 */
	private Property eventCommentProperty = null;


	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public EventInfoDialog(Shell parent, List<?> list) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		m_list = list;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 650);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.info.events"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		/*
		 * 属性プロパティシート
		 */

		// ラベル
		Label label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "attribute", label);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("attribute") + " : ");

		// プロパティシート
		Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, table);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		this.propertySheet = new PropertySheet(table);
		this.propertySheet.setSize(170, 280);

		// プロパティ取得及び設定
		eventCommentProperty = null;
		if (m_list != null) {
			String managerName = (String)m_list.get(GetEventListTableDefine.MANAGER_NAME);
			String monitorId = (String) m_list.get(GetEventListTableDefine.MONITOR_ID);
			String monitorDetailId = (String) m_list.get(GetEventListTableDefine.MONITOR_DETAIL_ID);
			String pluginId = (String) m_list.get(GetEventListTableDefine.PLUGIN_ID);
			String facilityId = (String) m_list.get(GetEventListTableDefine.FACILITY_ID);
			Date receiveTime = (Date) m_list.get(GetEventListTableDefine.RECEIVE_TIME);

			try {
				MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(managerName);
				EventDataInfo info = wrapper.getEventInfo(monitorId, monitorDetailId, pluginId, facilityId, receiveTime.getTime());
				eventCommentProperty = EventDataPropertyUtil.dto2property(info, Locale.getDefault());
				this.propertySheet.setInput(eventCommentProperty);
			} catch (InvalidRole_Exception e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("customizeDialog() getEventInfo, " + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		
		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 入力値を保持したプロパティを返します。<BR>
	 * プロパティシートよりプロパティを取得します。
	 *
	 * @return プロパティ
	 *
	 * @see com.clustercontrol.viewer.PropertySheet#getInput()
	 */
	public Property getInputData() {
		if(eventCommentProperty != null){
			Property copy = PropertyUtil.copy(eventCommentProperty);
			return copy;
		}
		else{
			return null;
		}
	}

	/**
	 * 入力値を保持したプロパティを設定します。
	 *
	 * @param property プロパティ
	 */
	public void setInputData(Property property) {
		propertySheet.setInput(property);
	}

	/**
	 * 閉じるボタンを作成します。
	 *
	 * @param parent 親のコンポジット（ボタンバー）
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//保存(ok)ボタン
		this.createButton(parent, IDialogConstants.OK_ID, Messages.getString("register"), true);
		// 閉じる(cancel)ボタン
		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);

	}
}
