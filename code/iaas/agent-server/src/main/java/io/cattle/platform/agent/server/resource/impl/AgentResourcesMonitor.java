package io.cattle.platform.agent.server.resource.impl;

import static io.cattle.platform.core.model.tables.HostTable.*;
import static io.cattle.platform.core.model.tables.PhysicalHostTable.*;
import io.cattle.platform.agent.server.ping.dao.PingDao;
import io.cattle.platform.archaius.util.ArchaiusUtil;
import io.cattle.platform.core.constants.AgentConstants;
import io.cattle.platform.core.constants.HostConstants;
import io.cattle.platform.core.constants.IpAddressConstants;
import io.cattle.platform.core.constants.StoragePoolConstants;
import io.cattle.platform.core.dao.AgentDao;
import io.cattle.platform.core.dao.GenericResourceDao;
import io.cattle.platform.core.dao.IpAddressDao;
import io.cattle.platform.core.dao.StoragePoolDao;
import io.cattle.platform.core.model.Agent;
import io.cattle.platform.core.model.Host;
import io.cattle.platform.core.model.IpAddress;
import io.cattle.platform.core.model.PhysicalHost;
import io.cattle.platform.core.model.StoragePool;
import io.cattle.platform.eventing.EventService;
import io.cattle.platform.eventing.annotation.AnnotatedEventListener;
import io.cattle.platform.eventing.annotation.EventHandler;
import io.cattle.platform.eventing.model.Event;
import io.cattle.platform.eventing.model.EventVO;
import io.cattle.platform.framework.event.FrameworkEvents;
import io.cattle.platform.framework.event.Ping;
import io.cattle.platform.lock.LockCallbackNoReturn;
import io.cattle.platform.lock.LockManager;
import io.cattle.platform.object.ObjectManager;
import io.cattle.platform.object.meta.ObjectMetaDataManager;
import io.cattle.platform.object.util.DataAccessor;
import io.cattle.platform.util.type.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.exception.DataChangedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.netflix.config.DynamicLongProperty;

