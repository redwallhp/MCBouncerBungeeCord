package com.mcbouncer.bungee;

import java.util.List;

import com.mcbouncer.api.MCBouncerAPI;
import com.mcbouncer.api.UserBan;
import com.mcbouncer.api.UserNote;
import com.mcbouncer.bungee.command.*;
import com.mcbouncer.bungee.listener.PluginMessageListener;
import com.mcbouncer.bungee.listener.ProxiedPlayerListener;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class MCBouncer extends Plugin {

    public MCBouncerAPI api;
    public MainConfig config;
    
    @Override
    public void onEnable() {
        config = new MainConfig(this);
        api = new MCBouncerAPI("http://mcbouncer.com", config.apikey);
        getProxy().getPluginManager().registerListener(this, new ProxiedPlayerListener(this));
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));
        getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new KickCommand(this));
        getProxy().getPluginManager().registerCommand(this, new LookupCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new AddNoteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new DelNoteCommand(this));
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public void kick(ProxiedPlayer sender, ProxiedPlayer toKick, String reason) {
        if (toKick != null) {
        	if (reason.trim().length() == 0)
        		reason = config.defaultKickMessage;

        	toKick.disconnect("Kicked: " + reason);

            String message = ChatColor.GREEN + "User " + toKick + " has been kicked by " + sender.getName() + ". (" + reason + ")";
            getLogger().info(ChatColor.stripColor(message));
            if (config.showBanMessages) {
                getProxy().broadcast(message);
            }
            else {
                for (ProxiedPlayer pl : getProxy().getPlayers()) {
                    if (pl.hasPermission("mcbouncer.mod")) {
                        pl.sendMessage(message);
                    }
                }
            }
        }
    }

    public void ban(ProxiedPlayer baner, String banee, String reason) throws NetworkException, APIException {
        ProxiedPlayer p = getProxy().getPlayer(banee);
        if (p != null) {
            p.disconnect("Banned: " + reason);
        }

        boolean success = false;
        success = api.addBan(baner.getName(), banee, reason);

        if (success) {
            String message = ChatColor.GREEN + "User " + banee + " has been banned by " + baner.getName() + ". (" + reason + ")";
            getLogger().info(ChatColor.stripColor(message));
            if (config.showBanMessages) {
                getProxy().broadcast(message);
            }
            else {
                for (ProxiedPlayer pl : getProxy().getPlayers()) {
                    if (pl.hasPermission("mcbouncer.mod")) {
                        pl.sendMessage(message);
                    }
                }
            }
        }
    }

    public void unban(ProxiedPlayer sender, String toUnban) throws NetworkException, APIException {
        boolean success = api.removeBan(toUnban);

        if (success) {
	        String message = ChatColor.GREEN + "User " + toUnban + " unbanned by " + sender.getName();
	        for (ProxiedPlayer pl : getProxy().getPlayers()) {
	            if (pl.hasPermission("mcbouncer.mod")) {
	                pl.sendMessage(message);
	            }
	        }
        }
    }

    public void lookup(ProxiedPlayer player, String username) throws NetworkException, APIException {
        List<UserBan> bans = api.getBans(username);
        List<UserNote> notes = api.getNotes(username);

        player.sendMessage(ChatColor.AQUA + username + " has " + bans.size() + " ban" + (bans.size() == 1 ? "" : "s") + " and " + notes.size() + " note" + (notes.size() == 1 ? "" : "s"));

        for (int i = 0; i < bans.size() && i < 5; i++) {
        	player.sendMessage(ChatColor.GREEN + "Ban #" + (i + 1) + ": " + bans.get(i).getServer() + " (" + bans.get(i).getIssuer() + ") [" + bans.get(i).getReason() + "]");
        }

        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).isGlobal()) {
            	player.sendMessage(ChatColor.GREEN + "Note #" + notes.get(i).getNoteID().toString() + " - GLOBAL: " + notes.get(i).getServer() + " (" + notes.get(i).getIssuer() + ") [" + notes.get(i).getNote() + "]");
            } else {
            	player.sendMessage(ChatColor.GREEN + "Note #" + notes.get(i).getNoteID().toString() + ": " + notes.get(i).getServer() + " (" + notes.get(i).getIssuer() + ") [" + notes.get(i).getNote() + "]");
            }
        }
    }

    public void addnote(ProxiedPlayer sender, String player, String note) throws NetworkException, APIException {
    	api.addNote(sender.getName(), player, note);
    	sender.sendMessage(ChatColor.GREEN + "Note added to " + player + " successfully.");
    	getLogger().info(sender.getName() + " added note to " + player + " - " + note);
    }

    public void delnote(ProxiedPlayer sender, int note_id) throws NetworkException, APIException {
    	api.removeNote(note_id, sender.getName());
    	sender.sendMessage(ChatColor.GREEN + "Note removed successfully.");
    	getLogger().info(sender.getName() + " removed note ID " + note_id);
    }
}
