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
		int nthreads = 0;
		try {
			srcFileName = args[0];
			nthreads = Runtime.getRuntime().availableProcessors();
			File srcFile = new File(srcFileName);
			image = ImageIO.read(srcFile);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java TestAll <image-file>");
			System.exit(1);
		}
		catch (IIOException e) {
			System.out.println("Error reading image file " + srcFileName + " !");
			System.exit(1);
		}
		catch (Exception e)
		{
			nthreads = 0;
		}

		System.out.println("Source image: " + srcFileName);

		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("Image size is " + w + "x" + h);
		System.out.println();
	
		int[] src = image.getRGB(0, 0, w, h, null, 0, w);
		int[] dst = new int[src.length];

//		System.out.println("Starting sequential image filter.");
//
//		long startTime = System.currentTimeMillis();
//		ImageFilter filter0 = new ImageFilter(src, dst, w, h);
//		filter0.apply();
//		long endTime = System.currentTimeMillis();
//
//		long tSequential = endTime - startTime;
//		System.out.println("Sequential image filter took " + tSequential + " milliseconds.");


//		Read image just for testing phase
		BufferedImage dstImage = ImageIO.read(new File("FilteredIMAGE1.JPG"));

//		String dstName = "Filtered" + srcFileName;
//		File dstFile = new File(dstName);
//		ImageIO.write(dstImage, "jpg", dstFile);
		long tSequential = 80000;
		if(nthreads > 0)
		{

			BufferedImage dstParallelImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			for (int i = nthreads; i > 3; i = i / 2)
			{
				System.out.println("Starting parallel image filter using "+i+" threads.");
				long startTime_p = System.currentTimeMillis();
				int[] Paralleldst = new int[src.length];
				ParallelFJImageFilter filter_p = new ParallelFJImageFilter(src, Paralleldst, w, h, 1, h-1);
				ForkJoinPool pool = new ForkJoinPool(i);
				pool.invoke(filter_p);
				long endTime_p = System.currentTimeMillis();

				long tParallel = endTime_p - startTime_p;

				dstParallelImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				dstParallelImage.setRGB(0, 0, w, h, Paralleldst, 0, w);

				boolean verification_passed = Extension.CompareImages(dstImage, dstParallelImage);

				System.out.println("Parallel image filter took " + tParallel + " milliseconds using " +i+" threads.");
				System.out.println("Output image verified " + (verification_passed ? "successfully" : "with errors!"));
				System.out.println("Speedup "+(double)(tSequential/tParallel)+((double)(tParallel / tSequential) > 0.7 ? " ok (>= "+(tSequential / tParallel)+")" : ""));
				System.out.println();
			}

			String dstParallel_Name = "ParallelFiltered" + srcFileName;
			File dstParallel_File = new File(dstParallel_Name);
			ImageIO.write(dstParallelImage, "jpg", dstParallel_File);

			System.out.println("Output image (parallel filter): " + dstParallel_Name);
		}
		else {
			System.out.println("Can't execute parallel code because number of threads hasn't been specified!");
		}

		PrintStream fileStream = new PrintStream("output.txt");
		System.setOut(fileStream);
	}
}
