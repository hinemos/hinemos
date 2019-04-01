/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.InvalidUserPass_Exception;

/**
 * スコープツリーアイテムを取得するクライアント側のデータ<BR>
 *
 * @since 5.0.0
 */
public class FacilityTreeCache {
	// ログ
	private static Log m_log = LogFactory.getLog( FacilityTreeCache.class );

	// ファシリティツリーのクライアント側ローカルキャッシュを保持
	private FacilityTreeItem m_facilityTreeCache = null;

	// ファシリティツリーを表示しているビューComposite
	private final Set<Composite> m_DisplayViewSet = new HashSet<Composite>();

	// リポジトリ最終更新時刻
	private Map<String, Date> cacheDateMap = new ConcurrentHashMap<>();

	/** Private constructor */
	private FacilityTreeCache(){}

	/** Instance getter */
	private static FacilityTreeCache getInstance(){
		return SingletonUtil.getSessionInstance( FacilityTreeCache.class );
	}

	/**
	 * スコープツリー構造を返します。
	 *
	 * @return スコープツリー構造
	 */
	public static FacilityTreeItem getTreeItem(String managerName) {
		m_log.trace("FacilityTreeCache.getTreeItem() start ");

		FacilityTreeCache facilityTree = getInstance();
		if( null == facilityTree.m_facilityTreeCache ){
			// 存在しない場合はキャッシュ再交信再取得
			facilityTree.init();
		}
		// 要素を取得
		FacilityTreeItem item = FacilityTreeItemUtil.deepCopy( facilityTree.m_facilityTreeCache, null );

		if(managerName == null) {
			return item;
		}

		// マネージャが指定されている場合は絞り込み
		FacilityTreeItem targetTree = new FacilityTreeItem();
		targetTree.setData(item.getData());
		targetTree.getChildren().add(new FacilityTreeItem());
		targetTree.getChildren().get(0).setData(item.getChildren().get(0).getData());
		for(FacilityTreeItem tree : item.getChildren().get(0).getChildren()) {
			if(tree.getData().getFacilityId().equals(managerName)) {
				targetTree.getChildren().get(0).getChildren().add(tree);
				break;
			}
		}

		m_log.trace("FacilityTreeCache.getTreeItem() end");
		return targetTree;
	}

	public static void removeCache(String managerName) {
		// キャッシュ情報を削除する
		m_log.trace("FacilityTreeCache.clearCache() : clear cache " + managerName);
		FacilityTreeItem cache = getInstance().m_facilityTreeCache;
		if (cache != null) {
			FacilityTreeItem tree = cache.getChildren().get(0);
			for( FacilityTreeItem scope : tree.getChildren() ){
				if( scope.getData().getFacilityId().equals(managerName) ){
					m_log.debug("clear managerScope " + managerName);
					tree.getChildren().remove(scope);
					break;
				}
			}
		}
		getInstance().cacheDateMap.remove(managerName);
		getInstance().refreshComposite();
	}
	
	

	/**
	 * キャッシュおよび画面のリフレッシュ
	 * このメソッドはClientSession以外から呼んではならない！
	 */
	public static void refresh( String managerName, Date cacheDate ){
		m_log.debug("FacilityTreeCache.refresh() start. managerName=" + managerName + ", cacheDate=" + cacheDate);

		FacilityTreeCache facilityTree = getInstance();
		if( null == facilityTree.m_facilityTreeCache ){
			// 存在しない場合はキャッシュ再交信再取得
			facilityTree.init();
		}

		facilityTree.refreshCache(managerName, cacheDate);
		facilityTree.refreshComposite();

		m_log.trace("FacilityTreeCache.refresh() end");
	}

	/**
	 * ツリーの初期化
	 */
	private void init() {
		m_log.debug("init()");
		try {
			FacilityTreeItem rootTree = new FacilityTreeItem();
			
			// 木構造最上位インスタンスの生成
			FacilityInfo rootInfo = new FacilityInfo();
			rootInfo.setBuiltInFlg(true);
			rootInfo.setFacilityName(FacilityConstant.STRING_COMPOSITE);
			rootInfo.setFacilityType(FacilityConstant.TYPE_COMPOSITE);
			rootTree.setData(rootInfo);

			// コンポジットアイテムの生成
			FacilityTreeItem compositeTree = new FacilityTreeItem();
			FacilityInfo compositeInfo = new FacilityInfo();
			compositeInfo.setBuiltInFlg(true);
			compositeInfo.setFacilityId(ReservedFacilityIdConstant.ROOT_SCOPE);
			compositeInfo.setFacilityName(Messages.getString("root"));
			compositeInfo.setFacilityType(FacilityConstant.TYPE_COMPOSITE);
			compositeInfo.setNotReferFlg(true);
			compositeTree.setData(compositeInfo);
			compositeTree.setParent(rootTree);
			rootTree.getChildren().add(compositeTree);

			m_facilityTreeCache = rootTree;
		} catch (Exception e) {
			m_log.warn("getFacilityTree(), " + e.getMessage(), e);
			return;
		}

		m_log.trace("FacilityTreeCache.refreshCache() : stop");
	}

