import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
public class Camera extends Task{
  static UsbCamera camera;
  static double resizeImageWidth = 320.0;
  static double resizeImageHeight = 240.0;
  
  CvSink imageSink = new CvSink("CV Image Grabber");
  CvSource imageSource = new CvSource("CV Image Source", VideoMode.PixelFormat.kMJPEG, 640, 480, 30);
    MjpegServer cvStream = new MjpegServer("CV Image Stream", 1186);
  MatOfKeyPoint blobsMat = new MatOfKeyPoint();
    Mat image = new Mat();
    double[] hsvThresholdHue = { 24.280575539568343, 61.740614334471 };
    double[] hsvThresholdSaturation = { 50.44964028776978, 120.10238907849829 };
    double[] hsvThresholdValue = { 123.83093525179855, 255.0 };
    Mat resizedImage = new Mat();
    Mat blurredImage = new Mat();
    Mat HSV_Threshold = new Mat();
    Mat erode = new Mat();
    Mat dilate = new Mat();
    Mat empty = new Mat();
    Scalar Bordervalue = new Scalar(-1);
    double erodeAndDilateIterations = 8.0;
    org.opencv.core.Point anchor = new org.opencv.core.Point(-1, -1);

    // resize variables

    int resizeImageInterpolation = Imgproc.INTER_CUBIC;

    // blur variables
    double cvMedianblurKsize = 7.0;

