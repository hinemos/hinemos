/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository;

/**
 * リポジトリ定義の更新を待ち受けるリスナクラスのインタフェース<br/>
 * 
 */
public interface IRepositoryListener {
	
	/**
	 * リスナクラスの識別子(リスナクラスの削除に利用する文字列)を返すメソッド.
	 * @return リスナクラスの識別子
	 */
	public String getListenerId();
	
	/**
	 * ノード追加直後にキックされるメソッド.
	 * @param facilityId 追加されたノードのファシリティID
	 */
	public void postAddingNode(String facilityId);
	
	/**
	 * ノード更新直後にキックされるメソッド.
	 * @param facilityId 更新されたノードのファシリティID
	 */
	public void postChangingNode(String facilityId);
	
	/**
	 * ノード削除直後にキックされるメソッド.
	 * @param facilityId 削除されたノードのファシリティID
	 */
	public void postRemovingNode(String facilityId);
	
	/**
	 * スコープ追加直後にキックされるメソッド.
	 * @param facilityId 追加されたスコープのファシリティID
	 */
	public void postAddingScope(String facilityId);
	
	/**
	 * スコープ更新直後にキックされるメソッド.
	 * @param facilityId 更新されたスコープのファシリティID
	 */
	public void postChangingScope(String facilityId);
	
	/**
	 * スコープ削除直後にキックされるメソッド.
	 * @param facilityId 削除されたスコープのファシリティID
	 */
	public void postRemovingScope(String facilityId);
	
	/**
	 * スコープへのノード割り当て直後にキックされるメソッド.
	 * @param scopeFacilityId スコープのファシリティID
	 * @param nodeFacilityId 割り当てられたノードのファシリティID
	 */
	public void postAssigningNodeToScope(String scopeFacilityId, String nodeFacilityId);
	
	/**
	 * スコープからのノード割り当て解除直後にキックされるメソッド.
	 * @param scopeFacilityId スコープのファシリティID
	 * @param nodeFacilityId 割り当て解除されたノードのファシリティID
	 */
	public void postReleasingNodeFromScope(String scopeFacilityId, String nodeFacilityId);
	
}
