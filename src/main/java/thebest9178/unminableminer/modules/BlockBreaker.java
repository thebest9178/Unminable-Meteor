package thebest9178.unminableminer.modules;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockBreaker {
    public static void breakBlock(ClientWorld world, BlockPos pos) {
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
        mc.interactionManager.attackBlock(pos, Direction.UP);
    }
}
