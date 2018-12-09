/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.nodemap.composite.ScopeComposite;
import com.clustercontrol.nodemap.view.EventViewM;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.nodemap.view.ScopeTreeView;
import com.clustercontrol.nodemap.view.StatusViewM;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.nodemap.NodeMapException;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * ビュー連携を図るユーティリティクラス。
 * 
 * 「複数」のビューにまたがる処理は、このクラスで処理する事。
 * @since 1.0.0
 */
public class RelationViewController {

	// ログ
	private static Log m_log = LogFactory.getLog( RelationViewController.class );

	/**
	 * イベントビューとステータスビューの表示を変更する。
	 * 引数のファシリティID配下のイベント履歴やステータス履歴が表示されるようになる。
	 * @param facilityId
	 * @param selectItem	nullの場合、イベントビューの「スコープ：」という箇所の表示が微妙におかしくなる。
	 * 						(スコープツリービューが存在しない場合は、selectItemがnullになる。)
	 */
	public static void updateStatusEventView(String facilityId, String parentId) {
		if (facilityId == null) {
			m_log.warn("updateStatusEventView(), RelationViewController updateStatusEventView facility Id is null");
			return;
		}
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page == null) {
			m_log.warn("updateStatusEventView(), updateScopeTreeView page is null");
			return;
		}
		IWorkbenchPart activePart = page.getActivePart();
		if (activePart == null) {
			m_log.warn("updateStatusEventView(), updateScopeTreeView ActivePart is null");
			return;
		}
		m_log.debug("ActivePart-class is " + activePart.getClass().getSimpleName() +
				", " + activePart.getTitle());
		if (!(activePart instanceof NodeMapView)) {
			m_log.debug("RelationViewController this class is not NodeMapView-class");
			return;
		} else {
			NodeMapView view = (NodeMapView)activePart;
			if (!view.getMode().equals(NodeMapView.Mode.FIXED_MODE) &&
					!view.getMode().equals(NodeMapView.Mode.LIST_MODE)) {
				m_log.debug("not view-relation causes FIXED_MODE,LIST_MODE");
				return;
			}
		}
		IViewPart viewPart = null;
		IViewReference viewReference = null;

		try {
			viewReference = page.findViewReference(StatusViewM.ID);
			if (viewReference == null) {
				m_log.debug("RelationViewController(status) viewReference is null");
			} else {
				viewPart = viewReference.getView(false);
				if (viewPart == null) {
					m_log.debug("StatusView is null");
					page.showView(StatusViewM.ID, null, IWorkbenchPage.VIEW_CREATE);
				}
				
				viewReference = page.findViewReference(StatusViewM.ID);
				if (viewReference == null) {
					m_log.debug("viewReference is null");
					return;
				}
				viewPart = viewReference.getView(false);
				if (viewPart != null && viewPart instanceof StatusViewM) {
					StatusViewM view = (StatusViewM) viewPart;

					/*
					 * TreeItem topItem = view.getScopeComposite().getTree().getTopItem();
					 * getTopItem()は利用しないこと。
					 * getTopItemは表示されている箇所のtopが取得される。
					 * そのため、COMPOSITEが非表示(スクロールしなければ見えない)の場合は、
					 * getTopItemでCOMPOSITEが取得できない。
					 * 見える範囲の一番上が取得されてしまう。
					 * 
					 * getItems()[0]を利用すれば良さそう。
					 */
					TreeItem[] topItemArray = view.getScopeTreeComposite().getTree().getItems();
					if (topItemArray.length == 0) {
						m_log.debug("updateScopeTreeView treeItem.length = 0");
						return ;
					}
					TreeItem topItem = topItemArray[0];
					FacilityTreeItem fti = (FacilityTreeItem)topItem.getData();
					FacilityTreeItem selectItem = selectTreeItem(fti, parentId, facilityId);

					// スコープツリービューの該当アイテムにフォーカスを当てる。
					FacilityTreeComposite composite = view.getScopeTreeComposite();
					FacilityTreeItem alreadyItem = composite.getSelectItem();
					composite.setSelectItem(selectItem);
					if ((alreadyItem == null && selectItem != null) 
							|| (alreadyItem != null && selectItem != null 
								&& !alreadyItem.getData().getFacilityId().equals(selectItem.getData().getFacilityId()))) {
						composite.getTreeViewer().setSelection(new StructuredSelection(selectItem), true);
					}
				} else {
					m_log.debug("RelationViewController(status) viewPart is null");
				}
			}
		} catch (Exception e) {
			m_log.info(e.getMessage());
		}
		
