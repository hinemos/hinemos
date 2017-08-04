package com.clustercontrol.priority.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the cc_priority_info database table.
 * 
 */
@Entity
@Table(name="cc_priority_info", schema="setting")
@Cacheable(true)
public class PriorityJudgmentInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String judgmentId;
	private String description;
	private Integer pattern01;
	private Integer pattern02;
	private Integer pattern03;
	private Integer pattern04;
	private Integer pattern05;
	private Integer pattern06;
	private Integer pattern07;
	private Integer pattern08;
	private Integer pattern09;
	private Integer pattern10;
	private Integer pattern11;
	private Integer pattern12;
	private Integer pattern13;
	private Integer pattern14;
	private Integer pattern15;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;

	public PriorityJudgmentInfo() {
	}

	public PriorityJudgmentInfo(String judgmentId) {
		this.setJudgmentId(judgmentId);
	}


	@Id
	@Column(name="judgment_id")
	public String getJudgmentId() {
		return this.judgmentId;
	}

	public void setJudgmentId(String judgmentId) {
		this.judgmentId = judgmentId;
	}


	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="pattern_01")
	public Integer getPattern01() {
		return this.pattern01;
	}

	public void setPattern01(Integer pattern01) {
		this.pattern01 = pattern01;
	}


	@Column(name="pattern_02")
	public Integer getPattern02() {
		return this.pattern02;
	}

	public void setPattern02(Integer pattern02) {
		this.pattern02 = pattern02;
	}


	@Column(name="pattern_03")
	public Integer getPattern03() {
		return this.pattern03;
	}

	public void setPattern03(Integer pattern03) {
		this.pattern03 = pattern03;
	}


	@Column(name="pattern_04")
	public Integer getPattern04() {
		return this.pattern04;
	}

	public void setPattern04(Integer pattern04) {
		this.pattern04 = pattern04;
	}


	@Column(name="pattern_05")
	public Integer getPattern05() {
		return this.pattern05;
	}

	public void setPattern05(Integer pattern05) {
		this.pattern05 = pattern05;
	}


	@Column(name="pattern_06")
	public Integer getPattern06() {
		return this.pattern06;
	}

	public void setPattern06(Integer pattern06) {
		this.pattern06 = pattern06;
	}


	@Column(name="pattern_07")
	public Integer getPattern07() {
		return this.pattern07;
	}

	public void setPattern07(Integer pattern07) {
		this.pattern07 = pattern07;
	}


	@Column(name="pattern_08")
	public Integer getPattern08() {
		return this.pattern08;
	}

	public void setPattern08(Integer pattern08) {
		this.pattern08 = pattern08;
	}


	@Column(name="pattern_09")
	public Integer getPattern09() {
		return this.pattern09;
	}

	public void setPattern09(Integer pattern09) {
		this.pattern09 = pattern09;
	}


	@Column(name="pattern_10")
	public Integer getPattern10() {
		return this.pattern10;
	}

	public void setPattern10(Integer pattern10) {
		this.pattern10 = pattern10;
	}


	@Column(name="pattern_11")
	public Integer getPattern11() {
		return this.pattern11;
	}

	public void setPattern11(Integer pattern11) {
		this.pattern11 = pattern11;
	}


	@Column(name="pattern_12")
	public Integer getPattern12() {
		return this.pattern12;
	}

	public void setPattern12(Integer pattern12) {
		this.pattern12 = pattern12;
	}


	@Column(name="pattern_13")
	public Integer getPattern13() {
		return this.pattern13;
	}

	public void setPattern13(Integer pattern13) {
		this.pattern13 = pattern13;
	}


	@Column(name="pattern_14")
	public Integer getPattern14() {
		return this.pattern14;
	}

	public void setPattern14(Integer pattern14) {
		this.pattern14 = pattern14;
	}


	@Column(name="pattern_15")
	public Integer getPattern15() {
		return this.pattern15;
	}

	public void setPattern15(Integer pattern15) {
		this.pattern15 = pattern15;
	}


	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}


	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}


	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

}