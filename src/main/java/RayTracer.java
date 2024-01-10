package main.java;

import main.java.light.Colors;
import main.java.light.DirectionalLight;
import main.java.light.Light;
import main.java.math.Point;
import main.java.math.Vector;
import main.java.object.SceneObject;
import main.java.scene.Camera;
import main.java.scene.Scene;
import main.java.scene.SceneParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The type Ray tracer.
 */
public class RayTracer {
    /***
     *
     * To compil the program :  javac -cp src -d out src/main/java/RayTracer.java
     * To start the program : java -cp out main.java.RayTracer <scene_file>.txt
     * @param args the input arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.Logger.Level.ERROR.getName();
            System.exit(1);
        }

        String sceneFileName = args[0];
        Scene scene = SceneParser.parseScene(sceneFileName);

        int imgWidth = scene.getImageWidth();
        int imgHeight = scene.getImageHeight();

        Camera camera = scene.getCamera();
        Point lookFrom = camera.getLookFrom();
        Point lookAt = camera.getLookAt();

        Vector wNotNormalize = lookAt.subtract(lookFrom);
        Vector w = wNotNormalize.normalize();
        Vector uNotNormalize = w.dotVectorial(camera.getUp());
        Vector u = uNotNormalize.normalize();
        Vector v = w.dotVectorial(u).normalize();

        double fovr = Math.toRadians(camera.getFov());
        double realHeight = 2 * Math.tan(fovr / 2);
        double pixelHeight = realHeight / imgHeight;
        double realWidth = pixelHeight * imgWidth;
        double pixelWidth = realWidth / imgWidth;

        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

        for (int j = 0; j < imgHeight; j++) {
            for (int i = 0; i < imgWidth; i++) {

                double a = (-realWidth / 2.0) + (i + 0.5) * pixelWidth;
                double b = (realHeight / 2.0) - (j + 0.5) * pixelHeight;
                Vector d = u.multiplicationScalar(a).addition(v.multiplicationScalar(b)).addition(w).normalize();

                Colors pixelColor = new Colors(0, 0, 0);
                double closestIntersection = Double.POSITIVE_INFINITY;

                for (Light light : scene.getLights()) {
                    if (light instanceof DirectionalLight directionalLight) {
                        for (SceneObject object : scene.getObjects()) {
                            double t = object.intersect(lookFrom, d);
                        if (t >= 0 && t < closestIntersection) {
                            closestIntersection = t;
                            pixelColor = calculateColor(object, object.createVectorN(lookFrom, d),directionalLight );}
                        }
                    }
                }
                image.setRGB(i, imgHeight - j - 1, pixelColor.toRGB());}
        }
        saveImage(image, scene.getOutputFileName());
    }

    /**
     * Calculate color colors.
     *
     * @param object  the object
     * @param normale the normale
     * @param light   the light
     * @return the colors
     */
    public static Colors calculateColor(SceneObject object, Vector normale, DirectionalLight light) {
        double cosTheta = Math.max(0, normale.dotScalar(light.getDirection().normalize()));
        if (cosTheta <= 0) {
            return new Colors(0, 0, 0);}

        Colors cdif = object.getColor();
        Colors lightColor = light.getColor();

        double red = cosTheta * lightColor.getR() * cdif.getR();
        double green = cosTheta * lightColor.getG() * cdif.getG();
        double blue = cosTheta * lightColor.getB() * cdif.getB();

        red = Math.min(1.0, Math.max(0.0, red));
        green = Math.min(1.0, Math.max(0.0, green));
        blue = Math.min(1.0, Math.max(0.0, blue));


        return new Colors(red, green, blue);
    }


    /**
     * Save image.
     *
     * @param image          the image
     * @param outputFileName the output file name
     */
    public static void saveImage(BufferedImage image, String outputFileName) {
        try {
            File outputDir = new File("images");
            if(!outputDir.exists()){
                outputDir.mkdirs();
            }
            File outputfile = new File(outputDir, outputFileName);
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
