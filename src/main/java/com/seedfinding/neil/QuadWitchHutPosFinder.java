package com.seedfinding.neil;


import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mcfeature.structure.SwampHut;
import com.seedfinding.mcmath.component.vector.QVector;
import com.seedfinding.mcmath.util.Mth;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.RegionSeed;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcreversal.Lattice2D;
import com.seedfinding.mcreversal.ChunkRandomReverser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class QuadWitchHutPosFinder implements Runnable {
	private static final long[] REGION_SEEDS = getQuadRegionSeeds();
	public static final MCVersion VERSION = MCVersion.v1_17;
	public static final SwampHut SWAMP_HUT= new SwampHut(VERSION);
	private static final Lattice2D REGION_LATTICE = new Lattice2D(RegionSeed.A, RegionSeed.B, 1L << 48);

	public static final RegionStructure<?, ?> CURRENT_STRUCTURE = SWAMP_HUT;
	public static final int WORLD_SIZE=30_000_000;

	@Override
	public void run() {
		long worldSeed=152177820516312529L;
		int regionSize=CURRENT_STRUCTURE.getSpacing()*16;
		int numberRegions=WORLD_SIZE/regionSize;
		BiomeSource biomeSource = BiomeSource.of(CURRENT_STRUCTURE.getValidDimension(),VERSION, worldSeed);
		for (long regionSeed:REGION_SEEDS){
			for(QVector solution : REGION_LATTICE.findSolutionsInBox(regionSeed - worldSeed - CURRENT_STRUCTURE.getSalt(), -numberRegions, -numberRegions, numberRegions, numberRegions)) {
				int regX=solution.get(0).intValue();
				int regZ=solution.get(1).intValue();
				if(!checkBiomes(biomeSource, regX,regZ, CURRENT_STRUCTURE)) continue;
				System.out.println(new RPos(regX,regZ,regionSize).toBlockPos());
			}
		}
	}

	private static boolean checkBiomes(BiomeSource source, int regX, int regZ, RegionStructure<?,?> structure) {
		if(checkStructure(source, regX,regZ, structure)) return false;
		if(checkStructure(source,regX-1, regZ, structure)) return false;
		if(checkStructure(source, regX, regZ-1, structure)) return false;
		if(checkStructure(source, regX-1, regZ-1, structure)) return false;
		return true;
	}

	private static boolean checkStructure(BiomeSource source, int regX, int regZ, RegionStructure<?,?> structure) {
		CPos chunk = structure.getInRegion(source.getWorldSeed(), regX, regZ, new ChunkRand());
		return !structure.canSpawn(chunk.getX(), chunk.getZ(), source);
	}

	private static long moveStructure(long regionSeed, int regX, int regZ) {
		return regionSeed - regX * RegionSeed.A - regZ * RegionSeed.B & Mth.MASK_48;
	}

	private static long[] getQuadRegionSeeds() {
		InputStream stream = Main.class.getResourceAsStream("/regionSeeds.txt");
		return new BufferedReader(new InputStreamReader(Objects.requireNonNull(stream)))
				.lines().mapToLong(Long::parseLong).toArray();
	}

	public static void main(String[] args) {
		new QuadWitchHutPosFinder().run();
	}
}
