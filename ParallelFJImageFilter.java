import java.util.concurrent.*;

public class ParallelFJImageFilter extends RecursiveAction{
    private int[] src;
    private int[] dst;
    private int width;
    private int start;
    private int end;

    private final int NRSTEPS = 100;
    private final int threshold = 500;

    public ParallelFJImageFilter(int[] src, int[] dst, int w, int start, int end) {
        this.src = src;
        this.dst = dst;
        width = w;
        this.start = start;
        this.end = end;
    }

    public void apply() {
        int index, pixel;
        for (int steps = 0; steps < NRSTEPS; steps++) {
            for (int i = start; i < end; i++) {
                for (int j = 1; j < width - 1; j++) {
                    float rt = 0, gt = 0, bt = 0;
                    for (int k = i - 1; k <= i + 1; k++) {
                        index = k * width + j - 1;
                        pixel = src[index];
                        rt += (float) ((pixel & 0x00ff0000) >> 16);
                        gt += (float) ((pixel & 0x0000ff00) >> 8);
                        bt += (float) ((pixel & 0x000000ff));

                        index = k * width + j;
                        pixel = src[index];
                        rt += (float) ((pixel & 0x00ff0000) >> 16);
                        gt += (float) ((pixel & 0x0000ff00) >> 8);
                        bt += (float) ((pixel & 0x000000ff));

                        index = k * width + j + 1;
                        pixel = src[index];
                        rt += (float) ((pixel & 0x00ff0000) >> 16);
                        gt += (float) ((pixel & 0x0000ff00) >> 8);
                        bt += (float) ((pixel & 0x000000ff));
                    }
                    // Re-assemble destination pixel.
                    index = i * width + j;
                    int dpixel = (0xff000000) | (((int) rt / 9) << 16) | (((int) gt / 9) << 8) | (((int) bt / 9));
                    dst[index] = dpixel;
                }
            }
            // swap references
            int[] help; help = src; src = dst; dst = help;
        }
    }

    @Override
    protected void compute() {
        if (end - start > threshold) {
            int distance = (end - start) / 2;

            ParallelFJImageFilter subTask1 = new ParallelFJImageFilter(src, dst, width, start, start + distance);
            ParallelFJImageFilter subTask2 = new ParallelFJImageFilter(src, dst, width, start + distance, end);
            invokeAll(subTask1, subTask2);
        } else {
            apply();
        }
    }
}
