/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.composite;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.CalendarInfoResponse;

import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.notify.composite.NotifyBasicComposite;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * カレンダIDコンポジットクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class CalendarIdListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( CalendarIdListComposite.class );

	// 表示とIDを紐付けるマップ
	// key=CalendarId
	// value=CalendarName(CalendarId)
	private ConcurrentHashMap<String, String> dispMap = new ConcurrentHashMap<>(); 
	
	// ----- instance フィールド ----- //

	/** カレンダIDラベル */
	private Label labelCalendarId = null;

	/** カレンダIDコンボボックス */
	private Combo calIdCombo = null;

	/** ラベル幅 */
	private Integer labelWidth = null;

	/** コンボボックス幅 */
	private Integer comboWidth = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。<BR>
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param labelFlg カレンダIDラベル表示フラグ
	 */
	public CalendarIdListComposite(Composite parent, int style, boolean labelFlg) {
		super(parent, style);

		initialize(parent, labelFlg);
	}

	/**
	 * インスタンスを返します。<BR>
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param labelFlg カレンダIDラベル表示フラグ
	 * @param labelFlg カレンダIDラベル表示フラグ
	 * @param labelFlg カレンダIDラベル表示フラグ
	 */
	public CalendarIdListComposite(Composite parent, int style, boolean labelFlg,
			int labelWidth, int comboWidth) {
		super(parent, style);
		this.labelWidth = labelWidth;
		this.comboWidth = comboWidth;
		initialize(parent, labelFlg);
	}


	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。<BR>
	 */
	private void initialize(Composite parent, boolean labelFlg) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		if(labelFlg){
			layout.numColumns = 15;
		}
		else{
			layout.numColumns = 10;
		}
		setLayout(layout);

		/*
		 * カレンダID
		 */
		if(labelFlg){
			// ラベル
			labelCalendarId = new Label(this, SWT.NONE);
			WidgetTestUtil.setTestId(this, "calendarid", labelCalendarId);
			gridData = new GridData();
			if (this.labelWidth != null) {
				gridData.horizontalSpan = this.labelWidth;
			} else if (parent instanceof NotifyBasicComposite) {
				gridData.horizontalSpan = NotifyBasicComposite.WIDTH_TITLE;
			} else {
				gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE;
			}
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			labelCalendarId.setLayoutData(gridData);
			labelCalendarId.setText(Messages.getString("calendar.id") + " : ");
		}

		// コンボボックス
		calIdCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, calIdCombo);
		gridData = new GridData();
		if(labelFlg){
			if (this.comboWidth != null) {
				gridData.horizontalSpan = this.comboWidth;
			} else if (parent instanceof NotifyBasicComposite) {
				gridData.horizontalSpan = CommonMonitorDialog.SMALL_UNIT;
			} else {
				gridData.horizontalSpan = CommonMonitorDialog.SHORT_UNIT;
			}
		}
		else{
			gridData.horizontalSpan = 5;
		}
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calIdCombo.setLayoutData(gridData);

		this.update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 * <p>
	 *
	 */
	@Override
	public void update() {
	}

	private void removeAllMap() {
		dispMap.clear();
		calIdCombo.removeAll();
	}
	
	private String putMap(String calId, String calName) {
		String disp = null;
		if (calId.length() == 0) {
			disp = "";
		} else {
			int maxLength = 32;
			if (maxLength < calName.length()) {
				calName = calName.substring(0, maxLength) + "...";
			}
			disp = calName + "(" + calId + ")";
		}
		dispMap.put(calId, disp);
		calIdCombo.add(disp);
		return disp;
	}

	/**
	 * オーダーロールIDが参照権限があるカレンダを選択するコンボボックスを生成します。
	 *
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーダーロールID
	 */
	public void createCalIdCombo(String managerName, String ownerRoleId){

		// 初期化
		removeAllMap();

		// 空欄
		putMap("", "");

		List<CalendarInfoResponse> calList = null;
		// データ取得
		try {
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
				calList = wrapper.getCalendarList(ownerRoleId);
			}
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					e.getMessage());
			setEnabled(false);

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					e.getMessage());
		}

		if(calList != null){
			// カレンダIDリスト
			for(CalendarInfoResponse info : calList){
				putMap(info.getCalendarId(), info.getCalendarName());
			}
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		calIdCombo.setEnabled(enabled);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getEnabled()
	 */
	@Override
	public boolean getEnabled() {
		return calIdCombo.getEnabled();
	}

	/**
	 * 表示しているCalendarIdを返す
	 * 表示されているのは、「CalendarName(CalendarId)」
	 */
	public String getText() {
		String selectedText = calIdCombo.getText();
		if (selectedText == null || selectedText.isEmpty()) {
			return "";
		}
		for (Map.Entry<String, String> tmpId : dispMap.entrySet()) {
			if (tmpId.getValue().equals(selectedText)) {
				return tmpId.getKey();
			}
		}
		m_log.warn("getText() warning"); // ここには到達しないはず。
		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String calId) {
		m_log.debug("setText() : calId=" + calId + ", dispMap=" + dispMap);
		if (calId == null) {
			return;
		}
		// 権限によっては、存在しないカレンダを選択(setText)する場合があるので、
		// ここで追加する。
		if (!dispMap.keySet().contains(calId)) {
			// カレンダの参照権限がない場合のみ、ここに到達する。
			// 参照権限がないので、IDしかわからない。
			putMap(calId, "");
		}

		// 選択する
		calIdCombo.setText(dispMap.get(calId));
	}

}
