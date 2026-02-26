package tk.therealsuji.vtopchennai.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;

public class CaptchaHelper {

    private static final int HEIGHT = 40;
    private static final int WIDTH = 200;
    private static final String LABELS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final float[][] weights;
    private final float[] biases;

    private final float[] saturationBuffer = new float[WIDTH * HEIGHT];
    private final float[][] deflatBuffer = new float[HEIGHT][WIDTH];
    private final float[][][] blockBuffer = new float[6][][];
    private final float[] flattenBuffer;
    private final float[] logitsBuffer;
    private final float[] softmaxBuffer;

    public CaptchaHelper(Context context) {
        ModelData model = loadModel(context);
        this.weights = model.weights;
        this.biases = model.biases;

        int inputSize = weights.length;
        flattenBuffer = new float[inputSize];
        logitsBuffer = new float[biases.length];
        softmaxBuffer = new float[biases.length];
    }

    private ModelData loadModel(Context context) {
        try {
            InputStream is = context.getAssets().open("weights.json");
            InputStreamReader reader = new InputStreamReader(is);
            return new Gson().fromJson(reader, ModelData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load weights.json", e);
        }
    }

    private static class ModelData {
        float[][] weights;
        float[] biases;
    }

    private void softmax(float[] input, float[] output) {
        float sum = 0f;

        for (int i = 0; i < input.length; i++) {
            output[i] = (float) Math.exp(input[i]);
            sum += output[i];
        }

        for (int i = 0; i < output.length; i++) {
            output[i] /= sum;
        }
    }

    private void computeSaturation(Bitmap bitmap) {

        int[] pixels = new int[WIDTH * HEIGHT];
        bitmap.getPixels(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < pixels.length; i++) {

            int pixel = pixels[i];
            int r = Color.red(pixel);
            int g = Color.green(pixel);
            int b = Color.blue(pixel);

            int min = Math.min(r, Math.min(g, b));
            int max = Math.max(r, Math.max(g, b));

            if (max == 0) {
                saturationBuffer[i] = 0;
            } else {
                saturationBuffer[i] = ((max - min) * 255f) / max;
            }
        }
    }

    private void deflatten() {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                deflatBuffer[i][j] = saturationBuffer[i * WIDTH + j];
            }
        }
    }

    private void preprocess(float[][] block) {

        int h = block.length;
        int w = block[0].length;

        float avg = 0f;

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                avg += block[i][j];
            }
        }

        avg /= (h * w);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                block[i][j] = block[i][j] > avg ? 1f : 0f;
            }
        }
    }

    public String solve(Bitmap bitmap) {

        computeSaturation(bitmap);
        deflatten();

        StringBuilder output = new StringBuilder();

        for (int a = 0; a < 6; a++) {

            int x1 = (a + 1) * 25 + 2;
            int y1 = 7 + 5 * (a % 2) + 1;
            int x2 = (a + 2) * 25 + 1;
            int y2 = 35 - 5 * ((a + 1) % 2);

            int h = y2 - y1;
            int w = x2 - x1;

            float[][] block = new float[h][w];

            for (int i = y1; i < y2; i++) {
                for (int j = x1; j < x2; j++) {
                    block[i - y1][j - x1] = deflatBuffer[i][j];
                }
            }

            preprocess(block);

            int index = 0;
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    flattenBuffer[index++] = block[i][j];
                }
            }

            for (int j = 0; j < biases.length; j++) {
                float sum = biases[j];
                for (int i = 0; i < flattenBuffer.length; i++) {
                    sum += flattenBuffer[i] * weights[i][j];
                }
                logitsBuffer[j] = sum;
            }

            softmax(logitsBuffer, softmaxBuffer);

            int maxIndex = 0;
            for (int i = 1; i < softmaxBuffer.length; i++) {
                if (softmaxBuffer[i] > softmaxBuffer[maxIndex]) {
                    maxIndex = i;
                }
            }

            output.append(LABELS.charAt(maxIndex));
        }

        return output.toString();
    }
}