package com.example.jsonconsumer.model;

public class Company {
	private Double coordinateNorth;
	private Double coordinateEast;
	private String displayName;
	
	public Company(Double coordinateNorth, Double coordinateEast, String displayName) {
		super();
		this.coordinateNorth = coordinateNorth;
		this.coordinateEast = coordinateEast;
		this.displayName = displayName;
	}
	
	public Double getCoordinateNorth() {
		return coordinateNorth;
	}
	
	public void setCoordinateNorth(Double coordinateNorth) {
		this.coordinateNorth = coordinateNorth;
	}
	public Double getCoordinateEast() {
		return coordinateEast;
	}
	public void setCoordinateEast(Double coordinateEast) {
		this.coordinateEast = coordinateEast;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	
}
