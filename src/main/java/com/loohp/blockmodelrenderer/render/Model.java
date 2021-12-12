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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.loohp.blockmodelrenderer.threading.GraphicsService;

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
		Map<BufferedImage, ZBuffer> data = new LinkedHashMap<>();
		List<Future<?>> tasks = new ArrayList<>();
		for (Face face : faces) {
			tasks.add(GraphicsService.execute(() -> {
				BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = img.createGraphics();
				g2.setTransform(transform);
				ZBuffer z = new ZBuffer(w, h, (int) g.getTransform().getTranslateX(), (int) g.getTransform().getTranslateY());
				face.render(g2, z);
				g2.dispose();
				z.setCenter(0, 0);
				data.put(img, z);
			}));
		}
		tasks.forEach(each -> {
			try {each.get();} catch (InterruptedException | ExecutionException e) {}
		});
		Graphics2D g2 = image.createGraphics();
		Iterator<Entry<BufferedImage, ZBuffer>> itr = data.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<BufferedImage, ZBuffer> entry = itr.next();
			g2.drawImage(entry.getKey(), 0, 0, null);
			if (entry.getValue().ignoreBuffer()) {
				itr.remove();
			}
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int finalX = x;
				int finalY = y;
				data.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getValue().get(finalX, finalY))).forEachOrdered(entry -> {
					int color = entry.getKey().getRGB(finalX, finalY);
					int alpha = (color >> 24) & 0xff;
					if (alpha == 255) {
						image.setRGB(finalX, finalY, color);
					} else if (alpha != 0) {
						g2.setColor(new Color(color, true));
						g2.drawPolygon(new int[] {finalX}, new int[] {finalY}, 1);
					}
				});
			}
		}
		g2.dispose();
	}

}
