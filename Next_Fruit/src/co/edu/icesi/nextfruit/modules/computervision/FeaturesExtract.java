package co.edu.icesi.nextfruit.modules.computervision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import co.edu.icesi.nextfruit.modules.model.CameraCalibration;
import co.edu.icesi.nextfruit.modules.model.ColorDistribution;
import co.edu.icesi.nextfruit.modules.model.MatchingColor;
import co.edu.icesi.nextfruit.modules.model.PolygonWrapper;
import co.edu.icesi.nextfruit.util.Statistics;

public class FeaturesExtract {

	private Mat mat;
	private PolygonWrapper polygon;
	private Histogram histogram;
	private int numberOfPixels;
	private Collection<ColorDistribution> colorStatistics;
	private Collection<ColorDistribution> matchingColors;
	private Statistics luminanceStatistics;

	public FeaturesExtract(String imagePath) {
		mat = Imgcodecs.imread(imagePath);
	}

	// ***************** PUBLIC METHODS *****************

	public void extractFeatures(CameraCalibration calibration) {
		polygon = getContours(mat.clone(), 80);
		histogram = new Histogram(mat);
		histogram.applyWhitePatch();
		colorStatistics = histogram.getStatisticalColors(polygon);
		for (ColorDistribution color : colorStatistics)
			numberOfPixels += color.getRepeat();
	}

	public boolean hasExtractedFeatures() {
		return colorStatistics != null;
	}

	public void processLuminanceAnalysis() {
		luminanceStatistics = luminanceAnalysis();
	}

	public void processColorAnalysis(CameraCalibration calibration, List<MatchingColor> colors) {
		matchingColors = colorMatching(colors, calibration);
	}

	public void analizeData(CameraCalibration calibration, List<MatchingColor> colors) {
		processColorAnalysis(calibration, colors);
		processLuminanceAnalysis();
	}

	// ***************** PUBLIC METHODS *****************

	// **************** PRIVATE METHODS *****************

	private PolygonWrapper getContours(Mat src, int sensibility) {

		Imgproc.threshold(src, src, sensibility, 255, 1);
		Mat src_gray = new Mat();
		Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.blur( src_gray, src_gray, new Size(10, 10));

		// Histogram
		Histogram histogram2 = new Histogram(src_gray);
		histogram2.filterFigureByGrayProfile(0, 10, new double[]{0}, new double[]{255});
		src = histogram2.getImage();

		List<MatOfPoint> contours = findContours(src);
		ArrayList<PolygonWrapper> boxes = new ArrayList<>();
		PolygonWrapper biggest = null;
		double biggestArea = 0;
		for (MatOfPoint cnt : contours) {
			PolygonWrapper polygon = new PolygonWrapper(cnt.toArray(), false);
			boxes.add(polygon);
			if(polygon.getArea() > biggestArea) {
				biggest = polygon;
				biggestArea = polygon.getArea();
			}
		}
		return biggest;
	}

	private List<MatOfPoint> findContours(Mat mat) {
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours( mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);
		return contours;
	}

	private List<ColorDistribution> colorMatching(List<MatchingColor> colors, CameraCalibration calibration) {
		ArrayList<ColorDistribution> newColors = new ArrayList<>();
		for (MatchingColor color : colors) {
			color.restartRepeatCount();
			for (ColorDistribution c : colorStatistics) {
				c.transform2xyY(calibration);
				color.increaseIfClose(c.getxyY(), c.getRepeat());
			}
			newColors.add(color);
		}
		return newColors;
	}

	private Statistics luminanceAnalysis() {
		Statistics stat = new Statistics();
		histogram.generateEmptyLuminanceHistogram(256);
		for (ColorDistribution c : colorStatistics) {
			double luminance = Math.max(0, Math.min(c.getxyY()[2], 1));
			histogram.increaseLuminancePosition(luminance, true);
			stat.addValue(c.getxyY()[2]);
		}
		return stat;
	}

	// **************** PRIVATE METHODS *****************

	// ******************** GETTERS *********************

	public Mat getMat() {
		return mat;
	}

	public PolygonWrapper getPolygon() {
		return polygon;
	}

	public Collection<ColorDistribution> getColorStatistics() {
		return colorStatistics;
	}

	public Collection<ColorDistribution> getMatchingColors() {
		return matchingColors;
	}

	public int getNumberOfPixels() {
		return numberOfPixels;
	}

	public Histogram getHistogram() {
		return histogram;
	}

	public Statistics getLuminanceStatistics() {
		return luminanceStatistics;
	}

	// ******************** GETTERS *********************

}
