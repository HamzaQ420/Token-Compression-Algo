import java.util.Scanner; import java.text.DecimalFormat;
import java.util.Collections; import java.io.File; import java.io.IOException;
import java.io.FileNotFoundException; import java.util.Comparator;
import java.util.ArrayList; import java.util.Arrays; import java.io.FileWriter;
import java.util.HashMap; import java.util.TreeMap; import java.util.Map;

public class Compressor {

    private static ArrayList<ArrayList<Object>> keys = new ArrayList<>();
    private static HashMap<Integer, ArrayList<String>> sortedKeys = new HashMap<>();

    public static void compressor(String fileName) {
        double startTime = System.nanoTime();
        try {
            Scanner scnr = new Scanner(new File(fileName));
            int sumoflines = 0;
            while (scnr.hasNextLine()) { sumoflines += scnr.nextLine().split(" ").length; } System.out.println("Compression Started");
            scnr = new Scanner(new File(fileName)); double lastPrintedPercentage = -1;
            while(scnr.hasNextLine()) {
                // Algorithm to get all keys found in a string.
                String[] words = scnr.nextLine().split(" "); double num = 1; DecimalFormat df = new DecimalFormat("#.##");
                for (String word : words) {
                    word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    boolean foundWord = false; int wordIndex = 0; Integer newWordVal = 0;
                    for (ArrayList<Object> keyPair : keys) {
                        if (word.equals((String) keyPair.get(0))) { foundWord = true; wordIndex = keys.indexOf(keyPair); newWordVal = (Integer) keyPair.get(1) + 1; break; }
                    } if (foundWord) { keys.set(wordIndex, new ArrayList<Object>(Arrays.asList(word, newWordVal))); }
                    else { if (word.length() > 2) { keys.add(new ArrayList<Object>(Arrays.asList(word, 1))); } }
                    int num_secs = 2;
                    for (int sec_len = word.length() - 1; sec_len > 2; sec_len--) {
                        for (int x = 0; x < num_secs; x++) {
                            boolean found = false; int index = 0;
                            // Checking if the key already appears in the ArrayList.
                            for (ArrayList<Object> keyPair : keys) {
                                if (keyPair.get(0).equals(word.substring(x, sec_len + x))) { found = true; index = keys.indexOf(keyPair); break; }
                            } if (!found) { keys.add(new ArrayList<Object>(Arrays.asList(word.substring(x, sec_len + x), 1))); }
                            else {
                                Integer newVal = (Integer) keys.get(index).get(1) + 1;
                                keys.set(index, new ArrayList<Object>(Arrays.asList(word.substring(x, sec_len + x), newVal)));
                            }
                        } num_secs++;
                    }
                    double currentPercentage = (num / sumoflines) * 100;

                    if (Math.floor(currentPercentage) != Math.floor(lastPrintedPercentage)) {
                        printProgressBar(currentPercentage, 50);
                        lastPrintedPercentage = currentPercentage;
                    } num++;
                }
            }
        } catch (FileNotFoundException e) { e.printStackTrace(); }

        // Loop to group keys among their frequencies.
        for (ArrayList<Object> keySet : keys) {
            if (sortedKeys.containsKey((Integer) keySet.get(1))) {
                sortedKeys.get((Integer) keySet.get(1)).add((String) keySet.get(0));
            } else {
                sortedKeys.put((Integer) keySet.get(1), new ArrayList<>(Arrays.asList((String) keySet.get(0))));
            }
        }

        // Sorting every AL in the map to be in descending order.
        TreeMap<Integer, ArrayList<String>> sortedMap = new TreeMap<>((key1, key2) -> key2 - key1);
        sortedMap.putAll(sortedKeys);

        for (Map.Entry<Integer, ArrayList<String>> entry : sortedMap.entrySet()) {
            String startKey = entry.getValue().get(0);
            for (String key : entry.getValue()) {
                if (key.length() >= startKey.length()) { startKey = key; }
                else { entry.getValue().set(entry.getValue().indexOf(key), "NULL"); }
            }
            entry.getValue().removeIf(s -> s.equals("NULL"));
        } keys.clear();
        for (Map.Entry<Integer, ArrayList<String>> entry : sortedMap.entrySet()) {
            for (String key : entry.getValue()) {
                keys.add(new ArrayList<>(Arrays.asList(entry.getKey(), key)));
            }
        }

        Collections.sort(keys, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                ArrayList<?> pair1 = (ArrayList<?>) o1;
                ArrayList<?> pair2 = (ArrayList<?>) o2;

                String str1 = (String) pair1.get(1); int num1 = (Integer) pair1.get(0);
                String str2 = (String) pair2.get(1); int num2 = (Integer) pair2.get(0);

                int value1 = num1 * str1.length(); int value2 = num2 * str2.length();

                return Integer.compare(value2, value1);
            }
        });

        double totalChar = 0; System.out.println();
        for (ArrayList<Object> keyPair : keys) { totalChar += ((Integer) keyPair.get(0)) * ((String) keyPair.get(1)).length(); System.out.println(keyPair.get(0) + " -> " + keyPair.get(1)); }

        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000;
        double minutes = elapsedSeconds / 60; double seconds = elapsedSeconds % 60;
        if (seconds < 10) { System.out.println("Elapsed Time: " + (int) minutes + ":0" + (int) seconds); }
        else { System.out.println("Elapsed Time: " + (int) minutes + ":" + (int) seconds); }
    }

    public static void keyCrafter(String fileName) {
        compressor(fileName); System.out.println(keys.size());
        String endKey = "";
        if (keys.size() == 234) { endKey = "Z9"; }
        else { endKey = retEndKey(); }
        System.out.println(endKey);
        try {
            String line = "";
            Scanner scnr = new Scanner(new File(fileName));
            while (scnr.hasNextLine()) {
                for (String word : scnr.nextLine().split(" ")) {
                    boolean foundKey = false; word = word.replace(" ", "");
                    for (ArrayList<Object> keyPair : keys) {
                        if (foundKey) { break; }
                        else {
                            if (keyPair.get(1).equals(word)) { line += retEndKey(keys.indexOf(keyPair)); foundKey = true; break; }
                            else {
                                int sections = 1 + word.length() - keyPair.get(1).toString().length();
                                for (int x = 0; x < sections; x++) {
                                    if (keyPair.get(1).equals(word.substring(x, x + keyPair.get(1).toString().length()))) {
                                        foundKey = true;
                                        line += word.substring(0, x) + retEndKey(keys.indexOf(keyPair)) + word.substring(x + keyPair.get(1).toString().length(), word.length()); break;
                                    }
                                }
                            }
                        }
                    } if (!foundKey) { line += word; }
                    line += " ";
                } line += "\n";
            } try {
                FileWriter writer = new FileWriter(fileName.replace(".txt", "_compressed.txt")); writer.write(line); writer.close();
            } catch (IOException e) { ; }
        } catch (FileNotFoundException e) { e.printStackTrace(); }
    }

    public static String retEndKey() {
        int num = keys.size() / 9; String endKey = "";
        switch (num) {
            case 0: endKey += "A"; break; case 1: endKey +=  "B"; break;
            case 2: endKey += "C"; break; case 3: endKey += "D"; break; case 4: endKey += "E"; break; case 5: endKey += "F"; break;
            case 6: endKey += "G"; break; case 7: endKey += "H"; break; case 8: endKey += "I"; break; case 9: endKey += "J"; break;
            case 10: endKey += "K"; break; case 11: endKey += "L"; break; case 12: endKey += "M"; break; case 13: endKey += "N"; break;
            case 14: endKey += "O"; break; case 15: endKey += "P"; break; case 16: endKey += "Q"; break; case 17: endKey += "R"; break;
            case 18: endKey += "S"; break; case 19: endKey += "T"; break; case 20: endKey += "U"; break; case 21: endKey += "V"; break;
            case 22: endKey += "W"; break; case 23: endKey += "X"; break; case 24: endKey += "Y"; break; case 25: endKey += "Z"; break;
        } endKey += keys.size() % 9; return endKey;
    }

    public static String retEndKey(int num) {
        int newnum = num / 9; String endKey = "";
        switch (newnum) {
            case 0: endKey += "A"; break; case 1: endKey +=  "B"; break;
            case 2: endKey += "C"; break; case 3: endKey += "D"; break; case 4: endKey += "E"; break; case 5: endKey += "F"; break;
            case 6: endKey += "G"; break; case 7: endKey += "H"; break; case 8: endKey += "I"; break; case 9: endKey += "J"; break;
            case 10: endKey += "K"; break; case 11: endKey += "L"; break; case 12: endKey += "M"; break; case 13: endKey += "N"; break;
            case 14: endKey += "O"; break; case 15: endKey += "P"; break; case 16: endKey += "Q"; break; case 17: endKey += "R"; break;
            case 18: endKey += "S"; break; case 19: endKey += "T"; break; case 20: endKey += "U"; break; case 21: endKey += "V"; break;
            case 22: endKey += "W"; break; case 23: endKey += "X"; break; case 24: endKey += "Y"; break; case 25: endKey += "Z"; break;
        } endKey += (num % 9); return endKey;
    }

    public static void printProgressBar(double percentage, int barLength) {
        // Calculate the number of 'filled' sections based on the percentage
        int filledLength = (int) (percentage / 100 * barLength);
        int emptyLength = barLength - filledLength;

        // Create the progress bar string
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < filledLength; i++) {
            progressBar.append("#");
        }
        for (int i = 0; i < emptyLength; i++) {
            progressBar.append(" ");
        }

        // Print the progress bar with the percentage
        System.out.print("\r[" + progressBar.toString() + "] " + String.format("%.2f", percentage) + "%");
    }

    public static void main(String[] args) {
        keyCrafter("text.txt");
    }
}
