package com.mcbouncer.bungee.listener;

import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;

public class ServerConnectedListener implements Listener {

    private MCBouncer plugin;

    public ServerConnectedListener(MCBouncer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerConnect(final ServerConnectedEvent event) {
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
                try {
                    ProxiedPlayer player = event.getPlayer();

                    int numBans = plugin.api.getBanCount(player.getName());
                    int numNotes = plugin.api.getNoteCount(player.getName());

                    if (numBans > 0 || numNotes > 0) {
                        String message = player.getName() + " has ";
                        if (numNotes == 0) {
                            message += numBans + " ban" + (numBans == 1 ? "." : "s.");
                        } else if (numBans == 0) {
                            message += numNotes + " note" + (numNotes == 1 ? "." : "s.");
                        } else {
                            message += numBans + " ban" + (numBans == 1 ? "" : "s") + " and " + numNotes + " note" + (numNotes == 1 ? "." : "s.");
                        }
                        for (ProxiedPlayer pl : plugin.getProxy().getPlayers()) {
                            if (pl.getServer() != null) {
                                if (pl.hasPermission("mcbouncer.mod") && pl.getServer().getInfo().getName() == event.getServer().getInfo().getName()) {
                                    pl.sendMessage(ChatColor.GREEN + message);
                                }
                            }
                        }
                    }
                } catch (NetworkException ex) {
                    plugin.getLogger().log(Level.INFO, "Error looking up user on join", ex);
                } catch (APIException ex) {
                    plugin.getLogger().log(Level.INFO, "API Error while looking up user on join", ex);
                }
            }
        });
    }

}
