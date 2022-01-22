package com.loohp.blockmodelrenderer.render;

public interface ITransformable {

    void scale(double x, double y, double z);

    void translate(double x, double y, double z);

    void rotate(double x, double y, double z, boolean saveAxis);

    void flipAboutPlane(boolean x, boolean y, boolean z);

    void updateLighting(Vector direction, double ambient, double max);

}
