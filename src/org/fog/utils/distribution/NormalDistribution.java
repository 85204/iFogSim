package org.fog.utils.distribution;

import java.util.Random;

public class NormalDistribution extends Distribution {

	private double mean;
	private double stdDev;

	public NormalDistribution(double mean, double stdDev) {
		setMean(mean);
		setStdDev(stdDev);
		setRandom(new Random());
	}

	@Override
	public double getNextValue() {
		return 40;
//		double value = random.nextGaussian() * stdDev + mean;
//		System.out.println(value);
//		return value;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getStdDev() {
		return stdDev;
	}

	public void setStdDev(double stdDev) {
		this.stdDev = stdDev;
	}

	@Override
	public int getDistributionType() {
		return Distribution.NORMAL;
	}

	@Override
	public double getMeanInterTransmitTime() {
		return mean;
	}

}
