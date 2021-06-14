package map;

import org.bytedeco.opencv.opencv_core.Scalar;

/**
 * ��ȡ���������ɫ��ԭͼ���ԭɫ���������൱��������ȡ����Χֵ
 * Ex:
 * ��ɫ��ȡ������
 * rgb(249.62991573033707,249.63600187265916,8.397705992509364)
 * ��ԭͼȡɫ������ȡ����
 * (255, 255, 0) or (255, 255, 1)
 *
 * @author Neal
 * @date 2021/6/14
 */
public class MapColorMatcher {

    /**
     * �������ɫ���ֵ
     */
    private static final int THRESHOLD = 10;

    public static boolean compareBGR(Scalar cur, Scalar prev) {
        if (prev == null) {
            return false;
        }

        double blue = Math.abs(cur.get(0) - prev.get(0));
        double green = Math.abs(cur.get(1) - prev.get(1));
        double red = Math.abs(cur.get(2) - prev.get(2));

        boolean isBlueMatch = blue >= 0 && blue < THRESHOLD;
        boolean isGreenMatch = green >= 0 && green < THRESHOLD;
        boolean isRedMatch = red >= 0 && red < THRESHOLD;

        return isBlueMatch && isGreenMatch && isRedMatch;
    }

}
