import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

// Cmd to run all three
// & 'C:\Program Files\Java\jdk-26.0.1\bin\java.exe' '--enable-preview' '-XX:+ShowCodeDetailsInExceptionMessages' '-cp' 'C:\Code\Projects\Java\Image Converter\bin' 'Main' gs 'Test/Test-2.jpg' 'Test/Out-1.jpg' && & 'C:\Program Files\Java\jdk-26.0.1\bin\java.exe' '--enable-preview' '-XX:+ShowCodeDetailsInExceptionMessages' '-cp' 'C:\Code\Projects\Java\Image Converter\bin' 'Main' avg 'Test/Test-2.jpg' 'Test/Out-2.jpg' && & 'C:\Program Files\Java\jdk-26.0.1\bin\java.exe' '--enable-preview' '-XX:+ShowCodeDetailsInExceptionMessages' '-cp' 'C:\Code\Projects\Java\Image Converter\bin' 'Main' bi 'Test/Test-2.jpg' 'Test/Out-3.jpg'

class Main {
    public static void main(String[] args) {
        // args => imgPath outputPath type
        final List<String> types = List.of("gs", "avg", "bi");

        if (!types.contains(args[0])) {
            System.out.println("Invalid type. Choose from: gs, avg, bi");
            return;
        }

        try {
            final String pathIn = Path.of(args[1]).toAbsolutePath().toString();
            final String pathOut = Path.of(args[2]).toAbsolutePath().toString();
            final String fileName = Paths
                    .get(pathOut, pathIn.substring(pathIn.lastIndexOf("\\"), pathIn.lastIndexOf(".")) + "-out.png")
                    .toString();

            File fileIn = new File(pathIn);
            BufferedImage image = ImageIO.read(fileIn);

            final int height = image.getHeight();
            final int width = image.getWidth();
            final int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
            final List<Integer> bwPixels = new ArrayList<>();

            for (int pixel : pixels) {
                int[] ARGB = getARGB(pixel);
                switch (args[0]) {
                    case "gs":
                        bwPixels.add(grayscale(ARGB));
                        break;
                    case "avg":
                        bwPixels.add(averageToBW(ARGB));
                        break;
                    case "bi":
                        bwPixels.add(binarization(ARGB));
                        break;
                    default:
                        System.out.println("Invalid type. Choose from: gs, avg, bi");
                        return;
                }
            }

            // System.out.println(bwPixels);
            BufferedImage bwImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            bwImage.setRGB(0, 0, width, height, bwPixels.stream().mapToInt(Integer::intValue).toArray(), 0, width);
            ImageIO.write(bwImage, "png", new File(fileName));

            System.out.println("Width: " + width + " Height: " + height);
        } catch (InvalidPathException e) {
            System.out.println("Arguments must be in-format: <type> <path of input image> <path of output image>");
        } catch (IOException e) {
            System.out.println("Error reading/writing file. Please check the file paths.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            System.out.println("Program finished.");
        }
    }

    static int grayscale(int[] argb) {
        int pixel = calculateGrayValue(argb);
        return toPixel(pixel, argb[0]);
    }

    static int calculateGrayValue(int[] argb) {
        return Math.round(0.2126f * argb[1] + 0.7152f * argb[2] + 0.0722f * argb[3]);
    }

    static int averageToBW(int[] argb) {
        int pixel = Math.round(argb[1] + argb[2] + argb[3]) / 3;
        return toPixel(pixel, argb[0]);
    }

    static int binarization(int[] argb) {
        int pixel = calculateGrayValue(argb) > 127 ? 255 : 0;
        return toPixel(pixel, argb[0]);
    }

    static int[] getARGB(int pixel) {
        int alpha = (pixel >> 24) & 0xFF;
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return new int[] { alpha, red, green, blue };
    }

    static int toPixel(int pixel, int alpha) {
        return alpha << 24 | pixel << 16 | pixel << 8 | pixel;
    }
}