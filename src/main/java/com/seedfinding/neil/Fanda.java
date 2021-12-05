package com.seedfinding.neil;

import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcfeature.structure.BuriedTreasure;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.StructureSeed;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public class Fanda {
	public static final MCVersion MC_VERSION = MCVersion.v1_17;
	public static final BuriedTreasure BURIED_TREASURE = new BuriedTreasure(MC_VERSION);
	public static final RegionStructure<?, ?> STRUCTURE = BURIED_TREASURE;
	public static final int RANGE = 2;
	public static final int REGION_STEP = STRUCTURE.getSpacing();
	public static final int REGION_RANGE = RANGE * REGION_STEP;
	public static final int THRESHOLD = 5;

	public static void main(String[] args) {
		LongStream.range(0, 1L << 48).boxed().parallel().forEach(Fanda::processSingleStructureSeed);
	}

	public static void processSingleStructureSeed(long structureSeed) {
		ChunkRand rand = new ChunkRand();
		List<CPos> chunkPositions = new ArrayList<>((RANGE * 2 + 1) * 2);
		for (int rx = -REGION_RANGE; rx <= REGION_RANGE; rx += REGION_STEP) {
			for (int rz = -REGION_RANGE; rz < REGION_RANGE; rz += REGION_STEP) {
				CPos cpos = STRUCTURE.getInRegion(structureSeed, rx, rz, rand);
				if (cpos != null) {
					chunkPositions.add(cpos);
				}
			}
		}
		// discard structure seed if not enough possible chests
		if (chunkPositions.size() < THRESHOLD) {
			return;
		}

		for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
			long seed = StructureSeed.toWorldSeed(structureSeed, upperBits);
			BiomeSource bs = BiomeSource.of(Dimension.OVERWORLD, MC_VERSION, seed);
			if (chunkPositions.stream().allMatch(cPos -> BURIED_TREASURE.canSpawn(cPos.getX(), cPos.getZ(), bs))) {
				System.out.println(seed);
			}

		}
	}


}
