package com.mcbouncer.bungee.command;

import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LookupCommand extends Command {

    MCBouncer plugin;
    
    public LookupCommand(MCBouncer plugin) {
        super("lookup");
        this.plugin = plugin;
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
                try {
                    ProxiedPlayer player = null;
                    
                    if (!sender.hasPermission("mcbouncer.mod")) {
                        sender.sendMessage(ChatColor.RED + "You need permission to run that command.");
                        return;
                    }
                    
                    if (sender instanceof ProxiedPlayer) {
                        player = (ProxiedPlayer)sender;
                    }
                    if (args.length == 0) {
                        sender.sendMessage(ChatColor.RED + "Syntax: /lookup <username>");
                        return;
                    }
                    String username = args[0];

                    plugin.lookup(player, username);
                } catch (NetworkException ex) {
                    sender.sendMessage(ChatColor.RED + "Network error, could not reach mcbouncer.com");
                } catch (APIException ex) {
                    sender.sendMessage(ChatColor.RED + ex.getMessage());
                }
            }
        });
    }

}
