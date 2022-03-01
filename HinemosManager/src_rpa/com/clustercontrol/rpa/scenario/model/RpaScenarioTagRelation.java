/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.scenario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;


/**
 * シナリオとシナリオタグの紐付け情報を格納するEntity定義
 * 
 */
@Entity
@Table(name="cc_rpa_scenario_tag_relation", schema="log")
public class RpaScenarioTagRelation implements Serializable {
	private static final long serialVersionUID = 1L;
	private RpaScenarioTagRelationPK id;

	public RpaScenarioTagRelation() {
	}

	@EmbeddedId
	public RpaScenarioTagRelationPK getId() {
		return this.id;
	}

	public void setId(RpaScenarioTagRelationPK id) {
		this.id = id;
	}
	
	@Embeddable
	public static class RpaScenarioTagRelationPK implements Serializable {
		//default serial version id, required for serializable classes.
		private static final long serialVersionUID = 1L;
		/** シナリオID */
		private String scenarioId;
		/** タグID */
		private String tagId;

		public RpaScenarioTagRelationPK() {
		}

		/** シナリオID */
		@Column(name="scenario_id")
		public String getScenarioId() {
			return this.scenarioId;
		}
		public void setScenarioId(String scenarioId) {
			this.scenarioId = scenarioId;
		}

		/** タグID */
		@Column(name="tag_id")
		public String getTagId() {
			return this.tagId;
		}
		public void setTagId(String tagId) {
			this.tagId = tagId;
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof RpaScenarioTagRelationPK)) {
				return false;
			}
			RpaScenarioTagRelationPK castOther = (RpaScenarioTagRelationPK)other;
			return 
				this.scenarioId.equals(castOther.scenarioId)
				&& this.tagId.equals(castOther.tagId);
		}

		public int hashCode() {
			final int prime = 31;
			int hash = 17;
			hash = hash * prime + this.scenarioId.hashCode();
			hash = hash * prime + this.tagId.hashCode();
			
			return hash;
		}
	}
}
