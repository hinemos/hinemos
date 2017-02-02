/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
