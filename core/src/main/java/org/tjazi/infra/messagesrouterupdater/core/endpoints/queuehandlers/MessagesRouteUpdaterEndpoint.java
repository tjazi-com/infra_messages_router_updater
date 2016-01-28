package org.tjazi.infra.messagesrouterupdater.core.endpoints.queuehandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tjazi.infra.messagesrouterupdater.messages.UpdateRouteMessage;

/**
 * Created by Krzysztof Wasiak on 22/01/2016.
 */

@Service
public class MessagesRouteUpdaterEndpoint {

    private final static Logger log = LoggerFactory.getLogger(MessagesRouteUpdaterEndpoint.class);

    public void updateRoute(UpdateRouteMessage updateRouteMessage) {

    }


}
