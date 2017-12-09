package tw.mayortw.blnpc.goal;

/*
 * Edited from net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal
 */

import java.util.Collection;

import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.PlayerAnimation;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import tw.mayortw.blnpc.util.TargetRule;
import tw.mayortw.blnpc.util.Util;

public class ShootTargetGoal extends BehaviorGoalAdapter {

    private final static int SHOOT_CD = 60;

    private boolean finished;
    private final NPC npc;
    private Entity target;

    private int shootCoolDown;

    public ShootTargetGoal(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void reset() {
        target = null;
        finished = false;
    }

    @Override
    public BehaviorStatus run() {

        double range = Util.getTargetRange(npc);

        if(target != null && !TargetRule.getInstance().isTarget(target) ||
                target.getLocation().distanceSquared(Util.getHomeLocation(npc))
                    > range * range || target.isDead() ||
                    !Util.canSeeTarget(npc, target)) {
            finished = true;
        } else {
            shootTarget();
        }

        if (finished) {
            return BehaviorStatus.SUCCESS;
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        if (!npc.isSpawned() || npc.getNavigator().isNavigating())
            return false;

        double range = Util.getTargetRange(npc);
        Collection<Entity> nearby = npc.getEntity().getNearbyEntities(range, range, range);

        this.target = null;
        for (Entity entity : nearby) {
            if(TargetRule.getInstance().isTarget(entity) &&
                    (target == null ||
                    entity.getLocation().distanceSquared(npc.getStoredLocation()) <
                    target.getLocation().distanceSquared(npc.getStoredLocation())) &&
                    entity.getLocation().distanceSquared(Util.getHomeLocation(npc))
                    <= range * range && //getNearbyEntities uses a box, but i'm using a circle
                    Util.canSeeTarget(npc, entity)) {
                target = entity;
                useItem(npc.getEntity());
                shootCoolDown = 15; //Time to animate
                break;
            }
        }

        return target != null;
    }

    private void shootTarget() {

        if(!(npc.getEntity() instanceof LivingEntity))
            return;

        LivingEntity shooter = (LivingEntity) npc.getEntity();

        Location shtLoc = shooter.getEyeLocation();
        Location tgtLoc = target instanceof LivingEntity ?
            ((LivingEntity) target).getEyeLocation() : target.getLocation();

        npc.faceLocation(target.getLocation()); //tgtLoc can be eye location

        if(shootCoolDown == 15)
            useItem(shooter);

        if(shootCoolDown <= 0) {

            double distance = shtLoc.distanceSquared(tgtLoc);

            Projectile arrow = (Projectile) shooter.getWorld().spawnArrow(
                    shtLoc.clone().add(shtLoc.getDirection().normalize()),
                    new Vector(tgtLoc.getX() - shtLoc.getX(),
                        tgtLoc.getY() - shtLoc.getY() + distance / 160,
                        tgtLoc.getZ() - shtLoc.getZ())
                    .normalize(), 2f, 1);

            arrow.setShooter(shooter);

            shootCoolDown = SHOOT_CD;
        } else {
            shootCoolDown--;
        }
    }

    public void useItem(Entity entity) {
        if (entity instanceof Player) {
            PlayerAnimation.START_USE_MAINHAND_ITEM.play((Player) entity);
        }
    }
}
