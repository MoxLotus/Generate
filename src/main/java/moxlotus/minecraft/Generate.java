package moxlotus.minecraft;

import moxlotus.minecraft.generate.Configuration;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
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
    public static final String VERSION = "1.2";

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
                Biome biome = event.getWorld().getBiome(event.getPos());
                IBlockState state = entry.getNewBlock(biome);
                if (state != null) event.setNewState(state);
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

                boolean exclusive_exclude = false;
                List<Biome> list = new LinkedList<>();

                if (split.length >= 4){
                    exclusive_exclude = !split[3].equals("-");
                    for (int i = 4; i < split.length; i++){
                        Biome b = Biome.REGISTRY.getObject(new ResourceLocation(split[i]));
                        if (b != null) list.add(b);
                    }
                }
                if (list.isEmpty()) exclusive_exclude = false;

                pairs.add(new Pair(state, weight, exclusive_exclude, list));
                totalWeight += weight;                                        //Calculate total weight
            }
            this.totalWeight = totalWeight;
        }

        private boolean hasOptionForBiome(Biome biome){
            for (Pair pair : pairs)
                if (pair.isBiomeValid(biome)) return true;
            return false;
        }

        private IBlockState getNewBlock(Biome biome){
            if (!hasOptionForBiome(biome)) return null;
            Pair pair;
            loop: do {
                int i = new Random().nextInt(totalWeight) + 1; //Pick a number between 1 and the total weight
                int currentWeight = 0;
                for (Pair p : pairs) {    //Iterate through pairs
                    currentWeight += p.getWeight();             //Calculate current weight
                    if (currentWeight >= i){
                        pair =  p; //Until we reach the selected weight from before
                        continue loop;
                    }
                }
                throw new Error("This should never happen.");  //We should never pick a number bigger than the total weight
            }while(!pair.isBiomeValid(biome));
            return pair.getState();
        }

        private class Pair{
            private final IBlockState state;
            private final int weight;
            private final boolean exclusive;
            private final List<Biome> biomes;
            Pair(IBlockState state, int weight, boolean exclusive, List<Biome> biomes){
                this.state = state;
                this.weight = weight;
                this.exclusive = exclusive;
                this.biomes = biomes;
            }

            public int getWeight(){
                return weight;
            }

            public IBlockState getState(){
                return state;
            }

            public boolean isBiomeValid(Biome biome){
                if (biomes.isEmpty()) return true;
                return exclusive == biomes.contains(biome);
            }
        }
    }
}
