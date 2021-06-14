package com.seedfinding.neil;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		System.out.println("Choose the program to execute among those :");
		for (Programs programs : Programs.values()) {
			System.out.printf("%d - %s: %s%n", programs.ordinal(), programs.name(), programs.description);
		}
		Scanner scanner = new Scanner(System.in);
		Integer ordinal = null;
		while (ordinal == null) {
			String nextLine = scanner.nextLine();
			try {
				ordinal = Integer.parseInt(nextLine);
				if (ordinal>= Programs.values().length || ordinal<0){
					ordinal=null;
					System.out.printf("You should only input a number between [%d;%d]%n",0, Programs.values().length-1);
				}
			} catch (NumberFormatException ignored) {
				System.out.println("Not a number!");
			}
		}
		Programs program= Programs.values()[ordinal];
		System.out.printf("Running %d: %s%n",program.ordinal(),program.description);
		Programs.values()[ordinal].program.run();
	}

	public enum Programs {
		WITCH_HUT_SEED_FINDER(new WitchHutSeedFinder(), "Find a witch hut seed with a set position"),
		WITCH_HUT_COORD_FINDER(new WitchHutPosFinder(), "Find witch hut position on a set seed"),
		QUAD_WITCH_HUT_SEED_FINDER(new QuadWitchHutFinder(), "Find quad witch hut seeds."),
		QUAD_WITCH_HUT_POS_FINDER(new QuadWitchHutPosFinder(), "Find quad witch hut pos on a set seed."),
		;
		Runnable program;
		String description;

		Programs(Runnable program, String description) {
			this.program = program;
			this.description = description;
		}
	}
}
