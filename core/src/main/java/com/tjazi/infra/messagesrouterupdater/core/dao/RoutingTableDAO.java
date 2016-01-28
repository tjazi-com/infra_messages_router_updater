package com.tjazi.infra.messagesrouterupdater.core.dao;

/**
 * Created by Krzysztof Wasiak on 25/01/2016.
 */

import com.tjazi.infra.messagesrouterupdater.core.dao.model.RoutingTableDAOModel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
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
            "SET rtdm.clusterNames=?2, " +
            "rtdm.version=?4 " +
            "WHERE rtdm.id=?1 AND " +
            "rtdm.version=?3")
    int updateClusterNamesOnRoutingRecord(Long recordId, String clusterNames, long previousVersion, long currentVersion);
}