    // Blob Variables
    double findBlobsMinArea = 1.0;
    double[] findBlobsCircularity = { 0.0, 1.0 };
    boolean findBlobsDarkBlobs = false;
    int streamPort = 1185;
    MjpegServer inputStream = new MjpegServer("MJPEG Server", streamPort);
    public void inititalize(){
    camera = setUsbCamera(0, inputStream);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        if (camera != null) {
          camera.free();
        }
      }
    });
    // Loads our OpenCV library. This MUST be included
    System.loadLibrary("opencv_java310");

    String PI_ADDRESS = "10.66.44.41";
    int PORT = 1185;
    NetworkTable.setClientMode();
    NetworkTable.setTeam(6644);
    NetworkTable.initialize();
    NetworkTable.getTable("CameraPublisher").getSubTable("T. J. Eckleburg").putStringArray("streams",
        new String[] { "mjpeg:http://" + PI_ADDRESS + ":" + PORT + "/stream.mjpg" });
    int resWidth = 640;
    int resHeight = 360;
    camera.setResolution(resWidth, resHeight);
    camera.setFPS(7);

    imageSink.setSource(camera);

    // This creates a CvSource to use. This will take in a Mat image that has had
    // OpenCV operations
    // operations

    
    cvStream.setSource(imageSource);
    }
    
    public void execute(){
      long frameTime = imageSink.grabFrame(image);
      if (frameTime == 0)
        return;
      resizeImage(image, resizeImageWidth, resizeImageHeight, resizeImageInterpolation, resizedImage);

      // Step CV_medianBlur:
      cvMedianblur(resizedImage, cvMedianblurKsize, blurredImage);

      // Step HSL_Threshold:
      hsvThreshold(blurredImage, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue, HSV_Threshold);

      // Step CV_erode:
      cvErode(HSV_Threshold, empty, anchor, erodeAndDilateIterations, Core.BORDER_CONSTANT, Bordervalue, erode);

      // Step CV_dilate:
      cvDilate(erode, empty, anchor, erodeAndDilateIterations, Core.BORDER_CONSTANT, Bordervalue, dilate);

      // Step Find_Blobs:
      findBlobs(HSV_Threshold, findBlobsMinArea, findBlobsCircularity, findBlobsDarkBlobs, blobsMat);

      imageSource.putFrame(HSV_Threshold);

      NetworkTable.getTable("SmartDashboard").putString("Yellow Ball", "" + blob(blobsMat)[0]);
    }



  public static String center(MatOfKeyPoint blobs) {
    double deadzone = 50.0;// This is the width of the area in the center that counts as "centered"
    if (blob(blobs)[0] != -1) {
      if (blob(blobs)[0] < (resizeImageWidth - deadzone) / 2) {
        return "left";
      } else if (blob(blobs)[0] > (resizeImageWidth - deadzone) / 2) {
        return "right";
      } else {
        return "centered";
      }
    }
    return "no blob";
  }

  public static int[] blob(MatOfKeyPoint blobs) {
    List<KeyPoint> list = blobs.toList();
    int largest = 0;// index of largest blob
    if (!list.isEmpty()) {
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i).size > list.get(largest).size) {
          largest = i;
        }
      }
      return new int[] { (int) list.get(largest).pt.x, (int) list.get(largest).pt.y };
    }
    return new int[] { -1, -1 };// No point found
  }

  /**
   * Scales and image to an exact size.
   * 
   * @param input         The image on which to perform the Resize.
   * @param width         The width of the output in pixels.
   * @param height        The height of the output in pixels.
   * @param interpolation The type of interpolation.
   * @param output        The image in which to store the output.
   */
  private static void resizeImage(Mat input, double width, double height, int interpolation, Mat output) {
    Imgproc.resize(input, output, new Size(width, height), 0.0, 0.0, interpolation);
  }

  /**
   * Performs a median blur on the image.
   * 
   * @param src   image to blur.
   * @param kSize size of blur.
   * @param dst   output of blur.
   */
  private static void cvMedianblur(Mat src, double kSize, Mat dst) {
    Imgproc.medianBlur(src, dst, (int) kSize);
  }

  /**
   * Segment an image based on hue, saturation, and value ranges.
   *
   * @param input  The image on which to perform the HSV threshold.
   * @param hue    The min and max hue
   * @param sat    The min and max saturation
   * @param lum    The min and max value
   * @param output The image in which to store the output.
   */
  private static void hsvThreshold(Mat input, double[] hue, double[] sat, double[] val, Mat out) {
    Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HSV);
    Core.inRange(out, new Scalar(hue[0], sat[0], val[0]), new Scalar(hue[1], sat[1], val[1]), out);
  }

  /**
   * Expands area of lower value in an image.
   * 
   * @param src         the Image to erode.
   * @param kernel      the kernel for erosion.
   * @param anchor      the center of the kernel.
   * @param iterations  the number of times to perform the erosion.
   * @param borderType  pixel extrapolation method.
   * @param borderValue value to be used for a constant border.
   * @param dst         Output Image.
   */
  private static void cvErode(Mat src, Mat kernel, org.opencv.core.Point anchor, double iterations, int borderType,
      Scalar borderValue, Mat dst) {
    if (kernel == null) {
      kernel = new Mat();
    }
    if (anchor == null) {
      anchor = new Point(-1, -1);
    }
    if (borderValue == null) {
      borderValue = new Scalar(-1);
    }
    Imgproc.erode(src, dst, kernel, anchor, (int) iterations, borderType, borderValue);
  }

  /**
   * Expands area of higher value in an image.
   * 
   * @param src         the Image to dilate.
   * @param kernel      the kernel for dilation.
   * @param anchor      the center of the kernel.
   * @param iterations  the number of times to perform the dilation.
   * @param borderType  pixel extrapolation method.
   * @param borderValue value to be used for a constant border.
   * @param dst         Output Image.
   */
  private static void cvDilate(Mat src, Mat kernel, Point anchor, double iterations, int borderType, Scalar borderValue,
      Mat dst) {
    if (kernel == null) {
      kernel = new Mat();
    }
    if (anchor == null) {
      anchor = new Point(-1, -1);
    }
    if (borderValue == null) {
      borderValue = new Scalar(-1);
    }
    Imgproc.dilate(src, dst, kernel, anchor, (int) iterations, borderType, borderValue);
  }

  /**
   * Filter out an area of an image using a binary mask.
   * 
   * @param input  The image on which the mask filters.
   * @param mask   The binary image that is used to filter.
   * @param output The image in which to store the output.
   */

  /**
   * Detects groups of pixels in an image.
   * 
   * @param input       The image on which to perform the find blobs.
   * @param minArea     The minimum size of a blob that will be found
   * @param circularity The minimum and maximum circularity of blobs that will be
   *                    found
   * @param darkBlobs   The boolean that determines if light or dark blobs are
   *                    found.
   * @param blobList    The output where the MatOfKeyPoint is stored.
   */
  private static void findBlobs(Mat input, double minArea, double[] circularity, Boolean darkBlobs,
      MatOfKeyPoint blobList) {

    FeatureDetector blobDet = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
    try {
      File tempFile = File.createTempFile("config", ".xml");

      StringBuilder config = new StringBuilder();

      config.append("<?xml version=\"1.0\"?>\n");
      config.append("<opencv_storage>\n");
      config.append("<thresholdStep>10.</thresholdStep>\n");
      config.append("<minThreshold>50.</minThreshold>\n");
      config.append("<maxThreshold>220.</maxThreshold>\n");
      config.append("<minRepeatability>2</minRepeatability>\n");
      config.append("<minDistBetweenBlobs>10.</minDistBetweenBlobs>\n");
      config.append("<filterByColor>1</filterByColor>\n");
      config.append("<blobColor>");
      config.append((darkBlobs ? 0 : 255));
      config.append("</blobColor>\n");
      config.append("<filterByArea>1</filterByArea>\n");
      config.append("<minArea>");
      config.append(minArea);
      config.append("</minArea>\n");
      config.append("<maxArea>");
      config.append(Integer.MAX_VALUE);
      config.append("</maxArea>\n");
      config.append("<filterByCircularity>1</filterByCircularity>\n");
      config.append("<minCircularity>");
      config.append(circularity[0]);
      config.append("</minCircularity>\n");
      config.append("<maxCircularity>");
      config.append(circularity[1]);
      config.append("</maxCircularity>\n");
      config.append("<filterByInertia>1</filterByInertia>\n");
      config.append("<minInertiaRatio>0.1</minInertiaRatio>\n");
      config.append("<maxInertiaRatio>" + Integer.MAX_VALUE + "</maxInertiaRatio>\n");
      config.append("<filterByConvexity>1</filterByConvexity>\n");
      config.append("<minConvexity>0.95</minConvexity>\n");
      config.append("<maxConvexity>" + Integer.MAX_VALUE + "</maxConvexity>\n");
      config.append("</opencv_storage>\n");
      FileWriter writer;
      writer = new FileWriter(tempFile, false);
      writer.write(config.toString());
      writer.close();
      blobDet.read(tempFile.getPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
    blobDet.detect(input, blobList);
  }

  private static UsbCamera setUsbCamera(int cameraId, MjpegServer server) {
    UsbCamera camera = new UsbCamera("CoprocessorCamera", cameraId);
    server.setSource(camera);
    return camera;
  }
}