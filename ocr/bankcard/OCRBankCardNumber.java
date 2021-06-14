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

        //==========����card========================
        // ��̬ѧ - kernel��ȥ���ţ�ȥ����
        Mat rectKernel = getStructuringElement(MORPH_RECT, new Size(10, 3));
        Mat sqKernel = getStructuringElement(MORPH_RECT, new Size(5, 5));


        Mat grayCardImg = new Mat();
        cvtColor(cardImg, grayCardImg, COLOR_BGR2GRAY);

        Mat resizedCardImg = new Mat();
        Rect cardR = boundingRect(grayCardImg);
        resize(cardImg, resizedCardImg, new Size(300, Math.round(cardR.height() / (cardR.width() * 1F / 300))));

        Mat grayResizedCardImg = new Mat();
        cvtColor(resizedCardImg, grayResizedCardImg, COLOR_BGR2GRAY);

        // ��ñ����ñ������ͻ���������������������ǰ������ִ�С��Ҫ�½���һ��rectKernel
        Mat tophat = new Mat();
        morphologyEx(grayResizedCardImg, tophat, MORPH_TOPHAT, rectKernel);
        ImageWindowUtil.imshow("tophat", tophat);

        // sobel - ����ֻ����x��ͨ�����ǻ����x��y��Ȼ����abs����normalize
        Mat gradX = new Mat();
        Sobel(tophat, gradX, CV_32F, 1, 0);
        // -1 �� 3Ч��һ����Ĭ����3*3
//        Sobel(tophat, gradX, CV_32F, 1, 0, -1,
//                1, 0, BORDER_DEFAULT);
        convertScaleAbs(gradX, gradX);

        // ������ν����ֿ���һ��������ղ����������ٸ�ʴ��������������һ��
        morphologyEx(gradX, gradX, MORPH_CLOSE, rectKernel);
        // THRESH_OTSU���Զ�Ѱ�Һ��ʵ���ֵ���ʺ�˫�壬�����ֵ��������Ϊ0
        Mat threshGradX = new Mat();
        threshold(gradX, threshGradX, 0, 255, THRESH_OTSU);
        ImageWindowUtil.imshow("threshGradX", threshGradX);

        // �������������ˣ����ǿ�����п�϶
        morphologyEx(threshGradX, threshGradX, MORPH_CLOSE, sqKernel);
        ImageWindowUtil.imshow("threshGradX 2", threshGradX);

        // ��������
        MatVector numBlockContours = new MatVector();
        findContours(threshGradX.clone(), numBlockContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        Mat curCardImg = resizedCardImg.clone();
        drawContours(curCardImg, numBlockContours, -1, Scalar.RED, 3,
                LINE_8, null, Integer.MAX_VALUE, null);
        ImageWindowUtil.imshow("curCardImg", curCardImg);

        List<Mat> selectedNumBlockContoursList = new ArrayList<>();
        for (int i = 0; i < numBlockContours.size(); i++) {
            // ������Ӿ���
            Mat contour = numBlockContours.get(i);
            Rect r = boundingRect(contour);
            float ar = r.width() * 1F / r.height();

            // ѡ����ʵ����򣬸���ʵ��������������Ļ�����������һ��
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

        // �����ź�������ֿ�
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

            // �ҵ����֮����Ƕ�ÿ�����ֽ������������
            // Ԥ���� - ��������
            threshold(groupOfNums, groupOfNums, 0, 255, THRESH_OTSU);
//            ImageWindowUtil.imshow("groupOfNums" + i, groupOfNums);

            MatVector numContours = new MatVector();
            findContours(groupOfNums.clone(), numContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            MatVector sortedNumContours = ContourSortUtil.sortContours(numContours, ContourSortUtil.CS_PS_X, false);

            List<String> groupOutput = new ArrayList<>();
            // ����ÿһ�����е�ÿһ����ֵ
            for (int j = 0; j < sortedNumContours.size(); j++) {
                Mat num = sortedNumContours.get(j);
                Rect numRoi = boundingRect(num);
                Mat numImg = groupOfNums.apply(numRoi);
                resize(numImg, numImg, new Size(57, 88));
                // ImageWindowUtil.imshow("numImg" + i + "," + j, numImg);

                // ����ƥ��÷�
                // ��ģ���м���ÿһ���÷�
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
            // �������
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
        // �������findContours�������ܵĲ���Ϊ��ֵͼ�����ڰ׵ģ����ǻҶ�ͼ����
        // RETR_EXTERNALֻ�����������CHAIN_APPROX_SIMPLEֻ�����յ����꣨�Ǽ�⣩
        // ���ص�list��ÿ��Ԫ�ض���ͼ���һ������
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
            // ������Ӿ��β���resize�ɺ��ʵĴ�С
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
