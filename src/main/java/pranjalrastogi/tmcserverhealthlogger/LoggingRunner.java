package pranjalrastogi.tmcserverhealthlogger;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Collections.singletonList;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

public class LoggingRunner extends BukkitRunnable {

    public LoggingRunner(Plugin plugin, MongoCollection<Document> health_collection) {
        this.plugin = plugin;
        this.health_collection = health_collection;
    }
    private final MongoCollection<Document> health_collection;
    private final Plugin plugin;

    @Override
    public void run() {
        // LOG: TPS, memory, petro_mem, CPU, player Count
        // Aggregate data over an hour (30 points)
        // pre aggregate:
        // avg tps [SUM_TPS]
        // avg mem [SUM_MEMORY]
        // avg petro_mem [SUM_PETRO_MEMORY]
        // avg cpu [SUM_CPU]

        DecimalFormat df = new DecimalFormat("#.##");

        // get TPS
        float TPS = Float.parseFloat(df.format(this.plugin.getServer().getTPS()[0]));

        // get Memory
        Runtime r = Runtime.getRuntime();
        long usedMemory = (r.totalMemory() - r.freeMemory()) / 1048576;  // converting to MB

        // get Ptero-Memory and Ptero-CPU (Pterodactyl API)
        long pteroMemory = 0L;
        double pteroCPU = 0.0;

        String url = this.plugin.getConfig().getString("petro.base_url") + "/api/client/servers/" +
                this.plugin.getConfig().getString("petro.server_id") + "/resources";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " +
                        this.plugin.getConfig().getString("petro.api_key"))
                .addHeader("Content-type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                this.plugin.getLogger().info("Something went wrong with pterodactyl, skipping those values.");
                // 0, 0.0
            }
            else {
                JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());
                JSONObject resources = json.getJSONObject("attributes").getJSONObject("resources");
                pteroCPU = Double.parseDouble(df.format(resources.getDouble("cpu_absolute")));
                pteroMemory = resources.getLong("memory_bytes") / 1048576;
            }
        }
        catch (IOException e){
            this.plugin.getLogger().info("API Error, returning. Message:\n" + e.getMessage());
            return;
        }

        // get player count
        int playerCount = this.plugin.getServer().getOnlinePlayers().size();


        // Sending to mongodb
        Document doc = this.health_collection.find(eq("server.id", this.plugin.getConfig()
                        .getString("petro.server_id")))
                .sort(Sorts.descending("dt_start"))
                .first();

        if (doc == null) {
            // This server_id has no documents at all
            Document docMini = new Document()
                    .append("tps", TPS)
                    .append("memory", usedMemory)
                    .append("ptero_mem", pteroMemory)
                    .append("cpu", pteroCPU)
                    .append("player_count", playerCount)
                    .append("dt", new BsonDateTime(Instant.now().toEpochMilli()));
            Document idDoc = new Document()
                    .append("id", this.plugin.getConfig().getString("petro.server_id"))
                    .append("name", this.plugin.getConfig().getString("server_name"));
            Document docToWrite = new Document()
                    .append("server", idDoc)
                    .append("dt_start", new BsonDateTime(Instant.now().toEpochMilli()))
                    .append("dt_end", new BsonDateTime(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                    .append("data", singletonList(docMini))
                    .append("sum_tps", TPS)
                    .append("sum_memory", usedMemory)
                    .append("sum_ptero_mem", pteroMemory)
                    .append("sum_cpu", pteroCPU)
                    .append("data_count", 1);
            this.health_collection.insertOne(docToWrite);
        } else {
            // found a document with the highest start_time
            // check if document has expired (end_time <= now_time)
            if ( ((Date) doc.get("dt_end")).compareTo(new Date()) <= 0) {
                // end time is less or equal to now time
                // document expired
                // send new doc
                Document docMini = new Document()
                        .append("tps", TPS)
                        .append("memory", usedMemory)
                        .append("ptero_mem", pteroMemory)
                        .append("cpu", pteroCPU)
                        .append("player_count", playerCount)
                        .append("dt", new BsonDateTime(Instant.now().toEpochMilli()));
                Document idDoc = new Document()
                        .append("id", this.plugin.getConfig().getString("petro.server_id"))
                        .append("name", this.plugin.getConfig().getString("server_name"));
                Document docToWrite = new Document()
                        .append("server", idDoc)
                        .append("dt_start", new BsonDateTime(Instant.now().toEpochMilli()))
                        .append("dt_end", new BsonDateTime(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                        .append("data", singletonList(docMini))
                        .append("sum_tps", TPS)
                        .append("sum_memory", usedMemory)
                        .append("sum_ptero_mem", pteroMemory)
                        .append("sum_cpu", pteroCPU)
                        .append("data_count", 1);
                this.health_collection.insertOne(docToWrite);
            }
            else {
                // last document hasn't expired, update it
                Document docMini = new Document()
                        .append("tps", TPS)
                        .append("memory", usedMemory)
                        .append("ptero_mem", pteroMemory)
                        .append("cpu", pteroCPU)
                        .append("player_count", playerCount)
                        .append("dt", new BsonDateTime(Instant.now().toEpochMilli()));
                Bson updates = Updates.combine(
                        Updates.inc("sum_tps", TPS),
                        Updates.inc("sum_memory", usedMemory),
                        Updates.inc("sum_ptero_mem", pteroMemory),
                        Updates.inc("sum_cpu", pteroCPU),
                        Updates.inc("data_count", 1),
                        Updates.addToSet("data", docMini)
                );
                this.health_collection.updateOne(eq("_id", doc.getObjectId("_id")), updates);
            }
        }

    }

}
