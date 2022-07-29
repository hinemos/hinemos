/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.hub.action.GetLogSearchResultTableDefine;
import com.clustercontrol.hub.action.GetTransferTableDefine;
import com.clustercontrol.hub.composite.LogSearchComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * 収集蓄積[検索]ビュークラス<BR>
 *
 */
public class LogSearchView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	
	public static final String ID = LogSearchView.class.getName();
	
	public static final String SECONDARYID_SEPARATOR = ",node=";
	
	private LogSearchComposite logSearchComposite = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/**
	 * <BR><B>使用しないこと。</B></BR>
	 * 収集蓄積[検索]ビューは、
	 * LogSearchView.createSearchViewを利用し作成すること。
	 */
	@Deprecated
	public LogSearchView() {
		super();
	}

	/**
	 * 検索の際のビュー作成を行う。
	 * <BR><B>ログ検索ビューを作成する際は、ここを必ず使用すること。</B></BR>
	 * secondaryIdを、マネージャ名@ファシリティID で作成する
	 * @param page
	 * @param manager
	 * @param facilityid
	 * @return
	 * @throws PartInitException
	 * 
	 * XXX
	 * マネージャ名は、次の文字列の入力を受け付けてしまう。
	 * 半角文字列の -^\@[;:],./\=~|`{+*}<>?_!"#$%&'()
	 * 全角文字列の ー＾￥＠「；：」、。・￥＝～｜‘｛＋＊｝＜＞？＿！”＃＄％＆’（）
	 * そのため、上記の文字をセパレータとして使用することできない。
	 * 暫定対処で、",node="をセパレータとする。
	 */
	public static LogSearchView createSearchView(
			IWorkbenchPage page, String manager, String facilityid) throws PartInitException{
		
		//セカンダリーIDにコロンがあるとエラーになるので置き換え
		String managerRep = manager.replaceAll(":", "@");
		//マネージャ名とファシリティIDで一意なセカンダリIDを作成する
		String secondaryId = 
				managerRep + SECONDARYID_SEPARATOR + facilityid;
		return (LogSearchView) page.showView(LogSearchView.ID, secondaryId,IWorkbenchPage.VIEW_ACTIVATE);
	}
	
	/**
	 * ViewPartへのコントロール作成処理<BR>
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		logSearchComposite = new LogSearchComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, logSearchComposite);

		/*
		 * 収集蓄積[検索]ビューのタイトルにFacilityIDを含める
		 */
		if (this.getViewSite().getSecondaryId() != null) {
			
			String title = this.getViewSite().getSecondaryId();
			//getSecondaryIdに、マネージャ名とファシリティIDが含まれているので、そこから取得する
			String[] spltStr = title.split(SECONDARYID_SEPARATOR);
			String manager = spltStr[0];
			String facilityId = spltStr[1];
			//ビュータイトルを設定
			this.setPartName(Messages.getString("view.hub.search", new Object[]{manager,facilityId}));

			logSearchComposite.setManager(manager);
			logSearchComposite.setFacilityId(facilityId);
			logSearchComposite.updateMonitorCombo();
		}
		
		// ポップアップメニュー作成
		// createContextMenu();

		//ビューを更新
		this.update();
	}

	/**
	 * 収集蓄積[検索]ビュー更新<BR>
	 */
	@Override
	public void update() {
		logSearchComposite.update();
	}
	
	public LogSearchComposite getLogTransferComposite() {
		return logSearchComposite;
	}

	public List<String> getSelectedIdList() {
		StructuredSelection selection = (StructuredSelection) this.logSearchComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String id = null;
		List<String> idList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				id = objList.get(GetTransferTableDefine.TRANSFER_ID);
				idList.add(id);
			}
		}
		return idList;
	}

	public List<String> getSelectedManagerNameList() {
		StructuredSelection selection = (StructuredSelection) this.logSearchComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String managerName = null;
		List<String> managerList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				managerName = objList.get(GetTransferTableDefine.MANAGER_NAME);
				managerList.add(managerName);
			}
		}
		return managerList;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.logSearchComposite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.HUB_TRANSFER;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetTransferTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetTransferTableDefine.TRANSFER_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.logSearchComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetTransferTableDefine.OWNER_ROLE);
		}
		return id;
	}

	/**
	 * Get the number of selected items
	 * @return
	 */
	public int getSelectedNum(){
		return this.selectedNum;
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}
	
	public List<String> getSelectedItem() {
		StructuredSelection selection 
			= (StructuredSelection) this.logSearchComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String orgMsg = null;
		List<String> orgMsgList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				orgMsg = (String)objList.get(GetLogSearchResultTableDefine.ORG_MESSAGE);
				orgMsgList.add(orgMsg);
			}
		}
		return orgMsgList;
	}
	
	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction( int num, ISelection selection ){
		this.selectedNum = num;
		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		if (logSearchComposite != null) {
			logSearchComposite.setFocus();
		}
	}
}
