package pranjalrastogi.tmcserverhealthlogger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class StopLoggingCommand implements CommandExecutor {

    public StopLoggingCommand(Plugin plugin, LoggingManager loggingManager) {
        this.plugin = plugin;
        this.loggingManager = loggingManager;
    }

    private final Plugin plugin;
    private final LoggingManager loggingManager;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.loggingManager.getLog_runner().isCancelled()) {
            // not cancelled, cancel it.
            this.loggingManager.getLog_runner().cancel();
            sender.sendMessage("[TMCServerHealthLogger] Cancelled task.");
        } else {
            // error
            sender.sendMessage("[TMCServerHealthLogger] Already stopped.");
        }
        return true;
    }
}
