package tw.mayortw.blnpc.trait;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.trait.Trait;
import tw.mayortw.blnpc.goal.RandomStrollGoal;

public class BorderlessTrait extends Trait {

    public BorderlessTrait() {
        super("Borderless");
    }

    @Override
    public void onAttach() {

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new RandomStrollGoal(getNPC()), 1);

        getNPC().getNavigator().getLocalParameters().stuckAction((a, n) -> {return false;});
    }
}
