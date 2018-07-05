package moxlotus.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

@Mod(modid = Generate.MODID, name = Generate.NAME, version = Generate.VERSION)
@Mod.EventBusSubscriber
public final class Generate{
    public static final String MODID = "generate";
    public static final String NAME = "Generate";
    public static final String VERSION = "1.0";

    private static final List<ConfigEntry> list = new LinkedList<>();
    private static Configuration config;

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void preInit(FMLPreInitializationEvent event){
        config = new Configuration(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void postInit(FMLPostInitializationEvent event){
        //Defaults
        String[] cobblestone = new String[]{"16 minecraft:cobblestone", " 4 minecraft:stone variant=andesite", " 2 minecraft:stone variant=diorite", " 1 minecraft:stone variant=granite"};
        String[] stone = new String[]{"16384 minecraft:stone", " 1024 minecraft:coal_ore", "  256 minecraft:iron_ore", "   64 minecraft:redstone_ore", "   32 minecraft:lapis_ore", "   8  minecraft:gold_ore", "    1 minecraft:diamond_ore"};
        String[] obsidian = new String[]{"1 minecraft:obsidian"};

        config.load();
        list.add(new ConfigEntry(Blocks.COBBLESTONE, cobblestone));
        list.add(new ConfigEntry(Blocks.STONE, stone));
        list.add(new ConfigEntry(Blocks.OBSIDIAN, obsidian));
        config.save();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onFluidPlaceBlockEvent(BlockEvent.FluidPlaceBlockEvent event){
        for (ConfigEntry entry : list)
            if(event.getNewState().getBlock() == entry.toReplace){
                event.setNewState(entry.getNewBlock());
                return;
            }
    }

    private class ConfigEntry{
        final Block toReplace;
        final int totalWeight;
        final Collection<Pair> pairs;

        ConfigEntry(Block toReplace, String... defaults){
            this.toReplace = toReplace;
            String[] strings = config.get("Replacements", toReplace.toString(), defaults).getStringList();
            //Pairs will be sorted from greatest weight to least
            pairs = new PriorityQueue<>(Collections.reverseOrder(Comparator.comparingInt(Pair::getWeight)));
            int totalWeight = 0;
            for (String string : strings){                         //Read strings
                String s = string.trim();
                String[] split = s.split("\\s+");
                if (split.length < 2) continue;

                int weight = Integer.parseInt(split[0]);                      //Read weight
                Block block = Block.getBlockFromName(split[1]);               //Read Block
                if (block == null) continue;
                IBlockState state = null;
                if (split.length >= 3){
                    try{
                        state = CommandBase.convertArgToBlockState(block, split[2]); //Read state
                    }catch (NumberInvalidException | InvalidBlockStateException ignore){}
                }
                if (state == null) state = block.getDefaultState();           //Or use default state

                pairs.add(new Pair(state, weight));
                totalWeight += weight;                                        //Calculate total weight
            }
            this.totalWeight = totalWeight;
        }

        private IBlockState getNewBlock(){
            int i = new Random().nextInt(totalWeight) + 1; //Pick a number between 1 and the total weight
            int currentWeight = 0;
            for (Pair p : pairs){    //Iterate through pairs
                currentWeight += p.getWeight();             //Calculate current weight
                if (currentWeight >= i) return p.getState(); //Until we reach the selected weight from before
            }
            throw new Error("This should never happen.");  //We should never pick a number bigger than the total weight
        }

        private class Pair{
            private final IBlockState state;
            private final int weight;
            Pair(IBlockState state, int weight){
                this.state = state;
                this.weight = weight;
            }

            public int getWeight(){
                return weight;
            }

            public IBlockState getState(){
                return state;
            }
        }
    }
}
