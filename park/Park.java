package park;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.Vec4iVector;
import util.ImageWindowUtil;

import java.util.*;

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
 * @date 2021/6/13
 */
public class Park {

    public static void main(String[] args) {
        Mat src = imread("park/image-data/park1.png");
        ImageWindowUtil.imshow("src", src);

        // 低于lower和高于upper的部分变成0，之间变为255，相当于过滤背景
        Mat lower = new Mat(new Scalar(120, 120, 120, 0));
        Mat upper = new Mat(Scalar.WHITE);
        Mat whiteMask = new Mat();
        inRange(src, lower, upper, whiteMask);
        ImageWindowUtil.imshow("whiteMask", whiteMask);

        Mat masked = new Mat();
        bitwise_and(src, src, masked, whiteMask);
        ImageWindowUtil.imshow("masked", masked);

        Mat gray = new Mat();
        cvtColor(masked, gray, COLOR_BGR2GRAY);
        ImageWindowUtil.imshow("gray", gray);

        Mat edges = new Mat();
        Canny(gray, edges, 50, 200);
        ImageWindowUtil.imshow("edges", edges);

        // 手动选择区域
        // 这里按照原题比例找了下点，可以按照实际情况找出多边形的点
        Point hatPoints = new Point(6);
        int srcW = src.cols(), srcH = src.rows();
        hatPoints.position(0).x((int) Math.round(srcW * 0.05)).y((int) Math.round(srcH * 0.65));
        hatPoints.position(1).x((int) Math.round(srcW * 0.35)).y((int) Math.round(srcH * 0.5));
        hatPoints.position(2).x((int) Math.round(srcW * 0.65)).y((int) Math.round(srcH * 0.1));
        hatPoints.position(3).x((int) Math.round(srcW * 0.90)).y((int) Math.round(srcH * 0.1));
        hatPoints.position(4).x((int) Math.round(srcW * 0.90)).y((int) Math.round(srcH * 0.90));
        hatPoints.position(5).x((int) Math.round(srcW * 0.05)).y((int) Math.round(srcH * 0.90));
        Mat mask = Mat.zeros(src.size(), CV_8UC1).asMat();
        fillConvexPoly(mask, hatPoints.position(0), 6, Scalar.WHITE, CV_AA, 0);
        ImageWindowUtil.imshow("mask", mask);

        // 删除掉不需要的地方,只保留ROI region of interest
        Mat roi = new Mat();
        // TODO：为啥这两种写法，解雇一样？
        bitwise_and(edges, edges, roi, mask);
//        bitwise_and(edges, mask, roi);
        ImageWindowUtil.imshow("roi", roi);

        // 利用停车位的线来找车位, hough line
        // 输入的图象需要时边缘检测后的结果，输入参数按照图片里车位线来决定的，按实际需要调
        Vec4iVector lines = new Vec4iVector();
        // rho距离精度
        double rho = 0.1;
        // theta角度精度
        double theta = Math.PI / 10;
        // threshold炒股设定阈值才能被检测出线段
        int threshold = 15;
        // minLineLength线的最短长度，比这短的都被忽略
        int minLineLength = 9;
        // maxLineGap两条直线之间的最大间隔，小于此值，认为是一条线
        int maxLineGap = 4;
        HoughLinesP(roi, lines, rho, theta, threshold, minLineLength, maxLineGap);

        // 画出线的位置
        Mat tmpSrc4Draw = src.clone();
        System.out.println("lines total " + lines.size());
        List<Scalar4i> filteredLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Scalar4i scalar4i = lines.get(i);
            int x1 = scalar4i.get(0), y1 = scalar4i.get(1);
            int x2 = scalar4i.get(2), y2 = scalar4i.get(3);
            if (Math.abs(y2 - y1) <= 1 && Math.abs(x2 - x1) >= 25 && Math.abs(x2 - x1) <= 55) {
                line(tmpSrc4Draw, new Point(x1, y1), new Point(x2, y2), Scalar.GREEN);
                filteredLines.add(scalar4i);
            }
        }
        System.out.println("lines draw " + filteredLines.size());
        ImageWindowUtil.imshow("tmpSrc4Draw", tmpSrc4Draw);

        // 划分区域
        // 对直线按照x1进行排序
        Collections.sort(filteredLines, (l1, l2) -> l1.get(0) - l2.get(0));
        // 找到多个列，相当于每列一排车
        Map<Integer, List<Scalar4i>> clusters = new HashMap<>();
        int dIndex = 0;
        int clusDist = 10;

        for (int i = 0; i < filteredLines.size() - 1; i++) {
            Scalar4i currentLine = filteredLines.get(i);
            Scalar4i lineAfterCurrent = filteredLines.get(i + 1);
            int distance = Math.abs(lineAfterCurrent.get(0) - currentLine.get(0));
            if (distance <= clusDist) {
                if (!clusters.containsKey(dIndex)) {
                    clusters.put(dIndex, new ArrayList<>());
                }
                clusters.get(dIndex).add(currentLine);
                // add the last one
                if (i == (filteredLines.size() - 2)) {
                    clusters.get(dIndex).add(lineAfterCurrent);
                }
            } else {
                dIndex += 1;
            }
        }

