package tw.mayortw.blenemies.goal;

// Copied and edited from net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal
// Excludes NPCs in this goal

import java.util.Collection;

import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.PlayerAnimation;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

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

        double distance = target.getLocation().distanceSquared(npc.getEntity().getLocation());
        if(distance  <= range * range &&
                npc.getEntity() instanceof LivingEntity && target instanceof LivingEntity) {
            // Navigator doesn't let me shoot when target is higher
            // So I handle it myself
            arrowAttack.handle((LivingEntity) npc.getEntity(), (LivingEntity) target);

        }

        if(distance > radius * radius || distance <= range * range) {
            npc.getNavigator().cancelNavigation();
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
        if (!npc.isSpawned())
            return false;
        Collection<Entity> nearby = npc.getEntity().getNearbyEntities(radius, radius, radius);
        this.target = null;
        for (Entity entity : nearby) {
            if(entity.getType() == EntityType.PLAYER) {
                target = entity;
                break;
            }
        }
        if (target != null && !target.hasMetadata("NPC")) {
            npc.getNavigator().setTarget(target, aggressive);

            NavigatorParameters params = npc.getNavigator().getLocalParameters();
            params.avoidWater(false);
            //params.attackRange(range * range);
            //params.attackStrategy(arrowAttack);
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

                Location atkLoc = attacker.getEyeLocation();
                Location tgtLoc = target.getEyeLocation();

                npc.faceLocation(tgtLoc);

                if(attacker.getNoDamageTicks() == 0) {
                    Projectile arrow = (Projectile) attacker.getWorld().spawnEntity(atkLoc.clone().add(atkLoc.getDirection().normalize()), EntityType.ARROW);

                    arrow.setShooter(attacker);

                    arrow.setVelocity(new Vector(
                                tgtLoc.getX() - atkLoc.getX(),
                                tgtLoc.getY() - atkLoc.getY(),
                                tgtLoc.getZ() - atkLoc.getZ()).normalize().multiply(2.2));

                    attacker.setNoDamageTicks(60);
                    useItem(attacker);
                }
                return true;
            }
            return false;
        }

        public void useItem(Entity entity) {
            if (entity instanceof Player) {
                PlayerAnimation.START_USE_MAINHAND_ITEM.play((Player) entity);
            }
        }

    }
}
