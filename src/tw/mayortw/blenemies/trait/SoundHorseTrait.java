package tw.mayortw.blenemies.trait;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.event.NPCDamageEntityEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import tw.mayortw.blenemies.goal.RandomStrollGoal;
import tw.mayortw.blenemies.goal.TargetNearbyPlayerGoal;

public class SoundHorseTrait extends Trait {

    public SoundHorseTrait() {
        super("soundhorse");
    }

    @Override
    public void onAttach() {

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new TargetNearbyPlayerGoal(getNPC(), true, 5), 2);
        goalCtl.addGoal(new RandomStrollGoal(getNPC()), 1);

        getNPC().getNavigator().getLocalParameters().stuckAction((a, n) -> {return false;});
    }

    @Override
    public void run() {
        if(getNPC().isSpawned()) {
            if(getNPC().getEntity().getPassengers().size() == 0) {
                getNPC().despawn();
            } else {
                for(Entity entity : getNPC().getEntity().getPassengers()) {
                    entity.getLocation().setDirection(getNPC().getEntity().getLocation().getDirection());
                    NPC rider = CitizensAPI.getNPCRegistry().getNPC(entity);
                    if(rider != null) {
                        rider.faceLocation(getNPC().getEntity().getLocation()
                                .clone().add(getNPC().getEntity().getLocation().getDirection().normalize()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onNPCDamageEntity(NPCDamageEntityEvent eve) {
        if(eve.getNPC().equals(this.getNPC())) {
            Entity rider = eve.getNPC().getEntity().getPassengers().get(0);

            if(rider instanceof Player)
                PlayerAnimation.ARM_SWING.play((Player) rider);
        }
    }
}

