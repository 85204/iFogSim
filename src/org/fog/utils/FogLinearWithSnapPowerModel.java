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
	private double constant;
	/**
	 * The static power.
	 */
	private double staticPower;
	private double sleepPower;
	private String nodeName;

	/**
	 * Instantiates a new linear power model.
	 *
	 * @param maxPower    the max power
	 * @param staticPower the static power
	 */
	public FogLinearWithSnapPowerModel(String nodeName, double maxPower, double staticPower, double sleepPower) {
		this.nodeName = nodeName;
		setMaxPower(maxPower);
		setStaticPower(staticPower);
		setSleepPower(sleepPower);
		setConstant((maxPower - getStaticPower()) / 100);
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
		return getStaticPower() + getConstant() * utilization * 100;
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
	 * Gets the constant.
	 *
	 * @return the constant
	 */
	protected double getConstant() {
		return constant;
	}

	/**
	 * Sets the constant.
	 *
	 * @param constant the new constant
	 */
	protected void setConstant(double constant) {
		this.constant = constant;
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
