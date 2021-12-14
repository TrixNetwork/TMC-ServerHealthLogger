package pranjalrastogi.tmcserverhealthlogger;

import org.bukkit.plugin.java.JavaPlugin;

public final class TMCServerHealthLogger extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getLogger().info("[TMC ServerHealthLogger] Plugin enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getLogger().info("[TMC ServerHealthLogger] Plugin disabled.");
    }
}
