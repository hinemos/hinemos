/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.xcloud.bean;

/**
 * クラウド・仮想化で使用する定数のうち、クライアント、マネージャ間で共有されるものを宣言。
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class CloudConstant {
	// 各オプションのプラットフォームID
	public static final String platform_AWS = "AWS";
	public static final String platform_ESXi = "ESXi";
	public static final String platform_vCenter = "vCenter";
	public static final String platform_HyperV = "Hyper-V";
	public static final String platform_Azure = "AZURE";
	
	// プライベートクラウドのロケーションID
	public static final String location_ESXi = "ESXi";
	public static final String location_vCenter = "vCenter";
	public static final String location_HyperV = "Hyper-V";
	
	// AWS で使用する課金情報格納用のバケット名を格納する際のキー
	public static final String eprop_awsS3Bucket = "aws_billingDetailS3Bucket";
	
	// Azure の課金レートを取得する際に使用する情報格納用のキー
	public static final String eprop_azureBeginDate = "azure_beginDate";
	public static final String eprop_azurePlanId = "azure_planId";
	public static final String eprop_azureCurrency = "azure_currency";
	public static final String eprop_azureRegion = "azure_billingRegion";
	
	// Azure の認証情報の区切り文字
	public static final String azureKeySep = "/";
	
	// 
	public static final String filename_vmware = "/sdk/vimService";
	public static final String filename_hyperv = "/wsman";

	//クラウド通知で使用する連携情報のキー
	//共通
	public static final String notify_timestamp = "Timestamp";
	//AWS
	public static final String notify_aws_eventBus = "EventBus";
	public static final String notify_aws_detailType = "DetailType";
	public static final String notify_aws_source = "Source";
	public static final String notify_aws_detail = "Detail";
	public static final int notify_aws_platform = 0;
	//Azure
	public static final String notify_azure_endpoint = "Endpoint";
	public static final String notify_azure_accessKey = "AccessKey";
	public static final String notify_azure_subject = "Subject";
	public static final String notify_azure_eventType = "EventType";
	public static final String notify_azure_dataVersion = "DataVersion";
	public static final String notify_azure_data = "Data";
	public static final int notify_azure_platform = 1;
	
		// クラウドログ監視で使用するキー
	public static final String cloudLog_targetScope = "targetScope";
	public static final String cloudLog_targetScopeFacilityId = "targetScopeFacilityId";
	public static final String cloudLog_platform = "platform";
	public static final String cloudLog_LogGroup = "logGroup";
	public static final String cloudLog_LogStream ="logStream";
	public static final String cloudLog_ResourceGroup = "resourceGroup";
	public static final String cloudLog_ReturnCode = "returnCode";
	public static final String cloudLog_patternHead = "patternHead";
	public static final String cloudLog_patternTail ="patternTail";
	public static final String cloudLog_maxBytes = "maxBytes";
	public static final String cloudLog_secretKey="secret";
	public static final String cloudLog_accessKey="access";
	public static final String cloudLog_Location="location";
	public static final String cloudLog_LastFireTime="lastFireTime";
	public static final String cloudLog_Col="col";
	public static final String cloudLog_isPrefix="prefix";
	public static final String cloudLog_Offset="offset";

	// Hyper-Vの接続に利用するプロトコル
	// ※xCloudのHyperVConstantsにも同様のEnumがあるため整合性に注意してください
	public static enum HyperVProtocol {
		HTTP(5985),
		HTTPS(5986);

		private int port;

		HyperVProtocol(int port) {
			this.port = port;
		}

		public int defaultPort() {
			return port;
		}

		public static HyperVProtocol ignoreCaseValueOf(String name) {
			for (HyperVProtocol p : HyperVProtocol.values()) {
				if (p.name().equalsIgnoreCase(name)) {
					return p;
				}
			}
			if (name == null) {
				throw new NullPointerException("Protocol is null");
			}
			throw new IllegalArgumentException("Invalid Protocol. " + name);
		}
	}
}