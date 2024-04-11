/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.cloud.conv;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openapitools.client.model.AddCloudLoginUserRequest;
import org.openapitools.client.model.AddCloudScopeRequest;
import org.openapitools.client.model.CredentialResponse;
import org.openapitools.client.model.HinemosPropertyResponse;
import org.openapitools.client.model.HinemosPropertyResponseP1;
import org.openapitools.client.model.ModifyBillingSettingRequest;
import org.openapitools.client.model.ModifyCloudLoginUserRequest;
import org.openapitools.client.model.ModifyCloudScopeRequest;
import org.openapitools.client.model.OptionRequest;
import org.openapitools.client.model.PrivateEndpointRequest;
import org.openapitools.client.model.PrivateLocationRequest;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.utility.settings.cloud.action.CloudTools;
import com.clustercontrol.utility.settings.cloud.xml.Amazon;
import com.clustercontrol.utility.settings.cloud.xml.AmazonUser;
import com.clustercontrol.utility.settings.cloud.xml.Azure;
import com.clustercontrol.utility.settings.cloud.xml.AzureUser;
import com.clustercontrol.utility.settings.cloud.xml.Hyperv;
import com.clustercontrol.utility.settings.cloud.xml.HypervUser;
import com.clustercontrol.utility.settings.cloud.xml.ICloudScope;
import com.clustercontrol.utility.settings.cloud.xml.Vmware;
import com.clustercontrol.utility.settings.cloud.xml.VmwareUser;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.bean.CloudConstant.HyperVProtocol;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.ILoginUser;

public class CloudUserConv {
	
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private static final String schemaType=VersionUtil.getSchemaProperty("CLOUD.CLOUDUSER.SCHEMATYPE");
	private static final String schemaVersion=VersionUtil.getSchemaProperty("CLOUD.CLOUDUSER.SCHEMAVERSION");
	private static final String schemaRevision=VersionUtil.getSchemaProperty("CLOUD.CLOUDUSER.SCHEMAREVISION");

	/* キー情報保護 */
	private static boolean HP_EXPIMP_XCLOUD_KEYPROTECT_ENABLE_DEFAULT = false;
	private static volatile boolean keyProtected;
	
