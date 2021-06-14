package map;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;

/**
 * 将轮廓里计算得到的数据汇总在这里，方便后面生成svg等。
 *
 * @author Neal
 * @date 2021/6/14
 */
public class ContourData {
    public static final int TYPE_COLOR_SAMPLE = 0;
    public static final int TYPE_BLOCK = 1;

    private int type = -1;
    private Mat contour;
    private Scalar cntBGR;
    private double area;

    public ContourData(Mat contour, Scalar cntBGR) {
        this.type = TYPE_COLOR_SAMPLE;
        this.contour = contour;
        this.cntBGR = cntBGR;
    }

    public ContourData(Mat contour, Scalar cntBGR, double area) {
        this.type = TYPE_BLOCK;
        this.contour = contour;
        this.cntBGR = cntBGR;
        this.area = area;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public Mat getContour() {
        return contour;
    }

    public void setContour(Mat contour) {
        this.contour = contour;
    }

    public Scalar getCntBGR() {
        return cntBGR;
    }

    public void setCntBGR(Scalar cntBGR) {
        this.cntBGR = cntBGR;
    }

    public boolean isBlock() {
        return type == TYPE_BLOCK;
    }
}
