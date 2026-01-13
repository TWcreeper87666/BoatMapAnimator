package boatmapanimator;

import org.bukkit.plugin.java.JavaPlugin;

public class BoatMapAnimatorPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("boatmap").setExecutor(new BoatMapCommand(this));
        getLogger().info("BoatMapAnimator Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BoatMapAnimator Plugin Disabled");
    }
}
