import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class MergeSort {

        static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

        /* define constants */
        static long MAXVALUE =  2000000000;
        static long MINVALUE = -2000000000;
        static int numberOfTrials = 100;
        static int MAXINPUTSIZE  = (int) Math.pow(1.5,31);
        static int MININPUTSIZE  =  1;
        // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

        static String ResultsFolderPath = "/home/diana/Results/"; // pathname to results folder
        static FileWriter resultsFile;
        static PrintWriter resultsWriter;


        public static void main(String[] args) {
            //function to verify it is sorting correctly

            checkSortCorrectness();

            // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized

            System.out.println("Running first full experiment...");
            runFullExperiment("MergeSort-Exp1-ThrowAway.txt");
            System.out.println("Running second full experiment...");
            runFullExperiment("MergeSort-Exp2.txt");
            System.out.println("Running third full experiment...");
            runFullExperiment("MergeSort-Exp3.txt");
        }

        static void runFullExperiment(String resultsFileName){
            //declare variables for doubling ratio
            double[] averageArray = new double[1000];
            double currentAv = 0;
            double doublingTotal = 0;
            int x = 0;

            //set up print to file
            try {
                resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
                resultsWriter = new PrintWriter(resultsFile);
            } catch(Exception e) {
                System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
                return; // not very foolproof... but we do expect to be able to create/open the file...
            }

            //declare variables for stop watch
            ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
            ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

            //add headers to text file
            resultsWriter.println("#InputSize    AverageTime    DoublingRatio"); // # marks a comment in gnuplot data
            resultsWriter.flush();

            /* for each size of input we want to test: in this case starting small and doubling the size each time */
            for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {

                // progress message...
                System.out.println("Running test for input size "+inputSize+" ... ");

                /* repeat for desired number of trials (for a specific size of input)... */
                long batchElapsedTime = 0;
                // generate a list of randomly spaced integers in ascending sorted order to use as test input
                // In this case we're generating one list to use for the entire set of trials (of a given input size)
                // but we will randomly generate the search key for each trial
                System.out.print("    Generating test data...");

                //generate random integer list
                long[] testList = createRandomIntegerList(inputSize);

                //print progress to screen
                System.out.println("...done.");
                System.out.print("    Running trial batch...");

                /* force garbage collection before each batch of trials run so it is not included in the time */
                System.gc();


                // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
                // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
                // stopwatch methods themselves
                BatchStopwatch.start(); // comment this line if timing trials individually

                // run the trials
                for (long trial = 0; trial < numberOfTrials; trial++) {
                    // generate a random key to search in the range of a the min/max numbers in the list
                    //long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                    /* force garbage collection before each trial run so it is not included in the time */
                    // System.gc();

                    //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                    /* run the function we're testing on the trial input */
                    //long foundIndex = MergeSort(testList);
                    long[] SortedList = mergeSortAlg(testList);
                    // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
                }
                batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
                double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

                //put current average time in array of average times. We will be able to use this to calculate the doubling ratio
                averageArray[x] = averageTimePerTrialInBatch;

                //skip this round if this is the first one (no previous average for calculation)
                if(inputSize != 1){
                    doublingTotal = averageTimePerTrialInBatch/averageArray[x-1]; //Calculate doubling ratio

                }
                x++;
                /* print data for this size of input */
                resultsWriter.printf("%12d  %15.2f %15.2f \n",inputSize, averageTimePerTrialInBatch, doublingTotal); // might as well make the columns look
                /*resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice*/
                resultsWriter.flush();
                System.out.println(" ....done.");
            }
        }

        /*Verify merge sort is working*/
        static boolean verifySort(long[] list){

            boolean sorted = true;
            int i = 0;
            //loop through list
            for(i = 1; i < list.length; i++) {
                //set the current number to number on right side of the pair of numbers you are checking
                long currentNum = list[i];
                //set j equal to the left number of the pair of numbers you are checking
                int j = i - 1;
                //check if the left side number is greater than the one on the right.
                //if it is, move it to the right decrement j. Keep doing this until j is less than 0
                while (j >= 0 && list[j] > currentNum) {
                    list[j + 1] = list[j];
                    j = j - 1;
                    sorted = false;
                }
                //set the far left number equal to the current number.
                list[j + 1] = currentNum;
            }
            return sorted;
        }

        static void checkSortCorrectness(){
            //test to sort small list - print before and after sort
            long[] testList1 = createRandomIntegerList(15);
            long[] resultList = mergeSortAlg(testList1);
            System.out.println("Small list test: " );
            System.out.println(Arrays.toString(resultList));

            //test to sort medium list - verifySort through function
            //and return true if sorted, false if not sorted
            long[] testList2 = createRandomIntegerList(200);
            long[] resultList2 = mergeSortAlg(testList2);
            boolean sorted2 = verifySort(resultList2);
            System.out.println("Medium list test sorted: " + sorted2);

            //test to sort large list - verifySort through function
            //and return true if sorted, false if not sorted
            long[] testList3 = createRandomIntegerList(1000);
            long[] resultList3 = mergeSortAlg(testList2);
            boolean sorted3 = verifySort(resultList2);
            System.out.println("Large list test sorted: " + sorted3);

        }

        public static long[] mergeSortAlg(long[] list){
            //divide list into two halves
            //if list.length is less than or equal to 1 the list is either empty or
            //has one element and can't be divided
            if(list.length <= 1){
                //return list
                return list;
            }
            else{
                //set middle index as list length / 2 and set the left array with
                //the length of the middle index
                int mid = list.length / 2;
                long[] left = new long[mid];

                //create array for right side of list and if it is even, set it to the
                //length of the middle index. if odd, set it to the length of middle
                //index and add one to account for the odd number.
                long[] right;
                if(list.length % 2 == 0){
                    right = new long[mid];
                }
                else{

                    right = new long[mid+1];
                }
                //loop through the lower half of the list (array) and add it
                //to the left array
                for(int i = 0; i < mid; i++){
                    left[i] = list[i];
                }
                //loop through the upper half of the list (array) and add it
                //to the right array
                for(int j=0; j< right.length; j++){
                    right[j] = list[mid+j];
                }

                //create an array to hold the results of the merged array
                long[] result = new long[list.length];
                //call the function recursively to continue to populate the left and
                //right arrays until all the numbers have been copied over
                left = mergeSortAlg(left);
                right = mergeSortAlg(right);
                //call the merge function to merge both lists together with the values sorted
                result = Merge(left, right);
                //return the result
                return result;
            }
        }



    public static long[] Merge(long[] listA, long[] listB){
        //declare array for mered list that equals the length of each list added together
        long[] mergedList = new long[listA.length + listB.length];

        int iA = 0;
        int iB = 0;
        int iM = 0;
        //loop until iA or iB is less than the length of the list it's helping merge
        while(iA < listA.length || iB < listB.length){
            //if count iA is less than the length of list A AND count iB is less than the legnth of list B
            //check if the value in list A at index iA is less than the index iB in list B
            if(iA < listA.length && iB < listB.length){
                if(listA[iA] < listB[iB]){
                    //if true, set value at index iM in merged list to listA index iA
                    mergedList[iM] = listA[iA];
                    //increment iA and IM
                    iA++;
                    iM++;
                }
                else
                {
                    //if false, set value at index iM in merged list to listB index iB
                    mergedList[iM] = listB[iB];
                    //increment iB and iM
                    iB++;
                    iM++;
                }
            }
            else if(iA < listA.length){ //check to see if index iA is less than listA length
                //if true, set merged list index iM to list A index iA
                mergedList[iM] = listA[iA];
                //increment iA and IM
                iA++;
                iM++;
            }
            else if(iB < listB.length){//check to see if index iB is less than listB length
                //if true, set merged list index iM to list B index iB
                mergedList[iM] = listB[iB];
                //increment iA and IM
                iB++;
                iM++;
            }

        }
    //return merged list
        return mergedList;
    }

        public static long[] createRandomIntegerList(int size) {

            long[] newList = new long[size];
            for (int j = 0; j < size; j++) {
                newList[j] = (long) (MINVALUE + Math.random() * (MAXVALUE - MINVALUE));
            }
            return newList;
        }

}
