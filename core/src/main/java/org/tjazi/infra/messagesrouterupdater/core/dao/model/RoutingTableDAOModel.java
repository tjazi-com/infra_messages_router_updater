package org.tjazi.infra.messagesrouterupdater.core.dao.model;

import javax.persistence.*;

/**
 * Created by Krzysztof Wasiak on 25/01/2016.
 */

/**
 * Single entry in the routing table - this is shared with MessageRouter service.
 */
@Entity
@Table(name = "RoutingTable")
public class RoutingTableDAOModel {

    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * This field will help with dealing with concurent access to the same record by multiple processes.
     * It's highly unlikely that multiple services will run writting for the same record in the same time,
     * however: if it happens each writter should first read the record, change values and write it back again,
     * but only of version at the time of read and write is the same. Otherwise: repeat operation... (read, change, write)
     */
    @Column(name = "Version", unique = false, nullable = false)
    private long version;

    @Column(name = "ReceiverID", unique = true, nullable = false)
    private String receiverId;

    @Column(name = "ClusterNames", unique = false, nullable = false)
    private String clusterNames;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getClusterNames() {
        return clusterNames;
    }

    public void setClusterNames(String clusterNames) {
        this.clusterNames = clusterNames;
    }
}

