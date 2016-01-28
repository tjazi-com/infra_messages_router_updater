package com.tjazi.infra.messagesrouterupdater.core.core;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Created by Krzysztof Wasiak on 25/01/2016.
 */

@Service
public class ListOfClustersProviderImpl implements ListOfClustersProvider {

    @Override
    public List<String> getListOfClusters(boolean excludeCurrent) {
        return Collections.emptyList();
    }
}
