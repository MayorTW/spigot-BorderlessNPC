package tw.mayortw.blnpc.goal;

/*
 * Edited from net.citizensnpcs.api.ai.goals.WanderGoal
 */

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPC;

import tw.mayortw.blnpc.BorderlessNPCPlugin;

public class StrollNearHomeGoal extends BehaviorGoalAdapter implements Listener {
    private boolean forceFinish;
    private final NPC npc;
    private final Random random = new Random();

    public StrollNearHomeGoal(NPC npc) {
        this.npc = npc;
    }

    private Location findRandomHomePosition(Location home, int range) {
        Location found = null;

        for (int i = 0; i < 50; i++) {
            int x = home.getBlockX() + random.nextInt(2 * range) - range;
            int y = home.getBlockY() + random.nextInt(2 * range) - range;
            int z = home.getBlockZ() + random.nextInt(2 * range) - range;
            Block block = home.getWorld().getBlockAt(x, y, z);
            if (MinecraftBlockExaminer.validPosition(block)) {
                found = block.getLocation();
                break;
            }
        }
        return found;
    }

    @EventHandler
    public void onFinish(NavigationCompleteEvent event) {
        forceFinish = true;
    }

    @Override
    public void reset() {
        forceFinish = false;
        HandlerList.unregisterAll(this);
    }

    @Override
    public BehaviorStatus run() {
        if (!npc.getNavigator().isNavigating() || forceFinish) {
            npc.getNavigator().getLocalParameters().speedModifier(1f);
            return BehaviorStatus.SUCCESS;
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        if (!npc.isSpawned() || npc.getNavigator().isNavigating())
            return false;

        if(random.nextInt(200) != 0) return false;

        MetadataStore data = npc.data();

        int range;
        Location npcLoc = npc.getEntity().getLocation();
        Location home;

        if(data.has(BorderlessNPCPlugin.HOME_X_METADATA) &&
                data.has(BorderlessNPCPlugin.HOME_Y_METADATA) &&
                data.has(BorderlessNPCPlugin.HOME_Z_METADATA)) {
            home = new Location(npcLoc.getWorld(),
                    data.get(BorderlessNPCPlugin.HOME_X_METADATA),
                    data.get(BorderlessNPCPlugin.HOME_Y_METADATA),
                    data.get(BorderlessNPCPlugin.HOME_Z_METADATA));
        } else {
            home = npcLoc;
        }

        if(data.has(BorderlessNPCPlugin.TARGET_RANGE)) {
            range = data.get(BorderlessNPCPlugin.TARGET_RANGE);
        } else {
            range = 10;
        }

        Location dest = findRandomHomePosition(home, range);
        if (dest == null)
            return false;
        npc.getNavigator().setTarget(dest);
        npc.getNavigator().getLocalParameters().speedModifier(.8f);
        CitizensAPI.registerEvents(this);
        return true;
    }
}
