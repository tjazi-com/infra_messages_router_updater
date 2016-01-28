package org.tjazi.infra.messagesrouterupdater.core.core;

import java.util.List;

/**
 * Created by Krzysztof Wasiak on 25/01/2016.
 */
public interface ListOfClustersProvider {

    /**
     * Get list of all clusters registered in tjazi.com.
     * @param excludeCurrent True - exclude current cluster (i.e.: list ALL cluster names, excluding the
     * @return List of all nodes
     */
    List<String> getListOfClusters(boolean excludeCurrent);
}
