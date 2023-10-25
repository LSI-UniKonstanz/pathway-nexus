package org.vanted.addons.matrix.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.function.Function;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.vanted.addons.matrix.graph.CpdPwayGraph;

class TimeSeries {
    public double[] timeSeries;
    public double[] stdDeviation;
    public String name;
    public TimeSeries(int sampleNumber) {
        timeSeries = new double[sampleNumber];
        stdDeviation = new double[sampleNumber];
        name = "";
    }
}


public class TimeSeriesClustering implements Runnable {

    static String[] timePoints;
    TimeSeries[] ts;
    int size;
    int samples;
    double q;
    double beta;
    double m;
    double[][] membership;
    double threshold;
    boolean isRunning;
    Function<TimeSeries[], Double> distanceFunction;
    int numClusters;
    int clusteringMethod;
    int progress;
    CpdPwayGraph graphReference;
    BackgroundTaskHelper task_helper;
    BackgroundTaskStatusProviderSupportingExternalCallImpl mappingWorkStatusProvider;

    public TimeSeriesClustering(CpdPwayGraph graph) {
        timePoints =  Experiment.getTimes(graph.getExperiment());
        graphReference = graph;
        size = graph.getCompoundNodes().size();
        samples = timePoints.length;
        ts = new TimeSeries[size];
        int i = 0;
        q = 2;
        beta = 2;
        m = 2;
        progress = 0;
        isRunning = false;
        mappingWorkStatusProvider =
                new BackgroundTaskStatusProviderSupportingExternalCallImpl("Calculating clusters.", "");
        for (CompoundTextNode node : graph.getCompoundNodes()) {
            ts[i] = new TimeSeries(samples);
            int j = 0;
            for (String timePoint : timePoints) {
                ts[i].timeSeries[j] = node.getSampleMeanFor(timePoint);
                ts[i].stdDeviation[j] = node.getStdDevFor(timePoint);
                j++;
            }
            ts[i].name = node.getSubstance().getName();
            i++;
        }
    }

    /**
     * This function is called when the user wants to start a clustering algorithm via a ClusteringWindow
     * @param clusteringMethod 0: hierarchical, 1: fuzzy c-means, default: k-means
     * @param distanceMethod Choice of the distance function. 1: crisp correlation-based, 2: fuzzy correlation based, 3, 4: Mikowski,
     *                       5: short time series, 6: dynamic time warp, default: probability-based
     * @param numClusters The desired number of clusters
     * @param fuzz The amount of fuzziness for the fuzzy c-means algorithm
     * @param exponent The exponent for the Mikowski distance
     * @param beta Fuzziness of the fuzzy correlation-based distance
     * @param threshold Threshold for when a time series is considered to be part of a cluster in fuzzy c-means
     */
    public void computeCluster(int clusteringMethod,
                               int distanceMethod,
                               int numClusters,
                               double fuzz,
                               double exponent,
                               double beta,
                               double threshold) {
        isRunning = true;
        this.clusteringMethod = clusteringMethod;
        this.m = fuzz;
        this.numClusters = numClusters;
        this.q = exponent;
        this.beta = beta;
        this.threshold = threshold;
        distanceFunction = this::probability_distance;
        switch (distanceMethod) {
            case 1:
                distanceFunction = this::crisp_cc;
                break;
            case 2:
                distanceFunction = this::fuzzy_cc;
                break;
            case 3:
            case 4:
                distanceFunction = this::mikowski_distance;
                break;
            case 5:
                distanceFunction = this::short_ts;
                break;
            case 6:
                distanceFunction = this::dtw;
                break;
        }
        task_helper = new BackgroundTaskHelper(this,
                mappingWorkStatusProvider, "Clustering", "Clustering", true, false);
        task_helper.startWork(this);
    }

    /**
     * Run the clustering algorithm in its own thread so the add-on is still usable
     */
    public void run() {
        switch (clusteringMethod) {
            case 0:
                agglomerativeClustering(numClusters, distanceFunction);
                break;
            case 1:
                fuzzyCMeans(numClusters, distanceFunction);
                break;
            default:
                kMeans(numClusters, distanceFunction);
                break;
        }
        assignClusterIDs();
        isRunning = false;
    }

