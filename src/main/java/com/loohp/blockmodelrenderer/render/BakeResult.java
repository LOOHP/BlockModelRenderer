package com.loohp.blockmodelrenderer.render;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;

import com.loohp.blockmodelrenderer.utils.DoubleBiFunction;
import com.loohp.blockmodelrenderer.utils.DoubleBiPredicate;
import com.loohp.blockmodelrenderer.utils.MathUtils;

public class BakeResult {
	
	private BufferedImage texture;
	private AffineTransform transform;
	private AffineTransform inverseTransform;
	private DoubleBiFunction depthFunction;
	private DoubleBiPredicate inBoundPredicate;
	private boolean ignoreZFight;
	
	public BakeResult(BufferedImage texture, AffineTransform transform, DoubleBiFunction depthFunction, DoubleBiPredicate inBoundPredicate, boolean ignoreZFight) {
		this.texture = texture;
		this.transform = transform;
		try {
			this.inverseTransform = MathUtils.equals(transform.getDeterminant(), 0.0) ? null : transform.createInverse();
		} catch (NoninvertibleTransformException e) {
			this.inverseTransform = null;
		}
		this.depthFunction = depthFunction;
		this.inBoundPredicate = inBoundPredicate;
		this.ignoreZFight = ignoreZFight;
	}

	public BufferedImage getTexture() {
		return texture;
	}

	public AffineTransform getTransform() {
		return transform;
	}
	
	public boolean hasInverseTransform() {
		return inverseTransform != null;
	}
	
	public AffineTransform getInverseTransform() {
		return inverseTransform;
	}

	public double getDepthAt(double x, double y) {
		return depthFunction.apply(x, y);
	}

	public boolean ignoreZFight() {
		return ignoreZFight;
	}
	
	public boolean isWithinBound(double x, double y) {
		return inBoundPredicate.test(x, y);
	}

}