	/* ロガー */
	private static Logger log = Logger.getLogger(CloudUserConv.class);

	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.cloud.xml.Common
		versioncloudDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.cloud.xml.Common com =
				new com.clustercontrol.utility.settings.cloud.xml.Common();

		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		com.setGenerateDate(dateFormat.format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));
		
		return com;
	}
	
	/**
	 * XMLとツールの対応バージョンをチェック */
	static public int checkSchemaVersion(String type, String version ,String revision){
		return BaseConv.checkSchemaVersion(schemaType, schemaVersion, schemaRevision, 
				type, version, revision);
	}

	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	static public com.clustercontrol.utility.settings.cloud.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.cloud.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.cloud.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}

	public static ICloudScope getICloudScope(com.clustercontrol.xcloud.model.cloud.ICloudScope cloudScopeEndpoint) {
		ICloudScope cloudScope = new ICloudScope();
		final ILoginUser accountUser = cloudScopeEndpoint.getLoginUsers().getLoginUser(cloudScopeEndpoint.getAccountId());
		cloudScope.setCloudPlatformId(cloudScopeEndpoint.getPlatformId());
		cloudScope.setCloudScopeId(cloudScopeEndpoint.getId());
		cloudScope.setCloudScopeName(cloudScopeEndpoint.getName());
		cloudScope.setDescription(cloudScopeEndpoint.getDescription());
		
		updateKeyProtectedFlag();

		if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_ESXi) ||
				cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_vCenter)) {
			URL url=null;
			VmwareUser vmwareUser = null;
			try {
				url = new URL(cloudScopeEndpoint.getLocation(cloudScopeEndpoint.getPlatformId()).getEndpoints()[0].getUrl());
				String protocol = url.getProtocol();
				String ipAddr = url.getHost();
				
				Vmware vmware = new Vmware();
				vmware.setVmwareExiIp(ipAddr);
				vmware.setVmwareExiProtocol(protocol);
				// メインユーザ
				vmwareUser = new VmwareUser();
				vmwareUser.setVmwareUserName(((CredentialResponse)accountUser.getCredential()).getUser());
				vmwareUser.setVmwarePassword(((CredentialResponse)accountUser.getCredential()).getPassword());
				vmwareUser.setAccountId(accountUser.getId());
				vmwareUser.setDisplayName(accountUser.getName());
				vmware.addVmwareUser(hideProtectedKey(vmwareUser));
				cloudScope.setVmware(vmware);
				
				List<ILoginUser> currentUsers = new ArrayList<>(Arrays.asList(cloudScopeEndpoint.getLoginUsers().getLoginUsers()));
				currentUsers.remove(cloudScopeEndpoint.getLoginUsers().getLoginUser(cloudScopeEndpoint.getAccountId()));
				
				for (ILoginUser user : currentUsers){
					vmwareUser = new VmwareUser();
					vmwareUser.setVmwareUserName(((CredentialResponse)user.getCredential()).getUser());
					vmwareUser.setVmwarePassword(((CredentialResponse)user.getCredential()).getPassword());
					vmwareUser.setAccountId(user.getId());
					vmwareUser.setDisplayName(user.getName());
					vmware.addVmwareUser(hideProtectedKey(vmwareUser));
					cloudScope.setVmware(vmware);
				}
				
			} catch (MalformedURLException e) {
				log.error(e);
			}
		} else if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_AWS)) {
			Amazon amazon = new Amazon();
			AmazonUser amazonUser = new AmazonUser();
			
			// メインユーザ
			amazonUser.setAccessKey(((CredentialResponse) accountUser.getCredential()).getAccessKey());
			amazonUser.setSecretKey(((CredentialResponse) accountUser.getCredential()).getSecretKey());
			amazonUser.setAccountId(accountUser.getId());
			amazonUser.setDisplayName(accountUser.getName());
			amazon.addAmazonUser(hideProtectedKey(amazonUser));
			
			List<ILoginUser> currentUsers = new ArrayList<>(Arrays.asList(cloudScopeEndpoint.getLoginUsers().getLoginUsers()));
			currentUsers.remove(cloudScopeEndpoint.getLoginUsers().getLoginUser(cloudScopeEndpoint.getAccountId()));
			for (ILoginUser iLoginUser :currentUsers ){
				amazonUser = new AmazonUser();
				
				// サブユーザ
				amazonUser.setAccessKey(((CredentialResponse) iLoginUser.getCredential()).getAccessKey());
				amazonUser.setSecretKey(((CredentialResponse) iLoginUser.getCredential()).getSecretKey());
				amazonUser.setAccountId(iLoginUser.getId());
				amazonUser.setDisplayName(iLoginUser.getName());
				amazon.addAmazonUser(hideProtectedKey(amazonUser));
			}

			
			// 課金情報
			amazon.setValidCollect(cloudScopeEndpoint.getBillingDetailCollectorFlg());
			if (cloudScopeEndpoint.getBillingDetailCollectorFlg()){
				amazon.setTerm(cloudScopeEndpoint.getRetentionPeriod());
				amazon.setS3packet(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_awsS3Bucket));
			}
			cloudScope.setAmazon(amazon);
		} else if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_HyperV)) {
			Hyperv hyperv = new Hyperv();

			// メインユーザ
			HypervUser rootUser = new HypervUser();
			rootUser.setUserName(((CredentialResponse)accountUser.getCredential()).getUser());
			rootUser.setPassword(((CredentialResponse)accountUser.getCredential()).getPassword());
			rootUser.setAccountId(accountUser.getId());
			rootUser.setDisplayName(accountUser.getName());
			hyperv.addHypervUser(hideProtectedKey(rootUser));
			
			for (ILoginUser user : cloudScopeEndpoint.getLoginUsers().getLoginUsers()){
				if (user.getId().equals(accountUser.getId()))
					continue;
				
				HypervUser subUser = new HypervUser();
				subUser.setUserName(((CredentialResponse)user.getCredential()).getUser());
				subUser.setPassword(((CredentialResponse)user.getCredential()).getPassword());
				subUser.setAccountId(user.getId());
				subUser.setDisplayName(user.getName());
				hyperv.addHypervUser(hideProtectedKey(subUser));
			}
			
			// 接続先設定
			try {
				URL url = new URL(cloudScopeEndpoint.getLocation(cloudScopeEndpoint.getPlatformId()).getEndpoints()[0].getUrl());
				hyperv.setIpAddress(url.getPort()!= -1 ? url.getHost() + ":"  + url.getPort() : url.getHost());
				hyperv.setProtocol(url.getProtocol());
			} catch (MalformedURLException e) {
				log.error(e);
			}
			cloudScope.setHyperv(hyperv);
		} else if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_Azure)) {
			Azure azure = new Azure();
			AzureUser rootUser = new AzureUser();
			
			// メインユーザ
			String[] ids = ((CredentialResponse) accountUser.getCredential()).getAccessKey().split(CloudConstant.azureKeySep);
			rootUser.setTenantId(ids[0]);
			rootUser.setApplicationId(ids[1]);
			rootUser.setSubscriptionId(ids[2]);
			rootUser.setSecretKey(((CredentialResponse) accountUser.getCredential()).getSecretKey());
			rootUser.setAccountId(accountUser.getId());
			rootUser.setDisplayName(accountUser.getName());
			azure.addAzureUser(hideProtectedKey(rootUser));
			
			for (ILoginUser user : cloudScopeEndpoint.getLoginUsers().getLoginUsers()) {
				if (user.getId().equals(accountUser.getId()))
					continue;
				
				// サブユーザ
				AzureUser subUser = new AzureUser();
				String[] subIds = ((CredentialResponse) user.getCredential()).getAccessKey().split(CloudConstant.azureKeySep);
				subUser.setTenantId(subIds[0]);
				subUser.setApplicationId(subIds[1]);
				subUser.setSubscriptionId(subIds[2]);
				subUser.setSecretKey(((CredentialResponse) user.getCredential()).getSecretKey());
				subUser.setAccountId(user.getId());
				subUser.setDisplayName(user.getName());
				azure.addAzureUser(hideProtectedKey(subUser));
			}
			
			// 課金情報
			azure.setValidCollect(cloudScopeEndpoint.getBillingDetailCollectorFlg());
			if (cloudScopeEndpoint.getBillingDetailCollectorFlg()) {
				azure.setTerm(cloudScopeEndpoint.getRetentionPeriod());
				azure.setBeginDate(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azureBeginDate).replace("-", "/"));
				azure.setBillingRegion(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azureRegion));
				azure.setPlanId(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azurePlanId));
				azure.setCurrency(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azureCurrency));
			}
			
			cloudScope.setAzure(azure);
		}
		
		cloudScope.setOwnerRoleId(cloudScopeEndpoint.getOwnerRoleId());
		
		return cloudScope;
	}
	
	// Amazon
	public static AddCloudScopeRequest getPublicCloudScopeRequestDto(ICloudScope cloudScope) throws InvalidSetting, HinemosUnknown {
		AddCloudScopeRequest req = new AddCloudScopeRequest();
		req.setPlatformId(cloudScope.getCloudPlatformId());
		req.setCloudScopeId(cloudScope.getCloudScopeId());
		req.setScopeName(cloudScope.getCloudScopeName());
		req.setOwnerRoleId(cloudScope.getOwnerRoleId());
		req.setDescription(cloudScope.getDescription());

		// メイン登録
		if (cloudScope.getAmazon() != null
				&& cloudScope.getAmazon().getAmazonUserCount() > 0) {
			AddCloudLoginUserRequest account = new AddCloudLoginUserRequest();
			AmazonUser amazonUser = cloudScope.getAmazon().getAmazonUser(0);
			account.setAccessKey(amazonUser.getAccessKey());
			account.setSecretKey(ObjectUtils.defaultIfNull(amazonUser.getSecretKey(), "")); // 空欄可(IAMロール)
			account.setDescription(cloudScope.getDescription());
			account.setLoginUserId(amazonUser.getAccountId());
			account.setUserName(amazonUser.getDisplayName());
			req.setAccount(account);
		}
		
		return req;
	}
	
	
	// Amazon
	public static ModifyCloudScopeRequest getModifyPublicCloudScopeRequestDto(ICloudScope info) {
		ModifyCloudScopeRequest ret = new ModifyCloudScopeRequest();
		ret.setScopeName(info.getCloudScopeName());
		ret.setDescription(info.getDescription());
	
		return ret;
	}
	
	// VMware
	public static AddCloudScopeRequest getPrivateCloudScopeRequestDto(ICloudScope cloudScope) throws InvalidSetting, HinemosUnknown {
		
		AddCloudScopeRequest req = new AddCloudScopeRequest();
		
		req.setPlatformId(cloudScope.getCloudPlatformId());
		req.setCloudScopeId(cloudScope.getCloudScopeId());
		req.setScopeName(cloudScope.getCloudScopeName());
		req.setOwnerRoleId(cloudScope.getOwnerRoleId());
		req.setDescription(cloudScope.getDescription());
		
		// メイン (XML上は１件目がメイン)
		if (cloudScope.getVmware() != null
				&& cloudScope.getVmware().getVmwareUserCount() > 0) {
			AddCloudLoginUserRequest account = new AddCloudLoginUserRequest();
			VmwareUser vmwareUser = cloudScope.getVmware().getVmwareUser(0);
			account.setUser(vmwareUser.getVmwareUserName());//root
			account.setPassword(vmwareUser.getVmwarePassword());
			account.setDescription(cloudScope.getDescription());
			account.setLoginUserId(vmwareUser.getAccountId());
			account.setUserName(vmwareUser.getDisplayName());
			req.setAccount(account);
		}
		
		PrivateLocationRequest location = new PrivateLocationRequest();
		
		PrivateEndpointRequest e = new PrivateEndpointRequest();
		if (cloudScope.getCloudPlatformId().equals(CloudConstant.platform_ESXi)) {
			location.setLocationId(CloudConstant.location_ESXi);
			location.setName(CloudConstant.location_ESXi);
			e.setEndpointId(CloudConstant.location_ESXi);
		} else {
			location.setLocationId(CloudConstant.location_vCenter);
			location.setName(CloudConstant.location_vCenter);
			e.setEndpointId(CloudConstant.location_vCenter);
		}

		if (cloudScope.getVmware() != null) {
			try {
				e.setUrl(new URL(
						cloudScope.getVmware().getVmwareExiProtocol(),
						cloudScope.getVmware().getVmwareExiIp() ,
						CloudConstant.filename_vmware).toString()
						);
			} catch (MalformedURLException e1) {
				throw new InvalidSetting(e1);
			}
		}
		
		location.setEndpoints(new ArrayList<PrivateEndpointRequest>());
		location.getEndpoints().add(e);
		req.setPrivateLocations(new ArrayList<PrivateLocationRequest>());
		req.getPrivateLocations().add(location);
		return req;
	}

	// VMware
	public static ModifyCloudScopeRequest getModifyPrivateCloudScopeRequestDto(ICloudScope info) {
		ModifyCloudScopeRequest ret = new ModifyCloudScopeRequest();
		
		ret.setScopeName(info.getCloudScopeName());
		ret.setDescription(info.getDescription());
		PrivateLocationRequest location = new PrivateLocationRequest();
		PrivateEndpointRequest e = new PrivateEndpointRequest();
		
		if (info.getCloudPlatformId().equals(CloudConstant.platform_ESXi)){
			location.setLocationId(CloudConstant.location_ESXi);
			location.setName(CloudConstant.location_ESXi);
			e.setEndpointId(CloudConstant.location_ESXi);
		} else {
			location.setLocationId(CloudConstant.location_vCenter);
			location.setName(CloudConstant.location_vCenter);
			e.setEndpointId(CloudConstant.location_vCenter);
		}
		
		try {
			e.setUrl(new URL(info.getVmware().getVmwareExiProtocol(),
					info.getVmware().getVmwareExiIp(),
					CloudConstant.filename_vmware).toString()
					);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		location.setEndpoints(new ArrayList<PrivateEndpointRequest>());
		location.getEndpoints().add(e);
		ret.setPrivateLocations(new ArrayList<PrivateLocationRequest>());
		ret.getPrivateLocations().add(location);
		
		return ret;
	}
	
	// Amazon or VMware or Hyper-V or Azure
	public static ModifyCloudLoginUserRequest getModifyCloudLoginUserRequestDto(ICloudScope info) {
		ModifyCloudLoginUserRequest ret = new ModifyCloudLoginUserRequest();
		ret.setDescription(info.getDescription());
		if (info.getCloudPlatformId().equals(CloudConstant.platform_ESXi) || info.getCloudPlatformId().equals(CloudConstant.platform_vCenter)) {
			//メインユーザ
			VmwareUser vmwareUser = info.getVmware().getVmwareUser(0);
			ret.setUserName(vmwareUser.getDisplayName());

			ret.setUser(vmwareUser.getVmwareUserName());
			ret.setPassword(vmwareUser.getVmwarePassword());
			
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_AWS)) {
			// メインユーザ
			AmazonUser amazonUser = info.getAmazon().getAmazonUser(0);
			ret.setUserName(amazonUser.getDisplayName());
			
			ret.setAccessKey(amazonUser.getAccessKey());
			ret.setSecretKey(ObjectUtils.defaultIfNull(amazonUser.getSecretKey(), "")); // 空欄可(IAMロール)
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_HyperV)) {
			//メインユーザ
			HypervUser hypervUser = info.getHyperv().getHypervUser(0);
			ret.setUserName(hypervUser.getDisplayName());

			ret.setUser(hypervUser.getUserName());
			ret.setPassword(hypervUser.getPassword());
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_Azure)) {
			// メインユーザ
			AzureUser azureUser = info.getAzure().getAzureUser(0);
			ret.setUserName(azureUser.getDisplayName());
			
			ret.setAccessKey(azureUser.getTenantId() + CloudConstant.azureKeySep +
									azureUser.getApplicationId() + CloudConstant.azureKeySep +
									azureUser.getSubscriptionId());
			ret.setSecretKey(azureUser.getSecretKey());
		}
		
		return ret;
	}

	public static void convertPublicCloudUserRequestDto(ICloudScope info, AddCloudScopeRequest cloudScope,
			List<AddCloudLoginUserRequest> addRequestList, List<String> idList) {

		if (info.getAmazon() == null) {
			return;
		}
		List<AmazonUser> output = new ArrayList<>(Arrays.asList(info.getAmazon().getAmazonUser()));

		int index = 0;
		for (AmazonUser amazonUser : new ArrayList<>(output)) {
			if (amazonUser.getAccountId().equals(cloudScope.getAccount().getLoginUserId())) {
				output.remove(index);
				index--;
			}
			index++;
		}

		for (AmazonUser item : output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setUserName(item.getDisplayName());
			request.setAccessKey(item.getAccessKey());
			request.setSecretKey(item.getSecretKey());
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}
	}

	public static void convertPrivateCloudUserRequestDto(ICloudScope info, AddCloudScopeRequest cloudScope,
			List<AddCloudLoginUserRequest> addRequestList, List<String> idList) {

		if (info.getVmware() == null) {
			return;
		}
		List<VmwareUser> output = new ArrayList<>(Arrays.asList(info.getVmware().getVmwareUser()));

		int index = 0;
		// メインユーザ削除
		for (VmwareUser vmwareUser : new ArrayList<>(output)) {
			if (vmwareUser.getAccountId().equals(cloudScope.getAccount().getLoginUserId())) {
				output.remove(index);
				break;
			}
			index++;
		}

		for (VmwareUser item : output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setUserName(item.getDisplayName());
			request.setUser(item.getVmwareUserName());
			request.setPassword(item.getVmwarePassword());
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}
	}

	// publicクラウド(AWS,Azure)固有
	public static ModifyBillingSettingRequest createBillingSettingRequest(ICloudScope info) {
		ModifyBillingSettingRequest req = new ModifyBillingSettingRequest();
		
		if (info.getAmazon() != null) {
			req.setRetentionPeriod((int)info.getAmazon().getTerm());
			req.setBillingDetailCollectorFlg(info.getAmazon().getValidCollect());
			req.setOptions(new ArrayList<OptionRequest>());
			req.getOptions().add(createOption(CloudConstant.eprop_awsS3Bucket, info.getAmazon().getS3packet()));
		} else if (info.getAzure() != null) {
			req.setRetentionPeriod((int)info.getAzure().getTerm());
			req.setBillingDetailCollectorFlg(info.getAzure().getValidCollect());
			req.setOptions(new ArrayList<OptionRequest>());
			req.getOptions().add(createOption(CloudConstant.eprop_azureBeginDate, 
					info.getAzure().getBeginDate()!= null 
					? info.getAzure().getBeginDate().replace("/", "-")
					: ""
			));
			req.getOptions().add(createOption(CloudConstant.eprop_azureRegion, info.getAzure().getBillingRegion()));
			req.getOptions().add(createOption(CloudConstant.eprop_azurePlanId, info.getAzure().getPlanId()));
			req.getOptions().add(createOption(CloudConstant.eprop_azureCurrency, info.getAzure().getCurrency()));
		}
		return req;
	}

	public static AddCloudScopeRequest getHyperVCloudScopeRequestDto(ICloudScope info) throws InvalidSetting, HinemosUnknown {
		AddCloudScopeRequest req = new AddCloudScopeRequest();
		
		req.setPlatformId(info.getCloudPlatformId());
		req.setCloudScopeId(info.getCloudScopeId());
		req.setScopeName(info.getCloudScopeName());
		req.setOwnerRoleId(info.getOwnerRoleId());
		req.setDescription(info.getDescription());
		
		// メイン (XML上は１件目がメイン)
		if (info.getHyperv() != null
				&& info.getHyperv().getHypervUserCount() > 0) {
			AddCloudLoginUserRequest account = new AddCloudLoginUserRequest();
			HypervUser hypervUser = info.getHyperv().getHypervUser(0);
		
			account.setUser(hypervUser.getUserName());
			account.setPassword(hypervUser.getPassword());
			account.setDescription(info.getDescription());
			account.setLoginUserId(hypervUser.getAccountId());
			account.setUserName(hypervUser.getDisplayName());
			req.setAccount(account);
		}

		
		PrivateEndpointRequest e = new PrivateEndpointRequest();
		e.setEndpointId(info.getCloudPlatformId());
		String url = null;
		if (info.getHyperv() != null){
			Matcher m = Pattern.compile("(.*):([0-9]*)").matcher(info.getHyperv().getIpAddress());
			try {
				if (m.matches()) {
					String iphost = m.group(1);
					Integer port = Integer.valueOf(m.group(2));
					url = new URL(info.getHyperv().getProtocol(), iphost ,port, CloudConstant.filename_hyperv.toString()).toString();
				} else {
					int port = HyperVProtocol.ignoreCaseValueOf(info.getHyperv().getProtocol()).defaultPort();
					url = new URL(info.getHyperv().getProtocol(), info.getHyperv().getIpAddress(), port,
							CloudConstant.filename_hyperv.toString()).toString();
				}
			} catch (MalformedURLException | IllegalArgumentException | NullPointerException e1) {
				throw new InvalidSetting(e1);
			}
		}
		e.setUrl(url);
		
		PrivateLocationRequest location = new PrivateLocationRequest();

		location.setLocationId(CloudConstant.location_HyperV);
		location.setName(info.getCloudPlatformId());
		location.setEndpoints(new ArrayList<PrivateEndpointRequest>());
		location.getEndpoints().add(e);
		req.setPrivateLocations(new ArrayList<PrivateLocationRequest>());
		req.getPrivateLocations().add(location);
		
		return req;
	}

	public static ModifyCloudScopeRequest getModifyHyperVCloudScopeRequestDto(ICloudScope info) {
		ModifyCloudScopeRequest req = new ModifyCloudScopeRequest();
		
		req.setScopeName(info.getCloudScopeName());
		req.setDescription(info.getDescription());
		
		PrivateEndpointRequest e = new PrivateEndpointRequest();
		e.setEndpointId(info.getCloudPlatformId());
		Matcher m = Pattern.compile("(.*):([0-9]*)").matcher(info.getHyperv().getIpAddress());
		String url = null;
		try {
			if (m.matches()) {
				String iphost = m.group(1);
				Integer port = Integer.valueOf(m.group(2));
				url = new URL(info.getHyperv().getProtocol(), iphost ,port, CloudConstant.filename_hyperv.toString()).toString();
			} else {
				url = new URL(info.getHyperv().getProtocol(), info.getHyperv().getIpAddress() ,CloudConstant.filename_hyperv.toString()).toString();
			}
		} catch (MalformedURLException e1) {
			throw new CloudModelException(e1);
		}
		e.setUrl(url);
		PrivateLocationRequest location = new PrivateLocationRequest();
		location.setLocationId(info.getCloudPlatformId());
		location.setName(info.getCloudPlatformId());
		location.getEndpoints().add(e);
		req.getPrivateLocations().add(location);
		
		return req;
	}

	public static AddCloudScopeRequest getAzureCloudScopeRequestDto(ICloudScope cloudScopeXML) throws InvalidSetting, HinemosUnknown {
		AddCloudScopeRequest req = new AddCloudScopeRequest();

		req.setPlatformId(cloudScopeXML.getCloudPlatformId());
		req.setCloudScopeId(cloudScopeXML.getCloudScopeId());
		req.setScopeName(cloudScopeXML.getCloudScopeName());
		req.setOwnerRoleId(cloudScopeXML.getOwnerRoleId());
		req.setDescription(cloudScopeXML.getDescription());

		// メイン登録
		if (cloudScopeXML.getAzure() != null
				&& cloudScopeXML.getAzure().getAzureUserCount() > 0) {
			AddCloudLoginUserRequest account = new AddCloudLoginUserRequest();
			AzureUser azureUser = cloudScopeXML.getAzure().getAzureUser(0);
			account.setAccessKey(azureUser.getTenantId() + CloudConstant.azureKeySep +
					azureUser.getApplicationId() + CloudConstant.azureKeySep +
					azureUser.getSubscriptionId());
			account.setSecretKey(azureUser.getSecretKey());
			account.setDescription(cloudScopeXML.getDescription());
			account.setLoginUserId(azureUser.getAccountId());
			account.setUserName(azureUser.getDisplayName());
			req.setAccount(account);
		}
		
		return req;
	}

	private static OptionRequest createOption(String name, String value) {
		OptionRequest option = new OptionRequest();
		option.setName(name);
		option.setValue(value);
		return option;
	}

	public static void convertHyperVCloudUserRequestDto(ICloudScope info, AddCloudScopeRequest cloudScope,
			List<AddCloudLoginUserRequest> addRequestList, List<String> idList) {

		if (info.getHyperv() == null) {
			return;
		}
		List<HypervUser> output = new ArrayList<>(Arrays.asList(info.getHyperv().getHypervUser()));

		int index = 0;
		// メインユーザ削除
		for (HypervUser vmwareUser : new ArrayList<>(output)) {
			if (vmwareUser.getAccountId().equals(cloudScope.getAccount().getLoginUserId())) {
				output.remove(index);
				break;
			}
			index++;
		}

		for (HypervUser item : output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setUserName(item.getDisplayName());
			request.setUser(item.getUserName());
			request.setPassword(item.getPassword());
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}
	}

	public static void convertAzureCloudUserRequestDto(ICloudScope info, AddCloudScopeRequest cloudScope,
			List<AddCloudLoginUserRequest> addRequestList, List<String> idList) {

		if (info.getAzure() == null) {
			return;
		}
		List<AzureUser> output = new ArrayList<>(Arrays.asList(info.getAzure().getAzureUser()));

		int index = 0;
		for (AzureUser amazonUser : new ArrayList<>(output)) {
			if (amazonUser.getAccountId().equals(cloudScope.getAccount().getLoginUserId())) {
				output.remove(index);
				index--;
			}
			index++;
		}

		for (AzureUser item : output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setUserName(item.getDisplayName());
			request.setAccessKey(
					item.getTenantId() + CloudConstant.azureKeySep + 
					item.getApplicationId() + CloudConstant.azureKeySep +
					item.getSubscriptionId());
			request.setSecretKey(item.getSecretKey());
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}
	}

	/**
	 * キー情報保護が有効か無効かのフラグ情報をマネージャから取得して
	 * staticフィールド {@link #keyProtected} へ保存します。
	 */
	private static void updateKeyProtectedFlag() {
		CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		HinemosPropertyResponseP1 hp;
		try {
			hp = wrapper.getExpimpXcloudKeyprotectEnable();
			if ("true".equals(hp.getValue())) {
				keyProtected = true;
			} else if ("false".equals(hp.getValue())) {
				keyProtected = false;
			} else {
				// 異常な値ならデフォルト値
				keyProtected = HP_EXPIMP_XCLOUD_KEYPROTECT_ENABLE_DEFAULT;
			}
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * キー情報保護が有効な場合に、保護対象項目を空欄にします。
	 */
	private static VmwareUser hideProtectedKey(VmwareUser user) {
		if (!keyProtected) return user;
		user.setVmwarePassword("");
		return user;
	}
	
	/**
	 * キー情報保護が有効な場合に、保護対象項目を空欄にします。
	 */
	private static HypervUser hideProtectedKey(HypervUser user) {
		if (!keyProtected) return user;
		user.setPassword("");
		return user;
	}
	
	/**
	 * キー情報保護が有効な場合に、保護対象項目を空欄にします。
	 */
	private static AmazonUser hideProtectedKey(AmazonUser user) {
		if (!keyProtected) return user;
		user.setSecretKey("");
		return user;
	}
	
	/**
	 * キー情報保護が有効な場合に、保護対象項目を空欄にします。
	 */
	private static AzureUser hideProtectedKey(AzureUser user) {
		if (!keyProtected) return user;
		user.setSecretKey("");
		return user;
	}

	/**
	 * キー情報保護が有効な場合に、指定されたクラウドスコープXML入出力オブジェクト内の保護対象項目のうち、
	 * 空欄であるものをマネージャが保持している値へ置換します。
	 */
	public static void restoreProtectedKeys(ICloudScope xml) {
		updateKeyProtectedFlag();
		if (!keyProtected) return;
		
		com.clustercontrol.xcloud.model.cloud.ICloudScope model =
				CloudTools.getCloudScopeOrNull(xml.getCloudScopeId());
		if (model == null) return;

		if (xml.getCloudPlatformId().equals(CloudConstant.platform_ESXi) ||
				xml.getCloudPlatformId().equals(CloudConstant.platform_vCenter)) {
			// VMware
			for (VmwareUser user : xml.getVmware().getVmwareUser()) {
				if (StringUtils.isEmpty(user.getVmwarePassword())) {
					ILoginUser currUser = model.getLoginUsers().getLoginUser(user.getAccountId());
					if (currUser != null) {
						user.setVmwarePassword(((CredentialResponse) currUser.getCredential()).getPassword());
					}
				}
			}
		} else if (xml.getCloudPlatformId().equals(CloudConstant.platform_AWS)) {
			// AWS
			for (AmazonUser user : xml.getAmazon().getAmazonUser()) {
				if (StringUtils.isEmpty(user.getSecretKey())) {
					ILoginUser currUser = model.getLoginUsers().getLoginUser(user.getAccountId());
					if (currUser != null) {
						user.setSecretKey(((CredentialResponse) currUser.getCredential()).getSecretKey());
					}
				}
			}
		} else if (xml.getCloudPlatformId().equals(CloudConstant.platform_HyperV)) {
			// Hyper-V
			for (HypervUser user : xml.getHyperv().getHypervUser()) {
				if (StringUtils.isEmpty(user.getPassword())) {
					ILoginUser currUser = model.getLoginUsers().getLoginUser(user.getAccountId());
					if (currUser != null) {
						user.setPassword(((CredentialResponse) currUser.getCredential()).getPassword());
					}
				}
			}
		} else if (xml.getCloudPlatformId().equals(CloudConstant.platform_Azure)) {
			// Azure
			for (AzureUser user : xml.getAzure().getAzureUser()) {
				if (StringUtils.isEmpty(user.getSecretKey())) {
					ILoginUser currUser = model.getLoginUsers().getLoginUser(user.getAccountId());
					if (currUser != null) {
						user.setSecretKey(((CredentialResponse) currUser.getCredential()).getSecretKey());
					}
				}
			}
		} else {
			throw new UnsupportedOperationException("Unknown cloud platform ID: " + xml.getCloudPlatformId());
		}
	}

}