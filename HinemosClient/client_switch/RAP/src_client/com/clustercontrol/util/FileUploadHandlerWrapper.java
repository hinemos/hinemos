/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;

import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadReceiver;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;


public class FileUploadHandlerWrapper extends FileUploadHandler {

	private UISessionListener listener;
	public FileUploadHandlerWrapper(FileUploadReceiver receiver) {
		super(receiver);
		this.listener = new UISessionListener() {
			public void beforeDestroy(UISessionEvent event) {
				dispose();
			}
		};
		ContextProvider.getUISession().addUISessionListener(listener);
	}
	
	/**
	 * キャッシュされたFileUploadHandlerのインスタンスや追加したリスナーを削除する
	 */
	@Override
	public void dispose() {
		super.dispose();
		ContextProvider.getUISession().removeUISessionListener(listener);
	}
}
