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
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Equipment;
import tw.mayortw.blenemies.trait.*;

public class BlEnemiesPlugin extends JavaPlugin {

    private TraitFactory traitFact;

    @Override
    public void onEnable() {
        if(getServer().getPluginManager().getPlugin("Citizens") == null || getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false) {
            getLogger().log(java.util.logging.Level.SEVERE, "Citizens 2.0 not found or not enabled");
            getServer().getPluginManager().disablePlugin(this); 
            return;
        }

        traitFact = CitizensAPI.getTraitFactory();

        traitFact.registerTrait(TraitInfo.create(BanditTrait.class).withName("bandit")); 
        traitFact.registerTrait(TraitInfo.create(ForestRobberTrait.class).withName("forestrobber")); 
        traitFact.registerTrait(TraitInfo.create(MercenaryTrait.class).withName("mercenary")); 
        traitFact.registerTrait(TraitInfo.create(OrcTrait.class).withName("orc")); 
        traitFact.registerTrait(TraitInfo.create(PirateTrait.class).withName("pirate")); 
        traitFact.registerTrait(TraitInfo.create(SoundHorseTrait.class).withName("soundhorse")); 
        traitFact.registerTrait(TraitInfo.create(SoundRiderTrait.class).withName("soundrider")); 
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(cmd.getName().equalsIgnoreCase("kt")) {
            if(sender instanceof Entity) {
                if(args.length > 0) {

                    Trait trait = traitFact.getTrait(args[0]);
                    if(trait != null) {

                        if(args[0].equalsIgnoreCase("soundhorse") || args[0].equalsIgnoreCase("soundrider")) {

                            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "soundrider");
                            NPC horse = CitizensAPI.getNPCRegistry().createNPC(EntityType.HORSE, "soundhorse");

                            npc.addTrait(SoundRiderTrait.class);
                            npc.addTrait(Equipment.class);
                            horse.addTrait(SoundHorseTrait.class);

                            npc.spawn(((Entity) sender).getLocation());
                            horse.spawn(((Entity) sender).getLocation());

                            horse.setProtected(false);
                            horse.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
                            horse.data().setPersistent(NPC.COLLIDABLE_METADATA, true);

                            npc.setProtected(false);
                            npc.getTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.BOW));
                            npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
                            npc.data().setPersistent(NPC.COLLIDABLE_METADATA, true);

                            horse.getEntity().addPassenger(npc.getEntity());

                            return true;
                        }

                        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, args[0]);

                        npc.addTrait(trait);
                        npc.addTrait(Equipment.class);

                        npc.spawn(((Entity) sender).getLocation());

                        npc.setProtected(false);
                        npc.getTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.BOW));
                        npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
                        npc.data().setPersistent(NPC.COLLIDABLE_METADATA, true);

                        return true;
                    }
                }
            }
        }
        return false;
    }
}
