package tw.mayortw.blenemies;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Equipment;
import tw.mayortw.blenemies.trait.*;

public class BlEnemiesPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if(getServer().getPluginManager().getPlugin("Citizens") == null || getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false) {
            getLogger().log(java.util.logging.Level.SEVERE, "Citizens 2.0 not found or not enabled");
            getServer().getPluginManager().disablePlugin(this); 
            return;
        }

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BanditTrait.class).withName("bandit")); 
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ForestRobberTrait.class).withName("foresthorse")); 
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MercenaryTrait.class).withName("mercenary")); 
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(OgreTrait.class).withName("ogre")); 
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PirateTrait.class).withName("pirate")); 
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SoundHorseTrait.class).withName("soundhorse")); 
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SoundRiderTrait.class).withName("soundrider")); 
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(cmd.getName().equalsIgnoreCase("kt")) {
            if(sender instanceof Entity) {
                
                NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Herobrine");

                npc.addTrait(BanditTrait.class);
                npc.addTrait(Equipment.class);

                npc.spawn(((Entity) sender).getLocation());

                npc.setProtected(false);
                npc.getTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.STONE_SWORD));
                npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
            }
            return true;
        }
        return false;
    }
}