	private void refreshCache( String managerName, Date cacheDate ){
		if(null == m_facilityTreeCache){
			m_log.debug("FacilityTreeCache.refreshCache() : skip");
			return;
		}
		m_log.debug("FacilityTreeCache.refreshCache() : refresh " + managerName + ", cacheDate=" + cacheDate);

		if (managerName == null) {
			m_log.debug("managerName is null");
			return;
		}
		
		// 再登録 at first
		if (cacheDate == null) {
			cacheDate = new Date(0); // ConcurrentHashMapにnullが投入できないため。接続断のときに、ここを通る。
		}
		try {
			FacilityTreeItem managerScope = null;
			FacilityTreeItem newTree = null;
			if (cacheDate.getTime() != 0) {
				long start = System.currentTimeMillis();
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
				newTree = wrapper.getFacilityTree(null);
				m_log.debug("getFacilityTree " + managerName + ", time=" + (System.currentTimeMillis() - start));
			}
			for( FacilityTreeItem scope : m_facilityTreeCache.getChildren().get(0).getChildren() ){
				if( scope.getData().getFacilityId().equals(managerName) ){
					m_log.debug("clear managerScope " + managerName);
					scope.getChildren().clear();
					managerScope = scope;
					break;
				}
			}
			if (managerScope == null) {
				managerScope = new FacilityTreeItem();
				managerScope.setParent(m_facilityTreeCache.getChildren().get(0));
				FacilityInfo managerInfo = new FacilityInfo();
				managerScope.setData(managerInfo);
				managerInfo.setFacilityId(managerName);
				managerInfo.setFacilityName(managerName);
				managerInfo.setFacilityType(FacilityConstant.TYPE_MANAGER);
				managerInfo.setDisplaySortOrder(EndpointManager.getOrder(managerName));
				managerInfo.setNotReferFlg(false);
				
				m_facilityTreeCache.getChildren().get(0).getChildren().add(managerScope);
			}
			if (cacheDate.getTime() != 0) {
				managerScope.getChildren().addAll(newTree.getChildren());
				m_log.debug("add managerScope " + managerName);
			}
			
			m_log.debug("put cacheDate : managerName=" + managerName + ", cacheDate=" + cacheDate);
			cacheDateMap.put(managerName, cacheDate);
		} catch (RuntimeException | HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			m_log.warn("refreshCache(), " + e.getMessage(), e);
			return;
		}
		m_log.trace("FacilityTreeCache.refreshCache() : done");
	}

	public static Date getCacheDate( String managerName ) {
		return getInstance().cacheDateMap.get(managerName);
	}

	/**
	 * ファシリティツリー画面を再描画する
	 */
	private void refreshComposite() {
		m_log.trace("FacilityTreeCache.refreshComposite() : start");
		FacilityTreeCache facilityTree = getInstance();

		synchronized (facilityTree.m_DisplayViewSet) {
			Iterator<Composite> iterator = this.m_DisplayViewSet.iterator();
			while (iterator.hasNext()) {
				FacilityTreeComposite composite = (FacilityTreeComposite) iterator.next();
				if (!composite.isDisposed()) {
					m_log.trace("FacilityTreeCache.refreshComposite() : target is " + composite.getClass().getCanonicalName());
					composite.update();
				} else {
					iterator.remove();
				}
			}
		}

		m_log.trace("refreshComposite() : stop");
	}

	/**
	 * キャッシュ更新時にリフレッシュするCompositeを一覧に追加
	 *
	 * @param composite
	 */
	public static void addComposite( FacilityTreeComposite composite ){
		FacilityTreeCache facilityTree = getInstance();
		synchronized( facilityTree.m_DisplayViewSet ){
			m_log.trace("FacilityTreeCache.addComposite() composite is " + composite.getClass().getCanonicalName());
			facilityTree.m_DisplayViewSet.add( composite );
		}
	}

	/**
	 * キャッシュ更新時にリフレッシュするCompositeを一覧から削除
	 *
	 * @param composite
	 */
	public static void delComposite(FacilityTreeComposite composite) {
		FacilityTreeCache facilityTree = getInstance();
		synchronized( facilityTree.m_DisplayViewSet ){
			facilityTree.m_DisplayViewSet.remove( composite );
		}
	}

}
