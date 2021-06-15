package map;

import org.bytedeco.opencv.opencv_core.*;
import util.ImageWindowUtil;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * @author Neal
 * @date 2021/6/13
 */
public class MapColorSampleAnalysis {

    public static void main(String[] args) {
        Mat src = imread("map/image-data/map1.png");
        ImageWindowUtil.imshow("src", src);

        process(src);
    }

    public static List<ContourData> process(Mat src) {

        // 手动选择区域 ROI
        // 这里按照原题比例找了下点，可以按照实际情况找出多边形的点
        Point hatPoints = new Point(4);
        int srcW = src.cols(), srcH = src.rows();
        hatPoints.position(0).x((int) Math.round(srcW * 0.85)).y((int) Math.round(srcH * 0.36));
        hatPoints.position(1).x((int) Math.round(srcW * 0.89)).y((int) Math.round(srcH * 0.36));
        hatPoints.position(2).x((int) Math.round(srcW * 0.89)).y((int) Math.round(srcH * 0.9));
        hatPoints.position(3).x((int) Math.round(srcW * 0.85)).y((int) Math.round(srcH * 0.9));

        // 制作一个黑白mask
        Mat mask = Mat.zeros(src.size(), CV_8UC1).asMat();
        fillConvexPoly(mask, hatPoints.position(0), 4, Scalar.WHITE, CV_AA, 0);
        ImageWindowUtil.imshow("mask", mask);

        // 删除掉不需要的地方,只保留ROI region of interest
        Mat roi = new Mat();
        bitwise_and(src, src, roi, mask);
        ImageWindowUtil.imshow("roi", roi);

        // 边缘检测 - 因为图片已经颜色分明，可以开始检测边缘了
        Mat edges = new Mat();
        Canny(roi, edges, 80, 180);
        ImageWindowUtil.imshow("Canny", edges);

        // 计算轮廓 - 计算出边缘就可以计算轮廓了
        MatVector contours = new MatVector();
        findContours(edges.clone(), contours, RETR_LIST, CHAIN_APPROX_SIMPLE);

        // 画出轮廓 - 测试用
        Mat drawContoursImg = roi.clone();
        drawContours(drawContoursImg, contours, -1, Scalar.RED);
        ImageWindowUtil.imshow("drawContoursImg", drawContoursImg);

        List<ContourData> contourDataList = new ArrayList<>();
        Mat drawRectImg = roi.clone();
        Scalar prevBFR = null;
        for (int i = 0; i < contours.size(); i++) {
            // 计算外接矩形
            Mat contour = contours.get(i);
            Rect r = boundingRect(contour);
            rectangle(drawRectImg, r, Scalar.RED);

            double area = contourArea(contour);
            if (area > 50 && area < 150) {

                // 利用原图来制作一个蒙版
                Mat mask4contour = Mat.zeros(roi.size(), CV_8UC1).asMat();
                fillConvexPoly(mask4contour, contour, Scalar.WHITE);
                Scalar cntBGR = mean(roi, mask4contour);
                if(MapColorMatcher.compareBGR(cntBGR, prevBFR)) {
                    continue;
                }
                prevBFR = cntBGR;
                contourDataList.add(new ContourData(contour, cntBGR));
            }
        }

        ImageWindowUtil.imshow("drawRectImg", drawRectImg);

        ImageWindowUtil.pressEscToExit();

        return contourDataList;
    }

}
