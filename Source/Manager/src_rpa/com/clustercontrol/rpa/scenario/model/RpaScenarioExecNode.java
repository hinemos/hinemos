/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.model;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.session.RepositoryControllerBean;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * RPAシナリオの実行ノード定義
 */
@Entity
@Table(name="cc_rpa_scenario_node", schema="log")
public class RpaScenarioExecNode implements Serializable {
	private static final long serialVersionUID = 1L;

	@Embeddable
	public static class RpaScenarioExecNodePK implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public RpaScenarioExecNodePK() {
		}

		public RpaScenarioExecNodePK(String scenarioId, String facilityId, String scenarioIdentifyString) {
			this.scenarioId = scenarioId;
			this.facilityId = facilityId;
			this.scenarioIdentifyString = scenarioIdentifyString;
		}


		/** シナリオID */
		private String scenarioId;
		/** ファシリティID*/
		private String facilityId;
		/** シナリオ識別文字列 */
		private String scenarioIdentifyString;

		/** シナリオID */
		@Column(name="scenario_id")
		public String getScenarioId() {
			return scenarioId;
		}
		
		public void setScenarioId(String scenarioId) {
			this.scenarioId = scenarioId;
		}
		
		/** ファシリティID */
		@Column(name="facility_id")
		public String getFacilityId() {
			return facilityId;
		}
		public void setFacilityId(String facilityId) {
			this.facilityId = facilityId;
		}

		/** シナリオ識別文字列 */
		@Column(name="scenario_identify_string")
		public String getScenarioIdentifyString() {
			return scenarioIdentifyString;
		}
		public void setScenarioIdentifyString(String scenarioIdentifyString) {
			this.scenarioIdentifyString = scenarioIdentifyString;
		}
	}
	
	private RpaScenarioExecNodePK id;
	
	/** シナリオ */
	private RpaScenario rpaScenario;

	/** ファシリティ名*/
	private String facilityName;

	public RpaScenarioExecNode() {
	}

	public RpaScenarioExecNode(String scenarioId, String facilityId, String scenarioIdentifyString) {
		this.id = new RpaScenarioExecNodePK(scenarioId, facilityId, scenarioIdentifyString);
	}


	@EmbeddedId
	public RpaScenarioExecNodePK getId() {
		return id;
	}


	public void setId(RpaScenarioExecNodePK id) {
		this.id = id;
	}

	@Transient
	public String getScenarioId() {
		return id.getScenarioId();
	}
	
	public void setScenarioId(String scenarioId) {
		this.id.setScenarioId(scenarioId);
	}
	
	@Transient
	public String getScenarioIdentifyString() {
		return id.getScenarioIdentifyString();
	}
	
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.id.setScenarioIdentifyString(scenarioIdentifyString);
	}

	@Transient
	public String getFacilityId() {
		return id.getFacilityId();
	}
	
	public void setFacilityId(String facilityId) {
		this.id.setFacilityId(facilityId);
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="scenario_id", insertable=false, updatable=false)
	public RpaScenario getRpaScenario() {
		return rpaScenario;
	}

	@Deprecated
	public void setRpaScenario(RpaScenario rpaScenario) {
		this.rpaScenario = rpaScenario;
	}

	@Transient
	public String getFacilityName() {
		if (facilityName == null)
			try {
				facilityName = new RepositoryControllerBean().getNode(getFacilityId()).getNodeName();
			} catch (HinemosUnknown | FacilityNotFound e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return facilityName;
	}
}
