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
import net.citizensnpcs.api.npc.NPC;

import tw.mayortw.blnpc.BorderlessNPCPlugin;

public class StayNearHomeGoal extends BehaviorGoalAdapter implements Listener {
    private boolean forceFinish;
    private final NPC npc;
    private final Random random = new Random();
    private final int xrange;
    private final int yrange;

    public StayNearHomeGoal(NPC npc) {
        this(npc, 10, 2);
    }
    public StayNearHomeGoal(NPC npc, int xrange, int yrange) {
        this.npc = npc;
        this.xrange = xrange;
        this.yrange = yrange;
    }

    private Location findRandomHomePosition(Location home) {
        Location found = null;

        for (int i = 0; i < 10; i++) {
            int x = home.getBlockX() + random.nextInt(2 * xrange) - xrange;
            int y = home.getBlockY() + random.nextInt(2 * yrange) - yrange;
            int z = home.getBlockZ() + random.nextInt(2 * xrange) - xrange;
            Block block = home.getWorld().getBlockAt(x, y, z);
            if (MinecraftBlockExaminer.canStandOn(block)
                    && MinecraftBlockExaminer.canStandIn(block.getRelative(BlockFace.UP).getType())) {
                found = block.getLocation().add(0, 1, 0);
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
        if(!npc.data().has(BorderlessNPCPlugin.HOME_X_METADATA) ||
                !npc.data().has(BorderlessNPCPlugin.HOME_Y_METADATA) ||
                !npc.data().has(BorderlessNPCPlugin.HOME_Z_METADATA))
            return false;

        Location npcLoc = npc.getEntity().getLocation();
        Location home = new Location(npcLoc.getWorld(),
                npc.data().get(BorderlessNPCPlugin.HOME_X_METADATA),
                npc.data().get(BorderlessNPCPlugin.HOME_Y_METADATA),
                npc.data().get(BorderlessNPCPlugin.HOME_Z_METADATA));
        if(npcLoc.distanceSquared(home) < xrange * xrange) return false;

        Location dest = findRandomHomePosition(home);
        if (dest == null)
            return false;
        npc.getNavigator().setTarget(dest);
        npc.getNavigator().getLocalParameters().speedModifier(.8f);
        CitizensAPI.registerEvents(this);
        return true;
    }
}
