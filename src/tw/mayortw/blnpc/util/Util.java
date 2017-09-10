package tw.mayortw.blnpc.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.MetadataStore;

import tw.mayortw.blnpc.BorderlessNPCPlugin;

public class Util {

    public static final double DEFAULT_TARGET_RANGE = 10.0;
    public static final int DEFAULT_ATTACK_DAMAGE = 1;

    public static Location getHomeLocation(NPC npc) {

        MetadataStore data = npc.data();

        if(!data.has(BorderlessNPCPlugin.HOME_X_METADATA) ||
                !data.has(BorderlessNPCPlugin.HOME_Y_METADATA) ||
                !data.has(BorderlessNPCPlugin.HOME_Z_METADATA))
            return npc.getStoredLocation();

        return new Location(npc.getStoredLocation().getWorld(),
                data.get(BorderlessNPCPlugin.HOME_X_METADATA),
                data.get(BorderlessNPCPlugin.HOME_Y_METADATA),
                data.get(BorderlessNPCPlugin.HOME_Z_METADATA));
    }

    public static double getTargetRange(NPC npc) {
        if(npc.data().has(BorderlessNPCPlugin.TARGET_RANGE_METADATA))
            return npc.data().get(BorderlessNPCPlugin.TARGET_RANGE_METADATA);
        else
            return DEFAULT_TARGET_RANGE;
    }

    public static int getAttackDamage(NPC npc) {
        if(npc.data().has(BorderlessNPCPlugin.ATTACK_DAMAGE_METADATA))
            return npc.data().get(BorderlessNPCPlugin.ATTACK_DAMAGE_METADATA);
        else
            return DEFAULT_ATTACK_DAMAGE;
    }

    // Edited from net.citizensnpcs.trait.LookClose
    public static boolean canSeeTarget(NPC npc, Entity target) {
        return npc.getEntity() instanceof LivingEntity
            ? ((LivingEntity) npc.getEntity()).hasLineOfSight(target) : true;
    }
}
