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

    @Override
    public void updateRoutes(UpdateRouteMessage updateRouteMessage) throws Exception {

        if (updateRouteMessage == null) {
            String errorMessage = "updateRouteMessage is null";
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        addNewRouteToExistingRecord(updateRouteMessage);
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

            newClusterNames = routingTableDAOModel.getClusterNames() + ";" + newClusterNames;

            long previousVersion = routingTableDAOModel.getVersion();
            long currentVersion = previousVersion;

            boolean keepSaving = true;
            int savesAttemptsCounter = 0;

            while (keepSaving) {
                savesAttemptsCounter++;

                if (savesAttemptsCounter > 3) {
                    throw new UnsupportedOperationException(
                            "There were 3 attempts to update routing record, all failed. Receiver ID: " + receiverId);
                }

                previousVersion = currentVersion;
                currentVersion += 1;
                int numberOfRecordsUpdated = routingTableDAO.updateClusterNamesOnRoutingRecord(
                        routingTableDAOModel.getId(),
                        newClusterNames, previousVersion, currentVersion);

                keepSaving = numberOfRecordsUpdated == 0;
            }

        } else {
            routingTableDAOModel = new RoutingTableDAOModel();
            routingTableDAOModel.setReceiverId(updateRouteMessage.getReceiverId());
            routingTableDAOModel.setClusterNames(newClusterNames);
            routingTableDAOModel.setVersion(1);

            // this operation should not generate conflict, although who knows...
            /* TODO: cover scenario with test case, when there's already record saved with the same receiver ID */
            routingTableDAO.save(routingTableDAOModel);
        }
    }
}
