package tw.mayortw.blnpc.trait;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
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
        goalCtl.addGoal(new StayNearHomeGoal(getNPC()), 2);
        goalCtl.addGoal(new RandomStrollGoal(getNPC()), 1);

        NavigatorParameters navParm = getNPC().getNavigator().getLocalParameters();
        navParm.stuckAction((a, n) -> {return false;});
        navParm.examiner(new DoorExaminer());
        navParm.stationaryTicks(300);

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

        Block doorBlock;
        Door door;
        NPC npc;
        boolean checkerRunning;

        private BukkitRunnable doorChecker = new BukkitRunnable() {

            @Override
            public void run() {
                if(doorBlock != null && door != null && npc != null) {

                    BlockState state = doorBlock.getState();
                    double distance = npc.getStoredLocation().distance(doorBlock.getLocation());

                    if(distance < 2) {

                        if(!door.isOpen())
                            doorBlock.getWorld().playSound(doorBlock.getLocation(),
                                    Sound.BLOCK_WOODEN_DOOR_OPEN, .8f, 1);

                        door.setOpen(true);
                        state.setData(door);
                        state.update();
                    }
                    if(distance > 2 || !npc.getNavigator().isNavigating()) {


                        if(door.isOpen())
                            doorBlock.getWorld().playSound(doorBlock.getLocation(),
                                    Sound.BLOCK_WOODEN_DOOR_CLOSE, .8f, 1);

                        door.setOpen(false);
                        state.setData(door);
                        state.update();

                        this.cancel();
                        checkerRunning = false;
                    }
                } else {
                    this.cancel();
                    checkerRunning = false;
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

            if(!checkerRunning) {
                checkerRunning = true;
                doorChecker.runTaskTimer(BorderlessNPCPlugin.getPlugin(BorderlessNPCPlugin.class), 5, 10);
            }
        }
    }
}
