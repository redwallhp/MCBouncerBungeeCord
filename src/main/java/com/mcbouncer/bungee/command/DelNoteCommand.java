package com.mcbouncer.bungee.command;

import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DelNoteCommand extends Command {

    MCBouncer plugin;

    public DelNoteCommand(MCBouncer plugin) {
        super("delnote");
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {

            public void run() {
                if (!sender.hasPermission("mcbouncer.mod")) {
                    sender.sendMessage(ChatColor.RED + "You need permission to run that command.");
                    return;
                }

                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Syntax:  /delnote <note id>");
                    return;
                }

                try {
                    int note_id = Integer.parseInt(args[0]);
                    plugin.delnote((ProxiedPlayer) sender,  note_id);
	            } catch (NetworkException ex) {
	                sender.sendMessage(ChatColor.RED + "Network Timeout, could, not reach mcbouncer.com");
	            } catch (APIException ex) {
	                sender.sendMessage(ChatColor.RED + "An API Error Occured.");
	            } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "Syntax:  /delnote <note id>");
	            }
            }
        });
    }
}
