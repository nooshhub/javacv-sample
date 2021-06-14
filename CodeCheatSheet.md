##### read/write

```java
// opencv_imgcodecs
Mat src = imread(filename);
```

##### roi 

```java
// mask, bg-color: black
        Mat mask = Mat.zeros(srcImage.size(), CV_8UC3).asMat();
        ImageWindowUtil.imshow("mask1", mask);

        // TODO: get the blue line's point and create approxPolyDP
        // drawn rectangle
        Rect r = new Rect();
        r.x(250).y(60).width(450).height(430);
        int x = r.x(), y = r.y(), w = r.width(), h = r.height();
        rectangle(mask, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);
        ImageWindowUtil.imshow("rect", mask);

        // fill white in ROI
        Point hatPoints = new Point(4);
        hatPoints.position(0).x(x).y(y);
        hatPoints.position(1).x(x + w).y(y);
        hatPoints.position(2).x(x + w).y(y + h);
        hatPoints.position(3).x(x).y(y + h);
        fillConvexPoly(mask, hatPoints.position(0), 4, Scalar.WHITE, CV_AA, 0);
        ImageWindowUtil.imshow("mask2", mask);

        // cut out ROI image
        bitwise_and(srcImage, mask, srcImage);
        ImageWindowUtil.imshow("masked 3", srcImage);
```



##### split/merge

```java
// split Mat to MatVector, merge MatVector to Mat
        MatVector vec = new MatVector();
        split(src, vec);
        ImageWindowUtil.imshow("0", vec.get(0));
        ImageWindowUtil.imshow("1", vec.get(1));
        ImageWindowUtil.imshow("2", vec.get(2));

        merge(vec, src);
        ImageWindowUtil.imshow("merged", src);
```



##### keep RED channel's color


```java
// keep RED channel's color
        UByteIndexer srcIndexer = src.createIndexer();
        for (int x = 0; x < srcIndexer.rows(); x++) {
            for (int y = 0; y < srcIndexer.cols(); y++) {
                int[] values = new int[3];
                srcIndexer.get(x, y, values);
                values[0] = 0;
                values[1] = 0;
                srcIndexer.put(x, y, values);
            }
        }
        ImageWindowUtil.imshow("processed", src);
```

##### 卷积convolve和填充

```java
// 边界填充
        Mat replicate = new Mat();
        Mat reflect = new Mat();
        Mat reflect101 = new Mat();
        Mat wrap = new Mat();
        Mat constant = new Mat();
        copyMakeBorder(src, replicate, 50, 50, 50, 50, BORDER_REPLICATE);
        copyMakeBorder(src, reflect, 50, 50, 50, 50, BORDER_REFLECT);
        copyMakeBorder(src, reflect101, 50, 50, 50, 50, BORDER_REFLECT_101);
        copyMakeBorder(src, wrap, 50, 50, 50, 50, BORDER_WRAP);
        copyMakeBorder(src, constant, 50, 50, 50, 50, BORDER_CONSTANT, Scalar.BLACK);
        ImageWindowUtil.imshow("replicate", replicate);
        ImageWindowUtil.imshow("reflect", reflect);
        ImageWindowUtil.imshow("reflect101", reflect101);
        ImageWindowUtil.imshow("wrap", wrap);
        ImageWindowUtil.imshow("constant", constant);
```



##### matplotlib for java

https://stackoverflow.com/questions/18992184/java-plotting-library-like-pythons-matplotlib



##### 图像融合

```java
// 图像融合
        // size相同 add
        Mat addedMat = add(src, src).asMat();
        ImageWindowUtil.imshow("addedMat", addedMat);
        // size不同先resize，再add或者addWeighted
        Mat src2 = imread("test1.png");
//        resize(src2, src2, src.size());
        // fx, fy
        resize(src2, src2, src.size(), 3, 1, INTER_LINEAR );
        ImageWindowUtil.imshow("resize", src2);
        // gamma 0 - 偏置项 提亮？提量？ gamma scalar added to each sum.
        addWeighted(src, 0.4, src2, 0.6, 0, src2);
        ImageWindowUtil.imshow("addWeighted", src2);
```

