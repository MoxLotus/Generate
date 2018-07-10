package moxlotus.minecraft;

import moxlotus.minecraft.generate.Configuration;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

@Mod(modid = Generate.MODID, name = Generate.NAME, version = Generate.VERSION)
@Mod.EventBusSubscriber
public final class Generate{
    public static final String MODID = "generate";
    public static final String NAME = "Generate";
    public static final String VERSION = "1.1";

    private static final List<ConfigEntry> list = new LinkedList<>();

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void postInit(FMLPostInitializationEvent event){
        list.add(new ConfigEntry(Blocks.COBBLESTONE, Configuration.cobblestone));
        list.add(new ConfigEntry(Blocks.STONE, Configuration.stone));
        list.add(new ConfigEntry(Blocks.OBSIDIAN, Configuration.obsidian));
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onFluidPlaceBlockEvent(BlockEvent.FluidPlaceBlockEvent event){
        for (ConfigEntry entry : list)
            if (event.getNewState().getBlock() == entry.toReplace){
                event.setNewState(entry.getNewBlock());
                return;
            }
    }

    private class ConfigEntry{
        final Block toReplace;
        final int totalWeight;
        final Collection<Pair> pairs;

        ConfigEntry(Block toReplace, String... strings){
            this.toReplace = toReplace;
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

                int biome = -1;//TODO

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
