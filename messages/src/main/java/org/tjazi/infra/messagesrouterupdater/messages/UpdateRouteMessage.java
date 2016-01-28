package org.tjazi.infra.messagesrouterupdater.messages;

/**
 * Created by Krzysztof Wasiak on 22/01/2016.
 */

/**
 * Message, which intention is to update all routes and add / remove given receiver to specific cluster.
 * Example: user belonging to particular group (receiver ID: X) logs-in to cluster,
 *          so router gets updated so that all messages for group with receiver ID X will be sent to that cluster
 */
public class UpdateRouteMessage {

    private String receiverId;

    private String clusterName;

    private UpdateRouteMessageAction updateRouteMessageAction;

    /**
     * Allow message to be forwarded to other clusters - if needed.
     * Keeping this as 'true' will cause this message to be sent indefinetely between clusters,
     * which can result with spiraling number of messages in the system
     */
    private boolean allowForward;

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

    public UpdateRouteMessageAction getUpdateRouteMessageAction() {
        return updateRouteMessageAction;
    }

    public void setUpdateRouteMessageAction(UpdateRouteMessageAction updateRouteMessageAction) {
        this.updateRouteMessageAction = updateRouteMessageAction;
    }

    public boolean isAllowForward() {
        return allowForward;
    }

    public void setAllowForward(boolean allowForward) {
        this.allowForward = allowForward;
    }
}
