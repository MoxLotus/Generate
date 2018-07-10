package moxlotus.minecraft.generate;

import moxlotus.minecraft.Generate;
import net.minecraftforge.common.config.Config;

@Config(modid = Generate.MODID, category = "replacements")
public class Configuration{
    @Config.Name("Block{minecraft:cobblestone}")
    public static String[] cobblestone = new String[]{
            "16 minecraft:cobblestone",
            "4  minecraft:stone       variant=andesite",
            "2  minecraft:stone       variant=diorite",
            "1  minecraft:stone       variant=granite"
    };
    @Config.Name("Block{minecraft:stone}")
    public static String[] stone = new String[]{
            "16384 minecraft:stone",
            "1024  minecraft:coal_ore",
            "256   minecraft:iron_ore",
            "64    minecraft:redstone_ore",
            "32    minecraft:lapis_ore",
            "8     minecraft:gold_ore",
            "1     minecraft:diamond_ore"
    };
    @Config.Name("Block{minecraft:obsidian}")
    public static String[] obsidian = new String[]{
            "1 minecraft:obsidian"
    };
}
