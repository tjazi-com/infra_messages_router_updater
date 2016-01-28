package org.tjazi.infra.messagesrouterupdater.core.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tjazi.infra.messagesrouterupdater.core.dao.RoutingTableDAO;
import org.tjazi.infra.messagesrouterupdater.core.dao.model.RoutingTableDAOModel;
import org.tjazi.infra.messagesrouterupdater.messages.UpdateRouteMessage;

import java.util.List;

/**
 * Created by Krzysztof Wasiak on 22/01/2016.
 */

@Service
public class UpdateRoutesCoreImpl implements UpdateRoutesCore {

    private static final Logger log = LoggerFactory.getLogger(UpdateRoutesCoreImpl.class);

    @Autowired
    private RoutingTableDAO routingTableDAO;

    @Autowired
    private ListOfClustersProvider listOfClustersProvider;

    @Autowired
    private UpdateRoutesBroadcaster updateRoutesBroadcaster;

    @Override
    public void updateRoutes(UpdateRouteMessage updateRouteMessage) {

        if (updateRouteMessage == null) {
            String errorMessage = "updateRouteMessage is null";
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        addNewRouteToExistingRecord(updateRouteMessage);

        broadcastUpdateRouteMessageToOtherClusters(updateRouteMessage);
    }

    private void addNewRouteToExistingRecord(UpdateRouteMessage updateRouteMessage) {

        List<RoutingTableDAOModel> routingRecord = routingTableDAO.findByReceiverId(updateRouteMessage.getReceiverId());

        RoutingTableDAOModel extractedRoutingRecord = routingRecord.get(0);

        String existingClusterNames = extractedRoutingRecord.getClusterNames();
        existingClusterNames += ";" + updateRouteMessage.getClusterName();

        extractedRoutingRecord.setClusterNames(existingClusterNames);
        extractedRoutingRecord.setVersion(extractedRoutingRecord.getVersion() + 1);

        routingTableDAO.save(extractedRoutingRecord);
    }

    private void broadcastUpdateRouteMessageToOtherClusters(UpdateRouteMessage updateRouteMessage) {

        // make sure message is not routed further once it gets to final clusters
        updateRouteMessage.setAllowForward(false);
        updateRoutesBroadcaster.broadcastUpdateRouteMessage(updateRouteMessage);
    }
}
