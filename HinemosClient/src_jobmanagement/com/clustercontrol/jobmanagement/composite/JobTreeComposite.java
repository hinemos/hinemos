/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.openapitools.client.model.JobTreeItemResponseP1;
import org.openapitools.client.model.JobTreeItemResponseP2;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.viewer.JobTreeContentProvider;
import com.clustercontrol.jobmanagement.viewer.JobTreeLabelProvider;
import com.clustercontrol.jobmanagement.viewer.JobTreeViewer;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * ジョブツリー用のコンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobTreeComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( JobTreeComposite.class );

	/** ツリービューア */
	protected JobTreeViewer m_viewer = null;
	/** 選択ジョブツリーアイテムリスト */
	protected List<JobTreeItemWrapper> m_selectItemList = new ArrayList<JobTreeItemWrapper>();
	/** ツリーのみ */
	private boolean m_treeOnly = false;
	private String m_jobId = null;
	protected boolean m_useForView = false; //ジョブ[一覧]ビュー、ジョブツリービュー(ジョブマップオプション)で使うか
	private String managerName = null;

	/** Enable key press on search bar */
	protected boolean enableKeyPress = false;

	/**
	 * 表示ツリーの形式
	 * 値として、JobConstantクラスで定義したものが入る
	 * @see com.clustercontrol.jobmanagement.bean.JobConstant
	 *  null : 選択したユニット、ネットの子のみ表示する
	 *  TYPE_REFERJOB,TYPE_REFERJOBNET		: 選択したユニット、ネットの所属するジョブユニット以下すべて表示する
	 */
	private JobInfoWrapper.TypeEnum m_mode = null;

	/**
	 * 表示するジョブ種別のリスト
	 * 値として、JobConstantクラスで定義したものが入る
	 * @see com.clustercontrol.jobmanagement.bean.JobConstant
	 *  null : 全てのユニット、ネット
	 */
	private List<JobInfoWrapper.TypeEnum> m_targetJobTypeList = null;

	/** オーナーロールID */
	private String ownerRoleId = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親コンポジット
	 * @param style
	 *            スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public JobTreeComposite(Composite parent, int style, String ownerRoleId) {
		super(parent, style);

		m_treeOnly = false;
		this.ownerRoleId = ownerRoleId;
		m_useForView = true;
		enableKeyPress = true;
		initialize();
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親コンポジット
	 * @param style
	 *            スタイル
	 * @param treeOnly
	 *            true：ツリーのみ、false：ジョブ情報を含む
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public JobTreeComposite(Composite parent, int style, String managerName, String ownerRoleId, boolean treeOnly,
			boolean useForView) {
		super(parent, style);

		this.managerName = managerName;
		m_treeOnly = treeOnly;
		this.ownerRoleId = ownerRoleId;
		m_useForView = useForView;
		initialize();
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親コンポジット
	 * @param style
	 *            スタイル
	 * @param parentJobId
	 *            親ジョブID
	 * @param jobId
	 *            ジョブID
	 * @param mode
	 *            表示元ジョブ種別
	 * @param targetJobTypeList
	 *            表示対象のジョブ種別
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public JobTreeComposite(Composite parent, int style, String ownerRoleId, JobTreeItemWrapper jobTreeItem, JobInfoWrapper.TypeEnum mode,
			List<JobInfoWrapper.TypeEnum> targetJobTypeList) {
		super(parent, style);

		JobTreeItemWrapper selectItem = jobTreeItem;
		m_treeOnly = true;
		m_selectItemList.add(selectItem);
		m_jobId = selectItem.getData().getId();
		m_mode = mode;
		m_targetJobTypeList = targetJobTypeList;
		this.ownerRoleId = ownerRoleId;
		m_useForView = false;
		initialize();
	}

	private JobTreeItemWrapper searchNeighbors( JobTreeItemWrapper current, String keyword ){
		JobTreeItemWrapper found;
		JobTreeItemWrapper parent = current.getParent();
		if( null != parent ){
			do{
				int offset = parent.getChildren().indexOf( current ) + 1;
				found = searchChildren( parent, keyword, offset );
				if( null != found ){
					return found;
				}
				current = parent;
				parent = current.getParent();
			}while( null != parent );
		}
		return null;
	}

	private JobTreeItemWrapper searchChildren( JobTreeItemWrapper parent, String keyword, int offset ){
		List<JobTreeItemWrapper> children = parent.getChildren();
		int len = children.size();
		for( int i = offset; i<len; i++ ){
			JobTreeItemWrapper child = children.get(i);

			if( -1 != child.getData().getId().indexOf( keyword ) ){
				return child;
			}else{
				JobTreeItemWrapper found = searchChildren( child, keyword, 0 );
				if( null != found ){
					return found;
				}
			}
		}
		return null;
	}

	private JobTreeItemWrapper searchItem( JobTreeItemWrapper item, String keyword ){
		JobTreeItemWrapper found;

		// 1. Search children
		found= searchChildren(item, keyword, 0);
		if( null != found ){
			return found;
		}	

		// 2. If not found in children, search in neighbors
		found = searchNeighbors( item, keyword );
		if( null != found ){
			return found;
		}

		return null;
	}

	public void doSearch( String keyword ){
		// Check and format keyword
		if( null == keyword ){
			return;
		}
		keyword = keyword.trim();
		if( keyword.isEmpty() ){
			return;
		}

		StructuredSelection selection = (StructuredSelection) m_viewer.getSelection();
		Object targetItem = selection.getFirstElement();
		JobTreeItemWrapper result = searchItem( (JobTreeItemWrapper)( null != targetItem ? targetItem: m_viewer.getInput() ), keyword );
		if( null != result ){
			JobTreeItemWrapper trace = result;
			LinkedList<JobTreeItemWrapper> pathList = new LinkedList<>();
			do{
				pathList.addFirst( trace );
				trace = trace.getParent();
			}while( null != trace );
			TreePath path = new TreePath( pathList.toArray(new JobTreeItemWrapper[]{}) );
			m_viewer.setSelection( new TreeSelection(path), true );
		}else{
			MessageDialog.openInformation( this.getShell(), Messages.getString("message"), Messages.getString("search.not.found") );
			m_viewer.setSelection( new StructuredSelection(((JobTreeItemWrapper)m_viewer.getInput()).getChildren().get(0)), true );
		}
	}

	/**
	 * コンポジットを構築します。
	 */
	protected void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// Add search bar
		Composite compSearch = new TreeSearchBarComposite(this, SWT.NONE, enableKeyPress);
		WidgetTestUtil.setTestId(this, "search", compSearch);
		compSearch.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );

		Tree tree = new Tree(this, SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, tree);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tree.setLayoutData(gridData);

		m_viewer = new JobTreeViewer(tree);
		m_viewer.setContentProvider(new JobTreeContentProvider());
		m_viewer.setLabelProvider(new JobTreeLabelProvider(m_useForView));

		// 選択アイテム取得イベント定義
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				m_selectItemList.clear();
				m_selectItemList.addAll(selection.toList());
			}
		});

		// ダブルクリックしたらジョブを開く
		if (m_useForView) {
			m_viewer.addDoubleClickListener( new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					JobTreeItemWrapper item = (JobTreeItemWrapper) selection.getFirstElement();
					JobInfoWrapper.TypeEnum type = item.getData().getType();
					m_log.info("double click. type=" + type);
					if (type != JobInfoWrapper.TypeEnum.REFERJOB &&
						type != JobInfoWrapper.TypeEnum.REFERJOBNET &&
						type != JobInfoWrapper.TypeEnum.APPROVALJOB &&
						type != JobInfoWrapper.TypeEnum.FILEJOB &&
						type != JobInfoWrapper.TypeEnum.MONITORJOB &&
						type != JobInfoWrapper.TypeEnum.FILECHECKJOB &&
						type != JobInfoWrapper.TypeEnum.JOBLINKSENDJOB &&
						type != JobInfoWrapper.TypeEnum.JOBLINKRCVJOB &&
						type != JobInfoWrapper.TypeEnum.RPAJOB &&
						type != JobInfoWrapper.TypeEnum.JOB &&
						type != JobInfoWrapper.TypeEnum.JOBUNIT &&
						type != JobInfoWrapper.TypeEnum.JOBNET &&
						type != JobInfoWrapper.TypeEnum.RESOURCEJOB) {
						return;
					}

					String managerName = null;
					JobTreeItemWrapper mgrTree = JobTreeItemUtil.getManager(item);
					if(mgrTree == null) {
						managerName = item.getChildren().get(0).getData().getId();
					} else {
						managerName = mgrTree.getData().getId();
					}
					JobEditState jobEditState = JobEditStateUtil.getJobEditState( managerName );
					boolean readOnly = !jobEditState.isLockedJobunitId(item.getData().getJobunitId());
					JobDialog dialog = new JobDialog(
							JobTreeComposite.this,
							JobTreeComposite.this.getShell(),
							managerName, readOnly);
					dialog.setJobTreeItem(item);
					//ダイアログ表示
					if (dialog.open() == IDialogConstants.OK_ID) {
						if (jobEditState.isLockedJobunitId(item.getData().getJobunitId())) {
							// 編集モードのジョブが更新された場合(ダイアログで編集モードになったものを含む）
							jobEditState.addEditedJobunit(item);
							if (item.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
								JobUtil.setJobunitIdAll(item, item.getData().getJobunitId());
							}
						}
						m_viewer.sort(item.getParent());
						m_viewer.refresh(item.getParent());
						m_viewer.refresh(item);
						m_viewer.setSelection(new StructuredSelection(item), true);
					}
				}
			});
		}

		updateTree(m_useForView);
	}

	/**
	 * このコンポジットが利用するツリービューアを返します。
	 *
	 * @return ツリービューア
	 */
	public JobTreeViewer getTreeViewer() {
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
		updateTree(false);
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
	 * @param useChache キャッシュがあればそれを利用する(ジョブ[一覧]ビュー以外では考慮されない)
	 */
	public void updateTree(boolean useChache) {
		// 非Javadoc 継承の関係でupdate(boolean)はoverrideの警告が出るのでメソッド名を変更した
		List<JobTreeItemWrapper> jobTreeList = new ArrayList<JobTreeItemWrapper>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		//　ジョブ一覧情報取得
		if (m_useForView) {
			if( useChache ){
				JobTreeItemWrapper item = JobEditStateUtil.getJobTreeItem();
				if( null != item ){
					jobTreeList.add(item);
				}
			}
			if (jobTreeList.isEmpty()) {
				jobTreeList.add(JobEditStateUtil.updateJobTree(ownerRoleId, m_treeOnly));
			}
		} else if (m_jobId == null) {
			try {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(this.managerName);
				long start = System.currentTimeMillis();
				JobTreeItemWrapper res =null;
				JobTreeItemResponseP1 orgTreeRes = wrapper.getJobTree(ownerRoleId);
				if( m_treeOnly ){
					res = JobTreeItemUtil.getItemFromP1(orgTreeRes);
				}else{
					//不要な情報を削り落として変換
					if(orgTreeRes.getData().getDescription() == null) {
						JobTreeItemResponseP2 dtoP2 = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTreeJobInfoFull(ownerRoleId);
						res = JobTreeItemUtil.getItemFromP2ForTreeView(dtoP2);
					} else {
						res = JobTreeItemUtil.getItemFromP1ForTreeView(orgTreeRes);
					}
				}
				jobTreeList.add(res);
				long end = System.currentTimeMillis();
				m_log.info("getJobTree time=" + (end - start) + "ms");
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("update() getJobTree, " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			//メッセージ表示
			if( 0 < errorMsgs.size() ){
				UIManager.showMessageBox(errorMsgs, true);
			}
		} else if (m_mode == JobInfoWrapper.TypeEnum.REFERJOB || m_mode == JobInfoWrapper.TypeEnum.REFERJOBNET) {
			for(JobTreeItemWrapper selectItem : m_selectItemList) {
				jobTreeList.add(getJobTreeOneUnit(selectItem));
			}
		} else {
			for(JobTreeItemWrapper selectItem : m_selectItemList) {
				jobTreeList.add(getJobTreeOneLevel(selectItem, m_targetJobTypeList));
			}
		}
		m_selectItemList.clear();

		for(JobTreeItemWrapper tree : jobTreeList) {
			if(tree == null) {
				continue;
			}

			m_viewer.setInput( tree );
			if( m_useForView ){
				// JobTreeViewerをまとめて更新するためのリスト
				// ビューを更新した場合は、開いているジョブツリーの情報を全て更新する
				JobTreeViewerList.setInput(m_viewer, tree);
			}
		}

		//ジョブユニットのレベルまで展開
		m_viewer.expandToLevel(3);
	}

	
	/**
	 * 引数のjobIdと一致するJobTreeItemにフォーカスを当てる
	 * itemには子がいるので、再帰呼び出しでループ
	 * @param treeList
	 * @param jobId
	 */
	public void setFocus(String managerName, String jobunitId, String jobId){
		JobTreeItemWrapper root = (JobTreeItemWrapper) m_viewer.getInput();
		
		for (JobTreeItemWrapper item1 : root.getChildren().get(0).getChildren()) {
			String name = item1.getData().getName();
			if (managerName.equals(name)) {
				for (JobTreeItemWrapper item2 : item1.getChildren()) {
					String unit = item2.getData().getJobunitId();
					if (jobunitId.equals(unit)) {
						setFocus(item2, jobId);
					}
				}
			}
		}
	}
	
	// 目的のジョブが見つかったらtrueを、見つからなかったらfalseを返す
	private boolean setFocus (JobTreeItemWrapper item, String jobId) {
		if (jobId.equals(item.getData().getId())) {
			m_viewer.setSelection(new StructuredSelection(item), true);
			return true;
		}
		
		for (JobTreeItemWrapper child : item.getChildren()) {
			if (setFocus(child, jobId)) {
				return true;
			}
			
		}
		return false;
	}

	/**
	 * 選択ジョブツリーアイテムリストを返します。
	 *
	 * @return ジョブツリーアイテムリスト
	 */
	public List<JobTreeItemWrapper> getSelectItemList() {
		return m_selectItemList;
	}

	/**
	 * 選択ジョブツリーアイテムを設定
	 *
	 * @param itemList ジョブツリーアイテムリスト
	 */
	public void setSelectItem(List<JobTreeItemWrapper> itemList) {
		if(itemList != null) {
			m_selectItemList.clear();
			m_selectItemList.addAll(itemList);
		}
	}
	/**
	 * ジョブ[一覧]ビューのジョブツリー情報から、<BR>
	 * 引数で渡された親ジョブIDの直下のジョブツリーアイテムを取得する。<BR><BR>
	 * 取得したジョブツリーアイテムから、<BR>
	 * 引数で渡されたジョブIDと一致するジョブツリーアイテムを除いたジョブツリーアイテムを返す。
	 * ジョブ種別の指定があった場合はその種別のみ取得する。
	 *
	 * @return ジョブツリー情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}の階層オブジェクト
	 */
	private JobTreeItemWrapper getJobTreeOneLevel(JobTreeItemWrapper self, List<JobInfoWrapper.TypeEnum> targetJobTypeList) {
		JobTreeItemWrapper parentOrg = self.getParent();

		// selfの親
		JobTreeItemWrapper parentClone = new JobTreeItemWrapper();
		parentClone.setData(copyJobInfo(parentOrg.getData()));

		// selfの兄弟
		String jobId = self.getData().getId();
		for (JobTreeItemWrapper brotherOrg : self.getParent().getChildren()) {
			if (jobId.equals(brotherOrg.getData().getId())) {
				continue;
			}
			if(targetJobTypeList != null){
				if(!targetJobTypeList.contains(brotherOrg.getData().getType())){
					continue;
				}
			}
			JobTreeItemWrapper brotherClone = new JobTreeItemWrapper();
			brotherClone.setParent(parentClone);
			parentClone.getChildren().add(brotherClone);
			brotherClone.setData(copyJobInfo(brotherOrg.getData()));
		}
		return parentClone;
	}
	/**
	 * 自分が所属するジョブユニット以下のJobTreeItemを取得する。
	 * @version 4.1.0
	 * @param self
	 * @return
	 */
	public JobTreeItemWrapper getJobTreeOneUnit(JobTreeItemWrapper self) {
		// selfの親
		JobTreeItemWrapper parentOrg = self.getParent();
		// selfの親のクローン
		JobTreeItemWrapper ret = new JobTreeItemWrapper();
		//selfの所属するジョブユニットのジョブID
		String jobUnitId = self.getData().getJobunitId();
		//selfの階層から、root階層まで、探索する
		while (!(parentOrg.getData().getId()).equals("")) {
			//
			if (parentOrg.getData().getId().equals(jobUnitId)) {
				ret.setData(copyJobInfo(parentOrg.getData()));
				break;
			}
			//現在のJobTreeItemの親を取得する
			parentOrg = parentOrg.getParent();
		}
		//選択したJobTreeItemのジョブID
		String jobId = self.getData().getId();
		//deep copy
		ret = cloneChildren(jobId,ret,parentOrg.getChildren());
		return ret;
	}
	/**
	 * 子が存在した場合、下位のJobTreeItemをdeep copyする
	 * @version 4.1.0
	 * @param id 選択したJobTreeItemのジョブID
	 * @param parent 参照している階層の親
	 * @param itemList
	 * @return
	 */
	private JobTreeItemWrapper cloneChildren(String id, JobTreeItemWrapper parent, List<JobTreeItemWrapper> itemList){
		for (JobTreeItemWrapper childrenOrg : itemList) {
			if (!id.equals(childrenOrg.getData().getId())) {
				JobTreeItemWrapper childrenClone = new JobTreeItemWrapper();

				childrenClone.setData(copyJobInfo(childrenOrg.getData()));
				if(!childrenOrg.getChildren().isEmpty()){
					childrenClone = cloneChildren(id,childrenClone,childrenOrg.getChildren());
				}
				childrenClone.setParent(parent);
				parent.getChildren().add(childrenClone);
			}
		}
		return parent;
	}

	/**
	 * 引数で渡されたジョブ情報のコピーインスタンスを作成する。
	 *
	 * @param orgInfo コピー元ジョブ情報
	 * @return ジョブ情報
	 */
	private JobInfoWrapper copyJobInfo(JobInfoWrapper orgInfo) {

		JobInfoWrapper info = JobTreeItemUtil.createJobInfoWrapper();
		info.setJobunitId(orgInfo.getJobunitId());
		info.setId(orgInfo.getId());
		info.setName(orgInfo.getName());
		info.setType(orgInfo.getType());
		info.setRegistered(orgInfo.getRegistered());

		return info;
	}

	/**
	 * 表示しているすべてのジョブツリーの表示をリフレッシュする
	 */
	public void refresh() {
		JobTreeViewerList.refresh();
	}

	/**
	 * 表示しているすべてのジョブツリーの表示をリフレッシュする
	 * @param element
	 */
	public void refresh( Object element ){
		JobTreeViewerList.refresh( element );
	}

	/**
	 * 現在クライアントが表示しているジョブツリーのリストにこのインスタンスのツリーを追加する<BR>
	 * ビューを開く際に呼ぶこと
	 */
	public void addToTreeViewerList() {
		JobTreeViewerList.add( this.m_viewer );
	}

	/**
	 * 現在クライアントが表示しているジョブツリーのリストからこのインスタンスのツリーを削除する<BR>
	 * ビューを閉じる際に呼ぶこと
	 */
	public void removeFromTreeViewerList() {
		JobTreeViewerList.remove( this.m_viewer );
		super.dispose();
	}

}
