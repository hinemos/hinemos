/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

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
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.nodemap.view.StatusViewM;
import com.clustercontrol.repository.bean.FacilityConstant;

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
	 * Key   : ManagerId:FacilityId
	 * Value : SecondaryId
	 * 注）ConcurrentHashMapでは、valueにもnullは許容されないため、
	 * SecondaryId が null のものは、m_secondaryIdMapでは、文字列"null"として登録する
	 */
	private final ConcurrentHashMap<String, String> _secondaryIdMap = new ConcurrentHashMap<String, String>();

	volatile private boolean initFlag = false;
	
	private static final String NODEMAP_PERSPECTIVE_PREFIX = "com.clustercontrol.nodemap.NodeMapPerspective";
	
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
//						statusView.update(false);
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
//						eventView.update(false);
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
						if (view.getId().startsWith(NodeMapView.ID)) {
							page.hideView(view);
						}
					}
				} else if (changeId.equals(IWorkbenchPage.CHANGE_RESET_COMPLETE)) {
					m_log.debug("reset perspective done");
					for (String key : getInstance()._secondaryIdMap.keySet()) {
						try {
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(NodeMapView.ID, key, IWorkbenchPage.VIEW_ACTIVATE);
						} catch (PartInitException e) {
							m_log.info(e.getMessage());
						}
					}
				}
			}
		});
	}

	public static String createSecondaryId(String managerName, String facilityId) throws Exception {
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
						throw new Exception("too many nodeMapView");
					}
				}
			} catch (Exception e) {
				throw e;
			}
		}

		return putSecondaryId(resultStr, managerName, facilityId);
	}

	public static String putSecondaryId(String secondaryId, String managerName, String facilityId) {
		init();

		if(secondaryId == null){
			m_log.debug("updateSecondaryId secondaryId is null");
			return null;
		}
		
		if (managerName == null) {
			managerName = "";
		}

		logSecondaryId();
		m_log.info("putSecondaryId() : secondaryId=" + secondaryId + ", managerName=" + managerName + ", facilityId=" + facilityId);
		getInstance()._secondaryIdMap.put(secondaryId, managerName+":"+facilityId);
		logSecondaryId();

		Set<String>keys = getInstance()._secondaryIdMap.keySet();
		for (String key : keys) {
			m_log.debug("key:"+key+",value:"+getInstance()._secondaryIdMap.get(key));
		}

		return secondaryId;
	}

	public static String getSecondaryId(String managerName, String facilityId){
		init();

		String secondaryId = null;
		String targetValue = managerName + ":" + facilityId;
		for(String key : getInstance()._secondaryIdMap.keySet()){
			m_log.debug("getSecondaryId() : key:"+key+",get(key):"+getInstance()._secondaryIdMap.get(key)+",facilityId:"+facilityId);
			if (getInstance()._secondaryIdMap.get(key).equals(targetValue)) {
				secondaryId = key;
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
		String managerFacilityId = getInstance()._secondaryIdMap.get(secondaryId);

		/*
		 * パースペクティブから開いた場合のみ、下記のロジックに入る。
		 */
		if (managerFacilityId == null) {
			return FacilityConstant.STRING_COMPOSITE;
		}
		
		String facilityId = managerFacilityId.substring(managerFacilityId.lastIndexOf(":") + 1);
		
		return facilityId;
	}
	
	public static String getManagerName(String secondaryId) {
		init();
		
		// SecondaryIdの指定なしでビューを開いた場合は、nullとなる
		if(secondaryId == null){
			m_log.debug("getFacilityId secondaryId is null");
			// SecondaryIdがnullのビューのファシリティIDは、「null」で登録される
			return FacilityConstant.STRING_COMPOSITE;
		}
		
		String managerFacilityId = getInstance()._secondaryIdMap.get(secondaryId);
		String managerName = managerFacilityId.substring(0, managerFacilityId.lastIndexOf(":"));
		
		return managerName;
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
	public static String removeSecondaryId(String secondaryId){
		init();

		if (secondaryId == null) {
			m_log.debug("removeSecondaryId secondaryId is null");
			return null;
		}
		logSecondaryId();
		m_log.info("removeSecondaryId() : secondaryId="+secondaryId);
		String managerFacilityId = getInstance()._secondaryIdMap.remove(secondaryId);
		logSecondaryId();
		
		String facilityId = managerFacilityId.substring(managerFacilityId.lastIndexOf(":") + 1);
		return facilityId;
	}
	
	/*
	 * デバッグ用メソッド
	 * _secondaryIdMapの内容をログに出力する
	 */
	private static void logSecondaryId() {
		m_log.debug("logSecondaryId() : start");
		for (String key : getInstance()._secondaryIdMap.keySet()) {
			m_log.debug("logSecondaryId() : id:"+key+",value:"+getInstance()._secondaryIdMap.get(key));
		}
		m_log.debug("logSecondaryId() : end");
	}
}