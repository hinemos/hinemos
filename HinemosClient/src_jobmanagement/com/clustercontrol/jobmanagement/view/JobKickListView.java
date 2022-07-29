/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.openapitools.client.model.JobKickFilterInfoRequest.JobkickTypeEnum;
import org.openapitools.client.model.JobKickResponse;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.action.GetJobKickTableDefine;
import com.clustercontrol.jobmanagement.bean.JobKickFilterConstant;
import com.clustercontrol.jobmanagement.bean.JobKickTypeMessage;
import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.view.action.CopyJobKickAction;
import com.clustercontrol.jobmanagement.view.action.DeleteJobKickAction;
import com.clustercontrol.jobmanagement.view.action.DeletePremakeJobsessionAction;
import com.clustercontrol.jobmanagement.view.action.DisableJobKickAction;
import com.clustercontrol.jobmanagement.view.action.EnableJobKickAction;
import com.clustercontrol.jobmanagement.view.action.ModifyJobKickAction;
import com.clustercontrol.jobmanagement.view.action.ObjectPrivilegeJobKickListAction;
import com.clustercontrol.jobmanagement.view.action.RunJobKickAction;
import com.clustercontrol.util.FilterPropertyUpdater;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * ジョブ[実行契機]ビュークラスです。
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobKickListView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	// ログ
	private static Log m_log = LogFactory.getLog( JobHistoryView.class );

	/** ビューID */
	public static final String ID = JobKickListView.class.getName();
	/** ジョブ[実行契機]ビュー用のコンポジット */
	private JobKickListComposite m_jobKickList = null;
	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/** 選択実行契機種別 */
	private JobkickTypeEnum selectType = null;

	/** フィルタ条件 */
	private Property m_condition = null;

	/**
	 * コンストラクタ
	 */
	public JobKickListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを構築します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @see #createContextMenu()
	 * @see #update()
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_jobKickList = new JobKickListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_jobKickList);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_jobKickList.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		//ビューの更新
		this.update();
	}

	/**
	 * コンテキストメニューを作成します。
	 *
	 * @see org.eclipse.jface.action.MenuManager
	 * @see org.eclipse.swt.widgets.Menu
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(m_jobKickList.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		m_jobKickList.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.m_jobKickList.getTableViewer() );
	}

	/**
	 * ビューを更新します。
	 *
	 */
	@Override
	public void update() {
		try {
			if (m_condition == null) {
				this.m_jobKickList.update();
			} else {
				this.m_jobKickList.update(m_condition);
			}
		} catch (Exception e) {
			m_log.warn("update(), " + e.getMessage(), e);
		}
	}

	/**
	 * ジョブ[実行契機]ビュー用のコンポジットを返します。
	 *
	 * @return ジョブ[実行契機]ビュー用のコンポジット
	 */
	public JobKickListComposite getComposite() {
		return m_jobKickList;
	}

	/**
	 * Get the number of selected items
	 * @return
	 */
	public int getSelectedNum(){
		return this.selectedNum;
	}

	/**
	 * 選択されている実行契機種別を返します。
	 * @return selectType
	 */
	public JobkickTypeEnum getSelectType(){
		return this.selectType;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param type 選択実行契機種別
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction(int num, JobkickTypeEnum type, ISelection selection) {
		this.selectedNum = num;
		this.selectType = type;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
		if(null != service){
			service.refreshElements(ModifyJobKickAction.ID, null);
			service.refreshElements(DeleteJobKickAction.ID, null);
			service.refreshElements(CopyJobKickAction.ID, null);
			service.refreshElements(EnableJobKickAction.ID, null);
			service.refreshElements(DisableJobKickAction.ID, null);
			service.refreshElements(ObjectPrivilegeJobKickListAction.ID, null);
			service.refreshElements(RunJobKickAction.ID, null);
			service.refreshElements(DeletePremakeJobsessionAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	public List<String> getSelectedIdList() {
		StructuredSelection selection = (StructuredSelection) this.m_jobKickList.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		List<String> idList = new ArrayList<String>();
		for(Object obj : list) {
			if(obj instanceof ArrayList) {
				List<?> objList = (ArrayList<?>)obj;
				idList.add((String) objList.get(GetJobKickTableDefine.JOBKICK_ID));
			}
		}

		return idList;
	}

	public String getManagerName() {
		String str = "";
		StructuredSelection selection = (StructuredSelection) this.m_jobKickList.getTableViewer().getSelection();
		List<?> list = (List<?>) selection.toList();
		Object obj = list.get(0);

		if(obj instanceof ArrayList) {
			List<?> objList = (ArrayList<?>)obj;
			str = (String)objList.get(GetJobKickTableDefine.MANAGER_NAME);
		}

		return str;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.m_jobKickList.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = null;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetJobKickTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetJobKickTableDefine.JOBKICK_ID);

			String type = (String) ((List<?>)obj).get(GetJobKickTableDefine.TYPE);
			JobKickResponse.TypeEnum TypeNum = JobKickResponse.TypeEnum
					.fromValue(JobKickTypeMessage.stringToTypeEnumValue(type));
			if(TypeNum == JobKickResponse.TypeEnum.SCHEDULE
				|| TypeNum ==JobKickResponse.TypeEnum.FILECHECK
				|| TypeNum ==JobKickResponse.TypeEnum.JOBLINKRCV
				|| TypeNum == JobKickResponse.TypeEnum.MANUAL) {
				objectType = HinemosModuleConstant.JOB_KICK;
			}

			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.m_jobKickList.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetJobKickTableDefine.OWNER_ROLE);
		}
		return id;
	}

	/**
	 * フィルタ条件を返します。
	 *
	 * @return フィルタ条件
	 */
	public Property getFilterCondition() {
		return m_condition;
	}

	/**
	 * フィルタ条件を設定します。
	 *
	 * @param condition フィルタ条件
	 */
	public void setFilterCondition(Property condition) {
		FilterPropertyUpdater.getInstance().addFilterProperty(getClass(), condition,
				JobKickFilterConstant.MANAGER);
		m_condition = condition;
	}

}
