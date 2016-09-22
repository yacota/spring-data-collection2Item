package hello.model;

import hello.PatchMongoDbConfiguration;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import hello.Application;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringApplicationConfiguration(classes = {Application.class, PatchMongoDbConfiguration.class})
public class PatchCarRepositoryTest {

    static final String CAR_COLLECTION = "car";
    
    @Autowired
    protected CarRepository      carRepository;
    
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static MongodExecutable mongodExecutable;
    private static MongodProcess mongoD;
    private static MongoClient mongo;
    
    @Autowired
    private MongoClient client;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        int port = 12345;
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                                                              .net(new Net(port, Network.localhostIsIPv6())).build();
        mongodExecutable = starter.prepare(mongodConfig);
        mongoD = mongodExecutable.start();
        mongo = new MongoClient("localhost", port);
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        mongoD.stop();
        mongodExecutable.stop();
    }
    
    @Before
    public void before() {
        DB db = client.getDB("test");
        DBCollection col1 = db.getCollection(CAR_COLLECTION);
        BasicDBObject engine1 = new BasicDBObject("_id", "toyota");
        BasicDBObject engine2 = new BasicDBObject("_id", "secondOneNotPicked");
        col1.save(new BasicDBObject("_id", 1L).append("engine",  Arrays.asList(engine1, engine2)));
    }
    
    @After
    public void after() {
        client.getDB("test").getCollection(CAR_COLLECTION).drop();
    }
    
    //
    // Test
    //
    
    @Test
    public void testMapping() {
        Car findOne = carRepository.findOne(1L);
        Assert.assertNotNull(findOne);
        Assert.assertEquals("toyota", findOne.engine.id);
    }
}