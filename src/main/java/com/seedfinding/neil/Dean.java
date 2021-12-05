package com.seedfinding.neil;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcbiome.source.NetherBiomeSource;
import com.seedfinding.mcfeature.GenerationContext;
import com.seedfinding.mcfeature.loot.ChestContent;
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.misc.SpawnPoint;
import com.seedfinding.mcfeature.structure.Fortress;
import com.seedfinding.mcfeature.structure.RuinedPortal;
import com.seedfinding.mcfeature.structure.generator.structure.RuinedPortalGenerator;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import com.seedfinding.mcterrain.terrain.OverworldTerrainGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Dean {

	private static String getCenteredString(String string, int length) {
		if (string.length() > length) {
			return string.substring(0, length);
		} else {
			int first = (int) Math.ceil((length - string.length()) / 2f);
			int rest = length - first;
			return String.format("%-" + first + "s%" + rest + "s", "", string);
		}
	}

	private static void log(String... strings) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			String string = strings[i];
			if (i > 0) {
				stringBuilder.append(" | ");
			}
			stringBuilder.append(getCenteredString(string, 20));
		}
		System.out.println(stringBuilder);
	}

	public static void main(String[] args) {
//		Program.init(36, 0L, 1L << 48); // Initialize the program object/class
//		Program.startProgram();
		long[] worldseeds = new long[] {
				-5435021523372627359L,
				5284039786355615169L,
				5250808169803558400L,
				4563302479365427488L,
				-6732123915264077088L,
				3735046512537120084L,
				-6799333968027105484L,
				53691536683994655L,
				2684915310392452857L,
				-6613424217475731372L,
				5632029331943637396L,
				-890224035907383464L,
				8793744013581115123L,
				-7465121495622567341L,
				4162113317044841714L,
				-4548799870899019203L,
				};
		for (long worldseed : worldseeds) {
			BiomeSource biomeSource = BiomeSource.of(Dimension.OVERWORLD, MCVersion.v1_16_3, worldseed);
			Biome biome = biomeSource.getBiome(-310, 35, 97);
			if (biome == Biomes.PLAINS) {
				System.out.println(worldseed);
			}
		}
	}

	private static class Program extends Thread implements Runnable {

		private static final int groupCount = 1000;
		private static int threadCount;
		private static long structureSeedStart;
		private static long structureSeedEnd;
		private static boolean initialized = false;

		private final int id;
		private final long rangeStart;
		private final long rangeEnd;

		public Program(int id) {
			if (id < 0 || id >= Program.groupCount) {
				throw new IllegalStateException("Invalid thread id.");
			}
			this.id = id;
			long fullRange = Program.structureSeedEnd - Program.structureSeedStart;
			this.rangeStart = Program.structureSeedStart + fullRange * this.id / Program.threadCount / Program.groupCount * 2;
			this.rangeEnd = Program.structureSeedStart + fullRange * (this.id + 1) / Program.threadCount / Program.groupCount * 2;
		}

		public static double distanceTo(Vec3i fromPos, Vec3i toPos) {
			return DistanceMetric.EUCLIDEAN_SQ.getDistance(fromPos.getX() - toPos.getX(), 0, fromPos.getZ() - toPos.getZ());
		}

		public static void init(int threadCount, long structureSeedStart, long structureSeedEnd) {
			Program.threadCount = threadCount;
			Program.structureSeedStart = structureSeedStart;
			Program.structureSeedEnd = structureSeedEnd;
			Program.initialized = true;
		}

		public static void startProgram() {
			if (Program.initialized) {
				log("Program", "Message", "World Seed", "Structure Seed", "Spawn Point", "Portal Position", "Portal Type", "Fortress Position", "Seed Count");
				for (int threadId = 0; threadId < Program.groupCount; threadId++) {
					new Program(threadId).start();
				}
			} else {
				throw new IllegalStateException("Unable to start the program without initialization, please make sure you have initialized the program before you started it.");
			}
		}

		public void findSeed() {
			log(String.format("Program %s", this.id), "Started finding");
			MCVersion version = MCVersion.v1_16_1;
			ChunkRand chunkRand = new ChunkRand(); // for optimization
			long seedCount = 0L;
			long validSeedCount = 0L;
			for (long structureSeed = this.rangeStart; structureSeed < this.rangeEnd; structureSeed++) {
				if (++seedCount % 1000000 == 0) {
					log(String.format("Program %s", this.id), String.format("%s/%s", validSeedCount, seedCount));
				}
				RuinedPortal portal = new RuinedPortal(Dimension.OVERWORLD, version);
				Fortress fortress = new Fortress(version);
				CPos[] portalPositions = new CPos[4];
				int idx = 0;
				for (int x = -1; x <= 0; x++) {
					for (int z = -1; z <= 0; z++) {
						portalPositions[idx++] = portal.getInRegion(structureSeed, x, z, chunkRand);
					}
				}
				CPos fortressCPos = null;
				CPos portalCPos = null;
				for (int x1 = -1; x1 <= 0; x1++) {
					for (int z1 = -1; z1 <= 0; z1++) {
						fortressCPos = fortress.getInRegion(structureSeed, x1, z1, chunkRand);
						if (fortressCPos == null) {
							continue;
						}
						for (CPos portalPos : portalPositions) {
							if (Program.distanceTo(portalPos.shr(3), fortressCPos) <= 8 * 8) {
								portalCPos = portalPos;
								break;
							}
						}
					}
					if (portalCPos != null) break;
				}
				if (portalCPos == null || fortressCPos == null) continue;

				long finalStructureSeed = structureSeed;
				RuinedPortalGenerator prePortalGenerator = new RuinedPortalGenerator(version) {

					private CPos cPos;
					public String type = null;
					public String type2 = null;
					public String type3 = null;

					@Override
					public boolean generateStructure(TerrainGenerator generator, int chunkX, int chunkZ, ChunkRand rand) {
						this.cPos = new CPos(chunkX, chunkZ);
						// Desert, Swamp and Ocean biomes
						rand.setCarverSeed(finalStructureSeed, chunkX, chunkZ, this.getVersion());
						long seed = rand.getSeed();

						if (rand.nextFloat() < 0.05F) {
							this.type = rand.getRandom(STRUCTURE_LOCATION_GIANT_PORTALS);
						} else {
							this.type = rand.getRandom(STRUCTURE_LOCATION_PORTALS);
						}
						// jungle, mountains with inside, nether and other with inside
						// (I don't recommend this one except for jungle and nether)
						// inside is nasty
						rand.setSeed(seed, false);
						rand.nextFloat();
						if (rand.nextFloat() < 0.05F) {
							this.type2 = rand.getRandom(STRUCTURE_LOCATION_GIANT_PORTALS);
						} else {
							this.type2 = rand.getRandom(STRUCTURE_LOCATION_PORTALS);
						}
						// mountain and other with not inside
						rand.setSeed(seed, false);
						rand.nextFloat();
						rand.nextFloat();
						if (rand.nextFloat() < 0.05F) {
							this.type3 = rand.getRandom(STRUCTURE_LOCATION_GIANT_PORTALS);
						} else {
							this.type3 = rand.getRandom(STRUCTURE_LOCATION_PORTALS);
						}

						return true;
					}

					@Override
					public String getType() {
						return type + ";;" + type2 + ";;" + type3;
					}


					@Override
					public List<Pair<ILootType, BPos>> getLootPos() {
						if (cPos == null) {
							return Collections.emptyList();
						}
						return Collections.singletonList(new Pair<>(LootType.RUINED_PORTAL, cPos.toBlockPos()));
					}
				};
				prePortalGenerator.generate(null, portalCPos, chunkRand);
				// we know that none of the correct type is in
				String portalType = prePortalGenerator.getType();
				if (!(portalType.contains("portal_6") || portalType.contains("portal_9"))) {
					continue;
				}

				if (!prePortalGenerator.getPossibleLootItems().containsAll(Arrays.asList(Items.GOLD_BLOCK, Items.GOLD_INGOT))) {
					throw new UnsupportedOperationException("Chest loots are missing");
				}

				if (portal.getLoot(structureSeed, prePortalGenerator, chunkRand, false).stream()
						.noneMatch(chestContent -> chestContent.contains(Items.GOLD_BLOCK) || chestContent.contains(Items.GOLD_INGOT))) {
					continue;
				}


				int totalGoldIngots = 0;
				int totalObsidian = 0;

				boolean containsFlintAndSteelOrFireCharge = false;
				boolean containsGoldenAxe = false;
				boolean containsGoldenApple = false;

				// this could be optimized by a LUT
				for (ChestContent chestContent : portal.getLoot(structureSeed, prePortalGenerator, chunkRand, false)) {

					totalGoldIngots += chestContent.getCount(item -> item.getName().equals(Items.GOLD_BLOCK.getName())) * 9 + chestContent.getCount(item -> item.getName().equals(Items.GOLD_INGOT.getName()));
					totalObsidian += chestContent.getCount(item -> item.getName().equals(Items.OBSIDIAN.getName()));

					containsFlintAndSteelOrFireCharge |= chestContent.contains(Items.FLINT_AND_STEEL) || chestContent.containsAtLeast(Items.FIRE_CHARGE, 2);
					containsGoldenAxe |= chestContent.contains(Items.GOLDEN_AXE);
					containsGoldenApple |= chestContent.contains(Items.GOLDEN_APPLE);
				}

				if (!(totalGoldIngots >= 15 && containsFlintAndSteelOrFireCharge && totalObsidian >= 1 && containsGoldenAxe && containsGoldenApple)) {
					continue;
				}

				for (long biomeSeed = 0L; biomeSeed < 1L << 16; biomeSeed++) {

					long worldSeed = biomeSeed << 48 | structureSeed;

					GenerationContext.Context context = GenerationContext.getContext(worldSeed, Dimension.OVERWORLD, version);

					if (!portal.canSpawn(portalCPos, context.getBiomeSource())) continue;
					RuinedPortalGenerator portalGenerator = new RuinedPortalGenerator(version);
					if (!portalGenerator.generate(context.getGenerator(), portalCPos, chunkRand)) {
						continue;
					}

					portalType = portalGenerator.getType();
					if (!(portalType.equals("portal_6") || portalType.equals("portal_9"))) {
						break;
					}
					if (portalType.equals("portal_9") && totalObsidian < 2) {
						break;
					}
					if (portalGenerator.getLocation() != RuinedPortalGenerator.Location.ON_LAND_SURFACE) {
						continue;
					}

					BPos spawn = SpawnPoint.getSpawn((OverworldTerrainGenerator) context.getGenerator());
					if (Program.distanceTo(spawn.toChunkPos(), portalCPos) > 2 * 2) continue;

					NetherBiomeSource nether = new NetherBiomeSource(version, worldSeed);
					if (!fortress.canSpawn(fortressCPos, nether)) continue;

					BPos portalBPos = portalCPos.toBlockPos();
					BPos fortressBPos = fortressCPos.toBlockPos();

					log(
							String.format("Program %s", this.id),
							"Found a seed",
							String.valueOf(worldSeed),
							String.valueOf(structureSeed),
							String.format("[%-5s, %-5s]", getCenteredString(String.valueOf(spawn.getX()), 5),
									getCenteredString(String.valueOf(spawn.getZ()), 5)),
							String.format("[%-5s, %-5s]", getCenteredString(String.valueOf(portalBPos.getX()), 5),
									getCenteredString(String.valueOf(portalBPos.getZ()), 5)),
							portalType,
							String.format("[%-5s, %-5s]", getCenteredString(String.valueOf(fortressBPos.getX()), 5),
									getCenteredString(String.valueOf(fortressBPos.getZ()), 5)),
							String.format("%s/%s", validSeedCount, seedCount)
					);
					++validSeedCount;
					break;
				}
			}
			log(String.format("Program %s", this.id), "Finished my work");
		}

		@Override
		public void run() {
			this.findSeed();
		}
	}
}
