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

        // �ֶ�ѡ������ ROI
        // ���ﰴ��ԭ����������µ㣬���԰���ʵ������ҳ�����εĵ�
        Point hatPoints = new Point(4);
        int srcW = src.cols(), srcH = src.rows();
        hatPoints.position(0).x((int) Math.round(srcW * 0.85)).y((int) Math.round(srcH * 0.36));
        hatPoints.position(1).x((int) Math.round(srcW * 0.89)).y((int) Math.round(srcH * 0.36));
        hatPoints.position(2).x((int) Math.round(srcW * 0.89)).y((int) Math.round(srcH * 0.9));
        hatPoints.position(3).x((int) Math.round(srcW * 0.85)).y((int) Math.round(srcH * 0.9));

        // ����һ���ڰ�mask
        Mat mask = Mat.zeros(src.size(), CV_8UC1).asMat();
        fillConvexPoly(mask, hatPoints.position(0), 4, Scalar.WHITE, CV_AA, 0);
        ImageWindowUtil.imshow("mask", mask);

        // ɾ��������Ҫ�ĵط�,ֻ����ROI region of interest
        Mat roi = new Mat();
        bitwise_and(src, src, roi, mask);
        ImageWindowUtil.imshow("roi", roi);

        // ��Ե��� - ��ΪͼƬ�Ѿ���ɫ���������Կ�ʼ����Ե��
        Mat edges = new Mat();
        Canny(roi, edges, 80, 180);
        ImageWindowUtil.imshow("Canny", edges);

        // �������� - �������Ե�Ϳ��Լ���������
        MatVector contours = new MatVector();
        findContours(edges.clone(), contours, RETR_LIST, CHAIN_APPROX_SIMPLE);

        // �������� - ������
        Mat drawContoursImg = roi.clone();
        drawContours(drawContoursImg, contours, -1, Scalar.RED);
        ImageWindowUtil.imshow("drawContoursImg", drawContoursImg);

        List<ContourData> contourDataList = new ArrayList<>();
        Mat drawRectImg = roi.clone();
        Scalar prevBFR = null;
        for (int i = 0; i < contours.size(); i++) {
            // ������Ӿ���
            Mat contour = contours.get(i);
            Rect r = boundingRect(contour);
            rectangle(drawRectImg, r, Scalar.RED);

            double area = contourArea(contour);
            if (area > 50 && area < 150) {

                // ����ԭͼ������һ���ɰ�
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
