/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

/**
 * 数値用KeyListenerクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class NumberKeyListener implements KeyListener {
	private final static String NUM_CHARS = "0123456789-";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		//キー入力以外は有効にする
		if (e.keyCode == 0) {
			return;
		}
		//BackspaceやDeleteが押されたときは、有効にする
		if (e.character == SWT.BS || e.character == SWT.DEL) {
			return;
		}
		//数字と'-'以外は無効にする
		if (NUM_CHARS.indexOf(Character.toString(e.character)) == -1) {
			e.doit = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e) {

	}
}
