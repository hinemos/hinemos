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
}