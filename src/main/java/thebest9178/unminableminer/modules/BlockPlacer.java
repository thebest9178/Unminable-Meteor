package thebest9178.unminableminer.modules;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockPlacer {
    public static void simpleBlockPlacement(BlockPos pos, ItemConvertible item) {
        InventoryManager.switchToItem(item);
        BlockHitResult hitResult = new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false);
        placeBlockWithoutInteractingBlock(mc, hitResult);
    }

    public static void pistonPlacement(BlockPos blockPos, Direction direction) {
        //Directly issue the packet to change the perspective of the player entity on the server side
        float pitch = switch (direction) {
            case UP -> 90f;
            case DOWN -> -90f;
            default -> 90f;
        };

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(1.0f), pitch, mc.player.isOnGround()));

        //Vec3d pos = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3i pos = new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        InventoryManager.switchToItem(Blocks.PISTON);
        //BlockHitResult hitResult = new BlockHitResult(pos, Direction.UP, blockPos, false);
        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.DOWN, blockPos, false);
        placeBlockWithoutInteractingBlock(mc, hitResult);
    }

    private static void placeBlockWithoutInteractingBlock(MinecraftClient minecraftClient, BlockHitResult hitResult) {
        ItemStack itemStack = mc.player.getStackInHand(Hand.MAIN_HAND);

        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 0));
        //mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(pos), Direction.DOWN, pos, false), 0));

        if (!itemStack.isEmpty() && !mc.player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
            ItemUsageContext itemUsageContext = new ItemUsageContext(mc.player, Hand.MAIN_HAND, hitResult);
            itemStack.useOnBlock(itemUsageContext);
        }
    }
}
