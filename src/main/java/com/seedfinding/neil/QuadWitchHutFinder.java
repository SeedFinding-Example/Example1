package com.seedfinding.neil;

import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.SwampHut;
import kaptainwutax.mathutils.util.Mth;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.RegionSeed;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class QuadWitchHutFinder implements Runnable {
	private static final long[] REGION_SEEDS = getQuadRegionSeeds();
	public static final MCVersion VERSION = MCVersion.v1_17;
	public static final SwampHut SWAMP_HUT= new SwampHut(VERSION);

	public static final RegionStructure<?, ?> CURRENT_STRUCTURE = SWAMP_HUT;

	@Override
	public void run() {
		int regX=10;
		int regZ=10;
		for (long regionSeed:REGION_SEEDS){
			long structureSeed=moveStructure(regionSeed,regX,regZ)-CURRENT_STRUCTURE.getSalt();
			WorldSeed.getSisterSeeds(structureSeed).asStream().filter(worldSeed->{
				BiomeSource biomeSource=BiomeSource.of(CURRENT_STRUCTURE.getValidDimension(),VERSION,worldSeed);
				return checkBiomes(biomeSource,regX,regZ,CURRENT_STRUCTURE);
			}).forEach(e-> System.out.println(e+" "+regionSeed));
		}
	}

	private static boolean checkBiomes(BiomeSource source, int regX,int regZ, RegionStructure<?,?> structure) {
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
		new QuadWitchHutFinder().run();
	}
}
