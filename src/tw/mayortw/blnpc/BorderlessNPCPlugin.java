package tw.mayortw.blnpc;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitInfo;

import tw.mayortw.blnpc.trait.BorderlessTrait;

import java.util.HashMap;
import java.util.Map;

public class BorderlessNPCPlugin extends JavaPlugin implements Listener {

    public static final String HOME_X_METADATA = "home_x";
    public static final String HOME_Y_METADATA = "home_y";
    public static final String HOME_Z_METADATA = "home_z";

    private Map<CommandSender, NPC> selectedNPCs = new HashMap<>();

    @Override
    public void onEnable() {
        if(getServer().getPluginManager().getPlugin("Citizens") == null || getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false) {
            getLogger().log(java.util.logging.Level.SEVERE, "Citizens 2.0 not found or not enabled");
            getServer().getPluginManager().disablePlugin(this); 
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);

        TraitFactory traitFact = CitizensAPI.getTraitFactory();
        traitFact.registerTrait(TraitInfo.create(BorderlessTrait.class).withName("borderless")); 
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(cmd.getName().equalsIgnoreCase("blnpc")) {

            NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);

            if(npc == null) {
                sender.sendMessage("Select a NPC first");
                return false;
            }

            sender.sendMessage("Right click on the block");
            selectedNPCs.put(sender, npc);

            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent eve) {

        Player player = eve.getPlayer();

        if(eve.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if(selectedNPCs.containsKey(player)) {
                Location loc = eve.getClickedBlock().getLocation();
                NPC npc = selectedNPCs.get(player);

                npc.data().setPersistent(HOME_X_METADATA, loc.getX());
                npc.data().setPersistent(HOME_Y_METADATA, loc.getY());
                npc.data().setPersistent(HOME_Z_METADATA, loc.getZ());

                player.sendMessage("Set to " + loc.toString());
                selectedNPCs.remove(player);
                eve.setCancelled(true);
            }
        }
    }
}
