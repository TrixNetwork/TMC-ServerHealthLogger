package pranjalrastogi.tmcserverhealthlogger;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TMCServerHealthLogger extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();

        LoggingManager loggingManager = new LoggingManager(this);
        Objects.requireNonNull(this.getCommand("startlogging")).setExecutor(new StartLoggingCommand(this, loggingManager));
        Objects.requireNonNull(this.getCommand("stoplogging")).setExecutor(new StopLoggingCommand(this, loggingManager));

        long initTime = 0L;
        long gap = 20 * 120;
        loggingManager.getLog_runner().runTaskTimer(this, initTime, gap);
        this.getLogger().info("=====\n Plugin enabled, started logger.\n=====");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getLogger().info("=====\n Plugin disabled, stopped logger.\n=====");
    }
}
