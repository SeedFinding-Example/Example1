package com.seedfinding.neil;


import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.SwampHut;
import kaptainwutax.mathutils.component.vector.QVector;
import kaptainwutax.mathutils.util.Mth;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.RegionSeed;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.util.pos.RPos;
import kaptainwutax.mcutils.version.MCVersion;
import mjtb49.hashreversals.ChunkRandomReverser;
import mjtb49.hashreversals.Lattice2D;

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
