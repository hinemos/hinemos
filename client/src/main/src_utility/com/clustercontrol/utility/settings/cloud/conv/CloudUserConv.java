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

import org.apache.log4j.Logger;

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
import com.clustercontrol.ws.xcloud.AccessKeyCredential;
import com.clustercontrol.ws.xcloud.Account;
import com.clustercontrol.ws.xcloud.AddCloudLoginUserRequest;
import com.clustercontrol.ws.xcloud.AddCloudScopeRequest;
import com.clustercontrol.ws.xcloud.AddPrivateCloudScopeRequest;
import com.clustercontrol.ws.xcloud.AddPublicCloudScopeRequest;
import com.clustercontrol.ws.xcloud.ModifyBillingSettingRequest;
import com.clustercontrol.ws.xcloud.ModifyCloudLoginUserRequest;
import com.clustercontrol.ws.xcloud.ModifyCloudScopeRequest;
import com.clustercontrol.ws.xcloud.ModifyPrivateCloudScopeRequest;
import com.clustercontrol.ws.xcloud.ModifyPublicCloudScopeRequest;
import com.clustercontrol.ws.xcloud.Option;
import com.clustercontrol.ws.xcloud.PrivateEndpoint;
import com.clustercontrol.ws.xcloud.PrivateLocation;
import com.clustercontrol.ws.xcloud.UserCredential;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.ILoginUser;

public class CloudUserConv {
	
	private static final String schemaType="I";
	private static final String schemaVersion="1";
	private static final String schemaRevision="1" ;
	
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
				vmwareUser.setVmwareUserName(((UserCredential)accountUser.getCredential()).getUser());;
				vmwareUser.setVmwarePassword(((UserCredential)accountUser.getCredential()).getPassword());;
				vmwareUser.setAccountId(accountUser.getId());
				vmwareUser.setDisplayName(accountUser.getName());
				vmware.addVmwareUser(vmwareUser);
				cloudScope.setVmware(vmware);
				
				List<ILoginUser> currentUsers = new ArrayList<>(Arrays.asList(cloudScopeEndpoint.getLoginUsers().getLoginUsers()));
				currentUsers.remove(cloudScopeEndpoint.getLoginUsers().getLoginUser(cloudScopeEndpoint.getAccountId()));
				
