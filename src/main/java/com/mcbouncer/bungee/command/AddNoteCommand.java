package com.mcbouncer.bungee.command;

import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;
import com.mcbouncer.util.MiscUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AddNoteCommand extends Command {

    MCBouncer plugin;

    public AddNoteCommand(MCBouncer plugin) {
        super("addnote");
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

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Syntax:  /addnote <user> <note>");
                    return;
                }
                String user = args[0];
                String note = MiscUtils.join(args, " ", 1, args.length);

                try {
                	plugin.addnote((ProxiedPlayer) sender, user, note);
	            } catch (NetworkException ex) {
	                sender.sendMessage(ChatColor.RED + "Network Timeout, could, not reach mcbouncer.com");
	            } catch (APIException ex) {
	                sender.sendMessage(ChatColor.RED + "An API Error Occured.");
	            }
            }
        });
    }
}
