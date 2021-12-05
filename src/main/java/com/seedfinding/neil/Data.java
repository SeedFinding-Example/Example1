package com.seedfinding.neil;

import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcfeature.structure.Fortress;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.block.Blocks;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.terrain.NetherTerrainGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Data {

	public static MCVersion VERSION = MCVersion.v1_12;
	public static Fortress FORTRESS = new Fortress(VERSION);
	public static List<CPos> fortresses = new ArrayList<CPos>() {{
		add(new CPos(-46522, 17751));
		add(new CPos(-46440, 17687));
		add(new CPos(-46442, 17669));
		add(new CPos(-46522, 17751));
		add(new CPos(-46587, 17798));
		add(new CPos(-46600, 17657));
		add(new CPos(-46582, 17684));

		add(new CPos(-46603, 17620));
		add(new CPos(-46601, 17606));
		add(new CPos(-46602, 17562));
	}};


	public static void main(String[] args) throws IOException {
		System.out.println(-4172144997902289642L&0xFFFF_FFFF_FFFFL);
		List<Long> seeds = readFile();
		System.out.println("File read");
		long count = seeds.size();
		BufferedWriter writer = new BufferedWriter(new FileWriter("final.txt", false));
		seeds.stream().parallel().filter(s -> process(s, count))
				.filter(Data::processTerrain)
				.forEach(x -> {
					try {
						writer.write(x + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				})
		;
		writer.flush();
		writer.close();
	}

	public static AtomicLong atomicLong = new AtomicLong(0);

	public static boolean process(long seed, long max) {
		long current = atomicLong.getAndIncrement();
		if (current % (max / 100) == 0) {
			System.out.println((int) (((double) current) / (((double) max / 100.0))) + "%");
		}
		ChunkRand rand = new ChunkRand();
		for (CPos fortress : fortresses) {
			RPos rPos = fortress.toRegionPos(FORTRESS.getSpacing());
			CPos pos = FORTRESS.getInRegion(seed, rPos.getX(), rPos.getZ(), rand);
			if (pos == null || !pos.equals(fortress)) {
//				System.out.println("WRONG: "+seed);
				return false;
			}
		}
		return true;
	}

	public static List<BPos> terrain = new ArrayList<>() {{
		add(new BPos(-745705, 53, 280525));
		add(new BPos(-745700, 56, 280525));
		add(new BPos(-745700, 55, 280526));
		add(new BPos(-745706, 53, 280519));
		add(new BPos(-745706, 53, 280521));
		add(new BPos(-745705, 54, 280521));
		add(new BPos(-745705, 54, 280519));
		add(new BPos(-745704, 55, 280519));
		add(new BPos(-745703, 56, 280519));
		add(new BPos(-745700, 57, 280519));
		add(new BPos(-745700, 58, 280520));
	}};

	///tp NeilCaffrei -745706 53 280521
	public static boolean processTerrain(long seed) {
		BiomeSource biomeSource = BiomeSource.of(Dimension.NETHER, VERSION, seed);
		NetherTerrainGenerator netherChunkGenerator = (NetherTerrainGenerator) NetherTerrainGenerator.of(biomeSource);
		for (BPos pos : terrain) {
			Block[] column = netherChunkGenerator.getColumnAt(pos.getX(), pos.getZ());
			if (column[pos.getY()-1] != netherChunkGenerator.getDefaultBlock() || column[pos.getY() ] != Blocks.AIR) {
				return false;
			}
		}
		System.out.println(seed);
		return true;
	}

	public static List<Long> readFile() {
		InputStream in = Data.class.getResourceAsStream("/out.txt");
		assert in != null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		return reader.lines().parallel().map(x -> Long.parseLong(x, 16)).collect(Collectors.toList());
	}


}
