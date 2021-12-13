package com.loohp.blockmodelrenderer.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

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
	
	public void render(int w, int h, Graphics2D g, BufferedImage image) {
		AffineTransform transform = g.getTransform();
		Map<ZBuffer, BufferedImage> data = new LinkedHashMap<>();
		for (Face face : faces) {
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = img.createGraphics();
			g2.setTransform(transform);
			ZBuffer z = new ZBuffer(w, h, (int) g.getTransform().getTranslateX(), (int) g.getTransform().getTranslateY());
			face.render(g2, z);
			g2.dispose();
			z.setCenter(0, 0);
			data.put(z, img);
		}
		Graphics2D g2 = image.createGraphics();
		Iterator<Entry<ZBuffer, BufferedImage>> itr = data.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<ZBuffer, BufferedImage> entry = itr.next();
			g2.drawImage(entry.getValue(), 0, 0, null);
			if (entry.getKey().ignoreBuffer()) {
				itr.remove();
			}
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int finalX = x;
				int finalY = y;
				SortedMap<ZBuffer, BufferedImage> map = new TreeMap<>(Comparator.comparing(zBuffer -> zBuffer.get(finalX, finalY)));
				map.putAll(data);
				for (BufferedImage each : map.values()) {
					int color = each.getRGB(x, y);
					int alpha = (color >> 24) & 0xff;
					if (alpha == 255) {
						image.setRGB(x, y, color);
					} else if (alpha != 0) {
						g2.setColor(new Color(color, true));
						g2.drawLine(x, y, x, y);
					}
				}
			}
		}
		g2.dispose();
	}

}
