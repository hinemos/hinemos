/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.util.ArrayList;
import java.util.List;

/**
 * 指定された複数のプロパティ値から、一意性を実現するクラス。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class PropValues {
	public List<PropValue> props = new ArrayList<PropValue>();
	
	public PropValues() {
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PropValues) {
			PropValues propsId2 = (PropValues)anObject;
			return props.equals(propsId2.props);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < props.size(); i++) {
			h = 31*h + props.get(i).hashCode();
		}
		return h;
	}

	@Override
	public String toString() {
		StringBuffer oneLine = new StringBuffer();
		for (int j = 0; j < props.size(); ++j) {
			if (j != 0) {
				oneLine.append('-');
			}
			else {
				oneLine.append('"');
			}
			
			oneLine.append((props.get(j) != null ? CSVUtil.getString(props.get(j).toString()): ""));

			if (j == props.size() - 1) {
				oneLine.append('"');
			}
		}
		return oneLine.toString();
	}
}