##### 图像阈值 threshold

```java
// 图像阈值 threshold
        Mat dst = new Mat();
        threshold(src, dst, 127, 255, THRESH_BINARY);
        ImageWindowUtil.imshow("THRESH_BINARY", dst);
        threshold(src, dst, 127, 255, THRESH_BINARY_INV);
        ImageWindowUtil.imshow("THRESH_BINARY_INV", dst);
        threshold(src, dst, 127, 255, THRESH_TRUNC);
        ImageWindowUtil.imshow("THRESH_BINARY", dst);
        threshold(src, dst, 127, 255, THRESH_TOZERO);
        ImageWindowUtil.imshow("THRESH_BINARY", dst);
        threshold(src, dst, 127, 255, THRESH_TOZERO_INV);
        ImageWindowUtil.imshow("THRESH_BINARY", dst);
```

##### 图像平滑处理 各种滤波器 卷积 

```java
// 图像阈值 threshold
Mat dst = new Mat();
// 均值滤波，简单的平均卷积操作
blur(src, dst, new Size(3, 3));
ImageWindowUtil.imshow("均值滤波", dst);
// 方框滤波 归一化 normalize true则和均值滤波一样，false了就没有除9，归一化了
// 这个滤波很牛逼啊，做完+再来个分水岭waterThreshold区块就分好了。。
boxFilter(src, dst, -1, new Size(3, 3), null, false, BORDER_DEFAULT);
ImageWindowUtil.imshow("方框滤波", dst);
// 高斯滤波 满足高斯分布，重视中间的，靠中间的近的权重越高
GaussianBlur(src, dst, new Size(5, 5), 1);
ImageWindowUtil.imshow("高斯滤波", dst);
// 中值滤波，5X5的矩阵里的值，从小到大，找中间的值
medianBlur(src, dst, 5);
ImageWindowUtil.imshow("中值滤波", dst);
```

##### 形态学 - 腐蚀操作 膨胀操作

```java
// 腐蚀操作 - 去毛刺，突出的细线
        Mat dst = new Mat();
        Mat kernel = Mat.ones(5,5, CV_8U).asMat();
        erode(src, dst, kernel);
//        erode(src, dst, kernel, null, 3, BORDER_DEFAULT, new Scalar(morphologyDefaultBorderValue()));
        ImageWindowUtil.imshow("腐蚀操作", dst);

        // 膨胀操作 - 把腐蚀变细的在膨胀变大
        dilate(dst, dst, kernel);
        ImageWindowUtil.imshow("膨胀操作", dst);
```

##### 开运算（先腐蚀再膨胀） 闭运算（先膨胀再腐蚀） 

```java
// 开运算
Mat dst = new Mat();
Mat kernel = Mat.ones(5,5, CV_8U).asMat();
morphologyEx(src, dst, MORPH_OPEN, kernel);
ImageWindowUtil.imshow("开运算", dst);

// 闭运算
morphologyEx(dst, dst, MORPH_CLOSE, kernel);
ImageWindowUtil.imshow("闭运算", dst);
```

##### 梯度运算 礼帽 黑帽

```java
// 梯度运算 - 膨胀的-腐蚀=边缘
        Mat dst = new Mat();
        Mat kernel = Mat.ones(7, 7, CV_8U).asMat();

        dilate(src, dst, kernel, null, 5, BORDER_DEFAULT, morphologyDefaultBorderValue());
        erode(dst, dst, kernel, null, 5, BORDER_DEFAULT, morphologyDefaultBorderValue());
        ImageWindowUtil.imshow("梯度运算 dilate erode", dst);

        //先膨胀再腐蚀，相当于闭运算
        morphologyEx(src, dst, MORPH_CLOSE, kernel, null, 5, BORDER_CONSTANT, morphologyDefaultBorderValue());
        ImageWindowUtil.imshow("梯度运算 CLOSE", dst);

        morphologyEx(src, dst, MORPH_GRADIENT, kernel);
//        morphologyEx(src, dst, MORPH_GRADIENT, kernel, null, 5, BORDER_CONSTANT, morphologyDefaultBorderValue());
        ImageWindowUtil.imshow("梯度运算 GRADIENT", dst);

        // 礼帽 原始输入 - 开运算 = 刺儿，边毛
        morphologyEx(src, dst, MORPH_TOPHAT, kernel);
        ImageWindowUtil.imshow("梯度运算 TOPHAT", dst);

        // 黑帽  闭运算 - 原始输入 = 无刺，得轮廓点
        morphologyEx(src, dst, MORPH_BLACKHAT, kernel);
        ImageWindowUtil.imshow("梯度运算 BLACKHAT", dst);
```



