package pranjalrastogi.tmcserverhealthlogger;

import com.mongodb.client.MongoCollection;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
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
        double TPS = Double.parseDouble(df.format(this.plugin.getServer().getTPS()[0]));

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
            this.plugin.getLogger().info("Error, cancelling");
            this.cancel();
        }

        // get player count
        int playerCount = this.plugin.getServer().getOnlinePlayers().size();

    }

}
