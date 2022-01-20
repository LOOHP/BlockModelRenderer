package com.loohp.blockmodelrenderer.render;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.loohp.blockmodelrenderer.utils.ColorUtils;
import com.loohp.blockmodelrenderer.utils.MathUtils;
import com.loohp.blockmodelrenderer.utils.TaskCompletation;

public class Model implements ITransformable {
	
	public static final int PIXEL_PER_THREAD = 256;
	
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
	
	public void flipAboutPlane(boolean x, boolean y, boolean z) {
		for (Hexahedron hexahedron : components) {
			hexahedron.flipAboutPlane(x, y, z);
		}
	}
	
	@Override
	public void updateLighting(Vector direction, double ambient, double max) {
		for (Hexahedron hexahedron : components) {
			hexahedron.updateLighting(direction, ambient, max);
		}
	}
	
	public void sortFaces() {
		Collections.sort(faces, Face.AVERAGE_DEPTH_COMPARATOR);
	}
	
	public TaskCompletation render(BufferedImage source, boolean useZBuffer, AffineTransform baseTransform, ThreadPoolExecutor service) {
		List<BakeResult> bakes = new LinkedList<>();
		for (Face face : faces) {
			BakeResult result = face.bake(baseTransform);
			if (result != null && result.hasInverseTransform()) {
				bakes.add(result);
			}
		}
		int w = source.getWidth();
		int h = source.getHeight();
		int[] sourceColors = source.getRGB(0, 0, w, h, null, 0, w);
		int pixelCount = w * h;
		int threadCount = service.getMaximumPoolSize();
		List<Future<?>> futures = new ArrayList<>(threadCount);
		for (int i = 0; i < pixelCount; i += PIXEL_PER_THREAD) {
			int currentI = i;
			futures.add(service.submit(() -> {
				double[] pointSrc = new double[2];
				double[] pointDes = new double[2];
				for (int u = 0; u < PIXEL_PER_THREAD; u++) {
					int position = currentI + u;
					if (position >= pixelCount) {
						break;
					}
					int sourceColor = sourceColors[position];
					int x = position % w;
					int y = position / w;
					int newColor = sourceColor;
					double z = -Double.MAX_VALUE;
					pointSrc[0] = x;
					pointSrc[1] = y;
					for (BakeResult bake : bakes) {
						bake.getInverseTransform().transform(pointSrc, 0, pointDes, 0, 1);
						BufferedImage image = bake.getTexture();
						if (MathUtils.greaterThanOrEquals(pointDes[0], 0.0) && MathUtils.greaterThanOrEquals(pointDes[1], 0.0) && MathUtils.lessThan(pointDes[0], image.getWidth()) && MathUtils.lessThan(pointDes[1], image.getHeight())) {
							int imageColor = image.getRGB((int) pointDes[0], (int) pointDes[1]);
							if (useZBuffer && !bake.ignoreZFight()) {
								int imageAlpha = ColorUtils.getAlpha(imageColor);
								int sourceAlpha = ColorUtils.getAlpha(sourceColor);
								if (imageAlpha > 0) {
									double depth = bake.getDepthAt(x, y);
									if (MathUtils.greaterThanOrEquals(depth, z)) {
										if (depth > z) {
											z = depth;
										}
										if (imageAlpha >= 255) {
											newColor = imageColor;
										} else {
											newColor = ColorUtils.composite(imageColor, newColor);
										}
									}
								} else if (sourceAlpha < 255) {
									newColor = ColorUtils.composite(newColor, imageColor);
								}
							} else {
								newColor = ColorUtils.composite(imageColor, newColor);
							}
						}
					}
					if (newColor != sourceColor) {
						source.setRGB(x, y, newColor);
					}
				}
			}));
		}
		return new TaskCompletation(futures);
	}

}
