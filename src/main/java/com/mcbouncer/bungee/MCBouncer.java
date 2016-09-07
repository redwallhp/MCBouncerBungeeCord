package com.mcbouncer.bungee;

import com.mcbouncer.api.MCBouncerAPI;
import com.mcbouncer.api.UserBan;
import com.mcbouncer.api.UserNote;
import com.mcbouncer.bungee.command.*;
import com.mcbouncer.bungee.database.BanTable;
import com.mcbouncer.bungee.database.NoteTable;
import com.mcbouncer.bungee.listener.PluginMessageListener;
import com.mcbouncer.bungee.listener.ProxiedPlayerListener;
import com.mcbouncer.bungee.listener.ServerConnectedListener;
import com.mcbouncer.exception.APIException;
import com.mcbouncer.exception.NetworkException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class MCBouncer extends Plugin {

    public MCBouncerAPI api;
    public MainConfig config;

	public Set<UserBan> cachedBans = new HashSet<UserBan>();

	public Connection conn;
	public BanTable banTable;
	public NoteTable noteTable;
    
    @Override
    public void onEnable() {
        config = new MainConfig(this);
        api = new MCBouncerAPI("http://mcbouncer.com", config.apikey);

        getProxy().getPluginManager().registerListener(this, new ProxiedPlayerListener(this));
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerConnectedListener(this));

		getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new KickCommand(this));
        getProxy().getPluginManager().registerCommand(this, new LookupCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new AddNoteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new DelNoteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MCBouncerCommand(this));

		if (config.useCacheDB) {
			setupDatabase();
			banTable = new BanTable(this);

			if (config.importBansOnStart) {
				getLogger().info("Importing bans.txt");
				importDatabase();
			}
		}

        super.onEnable();
    }

    @Override
    public void onDisable() {
		try {
			conn.close();
		} catch (SQLException e) {
			getLogger().log(Level.SEVERE, null, e);
		}

        super.onDisable();
    }

    public boolean setupDatabase() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + getDataFolder() + "/bans.db");
			conn.setAutoCommit(false);
			getLogger().info("Database opened successfully");

			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet tables = metaData.getTables(null, null, "bans", null);
			if (!tables.next()) {
				// Lets initialize the DB
				Statement stmt = conn.createStatement();
				stmt.execute("CREATE TABLE bans (uuid varchar(36), username varchar(16), reason varchar(128), unique (uuid) ON CONFLICT IGNORE, unique(username) ON CONFLICT IGNORE)");
				stmt.close();
			}

			return true;
		}
		catch (Exception e) {
			getLogger().log(Level.SEVERE, null, e);
		}

		return false;
    }

	public void importDatabase() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(getDataFolder() + "/bans.txt"));

			PreparedStatement stmt = conn.prepareStatement("INSERT INTO bans (uuid, username, reason) VALUES(?, ?, ?)");
			for (String line; (line = br.readLine()) != null;) {
				stmt.setString(2, line);
				stmt.execute();
			}
			conn.commit();

			br.close();
		} catch (Exception ex) {
			Logger.getLogger(MCBouncer.class.getName()).log(Level.SEVERE, null, ex);
		}
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
                        pl.sendMessage(message + " while on " + sender.getServer().getInfo().getName());
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

		if (api.isBanned(banee)) {
			baner.sendMessage("Player is already banned");
			return;
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
                        pl.sendMessage(message + " while on " + baner.getServer().getInfo().getName());
                    }
                }
            }

			if (config.useCacheDB) {
				try {
					PreparedStatement stmt = conn.prepareStatement("INSERT INTO bans (username) VALUES(?)");
					stmt.execute();
					conn.commit();
				} catch (SQLException ex) {
					Logger.getLogger(MCBouncer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
        }
    }

    public void unban(ProxiedPlayer sender, String toUnban) throws NetworkException, APIException {
		if (!api.isBanned(toUnban)) {
			sender.sendMessage("Player is not banned");
			return;
		}
		
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
        	UserBan ban = bans.get(i);
        	player.sendMessage(ChatColor.GREEN + "Ban #" + (i + 1) + ": " + ban.getServer() + " (" + ban.getIssuer() + ") [" + ban.getReason() + "] " + ban.getTime());
        }

        for (int i = 0; i < notes.size(); i++) {
        	UserNote note = notes.get(i);
            if (note.isGlobal()) {
            	player.sendMessage(ChatColor.GREEN + "Note #" + note.getNoteID().toString() + " - GLOBAL: " + note.getServer() + " (" + note.getIssuer() + ") [" + note.getNote() + "] " + note.getTime());
            } else {
            	player.sendMessage(ChatColor.GREEN + "Note #" + note.getNoteID().toString() + ": " + note.getServer() + " (" + note.getIssuer() + ") [" + note.getNote() + "] " + note.getTime());
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

	public Boolean isCachedBan(String baner) {
		for (UserBan userBan : this.cachedBans) {
			if (userBan.getUsername().equalsIgnoreCase(baner)) {
				return true;
			}
		}

		if (config.useCacheDB) {
			try {
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM bans WHERE username = ?");
				stmt.setString(1, baner);

				ResultSet results = stmt.executeQuery();
				return (results.getFetchSize() > 0);
			} catch (SQLException ex) {
				Logger.getLogger(MCBouncer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		return false;
	}
}
