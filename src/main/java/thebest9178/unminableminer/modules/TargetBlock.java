package thebest9178.unminableminer.modules;

import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class TargetBlock {
    private BlockPos blockPos;
    private BlockPos redstoneTorchBlockPos;
    private BlockPos pistonBlockPos;
    private ClientWorld world;
    private Status status;
    private BlockPos slimeBlockPos;
    private int tickTimes;
    private boolean hasTried;
    private int stuckTicksCounter;

    public TargetBlock(BlockPos pos, ClientWorld world) {
        this.hasTried = false;
        this.stuckTicksCounter = 0;
        this.status = Status.UNINITIALIZED;
        this.blockPos = pos;
        this.world = world;
        this.pistonBlockPos = pos.up();
        this.redstoneTorchBlockPos = CheckingEnvironment.findNearbyFlatBlockToPlaceRedstoneTorch(this.world, this.blockPos);
        if (redstoneTorchBlockPos == null) {
            this.slimeBlockPos = CheckingEnvironment.findPossibleSlimeBlockPos(world, pos);
            if (slimeBlockPos != null) {
                BlockPlacer.simpleBlockPlacement(slimeBlockPos, Blocks.SLIME_BLOCK);
                redstoneTorchBlockPos = slimeBlockPos.up();
            } else {
                this.status = TargetBlock.Status.FAILED;
            }
        }
    }

    public TargetBlock.Status tick() {
        this.tickTimes++;
        updateStatus();
        System.out.println(this.status);
        switch (this.status) {
            case UNINITIALIZED:
                InventoryManager.switchToItem(Blocks.PISTON);
                BlockPlacer.pistonPlacement(this.pistonBlockPos, Direction.UP);
                InventoryManager.switchToItem(Blocks.REDSTONE_TORCH);
                BlockPlacer.simpleBlockPlacement(this.redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                break;
            case UNEXTENDED_WITH_POWER_SOURCE:
                break;
            case EXTENDED:
                //Destroy the redstone torch
                ArrayList<BlockPos> nearByRedstoneTorchPosList = CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos);
                for (BlockPos pos : nearByRedstoneTorchPosList) {
                    BlockBreaker.breakBlock(world, pos);
                }
                //Destroy out the piston
                BlockBreaker.breakBlock(this.world, this.pistonBlockPos);
                //Place the piston facing down
                BlockPlacer.pistonPlacement(this.pistonBlockPos, Direction.DOWN);
                this.hasTried = true;
                break;
            case RETRACTED:
                BlockBreaker.breakBlock(world, pistonBlockPos);
                BlockBreaker.breakBlock(world, pistonBlockPos.up());
                if (this.slimeBlockPos != null) {
                    BlockBreaker.breakBlock(world, slimeBlockPos);
                }
                return TargetBlock.Status.RETRACTED;
            case RETRACTING:
                return TargetBlock.Status.RETRACTING;
            case UNEXTENDED_WITHOUT_POWER_SOURCE:
                InventoryManager.switchToItem(Blocks.REDSTONE_TORCH);
                BlockPlacer.simpleBlockPlacement(this.redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                break;
            case FAILED:
                BlockBreaker.breakBlock(world, pistonBlockPos);
                BlockBreaker.breakBlock(world, pistonBlockPos.up());
                return TargetBlock.Status.FAILED;
            case STUCK:
                BlockBreaker.breakBlock(world, pistonBlockPos);
                BlockBreaker.breakBlock(world, pistonBlockPos.up());
                break;
            case NEEDS_WAITING:
                break;
        }
        return null;
    }

    enum Status {
        FAILED,
        UNINITIALIZED,
        UNEXTENDED_WITH_POWER_SOURCE,
        UNEXTENDED_WITHOUT_POWER_SOURCE,
        EXTENDED,
        NEEDS_WAITING,
        RETRACTING,
        RETRACTED,
        STUCK
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ClientWorld getWorld() {
        return world;
    }

    public Status getStatus() {
        return status;
    }

    private void updateStatus() {
        if (this.tickTimes > 40) {
            this.status = Status.FAILED;
            return;
        }
        this.redstoneTorchBlockPos = CheckingEnvironment.findNearbyFlatBlockToPlaceRedstoneTorch(this.world, this.blockPos);
        if (this.redstoneTorchBlockPos == null) {
            this.slimeBlockPos = CheckingEnvironment.findPossibleSlimeBlockPos(world, blockPos);
            if (slimeBlockPos != null) {
                BlockPlacer.simpleBlockPlacement(slimeBlockPos, Blocks.SLIME_BLOCK);
                redstoneTorchBlockPos = slimeBlockPos.up();
            } else {
                this.status = Status.FAILED;
                Messenger.actionBar("Failed to place redstone torch.");
            }
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !(this.world.getBlockState(this.blockPos).getHardness(this.world, this.blockPos) < 0)) {
            this.status = Status.RETRACTED;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED)) {
            this.status = Status.EXTENDED;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.MOVING_PISTON)) {
            this.status = Status.RETRACTING;
        }  else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() != 0 && (this.world.getBlockState(this.blockPos).getHardness(this.world, this.blockPos) < 0)) {
            this.status = Status.UNEXTENDED_WITH_POWER_SOURCE;
        } else if (this.hasTried && this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.stuckTicksCounter < 15) {
            this.status = Status.NEEDS_WAITING;
            this.stuckTicksCounter++;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.DOWN && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() != 0 && (this.world.getBlockState(this.blockPos).getHardness(this.world, this.blockPos) < 0)) {
            this.status = Status.STUCK;
            this.hasTried = false;
            this.stuckTicksCounter = 0;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.UP && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() == 0 && (this.world.getBlockState(this.blockPos).getHardness(this.world, this.blockPos) < 0)) {
            this.status = Status.UNEXTENDED_WITHOUT_POWER_SOURCE;
        } else if (CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, this.blockPos)) {
            this.status = Status.UNINITIALIZED;
        } else if (!CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, this.blockPos)) {
            this.status = Status.FAILED;
            Messenger.actionBar("Failed to place piston.");
        } else {
            this.status = Status.FAILED;
        }
    }
}
