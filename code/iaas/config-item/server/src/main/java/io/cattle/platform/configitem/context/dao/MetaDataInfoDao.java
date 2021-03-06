package io.cattle.platform.configitem.context.dao;

import io.cattle.platform.configitem.context.data.metadata.common.ContainerMetaData;
import io.cattle.platform.configitem.context.data.metadata.common.HostMetaData;
import io.cattle.platform.configitem.context.data.metadata.common.NetworkMetaData;

import java.util.List;
import java.util.Map;

public interface MetaDataInfoDao {
    public enum Version {
        version1("2015-07-25", "2015-07-25"),
        version2("2015-12-19", "2015-12-19"),
        version3("2016-07-29", "2016-07-29"),
        latestVersion("latest", "2016-07-29");

        String tag;
        String value;

        private Version(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        public String getTag() {
            return this.tag;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    List<ContainerMetaData> getContainersData(long accountId);

    List<String> getPrimaryIpsOnInstanceHost(long hostId);

    Map<Long, HostMetaData> getHostIdToHostMetadata(long accountId);

    List<HostMetaData> getInstanceHostMetaData(long accountId, long instanceId);

    List<NetworkMetaData> getNetworksMetaData(long accountId);
}
