/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.util;

public class EmptyJpaTransactionCallback implements JpaTransactionCallback {

	@Override
	public void preBegin() {
	}

	@Override
	public void postBegin() {
	}

	@Override
	public void preFlush() {
	}

	@Override
	public void postFlush() {
	}

	@Override
	public void preCommit() {
	}

	@Override
	public void postCommit() {
	}

	@Override
	public void preRollback() {
	}

	@Override
	public void postRollback() {
	}

	@Override
	public void preClose() {
	}

	@Override
	public void postClose() {
	}
}
