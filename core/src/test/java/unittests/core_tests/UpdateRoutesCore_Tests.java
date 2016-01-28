package unittests.core_tests;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tjazi.infra.messagesrouterupdater.core.core.ListOfClustersProvider;
import org.tjazi.infra.messagesrouterupdater.core.core.UpdateRoutesBroadcaster;
import org.tjazi.infra.messagesrouterupdater.core.core.UpdateRoutesCoreImpl;
import org.tjazi.infra.messagesrouterupdater.core.dao.RoutingTableDAO;
import org.tjazi.infra.messagesrouterupdater.core.dao.model.RoutingTableDAOModel;
import org.tjazi.infra.messagesrouterupdater.messages.UpdateRouteMessage;
import org.tjazi.infra.messagesrouterupdater.messages.UpdateRouteMessageAction;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Created by Krzysztof Wasiak on 25/01/2016.
 */

@RunWith(MockitoJUnitRunner.class)
public class UpdateRoutesCore_Tests {

    @Rule
    public ExpectedException rule = ExpectedException.none();

    @Mock
    public RoutingTableDAO routingTableDAO;

    @Mock
    public UpdateRoutesBroadcaster updateRoutesBroadcaster;

    @Mock
    public ListOfClustersProvider listOfClustersProvider;

    @InjectMocks
    public UpdateRoutesCoreImpl updateRoutesCore;

    @Test
    public void updateRoutes_NullInput_Test() {

        rule.expect(IllegalArgumentException.class);

        updateRoutesCore.updateRoutes(null);
    }

    @Test
    public void updateRoutes_UpdateExistingRecord_BroadcastToOtherClusters_test() {

        String existingClusterName = "cluster1" + UUID.randomUUID().toString();
        String newClusterName = "cluster2" + UUID.randomUUID().toString();
        String expectedNewRecordClusters = existingClusterName + ";" + newClusterName;
        UpdateRouteMessageAction updateRouteMessageAction = UpdateRouteMessageAction.ADDROUTE;
        long recordId = 2427491;
        long recordVersion = 32;

        String receiverId = UUID.randomUUID().toString();

        RoutingTableDAOModel dataModel = new RoutingTableDAOModel();
        dataModel.setClusterNames(existingClusterName);
        dataModel.setId(recordId);
        dataModel.setReceiverId(receiverId.toString());
        dataModel.setVersion(32);

        when(routingTableDAO.findByReceiverId(anyString()))
                .thenReturn(Collections.singletonList(dataModel));


        // main call
        UpdateRouteMessage requestMessage = new UpdateRouteMessage();
        requestMessage.setReceiverId(receiverId);
        requestMessage.setAllowForward(true);
        requestMessage.setClusterName(newClusterName);
        requestMessage.setUpdateRouteMessageAction(updateRouteMessageAction);

        updateRoutesCore.updateRoutes(requestMessage);

        //**
        //** verification and assertion
        //**

        // STEP 1 - get existing record
        ArgumentCaptor<String> receiverIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(routingTableDAO).findByReceiverId(receiverIdCaptor.capture());

        Assert.assertEquals(receiverId, receiverIdCaptor.getValue());

        // STEP 2 - save updated record
        ArgumentCaptor<RoutingTableDAOModel> daoModelCaptor = ArgumentCaptor.forClass(RoutingTableDAOModel.class);
        verify(routingTableDAO).save(daoModelCaptor.capture());

        RoutingTableDAOModel capturedDaoModel = daoModelCaptor.getValue();
        Assert.assertNotNull(capturedDaoModel);
        Assert.assertEquals(recordId, capturedDaoModel.getId());
        Assert.assertEquals(expectedNewRecordClusters, capturedDaoModel.getClusterNames());
        Assert.assertEquals(receiverId, capturedDaoModel.getReceiverId());
        Assert.assertEquals(recordVersion + 1, capturedDaoModel.getVersion());

        // STEP 3 -broadcast message to other clusters
        ArgumentCaptor<UpdateRouteMessage> broadcastMessageCaptor = ArgumentCaptor.forClass(UpdateRouteMessage.class);
        verify(updateRoutesBroadcaster).broadcastUpdateRouteMessage(broadcastMessageCaptor.capture());

        UpdateRouteMessage broadcastedMessage = broadcastMessageCaptor.getValue();
        Assert.assertNotNull(broadcastedMessage);
        Assert.assertEquals(receiverId, broadcastedMessage.getReceiverId());
        Assert.assertEquals(updateRouteMessageAction, broadcastedMessage.getUpdateRouteMessageAction());
        Assert.assertEquals(newClusterName, broadcastedMessage.getClusterName());
        Assert.assertEquals(false, broadcastedMessage.isAllowForward());
    }
}