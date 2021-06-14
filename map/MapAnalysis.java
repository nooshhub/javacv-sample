package map;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.IntRawIndexer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;
import org.bytedeco.opencv.opencv_core.*;
import util.ImageUtil;
import util.ImageWindowUtil;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * 1. 图片里的区块 = 轮廓
 * 2. 计算各个区块的大小 = 面积，周长
 * 3. 按区域色彩分类
 *
 * @author Neal
 * @date 2021/6/13
 */
public class MapAnalysis {

    public static void main(String[] args) {
        Mat src = imread("map/image-data/map1.png");
        ImageWindowUtil.imshow("src", src);

        process(src);
    }

    public static List<ContourData> process(Mat src) {
        // 手动选择区域 ROI
        // 这里按照原题比例找了下点，可以按照实际情况找出多边形的点
        Point hatPoints = new Point(6);
        int srcW = src.cols(), srcH = src.rows();
        hatPoints.position(0).x((int) Math.round(srcW * 0.20)).y((int) Math.round(srcH * 0.10));
        hatPoints.position(1).x((int) Math.round(srcW * 0.70)).y((int) Math.round(srcH * 0.10));
        hatPoints.position(2).x((int) Math.round(srcW * 0.70)).y((int) Math.round(srcH * 0.60));
        hatPoints.position(3).x((int) Math.round(srcW * 0.45)).y((int) Math.round(srcH * 0.60));
        hatPoints.position(4).x((int) Math.round(srcW * 0.45)).y((int) Math.round(srcH * 0.70));
        hatPoints.position(5).x((int) Math.round(srcW * 0.35)).y((int) Math.round(srcH * 0.70));

        // 制作一个黑白mask
        Mat mask = Mat.zeros(src.size(), CV_8UC1).asMat();
        fillConvexPoly(mask, hatPoints.position(0), 6, Scalar.WHITE, CV_AA, 0);
        ImageWindowUtil.imshow("mask", mask);

        // 删除掉不需要的地方,只保留ROI region of interest
        Mat roi = new Mat();
        bitwise_and(src, src, roi, mask);
        ImageWindowUtil.imshow("roi", roi);

        // 梯度运算 - 先膨胀，在腐蚀，
        // 因为图片里有很多斜着的白线，先膨胀用周边的颜色把白色填满，在腐蚀回去
        Mat dst = new Mat();
        Mat kernel5 = Mat.ones(3, 3, CV_8U).asMat();
        morphologyEx(roi, dst, MORPH_CLOSE, kernel5);
        // 尝试多次闭操作，来加强mean出来的color，貌似效果不佳,
        // 或者可以尝试用同样的方式计算出图例里的颜色，并且扫描出旁边的文字
//        morphologyEx(roi, dst, MORPH_CLOSE, kernel5,
//                null, 3, BORDER_CONSTANT, morphologyDefaultBorderValue());
        ImageWindowUtil.imshow("MORPH_CLOSE", dst);

        // 边缘检测 - 因为图片已经颜色分明，可以开始检测边缘了
        Mat edges = new Mat();
        Canny(dst, edges, 80, 180);
        ImageWindowUtil.imshow("Canny", edges);

        // 计算轮廓 - 计算出边缘就可以计算轮廓了
        MatVector contours = new MatVector();
        findContours(edges.clone(), contours, RETR_TREE, CHAIN_APPROX_SIMPLE);

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
            if (area > 100) {

                // 利用原图来制作一个蒙版
                Mat mask4contour = Mat.zeros(roi.size(), CV_8UC1).asMat();
                fillConvexPoly(mask4contour, contour, Scalar.WHITE);
                Scalar cntBGR = mean(src, mask4contour);
                if (MapColorMatcher.compareBGR(cntBGR, prevBFR)) {
                    continue;
                }
                prevBFR = cntBGR;
                contourDataList.add(new ContourData(contour, cntBGR, area));
            }
        }

        ImageWindowUtil.imshow("drawRectImg", drawRectImg);
        ImageWindowUtil.pressEscToExit();

        return contourDataList;
    }

}

