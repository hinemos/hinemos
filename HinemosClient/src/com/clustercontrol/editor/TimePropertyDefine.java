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

package com.clustercontrol.editor;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TextCellEditor;

import com.clustercontrol.bean.Property;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.TimezoneUtil;

/**
 * 時刻プロパティ定義クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class TimePropertyDefine extends PropertyDefine implements Serializable {
	private static final long serialVersionUID = 4970684702759585048L;

	/**
	 * コンストラクタ
	 */
	public TimePropertyDefine() {
		m_cellEditor = new TextCellEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		if (value instanceof Date) {
			Date dateValue = (Date) value;
			//表示形式を0時未満および24時(及び48時)超にも対応する
			return TimeStringConverter.formatTime(dateValue);
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getValue(com.clustercontrol.bean.Property)
	 */
	@Override
	public Object getValue(Property element) {
		Object value = element.getValue();
		if (value instanceof Date) {
			Date dateValue = (Date) value;
			//表示形式を0時未満および24時(及び48時)超にも対応する
			return TimeStringConverter.formatTime(dateValue);
		}
		return "";
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
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		formatter.setTimeZone(TimezoneUtil.getTimeZone());
		Date date = null;
		try {
			//0時未満および24時(及び48時)超の文字列指定にも対応する
			date = TimeStringConverter.parseTime((String) value);
			check = true;
		} catch (ParseException e) {
//カレンダと同様の変換処理とするため、追加変換は行わない
//			formatter = new SimpleDateFormat("HH:mm:ss");
//			formatter.setTimeZone(TimezoneUtil.getTimeZone());
//			try {
//				date = formatter.parse((String) value);
//				check = true;
//			} catch (ParseException e1) {
//			}
		}

		if(check){
			element.setValue(date);
		}
		else{
			//エラーメッセージ
			MessageDialog.openWarning(
					null,
					Messages.getString("message.hinemos.1"),
					Messages.getString("message.hinemos.6"));
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
