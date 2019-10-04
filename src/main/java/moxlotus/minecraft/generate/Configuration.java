package moxlotus.minecraft.generate;

import moxlotus.minecraft.Generate;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber
public class Configuration{
    private static String[] defaultCobblestone = new String[]{
            "16 minecraft:cobblestone",
            "4  minecraft:andesite",
            "2  minecraft:diorite",
            "1  minecraft:granite"
    };

    private static String[] defaultStone = new String[]{
            "16384 minecraft:stone",
            "1024  minecraft:coal_ore",
            "256   minecraft:iron_ore",
            "64    minecraft:redstone_ore",
            "32    minecraft:lapis_ore",
            "8     minecraft:gold_ore",
            "1     minecraft:diamond_ore"
    };

    private static String[] defaultObsidian = new String[]{
            "1 minecraft:obsidian"
    };

    public static String[] cobblestone;
    public static String[] stone;
    public static String[] obsidian;

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onFMLServerAboutToStartEvent(FMLServerAboutToStartEvent event){
        String[] strings = null;
        Path path = event.getServer().getActiveAnvilConverter().getFile(event.getServer().getFolderName(), "serverconfig").toPath();
        path = path.resolve("moxlotus/generate");

        String[] txt = readFromWorldTxt(path, "cobblestone");
        if (txt.length == 0) writeToWorldTxt(path, "cobblestone", defaultCobblestone);
        else Configuration.cobblestone = txt;

        txt = readFromWorldTxt(path, "stone");
        if (txt.length == 0) writeToWorldTxt(path, "stone", defaultStone);
        else Configuration.stone = txt;

        txt = readFromWorldTxt(path, "obsidian");
        if (txt.length == 0) writeToWorldTxt(path, "obsidian", defaultObsidian);
        else Configuration.obsidian = txt;

        Generate.setup();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onFMLServerStoppedEvent(FMLServerStoppedEvent event){
        Generate.teardown();
    }

    public static String[] readFromWorldTxt(Path folder, String fileName){
        String[] strings = null;
        if (!fileName.contains(".txt")) fileName += ".txt";
        Path configPath = folder.resolve(fileName);
        if (!configPath.toFile().exists()) return new String[0];
        try(
                FileReader fileReader = new FileReader(configPath.toFile());
                BufferedReader reader = new BufferedReader(fileReader)
        ){
            strings = reader.lines()
                    .map(String::trim)
                    .filter(line -> (Character.isDigit(line.charAt(0))))
                    .toArray(String[]::new);
        }catch (IOException ignore){}
        return strings;
    }

    public static void writeToWorldTxt(Path folder, String fileName, String... toWrite){
        if (!fileName.contains(".txt")) fileName += ".txt";
        Path configPath = folder.resolve(fileName);
        if (!configPath.toFile().exists())
            try{
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
            }catch (IOException ignore){}
        try(
                FileWriter fileWriter = new FileWriter(configPath.toFile());
                BufferedWriter writer = new BufferedWriter(fileWriter)
        ){
            for (String s : toWrite){
                writer.write(s);
                writer.newLine();
            }
            writer.flush();
        }catch (IOException ignore){}
    }
}
