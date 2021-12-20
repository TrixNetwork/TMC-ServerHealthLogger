package pranjalrastogi.tmcserverhealthlogger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class StopLoggingCommand implements CommandExecutor {

    public StopLoggingCommand(Plugin plugin, LoggingManager loggingManager) {
        this.loggingManager = loggingManager;
    }

    private final LoggingManager loggingManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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
