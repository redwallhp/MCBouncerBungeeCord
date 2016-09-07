
package com.mcbouncer.bungee.listener;

import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;

import java.util.logging.Level;
import java.util.logging.Logger;

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
				boolean isBanned = false;
				String reason = plugin.config.defaultBanMessage;
				final String username = event.getConnection().getName();
				final String ip = event.getConnection().getAddress().getAddress().getHostAddress();

                try {
					plugin.getLogger().info("Looking up user " + username);
					isBanned = plugin.api.isBanned(username);
					reason = plugin.api.getBanReason(username);

					plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
						public void run() {
							try {
								plugin.api.updateUser(username, ip);
							} catch (Exception e) {	}
						}
					});
                }
                catch (NetworkException ex) {
                    plugin.getLogger().log(Level.INFO, "Network error while looking up user " + username, ex);
                }
                catch (APIException ex) {
                    plugin.getLogger().log(Level.INFO, "API error while looking up user " + username, ex);
                }
                finally {
					if (isBanned || plugin.isCachedBan(username)) {
						event.setCancelled(true);
						event.setCancelReason("Banned: " + reason);
					}

                    event.completeIntent(plugin);
                }
            }
        });
    }
}
