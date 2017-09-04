package tw.mayortw.blnpc.trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.ai.event.NavigationStuckEvent;
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
import java.util.Random;

import tw.mayortw.blnpc.BorderlessNPCPlugin;
import tw.mayortw.blnpc.goal.GoHomeAtNightGoal;
import tw.mayortw.blnpc.goal.StrollNearHomeGoal;
import tw.mayortw.blnpc.goal.RandomStrollGoal;

public class ResidentTrait extends Trait {

    private int lastX, lastZ, stationaryTicks; // Edited from net.citizensnpcs.npc.ai.CitizensNavigator
    private Random random = new Random();
    private Sound[] sounds = {  Sound.ENTITY_VILLAGER_AMBIENT,
                                Sound.ENTITY_VILLAGER_TRADING,
                                Sound.ENTITY_VILLAGER_YES,
                                Sound.ENTITY_VILLAGER_NO};

    public ResidentTrait() {
        super("Resident");
    }

    @Override
    public void onAttach() {

        npc.getTrait(LookClose.class).lookClose(true);

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new GoHomeAtNightGoal(getNPC()), 2);
        goalCtl.addGoal(new StrollNearHomeGoal(getNPC(), 10, 10), 1);

        NavigatorParameters navParm = getNPC().getNavigator().getLocalParameters();
        navParm.stuckAction((a, n) -> {return false;});
        navParm.addSingleUseCallback(cancelReason -> stationaryTicks = 0);
        navParm.examiner(new DoorExaminer());
        navParm.stationaryTicks(100);
        navParm.distanceMargin(1.5);
        navParm.useNewPathfinder(true);
    }

    @Override
    public void run() {
        checkStationaryStatus();

        if(npc.isSpawned() && random.nextInt(100) == 0) {
            // Make some noise
            npc.getEntity().getWorld().playSound(npc.getEntity().getLocation(),
                    sounds[random.nextInt(sounds.length)], 1, 1);
        }
    }

    // Edited from net.citizensnpcs.npc.ai.CitizensNavigator

    /*
     * The stuck action will not be called by Citizens API if NPC is jumping
     * so I handle it myself
     */

    private void checkStationaryStatus() {
        if(!npc.isSpawned()) return;

        Navigator navigator = npc.getNavigator();

        if(navigator.isNavigating()) {
            NavigatorParameters localParams = navigator.getLocalParameters();
            if (localParams.stationaryTicks() < 0)
                return;
            Location current = npc.getEntity().getLocation();
            if (lastX == current.getBlockX() && lastZ == current.getBlockZ()) {
                if (++stationaryTicks >= localParams.stationaryTicks()) {
                    StuckAction action = localParams.stuckAction();
                    NavigationStuckEvent event = new NavigationStuckEvent(navigator, action);
                    Bukkit.getPluginManager().callEvent(event);
                    action = event.getAction();
                    boolean shouldContinue = action != null ? action.run(npc, navigator) : false;
                    if (shouldContinue) {
                        stationaryTicks = 0;
                    } else {
                        navigator.cancelNavigation();
                    }
                    return;
                }
            } else
                stationaryTicks = 0;
            lastX = current.getBlockX();
            lastZ = current.getBlockZ();
        }
    }


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

                    double distance = npc.getStoredLocation().distance(doorBlock.getLocation());

                    if(distance > 2) {
                        openDoor(false);
                        this.cancel();
                    } else {
                        openDoor(true);
                    }

                } else {
                    this.cancel();
                }
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

                npc.getNavigator().getLocalParameters().addSingleUseCallback(cancelReason -> {
                    openDoor(false);
                    doorChecker.cancel();
                });

                doorChecker.runTaskTimer(BorderlessNPCPlugin.getPlugin(BorderlessNPCPlugin.class), 5, 10);
            }
        }

        private void swingArm(Entity entity) {
            if (entity instanceof Player) {
                PlayerAnimation.ARM_SWING.play((Player) entity);
            }
        }

        private void openDoor(boolean open) {
            BlockState state = doorBlock.getState();

            if(door.isOpen() != open) {
                doorBlock.getWorld().playSound(doorBlock.getLocation(),
                        open ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE,
                        .8f, 1);
                swingArm(npc.getEntity());
            }

            door.setOpen(open);
            state.setData(door);
            state.update();
        }
    }
}
