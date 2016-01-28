package com.tjazi.infra.messagesrouterupdater.core.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tjazi.infra.messagesrouterupdater.core.dao.RoutingTableDAO;
import com.tjazi.infra.messagesrouterupdater.core.dao.model.RoutingTableDAOModel;
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
    public void updateRoutes(UpdateRouteMessage updateRouteMessage) throws Exception {

        if (updateRouteMessage == null) {
            String errorMessage = "updateRouteMessage is null";
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        addNewRouteToExistingRecord(updateRouteMessage);

        broadcastUpdateRouteMessageToOtherClusters(updateRouteMessage);
    }

    private void addNewRouteToExistingRecord(UpdateRouteMessage updateRouteMessage) throws Exception {

        String receiverId = updateRouteMessage.getReceiverId();
        List<RoutingTableDAOModel> routingRecords = routingTableDAO.findByReceiverId(receiverId);

        if (routingRecords.size() > 1) {
            String message = "Got multiple routing records for receiver ID: " + receiverId;
            log.error(message);
            throw new Exception(message);
        }

        String newClusterNames = updateRouteMessage.getClusterName();
        RoutingTableDAOModel routingTableDAOModel;

        if (routingRecords.size() == 1) {
            routingTableDAOModel = routingRecords.get(0);

            long previousVersion = routingTableDAOModel.getVersion();
            long currentVersion = previousVersion + 1;

            newClusterNames = routingTableDAOModel.getClusterNames() + ";" + newClusterNames;

            routingTableDAO.updateClusterNamesOnRoutingRecord(
                    routingTableDAOModel.getId(),
                    newClusterNames, previousVersion, currentVersion);

        } else {
            routingTableDAOModel = new RoutingTableDAOModel();
            routingTableDAOModel.setReceiverId(updateRouteMessage.getReceiverId());
            routingTableDAOModel.setClusterNames(newClusterNames);
            routingTableDAOModel.setVersion(1);

            routingTableDAO.save(routingTableDAOModel);
        }
    }

    private void broadcastUpdateRouteMessageToOtherClusters(UpdateRouteMessage updateRouteMessage) {

        // make sure message is not routed further once it gets to final clusters
        updateRouteMessage.setAllowForward(false);
        updateRoutesBroadcaster.broadcastUpdateRouteMessage(updateRouteMessage);
    }
}
