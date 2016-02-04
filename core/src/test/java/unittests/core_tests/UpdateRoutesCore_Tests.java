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
import com.tjazi.infra.messagesrouterupdater.core.core.UpdateRoutesCoreImpl;
import com.tjazi.infra.messagesrouterupdater.core.dao.RoutingTableDAO;
import com.tjazi.infra.messagesrouterupdater.core.dao.model.RoutingTableDAOModel;
import org.tjazi.infra.messagesrouterupdater.messages.UpdateRouteMessage;
import org.tjazi.infra.messagesrouterupdater.messages.UpdateRouteMessageAction;

import javax.management.OperationsException;
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

    @InjectMocks
    public UpdateRoutesCoreImpl updateRoutesCore;

    @Test
    public void updateRoutes_NullInput_Test() throws Exception {

        rule.expect(IllegalArgumentException.class);

        updateRoutesCore.updateRoutes(null);
    }

    @Test
    public void updateRoutes_UpdateExistingRecord_Test() throws Exception {

        String existingClusterName = "cluster1" + UUID.randomUUID().toString();
        String newClusterName = "cluster2" + UUID.randomUUID().toString();
        String expectedNewRecordClusters = existingClusterName + ";" + newClusterName;
        UpdateRouteMessageAction updateRouteMessageAction = UpdateRouteMessageAction.ADDROUTE;
        long recordId = 2427491;
        long recordVersion = 32;
        long recordNewVersion = 33;

        String receiverId = UUID.randomUUID().toString();

        RoutingTableDAOModel dataModel = new RoutingTableDAOModel();
        dataModel.setClusterNames(existingClusterName);
        dataModel.setId(recordId);
        dataModel.setReceiverId(receiverId.toString());
        dataModel.setVersion(recordVersion);

        // find record to be updated
        when(routingTableDAO.findByReceiverId(anyString()))
                .thenReturn(Collections.singletonList(dataModel));

        when(routingTableDAO.updateClusterNamesOnRoutingRecord(anyLong(), anyString(), anyLong(), anyLong()))
                .thenReturn(1);

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
        verify(routingTableDAO).updateClusterNamesOnRoutingRecord(
                recordId, expectedNewRecordClusters, recordVersion, recordNewVersion);
    }

    /**
     * Check behaviour when:
     * 1. We've got routing record for the given receiver
     * 2. We change it and attempt to save.
     * 3. In the meantime record has been already updated by another instance of the updater
     * 4. We should re-read record and attempt to save it again
     */
    @Test
    public void updateRoutes_UpdateRecordOverwrittenInTheMiddleOfTheSession_Test() throws Exception {
        String existingClusterName = "cluster1" + UUID.randomUUID().toString();
        String newClusterName = "cluster2" + UUID.randomUUID().toString();
        String expectedNewRecordClusters = existingClusterName + ";" + newClusterName;
        UpdateRouteMessageAction updateRouteMessageAction = UpdateRouteMessageAction.ADDROUTE;
        long recordId = 2427491;
        long recordVersion = 32;
        long recordConflictingVersion = 33;
        long recordNewVersion = 34;

        String receiverId = UUID.randomUUID().toString();

        RoutingTableDAOModel dataModelVersion1 = new RoutingTableDAOModel();
        dataModelVersion1.setClusterNames(existingClusterName);
        dataModelVersion1.setId(recordId);
        dataModelVersion1.setReceiverId(receiverId.toString());
        dataModelVersion1.setVersion(recordVersion);

        RoutingTableDAOModel dataModelVersion2 = new RoutingTableDAOModel();
        dataModelVersion2.setClusterNames(existingClusterName);
        dataModelVersion2.setId(recordId);
        dataModelVersion2.setReceiverId(receiverId.toString());
        dataModelVersion2.setVersion(recordConflictingVersion);


        // find record to be updated
        when(routingTableDAO.findByReceiverId(anyString()))
                .thenReturn(Collections.singletonList(dataModelVersion1));


        when(routingTableDAO.updateClusterNamesOnRoutingRecord(anyLong(), anyString(), anyLong(), anyLong()))
                .thenReturn(0)
                .thenReturn(1);

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

        // STEP 2 - save updated record (2 attempts: 1 for conflicting version, which must be unsuccessful, second: working
        verify(routingTableDAO).updateClusterNamesOnRoutingRecord(
                recordId, expectedNewRecordClusters, recordVersion, recordConflictingVersion);

        verify(routingTableDAO).updateClusterNamesOnRoutingRecord(
                recordId, expectedNewRecordClusters, recordConflictingVersion, recordNewVersion);
    }

    /**
     * Check behaviour when:
     * 1. We've got routing record for the given receiver
     * 2. We change it and attempt to save.
     * 3. In the meantime record has been already updated by another instance of the updater
     * 4. We should re-read record and attempt to save it again. If 3rd attempt fails, we throw an exception
     */
    @Test(timeout = 500) // timeout, because there may be endless loop in the code
    public void updateRoutes_UpdateRecordOverwrittenInTheMiddleOfTheSession_FailOn3rdAttempt_Test() throws Exception {
        String existingClusterName = "cluster1" + UUID.randomUUID().toString();
        String newClusterName = "cluster2" + UUID.randomUUID().toString();
        String expectedNewRecordClusters = existingClusterName + ";" + newClusterName;
        UpdateRouteMessageAction updateRouteMessageAction = UpdateRouteMessageAction.ADDROUTE;
        long recordId = 2427491;
        long recordVersion = 32;
        long recordConflictingVersion = 33;
        long recordNewVersion = 34;

        String receiverId = UUID.randomUUID().toString();

        RoutingTableDAOModel dataModelVersion1 = new RoutingTableDAOModel();
        dataModelVersion1.setClusterNames(existingClusterName);
        dataModelVersion1.setId(recordId);
        dataModelVersion1.setReceiverId(receiverId.toString());
        dataModelVersion1.setVersion(recordVersion);

        RoutingTableDAOModel dataModelVersion2 = new RoutingTableDAOModel();
        dataModelVersion2.setClusterNames(existingClusterName);
        dataModelVersion2.setId(recordId);
        dataModelVersion2.setReceiverId(receiverId.toString());
        dataModelVersion2.setVersion(recordConflictingVersion);


        // find record to be updated
        when(routingTableDAO.findByReceiverId(anyString()))
                .thenReturn(Collections.singletonList(dataModelVersion1));

        when(routingTableDAO.updateClusterNamesOnRoutingRecord(anyLong(), anyString(), anyLong(), anyLong()))
                .thenReturn(0)
                .thenReturn(0)
                .thenReturn(0);

        // expect exception after 3rd failed attempt
        rule.expect(UnsupportedOperationException.class);

        // main call
        UpdateRouteMessage requestMessage = new UpdateRouteMessage();
        requestMessage.setReceiverId(receiverId);
        requestMessage.setAllowForward(true);
        requestMessage.setClusterName(newClusterName);
        requestMessage.setUpdateRouteMessageAction(updateRouteMessageAction);

        updateRoutesCore.updateRoutes(requestMessage);
    }

    @Test
    public void updateRoutes_CreateNewRoutingRecord_Test() throws Exception {

        String newClusterName = "cluster1" + UUID.randomUUID().toString();
        String expectedNewRecordClusters = newClusterName;
        UpdateRouteMessageAction updateRouteMessageAction = UpdateRouteMessageAction.ADDROUTE;
        long recordId = 0;
        long recordVersion = 1;

        String receiverId = UUID.randomUUID().toString();

        // find record to be updated
        when(routingTableDAO.findByReceiverId(anyString()))
                .thenReturn(Collections.emptyList());

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

        // STEP 2 - DAO CALL - save updated record
        ArgumentCaptor<RoutingTableDAOModel> daoModelCaptor = ArgumentCaptor.forClass(RoutingTableDAOModel.class);
        verify(routingTableDAO).save(daoModelCaptor.capture());

        RoutingTableDAOModel capturedDaoModel = daoModelCaptor.getValue();
        Assert.assertNotNull(capturedDaoModel);
        Assert.assertEquals(recordId, capturedDaoModel.getId());
        Assert.assertEquals(expectedNewRecordClusters, capturedDaoModel.getClusterNames());
        Assert.assertEquals(receiverId, capturedDaoModel.getReceiverId());
        Assert.assertEquals(recordVersion, capturedDaoModel.getVersion());
    }

    /**
     * Test case, when there's more than one routing record saved in the database
     * @throws Exception
     */
    @Test
    public void updateRoutes_UpdateExistingRecord_FailOnDuplicateRoutingRecord_Test() throws Exception {

        String receiverId = UUID.randomUUID().toString();

        RoutingTableDAOModel dataModel = new RoutingTableDAOModel();

        // find record to be updated
        // return 2 copies of the record, which should generate an exception
        when(routingTableDAO.findByReceiverId(anyString()))
                .thenReturn(Collections.nCopies(2, dataModel));

        // main call
        UpdateRouteMessage requestMessage = new UpdateRouteMessage();
        requestMessage.setReceiverId(receiverId);

        rule.expect(Exception.class);
        updateRoutesCore.updateRoutes(requestMessage);
    }
}
