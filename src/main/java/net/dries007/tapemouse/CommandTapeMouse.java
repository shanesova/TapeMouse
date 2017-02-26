package net.dries007.tapemouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.input.Keyboard.KEY_NONE;

/**
 * @author Dries007
 */
@SideOnly(Side.CLIENT)
public class CommandTapeMouse extends CommandBase {
    private static final List<KeyBinding> KEYBIND_ARRAY = ReflectionHelper.getPrivateValue(KeyBinding.class, null, "KEYBIND_ARRAY", "field_74516_a");
    private boolean prevPauseSetting = true; // defaults to true

    @Override
    public String getName() {
        return TapeMouse.MODID.toLowerCase();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return '/' + getName() + " [off|keybinding name...] <key down time> <key up time>\n" +
                "Use no arguments to get a list of keybindings.\n" +
                "Note: if key down and key up times are both 0 then the key will be held down.\n" +
                "Example: /" + getName() + " attack 20 20\n";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        switch (args.length) {
            case 0:
                ListKeybinds(sender);
                break;
            case 1:
                if (args[0].equalsIgnoreCase("off")) {
                    Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = prevPauseSetting;
                    TapeMouse.keyBinding = null;
                    TapeMouse.i = 0;
                    sender.sendMessage(new TextComponentString("TapeMouse off."));
                } else {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                }
                break;
            case 2:
                sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;
            default:
                final String askedName = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 2));

                try {
                    TapeMouse.keyDownDelay = parseInt(args[args.length - 2], 0);
                    TapeMouse.keyUpDelay = parseInt(args[args.length - 1], 0);
                } catch (NumberInvalidException e) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                }

                KeyBinding keyBinding = KEYBIND_ARRAY
                        .stream()
                        .filter(str -> str.getKeyDescription().replaceFirst("^key\\.", "").equalsIgnoreCase(askedName))
                        .findFirst().orElse(null);


                if (keyBinding != null) {
                    prevPauseSetting = Minecraft.getMinecraft().gameSettings.pauseOnLostFocus;
                    Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
                    TapeMouse.keyBinding = keyBinding;
                    sender.sendMessage(new TextComponentString("TapeMouse on '" + keyBinding.getDisplayName() + "' with key down " + TapeMouse.keyDownDelay + " and key up " + TapeMouse.keyUpDelay + " ticks."));
                } else {
                    throw new CommandException(askedName + " is unknown keybinding.");
                }

                break;
        }

    }

    private void ListKeybinds(ICommandSender sender) {
        sender.sendMessage(new TextComponentString("List of keybindings").setStyle(new Style().setColor(TextFormatting.AQUA)));
        sender.sendMessage(new TextComponentString("Key => Name (category)").setStyle(new Style().setColor(TextFormatting.AQUA)));
        for (KeyBinding keyBinding : KEYBIND_ARRAY) {
            if (keyBinding == null || keyBinding.getKeyCode() == KEY_NONE) continue;
            String name = keyBinding.getKeyDescription();
            if (name == null) continue;
            name = name.replaceFirst("^key\\.", "");

            String cat = keyBinding.getKeyCategory();
            if (cat == null) continue;
            cat = cat.replaceFirst("^key\\.categories\\.", "");

            sender.sendMessage(new TextComponentString(keyBinding.getDisplayName() + " => " + name + " (" + cat + ")"));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("off");
            for (KeyBinding keyBinding : KEYBIND_ARRAY) {
                if (keyBinding == null || keyBinding.getKeyCode() == KEY_NONE) continue;
                String name = keyBinding.getKeyDescription();
                if (name == null) continue;
                name = name.replaceFirst("^key\\.", "");
                list.add(name);
            }
            return getListOfStringsMatchingLastWord(args, list);
        }
        return super.getTabCompletions(server, sender, args, pos);
    }
}
