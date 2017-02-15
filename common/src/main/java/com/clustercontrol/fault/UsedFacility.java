/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.fault;

/**
 * facilityIDが監視等で利用されている場合に利用するException
 * @version 3.2.0
 */
public class UsedFacility extends HinemosException {

	private static final long serialVersionUID = 631321828052534786L;

	/**
	 * 機能ID(com.clustercontrol.bean.PluginConstant)
	 */
	public int plugin = 0;

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
