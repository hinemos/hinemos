/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;


public class FolderScope extends Scope implements IFolderScope {
	private String folderType;
	
	@Override
	public String getFolderType() {
		return folderType;
	}
	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}

	public static FolderScope convert(com.clustercontrol.ws.xcloud.HFolder source) {
		FolderScope scope = new FolderScope();
		scope.update(source);
		return scope;
	}
	
	protected void update(com.clustercontrol.ws.xcloud.HFolder folder) {
		super.update(folder);
		setFolderType(folder.getFolderType());
	}

	@Override
	public void visit(IVisitor visitor) {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformor) {
		return transformor.transform(this);
	}
}
