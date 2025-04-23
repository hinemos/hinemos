/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.cloud.conv;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openapitools.client.model.AddCloudLoginUserRequest;
import org.openapitools.client.model.AddCloudScopeRequest;
import org.openapitools.client.model.CredentialResponse;
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
import com.clustercontrol.utility.settings.cloud.action.CloudUserAction;
import com.clustercontrol.utility.settings.cloud.xml.Amazon;
import com.clustercontrol.utility.settings.cloud.xml.AmazonUser;
import com.clustercontrol.utility.settings.cloud.xml.Azure;
import com.clustercontrol.utility.settings.cloud.xml.AzureUser;
import com.clustercontrol.utility.settings.cloud.xml.Google;
import com.clustercontrol.utility.settings.cloud.xml.GoogleUser;
import com.clustercontrol.utility.settings.cloud.xml.Hyperv;
import com.clustercontrol.utility.settings.cloud.xml.HypervUser;
import com.clustercontrol.utility.settings.cloud.xml.ICloudScope;
import com.clustercontrol.utility.settings.cloud.xml.Oracle;
import com.clustercontrol.utility.settings.cloud.xml.OracleUser;
import com.clustercontrol.utility.settings.cloud.xml.Vmware;
import com.clustercontrol.utility.settings.cloud.xml.VmwareUser;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.bean.CloudConstant.HyperVProtocol;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.ILoginUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class CloudUserConv {
	
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType = VersionUtil.getSchemaProperty("CLOUD.CLOUDUSER.SCHEMATYPE");
	static private final String schemaVersion = VersionUtil.getSchemaProperty("CLOUD.CLOUDUSER.SCHEMAVERSION");
	static private String schemaRevision = VersionUtil.getSchemaProperty("CLOUD.CLOUDUSER.SCHEMAREVISION");
	
	//GCP/OCI cloudScope XML Specific Constants
	public static final String ICloudScope = "iCloudScope";
	public static final String CLOUDSCOPEID = "cloudScopeId";
	public static final String CLOUDPLATFORMID = "cloudPlatformId";
	public static final String ACCOUNTID = "accountId";
	
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
			if (cloudScopeEndpoint.getRetentionPeriod() != null) {
				amazon.setTerm(cloudScopeEndpoint.getRetentionPeriod());
			} else {
				amazon.setTerm(0);
			}
			amazon.setS3packet(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_awsS3Bucket));
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
			if (cloudScopeEndpoint.getRetentionPeriod() != null) {
				azure.setTerm(cloudScopeEndpoint.getRetentionPeriod());
			} else {
				azure.setTerm(0);
			}
			if (cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azureBeginDate) != null) {
				azure.setBeginDate(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azureBeginDate).replace("-", "/"));
			} else {
				azure.setBeginDate(null); // nullを設定した場合要素が出力されない
			}
			azure.setBillingRegion(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azureRegion));
			azure.setPlanId(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azurePlanId));
			azure.setCurrency(cloudScopeEndpoint.getExtendedProperty(CloudConstant.eprop_azureCurrency));
			
			cloudScope.setAzure(azure);
		} else if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_GCP)) {
			Google google = new Google();
			GoogleUser googleUser = new GoogleUser();

			// メインユーザ
			convertJSONObjectToGoogleUserObj(accountUser, googleUser);
			google.addGoogleUser(hideProtectedKey(googleUser));

			List<ILoginUser> currentUsers = new ArrayList<>(
					Arrays.asList(cloudScopeEndpoint.getLoginUsers().getLoginUsers()));
			currentUsers.remove(cloudScopeEndpoint.getLoginUsers().getLoginUser(cloudScopeEndpoint.getAccountId()));
			for (ILoginUser iLoginUser : currentUsers) {
				googleUser = new GoogleUser();

				// サブユーザ
				convertJSONObjectToGoogleUserObj(iLoginUser, googleUser);
				google.addGoogleUser(hideProtectedKey(googleUser));
			}

			// 課金情報
			ObjectMapper objMapper = new ObjectMapper();
			JsonNode serviceAccountJson = null;

			try {
				serviceAccountJson = objMapper.readTree(accountUser.getCredential().getJsonCredentialInfo());
				google.setType(serviceAccountJson.get(CloudConstant.AuthenticationType).asText());
				google.setProjectId(serviceAccountJson.get(CloudConstant.UseProjectId).asText());
			} catch (JsonProcessingException e) {
				log.warn("Error while processing json credential Info" + e.getMessage());
			}
			
			cloudScope.setGoogle(google);
		} else if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_OCI)) {
			Oracle oracle = new Oracle();
			OracleUser oracleUser = new OracleUser();

			// メインユーザ
			convertJSONObjectToOracleUserObj(accountUser, oracleUser);
			oracle.addOracleUser(hideProtectedKey(oracleUser));

			List<ILoginUser> currentUsers = new ArrayList<>(
					Arrays.asList(cloudScopeEndpoint.getLoginUsers().getLoginUsers()));
			currentUsers.remove(cloudScopeEndpoint.getLoginUsers().getLoginUser(cloudScopeEndpoint.getAccountId()));
			for (ILoginUser iLoginUser : currentUsers) {
				oracleUser = new OracleUser();

				// サブユーザ
				convertJSONObjectToOracleUserObj(iLoginUser, oracleUser);
				oracle.addOracleUser(hideProtectedKey(oracleUser));
			}

			// 課金情報
			ObjectMapper objMapper = new ObjectMapper();
			JsonNode privateKeyJson = null;

			try {
				privateKeyJson = objMapper.readTree(accountUser.getCredential().getJsonCredentialInfo());
				oracle.setAuthenticationType(privateKeyJson.get(CloudConstant.authenticationType).asText());
			} catch (JsonProcessingException e) {
				log.warn("Error while processing json credential Info" + e.getMessage());
			}
			
			cloudScope.setOracle(oracle);

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
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_GCP)) {
			// メインユーザ
			GoogleUser googleUser = info.getGoogle().getGoogleUser(0);
			ret.setUserName(googleUser.getDisplayName());
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
	
	private static GoogleUser hideProtectedKey(GoogleUser user) {
		if (!keyProtected)
			return user;
		if (user.getServiceAccountKeyFileName() != null) { // handle for attached service account.
			user.setServiceAccountKeyFileName("");
		}
		return user;
	}

	private static OracleUser hideProtectedKey(OracleUser user) {
		if (!keyProtected)
			return user;
		if (user.getPrivateKeyFileName() != null) { // handle for enable protection function
			user.setPrivateKeyFileName("");
		}
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
		} else if (xml.getCloudPlatformId().equals(CloudConstant.platform_GCP)) {
			// Google
			for (GoogleUser user : xml.getGoogle().getGoogleUser()) {
				if (StringUtils.isEmpty(user.getServiceAccountKeyFileName())) {
					ILoginUser currUser = model.getLoginUsers().getLoginUser(user.getAccountId());
					if (currUser != null) {
						ObjectMapper objMapper = new ObjectMapper();
						JsonNode serviceAccountJson = null;
						try {
							serviceAccountJson = objMapper.readTree(currUser.getCredential().getJsonCredentialInfo());
						} catch (JsonProcessingException e) {
							log.warn("Error occurred while parsing json: " + e.getMessage());
						}

						if (serviceAccountJson != null) {
							if (serviceAccountJson.get(CloudConstant.PrivateKeyFileName) != null
									&& !serviceAccountJson.get(CloudConstant.PrivateKeyFileName).asText().isEmpty()) {
								user.setServiceAccountKeyFileName(
										serviceAccountJson.get(CloudConstant.PrivateKeyFileName).asText());
							}
						}
					}
				}
			}
		} else if (xml.getCloudPlatformId().equals(CloudConstant.platform_OCI)) {
			// Oracle
			for (OracleUser user : xml.getOracle().getOracleUser()) {
				if (StringUtils.isEmpty(user.getPrivateKeyFileName())) {
					ILoginUser currUser = model.getLoginUsers().getLoginUser(user.getAccountId());
					if (currUser != null) {
						ObjectMapper objMapper = new ObjectMapper();
						JsonNode serviceAccountJson = null;
						try {
							serviceAccountJson = objMapper.readTree(currUser.getCredential().getJsonCredentialInfo());
						} catch (JsonProcessingException e) {
							log.warn("Error occurred while parsing json: " + e.getMessage());
						}

						if (serviceAccountJson != null) {
							if (serviceAccountJson.get(CloudConstant.privateKeyFileNameConst) != null
									&& !serviceAccountJson.get(CloudConstant.privateKeyFileNameConst).asText().isEmpty()) {
								user.setPrivateKeyFileName(
										serviceAccountJson.get(CloudConstant.privateKeyFileNameConst).asText());
							}
						}
					}
				}
			}
		} else {
			throw new UnsupportedOperationException("Unknown cloud platform ID: " + xml.getCloudPlatformId());
		}
	}

	/**
	 * Method added to convert Json object and set in the google user object
	 * 
	 * @param accountUser
	 * @param googleUser
	 * @return ILoginUser
	 */
	private static void convertJSONObjectToGoogleUserObj(ILoginUser accountUser, GoogleUser googleUser) {
		ObjectMapper objMapper = new ObjectMapper();
		JsonNode serviceAccountJson = null;
		try {
			serviceAccountJson = objMapper.readTree(accountUser.getCredential().getJsonCredentialInfo());

			if (serviceAccountJson != null) {
				if (serviceAccountJson.get(CloudConstant.PrivateKeyFileName) != null
						&& !serviceAccountJson.get(CloudConstant.PrivateKeyFileName).asText().isEmpty()) {
					googleUser.setServiceAccountKeyFileName(
							serviceAccountJson.get(CloudConstant.PrivateKeyFileName).asText());

				}
			}
			googleUser.setAccountId(accountUser.getId());
			googleUser.setDisplayName(accountUser.getName());
		} catch (JsonProcessingException e) {
			log.warn("Error occurred while parsing json object:" + e.getMessage());
		}
	}

	/**
	 * Method added to get GCP specific cloud scope request DTO.
	 * 
	 * @param cloudScope
	 * @param xmlFile
	 * @return
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws IOException
	 */
	public static AddCloudScopeRequest getGCPCloudScopeRequestDto(ICloudScope cloudScope, String xmlFile)
			throws InvalidSetting, HinemosUnknown {
		AddCloudScopeRequest req = new AddCloudScopeRequest();
		req.setPlatformId(cloudScope.getCloudPlatformId());
		req.setCloudScopeId(cloudScope.getCloudScopeId());
		req.setScopeName(cloudScope.getCloudScopeName());
		req.setOwnerRoleId(cloudScope.getOwnerRoleId());
		req.setDescription(cloudScope.getDescription());

		// メイン登録
		if (cloudScope.getGoogle() != null && cloudScope.getGoogle().getGoogleUserCount() > 0) {
			AddCloudLoginUserRequest account = new AddCloudLoginUserRequest();
			GoogleUser googleUser = cloudScope.getGoogle().getGoogleUser(0);

			account.setDescription(cloudScope.getDescription());
			account.setLoginUserId(googleUser.getAccountId());
			account.setUserName(googleUser.getDisplayName());
			account.setPlatform(CloudConstant.platform_GCP);
			String accountType = cloudScope.getGoogle().getType();
			fetchingGCPFolderDetails(new File(xmlFile), account, accountType, cloudScope.getCloudScopeId(), false, cloudScope.getGoogle().getProjectId(), googleUser.getServiceAccountKeyFileName());
			req.setAccount(account);
		}
		return req;
	}

	/**
	 * Invoked for Import functionality
	 * Purpose: This method is used to convert GCP cloud user object to request DTO.
	 * @param info
	 * @param cloudScope
	 * @param addRequestList
	 * @param idList
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static void convertGCPCloudUserRequestDto(ICloudScope info, AddCloudScopeRequest cloudScope,
			List<AddCloudLoginUserRequest> addRequestList, List<String> idList, String xmlFile) throws InvalidSetting, HinemosUnknown {
		if (info.getGoogle() == null) {
			return;
		}
		List<GoogleUser> output = new ArrayList<>(Arrays.asList(info.getGoogle().getGoogleUser()));
		int index = 0;
		for (GoogleUser googleUser : new ArrayList<>(output)) {
			if (googleUser.getAccountId().equals(cloudScope.getAccount().getLoginUserId())) {
				output.remove(index); //here we can directly input value as 0, first we need to create multiple google cloudscope
				break;
			}
		}
		
		for (GoogleUser item : output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setUserName(item.getDisplayName());
			request.setPlatform(CloudConstant.platform_GCP);
			fetchingGCPFolderDetails(new File(xmlFile), request, CloudConstant.ServiceAccountKey, info.getCloudScopeId(), true, info.getGoogle().getProjectId(), item.getServiceAccountKeyFileName());
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}
	}

	/**
	 * Invoked for export functionality Purpose: Method added to create folder
	 * structure to export GCP specific cloud scope details.
	 * 
	 * @param cloudScopeEndpoint
	 * @param xmlFile
	 * @return
	 */
	public static void getPrivateKeyFileContent(com.clustercontrol.xcloud.model.cloud.ICloudScope cloudScopeEndpoint,
			String xmlFile, String platform) {

		if (keyProtected) {
			return;
		}

		final ILoginUser accountUser = cloudScopeEndpoint.getLoginUsers()
				.getLoginUser(cloudScopeEndpoint.getAccountId());

		ObjectMapper objMapper = new ObjectMapper();
		JsonNode serviceAccountJsonNode = null;
		File cloudScopeFolder = null;
		File subAccountFolder = null;
		File cloudFile = null;
		String privateKeyFileName, privateKeyContent = null;
		FileWriter fileWriter = null;
		String dirPath = getDirectoryPath(xmlFile);

		
		try {
			cloudScopeFolder = new File(dirPath + cloudScopeEndpoint.getId());
			if (!cloudScopeFolder.mkdirs()){
				throw new IOException("Failed to create folder: " + cloudScopeFolder.getAbsolutePath());
			}
			
			serviceAccountJsonNode = objMapper.readTree(accountUser.getCredential().getJsonCredentialInfo());
			boolean isKeyfileType = false;
			String fileKey = "";
			if (platform.equals(CloudConstant.platform_GCP)) {
				isKeyfileType = serviceAccountJsonNode.get(CloudConstant.AuthenticationType).asText()
						.equals(CloudConstant.ServiceAccountKey);
				fileKey = CloudConstant.PrivateKeyFileName;
				privateKeyContent = accountUser.getCredential().getJsonCredentialInfo();
			} else if (platform.equals(CloudConstant.platform_OCI)){
				isKeyfileType = serviceAccountJsonNode.get(CloudConstant.authenticationType).asText()
						.equals(CloudConstant.apiKeyBasedAuthentication);
				fileKey = CloudConstant.privateKeyFileNameConst;
				if (isKeyfileType) {
					privateKeyContent = serviceAccountJsonNode.get(CloudConstant.privateKeyConst).asText();
				}
			}
			
			if (isKeyfileType) {

				privateKeyFileName = serviceAccountJsonNode.get(fileKey).asText();
				cloudFile = new File(cloudScopeFolder.getAbsolutePath().toString(), privateKeyFileName);

				fileWriter = new FileWriter(cloudFile);
				fileWriter.write(privateKeyContent);
				fileWriter.flush();

			}
		} catch (IOException e1) {
			log.warn("An error occurred while fetching/writing private key content" + e1.getMessage());
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					log.warn("An error occurred while closing file" + e.getMessage());
				}
			}
		}
		List<ILoginUser> currentUsers = new ArrayList<>(
				Arrays.asList(cloudScopeEndpoint.getLoginUsers().getLoginUsers()));
		currentUsers.remove(cloudScopeEndpoint.getLoginUsers().getLoginUser(cloudScopeEndpoint.getAccountId()));
		for (ILoginUser iLoginUser : currentUsers) {

			try {
				serviceAccountJsonNode = objMapper.readTree(iLoginUser.getCredential().getJsonCredentialInfo());

				subAccountFolder = new File(cloudScopeFolder + File.separator + iLoginUser.getId());
				if (!subAccountFolder.mkdirs()){
					throw new IOException("Failed to create folder: " + subAccountFolder.getAbsolutePath());
				}
				String fileKey = "";
				if (platform.equals(CloudConstant.platform_GCP)) {
					fileKey = CloudConstant.PrivateKeyFileName;
					privateKeyContent = iLoginUser.getCredential().getJsonCredentialInfo();
				} else if (platform.equals(CloudConstant.platform_OCI)){
					fileKey = CloudConstant.privateKeyFileNameConst;
					privateKeyContent = serviceAccountJsonNode.get(CloudConstant.privateKeyConst).asText();
				}
				
				privateKeyFileName = serviceAccountJsonNode.get(fileKey).asText();
				cloudFile = new File(subAccountFolder.getAbsolutePath(), privateKeyFileName);
				fileWriter = new FileWriter(cloudFile);
				fileWriter.write(privateKeyContent);
				fileWriter.flush();

			} catch (IOException e1) {
				log.warn("An error occurred while fetching/writing private key content" + e1.getMessage());
			} finally {
				if (fileWriter != null) {
					try {
						fileWriter.close();
					} catch (IOException e) {
						log.warn("An error occurred while closing file" + e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Invoked for import functionality Purpose: Method added to fetch
	 * cloudscope details from the path used to create zip file.
	 * 
	 * @param outFile
	 *            - This file will return path till .xml file
	 * @param account
	 *            - Forwarded to readJsonFile method
	 * @throws IOException
	 * @throws InvalidSetting
	 * @throws HinemosUnknown 
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 */
	@SuppressWarnings("unchecked")
	private static void fetchingGCPFolderDetails(File outFile, AddCloudLoginUserRequest account, String accountType, String cloudScopeId, boolean isSub, String projectId, String fileName)
			throws InvalidSetting, HinemosUnknown {
		
		String folderPath = outFile.getParent() + File.separator;
		String cloudFolderName = MultiManagerPathUtil.getCloudScopeFolder();
		
		folderPath += cloudFolderName + File.separator;
		if (isSub){
			folderPath += cloudScopeId + File.separator + account.getLoginUserId();
		} else {
			folderPath += cloudScopeId;
		}
		
		File checkIfFolderExist = new File(folderPath);
		if (accountType.equals(CloudConstant.ServiceAccountKey)) {
			boolean isEmptyDir = isEmptyDirectory(new File(checkIfFolderExist.getAbsolutePath()));
			boolean serviceAccExistInDB = CloudUserAction.checkServiceAccountDetailsExistinDB(cloudScopeId, account,
					isSub);

			if (!keyProtected && isEmptyDir) {
				log.warn("Service Account Key file not present for account: " + account.getLoginUserId());
				throw new InvalidSetting(
						"Service Account Key file not present for account: " + account.getLoginUserId());
			} else if (serviceAccExistInDB && isEmptyDir) {
				log.info("cloud scope already exist in DB");
				return;
			} else if (!serviceAccExistInDB && isEmptyDir) {
				log.warn("Service Account Key file not present for account: " + account.getLoginUserId());
				throw new InvalidSetting(
						"Service Account Key file not present for account: " + account.getLoginUserId());
			} else if (!(checkIfFolderExist.getName().equals(BackupUtil.getBackupFolder()))) {
				readJsonFile(checkIfFolderExist.getAbsolutePath(), account, isSub, fileName);
			}
			// update use specified project id
			ObjectMapper objMapper = new ObjectMapper();
			Map<String, String> tmpMap;
			try {
				tmpMap = objMapper.readValue(account.getJsonCredentialInfo(), Map.class);
				tmpMap.put(CloudConstant.UseProjectId, projectId);
				account.setJsonCredentialInfo(objMapper.writeValueAsString(tmpMap));
			} catch (JsonMappingException e) {
				throw new HinemosUnknown(e);
			} catch (JsonProcessingException e) {
				throw new HinemosUnknown(e);
			}
		} else {
			ObjectMapper objMapper = null;
			HashMap<String, String> jsonCredentialInfoMap = new HashMap<>();
			objMapper = new ObjectMapper();
			jsonCredentialInfoMap.put(CloudConstant.AuthenticationType, CloudConstant.AttachedServiceAccount);
			jsonCredentialInfoMap.put(CloudConstant.UseProjectId, projectId);
			String jsonCrdentialInfoString = null;
			try {
				jsonCrdentialInfoString = objMapper.writeValueAsString(jsonCredentialInfoMap);
			} catch (JsonProcessingException e) {
				log.warn("fetchingGCPFolderDetails(): failed to parse json", e);
				throw new HinemosUnknown(e);
			}
			
			account.setJsonCredentialInfo(jsonCrdentialInfoString);
		}
	}

	/**
	 * Invocation: This method will be invoked for import functionality.
	 * Purpose: Method added to read Service account json file of GCP
	 * 
	 * @param serviceAccountJson
	 *            - The json file present in .zip which is imported and part of
	 *            cloudscope
	 * @param account
	 *            - Contents of json file will be read and set in the
	 *            AddCloudLoginUserRequest
	 * @throws HinemosUnknown 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 */
	private static void readJsonFile(String serviceAccountJson, AddCloudLoginUserRequest account, boolean isSub, String fileName) throws HinemosUnknown {
		log.debug("readJson File: Start()");

		ObjectMapper objMapper = null;
		HashMap<String, String> jsonCredentialInfoMap = new HashMap<>();
		String jsonCrdentialInfoString = null;

		File[] files = new File(serviceAccountJson).listFiles();

		if (null == files) {
			log.info("dir.listFiles() == null");
			return;
		}
		
		boolean foundFile = false;
		for (File file : files) {
			if (file.isFile()) {
				if (!file.getName().equals(fileName)){
					log.info("readJsonFile(): skip different filename: " + file.getName());
					continue;
				}
				// This string will hold path till cloudScope folder and will be
				// appended with the service account json file.
				String serviceAccountJsonPath = serviceAccountJson + File.separator + file.getName();

				// create a reader to read the file
				try (BufferedReader reader = new BufferedReader(new FileReader(serviceAccountJsonPath))) {
					objMapper = new ObjectMapper();

					JsonNode serviceAccountJsonNode = null;

					serviceAccountJsonNode = objMapper.readTree(reader);

					jsonCredentialInfoMap.put(CloudConstant.ClientEmail,
							serviceAccountJsonNode.get(CloudConstant.ClientEmail).asText());
					jsonCredentialInfoMap.put(CloudConstant.ProjectId,
							serviceAccountJsonNode.get(CloudConstant.ProjectId).asText());
					jsonCredentialInfoMap.put(CloudConstant.PrivateKey,
							serviceAccountJsonNode.get(CloudConstant.PrivateKey).asText());
					jsonCredentialInfoMap.put(CloudConstant.PrivateKeyFileName, file.getName());
					
					
					if (serviceAccountJsonNode.get(CloudConstant.AuthenticationType) != null) {
						jsonCredentialInfoMap.put(CloudConstant.AuthenticationType,
								serviceAccountJsonNode.get(CloudConstant.AuthenticationType).asText());
					} else if (isSub) {
						// do not set anything for authentication type (Make it equivalent to creating setting from client)
					} else {
						jsonCredentialInfoMap.put(CloudConstant.AuthenticationType,
								CloudConstant.AttachedServiceAccount);
					}

					jsonCrdentialInfoString = objMapper.writeValueAsString(jsonCredentialInfoMap);
					account.setJsonCredentialInfo(jsonCrdentialInfoString);

				} catch (JsonProcessingException e) {
					log.warn("execute():Error in coverting Map to JsonString" + e);
					throw new HinemosUnknown(e);
				} catch (IOException e) {
					log.warn("Could not read json file: " + e.getMessage());
					throw new HinemosUnknown(e);
				}
				foundFile = true;
			}
		}
		if (!foundFile){
			log.warn("readJsonFile(): service account key file not found!");
			throw new HinemosUnknown("service account key file not found");
		}
	}

	/**
	 * Method added to check if the directory exist or not
	 * 
	 * @param directoryPath
	 */
	protected static void isExsitsAndCreate(String directoryPath) {
		File dir = new File(directoryPath);
		if (!dir.exists() && !directoryPath.endsWith("null")) {
			if (!dir.mkdir())
				log.warn(String.format("Fail to create Directory. %s", dir.getAbsolutePath()));
		}
	}

	/**
	 * Method added to fetch the directory Path
	 * 
	 * @param xmlFile
	 * @return directory path
	 */
	private static String getDirectoryPath(String xmlFile) {

		StringBuffer sb = new StringBuffer();
		String fileName = fetchFileNameWithoutExt(xmlFile);
		sb.append(fileName);
		sb.append(File.separator);
		isExsitsAndCreate(sb.toString());
		return sb.toString();
	}

	/**
	 * Method added to fetch filename without extension
	 * 
	 * @param xmlFile
	 * @return file name without extension.
	 */

	public static String fetchFileNameWithoutExt(String xmlFile) {

		int pos = xmlFile.lastIndexOf('.');
		if (pos > -1)
			return xmlFile.substring(0, pos);
		else
			return xmlFile;
	}
	
	/**
	 * This is a boolean function which return true if directory is empty else
	 * return false
	 * 
	 * @param directory
	 * @return
	 * @throws HinemosUnknown 
	 * @throws IOException
	 */
	public static boolean isEmptyDirectory(File directory) throws HinemosUnknown {
		// check if given path is a directory
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				// throw exception if given path is a
				// file
				throw new IllegalArgumentException("Expected directory, but was file: " + directory);
			} else {
				// create a stream and check for files
				try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory.toPath())) {
					// return false if there is a file
					return !directoryStream.iterator().hasNext();
				} catch (IOException e) {
					log.warn("isEmptyDirectory(): failed to check directory.", e);
					throw new HinemosUnknown(e);
				}
			}
		}
		// return true if no file is present
		return true;
	}

	/**
	 * Method added to get OCI specific cloud scope request DTO.
	 * 
	 * @param cloudScope
	 * @param xmlFile
	 * @return
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws IOException
	 */
	public static AddCloudScopeRequest getOCICloudScopeRequestDto(ICloudScope cloudScope, String xmlFile)
			throws InvalidSetting, HinemosUnknown {
		AddCloudScopeRequest req = new AddCloudScopeRequest();
		req.setPlatformId(cloudScope.getCloudPlatformId());
		req.setCloudScopeId(cloudScope.getCloudScopeId());
		req.setScopeName(cloudScope.getCloudScopeName());
		req.setOwnerRoleId(cloudScope.getOwnerRoleId());
		req.setDescription(cloudScope.getDescription());

		// メイン登録
		if (cloudScope.getOracle() != null && cloudScope.getOracle().getOracleUserCount() > 0) {
			AddCloudLoginUserRequest account = new AddCloudLoginUserRequest();
			OracleUser oracleUser = cloudScope.getOracle().getOracleUser(0);

			account.setDescription(cloudScope.getDescription());
			account.setLoginUserId(oracleUser.getAccountId());
			account.setUserName(oracleUser.getDisplayName());
			account.setPlatform(CloudConstant.platform_OCI);
			String accountType = cloudScope.getOracle().getAuthenticationType();
			fetchingOCIFolderDetails(new File(xmlFile), account, accountType, oracleUser, cloudScope.getCloudScopeId(),
					false);
			req.setAccount(account);
		}
		return req;
	}

	/**
	 * Invoked for Import functionality Purpose: This method is used to convert
	 * OCI cloud user object to request DTO.
	 * 
	 * @param info
	 * @param cloudScope
	 * @param addRequestList
	 * @param idList
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public static void convertOCICloudUserRequestDto(ICloudScope info, AddCloudScopeRequest cloudScope,
			List<AddCloudLoginUserRequest> addRequestList, List<String> idList, String xmlFile)
			throws InvalidSetting, HinemosUnknown {

		if (info.getOracle() == null) {
			return;
		}
		List<OracleUser> output = new ArrayList<>(Arrays.asList(info.getOracle().getOracleUser()));
		int index = 0;
		for (OracleUser oracleUser : new ArrayList<>(output)) {
			if (oracleUser.getAccountId().equals(cloudScope.getAccount().getLoginUserId())) {
				output.remove(index); // here we can directly input value as 0,
										// first we need to create multiple
										// oci cloudscope
				break;
			}
		}

		for (OracleUser item : output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setUserName(item.getDisplayName());
			request.setPlatform(CloudConstant.platform_OCI);
			fetchingOCIFolderDetails(new File(xmlFile), request, CloudConstant.apiKeyBasedAuthentication, item,
					info.getCloudScopeId(), true);
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}
	}

	/**
	 * Invoked for import functionality Purpose: Method added to fetch
	 * cloudscope details from the path used to create zip file.
	 * 
	 * @param outFile
	 *            - This file will return path till .xml file
	 * @param account
	 *            - Forwarded to readJsonFile method
	 * @throws IOException
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 */
	private static void fetchingOCIFolderDetails(File outFile, AddCloudLoginUserRequest account, String accountType,
			OracleUser oracleUser, String cloudScopeId, boolean isSub) throws InvalidSetting, HinemosUnknown {

		String folderPath = outFile.getParent() + File.separator;
		String cloudFolderName = MultiManagerPathUtil.getCloudScopeFolder();
		
		folderPath += cloudFolderName + File.separator;
		if (isSub){
			folderPath += cloudScopeId + File.separator + account.getLoginUserId();
		} else {
			folderPath += cloudScopeId;
		}

		File checkIfFolderExist = new File(folderPath);
		if (accountType.equals(CloudConstant.apiKeyBasedAuthentication)) {
			boolean isEmptyDir = isEmptyDirectory(new File(checkIfFolderExist.getAbsolutePath()));
			boolean serviceAccExistInDB = CloudUserAction.checkServiceAccountDetailsExistinDB(cloudScopeId, account,
					isSub);
			
			if (!keyProtected && isEmptyDir) {
				log.warn("Service Account Key file not present for account: " + account.getLoginUserId());
				throw new InvalidSetting(
						"Service Account Key file not present for account: " + account.getLoginUserId());
			} else if (serviceAccExistInDB && isEmptyDir) {
				log.info("cloud scope already exist in DB");
				return;
			} else if (!serviceAccExistInDB && isEmptyDir) {
				log.warn("Service Account Key file not present for account: " + account.getLoginUserId());
				throw new InvalidSetting(
						"Service Account Key file not present for account: " + account.getLoginUserId());
			} else if (!(checkIfFolderExist.getName().equals(BackupUtil.getBackupFolder()))) {
				readAPIAuthFile(checkIfFolderExist.getAbsolutePath(), account, accountType, oracleUser, isSub);
			}
		} else {
			ObjectMapper objMapper = null;
			HashMap<String, String> jsonCredentialInfoMap = new HashMap<>();
			objMapper = new ObjectMapper();
			jsonCredentialInfoMap.put(CloudConstant.authenticationType, CloudConstant.instancePrincipalAuthentication);
			jsonCredentialInfoMap.put(CloudConstant.tenantIdConst, oracleUser.getTenantId());
			String jsonCrdentialInfoString = null;
			try {
				jsonCrdentialInfoString = objMapper.writeValueAsString(jsonCredentialInfoMap);
			} catch (JsonProcessingException e) {
				log.warn("fetchingOCIFolderDetails(): failed to read file contents", e);
				throw new HinemosUnknown(e);
			}

			account.setJsonCredentialInfo(jsonCrdentialInfoString);
		}
	}

	/**
	 * Invocation: This method will be invoked for import functionality.
	 * Purpose: Method added to read pem file contents for OCI
	 * 
	 * @param serviceAccountJson
	 *            - The .pem file present in .zip which is imported and part of
	 *            cloudscope
	 * @param account
	 *            - Contents of pem file will be read and set in the
	 *            AddCloudLoginUserRequest
	 * @throws HinemosUnknown
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 */
	private static void readAPIAuthFile(String serviceAccountJson, AddCloudLoginUserRequest account, String accountType,
			OracleUser oracleUser, boolean isSub) throws HinemosUnknown {

		log.info("readAPIAuthFile File: Start()");

		HashMap<String, String> jsonCredentialInfoMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;

		File[] files = new File(serviceAccountJson).listFiles();

		if (null == files) {
			log.info("dir.listFiles() == null");
			return;
		}
		boolean foundFile = false;
		for (File file : files) {
			if (file.isFile()) {
				if (!file.getName().equals(oracleUser.getPrivateKeyFileName())){
					log.info("readAPIAuthFile(): skip different filename: " + file.getName());
					continue;
				}
				// This string will hold path till cloudScope folder and will be
				// appended with the service account file.
				String serviceAccountJsonPath = serviceAccountJson + File.separator + file.getName();
				if (oracleUser.getPassphrase() != null) {
					jsonCredentialInfoMap.put(CloudConstant.passPhraseConst, "");
				} else {
					jsonCredentialInfoMap.put(CloudConstant.passPhraseConst, oracleUser.getPassphrase());
				}
				jsonCredentialInfoMap.put(CloudConstant.regionConst, oracleUser.getDefaultRegion());
				jsonCredentialInfoMap.put(CloudConstant.tenantIdConst, oracleUser.getTenantId());
				jsonCredentialInfoMap.put(CloudConstant.userIdConst, oracleUser.getUserId());
				jsonCredentialInfoMap.put(CloudConstant.fingerprintConst, oracleUser.getFingerprint());
				jsonCredentialInfoMap.put(CloudConstant.privateKeyFileNameConst, oracleUser.getPrivateKeyFileName());
				if (accountType != null && accountType.equals(CloudConstant.apiKeyBasedAuthentication)) {
					jsonCredentialInfoMap.put(CloudConstant.authenticationType, accountType);
				} else if (isSub) {
					// do not set anything for authentication type (Make it
					// equivalent to creating setting from client)
				} else {
					jsonCredentialInfoMap.put(CloudConstant.authenticationType, accountType);
				}
				String filteredPrivateKeyContent = readFileContents(serviceAccountJsonPath);
				jsonCredentialInfoMap.put(CloudConstant.privateKeyConst, filteredPrivateKeyContent);

				try {
					jsonString = mapper.writeValueAsString(jsonCredentialInfoMap);
				} catch (JsonProcessingException e) {
					log.error("Error in coverting Map to JsonString: " + e);
				}
				account.setJsonCredentialInfo(jsonString);
				foundFile = true;
			}
		}
		if (!foundFile) {
			log.warn("readAPIAuthFile(): private key file not found!");
			throw new HinemosUnknown("private key file not found");
		}
	}

	/**
	 * Method added to read file contents(i.e. .pem file)
	 * 
	 * @param serviceAccountJsonPath
	 * @return String containing file contents
	 * @throws HinemosUnknown
	 */
	public static String readFileContents(String serviceAccountJsonPath) throws HinemosUnknown {
		String filteredPrivateKeyContent = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			
			Files.copy(Paths.get(serviceAccountJsonPath), baos);
			filteredPrivateKeyContent = baos.toString();
		} catch (IOException e) {
			log.warn("Could not read pem file: " + e.getMessage());
			throw new HinemosUnknown(e);
		}
		return filteredPrivateKeyContent;
	}

	/**
	 * Method added to convert Json object and set in the google user object
	 * 
	 * @param accountUser
	 * @param googleUser
	 * @return ILoginUser
	 */
	private static void convertJSONObjectToOracleUserObj(ILoginUser accountUser, OracleUser oracleUser) {
		ObjectMapper objMapper = new ObjectMapper();
		JsonNode serviceAccountJson = null;
		try {
			serviceAccountJson = objMapper.readTree(accountUser.getCredential().getJsonCredentialInfo());

			if (serviceAccountJson != null) {
				if (serviceAccountJson.get(CloudConstant.privateKeyFileNameConst) != null
						&& !serviceAccountJson.get(CloudConstant.privateKeyFileNameConst).asText().isEmpty()) {
					oracleUser.setPrivateKeyFileName(
							serviceAccountJson.get(CloudConstant.privateKeyFileNameConst).asText());
					oracleUser.setDefaultRegion(serviceAccountJson.get(CloudConstant.regionConst).asText());
					oracleUser.setUserId(serviceAccountJson.get(CloudConstant.userIdConst).asText());
					oracleUser.setFingerprint(serviceAccountJson.get(CloudConstant.fingerprintConst).asText());
					oracleUser.setPassphrase(serviceAccountJson.get(CloudConstant.passPhraseConst).asText());
				}
				oracleUser.setTenantId(serviceAccountJson.get(CloudConstant.tenantIdConst).asText());
			}
			
			oracleUser.setAccountId(accountUser.getId());
			oracleUser.setDisplayName(accountUser.getName());
		} catch (JsonProcessingException e) {
			log.warn("Error occurred while parsing json object:" + e.getMessage());
		}
	}

}