##### 梯度计算- Sobel算子

有边缘的地方，有不同颜色，就是梯度，同样的颜色，那也就没梯度了

```java
 // Sobel算子 边缘检测滤波器 ，不建议dx，dy都设置为1一起计算，分开计算会清楚点
        Mat soblex = new Mat();
        // 默认depth -1
//        Sobel(src, dst, -1, 1, 0);
        Sobel(src, soblex, CV_64F, 1, 0);
        ImageWindowUtil.imshow("Sobelx算子1", soblex);

        convertScaleAbs(soblex, soblex);
        ImageWindowUtil.imshow("Sobelx算子2", soblex);

        Mat sobley = new Mat();
        // 默认depth -1
//        Sobel(src, dst, -1, 1, 0);
        Sobel(src, sobley, CV_64F, 0, 1);
        ImageWindowUtil.imshow("Sobely算子1", sobley);

        convertScaleAbs(sobley, sobley);
        ImageWindowUtil.imshow("Sobely算子2", sobley);

        Mat dst = new Mat();
        addWeighted(soblex, 0.5, sobley, 0.5, 0, dst);
        ImageWindowUtil.imshow("dst", dst);
```

##### 梯度计算- Scharr算子

kernel里更敏感

##### 梯度计算- laplacian算子

对变换更敏感，对噪音点敏感，通常要和其他算子一起使用

```java
// load image
        String filename = "demo3.png";
        Mat src = imread(filename, IMREAD_GRAYSCALE);
        ImageWindowUtil.imshow(filename, src);

        // Sobel算子 边缘检测滤波器 ，不建议dx，dy都设置为1一起计算，分开计算会清楚点
        Mat sobelx = new Mat();
        Sobel(src, sobelx, CV_64F, 1, 0);
        convertScaleAbs(sobelx, sobelx);
        Mat sobely = new Mat();
        Sobel(src, sobely, CV_64F, 0, 1);
        convertScaleAbs(sobely, sobely);
        Mat dst1 = new Mat();
        addWeighted(sobelx, 0.5, sobely, 0.5, 0, dst1);
        ImageWindowUtil.imshow("Sobel", dst1);

        // scharr算子
        Mat scharrx = new Mat();
        Scharr(src, scharrx, CV_64F, 1, 0);
        convertScaleAbs(scharrx, scharrx);
        Mat scharry = new Mat();
        Scharr(src, scharry, CV_64F, 0, 1);
        convertScaleAbs(scharry, scharry);
        Mat dst2 = new Mat();
        addWeighted(scharrx, 0.5, scharry, 0.5, 0, dst2);
        ImageWindowUtil.imshow("Scharr", dst2);

        Mat dst3 = new Mat();
        Laplacian(src, dst3, CV_64F);
        convertScaleAbs(dst3, dst3);
        ImageWindowUtil.imshow("Laplacian", dst3);
```



##### Canny边缘检测

1） 使用高斯滤波器，以平滑图片，滤除噪声

2）计算图像中每个像素点的梯度强度和方向

3）应用非极大值（Non-Maximum Suppression）抑制，以消除边缘检测带来的杂散响应，就是从多个结果里选占比率最大的

4）应用双阈值（Double-Threshold）检测来确定真实的和存在的边缘

5）通过抑制孤立的弱边缘最终完成边缘检测

 

```java
  // Canny 太牛逼了，不做灰度也能找出边缘
        Mat dst3 = new Mat();
        Canny(src, dst3, 80, 150);
//        Canny(src, dst3, 50, 150);
        ImageWindowUtil.imshow("Canny", dst3);
```



