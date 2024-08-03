package me.dunescifye.sivilsenchants.listeners;

import me.dunescifye.sivilsenchants.SivilsEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;
import java.util.function.Predicate;

public class PlayerBlockBreakListener implements Listener {

    private Enchantment timber;

    private static final List<Predicate<Block>> logs = List.of(
            block -> block.getType().equals(Material.OAK_LOG),
            block -> block.getType().equals(Material.SPRUCE_LOG),
            block -> block.getType().equals(Material.BIRCH_LOG),
            block -> block.getType().equals(Material.JUNGLE_LOG),
            block -> block.getType().equals(Material.ACACIA_LOG),
            block -> block.getType().equals(Material.DARK_OAK_LOG),
            block -> block.getType().equals(Material.MANGROVE_LOG),
            block -> block.getType().equals(Material.CHERRY_LOG),
            block -> block.getType().equals(Material.CRIMSON_STEM),
            block -> block.getType().equals(Material.WARPED_STEM)
    );
    public void playerBlockBreakHandler(SivilsEnchants plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        timber = Registry.ENCHANTMENT.get(NamespacedKey.fromString("timber"));
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.hasItemMeta()) {
            if (item.containsEnchantment(timber)) {
                for (Predicate<Block> predicate : logs) {
                    if (predicate.test(b)) {
                        veinMine(b, p.getInventory().getItemInMainHand(), item.getEnchantmentLevel(timber) * 20);
                        return;
                    }
                }
            }
        }
    }


    public static void veinMine(Block block, ItemStack heldItem, int max) {
        Set<Block> blocksToBreak = new HashSet<>();
        Collection<ItemStack> drops = new ArrayList<>();
        findVein(block, block.getType(), blocksToBreak, 0, max);

        // Break all collected blocks
        for (Block b : blocksToBreak) {
            drops.addAll(b.getDrops(heldItem));
            b.setType(Material.AIR);
        }

        for (ItemStack drop : mergeSimilarItemStacks(drops)){
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }
    }

    private static void findVein(Block block, Material blockType, Set<Block> blocksToBreak, int blocksBroken, int max) {
        if (block.getType() != blockType || blocksToBreak.contains(block) || blocksBroken > max) {
            return;
        }

        blocksBroken++;
        blocksToBreak.add(block);

        for (Block adjacent : getAdjacentBlocks(block)) {
            findVein(adjacent, blockType, blocksToBreak, blocksBroken, max);
        }
    }

    private static Set<Block> getAdjacentBlocks(Block block) {
        Set<Block> adjacentBlocks = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block adjacent = block.getRelative(x, y, z);
                    adjacentBlocks.add(adjacent);
                }
            }
        }

        return adjacentBlocks;
    }

    public static Collection<ItemStack> mergeSimilarItemStacks(Collection<ItemStack> itemStacks) {
        Map<Material, ItemStack> mergedStacksMap = new HashMap<>();

        for (ItemStack stack : itemStacks) {
            Material material = stack.getType();
            ItemStack existing = mergedStacksMap.get(material);
            if (existing == null) {
                mergedStacksMap.put(material, stack.clone());
            } else {
                existing.setAmount(existing.getAmount() + stack.getAmount());
            }
        }
        return mergedStacksMap.values();
    }
}
