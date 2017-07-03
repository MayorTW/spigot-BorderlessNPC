package tw.mayortw.blenemies.trait;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.trait.Trait;
import tw.mayortw.blenemies.goal.RandomStrollGoal;
import tw.mayortw.blenemies.goal.ShootNearbyPlayerGoal;
import tw.mayortw.blenemies.goal.TargetNearbyPlayerGoal;

public class ForestRobberTrait extends Trait {

    public ForestRobberTrait() {
        super("forestrobber");
    }

    @Override
    public void onAttach() {

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new ShootNearbyPlayerGoal(getNPC(), true, 30, 15), 3);
        goalCtl.addGoal(new TargetNearbyPlayerGoal(getNPC(), true, 10), 2);
        goalCtl.addGoal(new RandomStrollGoal(getNPC()), 1);

        getNPC().getNavigator().getLocalParameters().stuckAction((a, n) -> {return false;});
    }
}

