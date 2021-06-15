# JavaCV Samples

[javacv](https://github.com/bytedeco/javacv)

## Python转Java 代码练习 视频 1-35

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y)

采坑笔记：[Code Cheat Sheet](CodeCheatSheet.md)

代码: 都在code cheat sheet里，写个main方法，传入src image就可以运行

状态：98%完成，有两个函数出错，先放放

## OCR Bank Card Number 视频 36-40

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p36)

采坑笔记：[OCR Bank Card Number](ocr/bankcard/OCRBackCardNumber.md)

状态：100%成功

## OCR Document Scan 视频 41-46

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=41)

采坑笔记：[OCR Document Scan](ocr/document/OCRDocumentScan.md)

代码: ocr/document

状态：40%，因两个老师自己实现的算法，未能完成代码转换

## 角点检测 视频 47-51

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=47)

采坑笔记：无

代码: 无

状态：10%，偏理论讲解，对harris简介



## 尺度空间 视频 52-57

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=52)

采坑笔记：无

代码: 无

状态：10%，偏理论讲解，对SIFT简介

## 特征匹配 视频 58-61

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=58)

采坑笔记：无

代码: 无

状态：10%，偏理论讲解，RANSAC算法，图像拼接



## 实战：停车场车位识别 视频 62-69

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=62)

采坑笔记：[park](park/Park.md)

代码: park

状态：70%, 结果不精确，可能是因为截图和老师的原图不一样，要调数值。最后两集 训练和检测视频中的车位暂时没有实现



## 实战：答题卡自动识别 视频 70-73

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=70)

采坑笔记：无

代码: 无

状态：

## 背景建模 视频 74-77

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=74)

采坑笔记：无

代码: 无

状态：

## 光流估计 视频 78-81

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=78)

采坑笔记：无

代码: 无

状态：

## DNN 深度神经网络(Deep Neural Networks) 视频 82-83

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=82)

采坑笔记：无

代码: 无

状态：

## 目标追踪 视频 84-89

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=84)

采坑笔记：无

代码: 无

状态：

## 卷积 convolve 视频 90-91

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=90)

采坑笔记：无

代码: 无

状态：

## 关键点定位 视频 92-96

参照课程：[OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y?p=92)

采坑笔记：无

代码: 无

状态：



# 自我实践1

## 土地规划图转SVG

参照课程：OpenCV3计算机视觉 Python语言实现（第二版）+ [OpenCV从入门到实战Python版本](https://www.bilibili.com/video/BV1PV411774y)

采坑笔记：[土地规划图转SVG实现过程](map/Map.md)

代码: map/MapProcessor

状态: 100%，虽然导出的svg和原图对比还是有很多瑕疵，但是相当于维护阶段，修改bug了，另外OCR识别中文还没搞出来，但是还是给自己100分，6/9-6/14 零基础入门用javacv写出来了。

**建议**：直接使用python或者C++来实现，1是资料多，2是官方api全 java在这两方面都很欠缺。3 java对基础数学的数据结构表达没有py的直接，而操作最多的就是数组矩阵，python基本两行就搞定的事情，java要洋洋洒洒写一大堆。



# 自我实践2

## 土地规划图绘制分区

参照课程：土地规划图转SVG

采坑笔记：[土地规划图绘制分区实现过程](partition/partition.md)

代码: partition

状态: 10%， 按照xmind整理出来的信息显示，应该是一个二维矩阵，类似相邻岛屿的问题，找出需要连接在一起的区块。

