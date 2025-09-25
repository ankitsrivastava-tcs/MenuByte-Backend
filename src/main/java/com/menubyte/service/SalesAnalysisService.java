package com.menubyte.service;


import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SalesAnalysisService {

    // Hyperparameters for the neural network
    private static final int LSTM_LAYER_SIZE = 10;
    private static final double LEARNING_RATE = 0.01;
    private static final int EPOCHS = 50;

    /**
     * Trains a simple LSTM neural network model on historical sales data to forecast future sales.
     * This method demonstrates a basic time-series forecasting approach.
     *
     * @param dailySales A map of daily sales, where the key is the date string and the value is the sales amount.
     * @return A trained MultiLayerNetwork model.
     */
    private MultiLayerNetwork trainModel(Map<String, Double> dailySales) {
        // Step 1: Prepare the data for the neural network.
        // We'll convert the Map of sales into an INDArray (ND4J's data structure).
        List<Double> salesList = new ArrayList<>(dailySales.values());
        if (salesList.size() < 2) {
            // Not enough data to train a time series model
            throw new IllegalArgumentException("Not enough daily sales data for training. At least 2 data points are required.");
        }

        // We'll use the sales data to predict the next day's sales.
        // The features (X) will be sales from days 1 to N-1.
        // The labels (Y) will be sales from days 2 to N.
        int dataPoints = salesList.size();
        INDArray features = Nd4j.create(1, 1, dataPoints - 1);
        INDArray labels = Nd4j.create(1, 1, dataPoints - 1);

        for (int i = 0; i < dataPoints - 1; i++) {
            features.putScalar(new int[]{0, 0, i}, salesList.get(i));
            labels.putScalar(new int[]{0, 0, i}, salesList.get(i + 1));
        }

        // Step 2: Configure the Neural Network
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(LEARNING_RATE))
                .weightInit(WeightInit.XAVIER)
                .list()
                // A single LSTM layer for learning sequential dependencies in the sales data
                .layer(new LSTM.Builder()
                        .nIn(1)
                        .nOut(LSTM_LAYER_SIZE)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.SIGMOID)
                        .build())
                // An output layer to produce a single forecasted value
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(LSTM_LAYER_SIZE)
                        .nOut(1)
                        .activation(Activation.IDENTITY)
                        .build())
                .build();

        // Step 3: Create and train the model
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10)); // Print loss every 10 iterations

        for (int i = 0; i < EPOCHS; i++) {
            model.fit(features, labels);
        }

        return model;
    }

    /**
     * Predicts the next day's sales using a trained model.
     *
     * @param model The trained MultiLayerNetwork model.
     * @param lastSale The sales amount from the last known day.
     * @return The forecasted sales amount.
     */
    private double predictNextDaySales(MultiLayerNetwork model, double lastSale) {
        // Create an input array with the last known sales value
        INDArray input = Nd4j.create(1, 1, 1);
        input.putScalar(new int[]{0, 0, 0}, lastSale);

        // Make the prediction
        INDArray output = model.rnnTimeStep(input);

        // Extract the predicted value
        return output.getDouble(0);
    }

    /**
     * Public method to perform a sales forecast.
     *
     * @param dailySales The historical daily sales data.
     * @return The forecasted sales for the next day.
     */
    public double getNextDaySalesForecast(Map<String, Double> dailySales) {
        // Sort the map keys (dates) to get the most recent sale
        List<String> sortedDates = new ArrayList<>(dailySales.keySet());
        sortedDates.sort(String::compareTo);

        if (sortedDates.isEmpty()) {
            return 0.0; // Return 0 if there is no sales data
        }

        // Train the model
        MultiLayerNetwork model = trainModel(dailySales);

        // Get the last known sales value
        String lastDate = sortedDates.get(sortedDates.size() - 1);
        double lastSale = dailySales.get(lastDate);

        // Make a prediction
        double forecast = predictNextDaySales(model, lastSale);
        System.out.println("Sales Forecast for next day: " + forecast); // Log the prediction
        return forecast;
    }
}