    //==================================================================================================================
    //  Clustering

    /**
     * Implementation of agglomerative hierarchical clustering
     * @param numClusters: The desired number of clusters.
     * @param distance: Distance function.
     */
    void agglomerativeClustering(int numClusters, Function<TimeSeries[], Double> distance) {
        membership = new double[size][numClusters];
        TimeSeries[] tmpTs = ts.clone();
        ArrayList<Integer>[] cluster = new ArrayList[size];
        for (int i = 0; i < size; i++) {
            cluster[i] = new ArrayList<>();
            cluster[i].add(i);
        }
        int currentClusters = size - 1;
        while (currentClusters > numClusters) {
            if (mappingWorkStatusProvider.wantsToStop())
                return;
            int[] minDistIDs = smallest_distance(tmpTs, currentClusters, distance);
            cluster[minDistIDs[0]].addAll(cluster[minDistIDs[1]]);
            tmpTs[minDistIDs[0]] = probability_merge(tmpTs[minDistIDs[0]], tmpTs[minDistIDs[1]]);
            cluster[minDistIDs[1]] = cluster[currentClusters];
            currentClusters--;
            mappingWorkStatusProvider.setCurrentStatusValue(100 * (size - currentClusters) / (size - numClusters));
        }

        for (int i = 0; i < numClusters; i++) {
            for (Integer id : cluster[i])
                membership[id][i] = 1;
        }
    }

    /**
     * Implementation of fuzzy c-means clustering
     * @param numClusters: The desired number of clusters.
     * @param distance: Distance function.
     */
    void fuzzyCMeans(int numClusters, Function<TimeSeries[], Double> distance) {
        // Initialize the membership matrix
        Random rng = new Random(0);
        membership = new double[size][numClusters];
        double fuzz = 0.2928932188134524 / numClusters;  // (1 - sqrt(2)/2) / numClusters
        for (int i = 0; i < size; i++) {
            membership[i][Math.abs(rng.nextInt()) % numClusters] = 0.7071067811865476;  // sqrt(2)/2
            for (int j = 0; j < numClusters; j++)
                membership[i][j] = membership[i][j] + fuzz;
        }
        TimeSeries[] centroids = computeCentroids(ts, membership, numClusters, m);

        int a = 0;
        double epsilon = Double.POSITIVE_INFINITY;
        while (a < 200 && epsilon > 0.01) {
            if (mappingWorkStatusProvider.wantsToStop())
                return;
            double[][] newMembership = updateMembership(ts, membership, centroids, m, distance);
            epsilon = frobeniusNorm(membership, newMembership);
            membership = newMembership;
            centroids = computeCentroids(ts, membership, numClusters, m);
            a++;
        }
    }

