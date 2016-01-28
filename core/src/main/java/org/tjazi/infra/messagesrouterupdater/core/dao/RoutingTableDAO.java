package org.tjazi.infra.messagesrouterupdater.core.dao;

/**
 * Created by Krzysztof Wasiak on 25/01/2016.
 */

import org.springframework.data.repository.CrudRepository;
import org.tjazi.infra.messagesrouterupdater.core.dao.model.RoutingTableDAOModel;

import java.util.List;

/**
 * Data Access Object for accessing routing table - this is shared with MessageRouter service
 */
public interface RoutingTableDAO extends CrudRepository<RoutingTableDAOModel, Long> {

    List<RoutingTableDAOModel> findByReceiverId(String receiverId);
}
