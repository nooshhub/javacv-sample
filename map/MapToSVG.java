package map;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.io.*;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.approxPolyDP;
import static org.bytedeco.opencv.global.opencv_imgproc.arcLength;

/**
 * 将提取出来的坐标和颜色，转换成SVG图像
 *
 * @author Neal
 * @date 2021/6/14
 */
public class MapToSVG {
    private static final StringBuilder SVG_BUILDER = new StringBuilder();

    public static void process(Mat src, List<ContourData> contourDataList) {
        generateSVGStart(src.cols(), src.rows());

        contourDataList.forEach(contourData -> {
            generateSVGPoly(contourData);
        });

        generateSVGEnd();
    }

    private static void generateSVGStart(int srcW, int srcH) {
        SVG_BUILDER.append("<svg height=\"" + srcH + "\" width=\"" + srcW + "\" xmlns=\"http://www.w3.org/2000/svg\">\n");
    }

    private static void generateSVGPoly(ContourData contourData) {
        Mat contour = contourData.getContour();
        Scalar cntBGR = contourData.getCntBGR();

        long blue = Math.round(cntBGR.get(0));
        long green = Math.round(cntBGR.get(1));
        long red = Math.round(cntBGR.get(2));
        String rgb = "rgb(" + red + "," + green + "," + blue + ")";

        // 获取contour在src里坐标，转换成poly减少坐标点coordinates
        double peri = arcLength(contour, true);
        Mat approx = new Mat();
        approxPolyDP(contour, approx, 0.02 * peri, true);

        StringBuilder points = new StringBuilder();
        for (int i = 0; i < approx.total(); i++) {
            Point v = new Point(approx.data().getPointer(i));
            points.append(v.x() + "," + v.y() + " ");
        }

        SVG_BUILDER.append("  <g>\n");
        // paint block
        SVG_BUILDER.append("     <polygon " +
                "onclick=\"alert('Area: " + contourData.getArea() + "')\" " +
                "points=\"" + points + "\" " +
                "style=\"fill:" + rgb + "\"/>\n");

        // paint area info as title
        if (contourData.isBlock()) {
            SVG_BUILDER.append("    <title>");
            SVG_BUILDER.append("Area: " + contourData.getArea());
            SVG_BUILDER.append("</title>\n");
        }
        SVG_BUILDER.append("  </g>\n");
    }

    private static void generateSVGEnd() {
        SVG_BUILDER.append("  Sorry, your browser does not support inline SVG.\n");
        SVG_BUILDER.append("</svg>");
        String svgStr = SVG_BUILDER.toString();
        System.out.println(svgStr);

        File file = new File("tmp/map1.svg");
        try (OutputStream os = new FileOutputStream(file);) {
            os.write(svgStr.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