    /**
     * Implementation of k-means clustering
     * @param numClusters: The desired number of clusters.
     * @param distance: Distance function.
     */
    void kMeans(int numClusters, Function<TimeSeries[], Double> distance) {
        membership = new double[size][numClusters];
        ArrayList<Integer> indexList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            indexList.add(i);
        }
        Collections.shuffle(indexList, new Random(0));
        List<Integer> randomIndices =  indexList.subList(0, numClusters);
        TimeSeries[] centroids = new TimeSeries[numClusters];
        for (int i = 0; i < numClusters; i++) {
            centroids[i] = ts[randomIndices.get(i)];
        }
        int a = 0;
        double epsilon = Double.POSITIVE_INFINITY;
        while (a < 200 && epsilon > 0.01) {
            if (mappingWorkStatusProvider.wantsToStop())
                return;
            double[][] newMembership = kMeansMembership(ts, centroids, distance);
            epsilon = frobeniusNorm(membership, newMembership);
            membership = newMembership;
            centroids = computeCentroids(ts, membership, numClusters, 1);
            a++;
        }
    }

    //==================================================================================================================
    //  Distance functions must all have the same parameters, a single array containing two time series,
    //  and return a double value.

    /**
     *
     * @param ts Two time series
     * @return Mikowski distance between two time series
     */
    double mikowski_distance(TimeSeries[] ts) {
        if (q == 0)
            return -1;
        double distance = 0;
        for (int i = 0; i < samples; i++)
            distance = distance + Math.pow(ts[0].timeSeries[i] - ts[1].timeSeries[i], q);
        return Math.pow(distance, 1 / q);
    }

    /**
     *
     * @param ts Two time series
     * @return Crisp correlation-based distance
     */
    double crisp_cc(TimeSeries[] ts) {
        return 2 - 2 * crossCorrelation(ts);
    }

    /**
     *
     * @param ts Two time series
     * @return Crisp correlation-based distance
     */
    double fuzzy_cc(TimeSeries[] ts) {
        double cc = crossCorrelation(ts);
        if (cc == -1)
            return Double.POSITIVE_INFINITY;
        return Math.pow((1 - cc) / (1 + cc), beta);
    }

    /**
     * Calculates the distance between two time series using the standard deviation.
     * @param ts Two time series
     * @return probability-based distance
     */
    Double probability_distance(TimeSeries[] ts) {
        double distance = 0.0;
        for (int i = 0; i < samples; i++) {
            double sampleMeanA = ts[0].timeSeries[i];
            double sampleMeanB = ts[1].timeSeries[i];
            // This distance will not work if both standard deviations are 0.
            double stdDevA = Math.max(ts[0].stdDeviation[i], 0.1);
            double stdDevB = Math.max(ts[1].stdDeviation[i], 0.1);
            distance = distance + Math.pow(sampleMeanA - sampleMeanB, 2) / (Math.pow(stdDevA, 2) + Math.pow(stdDevB, 2));
        }
        ChiSquaredDistribution csd = new ChiSquaredDistribution(samples - 1);
        return csd.cumulativeProbability(distance);
    }

    /**
     * Distance based on the slopes of the line segments
     * @param ts Two time series
     * @return Euclidean distance between the slopes of the line segments
     */
    Double short_ts(TimeSeries[] ts) {
        double distance = 0;
        for (int i = 1; i < samples; i++) {
            distance = distance + Math.sqrt( Math.pow(ts[0].timeSeries[i] - ts[0].timeSeries[i - 1]
                    - ts[1].timeSeries[i] + ts[1].timeSeries[i - 1], 2));
        }
        return distance;
    }

    /**
     * Dynamic time warp. Calculates the distance as the minimum warp needed to turn one time series into the other
     * @param ts Two time series
     * @return Warp-based distance between two time series
     */
    Double dtw(TimeSeries[] ts) {
        double[][] dm = distanceMatrix(ts);
        double[] distAndLength = cumulativeDist(dm, samples, samples, 0);
        return distAndLength[0] / distAndLength[1];
    }


    /**
     * Merges two time series
     * @param tsa Time series a
     * @param tsb Time series b
     * @return a and b merged into one
     */
    TimeSeries probability_merge(TimeSeries tsa, TimeSeries tsb) {
        TimeSeries mergedTS = new TimeSeries(samples);
        for (int i = 0; i < samples; i++) {
            mergedTS.timeSeries[i] = 1 / ((1 / Math.pow(tsa.stdDeviation[i], 2)) + (1 / Math.pow(tsb.stdDeviation[i], 2))) *
                    ((tsa.timeSeries[i] / Math.pow(tsa.stdDeviation[i], 2)) +
                            (tsb.timeSeries[i] / Math.pow(tsb.stdDeviation[i], 2)));
            mergedTS.stdDeviation[i] = 1 / Math.sqrt((1 / Math.pow(tsa.stdDeviation[i], 2)) +
                    (1 / Math.pow(tsb.stdDeviation[i], 2)));
        }
        return mergedTS;
    }

    /**
     *
     * @param ts Two time series
     * @return Pearson's correlation coefficient
     */
    double crossCorrelation(TimeSeries[] ts) {
        double muXi = 0;
        double muYi = 0;
        for (int i = 0; i < samples; i++) {
            muXi += ts[0].timeSeries[i];
            muYi += ts[1].timeSeries[i];
        }
        double scatterXi = 0;
        double scatterYi = 0;
        double cc = 0;
        for (int i = 0; i < samples; i++) {
            scatterXi += Math.pow(ts[0].timeSeries[i] - muXi, 2);
            scatterYi += Math.pow(ts[1].timeSeries[i] - muYi, 2);
            cc += (ts[0].timeSeries[i] - muXi) * (ts[1].timeSeries[i] - muYi);
        }
        scatterXi = Math.sqrt(scatterXi);
        scatterYi = Math.sqrt(scatterYi);
        return cc / (scatterXi * scatterYi);
    }

    /**
     * Compute cluster centroids for k-means and fuzzy c-means
     * @param data Set of all time series
     * @param membership A matrix that denotes membership probabilities for all time series
     * @param numClusters Desired number of clusters
     * @param m Fuzziness in the fuzzy c-means algorithm. m = 1 for k-means
     * @return The new set of centroids
     */
    TimeSeries[] computeCentroids(TimeSeries[] data, double[][] membership, int  numClusters, double m) {
        TimeSeries[] centroids = new TimeSeries[numClusters];
        for (int i = 0; i < numClusters; i++) {
            double[] meanTs = new double[samples];
            double[] meanStdDev = new double[samples];
            double normalization = 0;
            for (int a = 0; a <  size; a++) {
                for (int j = 0; j < samples; j++) {
                    meanTs[j] = meanTs[j] + Math.pow(membership[a][i], m) * data[a].timeSeries[j];
                    meanStdDev[j] = meanStdDev[j] + Math.pow(membership[a][i], m) * Math.pow(data[a].stdDeviation[j], 2);
                }
                normalization = normalization + Math.pow(membership[a][i], m);
            }
            for (int j = 0; j < samples; j++) {
                meanTs[j] = meanTs[j] / normalization;
                meanStdDev[j] = Math.sqrt(meanStdDev[j] / normalization);
            }
            centroids[i] = new TimeSeries(samples);
            centroids[i].timeSeries = meanTs;
            centroids[i].stdDeviation = meanStdDev;
        }
        return centroids;
    }

    /**
     * Updates the membership matrix in the fuzzy c-means algorithm
     * @param data Set of all time series
     * @param oldMembership Old membership matrix
     * @param centroids Set of centroids
     * @param m Fuzziness
     * @param distance Distance function
     * @return new membership matrix
     */
    double[][] updateMembership(TimeSeries[] data,
                                double[][] oldMembership,
                                TimeSeries[] centroids,
                                double m,
                                Function<TimeSeries[], Double> distance) {
        double[][] newMembership = oldMembership.clone();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < centroids.length; j++) {
                double s = 0;
                for (int k = 0; k < centroids.length; k++) {
                    if (j != k) {
                        TimeSeries[] pairForDistFunc1 = new TimeSeries[2];
                        pairForDistFunc1[0] = data[i];
                        pairForDistFunc1[1] = centroids[j];
                        TimeSeries[] pairForDistFunc2 = new TimeSeries[2];
                        pairForDistFunc2[0] = data[i];
                        pairForDistFunc2[1] = centroids[k];
                        s = s + Math.pow(distance.apply(pairForDistFunc1) / distance.apply(pairForDistFunc2), 1/(m-1));
                    }
                }
                newMembership[i][j] = 1 / (1 + s);
            }
        }
        return newMembership;
    }

    /**
     *
     * @param a A matrix
     * @param b Another matrix
     * @return Frobenius norm of the matrix a-b
     */
    double frobeniusNorm(double[][] a, double[][] b) {
        double norm = 0;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                norm = norm + Math.pow(a[i][j] - b[i][j], 2);
            }
        }
        return Math.sqrt(norm);
    }

    /**
     * Finds the two closest time series in a set of time series
     * @param ts Set of time series
     * @param maxSize Only use the first maxSize elements
     * @param distance distance function
     * @return the indices of the two closest time series
     */
    int[] smallest_distance(TimeSeries[] ts, int maxSize, Function<TimeSeries[], Double> distance) {
        double min_dist = Double.POSITIVE_INFINITY;
        int[] indices = new int[2];
        for (int i = 0; i < maxSize; i++) {
            for (int j = i + 1; j < maxSize; j++) {
                TimeSeries[] params = new TimeSeries[2];
                params[0] = ts[i];
                params[1] = ts[j];
                double d = distance.apply(params);
                if (d < min_dist) {
                    min_dist = d;
                    indices[0] = i;
                    indices[1] = j;
                }
            }
        }
        return indices;
    }

    /**
     *
     * @param ts Exactly two time series
     * @return A distance matrix for all samples of two time series
     */
    double[][] distanceMatrix(TimeSeries[] ts) {
        double[][] dm = new double[samples][samples];
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < samples; j++) {
                dm[i][j] = Math.abs(ts[0].timeSeries[i] - ts[1].timeSeries[j]);
            }
        }
        return dm;
    }

    /**
     *
     * @param dm Distance matrix
     * @param i x position in the distance matrix
     * @param j y position in the distance matrix
     * @param length Cumulative length
     * @return Cumulative distance and length in an array
     */
    double[] cumulativeDist(double[][] dm, int i, int j, int length) {
        double[] distAndLength = new double[2];
        boolean c1 = i <= 0;
        boolean c2 = j <= 0;
        if (c1 && c2) {
            distAndLength[0] = dm[i][j];
            distAndLength[1] = length;
            return distAndLength;
        }
        if (c1) {
            distAndLength = cumulativeDist(dm, i, j - 1, length + 1);
            distAndLength[0] = distAndLength[0] + dm[i][j];
            return distAndLength;
        }
        else if (c2) {
            distAndLength = cumulativeDist(dm, i - 1, j, length + 1);
            distAndLength[0] = distAndLength[0] + dm[i][j];
            return distAndLength;
        }
        else {
            double[] dl1 = cumulativeDist(dm, i - 1, j - 1, length + 1);
            double[] dl2 = cumulativeDist(dm, i - 1, j, length + 1);
            double[] dl3 = cumulativeDist(dm, i, j - 1, length + 1);
            double max_dist = Math.max(Math.max(dl1[0], dl2[0]), dl3[0]);
            if (max_dist == dl1[0]) {
                distAndLength[1] = dl1[1];
            }
            else if (max_dist == dl2[0]) {
                distAndLength[1] = dl2[1];
            }
            else {
                distAndLength[1] = dl3[1];
            }
            distAndLength[0] = dm[i][j] + Math.min(Math.min(dl1[0], dl2[0]), dl3[0]);
            return distAndLength;
        }
    }

    /**
     * Write the clustering results into the substance data structure
     */
    void assignClusterIDs() {
        int i = 0;
        for (CompoundTextNode node : graphReference.getCompoundNodes()) {
            int j = 0;
            int max_index = 0;
            double max_membership = 0;
            for (double m : membership[i]) {
                if (m > max_membership) {
                    max_membership = m;
                    max_index = j;
                }
                j++;
            }
            node.getSubstance().setClusterId(String.valueOf(max_index));
            i++;
        }
    }

    /**
     * Assigns all time series to a cluster in the k-means algorithm
     * @param data A set of time series
     * @param centroids The set of cluster centroids
     * @param distance Distance function
     * @return A sparse size x numClusters matrix indicating cluster membership of time series
     */
    double[][] kMeansMembership(TimeSeries[] data,
                                TimeSeries[] centroids,
                                Function<TimeSeries[], Double> distance) {
        double[][] newMembership = new double[size][centroids.length];
        for (int i = 0; i < size; i++) {
            int currentMin = 0;
            Double minDist = Double.POSITIVE_INFINITY;
            TimeSeries[] tsForDist = new TimeSeries[2];
            tsForDist[0] = data[i];
            for (int j = 0; j < centroids.length; j++) {
                tsForDist[1] = centroids[j];
                Double dist = distance.apply(tsForDist);
                if (dist < minDist) {
                    minDist = dist;
                    currentMin = j;
                }
                newMembership[i][j] = 0;
            }
            newMembership[i][currentMin] = 1;
        }
        return newMembership;
    }

    /**
     * Check if this runnable is currently running
     * @return True if this runnable is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
