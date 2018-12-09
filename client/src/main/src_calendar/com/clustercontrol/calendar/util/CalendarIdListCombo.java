/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

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

import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.calendar.CalendarInfo;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;

public class CalendarIdListCombo extends Composite{
	
	// ログ
	private static Log m_log = LogFactory.getLog( CalendarIdListComposite.class );

	// 表示とIDを紐付けるマップ
	// key=CalendarId
	// value=CalendarName(CalendarId)
	protected ConcurrentHashMap<String, String> dispMap;
	
	protected Combo cmbCalIdList;
	
	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public CalendarIdListCombo(Composite parent, int style) {
		super(parent, style);

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		this.setLayout(layout);
		
		cmbCalIdList = new Combo(this, style);
		cmbCalIdList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dispMap = new ConcurrentHashMap<>(); 
	}
	
	/**
	 * 
	 */
	private void removeAllMap() {
		dispMap.clear();
		cmbCalIdList.removeAll();
	}
	
	/**
	 * 
	 * @param calId
	 * @param calName
	 * @return
	 */
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
		cmbCalIdList.add(disp);
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

		List<CalendarInfo> calList = null;
		// データ取得
		try {
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
				calList = wrapper.getCalendarList(ownerRoleId);
			}
		} catch (InvalidRole_Exception e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			cmbCalIdList.setEnabled(false);

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		if(calList != null){
			// カレンダIDリスト
			for(CalendarInfo info : calList){
				putMap(info.getCalendarId(), info.getCalendarName());
			}
		}
	}
	
	/**
	 * 表示しているCalendarIdを返す
	 * 表示されているのは、「CalendarName(CalendarId)」
	 */
	public String getText() {
		String selectedText = this.cmbCalIdList.getText();
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
		cmbCalIdList.setText(dispMap.get(calId));
	}
}
