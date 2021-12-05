package com.seedfinding.neil;

import com.seedfinding.neil.util.StructureHelper;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mcfeature.structure.SwampHut;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;

import java.util.Objects;


public class WitchHutPosFinder implements Runnable{
	public static final MCVersion VERSION = MCVersion.v1_17;
	public static final SwampHut SWAMP_HUT= new SwampHut(VERSION);

	public static final RegionStructure<?, ?> CURRENT_STRUCTURE = SWAMP_HUT;

	public static final int BOUND=200;
	@Override
	public void run() {
		ChunkRand chunkRand=new ChunkRand();
		long worldSeed=1L;
		BiomeSource source=BiomeSource.of(CURRENT_STRUCTURE.getValidDimension(),VERSION,worldSeed);

		for (int regX = -BOUND; regX <= BOUND; regX++) {
			for (int regZ = -BOUND; regZ <= BOUND; regZ++) {
				CPos cPos=CURRENT_STRUCTURE.getInRegion(worldSeed,regX,regZ,chunkRand);
				if (cPos==null) continue;
				if (CURRENT_STRUCTURE.canSpawn(cPos,source)){
					System.out.printf("/tp @p %d ~ %d%n",cPos.toBlockPos().getX(),cPos.toBlockPos().getZ());
				}
			}
		}
	}

	public void withHelper(){
		long worldSeed=1L;
		BiomeSource source=BiomeSource.of(CURRENT_STRUCTURE.getValidDimension(),VERSION,worldSeed);
		BPos center=new BPos(0,0,0);
		Objects.requireNonNull(StructureHelper.getClosest(CURRENT_STRUCTURE, center, worldSeed, source, null, 1))
				.parallel()
				.limit(10000)
				.forEach(bpos->System.out.printf("/tp @p %d ~ %d%n",bpos.getX(),bpos.getZ()));

	}

	public static void main(String[] args) {
		new WitchHutPosFinder().run();
		new WitchHutPosFinder().withHelper();
	}
}
