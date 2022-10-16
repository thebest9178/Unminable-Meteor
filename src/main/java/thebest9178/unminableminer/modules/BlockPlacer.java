package thebest9178.unminableminer.modules;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
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

public class BlockPlacer {
    public static void simpleBlockPlacement(BlockPos pos, ItemConvertible item) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        InventoryManager.switchToItem(item);
        BlockHitResult hitResult = new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false);
        placeBlockWithoutInteractingBlock(minecraftClient, hitResult);
    }

    public static void pistonPlacement(BlockPos blockPos, Direction direction) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        //Directly issue the packet to change the perspective of the player entity on the server side
        PlayerEntity player = minecraftClient.player;
        float pitch = switch (direction) {
            case UP -> 90f;
            case DOWN -> -90f;
            default -> 90f;
        };

        minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(player.getYaw(1.0f), pitch, player.isOnGround()));

        Vec3d pos = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        InventoryManager.switchToItem(Blocks.PISTON);
        BlockHitResult hitResult = new BlockHitResult(pos, Direction.UP, blockPos, false);
        placeBlockWithoutInteractingBlock(minecraftClient, hitResult);
    }

    private static void placeBlockWithoutInteractingBlock(MinecraftClient minecraftClient, BlockHitResult hitResult) {
        ClientPlayerEntity player = minecraftClient.player;
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);

        minecraftClient.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 0));

        if (!itemStack.isEmpty() && !player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
            ItemUsageContext itemUsageContext = new ItemUsageContext(player, Hand.MAIN_HAND, hitResult);
            itemStack.useOnBlock(itemUsageContext);
        }
    }
}
