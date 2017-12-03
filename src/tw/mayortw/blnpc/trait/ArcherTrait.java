package tw.mayortw.blnpc.trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.LookClose;

import java.util.Random;

import tw.mayortw.blnpc.BorderlessNPCPlugin;
import tw.mayortw.blnpc.goal.MoveToHomeGoal;
import tw.mayortw.blnpc.goal.ShootTargetGoal;

public class ArcherTrait extends Trait {

    private Random random = new Random();
    private int soundCountdown;
    private Sound[] sounds = {  Sound.ENTITY_VILLAGER_AMBIENT,
                                Sound.ENTITY_VILLAGER_TRADING,
                                Sound.ENTITY_VILLAGER_YES,
                                Sound.ENTITY_VILLAGER_NO};

    public ArcherTrait() {
        super("Archer");
    }

    @Override
    public void onAttach() {

        npc.getTrait(LookClose.class).lookClose(true);

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new ShootTargetGoal(getNPC()), 2);
        goalCtl.addGoal(new MoveToHomeGoal(getNPC()), 1);

        NavigatorParameters navParm = getNPC().getNavigator().getLocalParameters();
        navParm.stationaryTicks(100);
        navParm.useNewPathfinder(true);

        npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, false);
    }

    @Override
    public void run() {
        if(npc.isSpawned() && soundCountdown <= 0) {
            // Make some noise
            npc.getEntity().getWorld().playSound(npc.getEntity().getLocation(),
                    sounds[random.nextInt(sounds.length)], 1, 1);
            soundCountdown = 100 + random.nextInt(100);
        } else soundCountdown--;
    }
}
