package com.jobsearch.skilljob.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {
	@Column(nullable = false, length = 150)
	private String name;
	private String website;
	@Column(length = 1000)
	private String description;
	private String location;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private AppUser owner;

	protected Company() {
	}

	public Company(String name, AppUser owner) {
		this.name = name;
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String v) {
		name = v;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String v) {
		website = v;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String v) {
		description = v;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String v) {
		location = v;
	}

	public AppUser getOwner() {
		return owner;
	}
}
