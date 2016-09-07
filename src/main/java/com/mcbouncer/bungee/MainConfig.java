package com.mcbouncer.bungee;

import java.io.File;
import java.util.logging.Level;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

public class MainConfig extends Config {
    
    private MCBouncer plugin;
    private File config;
    
    public MainConfig(MCBouncer plugin) {
        this.plugin = plugin;
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
        CONFIG_HEADER = new String[]{"MCBouncer Config file"};
        try {
            this.init();
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load config!", e);
        }
    }
    
    @Comment("MCBouncer API Key")
    public String apikey = "REPLACE";
    
    @Comment("If no kick message is supplied, this is used")
    public String defaultKickMessage = "Kicked by an admin.";
    
    @Comment("If no ban message is supplied, this is used")
    public String defaultBanMessage = "Banned for rule violation.";
    
    @Comment("If a user has more bans than stated they will not be allowed in. (-1 Turns this feature off)")
    public Integer numBansDisallow = -1;
    
    @Comment("Whether or not to show messgaes to all the users on the server when a user is banned.")
    public Boolean showBanMessages = false;
    
    @Comment("Whether or not to show messgaes to all the users on the server when a note is added to a user.")
    public Boolean showNoteMessages = false;

	@Comment("Whether or not to use a local cache to store bans")
	public Boolean useCacheDB = false;

	@Comment("Wehether or not to import bans.txt on startup")
	public Boolean importBansOnStart = false;
}
