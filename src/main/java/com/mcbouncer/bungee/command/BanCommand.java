package com.mcbouncer.bungee.command;

import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;
import com.mcbouncer.util.MiscUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BanCommand extends Command {

    MCBouncer plugin;
    
    public BanCommand(MCBouncer plugin) {
        super("ban");
        this.plugin = plugin;
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args) {
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {

            public void run() {
                ProxiedPlayer player = null;
                
                if (!sender.hasPermission("mcbouncer.mod")) {
                    sender.sendMessage(ChatColor.RED + "You need permission to run that command.");
                    return;
                }
                
                if (sender instanceof ProxiedPlayer) {
                    player = (ProxiedPlayer)sender;
                }
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "Syntax:  /ban <username> [reason]");
                    return;
                }
                String toBan = args[0];
                String reason = plugin.config.defaultBanMessage;
                
                if (args.length > 1) {
                    reason = MiscUtils.join(args, " ", 1, args.length);
                }

                ProxiedPlayer p = plugin.getProxy().getPlayer(toBan);
                if (p != null) {
                    p.disconnect("Banned: " + reason);
                }
                
                boolean success = false;
                try {
                    success = plugin.api.addBan(sender.getName(), toBan, reason);
                } catch (NetworkException ex) {
                    sender.sendMessage(ChatColor.RED + "Network Timeout, could, not reach mcbouncer.com");
                } catch (APIException ex) {
                    sender.sendMessage(ChatColor.RED + "An API Error Occured.");
                }
                
                if (success) {
                    String message = ChatColor.GREEN + "User " + toBan + " has been banned by " + sender.getName() + ". (" + reason + ")";
                    plugin.getLogger().info(ChatColor.stripColor(message));
                    if (plugin.config.showBanMessages) {
                        plugin.getProxy().broadcast(message);
                    }
                    else {
                        for (ProxiedPlayer pl : plugin.getProxy().getPlayers()) {
                            if (pl.hasPermission("mcbouncer.mod")) {
                                pl.sendMessage(message);
                            }
                        }
                    }
                }
            }
        });
    }
    
}
