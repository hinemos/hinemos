/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.cloud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;
import com.clustercontrol.agent.util.RetryFunction;
import com.clustercontrol.agent.util.RetryStrategy;

/**
 * AWS環境のエージェントでの自環境の確認
 *
 */
public class InstanceCheckAWS extends InstanceCheck {
	protected static Log log = LogFactory.getLog(InstanceCheckAWS.class);
	public InstanceCheckAWS(RetryStrategy retryStrategy) {
		super(retryStrategy);
	}

	/**
	 * AWS環境のエージェントで自身のリソースID(=EC2のインスタンスID)を取得し、マネージャから送られてきたファシリティIDと比較チェックする
	 * 
	 */
	public boolean judgeResourceId(String agentFacilityId) throws UnresolvedIdException {
		ownResourceId = retryStrategy.executeWithRetry(new RetryFunction<String>() {
			@Override
			public String execute() {
				return EC2MetadataUtils.getInstanceId();
			}
		});

		if (ownResourceId == null) {
			//AWSメタデータにアクセスできない場合到達する
			throw new UnresolvedIdException("Failed to get resource ID.");
		}

		log.debug("ownResourceId=" + ownResourceId + ", agentFacilityId=" + agentFacilityId);
		return agentFacilityId.endsWith(ownResourceId);
	}
}
