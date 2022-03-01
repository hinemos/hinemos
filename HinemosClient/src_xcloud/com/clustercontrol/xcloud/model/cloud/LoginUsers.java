/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.CloudLoginUserInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class LoginUsers extends Element implements ILoginUsers {
	private CloudScope cloudScope;
	private List<LoginUser> loginUsers;

	public LoginUsers(CloudScope cloudScope){
		this.cloudScope = cloudScope;
	}

	@Override
	public LoginUser getLoginUser(String loginUserId) {
		for(LoginUser cloudUser: getLoginUsers()){
			if(cloudUser.getId().equals(loginUserId)){
				return cloudUser;
			}
		}
		return null;
	}
	
	@Override
	public LoginUser[] getLoginUsers() {
		List<LoginUser> users = getLoginUserList();
		return users.toArray(new LoginUser[users.size()]);
	}

	protected List<LoginUser> getLoginUserList() {
		if (loginUsers == null)
			update();
		return loginUsers;
	}

	@Override
	public CloudScope getCloudScope() {
		return cloudScope;
	}
	
	private CloudRestClientWrapper getWrapper(){
		return getCloudScope().getCloudScopes().getHinemosManager().getWrapper();
	}

	@Override
	public void update() {
		List<CloudLoginUserInfoResponse> users;
		try {
			users = getWrapper().getAllCloudLoginUsers(getCloudScope().getId());
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | InvalidSetting | RestConnectFailed | HinemosUnknown e) {
			throw new CloudModelException(e);
		}
		
		update(users);
	}

	public void update(List<CloudLoginUserInfoResponse> users) {
		if (loginUsers == null)
			loginUsers = new ArrayList<>();
		
		CollectionComparator.compareCollection(loginUsers, users, new CollectionComparator.Comparator<LoginUser, CloudLoginUserInfoResponse>() {
			@Override public boolean match(LoginUser o1, CloudLoginUserInfoResponse o2) {return o1.getId().equals(o2.getEntity().getLoginUserId());}
			@Override public void matched(LoginUser o1, CloudLoginUserInfoResponse o2) {o1.overwrite(o2);}
			@Override public void afterO1(LoginUser o1) {loginUsers.remove(o1); firePropertyRemoved(p.loginUsers, o1);}
			@Override public void afterO2(CloudLoginUserInfoResponse o2) {
				LoginUser u = new LoginUser(LoginUsers.this);
				u.overwrite(o2);
				loginUsers.add(u);
				firePropertyAdded(p.loginUsers, u);
			}
		});
	}
}
