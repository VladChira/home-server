package com.anonymous.voiceclient;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;


class ONNXModelRunner {

    private static final int BATCH_SIZE = 1; // Replace with your batch size

    AssetManager assetManager;
    OrtSession hey_jarvis_session;
    OrtEnvironment hey_jarvis_env = OrtEnvironment.getEnvironment();

    public ONNXModelRunner(AssetManager assetManager) throws OrtException {
        this.assetManager = assetManager;

        try {
            hey_jarvis_session = hey_jarvis_env.createSession(readModelFile(assetManager, "hey_jarvis.onnx"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public float[][] get_mel_spectrogram(float[] inputArray) throws OrtException, IOException {
        OrtSession session;
        try (InputStream modelInputStream = assetManager.open("melspectrogram.onnx")) {
            byte[] modelBytes = new byte[modelInputStream.available()];
            modelInputStream.read(modelBytes);
            session = OrtEnvironment.getEnvironment().createSession(modelBytes);
        }
        float[][] outputArray = null;
        int SAMPLES = inputArray.length;
        // Convert the input array to ONNX Tensor
        FloatBuffer floatBuffer = FloatBuffer.wrap(inputArray);
        OnnxTensor inputTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), floatBuffer, new long[]{BATCH_SIZE, SAMPLES});

        // Run the model
        // Adjust this based on the actual expected output shape
        try (OrtSession.Result results = session.run(Collections.singletonMap(session.getInputNames().iterator().next(), inputTensor))) {

            float[][][][] outputTensor = (float[][][][]) results.get(0).getValue();
            // Here you need to cast the output appropriately
//            Object outputObject = outputTensor.getValue();

            // Check the actual type of 'outputObject' and cast accordingly
            // The following is an assumed cast based on your error message

            float[][] squeezed = squeeze(outputTensor);
            outputArray = applyMelSpecTransform(squeezed);


        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            inputTensor.close();
            session.close();
        }
        OrtEnvironment.getEnvironment().close();
        return outputArray;
    }

    public static float[][] squeeze(float[][][][] originalArray) {
        float[][] squeezedArray = new float[originalArray[0][0].length][originalArray[0][0][0].length];
        for (int i = 0; i < originalArray[0][0].length; i++) {
            for (int j = 0; j < originalArray[0][0][0].length; j++) {
                squeezedArray[i][j] = originalArray[0][0][i][j];
            }
        }

        return squeezedArray;
    }

    public static float[][] applyMelSpecTransform(float[][] array) {
        float[][] transformedArray = new float[array.length][array[0].length];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                transformedArray[i][j] = array[i][j] / 10.0f + 2.0f;
            }
        }

        return transformedArray;
    }

    public float[][] generateEmbeddings(float[][][][] input) throws OrtException, IOException {
        OrtEnvironment env = OrtEnvironment.getEnvironment();
        InputStream is = assetManager.open("embedding_model.onnx");
        byte[] model = new byte[is.available()];
        is.read(model);
        is.close();

        try (OrtSession sess = env.createSession(model); OnnxTensor inputTensor = OnnxTensor.createTensor(env, input); OrtSession.Result results = sess.run(Collections.singletonMap("input_1", inputTensor))) {
            // Extract the output tensor
            float[][][][] rawOutput = (float[][][][]) results.get(0).getValue();

            // Assuming the output shape is (41, 1, 1, 96), and we want to reshape it to (41, 96)
            float[][] reshapedOutput = new float[rawOutput.length][rawOutput[0][0][0].length];
            for (int i = 0; i < rawOutput.length; i++) {
                System.arraycopy(rawOutput[i][0][0], 0, reshapedOutput[i], 0, rawOutput[i][0][0].length);
            }
            return reshapedOutput;
        } catch (Exception e) {
            Log.d("exception", "not_predicted " + e.getMessage());
        }
        env.close();
        return null;
    }

    @SuppressLint("DefaultLocale")
    public String predictWakeWord(float[][][] inputArray) throws OrtException {
        float[][] result = new float[0][];
        String resultant = "";


        OnnxTensor inputTensor = null;

        try {
            // Create a tensor from the input array
            inputTensor = OnnxTensor.createTensor(hey_jarvis_env, inputArray);
            // Run the inference
            OrtSession.Result outputs = hey_jarvis_session.run(Collections.singletonMap(hey_jarvis_session.getInputNames().iterator().next(), inputTensor));
            // Extract the output tensor, convert it to the desired type
            result = (float[][]) outputs.get(0).getValue();
            resultant = String.format("%.5f", (double) result[0][0]);

        } catch (OrtException e) {
            e.printStackTrace();
        } finally {
            if (inputTensor != null) inputTensor.close();
            // Add this to ensure the session is properly closed.
        }
        return resultant;
    }

    private byte[] readModelFile(AssetManager assetManager, String filename) throws IOException {
        try (InputStream is = assetManager.open(filename)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return buffer;
        }
    }

}


