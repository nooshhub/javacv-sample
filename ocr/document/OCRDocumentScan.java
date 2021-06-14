package ocr.document;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.opencv.opencv_core.*;
import util.ContourSortUtil;
import util.ImageUtil;
import util.ImageWindowUtil;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * 1. clarification
 * 2. possible solutions -> optimal (time & space)
 * 3. code
 * 4. test cases
 *
 * @author Neal
 * @date 2021/6/12
 */
public class OCRDocumentScan {

    public static void main(String[] args) {
        // 加载图片
        Mat image = imread("ocr/document/image-data/doc1.png");
        // resize完，坐标也会相同变化，所以需要这个比例来计算坐标
        // image.shape[0]/500.0
        // The shape of an image is accessed by img.shape.
        // It returns a tuple of the number of rows, columns, and channels (if the image is color):
        float ratio = image.rows() / 500.0f;
        Mat orig = image.clone();

        image = ImageUtil.resizeImgByHeight(orig, 500);
        ImageWindowUtil.imshow("resizeImgByHeight", image);

        //预处理
        Mat gray = new Mat();
        cvtColor(image, gray, COLOR_BGR2GRAY);
        ImageWindowUtil.imshow("gray", gray);
        GaussianBlur(gray, gray, new Size(5, 5), 0);
        ImageWindowUtil.imshow("GaussianBlur", gray);
        Mat edges = new Mat();
        Canny(gray, edges, 75, 200);
        System.out.println("STEP1 边缘检测");
        ImageWindowUtil.imshow("Canny", edges);

        // 轮廓检测
        MatVector cnts = new MatVector();
        findContours(edges.clone(), cnts, RETR_LIST, CHAIN_APPROX_SIMPLE);
        MatVector sortedCnts = ContourSortUtil.sortContours(cnts, ContourSortUtil.CS_AREA, true);


        // 遍历轮廓
        Mat screenCnt = null;
        for (int i = 0; i < sortedCnts.size(); i++) {
            if (i > 5) {
                break;
            }

            // 计算轮廓近似，周长
            Mat c = sortedCnts.get(i);
            double peri = arcLength(c, true);
            // c表示输入的点集
            // epsilon表示从原始轮廓到近似轮廓的最大距离，它是一个准确度参数
            // true表示封闭的
            Mat approx = new Mat();
            // 这个可以将一个不连续的像虚线一样的轮廓近似得出一个略大的多边形或者矩形
            approxPolyDP(c, approx, 0.02 * peri, true);

            // 4个点的时候就拿出来
            if (approx.total() == 4) {
                screenCnt = approx;
                break;
            }

        }
        System.out.println("STEP2 获取轮廓");
        drawContours(image, new MatVector(screenCnt), -1, Scalar.GREEN);
        ImageWindowUtil.imshow("Outlined", image);

        // TODO: 透视变化
        Mat warped = fourPointTransform(orig, screenCnt.reshape(4, 2), ratio);

        // 灰度 + 二值 = 保留图像中想要的东西
        cvtColor(warped, warped, COLOR_BGR2GRAY);
        Mat ref = new Mat();
        threshold(warped, ref, 100, 255, THRESH_BINARY);
        imwrite("ocr/document/image-data/scan1.png", ref);

        System.out.println("STEP3 变换");
        ImageWindowUtil.imshow("Original", ImageUtil.resizeImgByHeight(orig, 650));
        ImageWindowUtil.imshow("Scanned", ImageUtil.resizeImgByHeight(ref, 650));

        ImageWindowUtil.pressEscToExit();
    }

    public static Mat orderPoints(PointerPointer pts) {
//        // 一共四个坐标点

        Point[] rect = new Point[4];

//        Mat rect = Mat.zeros(new Size(4,2), CV_32FC1).asMat();
//        // 按顺序找到对应坐标0123分表是左上，右上，右下，左下
//        // 计算左上，右下
        return null;
    }

    /**
     * 将图像转换成
     *
     * @param orig
     * @param pts
     * @param ratio
     */
    public static Mat fourPointTransform(Mat orig, Mat pts, float ratio) {
//        Mat rect = orderPoints(pts);
        //getPerspectiveTransform(rect, dst);
        // warpPerspective(image, M, (maxWidth, maxHeight))
        return null;
    }


}
