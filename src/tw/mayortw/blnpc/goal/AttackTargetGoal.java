package tw.mayortw.blnpc.goal;

/*
 * Edited from net.citizensnpcs.api.ai.goals.MoveToGoal
 * Target player that has certain permission
 */

import java.util.Collection;

import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;

import tw.mayortw.blnpc.util.TargetRule;
import tw.mayortw.blnpc.util.Util;

public class AttackTargetGoal extends BehaviorGoalAdapter {

    private final boolean aggressive;
    private boolean finished;
    private final NPC npc;
    private CancelReason reason;
    private Entity target;

    public AttackTargetGoal(NPC npc, boolean aggressive) {
        this.npc = npc;
        this.aggressive = aggressive;
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

        double range = Util.getTargetRange(npc);

        if(target != null && !TargetRule.getInstance().isTarget(target) ||
                target.getLocation().distanceSquared(Util.getHomeLocation(npc))
                    > range * range) {
            finished = true;
        }

        if (finished) {
            return reason == null ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        if(!npc.isSpawned())
            return false;

        double range = Util.getTargetRange(npc);

        Collection<Entity> nearby = npc.getEntity().getNearbyEntities(range, range, range);
        this.target = null;
        for(Entity entity : nearby) {
            if(TargetRule.getInstance().isTarget(entity) &&
                    (target == null ||
                    entity.getLocation().distanceSquared(npc.getStoredLocation()) <
                    target.getLocation().distanceSquared(npc.getStoredLocation()))) {
                target = entity;
            }
        }

        if (target != null && Util.canSeeTarget(npc, target) &&
                target.getLocation().distanceSquared(Util.getHomeLocation(npc))
                    < range * range) {

            if(npc.getNavigator().isNavigating())
                npc.getDefaultGoalController().cancelCurrentExecution();

            npc.getNavigator().setTarget(target, aggressive);
            npc.getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
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