##### 图像金字塔

高斯金字塔

拉普拉斯金字塔

用例：图象图书提取，每层提取的图象不一样，然后总结在一起。



##### 金字塔制作

高斯金字塔

```java
 // Gaussian pyramid pyrUp/pyrDown
        Mat dst3 = new Mat();
        pyrUp(src, dst3);
//        pyrDown(src, dst3);
        pyrDown(dst3, dst3);
        ImageWindowUtil.imshow("pyr", dst3);
```



拉普拉斯金字塔

```java
  // Laplacian pyramid pyrUp/pyrDown
        Mat down = new Mat();
        pyrDown(src, down);
        Mat downUp = new Mat();
        pyrUp(down, downUp);
        Mat laplacianPyr = subtract(src, downUp).asMat();
        ImageWindowUtil.imshow("laplacianPyr", laplacianPyr);
```

##### 轮廓检测方法

findContours(img, mode, method)

mode: 轮廓检索模式

RETR_EXTERNAL 

RETR_LIST

RETR_CCOMP

RETR_TREE 检索所有的轮廓，并重构嵌套轮廓的整个层次，平时就用这个就行

method: 轮廓逼近方法

CHAIN_APPROX_NONE 以freeman链码的方式输出罗阔，所有其他方法输出多边形（顶点的序列）

CHAIN_APPROX_SIMPLE 压缩水平的、垂直的和斜的部分，也就是，函数只保留他们的终点部分。

```java
 // 轮廓检测 find contours
        Mat gray = new Mat();
        cvtColor(src, gray, COLOR_BGR2GRAY);
        ImageWindowUtil.imshow("gray", gray);
        threshold(gray, gray, 127, 255, THRESH_BINARY);
        ImageWindowUtil.imshow("threshold", gray);

        MatVector contours = new MatVector();
        findContours(gray, contours, RETR_TREE, CHAIN_APPROX_NONE);

```

###### 绘制轮廓

```java

        // 绘制轮廓
        // clone一份，否则会改变原图
        Mat drawImage = src.clone();
        drawContours(drawImage, contours, -1, Scalar.RED);
//        drawContours(drawImage, contours, 0, Scalar.RED);
//        drawContours(drawImage, contours, -1, Scalar.RED, 2, LINE_8, null, Integer.MAX_VALUE, null);
//        for (int i = 0; i < contours.size(); i++) {
//            if (contourArea(contours.get(i)) > 1000) {
//                drawContours(drawImage, contours, i, Scalar.RED);
//            }
//        }
        ImageWindowUtil.imshow("drawContours", drawImage);
```

###### 轮廓特征

```java
// 轮廓特征
        // 轮廓个数
        System.out.println(contours.size());
        // 面积计算
        System.out.println(contourArea(contours.get(0)));
        System.out.println(contourArea(contours.get(1)));
        // 周长计算 true表示图形是闭合的
        System.out.println(arcLength(contours.get(1), true));
```

###### 轮廓近似

```java
// 轮廓检测 find contours
        Mat gray = new Mat();
        cvtColor(src, gray, COLOR_BGR2GRAY);
        ImageWindowUtil.imshow("gray", gray);
        threshold(gray, gray, 127, 255, THRESH_BINARY);
        ImageWindowUtil.imshow("threshold", gray);

        MatVector contours = new MatVector();
        findContours(gray, contours, RETR_TREE, CHAIN_APPROX_NONE);

        // 绘制轮廓
        // clone一份，否则会改变原图
        Mat drawImage = src.clone();
        drawContours(drawImage, contours, 0, Scalar.RED);
        ImageWindowUtil.imshow("drawContours", drawImage);

        // 轮廓近似
        double epsilon = 0.1 * arcLength(contours.get(0), true);
        Mat approx = new Mat();
        approxPolyDP(contours.get(0), approx, epsilon, true);
        ImageWindowUtil.imshow("approx", approx);
```



###### 边界矩形

