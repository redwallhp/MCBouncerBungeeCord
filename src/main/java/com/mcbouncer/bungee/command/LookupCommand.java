package com.mcbouncer.bungee.command;

import com.mcbouncer.api.UserBan;
import com.mcbouncer.api.UserNote;
import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;
import java.util.List;
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
                        sender.sendMessage(ChatColor.RED + "Syntax:  /lookup <username>");
                        return;
                    }
                    String username = args[0];
                    
                    boolean success = false;
                    
                    List<UserBan> bans = plugin.api.getBans(username);
                    List<UserNote> notes = plugin.api.getNotes(username);
                    
                    
                    sender.sendMessage(ChatColor.AQUA + username + " has " + bans.size() + " ban" + (bans.size() == 1 ? "" : "s") + " and " + notes.size() + " note" + (notes.size() == 1 ? "" : "s"));
                    
                    for (int i = 0; i < bans.size() && i < 5; i++) {
                        sender.sendMessage(ChatColor.GREEN + "Ban #" + (i + 1) + ": " + bans.get(i).getServer() + " (" + bans.get(i).getIssuer() + ") [" + bans.get(i).getReason() + "]");
                    }
                    
                    for (int i = 0; i < notes.size(); i++) {
                        if (notes.get(i).isGlobal()) {
                            sender.sendMessage(ChatColor.GREEN + "Note #" + notes.get(i).getNoteID().toString() + " - GLOBAL: " + notes.get(i).getServer() + " (" + notes.get(i).getIssuer() + ") [" + notes.get(i).getNote() + "]");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Note #" + notes.get(i).getNoteID().toString() + ": " + notes.get(i).getServer() + " (" + notes.get(i).getIssuer() + ") [" + notes.get(i).getNote() + "]");
                        }
                    }
                } catch (NetworkException ex) {
                    sender.sendMessage(ChatColor.RED + "Network error, could, not reach mcbouncer.com");
                } catch (APIException ex) {
                    sender.sendMessage(ChatColor.RED + ex.getMessage());
                }
            }
        });
    }
    
}
