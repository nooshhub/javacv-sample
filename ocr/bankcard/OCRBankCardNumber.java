package ocr.bankcard;

import org.bytedeco.opencv.opencv_core.*;
import util.ContourSortUtil;
import util.ImageWindowUtil;

import java.util.*;
import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Bank card number recognition
 *
 * @author sanyu
 * @date 2021/6/12
 */
public class OCRBankCardNumber {
    public static void main(String[] args) {
        Map<Integer, Mat> digits = prepareDigitTemplate();

        Mat cardImg = imread("ocr/bankcard/image-data/OCRBankCard1.png");
//        Mat cardImg = imread("ocr/bankcard/image-data/OCRBankCard2.png");
        ImageWindowUtil.imshow("cardImg", cardImg);

        //==========处理card========================
        // 形态学 - kernel来去干扰，去噪声
        Mat rectKernel = getStructuringElement(MORPH_RECT, new Size(10, 3));
        Mat sqKernel = getStructuringElement(MORPH_RECT, new Size(5, 5));


        Mat grayCardImg = new Mat();
        cvtColor(cardImg, grayCardImg, COLOR_BGR2GRAY);

        Mat resizedCardImg = new Mat();
        Rect cardR = boundingRect(grayCardImg);
        resize(cardImg, resizedCardImg, new Size(300, Math.round(cardR.height() / (cardR.width() * 1F / 300))));

        Mat grayResizedCardImg = new Mat();
        cvtColor(resizedCardImg, grayResizedCardImg, COLOR_BGR2GRAY);

        // 礼帽，顶帽操作，突出更明亮的区域，上面我们按照数字大小需要新建了一个rectKernel
        Mat tophat = new Mat();
        morphologyEx(grayResizedCardImg, tophat, MORPH_TOPHAT, rectKernel);
        ImageWindowUtil.imshow("tophat", tophat);

        // sobel - 这里只做了x，通常我们会计算x，y，然后在abs，在normalize
        Mat gradX = new Mat();
        Sobel(tophat, gradX, CV_32F, 1, 0);
        // -1 和 3效果一样，默认是3*3
//        Sobel(tophat, gradX, CV_32F, 1, 0, -1,
//                1, 0, BORDER_DEFAULT);
        convertScaleAbs(gradX, gradX);

        // 下面如何将数字看做一个块儿，闭操作（膨胀再腐蚀），将数字连在一起
        morphologyEx(gradX, gradX, MORPH_CLOSE, rectKernel);
        // THRESH_OTSU会自动寻找合适的阈值，适合双峰，需把阈值参数设置为0
        Mat threshGradX = new Mat();
        threshold(gradX, threshGradX, 0, 255, THRESH_OTSU);
        ImageWindowUtil.imshow("threshGradX", threshGradX);

        // 数字是连起来了，但是块儿里有空隙
        morphologyEx(threshGradX, threshGradX, MORPH_CLOSE, sqKernel);
        ImageWindowUtil.imshow("threshGradX 2", threshGradX);

        // 计算轮廓
        MatVector numBlockContours = new MatVector();
        findContours(threshGradX.clone(), numBlockContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        Mat curCardImg = resizedCardImg.clone();
        drawContours(curCardImg, numBlockContours, -1, Scalar.RED, 3,
                LINE_8, null, Integer.MAX_VALUE, null);
        ImageWindowUtil.imshow("curCardImg", curCardImg);

        List<Mat> selectedNumBlockContoursList = new ArrayList<>();
        for (int i = 0; i < numBlockContours.size(); i++) {
            // 计算外接矩形
            Mat contour = numBlockContours.get(i);
            Rect r = boundingRect(contour);
            float ar = r.width() * 1F / r.height();

            // 选择合适的区域，根据实际任务来，这里的基本都是四字一组
            if (ar > 2.5 && ar < 4.0) {
                System.out.println(ar + ", " + r.width() + ", " + r.height());
                if ((r.width() > 45 && r.width() < 55) & (r.height() > 10 && r.height() < 20)) {
                    selectedNumBlockContoursList.add(contour);
                }
            }
        }

        MatVector selectedNumBlockContours = new MatVector(selectedNumBlockContoursList.toArray(new Mat[selectedNumBlockContoursList.size()]));
        Mat tempCarImg = grayResizedCardImg.clone();
        drawContours(tempCarImg, selectedNumBlockContours, -1, Scalar.WHITE);
        ImageWindowUtil.imshow("tempCarImg", tempCarImg);

        List<Integer> cardNums = new ArrayList<>();

        // 遍历排好序的数字块
        MatVector sortedNumBlockContours = ContourSortUtil.sortContours(selectedNumBlockContours,
                ContourSortUtil.CS_PS_X, false);
        for (int i = 0; i < sortedNumBlockContours.size(); i++) {
            Mat numBlock = sortedNumBlockContours.get(i);
            Rect originalRoi = boundingRect(numBlock);
            Rect resizedRoi = new Rect(originalRoi.x() - 5,
                    originalRoi.y() - 5,
                    originalRoi.width() + 10,
                    originalRoi.height() + 10);
            Mat groupOfNums = grayResizedCardImg.apply(resizedRoi);

            // 找到块儿之后就是队每个数字进行轮廓检测了
            // 预处理 - 计算轮廓
            threshold(groupOfNums, groupOfNums, 0, 255, THRESH_OTSU);
//            ImageWindowUtil.imshow("groupOfNums" + i, groupOfNums);

            MatVector numContours = new MatVector();
            findContours(groupOfNums.clone(), numContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            MatVector sortedNumContours = ContourSortUtil.sortContours(numContours, ContourSortUtil.CS_PS_X, false);

            List<String> groupOutput = new ArrayList<>();
            // 计算每一个组中的每一个数值
            for (int j = 0; j < sortedNumContours.size(); j++) {
                Mat num = sortedNumContours.get(j);
                Rect numRoi = boundingRect(num);
                Mat numImg = groupOfNums.apply(numRoi);
                resize(numImg, numImg, new Size(57, 88));
                // ImageWindowUtil.imshow("numImg" + i + "," + j, numImg);

                // 计算匹配得分
                // 在模版中计算每一个得分
                double maxScore = Double.MIN_VALUE;
                int cardNum = -1;
                for (int k = 0; k < digits.size(); k++) {
                    Mat digitROI = digits.get(k);
                    Mat res = new Mat();
                    matchTemplate(numImg, digitROI, res, TM_CCOEFF);
                    double[] maxVal = new double[1];
                    minMaxLoc(res, null, maxVal, null, null, null);
                    System.out.println(k + ", " + maxVal[0]);
                    if (maxVal[0] > maxScore) {
                        maxScore = maxVal[0];
                        cardNum = k;
                    }
                }
                groupOutput.add(String.valueOf(cardNum));
                cardNums.add(cardNum);
            }
            // 画出结果
            rectangle(resizedCardImg, resizedRoi, Scalar.RED);
            putText(resizedCardImg, String.join(",", groupOutput),
                    new Point(resizedRoi.x(), resizedRoi.y() - 15),
                    FONT_HERSHEY_SIMPLEX, 0.5, Scalar.RED, 2, LINE_8, false);
        }
        ImageWindowUtil.imshow("draw card", resizedCardImg);
        System.out.println(Arrays.toString(cardNums.toArray()));

        ImageWindowUtil.pressEscToExit();
    }

    private static Map<Integer, Mat> prepareDigitTemplate() {
        // load image
        Mat numTemplImg = imread("ocr/bankcard/image-data/OCRBankCardNumberTemplate.png");

        // gray
        Mat gray = new Mat();
        cvtColor(numTemplImg, gray, COLOR_BGR2GRAY);

        // binary
        Mat binary = new Mat();
        threshold(gray, binary, 10, 255, THRESH_BINARY_INV);
        ImageWindowUtil.imshow("Template bianry", binary);

        // contours
        // 轮廓检测findContours函数接受的参数为二值图，即黑白的（不是灰度图），
        // RETR_EXTERNAL只检测外轮廓，CHAIN_APPROX_SIMPLE只保留终点坐标（角尖尖）
        // 返回的list中每个元素都是图像的一个轮廓
        MatVector contours = new MatVector();
        findContours(binary.clone(), contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        Mat drawContoursImg = numTemplImg.clone();
        drawContours(drawContoursImg, contours, -1, Scalar.RED, 3,
                LINE_8, null, Integer.MAX_VALUE, null);
        ImageWindowUtil.imshow("Template drawContoursImg", drawContoursImg);
        System.out.println("contours size " + contours.size());

        // sort contours
        MatVector sortedContours = ContourSortUtil.sortContours(contours, ContourSortUtil.CS_PS_X, false);

        // prepare number to template map
        Map<Integer, Mat> digits = new HashMap<>();
        // loop sorted contours
        for (int i = 0; i < sortedContours.size(); i++) {
            // 计算外接矩形并且resize成合适的大小
            Mat contour = sortedContours.get(i);
            Rect r = boundingRect(contour);
            Mat roi = binary.apply(r);
            resize(roi, roi, new Size(57, 88));
            ImageWindowUtil.imshow("Template roi" + i, roi);
            digits.put(i, roi);
        }
        return digits;
    }


}
