package org.tjazi.infra.messagesrouterupdater.core.core;

import org.springframework.stereotype.Service;
import org.tjazi.infra.messagesrouterupdater.messages.UpdateRouteMessage;

/**
 * Created by Krzysztof Wasiak on 22/01/2016.
 */

@Service
public class UpdateRoutesBroadcasterImpl implements UpdateRoutesBroadcaster {

    @Override
    public void broadcastUpdateRouteMessage(UpdateRouteMessage updateRouteMessage) {

    }
}
