package tw.mayortw.blenemies.goal;

// Mostly copied from net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal
// Excludes NPCs in this goal

import java.util.Collection;
//import java.util.Set;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class TargetNearbyPlayerGoal extends BehaviorGoalAdapter {
    private final boolean aggressive;
    private boolean finished;
    private final NPC npc;
    private final double radius;
    private CancelReason reason;
    private Entity target;
    //private final Set<EntityType> targets;

    public TargetNearbyPlayerGoal(NPC npc, boolean aggressive, double radius) {
        this.npc = npc;
        //this.targets = targets;
        this.aggressive = aggressive;
        this.radius = radius;
    }

    @Override
    public void reset() {
        npc.getNavigator().cancelNavigation();
        target = null;
        finished = false;
        reason = null;
    }

    @Override
    public BehaviorStatus run() {

        npc.faceLocation(target.getLocation());

        if(target.getLocation().distanceSquared(npc.getEntity().getLocation()) > radius * radius) {
            reason = null;
            finished = true;
        }

        if (finished) {
            return reason == null ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        //if (targets.size() == 0 || !npc.isSpawned())
        if (!npc.isSpawned())
            return false;
        Collection<Entity> nearby = npc.getEntity().getNearbyEntities(radius, radius, radius);
        this.target = null;
        for (Entity entity : nearby) {
            if(entity.getType() == EntityType.PLAYER &&
                    (((Player) entity).getGameMode() == GameMode.ADVENTURE ||
                     ((Player) entity).getGameMode() == GameMode.SURVIVAL)) {
                target = entity;
                break;
            }
        }
        if (target != null && !target.hasMetadata("NPC")) {
            npc.getNavigator().setTarget(target, aggressive);

            NavigatorParameters params = npc.getNavigator().getLocalParameters();
            params.avoidWater(false);
            params.attackStrategy(null); // Use default
            params.addSingleUseCallback(new NavigatorCallback() {
                @Override
                public void onCompletion(CancelReason cancelReason) {
                    reason = cancelReason;
                    finished = true;
                }
            });
            return true;
        }
        return false;
    }
}

