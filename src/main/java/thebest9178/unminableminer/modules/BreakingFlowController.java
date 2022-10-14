package thebest9178.unminableminer.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.world.ClientWorld;

import java.util.ArrayList;

public class BreakingFlowController {
    private static ArrayList<TargetBlock> cachedTargetBlockList = new ArrayList<>();

    public static boolean isWorking() {
        return working;
    }

    private static boolean working = false;


    public static void addBlockPosToList(BlockPos pos) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world.getBlockState(pos).getHardness(world, pos) < 0) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();

            String haveEnoughItems = InventoryManager.warningMessage();
            if (haveEnoughItems != null) {
                Messenger.actionBar(haveEnoughItems);
                return;
            }

            if (shouldAddNewTargetBlock(pos)){
                TargetBlock targetBlock = new TargetBlock(pos, world);
                cachedTargetBlockList.add(targetBlock);
                System.out.println("new task.");
            }
        } else {
            Messenger.actionBar("Please make sure the block you hit is still a valid block.");
        }
    }

    public static void tick() {
        if (InventoryManager.warningMessage() != null) {
            return;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;

        if (!"survival".equals(minecraftClient.interactionManager.getCurrentGameMode().getName())) {
            return;
        }

        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            TargetBlock selectedBlock = cachedTargetBlockList.get(i);

            //When the player switches worlds or is too far away from the target block, delete all cached tasks
            if (selectedBlock.getWorld() != MinecraftClient.getInstance().world ) {
                cachedTargetBlockList = new ArrayList<TargetBlock>();
                break;
            }

            if (blockInPlayerRange(selectedBlock.getBlockPos(), player, 3.4f)) {
                TargetBlock.Status status = cachedTargetBlockList.get(i).tick();
                if (status == TargetBlock.Status.RETRACTING) {
                    continue;
                } else if (status == TargetBlock.Status.FAILED || status == TargetBlock.Status.RETRACTED) {
                    cachedTargetBlockList.remove(i);
                } else {
                    break;
                }

            }
        }
    }

    private static boolean blockInPlayerRange(BlockPos blockPos, PlayerEntity player, float range) {
        return blockPos.isWithinDistance(player.getPos(), range);
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos){
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            if (cachedTargetBlockList.get(i).getBlockPos().getManhattanDistance(pos) == 0){
                return false;
            }
        }
        return true;
    }

    public static void switchOn() {
        if(working) {
            return;
        } else {
            /*Messenger.chat("");
            Messenger.chat("§5Unmineable Miner started. Left click an unminable block to break it.§r");
            Messenger.chat("");*/
            working = true;
        }
    }

    public static void switchOff() {
        if(!working) {
            return;
        } else {
            /*Messenger.chat("");
            Messenger.chat("§5Unminable Miner stopped.§r");
            Messenger.chat("");*/
            working = false;
        }
    }
}
