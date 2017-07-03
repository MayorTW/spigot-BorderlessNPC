package tw.mayortw.blenemies.trait;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.trait.Trait;
import tw.mayortw.blenemies.goal.RandomStrollGoal;
import tw.mayortw.blenemies.goal.TargetNearbyPlayerGoal;

public class PirateTrait extends Trait {

    public PirateTrait() {
        super("pirate");
    }

    @Override
    public void onAttach() {

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new TargetNearbyPlayerGoal(getNPC(), true, 5), 2);
        goalCtl.addGoal(new RandomStrollGoal(getNPC()), 1);

        getNPC().getNavigator().getLocalParameters().stuckAction((a, n) -> {return false;});
    }
}

