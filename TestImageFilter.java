import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.*;

import javax.imageio.ImageIO;

public class TestImageFilter {

	public static void main(String[] args) throws Exception {
		
		BufferedImage image = null;
		String srcFileName = null;
		FileOutputStream fileStream = new FileOutputStream("output.txt");
		PrintStream originalOut = new PrintStream(fileStream);
		System.setOut(originalOut);

		int[] nthreads = {8};
		try {
			srcFileName = args[0];
			File srcFile = new File(srcFileName);
			image = ImageIO.read(srcFile);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			originalOut.println("Usage: java TestAll <image-file>");
			System.exit(1);
		}
		catch (IIOException e) {
			originalOut.println("Error reading image file " + srcFileName + " !");
			System.exit(1);
		}

		originalOut.println("Source image: " + srcFileName);

		int w = image.getWidth();
		int h = image.getHeight();
		originalOut.println("Image size is " + w + "x" + h);
		originalOut.println();
	
		int[] src = image.getRGB(0, 0, w, h, null, 0, w);
		int[] dst = new int[src.length];

//		originalOut.println("Starting sequential image filter.");
//
//		long startTime = System.currentTimeMillis();
//		ImageFilter filter0 = new ImageFilter(src, dst, w, h);
//		filter0.apply();
//		long endTime = System.currentTimeMillis();
//
//		long tSequential = endTime - startTime;
//		originalOut.println("Sequential image filter took " + tSequential + " milliseconds.");


//		Read image just for testing phase
		BufferedImage dstImage = ImageIO.read(new File("FilteredIMAGE1.JPG"));
//		BufferedImage dstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//		dstImage.setRGB(0, 0, w, h, dst, 0, w);
//
//		String dstName = "Filtered" + srcFileName;
//		File dstFile = new File(dstName);
//		ImageIO.write(dstImage, "jpg", dstFile);
		long tSequential = 88967;

		BufferedImage dstParallelImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int nthread : nthreads) {
			System.out.println("Starting parallel image filter using " + nthread + " threads.");
			long startTime_p = System.currentTimeMillis();

			int[] Paralleldst = new int[src.length];
			ParallelFJImageFilter filter_p = new ParallelFJImageFilter(src, Paralleldst, w, 1, h-1);
			ForkJoinPool pool = new ForkJoinPool(nthread);
			pool.invoke(filter_p);

			long endTime_p = System.currentTimeMillis();

			long tParallel = endTime_p - startTime_p;

			dstParallelImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			dstParallelImage.setRGB(0, 0, w, h, Paralleldst, 0, w);

			boolean verification_passed = Extension.CompareImages(dstImage, dstParallelImage);

			originalOut.println("Parallel image filter took " + tParallel + " milliseconds using " + nthread + " threads.");
			originalOut.println("Output image verified " + (verification_passed ? "successfully" : "with errors!"));
			originalOut.println("Speedup " + (double) (tSequential / tParallel) + ((double) (tSequential / tParallel) > 0.7 ? " ok (>= " + (tSequential / tParallel) + ")" : ""));
			originalOut.println();
		}

		String dstParallel_Name = "ParallelFiltered" + srcFileName;
		File dstParallel_File = new File(dstParallel_Name);
		ImageIO.write(dstParallelImage, "jpg", dstParallel_File);

		originalOut.println("Output image (parallel filter): " + dstParallel_Name);
	}
}
