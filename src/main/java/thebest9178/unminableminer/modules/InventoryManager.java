package thebest9178.unminableminer.modules;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.FluidTags;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class InventoryManager {
    public static boolean switchToItem(ItemConvertible item) {
        int i = mc.player.getInventory().getSlotWithStack(new ItemStack(item));

        if ("diamond_pickaxe".equals(item.toString())) {
            i = getEfficientTool(mc.player.getInventory());
        }

        if (i != -1) {
            if (PlayerInventory.isValidHotbarIndex(i)) {
                mc.player.getInventory().selectedSlot = i;
            } else {
                mc.interactionManager.pickFromInventory(i);
            }
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            return true;
        }
        return false;
    }

    private static int getEfficientTool(PlayerInventory playerInventory) {
        for (int i = 0; i < playerInventory.main.size(); ++i) {
            if (getBlockBreakingSpeed(Blocks.PISTON.getDefaultState(), i) > 45f) {
                return i;
            }
        }
        return -1;
    }

    public static boolean canInstantlyMinePiston() {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (getBlockBreakingSpeed(Blocks.PISTON.getDefaultState(), i) > 45f) {
                return true;
            }
        }
        return false;
    }

    private static float getBlockBreakingSpeed(BlockState block, int slot) {
        ItemStack stack = mc.player.getInventory().getStack(slot);

        float f = stack.getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            ItemStack itemStack = mc.player.getInventory().getStack(slot);
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(mc.player)) {
            f *= 1.0F + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2F;
        }

        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k = switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= k;
        }

        if (mc.player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            f /= 5.0F;
        }

        if (!mc.player.isOnGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public static int getInventoryItemCount(ItemConvertible item) {
        PlayerInventory playerInventory = mc.player.getInventory();
        int counter = 0;

        for (int i = 0; i < playerInventory.size(); i++) {
            if (playerInventory.getStack(i).getItem() == new ItemStack(item).getItem()) {
                counter = counter + playerInventory.getStack(i).getCount();
            }
        }
        return counter;
    }

    public static String warningMessage() {
        if (!"survival".equals(mc.interactionManager.getCurrentGameMode().getName())) {
            return "Survival Only.";
        }

        if(InventoryManager.getInventoryItemCount(Blocks.PISTON) < 2) {
            return "Needs more pistons.";
        }

        if (InventoryManager.getInventoryItemCount(Blocks.REDSTONE_TORCH) < 1) {
            return "Needs more redstone torches.";
        }

        if (InventoryManager.getInventoryItemCount(Blocks.SLIME_BLOCK)<1){
            return "Needs more slime blocks.";
        }

        if (!InventoryManager.canInstantlyMinePiston()) {
            return "Can't instantly mine piston.";
        }
        return null;
    }
}
