/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.Command;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;

import com.clustercontrol.monitor.view.action.ScopeShowActionEvent;
import com.clustercontrol.monitor.view.action.ScopeShowActionStatus;
import com.clustercontrol.nodemap.view.EventViewM;
import com.clustercontrol.nodemap.view.NodeListView;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.nodemap.view.StatusViewM;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.view.CommonViewPart;

/**
 * 複数ノードマップビュー描画のためのID管理クラス。
 * @since 1.0.0
 */
public class SecondaryIdMap {

	// ログ
	private static Log m_log = LogFactory.getLog( SecondaryIdMap.class );

	private static String SECONDARY_ID_KEY_PREFIX = "secondary.id.";

	private static final Object _createSecondaryLock = new Object();

	// secondaryIdの上限。
	// 同時に開くことのできるノードマップビューはSECONDARY_ID_MAXだけ。
	// ビューを閉じればsecondaryIdは再利用されるので、64あれば十分だろう。
	private static final int SECONDARY_ID_MAX = 64;

	/*
	 * 描画対象スコープのファシリティIDとそれを描画しているビューのSecondaryIdをマップで保持
	 * Key : SecondaryId
	 * Value   : Viewクラス , ManagerId, FacilityId
	 * 注）ConcurrentHashMapでは、valueにもnullは許容されないため、
	 * SecondaryId が null のものは、m_secondaryIdMapでは、文字列"null"として登録する
	 */
	private final ConcurrentHashMap<String, ViewInfo> _secondaryIdMap = new ConcurrentHashMap<>();

	volatile private boolean initFlag = false;
	
	private static final String NODEMAP_PERSPECTIVE_PREFIX = "com.clustercontrol.enterprise.nodemap.NodeMapPerspective";
	
	private SecondaryIdMap() {};
	
	private static SecondaryIdMap getInstance() {
		return SingletonUtil.getSessionInstance(SecondaryIdMap.class);
	}

	/**
	 * プレファレンスからノードマップのセカンダリIDと表示対象ファシリティIDの対応情報を取得する
	 * １度だけ処理される
	 */
	public static void init(){
		if (getInstance().initFlag) {
			return;
		}
		getInstance().initFlag = true;
		m_log.debug("init called");
		
		// ワークベンチにパースペクティブを操作した場合のリスナを登録
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new PerspectiveAdapter(){

			@Override
			public void perspectiveClosed(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				m_log.debug("call perspectiveClosed()");
			}

			@Override
			public void perspectiveDeactivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				m_log.debug("call perspectiveDeactivated()");
			}

			@Override
			public void perspectiveOpened(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				m_log.debug("call perspectiveOpened()");
			}

			@Override
			public void perspectiveSavedAs(IWorkbenchPage page,
					IPerspectiveDescriptor oldPerspective,
					IPerspectiveDescriptor newPerspective) {
			}

			@Override
			public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective,
					IWorkbenchPartReference partRef, String changeId) {
				m_log.debug("call perspectiveChanged()");
			}

			@Override
			public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				m_log.debug("call perspectiveActivated()");

				if (page == null) {
					m_log.warn("perspectiveActivated(), NodeMapPerspective page is null");
					return;
				}

