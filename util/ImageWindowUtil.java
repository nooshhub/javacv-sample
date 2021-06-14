package util;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.*;

import static org.bytedeco.opencv.global.opencv_highgui.cvWaitKey;
import static org.bytedeco.opencv.global.opencv_highgui.destroyAllWindows;

/**
 * image utils for testing images with windows
 *
 * @author neals
 * @date 6/10/2021
 */
public class ImageWindowUtil {

    /**
     * image position x
     */
    static int x = 0;

    /**
     * image position y
     */
    static int y = 0;

    /**
     * total count of images
     */
    static int count = 0;

    /**
     * a global switch to turn on and off windows
     */
    public static boolean showImageWindow = true;

    /**
     * work with the {@code pressEscToExit}
     *
     * @param windowTitle
     * @param image
     */
    public static void imshow(String windowTitle, Mat image) {
        if (showImageWindow) {
            org.bytedeco.opencv.global.opencv_highgui.imshow(windowTitle, image);
            ++count;
        }
    }

    /**
     * imshow does not work on windows, build a new one
     * auto set with and height as image size
     * auto set position, each line has 4 images
     *
     * @param windowTitle
     * @param image
     */
    public static void imshow1(String windowTitle, Mat image) {
        if (showImageWindow) {
            //TODO: gamma , 1?
            CanvasFrame canvasFrame = new CanvasFrame(windowTitle);
            canvasFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
//            canvasFrame.setCanvasSize(image.cols(), image.rows());
            canvasFrame.setCanvasSize(300, 200);

            // each line has 4 images
//            ++count;
//            x += 200;
//            if (count % 4 == 0) {
//                x = 200;
//                y += 200;
//            }
//            canvasFrame.setLocation(x, y);

            // add scroll bar
            Canvas canvas = canvasFrame.getCanvas();
            canvasFrame.getContentPane().removeAll();
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.add(canvas);
            canvasFrame.add(scrollPane);

            // show image
            canvasFrame.showImage(new OpenCVFrameConverter.ToMat().convert(image));
            ++count;
        }
    }

    /**
     * press Esc to exit and close all windows
     */
    public static void pressEscToExit() {
        // if there is no images window, go ahead exit
        if (count == 0) {
            return;
        }

        // delay forever and press ESC to exit and close all windows
        int key = cvWaitKey(0);
        if (key == 27) {
            destroyAllWindows();
        }
    }
}
