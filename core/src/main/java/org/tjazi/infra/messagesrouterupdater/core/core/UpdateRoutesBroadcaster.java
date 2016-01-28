package org.tjazi.infra.messagesrouterupdater.core.core;

import org.tjazi.infra.messagesrouterupdater.messages.UpdateRouteMessage;

/**
 * Created by Krzysztof Wasiak on 22/01/2016.
 */
public interface UpdateRoutesBroadcaster {

    void broadcastUpdateRouteMessage(UpdateRouteMessage updateRouteMessage);
}