				/*
				 * ファシリティーツリーは非表示とする。
				 * StatusView
				 */
				m_log.debug("view class " + page.findViewReference(StatusViewM.ID));
				IViewReference viewReference = null;
				viewReference = page.findViewReference(StatusViewM.ID);
				if (viewReference != null) {
					StatusViewM statusView = (StatusViewM) viewReference.getView(true);
					if (statusView != null) {
						ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
						Command command = service.getCommand(ScopeShowActionStatus.ID);
						command.getState(RegistryToggleState.STATE_ID).setValue(false);
						service.refreshElements(ScopeShowActionStatus.ID, null);
						statusView.hide();
					} else {
						m_log.warn("perspectiveActivated(), statusView is null");
					}
				} else {
					m_log.warn("perspectiveActivated(), viewReference(status) is null");
				}
				/*
				 * ファシリティーツリーは非表示とする。
				 * EventView
				 */
				m_log.debug("view class " + page.findViewReference(EventViewM.ID));
				viewReference = page.findViewReference(EventViewM.ID);
				if (viewReference != null) {
					EventViewM eventView = (EventViewM) viewReference.getView(true);
					if (eventView != null) {
						ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
						Command command = service.getCommand(ScopeShowActionEvent.ID);
						command.getState(RegistryToggleState.STATE_ID).setValue(false);
						service.refreshElements(ScopeShowActionEvent.ID, null);
						eventView.hide();
					} else {
						m_log.warn("perspectiveActivated(), eventView is null");
					}
				} else {
					m_log.warn("perspectiveActivated(), viewReference(event) is null");
				}
			}

			@Override
			public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective, String changeId) {
				m_log.debug("call perspectiveChanged()");
				
				if (!perspective.getId().startsWith(NODEMAP_PERSPECTIVE_PREFIX)) {
					// ノードマップのパースペクティブ以外では何もしない
					return;
				}
				if (changeId.equals(IWorkbenchPage.CHANGE_RESET)) {
					m_log.debug("reset perspective");
					for (IViewReference view : page.getViewReferences()) {
						if (view.getId().startsWith(NodeMapView.ID)
								|| view.getId().startsWith(NodeListView.ID)) {
							page.hideView(view);
						}
					}
				} else if (changeId.equals(IWorkbenchPage.CHANGE_RESET_COMPLETE)) {
					m_log.debug("reset perspective done");
					for (Map.Entry<String, ViewInfo> entry : getInstance()._secondaryIdMap.entrySet()) {
						try {
							if (entry.getValue().getClass().equals(NodeMapView.class)) {
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(NodeMapView.ID, entry.getKey(), IWorkbenchPage.VIEW_ACTIVATE);
							} else if (entry.getValue().getClass().equals(NodeListView.class)) {
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(NodeListView.ID, entry.getKey(), IWorkbenchPage.VIEW_ACTIVATE);
							}
						} catch (PartInitException e) {
							m_log.info(e.getMessage());
						}
					}
				}
			}
		});
	}

	public static String createSecondaryId(String managerName, String facilityId, Class<? extends CommonViewPart> viewClass) throws Exception {
		init();

		String resultStr = "";
		synchronized (_createSecondaryLock) {
			try {
				// 払い出せるSecondaryIdの上限は、SECONDARY_ID_MAXまで
				for (int id=0; id < SECONDARY_ID_MAX; id++) {
					// セカンダリIDにはPerspectiveIdは含まない
					// secondary.id.<SecondaryID>
					String candidateSecondaryId = SECONDARY_ID_KEY_PREFIX + Integer.toString(id);

					//アクティブページを手に入れる
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewReference viewReference = page.findViewReference(NodeMapView.ID, candidateSecondaryId);
					if (viewReference == null) {
						viewReference = page.findViewReference(NodeListView.ID, candidateSecondaryId);
					}
					IViewReference[] viewRefers = page.getViewReferences();
					for (IViewReference view : viewRefers) {
						m_log.debug("primary="+ view.getId()+",second="+view.getSecondaryId());
					}

					{
						m_log.debug("windows = " + PlatformUI.getWorkbench().getWorkbenchWindowCount());
						m_log.debug("windows = " + PlatformUI.getWorkbench().getWorkbenchWindows().length);
						m_log.debug("page = " + PlatformUI.getWorkbench().
								getActiveWorkbenchWindow().getPages().length);
						m_log.debug("view = " + PlatformUI.getWorkbench().
								getActiveWorkbenchWindow().getActivePage().getViewReferences().length);
					}

					// 使われていないSecondaryIdの場合は、nullが返る
					if(viewReference == null){
						m_log.debug("candidateSecondaryId = " + candidateSecondaryId);
						resultStr = candidateSecondaryId;
						break;
					}
					if (id == SECONDARY_ID_MAX - 1) {
						throw new Exception("too many view");
					}
				}
			} catch (Exception e) {
				throw e;
			}
		}

		return putSecondaryId(resultStr, managerName, facilityId, viewClass);
	}

	public static String putSecondaryId(String secondaryId, String managerName, String facilityId, Class<? extends CommonViewPart> viewClass) {
		init();

		if(secondaryId == null){
			m_log.debug("updateSecondaryId secondaryId is null");
			return null;
		}
		
		if (managerName == null) {
			managerName = "";
		}

		logSecondaryId();
		m_log.info("putSecondaryId() : secondaryId=" + secondaryId + ", viewClass=" + viewClass.getSimpleName() + ", managerName=" + managerName + ", facilityId=" + facilityId);
		getInstance()._secondaryIdMap.put(secondaryId, new ViewInfo(viewClass, managerName, facilityId));
		logSecondaryId();

		Set<String> keys = getInstance()._secondaryIdMap.keySet();
		for (String key : keys) {
			m_log.debug("key:" + key + ",value:" + getInstance()._secondaryIdMap.get(key));
		}

		return secondaryId;
	}

	public static String getSecondaryId(String managerName, String facilityId){
		init();

		String secondaryId = null;
		for(Map.Entry<String, ViewInfo> entity : getInstance()._secondaryIdMap.entrySet()){
			m_log.debug(String.format("getSecondaryId() : key=%s, managerName=%s, facilityid=%s",
					entity.getKey(), managerName, facilityId));
			if (entity.getValue().getManagerName().equals(managerName)
					&& entity.getValue().getFacilityId().equals(facilityId)
					&& entity.getValue().getClass().equals(NodeMapView.class)) {
				secondaryId = entity.getKey();
				break;
			}
		}
		return secondaryId;
	}

	public static String getFacilityId(String secondaryId){
		init();

		// SecondaryIdの指定なしでビューを開いた場合は、nullとなる
		if(secondaryId == null){
			m_log.debug("getFacilityId secondaryId is null");
			// SecondaryIdがnullのビューのファシリティIDは、「null」で登録される
			return FacilityConstant.STRING_COMPOSITE;
		}
		ViewInfo viewInfo = getInstance()._secondaryIdMap.get(secondaryId);

		/*
		 * パースペクティブから開いた場合のみ、下記のロジックに入る。
		 */
		if (viewInfo == null || viewInfo.getFacilityId() == null) {
			return FacilityConstant.STRING_COMPOSITE;
		}
		
		m_log.debug("getManagerName() facilityId=" + viewInfo.getFacilityId());
		return viewInfo.getFacilityId();
	}
	
	public static String getManagerName(String secondaryId) {
		init();
		
		// SecondaryIdの指定なしでビューを開いた場合は、nullとなる
		if(secondaryId == null){
			m_log.debug("getFacilityId secondaryId is null");
			// SecondaryIdがnullのビューのファシリティIDは、「null」で登録される
			return FacilityConstant.STRING_COMPOSITE;
		}
		
		ViewInfo viewInfo = getInstance()._secondaryIdMap.get(secondaryId);
		if (viewInfo == null || viewInfo.getManagerName() == null) {
			return FacilityConstant.STRING_COMPOSITE;
		}
		m_log.debug("getManagerName() managerName=" + viewInfo.getManagerName());
		return viewInfo.getManagerName();
	}
	
	/*
	 * _secondaryIdMapに登録されていればtrue、されていなければfalseを返す
	 */
	public static boolean isRegisteredSecondaryId(String secondaryId) {
		return getInstance()._secondaryIdMap.containsKey(secondaryId);
	}

	/**
	 * secondaryIdをマップから削除するメソッド。
	 * NodeMapView.dispose()から呼ばれるためpublicにする。
	 */
	public static void removeSecondaryId(String secondaryId){
		init();

		if (secondaryId == null) {
			m_log.debug("removeSecondaryId secondaryId is null");
			return;
		}
		logSecondaryId();
		m_log.info("removeSecondaryId() : secondaryId="+secondaryId);
		getInstance()._secondaryIdMap.remove(secondaryId);
		logSecondaryId();
		
		return;
	}
	
	/*
	 * デバッグ用メソッド
	 * _secondaryIdMapの内容をログに出力する
	 */
	private static void logSecondaryId() {
		m_log.debug("logSecondaryId() : start");
		for (Map.Entry<String, ViewInfo> entity : getInstance()._secondaryIdMap.entrySet()) {
			m_log.debug(String.format("logSecondaryId() id=%s, managerId=%s, facilityId=%s, viewClass=%s",
					entity.getKey(), entity.getValue().getManagerName(), entity.getValue().getFacilityId(), entity.getValue().getViewClass().getSimpleName()));
		}
		m_log.debug("logSecondaryId() : end");
	}

	public static class ViewInfo {
		// Viewクラス
		private Class<? extends CommonViewPart> viewClass;
		// managerName
		private String managerName;
		// facilityId
		private String facilityId;

		public ViewInfo (Class<? extends CommonViewPart> viewClass,
				String managerName,
				String facilityId){
			this.viewClass = viewClass;
			this.managerName = managerName;
			this.facilityId = facilityId;
		}

		public String getManagerName() {
			return this.managerName;
		}

		public String getFacilityId() {
			return this.facilityId;
		}

		public Class<? extends CommonViewPart> getViewClass() {
			return this.viewClass;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((facilityId == null) ? 0 : facilityId.hashCode());
			result = prime * result + ((managerName == null) ? 0 : managerName.hashCode());
			result = prime * result + ((viewClass == null) ? 0 : viewClass.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ViewInfo other = (ViewInfo) obj;
			if (facilityId == null) {
				if (other.facilityId != null)
					return false;
			} else if (!facilityId.equals(other.facilityId))
				return false;
			if (managerName == null) {
				if (other.managerName != null)
					return false;
			} else if (!managerName.equals(other.managerName))
				return false;
			if (viewClass == null) {
				if (other.viewClass != null)
					return false;
			} else if (!viewClass.equals(other.viewClass))
				return false;
			return true;
		}
	}
}