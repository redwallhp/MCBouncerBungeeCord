package com.mcbouncer.bungee.command;

import com.mcbouncer.bungee.MCBouncer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class MCBouncerCommand extends Command {

    MCBouncer plugin;
    
    public MCBouncerCommand(MCBouncer plugin) {
        super("mcb");
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
				if (args.length == 1 && args[0].equalsIgnoreCase("import")) {
					sender.sendMessage("Importing bans.txt");
					plugin.importDatabase();
					sender.sendMessage("Finished importing bans");
				}
			}
		});
	}
}
