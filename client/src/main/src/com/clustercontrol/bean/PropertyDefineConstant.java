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

package com.clustercontrol.bean;

/**
 * ダイアログの処理の定数クラス<BR>
 * 
 * @version 2.2.0
 * @since 1.0.0
 */
public class PropertyDefineConstant {
	public static final String EDITOR_SELECT = com.clustercontrol.editor.ComboPropertyDefine.class.getName();
	public static final String EDITOR_PASSWORD = com.clustercontrol.editor.PasswordPropertyDefine.class.getName();
	public static final String EDITOR_FACILITY = com.clustercontrol.editor.ScopePropertyDefine.class.getName();
	public static final String EDITOR_NODE = com.clustercontrol.editor.NodePropertyDefine.class.getName();
	public static final String EDITOR_TEXT = com.clustercontrol.editor.TextPropertyDefine.class.getName();
	public static final String EDITOR_TEXTAREA = com.clustercontrol.editor.TextAreaPropertyDefine.class.getName();
	public static final String EDITOR_BOOL = com.clustercontrol.editor.BooleanPropertyDefine.class.getName();
	public static final String EDITOR_DATETIME = com.clustercontrol.editor.DateTimePropertyDefine.class.getName();
	public static final String EDITOR_TIME = com.clustercontrol.editor.TimePropertyDefine.class.getName();
	public static final String EDITOR_NUM = com.clustercontrol.editor.IntegerPropertyDefine.class.getName();
	public static final String EDITOR_JOB = com.clustercontrol.jobmanagement.editor.JobPropertyDefine.class.getName();
	public static final String EDITOR_IPV4 = com.clustercontrol.editor.IPv4PropertyDefine.class.getName();
	public static final String EDITOR_IPV6 = com.clustercontrol.editor.IPv6PropertyDefine.class.getName();

	public static final int MODIFY_NG = 0;
	public static final int MODIFY_OK = 1;
	public static final int MODE_ADD = 0;
	public static final int MODE_MODIFY = 1;
	public static final int MODE_SHOW = 2;
	public static final int MODE_COPY = 3;
	public static final int COPY_OK = 0;
	public static final int COPY_NG = 1;
	public static final int SELECT_DISP_TEXT = 0;
	public static final int SELECT_VALUE = 1;

	public static final String MAP_VALUE = "value";
	public static final String MAP_PROPERTY = "property";
}
