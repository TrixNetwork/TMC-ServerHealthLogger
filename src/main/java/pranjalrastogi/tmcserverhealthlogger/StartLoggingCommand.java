package pranjalrastogi.tmcserverhealthlogger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class StartLoggingCommand implements CommandExecutor {

    public StartLoggingCommand(Plugin plugin, LoggingManager loggingManager) {
        this.plugin = plugin;
        this.loggingManager = loggingManager;
    }

    private final Plugin plugin;
    private final LoggingManager loggingManager;

    @Override
    public boolean onCommand(CommandSender sender,Command command, String label, String[] args) {
        if (!this.loggingManager.getLog_runner().isCancelled()) {
            // not cancelled, don't do anything
            sender.sendMessage("[TMCServerHealthLogger] Already running");
        } else {
            // cancel, to start
            long initTime = 0L;
            long gap = (long) (20 * 120);
            this.loggingManager.setLog_runner(new LoggingRunner(this.plugin, this.loggingManager.getHealth_collection()));
            this.loggingManager.getLog_runner().runTaskTimer(this.plugin, initTime, gap);
            sender.sendMessage("[TMCServerHealthLogger] Started task.");
        }
        return true;
    }
}
