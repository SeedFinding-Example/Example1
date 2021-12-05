package com.seedfinding.neil;

import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mcfeature.structure.Stronghold;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;

import java.util.Arrays;
import java.util.List;

public class TestStronghold {
	public static void main(String[] args) {
		Stronghold stronghold = new Stronghold(MCVersion.v1_8);
		long worldSeed = 391430148412893L;
		OverworldBiomeSource obs = new OverworldBiomeSource(MCVersion.v1_8, worldSeed);
		ChunkRand chunkRand = new ChunkRand();
//Gets the first 3 strongholds, e.g. the first ring (consisting of 3)
		CPos[] strongholds = stronghold.getStarts(obs, 3, chunkRand);

		for (CPos strongholdPos : strongholds) {
			System.out.println(strongholdPos.toBlockPos());
		}
	}
}
