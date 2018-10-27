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

import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudLoginUser;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
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
	
	private CloudEndpoint getEndpoint(){
		return getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
	}

	@Override
	public void update() {
		List<CloudLoginUser> users;
		try {
			users = getEndpoint().getAllCloudLoginUsers(getCloudScope().getId());
		} catch (InvalidRole_Exception | InvalidUserPass_Exception | CloudManagerException e) {
			throw new CloudModelException(e);
		}
		
		update(users);
	}

	public void update(List<CloudLoginUser> users) {
		if (loginUsers == null)
			loginUsers = new ArrayList<>();
		
		CollectionComparator.compareCollection(loginUsers, users, new CollectionComparator.Comparator<LoginUser, CloudLoginUser>() {
			@Override public boolean match(LoginUser o1, CloudLoginUser o2) {return o1.getId().equals(o2.getId());}
			@Override public void matched(LoginUser o1, CloudLoginUser o2) {o1.overwrite(o2);}
			@Override public void afterO1(LoginUser o1) {loginUsers.remove(o1); firePropertyRemoved(p.loginUsers, o1);}
			@Override public void afterO2(CloudLoginUser o2) {
				LoginUser u = new LoginUser(LoginUsers.this);
				u.overwrite(o2);
				loginUsers.add(u);
				firePropertyAdded(p.loginUsers, u);
			}
		});
	}
}
