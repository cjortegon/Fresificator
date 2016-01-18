package test.silhouettedetector;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class SilhouetteDetector {

	private Mat image;

	public SilhouetteDetector(String imagePath, int sensibility, int repaintTone) {
		image = Imgcodecs.imread(imagePath);
		image = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_GRAYSCALE);
		Imgproc.threshold(image, image, sensibility, repaintTone, 1);
	}

	public Mat getOriginalImage() {
		return image;
	}

}
