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

        List<RoutingTableDAOModel> routingRecords = routingTableDAO.findByReceiverId(updateRouteMessage.getReceiverId());

        String newClusterNames = updateRouteMessage.getClusterName();

        RoutingTableDAOModel routingTableDAOModel;
        long recordVersion;

        if (routingRecords.size() == 1) {
            routingTableDAOModel = routingRecords.get(0);

            newClusterNames = routingTableDAOModel.getClusterNames() + ";" + newClusterNames;
            recordVersion = routingTableDAOModel.getVersion() + 1;
        } else {
            routingTableDAOModel = new RoutingTableDAOModel();
            routingTableDAOModel.setReceiverId(updateRouteMessage.getReceiverId());
            recordVersion = 1;
        }

        routingTableDAOModel.setClusterNames(newClusterNames);
        routingTableDAOModel.setVersion(recordVersion);

        routingTableDAO.save(routingTableDAOModel);
    }

    private void broadcastUpdateRouteMessageToOtherClusters(UpdateRouteMessage updateRouteMessage) {

        // make sure message is not routed further once it gets to final clusters
        updateRouteMessage.setAllowForward(false);
        updateRoutesBroadcaster.broadcastUpdateRouteMessage(updateRouteMessage);
    }
}
