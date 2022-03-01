/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.RoleInfoResponse.RoleTypeEnum;
import org.openapitools.client.model.RoleTreeItemResponseP1;
import org.openapitools.client.model.UserInfoResponse;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.bean.RoleTreeItemWrapper;
import com.clustercontrol.accesscontrol.dialog.RoleSettingDialog;
import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;
import com.clustercontrol.accesscontrol.viewer.RoleSettingTreeContentProvider;
import com.clustercontrol.accesscontrol.viewer.RoleSettingTreeLabelProvider;
import com.clustercontrol.accesscontrol.viewer.RoleSettingTreeViewer;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ロールツリー用のコンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class RoleSettingTreeComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( RoleSettingTreeComposite.class );

	/** ツリービューア */
	private RoleSettingTreeViewer m_viewer = null;
	/** ツリービュ */
	private RoleSettingTreeView m_view = null;
	/** 選択ジョブツリーアイテム */
	private RoleTreeItemWrapper m_selectItem = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public RoleSettingTreeComposite(RoleSettingTreeView view , Composite parent, int style) {
		super(parent, style);
		this.m_view = view;
		initialize();
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public RoleSettingTreeComposite(Composite parent, int style) {
		super(parent, style);

		initialize();
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 * @param selectItem 選択したノード
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public RoleSettingTreeComposite(Composite parent, int style, RoleTreeItemWrapper selectItem) {
		super(parent, style);

		m_selectItem = selectItem;
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Tree tree = new Tree(this, SWT.SINGLE | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, tree);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tree.setLayoutData(gridData);

		m_viewer = new RoleSettingTreeViewer(tree);
		m_viewer.setContentProvider(new RoleSettingTreeContentProvider());
		m_viewer.setLabelProvider(new RoleSettingTreeLabelProvider());

		// 選択アイテム取得イベント定義
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				m_selectItem = (RoleTreeItemWrapper) selection.getFirstElement();
			}
		});

		// ダブルクリックしたらジョブを開く
		m_viewer.addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						StructuredSelection selection = (StructuredSelection) event.getSelection();
						RoleTreeItemWrapper item = (RoleTreeItemWrapper) selection.getFirstElement();
						Object data = item.getData();
						if (data instanceof RoleInfoResponse
								&& !((RoleInfoResponse)data).getRoleId().equals(RoleSettingTreeConstant.ROOT_ID)
								&& !((RoleInfoResponse)data).getRoleId().equals(RoleSettingTreeConstant.MANAGER)) {
							RoleTreeItemWrapper manager = RoleSettingTreeView.getManager(item);
							RoleInfoResponse info = (RoleInfoResponse)manager.getData();
							String managerName = info.getRoleName();
							RoleSettingDialog dialog = new RoleSettingDialog(
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									managerName,
									((RoleInfoResponse)data).getRoleId());
							//ダイアログ表示
							if (dialog.open() == IDialogConstants.OK_ID) {
								m_view.update();
							}
						} else {
							return;
						}
					}
				});

		update();
	}

	/**
	 * このコンポジットが利用するツリービューアを返します。
	 *
	 * @return ツリービューア
	 */
	public RoleSettingTreeViewer getTreeViewer() {
		return m_viewer;
	}

	/**
	 * このコンポジットが利用するツリーを返します。
	 *
	 * @return ツリー
	 */
	public Tree getTree() {
		return m_viewer.getTree();
	}

	/**
	 * ツリービューアーを更新します。<BR>
	 * ツリー情報を取得し、ツリービューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>ロールツリー情報を取得します。</li>
	 * <li>ツリービューアーにロールツリー情報をセットします。</li>
	 * </ol>
	 *
	 */
	@Override
	public void update() {
		RoleTreeItemResponseP1 tree = null;

		Map<String, RoleTreeItemWrapper> dispDataMap= new ConcurrentHashMap<String, RoleTreeItemWrapper>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		//　ロール一覧情報取得
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
				tree = wrapper.getRoleTree();

				// RoleTreeItemResponseP1 を RoleTreeItemWrapper形式に変換
				RoleTreeItemWrapper convTree = ConvertRoleTreeItem(tree);
				dispDataMap.put(managerName, convTree);

			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("update() getJobTree, " +e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
			}

		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		m_selectItem = null;

		//ツリーの再構築
		RoleTreeItemWrapper newTree = new RoleTreeItemWrapper();
		RoleTreeItemWrapper roleTree = new RoleTreeItemWrapper();

		for(Map.Entry<String, RoleTreeItemWrapper> map : dispDataMap.entrySet()) {
			RoleTreeItemWrapper orgTree = new RoleTreeItemWrapper();
			orgTree.getChildren().add(map.getValue());
			
			//トップ
			if(newTree.getData() == null) {
				newTree.setData(orgTree.getData());
			}

			//"ロール"
			if(newTree.getChildren().isEmpty() == false) {
				roleTree = newTree.getChildren().get(0);
			} else {
				Object obj = orgTree.getChildren().get(0).getData();
				((RoleInfoResponse)obj).setRoleName(HinemosMessage.replace(((RoleInfoResponse)obj).getRoleName()));
				roleTree.setData(obj);
			}

			//マネージャ
			RoleInfoResponse role = new RoleInfoResponse();
			role.setRoleId(RoleSettingTreeConstant.MANAGER);
			role.setRoleName(map.getKey());
			RoleTreeItemWrapper managerTree = new RoleTreeItemWrapper();
			managerTree.setData(role);

			//詳細設定
			for(RoleTreeItemWrapper t : orgTree.getChildren().get(0).getChildren()) {
				managerTree.getChildren().add(t);
				t.setParent(managerTree);
			}
			roleTree.getChildren().add(managerTree);
			managerTree.setParent(roleTree);

			if(newTree.getChildren().isEmpty()) {
				newTree.getChildren().add(roleTree);
			}
		}
		
		// ロールツリーの展開情報を取得
		ArrayList<String> expandIdList = new ArrayList<String>();
		for (Object item : m_viewer.getExpandedElements()) {
			RoleInfoResponse roleInfo = (RoleInfoResponse)((RoleTreeItemWrapper)item).getData();
			expandIdList.add(roleInfo.getRoleId());
		}
		
		m_viewer.setInput(newTree);
		
		// ロールツリーの展開
		if (expandIdList.size() != 0) {
			// 前回展開されたところまで展開
			expand(newTree, expandIdList);
		} else {
			// ロールまで展開
			m_viewer.expandToLevel(3);
		}
	}

	/**
	 * ロールツリーを展開します。
	 */
	private void expand(RoleTreeItemWrapper item, List<String> expandIdList) {
		if (item.getData() != null) {
			if (item.getData() instanceof RoleInfoResponse
					&& expandIdList.contains(((RoleInfoResponse)item.getData()).getRoleId())) {
				// ツリーを展開（RoleInfo）
				m_viewer.expandToLevel(item, 1);
			} else if(item.getData() instanceof UserInfoResponse
					&& expandIdList.contains(((UserInfoResponse)item.getData()).getUserId())) {
				// ツリーを展開（UserInfo）
				m_viewer.expandToLevel(item, 1);
				// 最下層のため処理終了
				return;
			}
		}
		for (RoleTreeItemWrapper child : item.getChildren()) {
			expand(child, expandIdList);
		}
	}

	/**
	 * 選択ロールツリーアイテムを返します。
	 *
	 * @return ロールツリーアイテム
	 */
	public RoleTreeItemWrapper getSelectItem() {
		return m_selectItem;
	}

	/**
	 * 選択ロールツリーアイテムを設定
	 *
	 * @param item ロールツリーアイテム
	 */
	public void setSelectItem(RoleTreeItemWrapper item) {
		m_selectItem = item;
	}
	
	private RoleTreeItemWrapper ConvertRoleTreeItem(RoleTreeItemResponseP1 targetItem ) {
		RoleTreeItemWrapper top = new RoleTreeItemWrapper();
		setRecurciveRoleTreeItem(targetItem,top);
		return top;
	}
	private void setRecurciveRoleTreeItem(RoleTreeItemResponseP1 targetItem , RoleTreeItemWrapper destItem ) {
		if( targetItem.getType() != null && targetItem.getType().equals(RoleTreeItemResponseP1.TypeEnum.ROLE_INFO) ){
			RoleInfoResponse item = new RoleInfoResponse();
			item.setRoleId(targetItem.getId());
			item.setRoleName(targetItem.getName());
			item.setRoleType(RoleTypeEnum.USER_ROLE);
			destItem.setData(item);
		}
		if( targetItem.getType() != null && targetItem.getType().equals(RoleTreeItemResponseP1.TypeEnum.USER_INFO) ){
			UserInfoResponse item = new UserInfoResponse();
			item.setUserId(targetItem.getId());
			item.setUserName(targetItem.getName());
			destItem.setData(item);
		}
		for( RoleTreeItemResponseP1 srcRec : targetItem.getChildren() ){
			RoleTreeItemWrapper destRec = new RoleTreeItemWrapper();
			destRec.setParent(destItem);
			destItem.getChildren().add(destRec);
			setRecurciveRoleTreeItem(srcRec,destRec);
		}
	}
	
}
