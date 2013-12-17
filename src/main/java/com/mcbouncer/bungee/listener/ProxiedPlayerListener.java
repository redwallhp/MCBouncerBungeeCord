
package com.mcbouncer.bungee.listener;

import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;

import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxiedPlayerListener implements Listener {

    private MCBouncer plugin;
    
    public ProxiedPlayerListener(MCBouncer plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(final LoginEvent event) {
        event.registerIntent(plugin);
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
                try {
                    String username = event.getConnection().getName();
                    String ip = event.getConnection().getAddress().getAddress().getHostAddress();
                    plugin.api.updateUser(username, ip);
                    
                    if (plugin.api.isBanned(username)) {
                        String reason = plugin.api.getBanReason(username);
                        event.setCancelled(true);
                        event.setCancelReason("Banned: " + reason);
                    }

                    int numBans = plugin.api.getBanCount(username);
                    int numNotes = plugin.api.getNoteCount(username);

        			if (numBans > 0 || numNotes > 0) {
        				String message = username + " has ";
        				if (numNotes == 0) {
        					message += numBans + " ban" + (numBans == 1 ? "." : "s.");
        				} else if (numBans == 0) {
        					message += numNotes + " note" + (numNotes == 1 ? "." : "s.");
        				} else {
        					message += numBans + " ban" + (numBans == 1 ? "" : "s") + " and " + numNotes + " note" + (numNotes == 1 ? "." : "s.");
        				}
                        for (ProxiedPlayer pl : plugin.getProxy().getPlayers()) {
                            if (pl.hasPermission("mcbouncer.mod")) {
                                pl.sendMessage(ChatColor.GREEN + message);
                            }
                        }
        			}
                }
                catch (NetworkException ex) {
                    plugin.getLogger().log(Level.INFO, "Error looking up user on join", ex);
                    event.setCancelled(true);
                    event.setCancelReason("Error lookup up user");
                }
                catch (APIException ex) {
                    plugin.getLogger().log(Level.INFO, "API Error while looking up user on join", ex);
                    event.setCancelled(true);
                    event.setCancelReason("Error lookup up user");
                }
                finally {
                    event.completeIntent(plugin);
                }
            }
        });
    }
}
