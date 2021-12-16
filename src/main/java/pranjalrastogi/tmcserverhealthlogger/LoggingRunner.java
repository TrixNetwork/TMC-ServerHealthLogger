package pranjalrastogi.tmcserverhealthlogger;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;

public class LoggingRunner extends BukkitRunnable {

    public LoggingRunner(Plugin plugin, MongoCollection<Document> health_collection) {
        this.plugin = plugin;
        this.health_collection = health_collection;
    }
    private final MongoCollection<Document> health_collection;
    private final Plugin plugin;

    @Override
    public void run() {
        // LOG: TPS, memory, CPU, player Count
        // Aggregate data over an hour (30 points)
        // pre aggregate:
        // avg tps [SUM_TPS]
        // avg mem [SUM_MEMORY]
        // avg cpu [SUM_CPU]

        // get TPS
        DecimalFormat df = new DecimalFormat("#.##");
        double TPS = Double.parseDouble(df.format(this.plugin.getServer().getTPS()[0]));

        // get Memory
        Runtime r = Runtime.getRuntime();
        long usedMemory = r.totalMemory() - r.freeMemory() / 1048576;  // converting to MB
        long allocatedMemory = r.totalMemory() / 1048576;

        // TODO: get CPU (See OSBean or Pterodactyl API)

        // get player count
        int playerCount = this.plugin.getServer().getOnlinePlayers().size();

        this.plugin.getLogger().info(TPS + " TPS (1m) | " + usedMemory + "MB currently used. Allocated: "
                + allocatedMemory + "MB. | Player Count: " + playerCount);

    }

}
