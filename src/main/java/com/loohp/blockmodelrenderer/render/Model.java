package com.loohp.blockmodelrenderer.render;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Model {
	
	private List<Hexahedron> components;
	private List<Face> faces;

	public Model(List<Hexahedron> components) {
		this.components = components;
		this.faces = new ArrayList<>();
		for (Hexahedron hexahedron : components) {
			this.faces.addAll(hexahedron.getFaces());
		}
		sortFaces();
	}
	
	public Model(Hexahedron... components) {
		this(new ArrayList<>(Arrays.asList(components)));
	}

	public List<Hexahedron> getComponents() {
		return components;
	}
	
	public void rotate(double x, double y, double z) {
		for (Hexahedron hexahedron : components) {
			hexahedron.rotate(x, y, z);
		}
		sortFaces();
	}
	
	public void translate(double x, double y, double z) {
		for (Hexahedron hexahedron : components) {
			hexahedron.translate(x, y, z);
		}
	}
	
	public void scale(double x, double y, double z) {
		for (Hexahedron hexahedron : components) {
			hexahedron.scale(x, y, z);
		}
	}
	
	public void updateLightingRatio(double up, double down, double north, double east, double south, double west) {
		for (Hexahedron hexahedron : components) {
			hexahedron.updateLightingRatio(up, down, north, east, south, west);
		}
	}
	
	public void sortFaces() {
		Collections.sort(faces, Face.DEPTH_COMPARATOR);
	}
	
	public void render(Graphics2D g) {
		for (Face face : faces) {
			face.render(g);
		}
	}

}
