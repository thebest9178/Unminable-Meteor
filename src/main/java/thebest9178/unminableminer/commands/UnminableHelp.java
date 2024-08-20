package thebest9178.unminableminer.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.screen.slot.SlotActionType;

public class UnminableHelp extends Command {
    public UnminableHelp() {
        super("unminable-help", "How to use unminable miner", "unminable");
    }

    public void build(LiteralArgumentBuilder builder) {
        builder.executes((context) -> {
            info("Setup a haste 2 beacon. In your hotbar, have redstone torches, slime blocks, pistons, and hold a diamond or netharite efficiency 5 pickaxe. Click the unbreakable block with the pickaxe.");
            return 1;
        });
    }
}
