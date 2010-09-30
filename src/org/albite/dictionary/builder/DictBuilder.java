/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.albite.dictionary.builder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.kxml2.kdom.*;
import org.kxml2.io.*;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author albus
 */
public class DictBuilder {

    public static final String FILE_EXTENSION = ".ald";
    public static final int MAGIC_NUMBER = 1095516740;

    /**
     * Converts a dictionary.xml to dictionary.ald or
     * dictionary.xml + wordlist.txt to dictionary.ald
     *
     * @param dictFile
     * @param wordsFile
     */
    public static String build (final String dictFileName,
            final String wordsFileName, final boolean debug)
            throws DictBuilderException {

        //check if dict file exists
        File dictFile = new File(dictFileName);

        if (!dictFile.exists() || !dictFile.isFile()) {
            throw new DictBuilderException(
                    "Dictionary file " + dictFileName + " does not exists!");
        }

        //get list of words, if such is available
        File wordsFile = null;

        if (wordsFileName != null) {

            //check if words file exists
            wordsFile = new File(wordsFileName);

            if (!wordsFile.exists() || !wordsFile.isFile()) {
                throw new DictBuilderException("Wordlist file "
                        + wordsFileName + " does not exists!");
            }
        }

        List<DictEntry> dictionary = new ArrayList<DictEntry>();
        String dictionaryTitle = "Untitled Dictionary";
        int dictionaryLanguage = Languages.LANG_UNKNOWN;

        if (wordsFile != null) {
            //fill words in dictionary from list
            System.out.println(
                    "Processing words list: " + wordsFile.getName() + "...");
            try {
                InputStream in = new FileInputStream(wordsFile);
                try {
                    BufferedReader bufin = new BufferedReader(
                            new InputStreamReader(in, "UTF-8"));
                    String word;
                    int i = 0;
                    while ( (word = bufin.readLine()) != null) {
                        dictionary.add(
                                new DictEntry(word.toLowerCase().trim()));
                        i++;
                    }

                    System.out.println(i + " words found.");
                } finally {
                    in.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new DictBuilderException();
            }
        }

        //fill entries
        try {
            InputStream f = new FileInputStream(dictFile);

                try {
                KXmlParser parser = null;
                Document doc = null;
                Element eDict;
                Element eEntry;
                Element eWord;
                Element eDefinition;

                try {
                    parser = new KXmlParser();
                    parser.setInput(new InputStreamReader(f));

                    doc = new Document();
                    doc.parse(parser);
                    parser = null;
                } catch (XmlPullParserException xppe) {
                    parser = null;
                    doc = null;
                    throw new DictBuilderException(
                            "Malformed XML document. You need to have all &"
                            + "as &amp; for the parser to work.");
                }

                System.out.println(
                        "Processing dictionary: " + dictFile.getName() + "...");

                /*
                 * Root element looks like so
                 * <dictionary name="Webster's Dictionary 1913" language="en">
                 */
                eDict = doc.getRootElement();

                {
                    String s;

                    s = eDict.getAttributeValue(KXmlParser.NO_NAMESPACE,
                            "title");
                    if (s != null) {
                        dictionaryTitle = s;
                    }

                    s = eDict.getAttributeValue(KXmlParser.NO_NAMESPACE,
                            "lang");
                    if (s != null) {
                        dictionaryLanguage =
                                Languages.getLanguageIndex(s);
                    }
                }

                if (dictionaryLanguage == Languages.LANG_UNKNOWN) {
                    throw new
                            DictBuilderException(
                            "Dictionary language is invalid or is not set.");
                }

                System.out.println("Dictionary title: " + dictionaryTitle);
                System.out.println("Dictionary language: "
                        + dictionaryLanguage);

                final int childCount = eDict.getChildCount();
                for (int i = 0; i < childCount; i++) {

                    /*
                     * <entry>
                     */
                    Object o = eDict.getChild(i);
                    if (o instanceof Element) {
                        Element e = (Element) o;
                        if (e.getName().equals("entry")) {
                            String word = getElementString(e, "word");
                            String definition = getElementString(e,
                                    "definition");

                            if (word != null && definition != null) {

                                word = word.toLowerCase().trim();

                                int index = -1;

                                for (int j = 0; j < dictionary.size(); j++) {
                                    if (dictionary.get(j).equals(word)) {
                                        index = j;
                                        break;
                                    }
                                }

                                if (index == -1) {
                                    /*
                                     * word not in the dictionary; add it only,
                                     * if there is no list of filtering words
                                     */

                                    if (wordsFile == null) {
                                        dictionary.add(
                                                new DictEntry(
                                                word, definition));
                                    }
                                } else {
                                    /*
                                     * word already in dictionary; do we need to
                                     * append the definition or overwrite it?
                                     */
                                    DictEntry oldEntry = dictionary.get(index);
                                    if (oldEntry.definition == null) {
                                        /*
                                         * overwrite its value
                                         */
                                        oldEntry.definition = definition;
                                    } else {
                                        /*
                                         * append the new definition
                                         */
                                        oldEntry.definition += "\n\n"
                                                + definition;
                                    }
                                }
                            }
                        }
                    }
                    System.out.println(i + "/" + childCount);
                }
            } finally {
                f.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

       /*
        * Sort the entries by word
        */
        System.out.print("Sorting the index...");
        Collections.sort(dictionary);
        System.out.println("done");

        /*
         * Prepare output filename
         */
        String fileOutName = dictFile.getPath().substring(0,
                dictFile.getPath().lastIndexOf('.')) + FILE_EXTENSION;

        System.out.println("Writing file: " + fileOutName + "...");

        RandomAccessFile out;

        try {
            out = new RandomAccessFile(fileOutName, "rw");
            try {

                /*
                 * Write header
                 */
                 out.writeInt(MAGIC_NUMBER);
                 out.writeUTF(dictionaryTitle);
                 out.writeShort(dictionaryLanguage);

                 /*
                  * Store the current position in the stream.
                  * It shows where the header ends, and the
                  * skip value will be added. We'll need
                  * to seek back to it at the end
                  */
                 final int headerEnd = (int) out.getFilePointer();
                 if (debug) {
                     System.out.println("Header ends at: " + headerEnd);
                 }

                 /*
                  * Write dummy value that will be rewritten later.
                  * It will show the amount necessary to be skipped so
                  * that the stream would be ready to read the word index
                  */
                 out.writeInt(0);

                 /*
                  * write the definitions data
                  */
                 System.out.println("Writing definitions...");

                 for (int i = 0; i < dictionary.size(); i++) {
                     DictEntry e = dictionary.get(i);

                     /*
                      * Skip empty entries
                      */
                     if (e.definition != null) {
                         e.position = (int) out.getFilePointer();
                         out.writeUTF(e.definition);
                     }
                 }

                 /*
                  * Store the current position in the stream.
                  * It shows where the word index starts.
                  */
                 final int wordIndexPosition = (int) out.getFilePointer();
                 if (debug) {
                     System.out.println("Word index at: " + wordIndexPosition);
                 }

                 /*
                  * write a dummy number, which will be
                  * the number of word entries
                  */
                 out.writeInt(0);

                 /*
                  * write the word index
                  */
                 System.out.println("Writing word index...");

                 int wordsActuallyWritten = 0;

                 for (int i = 0; i < dictionary.size(); i++) {
                     DictEntry e = dictionary.get(i);

                     /*
                      * Skip empty entries
                      */
                     if(e.definition != null) {
                         out.writeUTF(e.word);
                         out.writeInt(e.position);

                         if (debug) {
                             System.out.println("Writing word: " + e.word);
                         }

                         wordsActuallyWritten++;
                     }
                 }

                 /*
                  * Seek to the place, we wrote the skip value
                  */
                 out.seek(headerEnd);

                 /*
                  * write the amount of bytes to be skipped so that
                  * the pointer would be exactly at the beginning of
                  * the word index. This is an absolute value from the start
                  * of the file.
                  */
                 final int skip = wordIndexPosition;
                 out.writeInt(skip);

                 if (debug) {
                     System.out.println(
                             "Skip value: " + (skip));
                 }

                 /*
                  * Seek to the place, where we'll write the number of words
                  */
                 out.seek(wordIndexPosition);
                 out.writeInt(wordsActuallyWritten);

                 System.out.println("Dictionary enties written: "
                         + wordsActuallyWritten);

                 System.out.println("Dictionary created successfully.");

                 return fileOutName;
            } finally {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new DictBuilderException();
        }
    }

    private static String getElementString(Element root, String name) {
        int index = root.indexOf(KXmlParser.NO_NAMESPACE, name, 0);

        if (index != -1) {
            Object kid = root.getChild(index);
            if (kid instanceof Element) {
                return ((Element) kid).getText(0);
            }
        }

        return null;
    }


    /**
     * Test the dictionary, through reading all the entries.
     * @param file
     * @throws IOException
     * @throws DictBuilderException
     */
    public static void test(final String fileName)
            throws IOException, DictBuilderException {

        final File file = new File(fileName);

        System.out.println("Testing dictionary: " + file.getName() + "...");

        DataInputStream in
                = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)));

        in.mark(Integer.MAX_VALUE);

        if (in.readInt() != DictBuilder.MAGIC_NUMBER) {
            throw new DictBuilderException("Magic number is wrong");
        }

        String title = in.readUTF();
        System.out.println("Title: " + title);

        int language = in.readShort();
        System.out.println("Language: " + language);

        /*
         * Skip value from the BEGINNING!
         */
        int skip = in.readInt();

        in.reset();

        System.out.println("Skipping " + skip);
        int skipped = (int) in.skipBytes(skip);
        System.out.println("Skipped " + skipped);

        final int wordsCount = in.readInt();
        System.out.println("words count: " + wordsCount);
        Map<String, Integer> entries = new HashMap<String, Integer>(wordsCount);

        for (int i = 0; i < wordsCount; i++) {
            System.out.print("Reading entry #" + (i + 1));

            String word = in.readUTF();
            System.out.print(", " + word);

            int position = in.readInt();
            System.out.println(", " + position);

            entries.put(word, position);
        }

        Set words = entries.keySet();
        Iterator it = words.iterator();

        while (it.hasNext()) {
            String word = (String) it.next();
            int position = entries.get(word).intValue();
            in.reset();
            in.skip(position);
            String definition = in.readUTF();

            System.out.println(
                    "Word: " + word + ", (" + definition.length() + ")");
        }

        System.out.println("Dictionary tested successfully.");
    }
}
