/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.utility.settings.ui.action.BuildFunctionTreeAction;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.bean.FuncTreeItem;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * 機能ツリー用のコンポジットクラスです。
 * 
 * @version 6.1.0
 * @since 1.2.0
 */
public class HinemosFuncTreeComposite extends Composite {
	/** ツリービューア */
	private TreeViewer m_viewer = null;

	/** チェックされている収集項目を保持 */
	private List<FuncInfo> itemList;

	/** イベントリスナ */
	//Listener subItemCheckListener;
	//Listener ItemCheckDisableListener;
	//Listener masterItemCheckListener;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *			親コンポジット
	 * @param style
	 *			スタイル
	 * 
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *  	style)
	 * @see #initialize()
	 */
	public HinemosFuncTreeComposite(Composite parent, int style) {
		super(parent, style);
		this.itemList = new ArrayList<FuncInfo>();
		initialize();
	}

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *			親コンポジット
	 * @param style
	 *			スタイル
	 * @param treeOnly
	 *			true：ツリーのみ、false：ジョブ情報を含む
	 * 
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *  	style)
	 * @see #initialize()
	 */
	/*
	public HinemosFuncTreeComposite(Composite parent, int style,
			boolean treeOnly) {
		super(parent, style);
		this.itemList = new ArrayList<FuncInfo>();
		initialize();
	}
	*/
	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *			親コンポジット
	 * @param style
	 *			スタイル
	 * @param parentId
	 *			親ID
	 * @param id
	 *			ID
	 * 
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *  	style)
	 * @see #initialize()
	 */
	/*
	public HinemosFuncTreeComposite(Composite parent, int style,
			String parentId, String id) {
		super(parent, style);
		this.itemList = new ArrayList<FuncInfo>();
		initialize();
	}
	*/
	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Composite managerCompoiste = new Composite(this, SWT.NONE);

		GridData gd_mngCmp = new GridData();
		gd_mngCmp.horizontalAlignment = GridData.FILL;
		gd_mngCmp.grabExcessHorizontalSpace = true;
		gd_mngCmp.horizontalSpan = 1;
		managerCompoiste.setLayoutData(gd_mngCmp);

		GridLayout gl_mngCmp = new GridLayout(2, false);
		managerCompoiste.setLayout(gl_mngCmp);
		gl_mngCmp.marginHeight = 5;
		gl_mngCmp.marginWidth = 5;
		
		Label label = new Label(managerCompoiste, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("string.manager") + " : ");
		
		Combo combo = new Combo(managerCompoiste, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		combo.setLayoutData(gridData);
		
		if(UtilityManagerUtil.getCurrentManagerName() != null){
			String name = UtilityManagerUtil.getCurrentManagerName();
			combo.add(name);
			combo.setText(name);
		}
		
		combo.addFocusListener(new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e) {
				Combo combo = (Combo)e.getSource();
				combo.removeAll();
				for(String mngName: RestConnectManager.getActiveManagerNameList()){
					combo.add(mngName);
				};
				if(combo.indexOf(UtilityManagerUtil.getCurrentManagerName()) != -1){
					combo.select(combo.indexOf(UtilityManagerUtil.getCurrentManagerName()));
				}
			}
		});
		
		combo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo)e.getSource();
				UtilityManagerUtil.setCurrentManagerName(combo.getText());
			}
		});
		
		Tree tree = new Tree(this, SWT.SINGLE | SWT.BORDER | SWT.CHECK);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		tree.setLayoutData(gridData);

		m_viewer = new TreeViewer(tree);
		m_viewer.setContentProvider(new HinemosFuncTreeContentProvider());
		m_viewer.setLabelProvider(new HinemosFuncTreeLabelProvider());
		
		this.m_viewer.getTree().addListener(SWT.Selection,
				//subItemCheckListener =
				new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (event.detail == SWT.CHECK) {
							// その項目の子供すべてのチェックを同期する。
							setChecked((TreeItem) event.item,
									((TreeItem) event.item).getChecked());
							// 現在のTreeの状態で選択されている項目のリストを更新する
							itemList.clear();
							treeToList(getTree().getItems());
						} else {
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
	public TreeViewer getTreeViewer() {
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
	 * ジョブツリー情報を取得し、ツリービューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>ジョブツリー情報を取得します。</li>
	 * <li>ツリービューアーにジョブツリー情報をセットします。</li>
	 * </ol>
	 * 
	 * @see com.clustercontrol.jobmanagement.action.GetJobTree#getJobTree(boolean)
	 */
	@Override
	public void update() {
		
		//ツリーを作ります。
		
		

		m_viewer.setInput(BuildFunctionTreeAction.buildTree());

		// ジョブユニットのレベルまで展開
		m_viewer.expandToLevel(3);
	}

	private void setChecked(TreeItem treeItem, boolean checked) {
		treeItem.setExpanded(true); // 子要素を展開する
		treeItem.setChecked(checked); // チェックを入れる

		TreeItem[] children = treeItem.getItems();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != null) {
				setChecked(children[i], checked);
			}
		}
	}

	/**
	 * GUI上でチェックされている項目の状態をフィールド変数itemListに反映します。
	 * @param treeItems
	 */
	private void treeToList(TreeItem[] treeItems) {
		for (int i = 0; i < treeItems.length; i++) {
			if (treeItems[i] != null) {
				if (treeItems[i].getChecked()) {
					// ツリーが保持しているデータを取得
					 
					FuncInfo treeItem = ((FuncTreeItem)treeItems[i].getData()).getData();
					// 項目がカテゴリではない場合
					if (treeItem.getType() == true) {
						this.itemList.add(treeItem);
					}
				}
				// 子要素を再帰的に処理
				TreeItem[] children = treeItems[i].getItems();
				if (children != null) {
					treeToList(children);
				}
			}
		}
	}
	/**
	 * 選択されている機能のリストを返します。
	 * @return
	 */
	public  List<FuncInfo> getCheckedFunc(){
		return this.itemList;
	}
	
	/**
	 * オブジェクト権限を返します。
	 * @return
	 */
	public FuncInfo getObjectPrivilegeFunc(){
		FuncTreeItem funcTreeItem = BuildFunctionTreeAction.getFuncTreeObjectPrivilege();
		return funcTreeItem.getData();
	}
}