package tw.mayortw.blenemies.trait;

import org.bukkit.entity.EntityType;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import java.util.HashSet;
import java.util.Set;
import tw.mayortw.blenemies.goal.RandomStrollGoal;

public class SoundHorseTrait extends Trait {

    public SoundHorseTrait() {
        super("soundhorse");
    }

    @Override
    public void onAttach() {

        Set<EntityType> targets = new HashSet<>();
        targets.add(EntityType.PLAYER);

        GoalController goalCtl = getNPC().getDefaultGoalController();
        goalCtl.addGoal(new TargetNearbyEntityGoal.Builder(getNPC()).targets(targets).aggressive(true).radius(5).build(), 2);
        goalCtl.addGoal(new RandomStrollGoal(getNPC()), 1);

        getNPC().getNavigator().getLocalParameters().stuckAction((a, n) -> {return false;});
    }
}

