/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * コネクション
 * @since 1.0.0
 */
@XmlType(namespace = "http://nodemap.ws.clustercontrol.com")
public class Association implements Serializable {
	private static final long serialVersionUID = 6019280510133385638L;

	public static String LINE_TYPE_NORMAL = "normal";

	private String source;
	private String target;

	public Association(String source, String target){
		this.source = source;
		this.target = target;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public boolean equals(Object obj){
		if (obj != null && obj instanceof Association) {
			Association other = (Association)obj;

			boolean srcEquals = false;
			boolean tgtEquals = false;

			if((other.getSource() == null || this.getSource() == null)){
				return false;
			}

			if((other.getTarget() == null || this.getTarget() == null)){
				return false;
			}

			if((other.getSource() == null && this.getSource() == null) ||
					(other.getSource() != null &&
							other.getSource().equals(this.getSource()))){
				srcEquals = true;
			}

			if((other.getTarget() == null && this.getTarget() == null) ||
					(other.getTarget() != null &&
							other.getTarget().equals(this.getTarget()))){
				tgtEquals = true;
			}

			return (srcEquals && tgtEquals);
		}

		return false;
	}

	@Override
	public int hashCode(){
		return source.hashCode() + target.hashCode();
	}
}