				for (ILoginUser user : currentUsers){
					vmwareUser = new VmwareUser();
					vmwareUser.setVmwareUserName(((UserCredential)user.getCredential()).getUser());;
					vmwareUser.setVmwarePassword(((UserCredential)user.getCredential()).getPassword());;
					vmwareUser.setAccountId(user.getId());
					vmwareUser.setDisplayName(user.getName());
					vmware.addVmwareUser(vmwareUser);
					cloudScope.setVmware(vmware);
				}
				
			} catch (MalformedURLException e) {
				log.error(e);
			}
		} else if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_AWS)) {
			Amazon amazon = new Amazon();
			AmazonUser amazonUser = new AmazonUser();
			
			// メインユーザ
			amazonUser.setAccessKey(((AccessKeyCredential) accountUser.getCredential()).getAccessKey());
			amazonUser.setSecretKey(((AccessKeyCredential) accountUser.getCredential()).getSecretKey());
			amazonUser.setAccountId(accountUser.getId());
			amazonUser.setDisplayName(accountUser.getName());
			amazon.addAmazonUser(amazonUser);
			
			List<ILoginUser> currentUsers = new ArrayList<>(Arrays.asList(cloudScopeEndpoint.getLoginUsers().getLoginUsers()));
			currentUsers.remove(cloudScopeEndpoint.getLoginUsers().getLoginUser(cloudScopeEndpoint.getAccountId()));
			for (ILoginUser iLoginUser :currentUsers ){
				amazonUser = new AmazonUser();
				
				// サブユーザ
				amazonUser.setAccessKey(((AccessKeyCredential) iLoginUser.getCredential()).getAccessKey());
				amazonUser.setSecretKey(((AccessKeyCredential) iLoginUser.getCredential()).getSecretKey());
				amazonUser.setAccountId(iLoginUser.getId());
				amazonUser.setDisplayName(iLoginUser.getName());
				amazon.addAmazonUser(amazonUser);
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
			rootUser.setUserName(((UserCredential)accountUser.getCredential()).getUser());;
			rootUser.setPassword(((UserCredential)accountUser.getCredential()).getPassword());;
			rootUser.setAccountId(accountUser.getId());
			rootUser.setDisplayName(accountUser.getName());
			hyperv.addHypervUser(rootUser);
			
			for (ILoginUser user : cloudScopeEndpoint.getLoginUsers().getLoginUsers()){
				if (user.getId().equals(accountUser.getId()))
					continue;
				
				HypervUser subUser = new HypervUser();
				subUser.setUserName(((UserCredential)user.getCredential()).getUser());;
				subUser.setPassword(((UserCredential)user.getCredential()).getPassword());;
				subUser.setAccountId(user.getId());
				subUser.setDisplayName(user.getName());
				hyperv.addHypervUser(subUser);
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
			String[] ids = ((AccessKeyCredential) accountUser.getCredential()).getAccessKey().split(CloudConstant.azureKeySep);
			rootUser.setTenantId(ids[0]);
			rootUser.setApplicationId(ids[1]);
			rootUser.setSubscriptionId(ids[2]);
			rootUser.setSecretKey(((AccessKeyCredential) accountUser.getCredential()).getSecretKey());
			rootUser.setAccountId(accountUser.getId());
			rootUser.setDisplayName(accountUser.getName());
			azure.addAzureUser(rootUser);
			
			for (ILoginUser user : cloudScopeEndpoint.getLoginUsers().getLoginUsers()) {
				if (user.getId().equals(accountUser.getId()))
					continue;
				
				// サブユーザ
				AzureUser subUser = new AzureUser();
				String[] subIds = ((AccessKeyCredential) user.getCredential()).getAccessKey().split(CloudConstant.azureKeySep);
				subUser.setTenantId(subIds[0]);
				subUser.setApplicationId(subIds[1]);
				subUser.setSubscriptionId(subIds[2]);
				subUser.setSecretKey(((AccessKeyCredential) user.getCredential()).getSecretKey());
				subUser.setAccountId(user.getId());
				subUser.setDisplayName(user.getName());
				azure.addAzureUser(subUser);
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
	public static AddPublicCloudScopeRequest getPublicCloudScopeRequestDto(ICloudScope cloudScope) {
		AddPublicCloudScopeRequest req = new AddPublicCloudScopeRequest();

		req.setPlatformId(cloudScope.getCloudPlatformId());
		req.setCloudScopeId(cloudScope.getCloudScopeId());
		req.setScopeName(cloudScope.getCloudScopeName());
		req.setOwnerRoleId(cloudScope.getOwnerRoleId());
		req.setDescription(cloudScope.getDescription());

		Account account = new Account();
		AccessKeyCredential credential = new AccessKeyCredential();
		
		// メイン登録
		AmazonUser amazonUser = cloudScope.getAmazon().getAmazonUser(0);
		credential.setAccessKey(amazonUser.getAccessKey());
		credential.setSecretKey(amazonUser.getSecretKey());
		account.setCredential(credential);
		account.setDescription(cloudScope.getDescription());
		account.setLoginUserId(amazonUser.getAccountId());
		account.setUserName(amazonUser.getDisplayName());
		req.setAccount(account);
		
		return req;
	}
	
	
	// Amazon
	public static ModifyPublicCloudScopeRequest getModifyPublicCloudScopeRequestDto(ICloudScope info) {
		ModifyPublicCloudScopeRequest ret = new ModifyPublicCloudScopeRequest();

		ret.setCloudScopeId(info.getCloudScopeId());
		ret.setScopeName(info.getCloudScopeName());
		ret.setDescription(info.getDescription());
	
		return ret;
	}
	
	// VMware
	public static AddCloudScopeRequest getPrivateCloudScopeRequestDto(ICloudScope cloudScope) {
		
		AddPrivateCloudScopeRequest req = new AddPrivateCloudScopeRequest();
		
		req.setPlatformId(cloudScope.getCloudPlatformId());
		req.setCloudScopeId(cloudScope.getCloudScopeId());
		req.setScopeName(cloudScope.getCloudScopeName());
		req.setOwnerRoleId(cloudScope.getOwnerRoleId());
		req.setDescription(cloudScope.getDescription());
		
		
		Account account = new Account();
		UserCredential credential = new UserCredential();
		
		// メイン (XML上は１件目がメイン)
		VmwareUser vmwareUser = cloudScope.getVmware().getVmwareUser(0);
		
		credential.setUser(vmwareUser.getVmwareUserName());//root
		credential.setPassword(vmwareUser.getVmwarePassword());
		account.setCredential(credential);
		account.setDescription(cloudScope.getDescription());
		account.setLoginUserId(vmwareUser.getAccountId());
		account.setUserName(vmwareUser.getDisplayName());
		req.setAccount(account);
		
		PrivateLocation location = new PrivateLocation();
		
		PrivateEndpoint e = new PrivateEndpoint();
		if (cloudScope.getCloudPlatformId().equals(CloudConstant.platform_ESXi)) {
			location.setLocationId(CloudConstant.location_ESXi);
			location.setName(CloudConstant.location_ESXi);
			e.setEndpointId(CloudConstant.location_ESXi);
		} else {
			location.setLocationId(CloudConstant.location_vCenter);
			location.setName(CloudConstant.location_vCenter);
			e.setEndpointId(CloudConstant.location_vCenter);
		}

		try {
			e.setUrl(new URL(
					cloudScope.getVmware().getVmwareExiProtocol(),
					cloudScope.getVmware().getVmwareExiIp() ,
					CloudConstant.filename_vmware).toString()
					);
		} catch (MalformedURLException e1) {
			throw new CloudModelException(e1);
		} catch(Throwable t){
			t.printStackTrace();
		}
		
		location.getEndpoints().add(e);
		req.getPrivateLocations().add(location);
		return req;
	}

	// VMware
	public static ModifyCloudScopeRequest getModifyPrivateCloudScopeRequestDto(ICloudScope info) {
		ModifyPrivateCloudScopeRequest ret = new ModifyPrivateCloudScopeRequest();
		
		ret.setCloudScopeId(info.getCloudScopeId());
		ret.setScopeName(info.getCloudScopeName());
		ret.setDescription(info.getDescription());
		PrivateLocation location = new PrivateLocation();
		PrivateEndpoint e = new PrivateEndpoint();
		
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
		
		location.getEndpoints().add(e);
		ret.getPrivateLocations().add(location);
		
		return ret;
	}
	
	// Amazon or VMware or Hyper-V or Azure
	public static ModifyCloudLoginUserRequest getModifyCloudLoginUserRequestDto(ICloudScope info) {
		ModifyCloudLoginUserRequest ret = new ModifyCloudLoginUserRequest();
		
		ret.setCloudScopeId(info.getCloudScopeId());
		ret.setDescription(info.getDescription());
		
		if (info.getCloudPlatformId().equals(CloudConstant.platform_ESXi) || info.getCloudPlatformId().equals(CloudConstant.platform_vCenter)) {
			//メインユーザ
			VmwareUser vmwareUser = info.getVmware().getVmwareUser(0);
			ret.setLoginUserId(vmwareUser.getAccountId());
			ret.setUserName(vmwareUser.getDisplayName());

			UserCredential credential = new UserCredential();
			credential.setUser(vmwareUser.getVmwareUserName());
			credential.setPassword(vmwareUser.getVmwarePassword());
			ret.setCredential(credential);
			
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_AWS)) {
			// メインユーザ
			AmazonUser amazonUser = info.getAmazon().getAmazonUser(0);
			ret.setLoginUserId(amazonUser.getAccountId());
			ret.setUserName(amazonUser.getDisplayName());
			
			AccessKeyCredential credential = new AccessKeyCredential();
			credential.setAccessKey(amazonUser.getAccessKey());
			credential.setSecretKey(amazonUser.getSecretKey());
			ret.setCredential(credential);
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_HyperV)) {
			//メインユーザ
			HypervUser hypervUser = info.getHyperv().getHypervUser(0);
			ret.setLoginUserId(hypervUser.getAccountId());
			ret.setUserName(hypervUser.getDisplayName());

			UserCredential credential = new UserCredential();
			credential.setUser(hypervUser.getUserName());
			credential.setPassword(hypervUser.getPassword());
			ret.setCredential(credential);
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_Azure)) {
			// メインユーザ
			AzureUser azureUser = info.getAzure().getAzureUser(0);
			ret.setLoginUserId(azureUser.getAccountId());
			ret.setUserName(azureUser.getDisplayName());
			
			AccessKeyCredential credential = new AccessKeyCredential();
			credential.setAccessKey(azureUser.getTenantId() + CloudConstant.azureKeySep +
									azureUser.getApplicationId() + CloudConstant.azureKeySep +
									azureUser.getSubscriptionId());
			credential.setSecretKey(azureUser.getSecretKey());
			ret.setCredential(credential);
		}
		
		return ret;
	}

	public static void getModifyPublicCloudUserRequestDto(ICloudScope info,
			List<AddCloudLoginUserRequest> addRequestList, List<String> removeIdList, List<String> idList) {

		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> currentAllList = CloudTools.getCloudScopeList();
		com.clustercontrol.xcloud.model.cloud.ICloudScope selectedScope = null;
		for (com.clustercontrol.xcloud.model.cloud.ICloudScope iCloudScope:currentAllList){
			if (iCloudScope.getId().equals(info.getCloudScopeId()))
			selectedScope = iCloudScope;
		}
		List<ILoginUser> currentUsers = new ArrayList<>(Arrays.asList(selectedScope.getLoginUsers().getLoginUsers()));
		currentUsers.remove(selectedScope.getLoginUsers().getLoginUser(selectedScope.getAccountId()));
		
		List<AmazonUser> output = new ArrayList<>(Arrays.asList(info.getAmazon().getAmazonUser()));
		int index = 0;
		for (AmazonUser amazonUser : new ArrayList<>(output)){
			if (amazonUser.getAccountId().equals(selectedScope.getAccountId())){
				output.remove(index);
				index--;
			}
			index++;
		}
		
		for (AmazonUser item: output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setCloudScopeId(info.getCloudScopeId());
			request.setUserName(item.getDisplayName());
			AccessKeyCredential credentail = new AccessKeyCredential();
			credentail.setAccessKey(item.getAccessKey());
			credentail.setSecretKey(item.getSecretKey());
			request.setCredential(credentail);
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}
		
		for(ILoginUser user: currentUsers){
			removeIdList.add(user.getId());
		}
	}
	
	public static void getModifyPrivateCloudUserRequestDto(ICloudScope info,
			List<AddCloudLoginUserRequest> addRequestList, List<String> removeIdList, List<String> idList) {
		
		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> currentAllList = CloudTools.getCloudScopeList();
		com.clustercontrol.xcloud.model.cloud.ICloudScope selectedScope = null;
		for (com.clustercontrol.xcloud.model.cloud.ICloudScope iCloudScope:currentAllList){
			if (iCloudScope.getId().equals(info.getCloudScopeId()))
			selectedScope = iCloudScope;
		}
		
		List<ILoginUser> currentUsers = new ArrayList<>(Arrays.asList(selectedScope.getLoginUsers().getLoginUsers()));
		//メインユーザ削除
		currentUsers.remove(selectedScope.getLoginUsers().getLoginUser(selectedScope.getAccountId()));
		
		List<VmwareUser> output = new ArrayList<>(Arrays.asList(info.getVmware().getVmwareUser()));
		int index = 0;
		//メインユーザ削除
		for (VmwareUser vmwareUser : new ArrayList<>(output)){
			if (vmwareUser.getAccountId().equals(selectedScope.getAccountId())){
				output.remove(index);
				break;
			}
			index++;
		}
		
		for (VmwareUser item: output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setCloudScopeId(info.getCloudScopeId());
			request.setUserName(item.getDisplayName());
			UserCredential credentail = new UserCredential();
			credentail.setUser(item.getVmwareUserName());
			credentail.setPassword(item.getVmwarePassword());
			request.setCredential(credentail);
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}

		for(ILoginUser user: currentUsers){
			removeIdList.add(user.getId());
		}
		
	}

	// publicクラウド(AWS,Azure)固有
	public static ModifyBillingSettingRequest createBillingSettingRequest(ICloudScope info) {
		ModifyBillingSettingRequest req = new ModifyBillingSettingRequest();
		
		req.setCloudScopeId(info.getCloudScopeId());
		if (info.getAmazon() != null) {
			req.setRetentionPeriod(info.getAmazon().getTerm());
			req.setBillingDetailCollectorFlg(info.getAmazon().getValidCollect());
			req.getOptions().add(createOption(CloudConstant.eprop_awsS3Bucket, info.getAmazon().getS3packet()));
		} else if (info.getAzure() != null) {
			req.setRetentionPeriod(info.getAzure().getTerm());
			req.setBillingDetailCollectorFlg(info.getAzure().getValidCollect());
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

	public static AddCloudScopeRequest getHyperVCloudScopeRequestDto(ICloudScope info) {
		AddPrivateCloudScopeRequest req = new AddPrivateCloudScopeRequest();
		
		req.setPlatformId(info.getCloudPlatformId());
		req.setCloudScopeId(info.getCloudScopeId());
		req.setScopeName(info.getCloudScopeName());
		req.setOwnerRoleId(info.getOwnerRoleId());
		req.setDescription(info.getDescription());
		
		// メイン (XML上は１件目がメイン)
		HypervUser hypervUser = info.getHyperv().getHypervUser(0);
		
		UserCredential credential = new UserCredential();
		credential.setUser(hypervUser.getUserName());
		credential.setPassword(hypervUser.getPassword());
		
		Account account = new Account();
		account.setCredential(credential);
		account.setDescription(info.getDescription());
		account.setLoginUserId(hypervUser.getAccountId());
		account.setUserName(hypervUser.getDisplayName());
		req.setAccount(account);
		
		PrivateEndpoint e = new PrivateEndpoint();
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
		
		PrivateLocation location =
				new PrivateLocation();
		location.setLocationId(CloudConstant.location_HyperV);
		location.setName(info.getCloudPlatformId());
		location.getEndpoints().add(e);
		req.getPrivateLocations().add(location);
		
		return req;
	}

	public static ModifyCloudScopeRequest getModifyHyperVCloudScopeRequestDto(ICloudScope info) {
		ModifyPrivateCloudScopeRequest req = new ModifyPrivateCloudScopeRequest();
		
		req.setCloudScopeId(info.getCloudScopeId());
		req.setScopeName(info.getCloudScopeName());
		req.setDescription(info.getDescription());
		
		PrivateEndpoint e = new PrivateEndpoint();
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
		PrivateLocation location = new PrivateLocation();
		location.setLocationId(info.getCloudPlatformId());
		location.setName(info.getCloudPlatformId());
		location.getEndpoints().add(e);
		req.getPrivateLocations().add(location);
		
		return req;
	}

	public static AddCloudScopeRequest getAzureCloudScopeRequestDto(ICloudScope cloudScopeXML) {
		AddPublicCloudScopeRequest req = new AddPublicCloudScopeRequest();

		req.setPlatformId(cloudScopeXML.getCloudPlatformId());
		req.setCloudScopeId(cloudScopeXML.getCloudScopeId());
		req.setScopeName(cloudScopeXML.getCloudScopeName());
		req.setOwnerRoleId(cloudScopeXML.getOwnerRoleId());
		req.setDescription(cloudScopeXML.getDescription());

		Account account = new Account();
		AccessKeyCredential credential = new AccessKeyCredential();
		
		// メイン登録
		AzureUser azureUser = cloudScopeXML.getAzure().getAzureUser(0);
		credential.setAccessKey(azureUser.getTenantId() + CloudConstant.azureKeySep +
								azureUser.getApplicationId() + CloudConstant.azureKeySep +
								azureUser.getSubscriptionId());
		credential.setSecretKey(azureUser.getSecretKey());
		account.setCredential(credential);
		account.setDescription(cloudScopeXML.getDescription());
		account.setLoginUserId(azureUser.getAccountId());
		account.setUserName(azureUser.getDisplayName());
		req.setAccount(account);
		
		return req;
	}

	private static Option createOption(String name, String value) {
		Option option = new Option();
		option.setName(name);
		option.setValue(value);
		return option;
	}

	public static void getModifyHyperVCloudUserRequestDto(ICloudScope info,
			List<AddCloudLoginUserRequest> addRequestList, List<String> removeIdList, List<String> idList) {
		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> currentAllList = CloudTools.getCloudScopeList();
		com.clustercontrol.xcloud.model.cloud.ICloudScope selectedScope = null;
		for (com.clustercontrol.xcloud.model.cloud.ICloudScope iCloudScope:currentAllList){
			if (iCloudScope.getId().equals(info.getCloudScopeId()))
			selectedScope = iCloudScope;
		}
		
		List<ILoginUser> currentUsers = new ArrayList<>(Arrays.asList(selectedScope.getLoginUsers().getLoginUsers()));
		//メインユーザ削除
		currentUsers.remove(selectedScope.getLoginUsers().getLoginUser(selectedScope.getAccountId()));
		
		List<HypervUser> output = new ArrayList<>(Arrays.asList(info.getHyperv().getHypervUser()));
		int index = 0;
		//メインユーザ削除
		for (HypervUser vmwareUser : new ArrayList<>(output)){
			if (vmwareUser.getAccountId().equals(selectedScope.getAccountId())){
				output.remove(index);
				break;
			}
			index++;
		}
		
		for (HypervUser item: output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setCloudScopeId(info.getCloudScopeId());
			request.setUserName(item.getDisplayName());
			UserCredential credentail = new UserCredential();
			credentail.setUser(item.getUserName());
			credentail.setPassword(item.getPassword());
			request.setCredential(credentail);
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}

		for(ILoginUser user: currentUsers){
			removeIdList.add(user.getId());
		}
		
	}

	public static void getModifyAzureCloudUserRequestDto(ICloudScope info,
			List<AddCloudLoginUserRequest> addRequestList, List<String> removeIdList, List<String> idList) {
		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> currentAllList = CloudTools.getCloudScopeList();
		com.clustercontrol.xcloud.model.cloud.ICloudScope selectedScope = null;
		for (com.clustercontrol.xcloud.model.cloud.ICloudScope iCloudScope:currentAllList){
			if (iCloudScope.getId().equals(info.getCloudScopeId()))
			selectedScope = iCloudScope;
		}
		List<ILoginUser> currentUsers = new ArrayList<>(Arrays.asList(selectedScope.getLoginUsers().getLoginUsers()));
		currentUsers.remove(selectedScope.getLoginUsers().getLoginUser(selectedScope.getAccountId()));
		
		List<AzureUser> output = new ArrayList<>(Arrays.asList(info.getAzure().getAzureUser()));
		int index = 0;
		for (AzureUser amazonUser : new ArrayList<>(output)){
			if (amazonUser.getAccountId().equals(selectedScope.getAccountId())){
				output.remove(index);
				index--;
			}
			index++;
		}
		
		for (AzureUser item: output) {
			AddCloudLoginUserRequest request = new AddCloudLoginUserRequest();
			request.setLoginUserId(item.getAccountId());
			request.setCloudScopeId(info.getCloudScopeId());
			request.setUserName(item.getDisplayName());
			AccessKeyCredential credential = new AccessKeyCredential();
			credential.setAccessKey(item.getTenantId() + CloudConstant.azureKeySep +
					item.getApplicationId() + CloudConstant.azureKeySep +
					item.getSubscriptionId());
			credential.setSecretKey(item.getSecretKey());
			request.setCredential(credential);
			addRequestList.add(request);
			idList.add(item.getAccountId());
		}
		
		for(ILoginUser user: currentUsers){
			removeIdList.add(user.getId());
		}
	}
}