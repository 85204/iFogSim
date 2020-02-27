package org.fog.entities;

public class TransmitTuple {
	public Tuple tuple;
	public int srcId;
	TransmitTuple(Tuple tuple, int srcId){
		this.tuple = tuple;
		this.srcId = srcId;
	}
}
