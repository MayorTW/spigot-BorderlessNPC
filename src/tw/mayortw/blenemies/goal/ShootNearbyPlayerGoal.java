package tw.mayortw.blenemies.goal;

// Copied and edited from net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal
// Excludes NPCs in this goal

import java.util.Collection;
//import java.util.Set;

import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class ShootNearbyPlayerGoal extends BehaviorGoalAdapter {

    private final boolean aggressive;
    private boolean finished;
    private final NPC npc;
    private final double radius;
    private final double range;
    private CancelReason reason;
    private Entity target;
    private ArrowAttackStrategy arrowAttack;
    //private final Set<EntityType> targets;

    public ShootNearbyPlayerGoal(NPC npc, boolean aggressive, double radius, double range) {
        this.npc = npc;
        //this.targets = targets;
        this.aggressive = aggressive;
        this.radius = radius;
        this.range = range;

        arrowAttack = new ArrowAttackStrategy(npc);
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
            //if (targets.contains(entity.getType())) {
            if(entity.getType() == EntityType.PLAYER) {
                target = entity;
                break;
            }
        }
        if (target != null && !target.hasMetadata("NPC")) {
            npc.getNavigator().setTarget(target, aggressive);

            NavigatorParameters params = npc.getNavigator().getLocalParameters();
            params.attackRange(range * range);
            params.attackStrategy(arrowAttack);
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

    private class ArrowAttackStrategy implements AttackStrategy {

        private NPC npc;

        ArrowAttackStrategy(NPC npc) {
            this.npc = npc;
        }

        @Override
        public boolean handle(LivingEntity attacker, LivingEntity target) {
            if(npc.getEntity().equals(attacker)) {
                npc.getNavigator().cancelNavigation();
                return true;
            }
            return false;
        }
    }
}

