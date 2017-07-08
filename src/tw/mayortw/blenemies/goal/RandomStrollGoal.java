package tw.mayortw.blenemies.goal;

/*
 * Mostly copied from net.citizensnpcs.api.ai.goals.WanderGoal
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

public class RandomStrollGoal extends BehaviorGoalAdapter implements Listener {
    private boolean forceFinish;
    private final NPC npc;
    private final Random random = new Random();
    private final int xrange;
    private final int yrange;

    public RandomStrollGoal(NPC npc) {
        this(npc, 10, 2);
    }
    public RandomStrollGoal(NPC npc, int xrange, int yrange) {
        this.npc = npc;
        this.xrange = xrange;
        this.yrange = yrange;
    }

    private Location findRandomPosition() {
        Location base = npc.getEntity().getLocation();
        Location found = null;
        for (int i = 0; i < 10; i++) {
            int x = base.getBlockX() + random.nextInt(2 * xrange) - xrange;
            int y = base.getBlockY() + random.nextInt(2 * yrange) - yrange;
            int z = base.getBlockZ() + random.nextInt(2 * xrange) - xrange;
            Block block = base.getWorld().getBlockAt(x, y, z);
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
        if(random.nextInt(20) != 0) return false;
        Location dest = findRandomPosition();
        if (dest == null)
            return false;
        npc.getNavigator().setTarget(dest);
        npc.getNavigator().getLocalParameters().speedModifier(.8f);
        CitizensAPI.registerEvents(this);
        return true;
    }
}
