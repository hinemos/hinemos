/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.swt.custom;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * LineStyleListener for RAP
 * 
 * @since 5.0.0
 */
@SuppressWarnings("restriction")
public interface LineStyleListener extends SWTEventListener {

	public void lineGetStyle(LineStyleEvent event);
}
