package com.loohp.blockmodelrenderer.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	
	public void render(int w, int h, Graphics2D g, BufferedImage image) {
		AffineTransform transform = g.getTransform();
		Map<BufferedImage, ZBuffer> data = new HashMap<>();
		for (Face face : faces) {
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = img.createGraphics();
			g2.setTransform(transform);
			ZBuffer z = new ZBuffer(w, h, (int) g.getTransform().getTranslateX(), (int) g.getTransform().getTranslateY());
			face.render(g2, z);
			g2.dispose();
			z.setCenter(0, 0);
			data.put(img, z);
		}
		Graphics2D g2 = image.createGraphics();
		for (Entry<BufferedImage, ZBuffer> entry : data.entrySet()) {
			g2.drawImage(entry.getKey(), 0, 0, null);
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int finalX = x;
				int finalY = y;
				data.entrySet().stream().filter(each -> !each.getValue().ignoreBuffer()).sorted(Comparator.comparing(entry -> entry.getValue().get(finalX, finalY))).forEachOrdered(entry -> {
					g2.setColor(new Color(entry.getKey().getRGB(finalX, finalY), true));
					g2.drawLine(finalX, finalY, finalX, finalY);
				});
			}
		}
		g2.dispose();
	}

}
