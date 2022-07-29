/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.repository.IRepositoryListener;

public class RepositoryListenerCallback implements JpaTransactionCallback {
	
	public static final Log _log = LogFactory.getLog( RepositoryListenerCallback.class );
	
	private final IRepositoryListener _listener;
	
	public final Type _type;
	public final String _scopeFacilityId;
	public final String _nodeFacilityId;
	
	public enum Type { ADD_NODE, CHANGE_NODE, REMOVE_NODE, ADD_SCOPE, CHANGE_SCOPE, REMOVE_SCOPE, 
		ASSIGN_NODE_TO_SCOPE, RELEASE_NODE_FROM_SCOPE };
	
	public RepositoryListenerCallback(IRepositoryListener listener, Type type, String scopeFacilityId, String nodeFacilityId) {
		this._listener = listener;
		this._type = type;
		this._scopeFacilityId = scopeFacilityId;
		this._nodeFacilityId = nodeFacilityId;
	}

	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() { }

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() {
		try {
			switch (_type) {
			case ADD_NODE :
				_listener.postAddingNode(_nodeFacilityId);
				break;
			case CHANGE_NODE :
				_listener.postChangingNode(_nodeFacilityId);
				break;
			case REMOVE_NODE :
				_listener.postRemovingNode(_nodeFacilityId);
				break;
			case ADD_SCOPE :
				_listener.postAddingScope(_scopeFacilityId);
				break;
			case CHANGE_SCOPE :
				_listener.postChangingScope(_scopeFacilityId);
				break;
			case REMOVE_SCOPE :
				_listener.postRemovingScope(_scopeFacilityId);
				break;
			case ASSIGN_NODE_TO_SCOPE :
				_listener.postAssigningNodeToScope(_scopeFacilityId, _nodeFacilityId);
				break;
			case RELEASE_NODE_FROM_SCOPE :
				_listener.postReleasingNodeFromScope(_scopeFacilityId, _nodeFacilityId);
				break;
			}
		} catch (Throwable t) {
			_log.warn("listener execution failure.", t);
		}
	}
	
	@Override
	public int hashCode() {
		int h = 1;
		h = h * 31 + (_listener == null ? 0 : _listener.hashCode());
		h = h * 31 + (_type == null ? 0 : _type.hashCode());
		h = h * 31 + (_scopeFacilityId == null ? 0 : _scopeFacilityId.hashCode());
		h = h * 31 + (_nodeFacilityId == null ? 0 : _nodeFacilityId.hashCode());
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof RepositoryListenerCallback) {
			RepositoryListenerCallback cast = (RepositoryListenerCallback)obj;
			if (_listener != null && _listener.equals(cast._listener)
					&& _type != null && _type == cast._type
					&& _scopeFacilityId != null && _scopeFacilityId.equals(cast._scopeFacilityId)
					&& _nodeFacilityId != null && _nodeFacilityId.equals(cast._nodeFacilityId)) {
				return true;
			}
		}
		return false;
	}
	
}
