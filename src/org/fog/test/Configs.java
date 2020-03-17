package org.fog.test;

import org.fog.test.perfeval.VRGameFog;


public class Configs {
	public static int USE_ANOTHER_DEVICE = 0;
	public static int MIPS = 1600;
	public static int TRANSPORT_TASK_NUM = 5;
	public static int MEAN = 6;
	public static int STD_DEV = 1;
	public static double a = 0;
	public static double aP = 0;
	public static double b = 0;
	public static double bP = 0;
	public static Thread aa;
	public static double getMaxTolerateDelay() {
		return 5;
	}


	public static void main(String[] args) {

		USE_ANOTHER_DEVICE = Integer.parseInt(System.getProperty("test"));
		MIPS = Integer.parseInt(System.getProperty("mips"));
		MEAN = Integer.parseInt(System.getProperty("mean"));
		aa = new Thread(() -> VRGameFog.main(new String[]{}));
		aa.start();
	}

	public static void another() {
		USE_ANOTHER_DEVICE = 1;
		aa.interrupt();
		System.exit(0);
		aa = new Thread(() -> VRGameFog.main(new String[]{}));
		aa.start();
	}
}
