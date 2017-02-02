package com.clustercontrol.accesscontrol.view;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.composite.RoleSettingTreeComposite;
import com.clustercontrol.accesscontrol.composite.action.RoleSettingTreeSelectionChangedListener;
import com.clustercontrol.accesscontrol.view.action.RoleSettingAssignSystemPrivilegeAction;
import com.clustercontrol.accesscontrol.view.action.RoleSettingAssignUserAction;
import com.clustercontrol.accesscontrol.view.action.RoleSettingRefreshAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.ws.accesscontrol.RoleTreeItem;

/**
 * アカウント[ロール設定]ビュークラス<BR>
 *
 * クライアントの画面を構成します。
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class RoleSettingTreeView extends CommonViewPart {

	/** ビューID */
	public static final String ID = RoleSettingTreeView.class.getName();

	/** ツリー用コンポジット */
	private RoleSettingTreeComposite m_tree = null;

	/** ロールID */
	private String roleId = null;

	/**
	 * コンストラクタ
	 */
	public RoleSettingTreeView() {
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

		//階層ツリー作成
		m_tree = new RoleSettingTreeComposite(this, parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_tree);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_tree.setLayoutData(gridData);

		m_tree.getTreeViewer().addSelectionChangedListener(
				new RoleSettingTreeSelectionChangedListener(m_tree));

		//ポップアップメニュー作成
		createContextMenu();

		//ビューを更新
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
		Menu treeMenu = menuManager.createContextMenu(m_tree.getTree());
		WidgetTestUtil.setTestId(this, null, treeMenu);
		m_tree.getTree().setMenu(treeMenu);
		getSite().registerContextMenu( menuManager, m_tree.getTreeViewer() );
	}

	/**
	 * ビューを更新します。
	 *
	 */
	public void update() {
		this.m_tree.update();
	}

	/**
	 * ロールIDを返します。
	 * @return roleId
	 */
	public String getRoleId() {
		return this.roleId;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param selectedInfo 現在選択中のロール・ユーザのRoleInfo/UserInfoを指定<br>
	 * 何も選択していない場合にはnullを指定する
	 *
	 * @param selection ボタン（アクション）を有効にするための情報
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setEnabledAction(Object selectedInfo, ISelection selection) {
		if(selectedInfo instanceof RoleInfo ) {
			RoleInfo roleInfo = (RoleInfo)selectedInfo;
			if(RoleSettingTreeConstant.ROOT_ID.equals(roleInfo.getRoleId()) == false &&
					RoleSettingTreeConstant.MANAGER.equals(roleInfo.getRoleId()) == false){
				roleId = roleInfo.getRoleId();
			} else {
				roleId = null;
			}
		} else {
			roleId = null;
		}

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(RoleSettingAssignUserAction.ID, null);
			service.refreshElements(RoleSettingAssignSystemPrivilegeAction.ID, null);
			service.refreshElements(RoleSettingRefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	/**
	 * ロールツリー用のコンポジットを返します。
	 *
	 * @return ロールツリー用のコンポジット
	 */
	public RoleSettingTreeComposite getTreeComposite() {
		return m_tree;
	}

	/**
	 * 選択ロールツリーアイテムを返します。
	 *
	 * @return RoleTreeItem 選択されたロールツリーアイテム
	 */
	public RoleTreeItem getSelectJobTreeItem() {
		return m_tree.getSelectItem();
	}

	public static RoleTreeItem getManager(RoleTreeItem item) {
		if (item == null) {
			return null;
		} else if (item.getData() instanceof RoleInfo &&
				((RoleInfo)item.getData()).getRoleId().equals(RoleSettingTreeConstant.MANAGER)) {
			return item;
		}

		return getManager(item.getParent());
	}
}
