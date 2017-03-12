/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.action;

import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.bean.EventInfoConstant;
import com.clustercontrol.monitor.util.MonitorEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.ws.monitor.EventLogNotFound_Exception;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;

public class CommentEvent {
	/**
	 * イベント情報のコメントを更新するクライアント側アクションクラス<BR>
	 *
	 * マネージャにSessionBean経由でアクセスし、イベント情報の確認を更新します。
	 *
	 * @version 4.0.0
	 * @since 1.0.0
	 */
	// ----- コンストラクタ ----- //

	// ----- instance メソッド ----- //

	/**
	 * マネージャにSessionBean経由でアクセスし、引数で指定されたイベント情報一覧のコメントを更新します。<BR>
	 * <p>指定されたリストには、各イベント情報のリスト（{@link ArrayList}）が格納されています。
	 * また、１イベント情報の各値は、テーブルの値を元とした、
	 * EventLogData（{@link com.clustercontrol.monitor.ejb.entity.EventLogData}）クラスのオブジェクトとして格納されています。
	 *
	 * したがって、テーブルに表示されていない情報（例：オリジナルメッセージ）は格納されていません。
	 * <p>
	 * <dl>
	 *  <dt>イベント情報一覧（EventLogDataのリスト）</dt>
	 * </dl>
	 *
	 * @param list 更新対象のイベント情報一覧（EventLogDataのリスト）
	 * @return 更新に成功した場合、</code> true </code>
	 *
	 */
	public boolean updateComment(String managerName, Property property) {

		ArrayList<?> value = null;

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.MONITOR_ID);
		String monitorId = "";
		if (!"".equals(value.get(0))) {
			monitorId = (String) value.get(0);
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.MONITOR_DETAIL_ID);
		String monitorDetailId = "";
		if (!"".equals(value.get(0))) {
			monitorDetailId = (String) value.get(0);
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.PLUGIN_ID);
		String pluginId = "";
		if (!"".equals(value.get(0))) {
			pluginId = (String) value.get(0);
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.FACILITY_ID);
		String facilityId = "";
		if (!"".equals(value.get(0))) {
			facilityId = (String) value.get(0);
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.OUTPUT_DATE);
		Long outputDate = null;
		if (!"".equals(value.get(0)) && value.get(0) != null) {
			outputDate = ((Date) value.get(0)).getTime();
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.COMMENT);
		String comment = null;

		if (!"".equals(value.get(0))) {
			comment = (String) value.get(0);
		}else{
			comment = "";
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.COMMENT_DATE);
		Long commentDate = null;
		if (!"".equals(value.get(0)) && value.get(0) != null) {
			commentDate= ((Date) value.get(0)).getTime();
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.COMMENT_USER);
		String commentUser = null;
		if (!"".equals(value.get(0))){
			commentUser = (String) value.get(0);
		}

		try {
			MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(managerName);
			wrapper.modifyComment(monitorId, monitorDetailId, pluginId, facilityId, outputDate, comment, commentDate, commentUser );
			return true;

		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (HinemosUnknown_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.monitor.58") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (EventLogNotFound_Exception e){
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.monitor.59") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.monitor.58") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return false;
	}

}
