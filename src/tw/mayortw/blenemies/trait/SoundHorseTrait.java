package tw.mayortw.blenemies.trait;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.trait.Trait;
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
            }
        }
    }
}

