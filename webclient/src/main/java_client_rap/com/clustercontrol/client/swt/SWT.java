/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.client.swt;

/**
 * Single-sourcing implementation for SWT Class
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class SWT extends org.eclipse.swt.SWT {

	/**
	 * @see org.eclipse.swt.SWT#TRAVERSE_ARROW_PREVIOUS
	 */
	public static final int TRAVERSE_ARROW_PREVIOUS = 1 << 5;

	/**
	 * @see org.eclipse.swt.SWT#TRAVERSE_ARROW_NEXT
	 */
	public static final int TRAVERSE_ARROW_NEXT = 1 << 6;

	/**
	 * @see org.eclipse.swt.SWT#TRAVERSE_MNEMONIC
	 */
	public static final int TRAVERSE_MNEMONIC = 1 << 7;

	/**
	 * @see org.eclipse.swt.SWT#TRAVERSE_PAGE_PREVIOUS
	 */
	public static final int TRAVERSE_PAGE_PREVIOUS = 1 << 8;

	/**
	 * @see org.eclipse.swt.SWT#TRAVERSE_PAGE_NEXT
	 */
	public static final int TRAVERSE_PAGE_NEXT = 1 << 9;
	/**
	 * @see org.eclipse.swt.SWT#RIGHT_TO_LEFT
	 */
	public static final int RIGHT_TO_LEFT = 1 << 26;
}
