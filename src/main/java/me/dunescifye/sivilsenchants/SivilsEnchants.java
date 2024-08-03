package me.dunescifye.sivilsenchants;

import me.dunescifye.sivilsenchants.listeners.PlayerBlockBreakListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SivilsEnchants extends JavaPlugin {

    @Override
    public void onEnable() {
        new PlayerBlockBreakListener().playerBlockBreakHandler(this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
