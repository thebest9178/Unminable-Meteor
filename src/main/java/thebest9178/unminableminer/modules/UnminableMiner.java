package thebest9178.unminableminer.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class UnminableMiner extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //TODO: Add a button to print a help message explaining how to use it
    public final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Placing/breaking range")
        .defaultValue(3.6)
        .sliderMax(10)
        .max(10)
        .build()
    );

    public final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("The blocks you want to mine.")
        .build()
    );

    public UnminableMiner() {
        super(Categories.World, "unminable-miner", "Allows breaking unminable blocks.");
    }

    public boolean isBlockOnWhitelist(ClientWorld world, BlockPos blockPos) {
        return whitelist.get().contains(world.getBlockState(blockPos).getBlock());
    }

    @EventHandler
    public void onActivate() {
        BreakingFlowController.setWorking(true);
    }

    @EventHandler
    public void onDeactivate() {
        BreakingFlowController.setWorking(false);
    }

    @EventHandler
    private void onTick(TickEvent.Pre Event) {
        if(BreakingFlowController.isWorking()) {
            BreakingFlowController.tick();
        }
    }
}
