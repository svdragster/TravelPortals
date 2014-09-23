package de.svdragster.travelportals;

public class Link {

	private int x, y, z; // Store the location 
	private String world;
	private String name; // The name of this portal
	private String destination; // The name of the destination portal
	private String owner; // Playername of the owner
	
	public Link(int _x, int _y, int _z, String _world) {
		x = _x; y = _y; z = _z;
		world = _world;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public String getName() {
		return name;
	}
	
	public String getWorld() {
		return world;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public boolean hasName() {
		if (name != null) {
			return true;
		}
		return false;
	}
	
	public boolean hasDestination() {
		if (destination != null) {
			return true;
		}
		return false;
	}
	
	public boolean hasOwner() {
		if (owner != null) {
			return true;
		}
		return false;
	}
	
	public void setName(String _name) {
		name = _name;
	}
	
	public void setDestination(String _destination) {
		destination = _destination;
	}
	
	public void setOwner(String _owner) {
		owner = _owner;
	}
}
