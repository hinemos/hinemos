/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.jobmap.action.GetJobMapImageListTableDefine;
import com.clustercontrol.jobmap.composite.JobMapImageListComposite;
import com.clustercontrol.jobmap.view.action.JobMapImageDeleteAction;
import com.clustercontrol.jobmap.view.action.JobMapImageListObjectPrivilegeAction;
import com.clustercontrol.jobmap.view.action.JobMapImageModifyAction;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * ジョブマップ用イメージファイル一覧ビュークラス<BR>
 *
 * @version 6.0.a
 */
public class JobMapImageListView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	public static final String ID = JobMapImageListView.class.getName();
	private JobMapImageListComposite composite = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/**
	 * コンストラクタ
	 */
	public JobMapImageListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ViewPartへのコントロール作成処理<BR>
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.composite = new JobMapImageListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, composite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		//ビューを更新
		this.update();
	}

	/**
	 * ポップアップメニュー作成<BR>
	 *
	 *
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu ( this.composite.getTable() );
		WidgetTestUtil.setTestId(this, null, menu);
		this.composite.getTable().setMenu(menu);

		getSite().registerContextMenu( menuManager, this.composite.getTableViewer() );
	}

	/**
	 * ジョブマップ用アイコンイメージ一覧ビュー更新<BR>
	 */
	@Override
	public void update() {
		this.composite.update();
	}

	public List<String> getSelectedIdList() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		List<String> idList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				idList.add((String)objList.get(GetJobMapImageListTableDefine.ICON_ID));
			}
		}
		return idList;
	}

	public String getManagerName() {
		String str = "";
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();
		List<?> list = (List<?>) selection.toList();
		Object obj = list.get(0);

		if(obj instanceof ArrayList) {
			List<?> objList = (ArrayList<?>)obj;
			str = (String)objList.get(GetJobMapImageListTableDefine.MANAGER_NAME);
		}

		return str;
	}

	/**
	 * ビュー用のコンポジットを返します。
	 *
	 * @return ビュー用のコンポジット
	 */
	public JobMapImageListComposite getComposite() {
		return this.composite;
	}
	public Map<String, List<String>> getSelectedItem() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String iconId = null;
		String managerName = null;
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		if (list != null) {
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				managerName = (String)objList.get(GetJobMapImageListTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null){
					map.put(managerName, new ArrayList<String>());
				}
			}
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				iconId = (String)objList.get(GetJobMapImageListTableDefine.ICON_ID);
				managerName = (String)objList.get(GetJobMapImageListTableDefine.MANAGER_NAME);

				map.get(managerName).add(iconId);
			}
		}
		return map;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.JOBMAP_IMAGE_FILE;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetJobMapImageListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetJobMapImageListTableDefine.ICON_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetJobMapImageListTableDefine.OWNER_ROLE);
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
			service.refreshElements(JobMapImageModifyAction.ID, null);
			service.refreshElements(JobMapImageDeleteAction.ID, null);
			service.refreshElements(JobMapImageListObjectPrivilegeAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
}
