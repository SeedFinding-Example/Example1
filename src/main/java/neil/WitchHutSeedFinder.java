package neil;

import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.DesertPyramid;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.SwampHut;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.StructureSeed;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.util.pos.RPos;
import kaptainwutax.mcutils.version.MCVersion;

import java.util.ArrayList;
import java.util.List;

public class WitchHutSeedFinder implements Runnable {
	// find a seed with a few specific SwampHut
	public static final MCVersion VERSION = MCVersion.v1_17;
	public static final SwampHut SWAMP_HUT= new SwampHut(VERSION);

	public static final RegionStructure<?, ?> CURRENT_STRUCTURE = SWAMP_HUT;

	public static final RPos[] POSITIONS = new RPos[] {
			new RPos(0, 0, CURRENT_STRUCTURE.getSpacing() * 16),
			new RPos(0, 1, CURRENT_STRUCTURE.getSpacing() * 16)
	};


	@Override
	public void run() {
		ChunkRand chunkRand=new ChunkRand();
		for (long structureSeed = WorldSeed.toStructureSeed(2453054422033367042L); structureSeed < 1L << 48; structureSeed++) {
			List<CPos> cPosList=new ArrayList<>(POSITIONS.length);
			for (RPos rPos:POSITIONS){
				CPos cPos=CURRENT_STRUCTURE.getInRegion(structureSeed,rPos.getX(),rPos.getZ(),chunkRand);
				if (cPos!=null){
					cPosList.add(cPos);
				}
			}
			if (cPosList.size() < POSITIONS.length) continue;
			if (!(DistanceMetric.EUCLIDEAN.getDistance(
					cPosList.get(0).getX()-cPosList.get(1).getX(),
					cPosList.get(0).getY()-cPosList.get(1).getY(),
					cPosList.get(0).getZ()-cPosList.get(1).getZ())<13)){
				continue;
			}
			for (long upperBits = 0; upperBits <1L<<16; upperBits++) {
				long worldSeed= StructureSeed.toWorldSeed(structureSeed,upperBits);
				BiomeSource biomeSource=BiomeSource.of(Dimension.OVERWORLD,VERSION,worldSeed);
				boolean canSpawnAtBoth=true;
				for (CPos cPos:cPosList){
					canSpawnAtBoth &= CURRENT_STRUCTURE.canSpawn(cPos,biomeSource);
				}
				if (canSpawnAtBoth) System.out.println(worldSeed);
			}
		}
	}
	public static void main(String[] args) {
		new WitchHutSeedFinder().run();
	}
}