		try {
			viewReference = page.findViewReference(EventViewM.ID);
			if (viewReference == null) {
				m_log.debug("RelationViewController(status) viewReference is null");
			} else {
				viewPart = viewReference.getView(false);
				if (viewPart == null) {
					m_log.debug("EventView is null");
					page.showView(EventViewM.ID, null, IWorkbenchPage.VIEW_CREATE);
				}
				
				viewReference = page.findViewReference(EventViewM.ID);
				if (viewReference == null) {
					m_log.debug("viewReferece is null");
					return;
				}
				viewPart = viewReference.getView(false);
				if (viewPart != null && viewPart instanceof EventViewM) {
					EventViewM view = (EventViewM)viewPart;
					

					/*
					 * TreeItem topItem = view.getScopeComposite().getTree().getTopItem();
					 * getTopItem()は利用しないこと。
					 * getTopItemは表示されている箇所のtopが取得される。
					 * そのため、COMPOSITEが非表示(スクロールしなければ見えない)の場合は、
					 * getTopItemでCOMPOSITEが取得できない。
					 * 見える範囲の一番上が取得されてしまう。
					 * 
					 * getItems()[0]を利用すれば良さそう。
					 */
					TreeItem[] topItemArray = view.getScopeTreeComposite().getTree().getItems();
					if (topItemArray.length == 0) {
						m_log.debug("updateScopeTreeView treeItem.length = 0");
						return ;
					}
					TreeItem topItem = topItemArray[0];
					FacilityTreeItem fti = (FacilityTreeItem)topItem.getData();
					FacilityTreeItem selectItem = selectTreeItem(fti, parentId, facilityId);

					// スコープツリービューの該当アイテムにフォーカスを当てる。
					FacilityTreeComposite composite = view.getScopeTreeComposite();
					FacilityTreeItem alreadyItem = composite.getSelectItem();
					composite.setSelectItem(selectItem);
					if ((alreadyItem == null && selectItem != null) 
							|| (alreadyItem != null && selectItem != null 
								&& !alreadyItem.getData().getFacilityId().equals(selectItem.getData().getFacilityId()))) {
						composite.getTreeViewer().setSelection(new StructuredSelection(selectItem), true);
					}
				} else {
					m_log.debug("RelationViewController(event) viewPart is null");
				}
			}
		} catch (Exception e) {
			m_log.info(e.getMessage(), e);
		}

	}

	/**
	 * スコープツリービューの表示を変更する。
	 * @param facilityId
	 */
	public static FacilityTreeItem updateScopeTreeView(String parentId, String facilityId) {
		m_log.debug("updateScopeTreeView facilityId:" + facilityId + ", parentId:" + parentId);
		FacilityTreeItem selectItem = null;
		if (facilityId == null) {
			m_log.warn("updateScopeTreeView(), RelationViewController updateScopeView facility Id is null");
			return null;
		}
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page == null) {
			m_log.warn("updateScopeTreeView(), updateScopeTreeView page is null");
			return null;
		}
		IViewReference viewReference = page.findViewReference(ScopeTreeView.ID);
		if (viewReference == null){
			m_log.debug("updateScopeTreeView viewPreference is null");
			return null;
		}
		IViewPart viewPart = viewReference.getView(false);
		if (viewPart != null && viewPart instanceof ScopeTreeView) {
			// FacilityTreeItem を取得する。
			ScopeTreeView view = (ScopeTreeView) viewPart;
			/*
			 * TreeItem topItem = view.getScopeComposite().getTree().getTopItem();
			 * getTopItem()は利用しないこと。
			 * getTopItemは表示されている箇所のtopが取得される。
			 * そのため、COMPOSITEが非表示(スクロールしなければ見えない)の場合は、
			 * getTopItemでCOMPOSITEが取得できない。
			 * 見える範囲の一番上が取得されてしまう。
			 * 
			 * getItems()[0]を利用すれば良さそう。
			 */
			TreeItem[] topItemArray = view.getScopeComposite().getTree().getItems();
			if (topItemArray.length == 0) {
				m_log.debug("updateScopeTreeView treeItem.length = 0");
				return null;
			}
			TreeItem topItem = topItemArray[0];
			FacilityTreeItem fti = (FacilityTreeItem)topItem.getData();
			selectItem = selectTreeItem(fti, parentId, facilityId);

			// スコープツリービューの該当アイテムにフォーカスを当てる。
			ScopeComposite composite = view.getScopeComposite();
			composite.setSelectItem(selectItem);
			if (selectItem != null) {
				composite.getTreeViewer().setSelection(new StructuredSelection(selectItem), true);
			}
		} else {
			m_log.warn("updateScopeTreeView(), updateScopeTreeView ScopeTreeView is null");
		}
		return selectItem;
	}

	/**
	 * 再帰用メソッド
	 * スコープツリーをサーチして、parentId,facilityIdが一致したFacilityTreeItemを取得する。
	 * 
	 * parent
	 *   この配下をサーチする。子メソッドではparentが変更される。
	 * parentId, facilityId
	 *   このペアが一致している箇所を探す。子メソッドではparentId, facilityIdは変更されない。
	 * 
	 */
	private static FacilityTreeItem selectTreeItem(FacilityTreeItem parent,
			String parentId, String facilityId) {
		/*
		 * ROOT_SCOPEの場合はtop。
		 */
		if (ReservedFacilityIdConstant.ROOT_SCOPE.equals(facilityId)) {
			
			return parent;
		}
		for (FacilityTreeItem item : parent.getChildren()) {
			if (item == null) {
				continue;
			}
			if (item.getData() == null) {
				continue;
			}
			if (facilityId.equals(item.getData().getFacilityId()) &&
					(parentId == null ||
							parentId.equals("") ||
							parentId.equals(item.getParent().getData().getFacilityId()) || 
							parentId.equals(ReservedFacilityIdConstant.ROOT_SCOPE))) {
				return item;
			}

			FacilityTreeItem ret = selectTreeItem(item, parentId, facilityId);
			if(ret != null) {
				return ret;
			}
		}
		return null;
	}

	/**
	 * 新規ビューを作成しマップを表示します。
	 * 遷移先のスコープのビューが他のビューとして既に開かれている場合は、
	 * ビュー内で遷移せず、そのビューにフォーカスをあわせる。
	 * 
	 * @param targetScopeFacilityId 新規作成ビューの表示対象スコープのファシリティID
	 * @throws NodeMapException
	 */
	public static void createNewView(String managerName, String targetScopeFacilityId) {

		long start = System.currentTimeMillis();

		// 新規に表示対象のビューが既に開かれていないか確認
		String existSecondaryId = SecondaryIdMap.getSecondaryId(managerName, targetScopeFacilityId);
		m_log.debug("existSecondaryId:" + existSecondaryId);

		if(existSecondaryId != null){
			// 既に存在するビューにフォーカスをあわせる
			openExistView(existSecondaryId, managerName, targetScopeFacilityId);
		} else {
			// 既存のビューがないためビューを新規に生成
			try {
				// 描画対象スコープのFacilityIDからビューのSecondaryIdを特定できるよう、
				// FacilityIDをキーにSecondaryIdを登録する
				String newSecondaryId = SecondaryIdMap.createSecondaryId(managerName, targetScopeFacilityId);
				m_log.debug("createNewView showView " +
						"secondaryId=" + newSecondaryId +
						" facilityId=" + targetScopeFacilityId);
				showNewView(managerName, newSecondaryId);

			} catch (Exception e) {
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						e.getMessage() + " " + e.getClass().getSimpleName());
			}
		}
		long end = System.currentTimeMillis();
		m_log.debug("OpenNodeMap :" + (end - start) +"ms");
	}

	public static void showNewView(String managerName, String newSecondaryId) {
		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			NodeMapView view = (NodeMapView)page.showView(NodeMapView.ID,
					newSecondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			view.m_canvasComposite.setManagerName(managerName);
			view.setFocus();
			view.setMode(NodeMapView.Mode.FIXED_MODE);
			m_log.debug("showNewView " + newSecondaryId);
		} catch (PartInitException e) {
			m_log.warn("showNewView(), " + e.getMessage(), e);
		}
	}

	/**
	 * 既に存在するページにフォーカスを移す
	 * 
	 * @param secondaryId 表示対象ビューのSecondaryId
	 * @param targetScopeFacilityId 描画対象スコープのファシリティID
	 */
	private static void openExistView(String secondaryId, String managerName, String targetScopeFacilityId) {
		m_log.debug("openExistView secondaryId = " + secondaryId);

		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		// 既に開かれているためそのビューを取得する
		IViewReference viewReference = page.findViewReference(NodeMapView.ID, secondaryId);

		// 実際に存在した場合はそのビューをフォーカスする
		if(viewReference != null){
			// 既に存在するビューにフォーカスを移す
			try {
				NodeMapView view = (NodeMapView)page.showView(NodeMapView.ID,
						secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
				view.m_canvasComposite.setManagerName(managerName);
				view.setFocus();
			} catch (PartInitException e) {
				m_log.warn("openExistView(), " + e.getMessage(), e);
			}
		} else {
			// エラーハンドリング
			// m_secondaryIdMap上はスコープに対応するビューのSecondaryIdの情報があるにも関わらず、
			// 実際にはビューの取得ができていないため、マップを更新する。
			SecondaryIdMap.removeSecondaryId(secondaryId);
		}
	}
	
	/**
	 * スコープツリービューを取得します。
	 * @param parentId
	 * @param facilityId
	 * @return
	 */
	public static FacilityTreeItem getScopeTreeView(String parentId, String facilityId) {
		m_log.debug("updateScopeTreeView " + facilityId);
		FacilityTreeItem selectItem = null;
		if (facilityId == null) {
			m_log.warn("updateScopeTreeView(), RelationViewController updateScopeView facility Id is null");
			return null;
		}
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page == null) {
			m_log.warn("updateScopeTreeView(), updateScopeTreeView page is null");
			return null;
		}
		IViewReference viewReference = page.findViewReference(ScopeTreeView.ID);
		if (viewReference == null){
			m_log.debug("updateScopeTreeView viewPreference is null");
			return null;
		}
		IViewPart viewPart = viewReference.getView(false);
		if (viewPart != null && viewPart instanceof ScopeTreeView) {
			// FacilityTreeItem を取得する。
			ScopeTreeView view = (ScopeTreeView) viewPart;
			/*
			 * TreeItem topItem = view.getScopeComposite().getTree().getTopItem();
			 * getTopItem()は利用しないこと。
			 * getTopItemは表示されている箇所のtopが取得される。
			 * そのため、COMPOSITEが非表示(スクロールしなければ見えない)の場合は、
			 * getTopItemでCOMPOSITEが取得できない。
			 * 見える範囲の一番上が取得されてしまう。
			 * 
			 * getItems()[0]を利用すれば良さそう。
			 */
			TreeItem[] topItemArray = view.getScopeComposite().getTree().getItems();
			if (topItemArray.length == 0) {
				m_log.debug("updateScopeTreeView treeItem.length = 0");
				return null;
			}
			TreeItem topItem = topItemArray[0];
			FacilityTreeItem fti = (FacilityTreeItem)topItem.getData();
			selectItem = selectTreeItem(fti, parentId, facilityId);
		} else {
			m_log.warn("updateScopeTreeView(), updateScopeTreeView ScopeTreeView is null");
		}
		return selectItem;
	}

}
