package com.tjazi.infra.messagesrouterupdater.core.dao;

/**
 * Created by Krzysztof Wasiak on 25/01/2016.
 */

import com.tjazi.infra.messagesrouterupdater.core.dao.model.RoutingTableDAOModel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Data Access Object for accessing routing table - this is shared with MessageRouter service
 */
public interface RoutingTableDAO extends CrudRepository<RoutingTableDAOModel, Long> {

    List<RoutingTableDAOModel> findByReceiverId(String receiverId);

    @Modifying
    @Transactional
    @Query("UPDATE RoutingTableDAOModel rtdm " +
            "SET rtdm.clusterNames=:clusterNames, " +
            "rtdm.version=:currentVersion " +
            "WHERE rtdm.id=:recordId AND " +
            "rtdm.version=:previousVersion")
    int updateClusterNamesOnRoutingRecord(
            @Param("recordId") Long recordId,
            @Param("clusterNames") String clusterNames,
            @Param("previousVersion") long previousVersion,
            @Param("currentVersion") long currentVersion);
}