        // 得到坐标
        Map<Integer, Scalar4i> rects = new HashMap<>();
        int i = 0;
        for (List<Scalar4i> cleaned : clusters.values()) {
            if (cleaned.size() > 8) {
                Collections.sort(cleaned, (l1, l2) -> l1.get(1) - l2.get(1));
                int avg_y1 = cleaned.get(0).get(1);
                int avg_y2 = cleaned.get(cleaned.size() - 1).get(1);
                System.out.println(avg_y1);
                int avg_x1 = 0;
                int avg_x2 = 0;
                for (Scalar4i tup : cleaned) {
                    avg_x1 += tup.get(0);
                    avg_x2 += tup.get(2);
                }
                avg_x1 = avg_x1 / cleaned.size();
                avg_x2 = avg_x2 / cleaned.size();
                rects.put(i, new Scalar4i(avg_x1, avg_y1, avg_x2, avg_y2));
                i += 1;
            }
        }
        System.out.println("Num of Parking Lanes " + rects.size());

        // 把列矩形画出来
        Mat tmpSrc4Block = src.clone();
        int buff = 7;
        rects.forEach((key, rect) -> {
            Point tup_topLeft = new Point(rect.get(0) - buff, rect.get(1));
            Point tup_botRight = new Point(rect.get(2) + buff, rect.get(3));
            rectangle(tmpSrc4Block, tup_topLeft, tup_botRight, Scalar.GREEN);
        });
        ImageWindowUtil.imshow("tmpSrc4Block", tmpSrc4Block);

        Mat delineated = src.clone();
        List spot_Pos = new ArrayList<>();
        double gap = 15.5;
        Map spot_dict = new HashMap();
        int tot_spots = 0;

        // 微调
        Map<Integer, Integer> adj_y1 = new HashMap<Integer, Integer>();
        adj_y1.put(0, 20);
        adj_y1.put(1, -10);
        adj_y1.put(2, 0);
        adj_y1.put(3, -11);
        adj_y1.put(4, 28);
        adj_y1.put(5, 5);
        adj_y1.put(6, -15);
        adj_y1.put(7, -15);
        adj_y1.put(8, -10);
        adj_y1.put(9, -30);
        adj_y1.put(10, 9);
        adj_y1.put(11, -32);

        Map<Integer, Integer> adj_y2 = new HashMap<Integer, Integer>();
        adj_y2.put(0, 30);
        adj_y2.put(1, 50);
        adj_y2.put(2, 15);
        adj_y2.put(3, 10);
        adj_y2.put(4, -15);
        adj_y2.put(5, 15);
        adj_y2.put(6, 15);
        adj_y2.put(7, -20);
        adj_y2.put(8, 15);
        adj_y2.put(9, 15);
        adj_y2.put(10, 0);
        adj_y2.put(11, 30);

        Map<Integer, Integer> adj_x1 = new HashMap<Integer, Integer>();
        adj_x1.put(0, -8);
        adj_x1.put(1, -15);
        adj_x1.put(2, -15);
        adj_x1.put(3, -15);
        adj_x1.put(4, -15);
        adj_x1.put(5, -15);
        adj_x1.put(6, -15);
        adj_x1.put(7, -15);
        adj_x1.put(8, -10);
        adj_x1.put(9, -10);
        adj_x1.put(10, -10);
        adj_x1.put(11, 0);

        Map<Integer, Integer> adj_x2 = new HashMap<Integer, Integer>();
        adj_x2.put(0, 0);
        adj_x2.put(1, 15);
        adj_x2.put(2, 15);
        adj_x2.put(3, 15);
        adj_x2.put(4, 15);
        adj_x2.put(5, 15);
        adj_x2.put(6, 15);
        adj_x2.put(7, 15);
        adj_x2.put(8, 10);
        adj_x2.put(9, 10);
        adj_x2.put(10, 10);
        adj_x2.put(11, 0);

        for (Map.Entry<Integer, Scalar4i> entry : rects.entrySet()) {
            int key = entry.getKey();
            Scalar4i rect = entry.getValue();

            int x1 = rect.get(0) + adj_x1.getOrDefault(key, 0);
            int x2 = rect.get(2) + adj_x2.getOrDefault(key, 0);
            int y1 = rect.get(1) + adj_y1.getOrDefault(key, 0);
            int y2 = rect.get(3) + adj_y2.getOrDefault(key, 0);

            rectangle(delineated, new Point(x1, y1),
                    new Point(x2, y2), Scalar.GREEN);
            int num_splits = (int) Math.round(Math.abs(y2 - y1) / gap);

            for (int j = 0; j < num_splits + 1; j++) {
                // 横直线
                int y = (int) (y1 + j * gap);
//                System.out.println(y);
                line(delineated, new Point(x1, y), new Point(x2, y), Scalar.GREEN);
            }

            if (key > 0 && key < rects.size() - 1) {
                // 竖直线
                int x = (x1 + x2) / 2;
                line(delineated, new Point(x, y1), new Point(x, y2), Scalar.GREEN);
            }

            // 计算数量
            if (key == 0 || key == rects.size() - 1) {
                tot_spots += num_splits + 1;
            } else {
                tot_spots += 2 * (num_splits + 1);
            }

            // 字典对应好
            if (key == 0 || key == rects.size() - 1) {
                for (int j = 0; j < num_splits + 1; j++) {
                    int cur_len = spot_dict.size();
                    int y = (int) (y1 + i * gap);
                    int x = (int) ((x1 + x2) / 2);
                    spot_dict.put(x1 + "," + y + "," + x + "," + (y + gap), cur_len + 1);
                    spot_dict.put(x + "," + y + "," + x2 + "," + (y + gap), cur_len + 2);
                }
            }
        }
        System.out.println("total parking spaces " + tot_spots);
        ImageWindowUtil.imshow("delineated", delineated);


        ImageWindowUtil.pressEscToExit();
    }
}
