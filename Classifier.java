/*
 * 3/4/25
 */
import java.io.*;
import java.util.*;

/*
 * The following creates a program that classifies if text-based inputs are spam or not. 
 * This program can train a model that could be used later to classify if texts are spam or not.
 * This program also offers a feature that could save the classification algorithm into an 
 * external file. 
 */
public class Classifier {
    private ClassifierNode overallRoot;
    /*
     * This creates a predictive model for text-based data. The algorithm for the predictions 
     *      is determined by a given file. Any input that is predicted as spam is classified as
     *      "spam" and anything that isn't is classified as "Ham". 
     * Parameters: 
     *      - input: reads the given file line by line
     * Exceptions: 
     *      - IllegalArgumentException(): gets thrown if the input parameter is null
     */
    public Classifier(Scanner input) {
        if (input == null) {
            throw new IllegalArgumentException("The input cannot be null!");
        }
        overallRoot = writeScannerTree(input);
    }

    /*
     * This actually contains the logic behind building a spam classifier tree for the predictive
     *      algorithm.
     * Parameters: 
     *      - input: reads the given file line by line
     * Return: 
     *      - ClassifierNode: the spam classifier tree that is getting built
     */
    private ClassifierNode writeScannerTree(Scanner input) {
        ClassifierNode currentNode = null;
        String line = input.nextLine();
        if (line.contains("Feature")) {
            double threshold = Double.parseDouble(input.nextLine().substring("Threshold: ".length()));
            ClassifierNode left = writeScannerTree(input);
            ClassifierNode right = writeScannerTree(input);
            return new ClassifierNode(left, right, line.substring(9, line.length()), threshold);
        }
        else {
            currentNode = new ClassifierNode(line);
            return currentNode;
        }
    }

    /*
     * This makes a predictive model for text-based data. This builds an algorithm for it
     *      from scratch. Any text that is predicted as spam is classified as
     *      "spam" and anything that isn't is classified as "Ham". 
     * Parameters: 
     *      - data: these are training data for the model. It is a list of text-based data. 
     *      - labels: a list of corresponding "Ham" or "Spam" labels for the training data
     * Exceptions: 
     *      - IllegalArgumentException(): gets thrown if the training data or labels are null,
     *          empty, or have differing sizes from each other
     */
    public Classifier(List<TextBlock> data, List<String> labels) {
        if (data == null || labels == null) {
            throw new IllegalArgumentException();
        }
        if (data.size() != labels.size()) {
            throw new IllegalArgumentException();
        }
        if (data.isEmpty() || labels.isEmpty()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < data.size(); i++) {
            overallRoot = writeTree(data.get(i), labels.get(i), overallRoot);
        }
    }

    /*
     * This actually contains the logic behind writing a decision tree based on the training data. 
     *      If the tree is empty, then a new data-label pair is added. If a current label node 
     *      for the tree does not agree with a provided label, then an additional data-label 
     *      pair is created to accomodate for those differences. The new data-label pair is 
     *      added either to the left side or right side and the existing data-label pair it 
     *      differed with is moved to the opposite side of it. These sides are determined by 
     *      if the new data-label pair has its most differing data word probability above or below 
     *      the midpoint threshold of the word probabiltiies of the existing and new pair. 
     *      Left side is for whichever pair has less word probability than threshold and right side
     *      is for the greater probability.
     * Parameters: 
     *      - datas: the training data that is waitign to be classified
     *      - labels: the classification label for the training data
     *      - curr: the current node in the traversal of the decision tree
     * Return: 
     *      - The updated tree with the new data-label pair inserted if needed based on the
     *          conditions defined above.   
     */
    private ClassifierNode writeTree(TextBlock datas, String labels, ClassifierNode curr) {
        if (curr == null) {
            return new ClassifierNode(datas, labels);
        }
        if (curr.isLeaf()) {
            if (curr.classificationLabel.equals(labels)) {
                return curr;
            }
            String feature = datas.findBiggestDifference(curr.data);
            double threshold = midpoint(curr.data.get(feature), datas.get(feature));
            ClassifierNode left;
            ClassifierNode right;
            if (datas.get(feature) < threshold) {
                left = new ClassifierNode(datas, labels);
                right = curr;
            } 
            else {
                left = curr;
                right = new ClassifierNode(datas, labels);
            }
            return new ClassifierNode(left, right, feature, threshold);
        }
            
         if (datas.get(curr.featureWord) < curr.threshold) {
            curr.leftLabel = writeTree(datas, labels, curr.leftLabel);
         } else {
            curr.rightLabel = writeTree(datas, labels, curr.rightLabel);
         }
         return curr;
    }


