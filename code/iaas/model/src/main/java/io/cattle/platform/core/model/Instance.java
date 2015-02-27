/**
 * This class is generated by jOOQ
 */
package io.cattle.platform.core.model;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
@javax.persistence.Entity
@javax.persistence.Table(name = "instance", schema = "cattle")
public interface Instance extends java.io.Serializable {

	/**
	 * Setter for <code>cattle.instance.id</code>.
	 */
	public void setId(java.lang.Long value);

	/**
	 * Getter for <code>cattle.instance.id</code>.
	 */
	@javax.persistence.Id
	@javax.persistence.Column(name = "id", unique = true, nullable = false, precision = 19)
	public java.lang.Long getId();

	/**
	 * Setter for <code>cattle.instance.name</code>.
	 */
	public void setName(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.name</code>.
	 */
	@javax.persistence.Column(name = "name", length = 255)
	public java.lang.String getName();

	/**
	 * Setter for <code>cattle.instance.account_id</code>.
	 */
	public void setAccountId(java.lang.Long value);

	/**
	 * Getter for <code>cattle.instance.account_id</code>.
	 */
	@javax.persistence.Column(name = "account_id", precision = 19)
	public java.lang.Long getAccountId();

	/**
	 * Setter for <code>cattle.instance.kind</code>.
	 */
	public void setKind(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.kind</code>.
	 */
	@javax.persistence.Column(name = "kind", nullable = false, length = 255)
	public java.lang.String getKind();

	/**
	 * Setter for <code>cattle.instance.uuid</code>.
	 */
	public void setUuid(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.uuid</code>.
	 */
	@javax.persistence.Column(name = "uuid", unique = true, nullable = false, length = 128)
	public java.lang.String getUuid();

	/**
	 * Setter for <code>cattle.instance.description</code>.
	 */
	public void setDescription(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.description</code>.
	 */
	@javax.persistence.Column(name = "description", length = 1024)
	public java.lang.String getDescription();

	/**
	 * Setter for <code>cattle.instance.state</code>.
	 */
	public void setState(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.state</code>.
	 */
	@javax.persistence.Column(name = "state", nullable = false, length = 128)
	public java.lang.String getState();

	/**
	 * Setter for <code>cattle.instance.created</code>.
	 */
	public void setCreated(java.util.Date value);

	/**
	 * Getter for <code>cattle.instance.created</code>.
	 */
	@javax.persistence.Column(name = "created")
	public java.util.Date getCreated();

	/**
	 * Setter for <code>cattle.instance.removed</code>.
	 */
	public void setRemoved(java.util.Date value);

	/**
	 * Getter for <code>cattle.instance.removed</code>.
	 */
	@javax.persistence.Column(name = "removed")
	public java.util.Date getRemoved();

	/**
	 * Setter for <code>cattle.instance.remove_time</code>.
	 */
	public void setRemoveTime(java.util.Date value);

	/**
	 * Getter for <code>cattle.instance.remove_time</code>.
	 */
	@javax.persistence.Column(name = "remove_time")
	public java.util.Date getRemoveTime();

	/**
	 * Setter for <code>cattle.instance.data</code>.
	 */
	public void setData(java.util.Map<String,Object> value);

	/**
	 * Getter for <code>cattle.instance.data</code>.
	 */
	@javax.persistence.Column(name = "data", length = 65535)
	public java.util.Map<String,Object> getData();

	/**
	 * Setter for <code>cattle.instance.allocation_state</code>.
	 */
	public void setAllocationState(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.allocation_state</code>.
	 */
	@javax.persistence.Column(name = "allocation_state", length = 255)
	public java.lang.String getAllocationState();

	/**
	 * Setter for <code>cattle.instance.compute</code>.
	 */
	public void setCompute(java.lang.Long value);

	/**
	 * Getter for <code>cattle.instance.compute</code>.
	 */
	@javax.persistence.Column(name = "compute", precision = 19)
	public java.lang.Long getCompute();

	/**
	 * Setter for <code>cattle.instance.memory_mb</code>.
	 */
	public void setMemoryMb(java.lang.Long value);

	/**
	 * Getter for <code>cattle.instance.memory_mb</code>.
	 */
	@javax.persistence.Column(name = "memory_mb", precision = 19)
	public java.lang.Long getMemoryMb();

	/**
	 * Setter for <code>cattle.instance.image_id</code>.
	 */
	public void setImageId(java.lang.Long value);

	/**
	 * Getter for <code>cattle.instance.image_id</code>.
	 */
	@javax.persistence.Column(name = "image_id", precision = 19)
	public java.lang.Long getImageId();

	/**
	 * Setter for <code>cattle.instance.offering_id</code>.
	 */
	public void setOfferingId(java.lang.Long value);

	/**
	 * Getter for <code>cattle.instance.offering_id</code>.
	 */
	@javax.persistence.Column(name = "offering_id", precision = 19)
	public java.lang.Long getOfferingId();

	/**
	 * Setter for <code>cattle.instance.hostname</code>.
	 */
	public void setHostname(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.hostname</code>.
	 */
	@javax.persistence.Column(name = "hostname", length = 255)
	public java.lang.String getHostname();

	/**
	 * Setter for <code>cattle.instance.zone_id</code>.
	 */
	public void setZoneId(java.lang.Long value);

	/**
	 * Getter for <code>cattle.instance.zone_id</code>.
	 */
	@javax.persistence.Column(name = "zone_id", precision = 19)
	public java.lang.Long getZoneId();

	/**
	 * Setter for <code>cattle.instance.instance_triggered_stop</code>.
	 */
	public void setInstanceTriggeredStop(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.instance_triggered_stop</code>.
	 */
	@javax.persistence.Column(name = "instance_triggered_stop", length = 128)
	public java.lang.String getInstanceTriggeredStop();

	/**
	 * Setter for <code>cattle.instance.agent_id</code>.
	 */
	public void setAgentId(java.lang.Long value);

	/**
	 * Getter for <code>cattle.instance.agent_id</code>.
	 */
	@javax.persistence.Column(name = "agent_id", precision = 19)
	public java.lang.Long getAgentId();

	/**
	 * Setter for <code>cattle.instance.domain</code>.
	 */
	public void setDomain(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.domain</code>.
	 */
	@javax.persistence.Column(name = "domain", length = 128)
	public java.lang.String getDomain();

	/**
	 * Setter for <code>cattle.instance.first_running</code>.
	 */
	public void setFirstRunning(java.util.Date value);

	/**
	 * Getter for <code>cattle.instance.first_running</code>.
	 */
	@javax.persistence.Column(name = "first_running")
	public java.util.Date getFirstRunning();

	/**
	 * Setter for <code>cattle.instance.token</code>.
	 */
	public void setToken(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.token</code>.
	 */
	@javax.persistence.Column(name = "token", length = 255)
	public java.lang.String getToken();

	/**
	 * Setter for <code>cattle.instance.userdata</code>.
	 */
	public void setUserdata(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.userdata</code>.
	 */
	@javax.persistence.Column(name = "userdata", length = 65535)
	public java.lang.String getUserdata();

	/**
	 * Setter for <code>cattle.instance.system_container</code>.
	 */
	public void setSystemContainer(java.lang.String value);

	/**
	 * Getter for <code>cattle.instance.system_container</code>.
	 */
	@javax.persistence.Column(name = "system_container", length = 128)
	public java.lang.String getSystemContainer();

	// -------------------------------------------------------------------------
	// FROM and INTO
	// -------------------------------------------------------------------------

	/**
	 * Load data from another generated Record/POJO implementing the common interface Instance
	 */
	public void from(io.cattle.platform.core.model.Instance from);

	/**
	 * Copy data into another generated Record/POJO implementing the common interface Instance
	 */
	public <E extends io.cattle.platform.core.model.Instance> E into(E into);
}
