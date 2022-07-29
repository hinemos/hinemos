/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.dialog;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;

public class ChangeBackgroundModifyListener implements ModifyListener {

	@Override
	public void modifyText(ModifyEvent e) {
		if (!(e.widget instanceof Text)) {
			return;
		}

		Text textControl = (Text)e.widget;
		if("".equals(textControl.getText())){
			textControl.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			textControl.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
}
