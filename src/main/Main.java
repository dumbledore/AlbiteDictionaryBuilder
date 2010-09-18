/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import org.albite.dictionary.builder.DictBuilder;
import org.albite.dictionary.builder.DictBuilderException;

/**
 *
 * @author albus
 */
public class Main {

    /**
     * Converts a dictionary.xml to dictionary.ald or
     * dictionary.xml + wordlist.txt to dictionary.ald
     *
     * OR
     *
     * Test a builded dictionary.ald for errors
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            showUsage();
            return;
        }

        if (args[0].endsWith(DictBuilder.FILE_EXTENSION)) {
            /* Testing the dict */
            final String dictFileName = args[0];
            try {
                DictBuilder.test(dictFileName);
            } catch (Exception e) {
                System.out.println("Dictionary test failed.");
                System.out.println(e.getMessage());
                throw new RuntimeException("Dictionary test failed.");
            }
        } else {

            final String dictFileName = args[0];
            final String wordsFileName;

            if (args.length > 1) {
                wordsFileName = args[1];
            } else {
                wordsFileName = null;
            }

            try {
                DictBuilder.build(dictFileName, wordsFileName, false);
            } catch (DictBuilderException e) {
                System.out.println("Dictionary build failed.");
                System.out.println(e.getMessage());
                throw new RuntimeException("Dictionary build failed.");
            }
        }
    }

    private static void showUsage() {
        System.out.println("-------------------------");
        System.out.println("Albite Dictionary Builder");
        System.out.println();
        System.out.println("AlbDict.jar dictionary.xml [wordlist.txt]");
        System.out.println("- dictionary.xml is the dictionary itself");
        System.out.println("- wordlist.txt is used if you'd like to include only some words");
        System.out.println();
        System.out.println("Note that both files must be in UTF-8. See the readme for more info.");
        System.out.println();
        System.out.println("One can test a builded dictionary for errors, too:");
        System.out.println("AlbDict.jar dictionary.ald");
        System.out.println();
        System.out.println("A built dictionary is distinguished by the app by its extension: .ald");
    }
}