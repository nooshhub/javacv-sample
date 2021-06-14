package map;

import org.bytedeco.opencv.opencv_core.Scalar;

/**
 * 提取轮廓里的颜色和原图里的原色并不绝对相当，这里先取个范围值
 * Ex:
 * 黄色提取出来是
 * rgb(249.62991573033707,249.63600187265916,8.397705992509364)
 * 而原图取色器里提取的是
 * (255, 255, 0) or (255, 255, 1)
 *
 * @author Neal
 * @date 2021/6/14
 */
public class MapColorMatcher {

    /**
     * 允许的颜色误差值
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
