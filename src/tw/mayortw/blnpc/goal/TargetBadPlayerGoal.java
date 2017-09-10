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
import org.bukkit.permissions.Permissible;

import tw.mayortw.blnpc.BorderlessNPCPlugin;

public class TargetBadPlayerGoal extends BehaviorGoalAdapter {
    private final boolean aggressive;
    private boolean finished;
    private final NPC npc;
    private CancelReason reason;
    private Entity target;
    private String targetPerm;

    public TargetBadPlayerGoal(NPC npc, String targetPerm, boolean aggressive) {
        this.npc = npc;
        this.targetPerm = targetPerm;
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

        if(target != null && !((Permissible) target).isPermissionSet(targetPerm)) {
            target = null;
            npc.getNavigator().cancelNavigation();
        }

        if (finished) {
            return reason == null ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        if(targetPerm == null || !npc.isSpawned())
            return false;

        int range;
        if(npc.data().has(BorderlessNPCPlugin.TARGET_RANGE)) {
            range = npc.data().get(BorderlessNPCPlugin.TARGET_RANGE);
        } else {
            range = 10;
        }

        Collection<Entity> nearby = npc.getEntity().getNearbyEntities(range, range, range);
        this.target = null;
        for(Entity entity : nearby) {
            if(entity instanceof Permissible &&
                    ((Permissible) entity).isPermissionSet(targetPerm)) {
                target = entity;
                break;
            }
        }
        if (target != null) {
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
