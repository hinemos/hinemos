/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.HinemosPropertyTypeMessage;
import com.clustercontrol.maintenance.action.GetHinemosPropertyTableDefine;
import com.clustercontrol.maintenance.composite.HinemosPropertyComposite;
import com.clustercontrol.maintenance.dialog.HinemosPropertyDialog;
import com.clustercontrol.maintenance.view.HinemosPropertyView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.maintenance.HinemosPropertyInfo;

/**
 * メンテナンス[共通設定]ビューののコピーアクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyCopyAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( HinemosPropertyCopyAction.class );

	/** アクションID */
	public static final String ID = HinemosPropertyCopyAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		HinemosPropertyView view = null;
		try {
			view = (HinemosPropertyView)this.viewPart.getAdapter(HinemosPropertyView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null");
			return null;
		}

		HinemosPropertyComposite composite = (HinemosPropertyComposite) view.getComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		@SuppressWarnings("rawtypes")
		List<?> list = (List) selection.getFirstElement();
		String key = null;
		String managerName = null;
		int valueType = 0;
		if(list != null && list.size() > 0){
			key = (String) list.get(GetHinemosPropertyTableDefine.KEY);
			String valueTypeStr = (String)list.get(GetHinemosPropertyTableDefine.VALUE_TYPE);
			managerName = (String)list.get(GetHinemosPropertyTableDefine.MANAGER_NAME);
			valueType = HinemosPropertyTypeMessage.stringToType(valueTypeStr);
		}
		Table table = composite.getTableViewer().getTable();
		WidgetTestUtil.setTestId(this, null, table);

		//選択アイテムがある場合に、編集ダイアログを表示する
		if(key != null){

			try{
				HinemosPropertyInfo info = new HinemosPropertyInfo();
				info.setKey(key);
				info.setValueType(valueType);
				if (valueType == HinemosPropertyTypeConstant.TYPE_STRING) {
					String value = (String)list.get(GetHinemosPropertyTableDefine.VALUE);
					info.setValueString(value);
				} else if (valueType == HinemosPropertyTypeConstant.TYPE_NUMERIC) {
					Long value = (Long)list.get(GetHinemosPropertyTableDefine.VALUE);
					try {
						info.setValueNumeric(value);
					} catch (NumberFormatException e) {
						m_log.info("run() setValueNumeric(), " + e.getMessage());
						Object[] args = {Messages.getString("hinemos.property.key"), Long.MIN_VALUE, Long.MAX_VALUE};
						MessageDialog.openError(
								null,
								Messages.getString("failed"),
								Messages.getString("message.common.4", args));
					}
				} else {
					boolean value = Boolean.parseBoolean((String)list.get(GetHinemosPropertyTableDefine.VALUE));
					info.setValueBoolean(value);
				}
				info.setDescription((String)list.get(GetHinemosPropertyTableDefine.DESCRIPTION));

				// ダイアログを生成
				HinemosPropertyDialog dialog = new HinemosPropertyDialog(
						this.viewPart.getSite().getShell(), managerName, valueType,
						PropertyDefineConstant.MODE_ADD, info);

				if (dialog.open() == IDialogConstants.OK_ID) {
					int selectIndex = table.getSelectionIndex();
					view.update();
					table.setSelection(selectIndex);
				}
			}
			catch (Exception e1) {
				m_log.warn("run(), " + e1.getMessage(), e1);
			}
		}
		else{
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.hinemos.property.11"));
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof HinemosPropertyView){
					// Enable button when 1 item is selected
					HinemosPropertyView view = (HinemosPropertyView)part;

					if(view.getSelectedNum() == 1) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
