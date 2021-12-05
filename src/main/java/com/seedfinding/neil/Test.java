package com.seedfinding.neil;

import com.seedfinding.mcfeature.loot.ChestContent;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.structure.BuriedTreasure;
import com.seedfinding.mcfeature.structure.generator.structure.BuriedTreasureGenerator;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;

import java.util.List;
import java.util.function.Predicate;

public class Test {
	public static final MCVersion VERSION=MCVersion.v1_17;
	public static final BuriedTreasure buriedTreasure = new BuriedTreasure(VERSION);
	public static void main(String[] args) {
		ChunkRand rand=new ChunkRand();
		long structureSeed=10;
		for (int x = -16; x < 16; x++) {
			for (int z = -16; z < 16; z++) { //initial iterators for buried treasures
				CPos pos = buriedTreasure.getInRegion(structureSeed, x, z, rand); //gets the buriedTreasure in the region
				if (pos != null) { //checks if the buriedTreasure might spawn
					if (lootCheck(MCVersion.v1_17, structureSeed, pos)) {
						System.out.println(pos);
					}
				}
			}
		}
	}
	public static boolean lootCheck(MCVersion mcVersion, Long structureSeed, CPos pos){
		ChunkRand rand = new ChunkRand();
		BuriedTreasureGenerator generator=new BuriedTreasureGenerator(mcVersion);
		generator.generate(null, pos,rand);
		List<ChestContent> chestContents=buriedTreasure.getLoot(structureSeed, generator, new ChunkRand(), true);
		if (chestContents.get(0).containsAtLeast(Items.TNT, 8)){
			if(chestContents.get(0).containsAtLeast(Items.IRON_INGOT, 2)){
				if(chestContents.get(0).containsAtLeast(Items.IRON_INGOT, 2)||chestContents.get(0).containsAtLeast(Items.GOLD_INGOT, 2)){
					System.out.println("Buried Treasure with loot in structureseed: " + structureSeed + " at " + pos);
					return true;
				}
			}
		}
		return false;
	}
}
