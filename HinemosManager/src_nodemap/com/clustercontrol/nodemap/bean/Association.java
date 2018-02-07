/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * コネクション
 * @since 1.0.0
 */
@XmlType(namespace = "http://nodemap.ws.clustercontrol.com")
public class Association implements Serializable {
	private static final long serialVersionUID = 6019280510133385638L;

	private String source;
	private String target;

	private Integer type;

	public Association(){}

	public Association(String source, String target){
		this(source, target, AssociationConstant.NORMAL);
	}

	public Association(String source, String target, Integer type) {
		this.source = source;
		this.target = target;
		this.type = type;
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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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
