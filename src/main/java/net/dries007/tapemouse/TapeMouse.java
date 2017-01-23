package net.dries007.tapemouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Main mod file
 *
 * @author Dries007
 */
@SuppressWarnings({"NewExpressionSideOnly", "MethodCallSideOnly"}) // clientSideOnly does about the same thing...
@Mod(modid = TapeMouse.MODID, name = TapeMouse.NAME, dependencies = "before:*", useMetadata = false, clientSideOnly = true)
public class TapeMouse {
    public static final String MODID = "tapemouse";
    public static final String NAME = "TapeMouse";
    static int keyUpDelay = 0;
    static int keyDownDelay = 0;
    static KeyBinding keyBinding;
    static int i = 0;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        if (event.getSide().isClient()) ClientCommandHandler.instance.registerCommand(new CommandTapeMouse());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void textRenderEvent(RenderGameOverlayEvent.Text event) {
        if (keyBinding == null) return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            event.getLeft().add(MODID + " paused. Chat GUI open.");
        } else {
            event.getLeft().add(MODID + " active: " + keyBinding.getDisplayName() + " (" + keyBinding.getKeyDescription().replaceFirst("^key\\.", "") + ')');
            event.getLeft().add("Delay: " + i + " / " + keyUpDelay + " / " + keyDownDelay);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void tickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiChat) return;
        if (keyBinding == null) return;

        if (keyDownDelay == 0 && keyUpDelay == 0) {
            if (!keyBinding.isKeyDown()) {
                KeyBinding.setKeyBindState(keyBinding.getKeyCode(), true);
                KeyBinding.onTick(keyBinding.getKeyCode());
            }
        } else {
            i++;
            if (keyBinding.isKeyDown() && i >= keyDownDelay) {
                KeyBinding.setKeyBindState(keyBinding.getKeyCode(), false);
                i = 0;
            } else if (!keyBinding.isKeyDown() && i >= keyUpDelay) {
                KeyBinding.setKeyBindState(keyBinding.getKeyCode(), true);
                KeyBinding.onTick(keyBinding.getKeyCode());
                i = 0;
            }
        }
    }
}
