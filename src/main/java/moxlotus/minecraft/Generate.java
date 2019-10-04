package moxlotus.minecraft;

import moxlotus.minecraft.generate.Configuration;
import moxlotus.minecraft.generate.Node;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod(Generate.MODID)
@Mod.EventBusSubscriber
public final class Generate{
    public static final String MODID = "generate";
    public static final String NAME = "Generate";
    public static final String VERSION = "2.0-1.14";

    private static List<ConfigEntry> list = new LinkedList<>();

    public static void setup(){
        list.add(new ConfigEntry(Blocks.COBBLESTONE, Configuration.cobblestone));
        list.add(new ConfigEntry(Blocks.STONE, Configuration.stone));
        list.add(new ConfigEntry(Blocks.OBSIDIAN, Configuration.obsidian));
    }

    public static void teardown(){
        list = new LinkedList<>();
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

    private static class ConfigEntry{
        final Block toReplace;
        final int totalWeight;
        final Node<BlockState> tree;

        ConfigEntry(Block toReplace, String... strings){
            this.toReplace = toReplace;
            //Pairs will be sorted from greatest weight to least
            Collection<Pair> pairs = new PriorityQueue<>(Collections.reverseOrder(Comparator.comparingInt(Pair::getWeight)));
            int totalWeight = 0;
            for (String string : strings){                         //Read strings
                String s = string.trim();
                String[] split = s.split("\\s+");
                if (split.length < 2) continue;

                int weight = Integer.parseInt(split[0]);                      //Read weight
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[1]));//Read Block
                if (block == null) continue;
                BlockState state = null;
                if (split.length >= 3) state = convertArgToBlockState(block, split[2]); //Read state
                if (state == null) state = block.getDefaultState();           //Or use default state

                int biome = -1;//TODO

                pairs.add(new Pair(state, weight));
                totalWeight += weight;                                        //Calculate total weight
            }
            this.totalWeight = totalWeight;
            Iterator<Pair> iter = pairs.iterator();
            Pair pair = iter.next();
            Node<BlockState> root = Node.makeRoot(pair.weight, pair.state);
            while(iter.hasNext()){
                pair = iter.next();
                root.addChild(pair.weight, pair.state);
            }
            tree = root;
        }

        private static <T extends Comparable<T>> BlockState convertArgToBlockState(Block block, String s){
            String[] split = s.split("=");
            IProperty<T> property = (IProperty<T>) block.getStateContainer().getProperty(split[0]);
            if (property == null) return null;
            Optional<T> value = property.parseValue(split[1]);
            if (!value.isPresent()) return null;
            return block.getDefaultState().with(property, value.get());
        }

        private BlockState getNewBlock(){
            return tree.select(new Random().nextInt(tree.getSize() + 1));
        }

        private class Pair{
            private final BlockState state;
            private final int weight;
            Pair(BlockState state, int weight){
                this.state = state;
                this.weight = weight;
            }

            public int getWeight(){
                return weight;
            }

            public BlockState getState(){
                return state;
            }
        }
    }
}
