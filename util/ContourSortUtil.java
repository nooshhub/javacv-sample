package util;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;

import java.util.*;

import static org.bytedeco.opencv.global.opencv_imgproc.boundingRect;
import static org.bytedeco.opencv.global.opencv_imgproc.contourArea;

/**
 * sort contours by different ways
 * sortType:
 * CS_PS_X - sort by position x
 * CS_AREA - sort by area
 * <p>
 * reverse - true desc, false asc
 *
 * @author Neal
 * @date 2021/6/12
 */
public class ContourSortUtil {

    public static final int CS_PS_X = 0;
    public static final int CS_AREA = 1;

    /**
     * sort contours since the contours may not sorted as 01234...n
     *
     * @param contours original contours
     * @param reverse  reverse sort, true desc, false asc
     * @return sorted contours
     */
    public static MatVector sortContours(MatVector contours, int sortType, boolean reverse) {
        switch (sortType) {
            case CS_PS_X:
                return sortContoursByPositionX(contours, reverse);
            case CS_AREA:
                return sortContoursByArea(contours, reverse);
            default:
                return sortContoursByArea(contours, reverse);
        }
    }

    /**
     * sort contours by area since the contours may not sorted as 01234...n
     *
     * @param contours original contours
     * @param reverse  reverse sort
     * @return sorted contours
     */
    private static MatVector sortContoursByArea(MatVector contours, boolean reverse) {
        List<Mat> cntList = new ArrayList<>((int) contours.size());
        for (int i = 0; i < contours.size(); i++) {
            cntList.add(contours.get(i));
        }

        Collections.sort(cntList, (c1, c2) -> reverse ?
                (int) Math.round(contourArea(c2) - contourArea(c1)) :
                (int) Math.round(contourArea(c1) - contourArea(c2)));

        return new MatVector(cntList.toArray(new Mat[cntList.size()]));
    }

    /**
     * sort contours by position x since the contours may not sorted as 01234...n
     *
     * @param contours original contours
     * @param reverse  reverse sort
     * @return sorted contours
     */
    private static MatVector sortContoursByPositionX(MatVector contours, boolean reverse) {
        Comparator<Rect> rectComparator = (r1, r2) -> reverse ? r2.x() - r1.x() : r1.x() - r2.x();
        SortedMap<Rect, Mat> rectMatSortedMap = new TreeMap<>(rectComparator);

        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            Rect r = boundingRect(contour);
            rectMatSortedMap.put(r, contour);
        }

        return new MatVector(rectMatSortedMap.values().toArray(new Mat[(int) contours.size()]));
    }
}