public class AgentResourcesMonitor implements AnnotatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(AgentResourcesMonitor.class);
    private static final DynamicLongProperty CACHE_RESOURCE = ArchaiusUtil.getLong("agent.resource.monitor.cache.resource.seconds");

    private static final String[] UPDATABLE_HOST_FIELDS = new String[] {
            HostConstants.FIELD_API_PROXY,
            HostConstants.FIELD_HOSTNAME,
            HostConstants.FIELD_INFO,
            HostConstants.FIELD_LABELS };
    private static final Set<String> ORCHESTRATE_FIELDS = new HashSet<>(Arrays.asList(HostConstants.FIELD_LABELS));

    @Inject
    PingDao pingDao;
    @Inject
    AgentDao agentDao;
    @Inject
    GenericResourceDao resourceDao;
    @Inject
    StoragePoolDao storagePoolDao;
    @Inject
    IpAddressDao ipAddressDao;
    @Inject
    ObjectManager objectManager;
    @Inject
    LockManager lockManager;
    @Inject
    EventService eventService;
    Cache<String, Boolean> resourceCache;

    public AgentResourcesMonitor() {
        super();
        buildCache();
        CACHE_RESOURCE.addCallback(new Runnable() {
            @Override
            public void run() {
                buildCache();
            }
        });
    }

    protected void buildCache() {
        resourceCache = CacheBuilder.newBuilder().expireAfterWrite(CACHE_RESOURCE.get(), TimeUnit.SECONDS).build();
    }

    @EventHandler
    public void pingReply(Ping ping) {
        processPingReply(ping);
    }

    public void processPingReply(Ping ping) {
        String agentIdStr = ping.getResourceId();
        if (agentIdStr == null) {
            return;
        }

        long agentId = Long.parseLong(agentIdStr);
        if (ping.getData() == null) {
            return;
        }

        final AgentResources resources = processResources(ping);
        if (!resources.hasContent()) {
            return;
        }

        Boolean done = resourceCache.getIfPresent(resources.getHash());

        if (done != null && done.booleanValue()) {
            return;
        }

        final Agent agent = objectManager.loadResource(Agent.class, agentId);
        lockManager.lock(new AgentResourceCreateLock(agent), new LockCallbackNoReturn() {
            @Override
            public void doWithLockNoResult() {
                Boolean done = resourceCache.getIfPresent(resources.getHash());

                if (done != null && done.booleanValue()) {
                    return;
                }

                Map<String, Host> hosts = setHosts(agent, resources);
                setStoragePools(hosts, agent, resources);
                setIpAddresses(hosts, agent, resources);

                resourceCache.put(resources.getHash(), true);
            }
        });
    }

    protected Map<String, StoragePool> setStoragePools(Map<String, Host> hosts, Agent agent, AgentResources resources) {
        Map<String, StoragePool> pools = agentDao.getStoragePools(agent.getId());

        for (Map.Entry<String, Map<String, Object>> poolData : resources.getStoragePools().entrySet()) {
            String uuid = poolData.getKey();
            Map<String, Object> data = poolData.getValue();

            if (pools.containsKey(uuid)) {
                continue;
            }

            Host host = hosts.get(ObjectUtils.toString(data.get(HostConstants.FIELD_HOST_UUID), null));

            if (host == null) {
                continue;
            }

            data = createData(agent, uuid, data);
            pools.put(uuid, storagePoolDao.mapNewPool(host, data));
        }

        return pools;
    }

    protected void setIpAddresses(Map<String, Host> hosts, Agent agent, AgentResources resources) {
        for (Map.Entry<String, Map<String, Object>> ipData : resources.getIpAddresses().entrySet()) {
            String address = ipData.getKey();
            Map<String, Object> data = ipData.getValue();
            Host host = hosts.get(ObjectUtils.toString(data.get(HostConstants.FIELD_HOST_UUID), null));

            if (host == null) {
                continue;
            }

            List<IpAddress> ips = objectManager.mappedChildren(host, IpAddress.class);
            if (ips.size() == 0) {
                ipAddressDao.assignAndActivateNewAddress(host, address);
            } else {
                IpAddress ip = ips.get(0);
                if (!address.equalsIgnoreCase(ip.getAddress())) {
                    ipAddressDao.updateIpAddress(ip, address);
                }
                String currentIp = DataAccessor.fieldString(host, HostConstants.FIELD_IP_ADDRESS);
                if (!ObjectUtils.equals(currentIp, ip.getAddress())) {
                    try {
                        objectManager.setFields(host, HostConstants.FIELD_IP_ADDRESS, ip.getAddress());
                    } catch (DataChangedException e) {
                    }
                }
            }
        }
    }

    protected Map<String, Host> setHosts(Agent agent, AgentResources resources) {
        Map<String, Host> hosts = agentDao.getHosts(agent.getId());

        for (Map.Entry<String, Map<String, Object>> hostData : resources.getHosts().entrySet()) {
            String uuid = hostData.getKey();
            Map<String, Object> data = hostData.getValue();
            String physicalHostUuid = ObjectUtils.toString(data.get(HostConstants.FIELD_PHYSICAL_HOST_UUID), null);
            Long physicalHostId = getPhysicalHost(agent, physicalHostUuid, new HashMap<String, Object>());
            boolean orchestrate = false;

            if (!hosts.containsKey(uuid) && physicalHostId != null) {
                Host host = objectManager.findAny(Host.class,
                        HOST.PHYSICAL_HOST_ID, physicalHostId,
                        HOST.REMOVED, null);
                if (host != null) {
                    String reported = DataAccessor.fieldString(host, HostConstants.FIELD_REPORTED_UUID);
                    if (uuid.equals(reported)) {
                        hosts.put(reported, host);
                    }
                }
            }

            if (hosts.containsKey(uuid)) {
                Map<Object, Object> updates = new HashMap<>();
                Host host = hosts.get(uuid);

                if (physicalHostId != null) {
                    /* Add create labels and assign the agent ID */
                    if (physicalHostId.equals(host.getPhysicalHostId()) && host.getAgentId() == null) {
                        PhysicalHost physicalHost = objectManager.loadResource(PhysicalHost.class, physicalHostId);
                        /* Copy createLabels to labels */
                        Map<String, Object> labels = CollectionUtils.toMap(data.get(HostConstants.FIELD_LABELS));
                        labels.putAll(CollectionUtils.<String, Object>toMap(data.get(HostConstants.FIELD_CREATE_LABELS)));

                        updates.putAll(createData(agent, uuid, data));
                        updates.put(HostConstants.FIELD_LABELS, labels);
                        updates.put(HostConstants.FIELD_AGENT_ID, physicalHost.getAgentId());
                    }

                    if (!physicalHostId.equals(host.getPhysicalHostId())) {
                        updates.put(HOST.PHYSICAL_HOST_ID, physicalHostId);
                    }
                }

                for (String key : UPDATABLE_HOST_FIELDS) {
                    Object value = data.get(key);
                    if (value == null || StringUtils.isBlank(value.toString())) {
                        continue;
                    }
                    Object existingValue = io.cattle.platform.object.util.ObjectUtils.getValue(host, key);
                    if (value instanceof Map && existingValue instanceof Map) {
                        Map<Object, Object> newValueMap = new HashMap<>((Map<?, ?>)existingValue);
                        newValueMap.putAll((Map<?, ?>)value);
                        value = newValueMap;
                    }
                    if (ObjectUtils.notEqual(value, existingValue)) {
                        if (ORCHESTRATE_FIELDS.contains(key)) {
                            orchestrate = true;
                        }
                        updates.put(key, value);
                    }
                }

                if (host.getMemory() == null) {
                    Long memory = DataAccessor.fromMap(data).withKey(HostConstants.FIELD_MEMORY).as(Long.class);
                    if (memory != null) {
                        updates.put(HostConstants.FIELD_MEMORY, memory);
                    }
                }

                if (host.getMilliCpu() == null) {
                    Long cpu = DataAccessor.fromMap(data).withKey(HostConstants.FIELD_MILLI_CPU).as(Long.class);
                    if (cpu != null) {
                        updates.put(HostConstants.FIELD_MILLI_CPU, cpu);
                    }
                }

                if (host.getLocalStorageMb() == null) {
                    Long storage = DataAccessor.fromMap(data).withKey(HostConstants.FIELD_LOCAL_STORAGE_MB).as(Long.class);
                    if (storage != null) {
                        updates.put(HostConstants.FIELD_LOCAL_STORAGE_MB, storage);
                    }
                }

                if (updates.size() > 0) {
                    host = objectManager.reload(host);
                    Map<String, Object> updateFields = objectManager.convertToPropertiesFor(host, updates);
                    if (orchestrate && !HostConstants.STATE_PROVISIONING.equals(host.getState())) {
                        resourceDao.updateAndSchedule(host, updateFields);
                    } else {
                        try {
                            objectManager.setFields(host, updateFields);
                            updateFields.put(ObjectMetaDataManager.ACCOUNT_FIELD, host.getAccountId());
                            // send host update event
                            Event event = EventVO.newEvent(FrameworkEvents.STATE_CHANGE)
                                    .withData(updateFields)
                                    .withResourceType(HostConstants.TYPE)
                                    .withResourceId(host.getId().toString());
                            eventService.publish(event);
                        } catch (DataChangedException e) {
                            /*
                             * Various reasons why this might conflict with other processes, but here
                             * we can safely igore it.
                             */
                        }
                    }
                }
            } else {
                data = createData(agent, uuid, data);
                data.put(HostConstants.FIELD_PHYSICAL_HOST_ID, physicalHostId);

                /* Copy createLabels to labels */
                Map<String, Object> labels = CollectionUtils.toMap(data.get(HostConstants.FIELD_LABELS));
                labels.putAll(CollectionUtils.<String, Object>toMap(data.get(HostConstants.FIELD_CREATE_LABELS)));
                data.put(HostConstants.FIELD_LABELS, labels);

                hosts.put(uuid, resourceDao.createAndSchedule(Host.class, data));
            }
        }

        return hosts;
    }

    protected Long getPhysicalHost(Agent agent, String uuid, Map<String, Object> properties) {
        if (uuid == null) {
            return null;
        }

        Map<String, PhysicalHost> hosts = agentDao.getPhysicalHosts(agent.getId());
        PhysicalHost host = hosts.get(uuid);

        if (host != null) {
            return host.getId();
        }

        host = objectManager.findAny(PhysicalHost.class, PHYSICAL_HOST.UUID, uuid);

        if (host != null && host.getRemoved() == null) {
            Long agentId = DataAccessor.fields(host).withKey(AgentConstants.ID_REF).as(Long.class);
            // For security purposes, ensure the agentIds match.
            if (agentId != null && agentId.longValue() == agent.getId()) {
                host.setAgentId(agent.getId());
                DataAccessor.fields(host).withKey(AgentConstants.ID_REF).remove();
                objectManager.persist(host);
                return host.getId();
            }
        } else if (host == null) {
            host = objectManager.findAny(PhysicalHost.class, PHYSICAL_HOST.EXTERNAL_ID, uuid);
            // For security purposes, only allow this type of assignment if the
            // host doesn't yet have an agentId
            if (host != null && host.getAgentId() == null && host.getRemoved() == null) {
                host.setAgentId(agent.getId());
                DataAccessor.fields(host).withKey(AgentConstants.ID_REF).remove();
                objectManager.persist(host);
                return host.getId();
            }
        }

        Map<String, Object> data = createData(agent, uuid, properties);
        host = resourceDao.createAndSchedule(PhysicalHost.class, data);

        return host.getId();
    }

    protected Map<String, Object> createData(Agent agent, String uuid, Map<String, Object> data) {
        Map<String, Object> properties = new HashMap<>(data);
        properties.put(HostConstants.FIELD_REPORTED_UUID, uuid);
        properties.remove(ObjectMetaDataManager.UUID_FIELD);

        Long accountId = DataAccessor.fromDataFieldOf(agent).withKey(AgentConstants.DATA_AGENT_RESOURCES_ACCOUNT_ID).as(Long.class);

        if (accountId == null) {
            accountId = agent.getAccountId();
        }

        properties.put(ObjectMetaDataManager.ACCOUNT_FIELD, accountId);
        properties.put(AgentConstants.ID_REF, agent.getId());

        return properties;
    }

    protected AgentResources processResources(Ping ping) {
        AgentResources resources = new AgentResources();
        List<Map<String, Object>> pingData = ping.getData().getResources();

        if (pingData == null) {
            return resources;
        }

        for (Map<String, Object> resource : pingData) {
            final String type = ObjectUtils.toString(resource.get(ObjectMetaDataManager.TYPE_FIELD), null);
            final String uuid = ObjectUtils.toString(resource.get(ObjectMetaDataManager.UUID_FIELD), null);

            if (type == null || uuid == null) {
                log.error("type [{}] or uuid [{}] is null for resource on pong from agent [{}]", type, uuid, ping.getResourceId());
                continue;
            }

            if (type.equals(HostConstants.TYPE)) {
                resources.getHosts().put(uuid, resource);
            } else if (type.equals(StoragePoolConstants.TYPE)) {
                resources.getStoragePools().put(uuid, resource);
            } else if (type.equals(IpAddressConstants.TYPE)) {
                resources.getIpAddresses().put(uuid, resource);
            }
        }

        return resources;
    }
}
