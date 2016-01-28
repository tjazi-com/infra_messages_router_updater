package integrationtests;

import com.tjazi.infra.messagesrouterupdater.core.dao.RoutingTableDAO;
import com.tjazi.infra.messagesrouterupdater.core.dao.model.RoutingTableDAOModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.UUID;

/**
 * Created by Krzysztof Wasiak on 28/01/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {RepositoryConfiguration.class})
public class RoutingTableDAO_Tests {

    @Autowired
    private RoutingTableDAO routingTableDAO;

    @Test
    public void saveRecordVerifySaved_Test() {

        long version = 34;
        String receiverId = UUID.randomUUID().toString();
        String clusterNames = "cluster1;cluster2";

        RoutingTableDAOModel inputModel = new RoutingTableDAOModel();
        inputModel.setVersion(version);
        inputModel.setReceiverId(receiverId);
        inputModel.setClusterNames(clusterNames);

        // Save data
        routingTableDAO.save(inputModel);

        // and now extract those
        List<RoutingTableDAOModel> savedRecords = routingTableDAO.findByReceiverId(receiverId);

        Assert.assertEquals(1, savedRecords.size());

        RoutingTableDAOModel savedRecord = savedRecords.get(0);
        Assert.assertEquals(version, savedRecord.getVersion());
        Assert.assertEquals(receiverId, savedRecord.getReceiverId());
        Assert.assertEquals(clusterNames, savedRecord.getClusterNames());
    }

    @Test
    public void updateClustersRecord_Successful_Test() {

        long initialVersion = 34;
        long finalVersion = 35;
        String receiverId = UUID.randomUUID().toString();
        String initialClusterNames = "cluster1";
        String savedClusterNames = "cluster1;cluster2";

        RoutingTableDAOModel inputModel = new RoutingTableDAOModel();
        inputModel.setVersion(initialVersion);
        inputModel.setReceiverId(receiverId);
        inputModel.setClusterNames(initialClusterNames);

        // Save data
        routingTableDAO.save(inputModel);

        // extract record to get its record ID
        Long idOfTheSavedRecord = routingTableDAO.findByReceiverId(receiverId).get(0).getId();

        // update record with new cluster names
        int updatedRecords = routingTableDAO.updateClusterNamesOnRoutingRecord(idOfTheSavedRecord,
                savedClusterNames, initialVersion, finalVersion);

        // and now extract those to check if update was successful
        List<RoutingTableDAOModel> savedRecords = routingTableDAO.findByReceiverId(receiverId);

        Assert.assertEquals(1, savedRecords.size());

        RoutingTableDAOModel savedRecord = savedRecords.get(0);
        Assert.assertEquals(finalVersion, savedRecord.getVersion());
        Assert.assertEquals(receiverId, savedRecord.getReceiverId());
        Assert.assertEquals(savedClusterNames, savedRecord.getClusterNames());
        Assert.assertEquals(1, updatedRecords);
    }

    /**
     * This scenario covers case, when version of the record has changed between
     * reading it from database and saving updated version.
     * Record should not be saved.
     */
    @Test
    public void updateClustersRecord_Failed_ChangedVersion_Test() {

        long initialVersion = 34;
        long finalVersion = 35;
        String receiverId = UUID.randomUUID().toString();
        String initialClusterNames = "cluster1";
        String savedClusterNames = "cluster1;cluster2";

        RoutingTableDAOModel inputModel = new RoutingTableDAOModel();
        inputModel.setVersion(initialVersion);
        inputModel.setReceiverId(receiverId);
        inputModel.setClusterNames(initialClusterNames);

        // Save data
        routingTableDAO.save(inputModel);

        // extract record to get its record ID
        Long idOfTheSavedRecord = routingTableDAO.findByReceiverId(receiverId).get(0).getId();

        // update record with new cluster names
        int updatedRecords = routingTableDAO.updateClusterNamesOnRoutingRecord(idOfTheSavedRecord,
                savedClusterNames, finalVersion, finalVersion);


        // and now extract those to check if update was successful
        List<RoutingTableDAOModel> savedRecords = routingTableDAO.findByReceiverId(receiverId);

        Assert.assertEquals(1, savedRecords.size());

        RoutingTableDAOModel savedRecord = savedRecords.get(0);
        Assert.assertEquals(initialVersion, savedRecord.getVersion());
        Assert.assertEquals(receiverId, savedRecord.getReceiverId());
        Assert.assertEquals(initialClusterNames, savedRecord.getClusterNames());
        Assert.assertEquals(0, updatedRecords);
    }
}
