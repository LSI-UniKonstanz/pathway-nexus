package org.vanted.addons.matrix.ui;

import org.vanted.addons.matrix.clustering.TimeSeriesClustering;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * A small dialog to set the parameters for clustering and then start the clustering by clicking OK.
 */
public class ClusteringWindow extends JFrame {
    TimeSeriesClustering tsClustering;
    final String[] clusteringAlgorithms = {"Hierarchical", "Fuzzy C-Means"};
    final String[] distanceMetrics = {"Probability-Based",
            "Crisp Cross Correlation",
            "Fuzzy Cross Correlation",
            "Euclidean",
            "Mikowski",
            "Short Time Series",
            "Dynamic Time Warp"};

    JLabel clusteringLabel;
    JComboBox<String> clusteringAlgorithmSelection;
    JLabel numClusterLabel;
    JFormattedTextField numClusterTextField;
    JLabel thresholdLabel;
    JFormattedTextField thresholdTextField;
    JLabel fuzzinessLabel;
    JFormattedTextField fuzzinessTextField;
    JLabel distanceLabel;
    JComboBox<String> distanceMetricSelection;
    JLabel exponentLabel;
    JFormattedTextField exponentTextField;
    JLabel betaLabel;
    JFormattedTextField betaTextField;

    public ClusteringWindow(TimeSeriesClustering tsClustering) {
        super();
        this.tsClustering = tsClustering;
        setTitle("Clustering Setup");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        clusteringLabel = new JLabel("Clustering Algorithm:");
        clusteringAlgorithmSelection = new JComboBox<>(clusteringAlgorithms);
        clusteringAlgorithmSelection.setLightWeightPopupEnabled(false);

        numClusterLabel = new JLabel("Number of Clusters:");
        numClusterTextField = new JFormattedTextField(DecimalFormat.getIntegerInstance());
        numClusterTextField.setValue(10);

        thresholdLabel = new JLabel("Threshold:");
        thresholdLabel.setVisible(false);
        thresholdTextField = new JFormattedTextField(DecimalFormat.getInstance());
        thresholdTextField.setValue(0.5);
        thresholdTextField.setVisible(false);

        fuzzinessLabel = new JLabel("Fuzziness:");
        fuzzinessLabel.setVisible(false);
        fuzzinessTextField = new JFormattedTextField(DecimalFormat.getInstance());
        fuzzinessTextField.setValue(2);
        fuzzinessTextField.setVisible(false);

        distanceLabel = new JLabel("Distance Metric:");
        distanceMetricSelection = new JComboBox<>(distanceMetrics);
        distanceMetricSelection.setLightWeightPopupEnabled(false);

        exponentLabel = new JLabel("Exponent:");
        exponentTextField = new JFormattedTextField(DecimalFormat.getInstance());
        exponentTextField.setValue(2);
        exponentLabel.setVisible(false);
        exponentTextField.setVisible(false);

        betaLabel = new JLabel("Beta:");
        betaTextField = new JFormattedTextField(DecimalFormat.getInstance());
        betaTextField.setValue(2);
        betaLabel.setVisible(false);
        betaTextField.setVisible(false);

        clusteringAlgorithmSelection.addActionListener(e -> {
            if (e.getSource() == clusteringAlgorithmSelection) {
                String selectedTime = (String)clusteringAlgorithmSelection.getSelectedItem();
                if (selectedTime != null) {
                    switch (selectedTime) {
                        case "Hierarchical":
                            fuzzinessLabel.setVisible(false);
                            fuzzinessTextField.setVisible(false);
                            break;
                        case "Fuzzy C-Means":
                            fuzzinessLabel.setVisible(true);
                            fuzzinessTextField.setVisible(true);
                            break;
                    }
                }
            }
        });

        distanceMetricSelection.addActionListener(e -> {
            if (e.getSource() == distanceMetricSelection) {
                String selectedTime = (String)distanceMetricSelection.getSelectedItem();
                if (selectedTime != null) {
                    switch (selectedTime) {
                        case "Mikowski":
                            exponentLabel.setVisible(true);
                            exponentTextField.setVisible(true);
                            betaLabel.setVisible(false);
                            betaTextField.setVisible(false);
                            break;
                        case "Euclidean":
                            exponentLabel.setVisible(false);
                            exponentTextField.setVisible(false);
                            betaLabel.setVisible(false);
                            betaTextField.setVisible(false);
                            exponentTextField.setValue(2);
                            break;
                        case "Fuzzy Cross Correlation":
                            exponentLabel.setVisible(false);
                            exponentTextField.setVisible(false);
                            betaLabel.setVisible(true);
                            betaTextField.setVisible(true);
                            break;
                        default:
                            exponentLabel.setVisible(false);
                            exponentTextField.setVisible(false);
                            betaLabel.setVisible(false);
                            betaTextField.setVisible(false);
                            break;
                    }
                }
            }
        });

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());

        formPanel.add(clusteringLabel,              new GridBagConstraints(0, 0, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(clusteringAlgorithmSelection, new GridBagConstraints(3, 0, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(numClusterLabel,              new GridBagConstraints(0, 1, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(numClusterTextField,          new GridBagConstraints(3, 1, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(thresholdLabel,               new GridBagConstraints(0, 2, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(thresholdTextField,           new GridBagConstraints(3, 2, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(fuzzinessLabel,               new GridBagConstraints(0, 3, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(fuzzinessTextField,           new GridBagConstraints(3, 3, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(distanceLabel,                new GridBagConstraints(0, 4, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(distanceMetricSelection,      new GridBagConstraints(3, 4, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(exponentLabel,                new GridBagConstraints(0, 5, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(exponentTextField,            new GridBagConstraints(3, 5, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(betaLabel,                    new GridBagConstraints(0, 6, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        formPanel.add(betaTextField,                new GridBagConstraints(3, 6, 2, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            int cAlgoId = clusteringAlgorithmSelection.getSelectedIndex();
            int distId = distanceMetricSelection.getSelectedIndex();
            int numClusters = ((Number) numClusterTextField.getValue()).intValue();
            double fuzz = ((Number) fuzzinessTextField.getValue()).doubleValue();
            double exponent = ((Number) exponentTextField.getValue()).doubleValue();
            double beta = ((Number) betaTextField.getValue()).doubleValue();
            double threshold = ((Number) thresholdTextField.getValue()).doubleValue();
            tsClustering.computeCluster(cAlgoId, distId, numClusters, fuzz, exponent, beta, threshold);
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        JRootPane root = new JRootPane();
        root.setLayout(new BorderLayout());
        root.add(formPanel, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);
        setRootPane(root);
        pack();
    }
}
