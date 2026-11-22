package com.jobsearch.skilljob.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skills", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Skill extends BaseEntity {
	@Column(nullable = false, length = 80)
	private String name;

	protected Skill() {
	}

	public Skill(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = n;
	}
}
