package com.loohp.blockmodelrenderer.render;

public interface ITransformable {
	
	public void scale(double x, double y, double z);
	
	public void translate(double x, double y, double z);
	
	public void rotate(double x, double y, double z, boolean saveAxis);
	
	public void flipAboutPlane(boolean x, boolean y, boolean z);
	
	public void updateLighting(Vector direction, double ambient, double max);

}
