/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.openapitools.client.model.GetNodeListRequest;
import org.openapitools.client.model.ScopeInfoResponseP1;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.nodemap.composite.NodeListAttributeComposite;
import com.clustercontrol.nodemap.composite.NodeListComposite;
import com.clustercontrol.nodemap.dialog.NodeListFindDialog;
import com.clustercontrol.nodemap.editpart.ListViewController;
import com.clustercontrol.nodemap.util.SecondaryIdMap;
import com.clustercontrol.nodemap.view.action.NodeListDownloadAction;
import com.clustercontrol.nodemap.view.action.NodeListFindAction;
import com.clustercontrol.nodemap.view.action.NodeListScopeAddAction;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.CommonViewPart;

/**
 * 構成情報一覧ビューを描画するためのクラス。
 * コントロール部分はListViewController
 * @since 1.0.0
 */
public class NodeListView extends CommonViewPart {

	// ログ
	private static Log m_log = LogFactory.getLog( NodeListView.class );

	public static final String ID = NodeListView.class.getName();

	/** サッシュフォーム */
	private SashForm m_sash = null;

	public NodeListComposite m_listComposite;
	
	public NodeListAttributeComposite m_attributeComposite;

	private ListViewController m_controller;

	public String m_secondaryId;

	// 検索条件
	private GetNodeListRequest m_nodeFilterInfo;

	public NodeListView(){
		m_log.debug("nodelistview constructor");
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(1, false));

