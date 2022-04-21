package thebest9178.unminableminer.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class UnminableMiner extends Module {
    public UnminableMiner() {
        super(Categories.World, "unminable-miner", "Allows breaking unminable blocks.");
    }

    @EventHandler
    public void onActivate() {
        BreakingFlowController.switchOn();
    }

    @EventHandler
    public void onDeactivate() {
        BreakingFlowController.switchOff();
    }

    @EventHandler
    private void onTick(TickEvent.Post Event) {
        if(thebest9178.unminableminer.modules.BreakingFlowController.isWorking()) {
            thebest9178.unminableminer.modules.BreakingFlowController.tick();
        }
    }
}
