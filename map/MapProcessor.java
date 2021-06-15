package map;

import org.bytedeco.opencv.opencv_core.Mat;
import util.ImageWindowUtil;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

/**
 * Map处理入口程序
 *
 * @author Neal
 * @date 2021/6/14
 */
public class MapProcessor {
    public static void main(String[] args) {
        String filePath = "map/image-data/map1.png";
        Mat src = imread(filePath);
        ImageWindowUtil.imshow("src", src);

        List<ContourData> contourDataList = new ArrayList<>();

        // 区块
        List<ContourData> blockContours = MapAnalysis.process(src);
        contourDataList.addAll(blockContours);

        // 颜色图例
        List<ContourData> colorSampleContours = MapColorSampleAnalysis.process(src);
        contourDataList.addAll(colorSampleContours);

        // TODO: tessaract识别中文
        MapOCR.process(src);

        // 生成SVG
        MapToSVG.process(src, contourDataList);
    }
}
