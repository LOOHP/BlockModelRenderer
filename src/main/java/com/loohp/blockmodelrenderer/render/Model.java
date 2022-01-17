package com.loohp.blockmodelrenderer.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Model implements ITransformable {
	
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
	
	public void append(Model model) {
		for (Hexahedron hexahedron : model.components) {
			this.components.add(hexahedron);
			this.faces.addAll(hexahedron.getFaces());
		}
		sortFaces();
	}
	
	public List<Hexahedron> getComponents() {
		return components;
	}
	
	public void rotate(double x, double y, double z, boolean saveAxis) {
		for (Hexahedron hexahedron : components) {
			hexahedron.rotate(x, y, z, saveAxis);
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
		Collections.sort(faces, Face.AVERAGE_DEPTH_COMPARATOR);
	}
	
	public void render(Graphics2D g, BufferedImage image, boolean useZBuffer) {
		if (useZBuffer) {
			ZBuffer z = new ZBuffer(image.getWidth(), image.getHeight(), (int) g.getTransform().getTranslateX(), (int) g.getTransform().getTranslateY());
			for (Face face : faces) {
				face.render(g, image, z);
			}
		} else {
			for (Face face : faces) {
				face.render(g);
			}
		}
	}

}
