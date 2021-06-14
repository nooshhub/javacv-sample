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
        * IMG: ��������Ϊfloat32����ͼ��
        * blockSize���ǵ�����ָ������Ĵ�С
        * ksize��Sobel����ʹ�ô��ڵĴ�С
        * k��ȡֵ����Ϊ[0.04, 0.06]
         */
        cornerHarris(src, src, 2, 3, 0.04);
        ImageWindowUtil.imshow("cornerHarris", src);

        ImageWindowUtil.pressEscToExit();

    }

}
