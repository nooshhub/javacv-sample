package util;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import static org.bytedeco.opencv.global.opencv_imgproc.INTER_AREA;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * Image Util
 *
 * @author Neal
 * @date 2021/6/12
 */
public class ImageUtil {

    /**
     * resize image by width
     * @param image image
     * @param width width of image
     * @return resized image
     */
    public static Mat resizeImgByWidth(Mat image, int width) {
        return resizeImg(image, width, -1, INTER_AREA);
    }

    /**
     * resize image by height
     * @param image image
     * @param height height of image
     * @return resized image
     */
    public static Mat resizeImgByHeight(Mat image, int height) {
        return resizeImg(image, -1, height, INTER_AREA);
    }

    /**
     * resize image
     * @param image image
     * @param width width of image, -1 means use height to calculate width
     * @param height height of image, -1 means use width to calculate height
     * @param interpolation interpolation
     * @return resized image
     */
    public static Mat resizeImg(Mat image, int width, int height, int interpolation) {
        if (width == -1 && height == -1) {
            return image;
        }

        int w = image.cols(), h = image.rows();
        Size size = new Size();
        if (width == -1) {
            float ratio = height * 1F / h;
            size.width(Math.round(w * ratio));
            size.height(height);
        } else if (height == -1) {
            float ratio = width * 1F / w;
            size.width(width);
            size.height(Math.round(h * ratio));
        }

        Mat dst = new Mat();
        if (interpolation == -1) {
            interpolation = INTER_AREA;
        }
        resize(image, dst, size, 0, 0, interpolation);
        return dst;
    }
}