```kava
// 边界矩形
        Rect r = boundingRect(contours.get(0));
        rectangle(src,
                new Point(r.x(), r.y()), new Point(r.x() + r.width(), r.y() + r.height()),
                Scalar.GREEN, 2, LINE_8, 0);
        ImageWindowUtil.imshow("rectangle", src);
```

###### 轮廓面积与边界矩形比

```java
 double area= contourArea(contours.get(0));
        Rect r = boundingRect(contours.get(0));
        double rectArea = r.width() * r.height();
        double extent = area/rectArea;
        System.out.println("轮廓面积与边界矩形比 "+ extent);
```

###### 外接圆

```java
// 外接圆
        Point2f center = new Point2f();
        FloatPointer radius = new FloatPointer();
        Mat points = contours.get(0);
// 这个minEnclosingCircle有毒啊，先放放
        minEnclosingCircle(points, center, radius);
        circle(src, new Point(center), (int) radius.get(), Scalar.GREEN);
        System.out.println("外接圆 " + src);
```

##### 模版匹配

平方不同

系数不同

```java
 // 匹配人脸
//        Mat src = imread("face.png", IMREAD_GRAYSCALE);
        Mat src = imread("face1.jpeg", IMREAD_GRAYSCALE);
        Mat templ = imread("faceTempl.png", IMREAD_GRAYSCALE);
        ImageWindowUtil.imshow("src", src);
        ImageWindowUtil.imshow("templ", templ);
        Mat res = new Mat();
        matchTemplate(src, templ, res, TM_SQDIFF_NORMED);
        ImageWindowUtil.imshow("Res", res);

        // 定位人脸
        DoublePointer minVal = new DoublePointer(),  maxVal = new DoublePointer();
        Point minLoc = new Point(), maxLoc = new Point();
        minMaxLoc(res, minVal, maxVal, minLoc, maxLoc, null);

        // 画出人脸框
        int x = minLoc.x(), y = minLoc.y(), w = templ.cols(), h = templ.rows();
        rectangle(src, minLoc, new Point(x + w, y + h), Scalar.WHITE);
        ImageWindowUtil.imshow("rectangle", src);

        // TODO: 匹配多个对象 添加一个threshold，和CCOEFF_NORMED返回的res比较，
        // 但是res在java里是Mat，要如何实现呢？
```

##### 直方图

灰度级

```java
Mat hist = new Mat();
        // todo: 有毒
        calcHist(src, 1, new IntPointer(0), null, hist, 2, new IntPointer(256),
                new PointerPointer(256), true, false);
        ImageWindowUtil.imshow("calcHist", hist);
```

###### 均衡化原理

```java
// 纹理，具体细节无法保留
equalizeHist(src, hist);	
```

###### 自适应直方图均衡化

```java
Mat hist = new Mat();
// 可以保留细节       
CLAHE clahe = createCLAHE(2.0, new Size(8, 8));
        clahe.apply(src, hist);
        ImageWindowUtil.imshow("clahe", hist);
```

##### 傅里叶变换 fourier

###### 作用

高频：变化剧烈的灰度分量，例如边界

低频：变化缓慢的灰度分量，例如一片大海

###### 滤波

低通滤波器：只保留低频，会使得图像模糊

高通滤波器：只保留高频，会使得图像细节增强



opencv中主要就是dft/idft，需要输入图像先转换成np.float32模式

得到的结果中频率为0的部分会在左上角，通常要转换到中心位置，可以通过shift变换来实现

dft返回的结果是双通道的（实部，虚部），通常还需要转换成图像格式才能展示（0,255）

```java
 Mat src = imread("face.png", IMREAD_GRAYSCALE);
//        Mat src = imread("face1.jpeg", IMREAD_GRAYSCALE);
//        Mat templ = imread("faceTempl.png", IMREAD_GRAYSCALE);
        ImageWindowUtil.imshow("src", src);
        cvtColor(src, src, CV_32FC1); 
Mat dst = new Mat();
        // 有毒
        dft(src, dst, DFT_COMPLEX_OUTPUT, 0);
        ImageWindowUtil.imshow("dst", dst);
```

