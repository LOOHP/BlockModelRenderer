package com.loohp.blockmodelrenderer.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Hexahedron {
	
	private Face upFace;
	private Face downFace;
	private Face northFace;
	private Face eastFace;
	private Face southFace;
	private Face westFace;
	
	private List<Face> ordered;
	
	public Hexahedron(Face upFace, Face downFace, Face northFace, Face eastFace, Face southFace, Face westFace) {
		this.upFace = upFace;
		this.downFace = downFace;
		this.northFace = northFace;
		this.eastFace = eastFace;
		this.southFace = southFace;
		this.westFace = westFace;
		
		this.upFace.oppositeFace = this.downFace;
		this.downFace.oppositeFace = this.upFace;
		this.northFace.oppositeFace = this.southFace;
		this.eastFace.oppositeFace = this.westFace;
		this.southFace.oppositeFace = this.northFace;
		this.westFace.oppositeFace = this.eastFace;
		
		this.upFace.priority = 1;
		this.downFace.priority = 0;
		this.northFace.priority = 0;
		this.eastFace.priority = 1;
		this.southFace.priority = 1;
		this.westFace.priority = 0;
		
		ordered = new ArrayList<>(Arrays.asList(upFace, downFace, northFace, eastFace, southFace, westFace));
		sortFaces();
	}
	
	public static Hexahedron fromCorners(Point3D p1, Point3D p2, boolean ignoreZFight, BufferedImage[] images) {
		if (images.length != 6) {
			throw new IllegalArgumentException("images length must be 6");
		}
		return new Hexahedron(new Face(images[0], ignoreZFight, new Point3D(p1.x, p2.y, p1.z), new Point3D(p2.x, p2.y, p1.z), new Point3D(p2.x, p2.y, p2.z), new Point3D(p1.x, p2.y, p2.z)), 
							  new Face(images[1], ignoreZFight, new Point3D(p1.x, p1.y, p1.z), new Point3D(p2.x, p1.y, p1.z), new Point3D(p2.x, p1.y, p2.z), new Point3D(p1.x, p1.y, p2.z)),
							  new Face(images[2], ignoreZFight, new Point3D(p2.x, p2.y, p1.z), new Point3D(p1.x, p2.y, p1.z), new Point3D(p1.x, p1.y, p1.z), new Point3D(p2.x, p1.y, p1.z)),
							  new Face(images[3], ignoreZFight, new Point3D(p2.x, p2.y, p2.z), new Point3D(p2.x, p2.y, p1.z), new Point3D(p2.x, p1.y, p1.z), new Point3D(p2.x, p1.y, p2.z)),
							  new Face(images[4], ignoreZFight, new Point3D(p1.x, p2.y, p2.z), new Point3D(p2.x, p2.y, p2.z), new Point3D(p2.x, p1.y, p2.z), new Point3D(p1.x, p1.y, p2.z)),
							  new Face(images[5], ignoreZFight, new Point3D(p1.x, p2.y, p1.z), new Point3D(p1.x, p2.y, p2.z), new Point3D(p1.x, p1.y, p2.z), new Point3D(p1.x, p1.y, p1.z)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((downFace == null) ? 0 : downFace.hashCode());
		result = prime * result + ((eastFace == null) ? 0 : eastFace.hashCode());
		result = prime * result + ((northFace == null) ? 0 : northFace.hashCode());
		result = prime * result + ((ordered == null) ? 0 : ordered.hashCode());
		result = prime * result + ((southFace == null) ? 0 : southFace.hashCode());
		result = prime * result + ((upFace == null) ? 0 : upFace.hashCode());
		result = prime * result + ((westFace == null) ? 0 : westFace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Hexahedron)) {
			return false;
		}
		Hexahedron other = (Hexahedron) obj;
		if (downFace == null) {
			if (other.downFace != null) {
				return false;
			}
		} else if (!downFace.equals(other.downFace)) {
			return false;
		}
		if (eastFace == null) {
			if (other.eastFace != null) {
				return false;
			}
		} else if (!eastFace.equals(other.eastFace)) {
			return false;
		}
		if (northFace == null) {
			if (other.northFace != null) {
				return false;
			}
		} else if (!northFace.equals(other.northFace)) {
			return false;
		}
		if (ordered == null) {
			if (other.ordered != null) {
				return false;
			}
		} else if (!ordered.equals(other.ordered)) {
			return false;
		}
		if (southFace == null) {
			if (other.southFace != null) {
				return false;
			}
		} else if (!southFace.equals(other.southFace)) {
			return false;
		}
		if (upFace == null) {
			if (other.upFace != null) {
				return false;
			}
		} else if (!upFace.equals(other.upFace)) {
			return false;
		}
		if (westFace == null) {
			if (other.westFace != null) {
				return false;
			}
		} else if (!westFace.equals(other.westFace)) {
			return false;
		}
		return true;
	}
	
	public void setImage(BufferedImage[] images) {
		if (images.length != 6) {
			throw new IllegalArgumentException("images length must be 6");
		}
		upFace.image = images[0];
		downFace.image = images[1];
		northFace.image = images[2];
		eastFace.image = images[3];
		southFace.image = images[4];
		westFace.image = images[5];
	}

	public List<Face> getFaces() {
		return ordered;
	}

	public Face getUpFace() {
		return upFace;
	}

	public Face getDownFace() {
		return downFace;
	}

	public Face getNorthFace() {
		return northFace;
	}

	public Face getEastFace() {
		return eastFace;
	}

	public Face getSouthFace() {
		return southFace;
	}

	public Face getWestFace() {
		return westFace;
	}

	public void render(Graphics2D g) {
		for (Face face : getFaces()) {
			face.render(g);
		}
		sortFaces();
	}
	
	public void rotate(double x, double y, double z) {
		for (Face face : ordered) {
			face.rotate(x, y, z);
		}
		sortFaces();
	}
	
	public void translate(double x, double y, double z) {
		for (Face face : ordered) {
			face.translate(x, y, z);
		}
	}
	
	public void scale(double x, double y, double z) {
		for (Face face : ordered) {
			face.scale(x, y, z);
		}
	}
	
	public void updateLightingRatio(double up, double down, double north, double east, double south, double west) {
		this.upFace.setLightRatio(up);
		this.downFace.setLightRatio(down);
		this.northFace.setLightRatio(north);
		this.eastFace.setLightRatio(east);
		this.southFace.setLightRatio(south);
		this.westFace.setLightRatio(west);
	}
	
	public void sortFaces() {
		Collections.sort(ordered, Face.DEPTH_COMPARATOR);
	}

}
