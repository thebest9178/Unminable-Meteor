package thebest9178.unminableminer.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Messenger {
    public static void actionBar(String message){
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.inGameHud.setOverlayMessage(Text.of(message),false);
    }

    public static void chat(String message){
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.player.sendMessage(Text.of(message));
    }
}
