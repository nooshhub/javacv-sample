package map;

import org.bytedeco.opencv.opencv_core.Mat;
import util.ImageWindowUtil;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

/**
 * Map������ڳ���
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

        // ����
        List<ContourData> blockContours = MapAnalysis.process(src);
        contourDataList.addAll(blockContours);

        // ��ɫͼ��
        List<ContourData> colorSampleContours = MapColorSampleAnalysis.process(src);
        contourDataList.addAll(colorSampleContours);

        // TODO: tessaractʶ������
        MapOCR.process(src);

        // ����SVG
        MapToSVG.process(src, contourDataList);
    }
}
