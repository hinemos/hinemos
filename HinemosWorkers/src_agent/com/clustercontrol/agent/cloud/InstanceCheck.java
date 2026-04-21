/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.cloud;

import com.clustercontrol.agent.util.RetryStrategy;

/**
 * クラウドVM管理でエージェント自動登録時に自環境を確認する際に利用
 * エージェント自動登録ではマネージャでファシリティIDが採番され、エージェント環境へ送られてくる
 *
 */
public abstract class InstanceCheck {
	protected RetryStrategy retryStrategy;
	protected String ownResourceId;

	public InstanceCheck(RetryStrategy retryStrategy) {
		this.retryStrategy = retryStrategy;
	}
	

	/**
	 * 自身のリソースIDに当たるIDを確認し、ファシリティIDと比較チェックする
	 *
	 */
	public abstract boolean judgeResourceId(String agentFacilityId) throws UnresolvedIdException;

	/**
	 * 自身のリソースIDに当たるIDを確認し、ファシリティIDと比較チェックする
	 *
	 */
	public String getResourceId() {
		return ownResourceId;
	}

	
	/**
	 * 自身のリソースIDを取得できなかった場合出力する
	 * 
	 *
	 */
	public class UnresolvedIdException extends Exception {
		private static final long serialVersionUID = -8497741070947703632L;

		public UnresolvedIdException(String message, Exception cause) {
			super(message, cause);
		}

		public UnresolvedIdException(String message) {
			super(message);
		}
	}
}
