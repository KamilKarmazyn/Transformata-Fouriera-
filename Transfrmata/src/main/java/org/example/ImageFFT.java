package org.example;

import org.apache.commons.math3.complex.Complex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageFFT {
    public static void main(String[] args) {
        String imagePath = "C:\\Users\\kamil\\Documents\\GitHub\\Praca_Magisterska\\Transfrmata\\przyklad.jpg";

        try {
            BufferedImage image = ImageIO.read(new File(imagePath));

            int width = image.getWidth();
            int height = image.getHeight();

            int newWidth = nextPowerOfTwo(width);
            int newHeight = nextPowerOfTwo(height);

            int[][] pixelMatrix = new int[newHeight][newWidth];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    int grayValue = (pixel >> 16) & 0xFF;
                    pixelMatrix[y][x] = grayValue;
                }
            }

            Complex[][] spectrum = performFFT(pixelMatrix);

            Complex[][] invertedSpectrum = performIFFT(spectrum);

            int[][] invertedPixels = new int[newHeight][newWidth];
            for (int y = 0; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    if (y < height && x < width) {
                        invertedPixels[y][x] = (int) invertedSpectrum[y][x].getReal();
                    } else {
                        invertedPixels[y][x] = 0; // Wypełnienie zerami dla dodatkowych wierszy/kolumn
                    }
                }
            }

            BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int grayValue = invertedPixels[y][x];
                    int pixel = (grayValue << 16) | (grayValue << 8) | grayValue;
                    processedImage.setRGB(x, y, pixel);
                }
            }

            String outputImagePath = "C:\\Users\\kamil\\Documents\\GitHub\\Praca_Magisterska\\Transfrmata\\przykladv2.jpg";
            ImageIO.write(processedImage, "jpg", new File(outputImagePath));

            System.out.println("Przetwarzanie zakończone. Zapisano przetworzony obraz do pliku.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Complex[][] performFFT(int[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;

        Complex[][] spectrum = new Complex[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spectrum[y][x] = new Complex(pixels[y][x], 0);
            }
        }

        for (int y = 0; y < height; y++) {
            spectrum[y] = FFT(spectrum[y]);
        }

        Complex[][] transposedSpectrum = new Complex[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                transposedSpectrum[x][y] = spectrum[y][x];
            }
        }

        Complex[][] result = new Complex[width][height];
        for (int x = 0; x < width; x++) {
            result[x] = FFT(transposedSpectrum[x]);
        }

        return result;
    }

    private static Complex[][] performIFFT(Complex[][] spectrum) {
        int height = spectrum.length;
        int width = spectrum[0].length;

        Complex[][] invertedSpectrum = new Complex[height][width];
        for (int y = 0; y < height; y++) {
            invertedSpectrum[y] = IFFT(spectrum[y]);
        }

        Complex[][] transposedInvertedSpectrum = new Complex[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                transposedInvertedSpectrum[x][y] = invertedSpectrum[y][x];
            }
        }

        Complex[][] result = new Complex[height][width];
        for (int x = 0; x < width; x++) {
            result[x] = IFFT(transposedInvertedSpectrum[x]);
        }

        return result;
    }

    private static Complex[] FFT(Complex[] x) {
        int N = x.length;

        if (N == 1) {
            return new Complex[] { x[0] };
        }

        if (N % 2 != 0) {
            throw new IllegalArgumentException("Rozmiar tablicy musi być potęgą liczby 2.");
        }

        Complex[] even = new Complex[N / 2];
        Complex[] odd = new Complex[N / 2];

        for (int k = 0; k < N / 2; k++) {
            even[k] = x[2 * k];
            odd[k] = x[2 * k + 1];
        }

        Complex[] evenTransform = FFT(even);
        Complex[] oddTransform = FFT(odd);

        Complex[] y = new Complex[N];
        for (int k = 0; k < N / 2; k++) {
            double angle = -2 * k * Math.PI / N;
            Complex complex = new Complex(Math.cos(angle), Math.sin(angle));
            y[k] = evenTransform[k].add(complex.multiply(oddTransform[k]));
            y[k + N / 2] = evenTransform[k].subtract(complex.multiply(oddTransform[k]));
        }

        return y;
    }

    private static Complex[] IFFT(Complex[] x) {
        int N = x.length;

        if (N == 1) {
            return new Complex[] { x[0] };
        }

        if (N % 2 != 0) {
            throw new IllegalArgumentException("Rozmiar tablicy musi być potęgą liczby 2.");
        }

        Complex[] even = new Complex[N / 2];
        Complex[] odd = new Complex[N / 2];

        for (int k = 0; k < N / 2; k++) {
            even[k] = x[2 * k];
            odd[k] = x[2 * k + 1];
        }

        Complex[] evenTransform = IFFT(even);
        Complex[] oddTransform = IFFT(odd);

        Complex[] y = new Complex[N];
        for (int k = 0; k < N / 2; k++) {
            double angle = 2 * k * Math.PI / N;
            Complex complex = new Complex(Math.cos(angle), Math.sin(angle));
            y[k] = evenTransform[k].add(complex.multiply(oddTransform[k]));
            y[k + N / 2] = evenTransform[k].subtract(complex.multiply(oddTransform[k]));
        }

        return y;
    }

    private static int nextPowerOfTwo(int n) {
        return (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
    }
}
