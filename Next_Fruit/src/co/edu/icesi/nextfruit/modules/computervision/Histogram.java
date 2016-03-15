package co.edu.icesi.nextfruit.modules.computervision;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import co.edu.icesi.nextfruit.util.CumulativeStatistics;
import co.edu.icesi.nextfruit.util.Statistics;

public class Histogram {

	private Mat mat;
	private int[] histogram;
	private int[][] rgbHistogram;

	public Histogram(String imagePath) {
		mat = Imgcodecs.imread(imagePath);
	}

	public Histogram(Mat mat) {
		this.mat = mat;
	}

	public void convertToCromaticScale() {
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				double d[] = mat.get(i, j);
				double sum = (d[0] + d[1] + d[2]) / 255;
				if(sum > 0) {
					d[0] /= sum;
					d[1] /= sum;
					d[2] /= sum;
					mat.put(i, j, d);
				}
			}
		}
	}

	public void applyWhitePatch() {
		double Rmax = 0, Gmax = 0, Bmax = 0;
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				double d[] = mat.get(i, j);
				if(d[2] > Rmax)
					Rmax = d[2];
				if(d[1] > Gmax)
					Gmax = d[1];
				if(d[0] > Bmax)
					Bmax = d[0];
			}
		}
		Rmax = 255/Rmax;
		Gmax = 255/Gmax;
		Bmax = 255/Bmax;
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				double d[] = mat.get(i, j);
				d[0] *= Bmax;
				d[1] *= Gmax;
				d[2] *= Rmax;
				mat.put(i, j, d);
			}
		}
	}

	public double[][] obtainThreshold() {
		if(rgbHistogram == null)
			generateRGBHistogram(false);
		CumulativeStatistics r = new CumulativeStatistics();
		CumulativeStatistics g = new CumulativeStatistics();
		CumulativeStatistics b = new CumulativeStatistics();
		for (int i = 0; i < 256; i++) {
			if(rgbHistogram[0][i] > 0)
				r.addValue(i, rgbHistogram[0][i]);
			if(rgbHistogram[1][i] > 0)
				g.addValue(i, rgbHistogram[1][i]);
			if(rgbHistogram[2][i] > 0)
				b.addValue(i, rgbHistogram[2][i]);
		}
		return new double[][] {{r.getMean(), g.getMean(), b.getMean()},
			{r.getStandardDeviation()*6, g.getStandardDeviation()*6, b.getStandardDeviation()*6}};
	}

	public void generateAllHistograms(boolean useAutoFill) {
		generateHistogram(useAutoFill, true, true);
	}

	public void generateGrayscaleHistogram(boolean useAutoFill) {
		generateHistogram(useAutoFill, true, false);
	}

	public void generateRGBHistogram(boolean useAutoFill) {
		generateHistogram(useAutoFill, false, true);
	}

	private void generateHistogram(boolean useAutoFill, boolean gray, boolean rgb) {
		if(gray)
			histogram = new int[768];
		if(rgb)
			rgbHistogram = new int[3][256];
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				double d[] = mat.get(i, j);
				int pos = (int) (d[0]+d[1]+d[2]);
				if(useAutoFill) {
					if(gray)
						addToHistogram(pos, histogram);
					if(rgb) {
						addToHistogram((int)d[2], rgbHistogram[0]);
						addToHistogram((int)d[1], rgbHistogram[1]);
						addToHistogram((int)d[0], rgbHistogram[2]);
					}
				} else {
					if(gray)
						histogram[pos] ++;
					if(rgb) {
						rgbHistogram[0][(int)d[2]] ++;
						rgbHistogram[1][(int)d[1]] ++;
						rgbHistogram[2][(int)d[0]] ++;
					}
				}
			}
		}
	}

	private void addToHistogram(int pos, int[] histogram) {
		histogram[pos] += 3;
		if(pos > 1) {
			histogram[pos-2] ++;
			if(pos > 0)
				histogram[pos-1] += 2;
		}
		if(pos < histogram.length-1) {
			histogram[pos+1] += 2;
			if(pos < histogram.length-2)
				histogram[pos+2] ++;
		}
	}

	public void removeGrayRegion(int start, int end, double[] fill) {
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				double d[] = mat.get(i, j);
				int pos = (int) (d[0]+d[1]+d[2]);
				if(pos >= start && pos <= end) {
					mat.put(i, j, fill);
				}
			}
		}
	}

	public void filterFigureByColorProfile(double[] rgb, double[] range, double[] matchColor, double[] notMatchColor) {
		if(matchColor != null || notMatchColor != null) {
			range[0] /= 2;
			range[1] /= 2;
			range[2] /= 2;
			int minR = (int) (rgb[0] - range[0]), maxR = (int) (rgb[0] + range[0]);
			int minG = (int) (rgb[1] - range[1]), maxG = (int) (rgb[1] + range[1]);
			int minB = (int) (rgb[2] - range[2]), maxB = (int) (rgb[2] + range[2]);
			for (int i = 0; i < mat.height(); i++) {
				for (int j = 0; j < mat.width(); j++) {
					double[] d = mat.get(i, j);
					boolean match = d[0] > minB && d[0] < maxB
							&& d[1] > minG && d[1] < maxG
							&& d[2] > minR && d[2] < maxR;
							if(match) {
								if(matchColor != null) {
									mat.put(i, j, matchColor);
								}
							} else {
								if(notMatchColor != null) {
									mat.put(i, j, notMatchColor);
								}
							}
				}
			}
		}
	}

	public void smoothHistogram() {
		int smooth[] = new int[histogram.length];
		smooth[0] = histogram[0];
		smooth[histogram.length-1] = histogram[histogram.length-1];
		for (int i = 1; i < histogram.length-1; i++) {
			smooth[i] = (histogram[i-1]+histogram[i+1])/2;
		}
		histogram = smooth;
	}

	public void fillHistogram() {
		int smooth[] = new int[histogram.length];
		smooth[0] = histogram[0];
		smooth[histogram.length-1] = histogram[histogram.length-1];
		for (int i = 1; i < histogram.length-1; i++) {
			smooth[i] = Math.max(histogram[i-1], histogram[i+1]);
		}
		histogram = smooth;
	}

	public void smoothFillHistogram() {
		int smooth[] = new int[histogram.length];
		smooth[0] = histogram[0];
		smooth[histogram.length-1] = histogram[histogram.length-1];
		for (int i = 1; i < histogram.length-1; i++) {
			smooth[i] = (histogram[i-1]+histogram[i+1]+Math.max(histogram[i-1], histogram[i+1])*2)/4;
		}
		histogram = smooth;
	}

	public void statisticalSmothHistogram() {
		int smooth[] = new int[histogram.length];
		smooth[0] = histogram[0];
		smooth[1] = histogram[1];
		smooth[histogram.length-2] = histogram[histogram.length-2];
		smooth[histogram.length-1] = histogram[histogram.length-1];
		for (int i = 2; i < histogram.length-2; i++) {
			Statistics stat = new Statistics();
			stat.addValue(histogram[i-2]);
			stat.addValue(histogram[i-1]);
			stat.addValue(histogram[i]);
			stat.addValue(histogram[i+1]);
			stat.addValue(histogram[i+2]);
			double mean = stat.getMean();
			if(Math.abs(mean - histogram[i]) < stat.getStandardDeviation()/2)
				smooth[i] = histogram[i];
			else
				smooth[i] = (int) mean;
		}
		histogram = smooth;
	}

	public Mat getImage() {
		return mat;
	}

	public int getMaxHeight() {
		int max = 0;
		for (int i = 0; i < histogram.length; i++) {
			if(histogram[i] > max)
				max = histogram[i];
		}
		return max;
	}

	public int[] getHistogram() {
		return histogram;
	}

	public int[] getRedHistogram() {
		return rgbHistogram[0];
	}

	public int[] getGreenHistogram() {
		return rgbHistogram[1];
	}

	public int[] getBlueHistogram() {
		return rgbHistogram[2];
	}

	public void save(String filename) {
		Imgcodecs.imwrite(filename, mat);
	}

}
