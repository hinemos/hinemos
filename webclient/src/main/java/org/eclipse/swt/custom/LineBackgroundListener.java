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
 * Classes which implement this interface provide a method
 * that can provide the background color for a line that
 * is to be drawn.
 *
 * @see LineBackgroundEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
@SuppressWarnings("restriction")
public interface LineBackgroundListener extends SWTEventListener {
	
/**
 * This method is called when a line is about to be drawn in order to get its
 * background color.
 * <p>
 * The following event fields are used:<ul>
 * <li>event.lineOffset line start offset (input)</li>
 * <li>event.lineText line text (input)</li>
 * <li>event.lineBackground line background color (output)</li>
 * </ul>
 *
 * @param event the given event
 * @see LineBackgroundEvent
 */
public void lineGetBackground(Object event);
}
