package pranjalrastogi.tmcserverhealthlogger;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class LoggingManager {

    private LoggingRunner log_runner;
    private final MongoCollection<Document> health_collection;

    public LoggingManager(Plugin plugin) {

        FileConfiguration config = plugin.getConfig();
        String uri = config.getString("uri");

        if (uri == null) {
            plugin.getLogger().info("Big error why my URI broken!");
        }

        MongoClient mongoClient = MongoClients.create(Objects.requireNonNull(config.getString("uri")));

        MongoDatabase database = mongoClient.getDatabase("the_oracle_n");
        this.health_collection = database.getCollection("server_health_data");
        this.log_runner = new LoggingRunner(plugin, this.health_collection);
    }


    public MongoCollection<Document> getHealth_collection() {
        return health_collection;
    }

    public LoggingRunner getLog_runner() {
        return log_runner;
    }

    public void setLog_runner(LoggingRunner log_runner) {
        this.log_runner = log_runner;
    }
}
