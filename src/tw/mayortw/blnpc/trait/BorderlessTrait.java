package tw.mayortw.blnpc.trait;

import org.bukkit.entity.EntityType;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.LookClose;
import tw.mayortw.blnpc.goal.GoHomeAtNightGoal;
import tw.mayortw.blnpc.goal.StayNearHomeGoal;
import tw.mayortw.blnpc.goal.RandomStrollGoal;

public class BorderlessTrait extends Trait {

    public BorderlessTrait() {
        super("Borderless");
    }

    @Override
    public void onAttach() {

        npc.getTrait(LookClose.class).lookClose(true);

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new GoHomeAtNightGoal(getNPC()), 3);
        goalCtl.addGoal(new StayNearHomeGoal(getNPC()), 2);
        goalCtl.addGoal(new RandomStrollGoal(getNPC()), 1);

        getNPC().getNavigator().getLocalParameters().stuckAction((a, n) -> {return false;});
    }
}
