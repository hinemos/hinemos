/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth.ldap;

/**
 * LDAPへのアクセスにおいて問題が発生したことを表します。
 */
public class LdapAccessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public LdapAccessException() {
		super();
	}

	public LdapAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public LdapAccessException(String message) {
		super(message);
	}

	public LdapAccessException(Throwable cause) {
		super(cause);
	}
}
