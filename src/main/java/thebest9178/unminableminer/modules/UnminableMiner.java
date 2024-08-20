package thebest9178.unminableminer.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class UnminableMiner extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //TODO: Only add block pos to list when the player first clicks or when it is newly selected while player holds left click
    public final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Placing/breaking range")
        .defaultValue(3.6)
        .sliderMax(10)
        .max(10)
        .min(0)
        .build()
    );

    public final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("The blocks you want to mine.")
        .build()
    );

    public final Setting<Boolean> clearTargetedBlocksOnDeactivate = sgGeneral.add(new BoolSetting.Builder()
        .name("clear-targeted-blocks-on-deactivate")
        .description("Whether or not to clear the list of blocks planned to be broken, on deactivate.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("swing")
        .description("Swing hand.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> showErrors = sgGeneral.add(new BoolSetting.Builder()
        .name("show-errors")
        .description("Print errors in chat")
        .defaultValue(false)
        .build()
    );

    private static ArrayList<TargetBlock> cachedTargetBlockList = new ArrayList<>();

    public UnminableMiner() {
        super(Categories.World, "unminable-miner", "Allows breaking unminable blocks.");
    }

    public boolean isBlockOnWhitelist(ClientWorld world, BlockPos blockPos) {
        return whitelist.get().contains(world.getBlockState(blockPos).getBlock());
    }

    public void addBlockPosToList(BlockPos pos) {
        if (InventoryManager.warningMessage() != null) {
            if(showErrors.get()) {
                error(InventoryManager.warningMessage());
            }
            return;
        }

        if (shouldAddNewTargetBlock(pos)){
            TargetBlock targetBlock = new TargetBlock(pos, mc.world);
            cachedTargetBlockList.add(targetBlock);
            System.out.println("new task.");
        }
    }

    private static boolean blockInPlayerRange(BlockPos blockPos, PlayerEntity player, float range) {
        return blockPos.isWithinDistance(player.getPos(), range);
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos){
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            if (cachedTargetBlockList.get(i).getBlockPos() == pos){
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onDeactivate() {
        if(clearTargetedBlocksOnDeactivate.get()) {
            cachedTargetBlockList.clear();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre Event) {
        if (InventoryManager.warningMessage() != null) {
            return;
        }

        if (!"survival".equals(mc.interactionManager.getCurrentGameMode().getName())) {
            return;
        }

        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            TargetBlock selectedBlock = cachedTargetBlockList.get(i);

            //Reset cached tasks when player is not in same dimension as the target block
            if (selectedBlock.getWorld() != mc.world ) {
                cachedTargetBlockList = new ArrayList<TargetBlock>();
                break;
            }

            if (blockInPlayerRange(selectedBlock.getBlockPos(), mc.player, Modules.get().get(UnminableMiner.class).range.get().floatValue())) {
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
}
