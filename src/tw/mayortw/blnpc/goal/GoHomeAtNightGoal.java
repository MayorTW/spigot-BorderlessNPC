package tw.mayortw.blnpc.goal;

import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;

import tw.mayortw.blnpc.BorderlessNPCPlugin;

public class GoHomeAtNightGoal extends BehaviorGoalAdapter {

    private static final int DAY_TIME = 24000;
    private static final int NIGHT_TIME = 12517;

    private boolean finished;
    private final NPC npc;
    private CancelReason reason;

    public GoHomeAtNightGoal(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void reset() {
        npc.getNavigator().cancelNavigation();
        reason = null;
        finished = false;
    }

    @Override
    public BehaviorStatus run() {
        if (finished) {
            return reason == null ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {

        long currTime = npc.getEntity().getWorld().getTime();

        if(!npc.data().has(BorderlessNPCPlugin.HOME_X_METADATA) ||
                !npc.data().has(BorderlessNPCPlugin.HOME_Y_METADATA) ||
                !npc.data().has(BorderlessNPCPlugin.HOME_Z_METADATA))
            return false;

        if(currTime > DAY_TIME || currTime < NIGHT_TIME)
            return false;

        Location npcLoc = npc.getEntity().getLocation();
        Location target = new Location(npcLoc.getWorld(),
                npc.data().get(BorderlessNPCPlugin.HOME_X_METADATA),
                npc.data().get(BorderlessNPCPlugin.HOME_Y_METADATA),
                npc.data().get(BorderlessNPCPlugin.HOME_Z_METADATA));

        if(npcLoc.distanceSquared(target) > 2) {

            npc.getNavigator().setTarget(target);
            npc.getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
                @Override
                public void onCompletion(CancelReason cancelReason) {
                    finished = true;
                    reason = cancelReason;
                }
            });
        } else finished = true;
        return true;
    }
}
