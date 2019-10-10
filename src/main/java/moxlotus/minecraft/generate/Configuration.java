package moxlotus.minecraft.generate;

import moxlotus.minecraft.Generate;
import net.minecraftforge.common.config.Config;

@Config(modid = Generate.MODID, category = "replacements")
public class Configuration{
    @Config.Name("Block{minecraft:cobblestone}")
    public static String[] cobblestone = new String[]{
            "16 minecraft:cobblestone default          - minecraft:sky",
            "4  minecraft:stone       variant=andesite - minecraft:sky",
            "2  minecraft:stone       variant=diorite  - minecraft:sky",
            "1  minecraft:stone       variant=granite  - minecraft:sky",
            "8  minecraft:end_stone   default          + minecraft:sky"
    };
    @Config.Name("Block{minecraft:stone}")
    public static String[] stone = new String[]{
            "16384 minecraft:stone",
            "1024  minecraft:coal_ore",
            "256   minecraft:iron_ore",
            "64    minecraft:redstone_ore",
            "32    minecraft:lapis_ore",
            "8     minecraft:gold_ore",
            "8     minecraft:gold_ore default + minecraft:extreme_hills minecraft:extreme_hills_with_trees minecraft:mutated_extreme_hills minecraft:mutated_extreme_hills_with_trees minecraft:smaller_extreme_hills",
            "1     minecraft:diamond_ore"
    };
    @Config.Name("Block{minecraft:obsidian}")
    public static String[] obsidian = new String[]{
            "1 minecraft:obsidian"
    };
}
