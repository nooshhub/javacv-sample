package tmp;

import org.bytedeco.opencv.opencv_core.*;
import util.ImageWindowUtil;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * @author neals
 * @date 6/9/2021
 * <p>
 * mvn compile exec:java -Dexec.mainClass=tmp.Test
 */
public class Test {

    public static void main(String[] args) {
        Mat src = imread("tmp/demo3.png");
        cvtColor(src, src, COLOR_BGR2GRAY);
        ImageWindowUtil.imshow("COLOR_BGR2GRAY", src);

        /*
        * IMG: 数据类型为float32的入图像
        * blockSize：角点检测中指定区域的大小
        * ksize：Sobel求导中使用窗口的大小
        * k：取值参数为[0.04, 0.06]
         */
        cornerHarris(src, src, 2, 3, 0.04);
        ImageWindowUtil.imshow("cornerHarris", src);

        ImageWindowUtil.pressEscToExit();

    }

}
