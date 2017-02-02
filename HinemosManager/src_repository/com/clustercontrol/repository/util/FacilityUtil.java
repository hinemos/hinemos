/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository.util;

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.model.FacilityInfo;

/**
 * リポジトリに関するUtilityクラス<br/>
 *
 *
 */
public class FacilityUtil {

	/**
	 * FacilityEntityのNode判定
	 */
	public static boolean isNode(FacilityInfo entity) {
		if (entity.getFacilityType() != null && entity.getFacilityType() == FacilityConstant.TYPE_NODE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityInfoのNode判定
	 */
	public static boolean isNode_FacilityInfo(FacilityInfo info) {
		if (info.getFacilityType() != null && info.getFacilityType() == FacilityConstant.TYPE_NODE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityEntityのScope判定
	 */
	public static boolean isScope(FacilityInfo entity) {
		if (entity.getFacilityType() != null && entity.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityInfoのScope判定
	 */
	public static boolean isScope_FacilityInfo(FacilityInfo info) {
		if (info.getFacilityType() != null && info.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityEntityのValid判定
	 */
	public static boolean isValid(FacilityInfo entity) {
		if (entity.getValid() != null && entity.getValid()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * FacilityInfoのValid判定
	 */
	public static boolean isValid_FacilityInfo(FacilityInfo info) {
		if (info.getValid() != null && info.getValid()) {
			return true;
		} else {
			return false;
		}
	}
}
