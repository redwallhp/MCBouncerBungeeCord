
package com.mcbouncer.bungee.listener;

import com.mcbouncer.bungee.MCBouncer;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;
import java.util.logging.Level;
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
                }
                catch (NetworkException ex) {
                    plugin.getLogger().log(Level.INFO, "Error looking up user on join", ex);
                }
                catch (APIException ex) {
                    plugin.getLogger().log(Level.INFO, "API Error while looking up user on join", ex);
                }
                finally {
                    event.completeIntent(plugin);
                }
            }
        });
    }
}
