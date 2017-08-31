package tw.mayortw.blnpc.trait;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.PlayerAnimation;

import java.util.ListIterator;

import tw.mayortw.blnpc.BorderlessNPCPlugin;
import tw.mayortw.blnpc.goal.GoHomeAtNightGoal;
import tw.mayortw.blnpc.goal.StayNearHomeGoal;
import tw.mayortw.blnpc.goal.RandomStrollGoal;

public class ResidentTrait extends Trait {

    public ResidentTrait() {
        super("Resident");
    }

    @Override
    public void onAttach() {

        npc.getTrait(LookClose.class).lookClose(true);

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new GoHomeAtNightGoal(getNPC()), 3);
        goalCtl.addGoal(new StayNearHomeGoal(getNPC(), 10, 10), 2);
        goalCtl.addGoal(new RandomStrollGoal(getNPC(), 10, 10), 1);

        NavigatorParameters navParm = getNPC().getNavigator().getLocalParameters();
        navParm.stuckAction((a, n) -> {return false;});
        navParm.examiner(new DoorExaminer());
        navParm.stationaryTicks(300);
        navParm.useNewPathfinder(true);

    }

    // Edited from net.citizensnpcs.npc.ai.CitizensNavigator

    public static class DoorExaminer implements BlockExaminer {
        @Override
        public float getCost(BlockSource source, PathPoint point) {
            return 0F;
        }

        @Override
        public PassableState isPassable(BlockSource source, PathPoint point) {
            Material in = source.getMaterialAt(point.getVector());
            if (MinecraftBlockExaminer.isDoor(in)) {
                point.addCallback(new DoorOpener());
                return PassableState.PASSABLE;
            }
            return PassableState.IGNORE;
        }
    }

    private static class DoorOpener implements PathCallback {

        private Block doorBlock;
        private Door door;
        private NPC npc;

        private BukkitRunnable doorChecker = new BukkitRunnable() {

            @Override
            public void run() {
                if(doorBlock != null && door != null && npc != null) {

                    npc.getNavigator().getLocalParameters().addSingleUseCallback(cancelReason -> {
                        openDoor(false);
                        stopChecking();
                    });

                    double distance = npc.getStoredLocation().distance(doorBlock.getLocation());

                    if(distance > 2) {
                        if(door.isOpen()) {
                            doorBlock.getWorld().playSound(doorBlock.getLocation(),
                                    Sound.BLOCK_WOODEN_DOOR_CLOSE, .8f, 1);
                            swingArm(npc.getEntity());
                        }

                        openDoor(false);
                        stopChecking();
                    } else {

                        if(!door.isOpen()) {
                            doorBlock.getWorld().playSound(doorBlock.getLocation(),
                                    Sound.BLOCK_WOODEN_DOOR_OPEN, .8f, 1);
                            swingArm(npc.getEntity());
                        }
                        openDoor(true);
                    }

                } else {
                    stopChecking();
                }
            }

            private void openDoor(boolean open) {
                BlockState state = doorBlock.getState();

                door.setOpen(open);
                state.setData(door);
                state.update();
            }

            private void stopChecking() {
                this.cancel();
            }

        };

        @Override
        public void run(NPC npc, Block point, ListIterator<Block> path) {
            BlockState state = point.getState();
            door = (Door) state.getData();
            boolean bottom = !door.isTopHalf();

            doorBlock = bottom ? point : point.getRelative(BlockFace.DOWN);
            state = doorBlock.getState();
            door = (Door) state.getData();

            this.npc = npc;

            try {
                doorChecker.getTaskId();
            } catch(IllegalStateException exc) {
                doorChecker.runTaskTimer(BorderlessNPCPlugin.getPlugin(BorderlessNPCPlugin.class), 5, 10);
            }
        }

        private void swingArm(Entity entity) {
            if (entity instanceof Player) {
                PlayerAnimation.ARM_SWING.play((Player) entity);
            }
        }
    }
}
