package pranjalrastogi.tmcserverhealthlogger;

import org.bukkit.plugin.java.JavaPlugin;

public final class TMCServerHealthLogger extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();

        LoggingManager loggingManager = new LoggingManager(this);
        this.getCommand("startlogging").setExecutor(new StartLoggingCommand(this, loggingManager));
        this.getCommand("stoplogging").setExecutor(new StopLoggingCommand(this, loggingManager));

        long initTime = 0L;
        long gap = (long) (20 * 120);
        loggingManager.getLog_runner().runTaskTimer(this, initTime, gap);
        this.getLogger().info("Plugin enabled, started logger.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        this.getLogger().info("Plugin disabled.");
    }
}