		// サッシュフォーム作成及び設定
		m_sash = new SashForm(parent, SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		m_sash.setLayoutData(gridData);

		m_listComposite = new NodeListComposite(m_sash, SWT.BORDER, this);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_listComposite.setLayoutData(gridData);

		m_attributeComposite = new NodeListAttributeComposite(m_sash, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_attributeComposite.setLayoutData(gridData);

		// Sashの境界を調整 左部70% 右部30%
		m_sash.setWeights(new int[] {70, 30});
		
		// このビューのSecondaryIdを取得する
		IViewSite viewSite = getViewSite();
		if(viewSite != null){
			// ファシリティIDとSecondaryIdの対応マップから、
			// 今新規に生成しようとしているViewのファシリティIDを特定する
			m_secondaryId = viewSite.getSecondaryId();
			String facilityId = SecondaryIdMap.getFacilityId(m_secondaryId);
			String managerName = SecondaryIdMap.getManagerName(m_secondaryId);
			m_log.debug("createPartControl updateView " +
					"secondaryId= " + m_secondaryId + ", managerName=" + managerName + ", facilityId=" + facilityId);
			
			m_listComposite.setManagerName(managerName);
			// 指定のファシリティIDのスコープのマップを表示
			updateView(facilityId);
		} else {
			m_log.warn("createPartControl(), createPartControl logic error, viewSite is null");
		}
		
		getSite().getPage().addPartListener(new IPartListener() {
			
			@Override
			public void partOpened(IWorkbenchPart part) {
			}
			
			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}
			
			@Override
			public void partClosed(IWorkbenchPart part) {
			}
			
			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
			}
			
			@Override
			public void partActivated(IWorkbenchPart part) {
				setEnabledActionAll();
			}
		});
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if (m_listComposite != null) {
			m_listComposite.setFocus();
		}
		m_log.debug("id:"+getViewSite().getId()+",secondary:"+getViewSite().getSecondaryId());
		m_log.debug("setFocus end");
	}

	public void setEnabledActionAll() {
		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(NodeListDownloadAction.ID, null);
			service.refreshElements(NodeListFindAction.ID, null);
			service.refreshElements(NodeListScopeAddAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	/**
	 * 描画対象スコープのファシリティIDを設定し、ビューの内のマップを更新
	 */
	public void updateView(String facilityId){

		long start = System.currentTimeMillis();
		String scopeName = "";
		try {
			if (m_listComposite.getManagerName() != null
					&& !m_listComposite.getManagerName().isEmpty()) {
				RepositoryRestClientWrapper repositoryWrapper = RepositoryRestClientWrapper.getWrapper(m_listComposite.getManagerName());
				ScopeInfoResponseP1 scopeInfo = repositoryWrapper.getScope(facilityId);
				scopeName = HinemosMessage.replace(scopeInfo.getFacilityName());
			}
		} catch (FacilityNotFound | HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
			m_log.warn("updateView() : Failed to acquire scope info. facilityId=" + facilityId);
		}

		// 描画対象スコープのFacilityIDからビューのSecondaryIdを特定できるよう、
		// FacilityIDをキーにSecondaryIdを登録する
		SecondaryIdMap.putSecondaryId(m_secondaryId, m_listComposite.getManagerName(), facilityId, NodeListView.class);

		m_log.debug("put " + m_secondaryId + ", " + facilityId);

		// 描画対象スコープのFacilityIdを定義してコントローラを作成
		m_controller = new ListViewController(this, m_secondaryId, facilityId);

		// マネージャから情報を取得して描画
		reload();

		long end = System.currentTimeMillis();
		m_log.debug("reload() :" + (end - start) + "ms");

		setViewTabName(scopeName);
	}
	
	/**
	 * ビューのタブ情報表示
	 * 
	 * @param scopeName スコープ名
	 */
	public void setViewTabName(String scopeName) {
		if (m_controller.getManagerName().equals("")) {
			setPartName(Messages.getString("root"));
		} else {
			String managerName = m_controller.getManagerName();
			if (scopeName == null || scopeName.equals("")) {
				setPartName(Messages.getString("root") + "("+ managerName +")");
			} else {
				setPartName(scopeName + "("+ managerName +")");
			}
		}
	}

	/**
	 * マネージャから情報を取得し画面を再描画する
	 */
	public boolean reload() {
		long start = System.currentTimeMillis();
		try {
			m_controller.update(m_nodeFilterInfo);
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"), 
					Messages.getString("message.accesscontrol.16"));
			return false;
		} catch (Exception e) {
			String errMsg = "";
			if (e instanceof RestConnectFailed) {
				m_log.debug("reload(), " + e.getMessage());
				errMsg = Messages.getString("message.hinemos.failure.transfer") + ", " + e.getMessage();
			} else if (e instanceof InvalidSetting) {
				m_log.warn("reload(), " + e.getMessage(), e);
				errMsg = HinemosMessage.replace(e.getMessage());
			} else {
				String message = HinemosMessage.replace(e.getMessage());
				m_log.warn("reload(), " + e.getMessage(), e);
				errMsg = Messages.getString("message.hinemos.failure.unexpected") + ", " + message;
			}
			MessageDialog.openInformation(null, Messages.getString("message"), errMsg);
			//Exceptionが発生した場合は、検索情報を初期化する
			m_nodeFilterInfo=null;
			return false;
		}
		m_listComposite.update();

		long end = System.currentTimeMillis();
		m_log.debug("reload() :" + (end - start) + "ms");

		return true;
	}
	
	/**
	 * disposeの際にm_secondaryIdMapを更新する
	 */
	@Override
	public void dispose(){
		m_log.info("call dispose(), secondaryId=" + m_secondaryId 
			+ ",facilityId=" + SecondaryIdMap.getFacilityId(m_secondaryId));

		SecondaryIdMap.removeSecondaryId(m_secondaryId);
		NodeListFindDialog.removeFilterCache(m_secondaryId);

		super.dispose();
	}

	/**
	 * ノードフィルタ情報の設定
	 */
	public void setNodeFilterInfo(GetNodeListRequest nodeFilterInfo) {
		this.m_nodeFilterInfo = nodeFilterInfo;
	}
	/**
	 * ノードフィルタ情報の取得
	 */
	public GetNodeListRequest getNodeFilterInfo() {
		return this.m_nodeFilterInfo;
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * Controller取得
	 * 
	 * @return Controller
	 */
	public ListViewController getController() {
		return this.m_controller;
	}

	/**
	 * Composite取得
	 * 
	 * @return Composite
	 */
	public NodeListComposite getListComposite() {
		return m_listComposite;
	}

	/**
	 * SecondaryId取得
	 * 
	 * @return SecondaryId
	 */
	public String getSecondaryId() {
		return m_secondaryId;
	}
}