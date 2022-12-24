package thebest9178.unminableminer.modules;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class BlockBreaker {
    public static void breakBlock(BlockPos pos) {
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
        BlockUtils.breakBlock(pos, Modules.get().get(UnminableMiner.class).swing.get());
    }
}
