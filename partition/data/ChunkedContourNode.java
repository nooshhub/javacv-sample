package partition.data;

import org.bytedeco.opencv.opencv_core.Mat;

/**
 * 被切分后的Contour
 *
 * @author Neal
 * @date 2021/6/15
 */
public class ChunkedContourNode {
    /**
     * 被切分后的轮廓
     */
    private Mat chunkedContour;

    /**
     * 被切分后的轮廓在RR单元格里的百分比
     */
    private double chunkedContourInRRratio;

    /**
     * 原始轮廓ID
     */
    private int originContourId;

    /**
     * 是否被绘制出来
     */
    private boolean isDrawn;
}
