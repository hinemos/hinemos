/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * facilityIDが監視等で利用されている場合に利用するException
 * @version 3.2.0
 */
public class UsedFacility extends HinemosException {

	private static final long serialVersionUID = 631321828052534786L;

	@Deprecated
	/**
	 * 機能ID(com.clustercontrol.bean.PluginConstant)
	 * @deprecated Don't know what is this for. Switch to private to prevent being used.
 	 */
	private int plugin = 0;

	/**
	 * UsedFacilityExceptionコンストラクタ
	 */
	public UsedFacility() {
		super();
	}

	/**
	 * UsedFacilityExceptionコンストラクタ
	 * @param messages
	 */
	public UsedFacility(String messages) {
		super(messages);
	}

	/**
	 * FoundExceptionコンストラクタ
	 * @param e
	 */
	public UsedFacility(Throwable e) {
		super(e);
	}

	/**
	 * UsedFacilityExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UsedFacility(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param plugin プラグイン(機能)
	 * 
	 * @see com.clustercontrol.bean.PluginConstant
	 */
	public UsedFacility(int plugin, Throwable e) {
		super(e);
		this.plugin = plugin;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param plugin プラグイン(機能)
	 * 
	 * @see com.clustercontrol.bean.PluginConstant
	 */
	public UsedFacility(int plugin) {
		this.plugin = plugin;
	}


	/**
	 * プラグイン(機能)を返します。
	 * 
	 * @return プラグイン(機能)
	 * 
	 * @see com.clustercontrol.bean.PluginConstant
	 */
	public int getPlugin() {
		return plugin;
	}
}
