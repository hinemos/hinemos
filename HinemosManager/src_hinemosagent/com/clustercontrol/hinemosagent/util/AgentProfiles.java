/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.Singletons;

/**
 * Hinemosエージェントから送られてきた、MD5やJavaの情報を管理します。
 * <p>
 * newせずに、{@link Singletons}から取得して使用してください。
 * 
 * @since 6.2.0
 */
public class AgentProfiles {
	// logger
	private static final Log log = LogFactory.getLog(AgentProfiles.class);

	// ファシリティID -> AgentProfile
	private Map<String, AgentProfile> profiles;

	/**
	 * このコンストラクタを直接使用せず、{@link Singletons#get(Class)}を使用してください。
	 */
	public AgentProfiles() {
		this.profiles = new ConcurrentHashMap<>();
		log.info("ctor: Created.");
	}

	/**
	 * エージェントのプロファイルを登録します。
	 * <p>
	 * 既に同一ファシリティIDで登録されていた場合、上書きします。
	 * 
	 * @param facilityId エージェントのファシリティID。
	 * @param agentProfile 登録するプロファイル。
	 */
	public void registerProfile(String facilityId, AgentProfile agentProfile) {
		profiles.put(facilityId, agentProfile);
		log.info("registerProfile: facilityId=" + facilityId);
	}

	/**
	 * 指定されたファシリティIDのリストに対して、1つのプロファイルを登録します。
	 * <p>
	 * 既に同一ファシリティIDで登録されていた場合、上書きします。
	 * 複数のファシリティIDと紐づいたエージェントのためのメソッドです。
	 * 
	 * @param facilityId エージェントのファシリティIDのリスト。
	 * @param agentProfile 登録するプロファイル。
	 */
	public void registerProfile(List<String> facilityIds, AgentProfile agentProfile) {
		for (String facilityId : facilityIds) {
			registerProfile(facilityId, agentProfile);
		}
	}
	
	/**
	 * エージェントのプロファイルを削除します。
	 * 
	 * @param facilityId エージェントのファシリティID。
	 */
	public void removeProfile(String facilityId) {
		profiles.remove(facilityId);
		log.info("remove: facilityId=" + facilityId);
	}

	/**
	 * エージェントのプロファイルが登録されているかどうかを返します。
	 * 
	 * @param facilityId エージェントのファシリティID。
	 * @return 登録されているならtrue、そうでなければfalse。
	 */
	public boolean hasProfile(String facilityId) {
		return profiles.containsKey(facilityId);
	}

	/**
	 * 指定されたファシリティIDに対応するプロファイルを返します。
	 * 
	 * @param facilityId エージェントのファシリティID。
	 * @return 登録されているプロファイル。当該エージェントの情報が存在しない場合は null。
	 */
	public AgentProfile getProfile(String facilityId) {
		AgentProfile profile = profiles.get(facilityId);
		if (profile == null) {
			return null;
		}
		return profile;
	}

	/**
	 * 指定されたファシリティIDのリストのうち、最初に対応するものが見つかったプロファイルを返します。
	 * <p>
	 * 複数のファシリティIDと紐づいたエージェントのためのメソッドです。
	 * 
	 * @param facilityId エージェントのファシリティIDのリスト。
	 * @return 登録されているプロファイル。当該エージェントの情報が存在しない場合は null。
	 */
	public AgentProfile getProfile(List<String> facilityIds) {
		for (String facilityId : facilityIds) {
			AgentProfile ret = getProfile(facilityId);
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}

}
