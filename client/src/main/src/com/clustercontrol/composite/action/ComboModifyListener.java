/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.composite.action;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;

import com.clustercontrol.util.WidgetTestUtil;

/**
 * コンボボックス用ModifyListenerクラス<BR>
 *
 * 入力された文字列がコンボボックスに用意されていない場合、クリアする
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ComboModifyListener implements ModifyListener {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		Combo combo = (Combo) e.getSource();
		WidgetTestUtil.setTestId(this, null, combo);
		String modify = combo.getText();
		if (modify.length() == combo.getTextLimit()) {
			int index = 0;
			for (index = 0; index < combo.getItemCount(); index++) {
				if (modify.equals(combo.getItem(index))) {
					break;
				}
			}
			if (index >= combo.getItemCount()) {
				combo.select(0);
			}
		}
	}
}
