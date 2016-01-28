package org.tjazi.infra.messagesrouterupdater.messages;

/**
 * Created by Krzysztof Wasiak on 22/01/2016.
 */
public enum UpdateRouteMessageAction {

    /**
     * Add cluster name to the route, so that messages for that receiver were sent via that cluster
     */
    ADDROUTE,

    /**
     * Remove route for the given cluster for receiver with the specified ID.
     * This may happen if user leaves chat, logs-off, group is terminated, etc.
     */
    REMOVEROUTE
}
