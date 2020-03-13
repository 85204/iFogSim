package org.fog.utils;

import org.cloudbus.cloudsim.power.models.PowerModel;

import java.util.ArrayList;

public class FogLinearWithSnapPowerModel implements PowerModel {

	public ArrayList<Double> powerHistory = new ArrayList<>();
	/**
	 * The max power.
	 */
	private double maxPower;
	/**
	 * The constant.
	 */
	private double a;
	/**
	 * The static power.
	 */
	private double staticPower;
	private double sleepPower;
	private String nodeName;

	public final float minFrequencyRatio = 1.0f / 3;

	/**
	 * Instantiates a power model.
	 *
	 * @param maxPower    the max power
	 * @param staticPower the static power
	 */
	public FogLinearWithSnapPowerModel(String nodeName, double maxPower, double staticPower, double sleepPower) {
		this.nodeName = nodeName;
		setMaxPower(maxPower);
		setStaticPower(staticPower);
		setSleepPower(sleepPower);
		// P(max) = aU(max)^2
		a = maxPower;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.PowerModel#getPower(double)
	 */
	@Override
	public double getPower(double utilization) throws IllegalArgumentException {
		if (utilization < 0 || utilization > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		if (nodeName.startsWith("_mobile"))
			System.out.println(nodeName + ':' + utilization);
		if (utilization == 0 && nodeName.startsWith("_mobile")) {
			return sleepPower;
		}
		// 资源占用率低于最小频率的，此时频率只能为最小频率，所以返回最小功耗
		if (utilization <= minFrequencyRatio) {
			return staticPower;
		}
		return a * utilization * utilization;
	}

	/**
	 * Gets the max power.
	 *
	 * @return the max power
	 */
	protected double getMaxPower() {
		return maxPower;
	}

	/**
	 * Sets the max power.
	 *
	 * @param maxPower the new max power
	 */
	protected void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	/**
	 * Gets the static power.
	 *
	 * @return the static power
	 */
	protected double getStaticPower() {
		return staticPower;
	}

	/**
	 * Sets the static power.
	 *
	 * @param staticPower the new static power
	 */
	protected void setStaticPower(double staticPower) {
		this.staticPower = staticPower;
	}

	public void setSleepPower(double sleepPower) {
		this.sleepPower = sleepPower;
	}
}
