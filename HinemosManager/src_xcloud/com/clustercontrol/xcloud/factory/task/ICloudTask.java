/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.task;

/**
 * タスク用のインターフェース
 */
public interface ICloudTask {
	public static class TaskResult {
		public boolean isFinished;
	}
	
	public void register(String key, String cloudScopeId, ICloudTaskStore store) throws Exception;
	public TaskResult execute(String key, String cloudScopeId, ICloudTaskStore store) throws Exception;
	public void roleback();
}
