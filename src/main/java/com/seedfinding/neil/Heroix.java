package com.seedfinding.neil;

import com.seedfinding.mccore.rand.seed.RegionSeed;
import com.seedfinding.mccore.rand.seed.StructureSeed;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mcfeature.structure.SwampHut;
import com.seedfinding.mcmath.component.vector.QVector;
import com.seedfinding.mcreversal.Lattice2D;
import one.util.streamex.StreamEx;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class Heroix implements Runnable {
	public static final MCVersion VERSION_12 = MCVersion.v1_12;
	public static final MCVersion VERSION_13 = MCVersion.v1_13;
	public static final SwampHut SWAMP_HUT_12 = new SwampHut(VERSION_12);
	public static final SwampHut SWAMP_HUT_13 = new SwampHut(VERSION_13);
	// shrink it to the desired max distance to travel (will do +/-)
	public static final int WORLD_SIZE = 10_000_000;
	private static final long[] REGION_SEEDS = getQuadRegionSeeds();
	private static final Lattice2D REGION_LATTICE = new Lattice2D(RegionSeed.A, RegionSeed.B, 1L << 48);
	private static final RegionStructure<?, ?> COMMON_STRUCTURE = SWAMP_HUT_12;
	// for one million blocks out the max distance is in RPOS
	private static final Double MAX_DISTANCE = 7812.0;
	private static final int NUMBER_THREADS = 16;

	private static long[] getQuadRegionSeeds() {
		InputStream stream = Main.class.getResourceAsStream("/regionSeeds.txt");
		return new BufferedReader(new InputStreamReader(Objects.requireNonNull(stream)))
				.lines().mapToLong(Long::parseLong).toArray();
	}

	@Override
	public void run() {
		ForkJoinPool pool = new ForkJoinPool(NUMBER_THREADS);
		StreamEx.of(StructureSeed.iterator().asStream().boxed()).parallel(pool).forEach(this::run);
	}

	public void run(long structureSeed) {
		int regionSize = COMMON_STRUCTURE.getSpacing() * 16;
		int numberRegions = WORLD_SIZE / regionSize;
		List<QVector> LIST_12 = new ArrayList<>();
		List<QVector> LIST_13 = new ArrayList<>();
		for (long regionSeed : REGION_SEEDS) {
			LIST_12.addAll(REGION_LATTICE.findSolutionsInBox(regionSeed - structureSeed - SWAMP_HUT_12.getSalt(), -numberRegions, -numberRegions, numberRegions, numberRegions));
			LIST_13.addAll(REGION_LATTICE.findSolutionsInBox(regionSeed - structureSeed - SWAMP_HUT_13.getSalt(), -numberRegions, -numberRegions, numberRegions, numberRegions));
		}
		if (LIST_12.isEmpty() || LIST_13.isEmpty()) {
			return;
		}
		// we could use kdtree or shortest spatial along an axis Divide and conquer but what the hell
		List<RPos> LIST_RPOS_12 = LIST_12.stream().map(x -> new RPos(x.get(0).intValue(), x.get(1).intValue(), regionSize)).collect(Collectors.toList());
		List<RPos> LIST_RPOS_13 = LIST_13.stream().map(x -> new RPos(x.get(0).intValue(), x.get(1).intValue(), regionSize)).collect(Collectors.toList());

		for (RPos rpos12 : LIST_RPOS_12) {
			for (RPos rPos13 : LIST_RPOS_13) {
				// those can only be 117186 at max
				int distX = rpos12.getX() - rPos13.getX();
				int distZ = rpos12.getZ() - rPos13.getZ();
				// however multiplication will make them go wild so we use manhattan
				Double distance = DistanceMetric.MANHATTAN.getDistance(distX, 0, distZ);
				if (distance < MAX_DISTANCE) {
					System.out.printf("Distance in region : %f for RPOS_12 %s and RPOS_13 %s for seed %s\n", distance, rpos12.toBlockPos(), rPos13.toBlockPos(), structureSeed);

				}
			}
		}

	}

	public static void main(String[] args) {
		new Heroix().run();
	}
}