    /*
     * This saves the predictive decision tree algorithm to an external file in text-based format.
     * Parameters: 
     *      - output: the file that the algorithm will be saved in
     * Exceptions: 
     *      - IllegalArgumentException(): gets thrown if the output file is null
     */
    public void save(PrintStream output) {
        if (output == null) {
            throw new IllegalArgumentException();
        }
        save(output, overallRoot);
    }

    /*
     * This method actually contains the logic behind capturing data in the decision tree to put in
     *      the external file. This traverses through the whole decision tree and captures
     *      information from every single decision and label node that exists to put in the file.
     *      Decision nodes feature word is marked as "Feature: " and its threshold "Threshold: ".
     *      Label nodes are marked with just either "Ham" or "Spam"
     * Parameters: 
     *      - output: the file that the decision tree data is being stored into
     *      - curr: the current node being processed in the tree
     */
    private void save(PrintStream output, ClassifierNode curr) {
        if (curr != null) {
            if (curr.leftLabel != null && curr.rightLabel != null) {
                output.println("Feature: " + curr.featureWord);
                output.println("Threshold: " + curr.threshold);
                save(output, curr.leftLabel);
                save(output, curr.rightLabel);
            }
            else {
                output.println(curr.classificationLabel);
            }
        }
    }

    /*
     * This classifies an input as either "Ham" or "Spam".
     * Parameters: 
     *      - input: text-based data that is waiting to get classified
     * Exceptions: 
     *      - IllegalArgumentException(): gets thrown if the given input is null
     * Return: 
     *      - The classification label which is either "Ham" (not spam) or "Spam". If the decision
     *          tree is null, then "" is returned.
     */
    public String classify(TextBlock input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        return classify(input, overallRoot);
    }

    /*
     * This contains the logic behind classifying an input as "Ham" or "Spam".
     * This traverses through the decision tree. Whichever word the input has that aligns
     * with a certain word on a decision node, the method compares the probability of the word
     * in the TextBlock with the threshold number. If it is less than the threshold, then the 
     * algorithm would look at the left child nodes to the decision node. If greater, then the
     * right child nodes would be looked at. This goes on until a "Ham" or "Spam node is reached".
     * Parameters: 
     *      - input: text-based data that is waiting to get classified
     *      - curr: The current node that is being looked at in the decision tree
     * Return: 
     *      - String: If the entire tree is null, then an empty String is returned. Else,
     *          either "Ham" or "Spam" is returned.
     */
    private String classify(TextBlock input, ClassifierNode curr) {
        String result = "";
        if (curr != null) {
            if (curr.isLeaf()) {
                result = curr.classificationLabel;
                return result;
            }
            else {
                String specificWord = "";
                for (String element : input.getFeatures()) {
                    if (element.equals(curr.featureWord)) {
                        specificWord = element;
                    }  
                }
                if (input.get(specificWord) < curr.threshold) {
                    result = classify(input, curr.leftLabel);
                } else {
                    result = classify(input, curr.rightLabel);
                }
            }
        }
        return result;
    }
    

    ////////////////////////////////////////////////////////////////////
    // PROVIDED METHODS - **DO NOT MODIFY ANYTHING BELOW THIS LINE!** //
    ////////////////////////////////////////////////////////////////////

    // Helper method to calcualte the midpoint of two provided doubles.
    private static double midpoint(double one, double two) {
        return Math.min(one, two) + (Math.abs(one - two) / 2.0);
    }    

