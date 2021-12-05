package com.seedfinding.neil;

import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mcfeature.structure.SwampHut;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.StructureSeed;
import com.seedfinding.mccore.rand.seed.WorldSeed;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class WitchHutSeedFinder implements Runnable {
	// find a seed with a few specific SwampHut
	public static final MCVersion VERSION = MCVersion.v1_17;
	public static final SwampHut SWAMP_HUT = new SwampHut(VERSION);

	public static final RegionStructure<?, ?> CURRENT_STRUCTURE = SWAMP_HUT;

	public static final RPos[] POSITIONS = new RPos[] {
			new RPos(0, 0, CURRENT_STRUCTURE.getSpacing() * 16),
			new RPos(0, 1, CURRENT_STRUCTURE.getSpacing() * 16)
	};


	@Override
	public void run() {
		ChunkRand chunkRand = new ChunkRand();
		for (long structureSeed = 0; structureSeed < 1L << 48; structureSeed++) {
			List<CPos> cPosList = new ArrayList<>(POSITIONS.length);
			for (RPos rPos : POSITIONS) {
				CPos cPos = CURRENT_STRUCTURE.getInRegion(structureSeed, rPos.getX(), rPos.getZ(), chunkRand);
				if (cPos != null) {
					cPosList.add(cPos);
				}
			}
			if (cPosList.size() < POSITIONS.length) continue;
			if (!(DistanceMetric.EUCLIDEAN.getDistance(
					cPosList.get(0).getX() - cPosList.get(1).getX(),
					cPosList.get(0).getY() - cPosList.get(1).getY(),
					cPosList.get(0).getZ() - cPosList.get(1).getZ()) < 13)) {
				continue;
			}
			for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
				long worldSeed = StructureSeed.toWorldSeed(structureSeed, upperBits);
				BiomeSource biomeSource = BiomeSource.of(Dimension.OVERWORLD, VERSION, worldSeed);
				boolean canSpawnAtBoth = true;
				for (CPos cPos : cPosList) {
					canSpawnAtBoth &= CURRENT_STRUCTURE.canSpawn(cPos, biomeSource);
				}
				if (canSpawnAtBoth) System.out.println(worldSeed);
			}
		}
	}

	public void withStreams() {
		LongStream.rangeClosed(0, 1L << 48)
				.parallel()
				.mapToObj(structureSeed -> new Pair<>(Arrays.stream(POSITIONS).map(rPos ->
						CURRENT_STRUCTURE.getInRegion(structureSeed, rPos.getX(), rPos.getZ(),  new ChunkRand()))
						.filter(Objects::nonNull)
						.collect(Collectors.toList()),
						structureSeed)
				)
				.filter(pair -> pair.getFirst().size() == POSITIONS.length)
				.filter(pair -> DistanceMetric.EUCLIDEAN.getDistance(
						pair.getFirst().get(0).getX() - pair.getFirst().get(1).getX(),
						pair.getFirst().get(0).getY() - pair.getFirst().get(1).getY(),
						pair.getFirst().get(0).getZ() - pair.getFirst().get(1).getZ()) < 13)
				.forEach(pair -> StructureSeed.getWorldSeeds(pair.getSecond()).asStream().forEach(
						worldSeed -> {
							BiomeSource biomeSource = BiomeSource.of(Dimension.OVERWORLD, VERSION, worldSeed);
							boolean canSpawnAtBoth = true;
							for (CPos cPos : pair.getFirst()) {
								canSpawnAtBoth &= CURRENT_STRUCTURE.canSpawn(cPos, biomeSource);
							}
							if (canSpawnAtBoth) System.out.println(worldSeed);
						}
				));
	}

	public static void main(String[] args) {
		new WitchHutSeedFinder().run();
		new WitchHutSeedFinder().withStreams();
	}
}
