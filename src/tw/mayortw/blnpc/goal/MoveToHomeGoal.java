package tw.mayortw.blnpc.goal;

/*
 * Edited from net.citizensnpcs.api.ai.goals.MoveToGoal
 * Go to the home point
 */

import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;

import tw.mayortw.blnpc.BorderlessNPCPlugin;

public class MoveToHomeGoal extends BehaviorGoalAdapter {
    private boolean finished;
    private final NPC npc;
    private CancelReason reason;

    public MoveToHomeGoal(NPC npc) {
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
        if(npc.getNavigator().isNavigating())
            return false;

        MetadataStore data = npc.data();

        Location npcLoc = npc.getEntity().getLocation();
        Location home = new Location(npcLoc.getWorld(),
                data.get(BorderlessNPCPlugin.HOME_X_METADATA),
                data.get(BorderlessNPCPlugin.HOME_Y_METADATA),
                data.get(BorderlessNPCPlugin.HOME_Z_METADATA));

        if(!data.has(BorderlessNPCPlugin.HOME_X_METADATA) ||
                !data.has(BorderlessNPCPlugin.HOME_Y_METADATA) ||
                !data.has(BorderlessNPCPlugin.HOME_Z_METADATA))
            return false;

        if (npcLoc.getWorld() != home.getWorld() || npcLoc.distanceSquared(home) <= npc
                .getNavigator().getLocalParameters().distanceMargin() + 1)
            return false;

        npc.getNavigator().setTarget(home);
        npc.getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
            @Override
            public void onCompletion(CancelReason cancelReason) {
                finished = true;
                reason = cancelReason;
            }
        });
        return true;
    }
}
