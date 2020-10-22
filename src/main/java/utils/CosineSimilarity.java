package utils;

public class CosineSimilarity{

    public static double[] cosine_similarity(double[][] docMatrix, double[] query){
        double[] scores = new double[docMatrix.length];

        for (int i = 0; i < docMatrix.length; i++) {
            double[] docVector = docMatrix[i];
            double sumProduct = 0;
            double sumASq = 0;
            double sumBSq = 0;
            for (int j = 0; j < docVector.length; j++) {
                sumProduct += docVector[j]*query[j];
                sumASq += docVector[j] * docVector[j];
                sumBSq += query[j] * query[j];
            }
            scores[i] = sumProduct/(Math.sqrt(sumASq) * Math.sqrt(sumBSq));
        }

        return scores;
    }
}
