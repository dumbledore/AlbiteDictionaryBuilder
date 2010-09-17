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
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            showUsage();
            return;
        }

        String dictFileName = args[0];
        String wordsFileName = null;

        if (args.length > 1) {
            wordsFileName = args[1];
        }

        try {
            DictBuilder.build(dictFileName, wordsFileName, false);
        } catch (DictBuilderException e) {
            System.out.println("Dictionary build failed.");
            System.out.println(e.getMessage());
            throw new RuntimeException("Dictionary build failed.");
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
    }
}