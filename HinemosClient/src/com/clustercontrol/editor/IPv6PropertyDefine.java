/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.editor;

import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TextCellEditor;

import com.clustercontrol.bean.Property;
import com.clustercontrol.util.Messages;

/**
 * IPv6プロパティを定義するクラス<BR>
 * 
 * @version 2.2.0
 * @since 2.2.0
 */
public class IPv6PropertyDefine extends PropertyDefine implements Serializable {
	private static final long serialVersionUID = 3962737174668125479L;

	/**
	 * コンストラクタ
	 */
	public IPv6PropertyDefine() {
		m_cellEditor = new TextCellEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		return value.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getValue(com.clustercontrol.bean.Property)
	 */
	@Override
	public Object getValue(Property element) {
		return element.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#modify(com.clustercontrol.bean.Property,
	 *      java.lang.Object)
	 */
	@Override
	public void modify(Property element, Object value) {
		boolean check = false;
		try {
			if(((String)value).length() == 0){
				check = true;
			}

			// 指定のアドレスがIPv4かもしくは、IPv6のアドレスとして問題ないかをチェックする
			// IPv6のアドレスと認識された場合は、Inet6Addressが返る。
			InetAddress address = Inet6Address.getByName((String)value);

			// IPv6アドレスと判定された場合のみ値をセットする
			if(address instanceof Inet6Address){
				check = true;
			}
		} catch (UnknownHostException e) {
		}

		if(check){
			element.setValue(value);
		}
		else {
			//エラーメッセージ
			MessageDialog.openWarning(
					null,
					Messages.getString("message.hinemos.1"),
					Messages.getString("message.repository.25"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#initEditer()
	 */
	@Override
	public void initEditer() {

	}
}
