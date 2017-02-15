/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.bean;

import javax.xml.bind.annotation.XmlType;
/**
 * 定義する値は取得する値の種別
 * 
 * - NUMBER	数値
 * - STRING　文字列
 *   
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public enum Type {
    NUMBER,STRING
}
