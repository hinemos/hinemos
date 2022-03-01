/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.nodemap.composite.ScopeComposite;
import com.clustercontrol.nodemap.util.RelationViewController;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.nodemap.view.ScopeTreeView;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.Messages;

/**
 * スコープツリービューの新規ノードマップビューを開くためのボタン用
 * クライアント側アクションクラス<BR>
 * @since 1.0.0
 */
public class OpenNodeMapAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( OpenNodeMapAction.class );
	
	/** アクションID */
	protected static final String ActionIDBase = "com.clustercontrol.enterprise.nodemap.view.action." ;
	public static final String ID = ActionIDBase  + OpenNodeMapAction.class.getSimpleName();

	/** ビュー */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		
		ScopeTreeView view = (ScopeTreeView) viewPart.getAdapter(ScopeTreeView.class);

		ScopeComposite tree = view.getScopeComposite();

		FacilityTreeItemResponse item = tree.getSelectItem();
		if (item.getData().getFacilityType() == FacilityTypeEnum.NODE) {
			/*
			 * ボタンはdisableになっているので、呼ばれる事はない。
			 */
			m_log.warn("run(), cannot open new Nodemap of NODE");
			return null;
		}

		try {
			String facilityId = item.getData().getFacilityId();
			if (item.getData().getFacilityType() == FacilityTypeEnum.MANAGER) {
				facilityId = ReservedFacilityIdConstant.ROOT_SCOPE;
			}
			RelationViewController.createNewView(ScopePropertyUtil.getManager(item).getData().getFacilityId(), facilityId, NodeMapView.class);
		} catch (Exception e) {
			m_log.warn("run() createNewView, " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage() + " " + e.getClass().getSimpleName());
		}
		
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		boolean editEnable = false;
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				
				if(part instanceof ScopeTreeView){
					// Enable button when 1 item is selected
					ScopeTreeView view = (ScopeTreeView)part;
					ScopeComposite tree = view.getScopeComposite();

					FacilityTreeItemResponse item = tree.getSelectItem();
					
					FacilityTypeEnum type = item.getData().getFacilityType();
					if (type == FacilityTypeEnum.COMPOSITE) {
						editEnable = false;
					} else if (type == FacilityTypeEnum.MANAGER) {
						editEnable = true;
					} else if(item.getData().getBuiltInFlg()){
						editEnable = true;
					} else if (type == FacilityTypeEnum.SCOPE) {
						editEnable = true;
					} else if (type == FacilityTypeEnum.NODE) {
						editEnable = false;
					}
				}
			}
		}
		
		this.setBaseEnabled(editEnable);
	}
}
