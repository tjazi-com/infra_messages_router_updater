package org.tjazi.infra.messagesrouterupdater.messages;

/**
 * Created by Krzysztof Wasiak on 22/01/2016.
 */

/**
 * Message, which intention is to update all routes and add / remove given receiver to specific cluster.
 * Example: user belonging to particular group (receiver ID: X) logs-in to cluster,
 *          so router gets updated so that all messages for group with receiver ID X will be sent to that cluster
 */
public class UpdateRoute {

    private String receiverId;

    private String clusterName;

    private UpdateRouteAction updateRouteAction;

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public UpdateRouteAction getUpdateRouteAction() {
        return updateRouteAction;
    }

    public void setUpdateRouteAction(UpdateRouteAction updateRouteAction) {
        this.updateRouteAction = updateRouteAction;
    }
}