    // Behavior: Calculates the accuracy of this model on provided Lists of 
    //           testing 'data' and corresponding 'labels'. The label for a 
    //           datapoint at an index within 'data' should be found at the 
    //           same index within 'labels'.
    // Exceptions: IllegalArgumentException if the number of datapoints doesn't match the number 
    //             of provided labels
    // Returns: a map storing the classification accuracy for each of the encountered labels when
    //          classifying
    // Parameters: data - the list of TextBlock objects to classify. Should be non-null.
    //             labels - the list of expected labels for each TextBlock object. 
    //             Should be non-null.
    public Map<String, Double> calculateAccuracy(List<TextBlock> data, List<String> labels) {
        // Check to make sure the lists have the same size (each datapoint has an expected label)
        if (data.size() != labels.size()) {
            throw new IllegalArgumentException(
                    String.format("Length of provided data [%d] doesn't match provided labels [%d]",
                                  data.size(), labels.size()));
        }
        
        // Create our total and correct maps for average calculation
        Map<String, Integer> labelToTotal = new HashMap<>();
        Map<String, Double> labelToCorrect = new HashMap<>();
        labelToTotal.put("Overall", 0);
        labelToCorrect.put("Overall", 0.0);
        
        for (int i = 0; i < data.size(); i++) {
            String result = classify(data.get(i));
            String label = labels.get(i);

            // Increment totals depending on resultant label
            labelToTotal.put(label, labelToTotal.getOrDefault(label, 0) + 1);
            labelToTotal.put("Overall", labelToTotal.get("Overall") + 1);
            if (result.equals(label)) {
                labelToCorrect.put(result, labelToCorrect.getOrDefault(result, 0.0) + 1);
                labelToCorrect.put("Overall", labelToCorrect.get("Overall") + 1);
            }
        }

        // Turn totals into accuracy percentage
        for (String label : labelToCorrect.keySet()) {
            labelToCorrect.put(label, labelToCorrect.get(label) / labelToTotal.get(label));
        }
        return labelToCorrect;
    }

    /* 
     * This class will make the decision and label nodes in the Classifier decision tree.
     */
    private static class ClassifierNode {
        public ClassifierNode leftLabel;
        public ClassifierNode rightLabel;
        public final TextBlock data;
        public String classificationLabel;
        public String featureWord;
        public double threshold;

        /*
         * This deals with making a label node when only given a classification label. The data
         *      assosicated with it would be null in this case.
         * Parameter: 
         *      - classificationLabel: Either "Ham" or "Spam"
         */
        public ClassifierNode(String classificationLabel) {
            this(null, classificationLabel);
        }

        /*
         * This deals with making a full label node. This makes a label node for a Classifier tree
         *      be either assosocieted with "Ham" or "Spam". This node would also hold information 
         *      about the training data that is assosciated with it. 
         * Parameters: 
         *      - data: data that is put in when training the spam classifier model
         *      - classificationLabel: Either "Ham" or "Spam"
         */
        public ClassifierNode(TextBlock data, String classificationLabel) {
            this.data = data;
            this.leftLabel = null;
            this.rightLabel = null;
            this.classificationLabel = classificationLabel;
            this.featureWord = null;
            this.threshold = 0;
        }

        /*
         * This deals with making a decision node for the Classifier Tree. This contains
         *      information about a threshold number for a specific word. This node also would hold
         *      information to nodes to the bottom left of it (which would be used if words are 
         *      less than the threshold) and the bottom right of it (if words are greater
         *      than the threshold)
         * Parameters: 
         *      - leftLabel: the left bottom node to this current node being made
         *      - rightLabel: the right bottom node to this current node being made
         *      - featureWord: the word whose threshold will be compared to when the 
         *          tree is traversed
         *      - threshold: the threshold number for the featureWord. If the specific word in an
         *          input has a lesser probability than the threshold then the leftLabel node 
         *          would be used. Otherwise, the rightLabel node would be used. 
         */
        public ClassifierNode(ClassifierNode leftLabel, ClassifierNode rightLabel, String featureWord, double threshold) {
            this.leftLabel = leftLabel;
            this.rightLabel = rightLabel;
            this.featureWord = featureWord;
            this.threshold = threshold;
            this.data = null;
        }

        /*
         * This determines whether an end of the decision tree is reached or not. 
         * This tells you when a "ham" or "Spam" classification label node is reached
         * Return: 
         *      - true if the end is reached and false if not
         */
        public boolean isLeaf() {
            return this.classificationLabel != null;
        }
    }
}
