/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.swt.events;


import org.eclipse.swt.internal.SWTEventListener;

/**
 * Classes which implement this interface provide a method
 * that deals with the event that is generated when help is
 * requested for a control, typically when the user presses F1.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a control using the
 * <code>addHelpListener</code> method and removed using
 * the <code>removeHelpListener</code> method. When help
 * is requested for a control, the helpRequested method
 * will be invoked.
 * </p>
 *
 * @see HelpEvent
 * @since 1.3
 */
@SuppressWarnings("restriction")
public interface HelpListener extends SWTEventListener {

/**
 * Sent when help is requested for a control, typically
 * when the user presses F1.
 *
 * @param e an event containing information about the help
 */
public void helpRequested(HelpEvent e);
}
