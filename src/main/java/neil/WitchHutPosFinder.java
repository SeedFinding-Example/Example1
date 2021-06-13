package neil;

import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.SwampHut;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;


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

	public static void main(String[] args) {
		new WitchHutPosFinder().run();
	}
}
