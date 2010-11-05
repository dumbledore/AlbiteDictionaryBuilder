/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.albite.dictionary.builder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
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
import java.io.UTFDataFormatException;
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
        String dictionaryLanguage = "Unknown";

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
                    parser.setInput(new InputStreamReader(f, "UTF-8"));

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
                        dictionaryLanguage = s;
                    }
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
                 writeUTF(dictionaryTitle, out);
                 writeUTF(dictionaryLanguage, out);

                 /*
                  * Store the current position in the stream.
                  * It shows where the header ends, and the
                  * skip value will be added. We'll need
                  * to seek back to it at the end
                  */
                 final int headerEnd = ((int) out.getFilePointer()) + 4;

                 if (debug) {
                     System.out.println("Header ends at: " + headerEnd);
                 }

                 /*
                  * Header end = Index start
                  */
                 out.writeInt(headerEnd);

                 /*
                  * Dummy number of entries
                  */
                 out.writeInt(0);

                 /*
                  * Writing dummy index
                  */
                 System.out.println("Writing index...");
                 int entriesCount = writeIndex(out, dictionary, debug);

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
                         writeUTF(e.definition, out);
                     }
                 }

                 out.seek(headerEnd);
                 /*
                  * REwrite the word index
                  */
                 System.out.println("Re-writing word index...");
                 out.writeInt(entriesCount);

                 writeIndex(out, dictionary, debug);

                 System.out.println("Dictionary enties written: "
                         + entriesCount);

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

    private static int writeIndex(
            final RandomAccessFile out,
            final List<DictEntry> dictionary,
            final boolean debug) throws IOException {
         int wordsActuallyWritten = 0;

         for (int i = 0; i < dictionary.size(); i++) {
             DictEntry e = dictionary.get(i);

             /*
              * Skip empty entries
              */
             if(e.definition != null) {
                 writeUTF(e.word, out);
                 out.writeInt(e.position);

                 if (debug) {
                     System.out.println("Writing word: " + e.word);
                 }

                 wordsActuallyWritten++;
             }
         }

         return wordsActuallyWritten;
    }

    /*
     * There was a problem (i.e. writing data wrongly) with the current
     * writeUTF function so I am using the one from java 1.3
     */
    static int writeUTF(String str, DataOutput out) throws IOException {
	int strlen = str.length();
	int utflen = 0;
 	char[] charr = new char[strlen];
	int c, count = 0;

	str.getChars(0, strlen, charr, 0);

	for (int i = 0; i < strlen; i++) {
	    c = charr[i];
	    if ((c >= 0x0001) && (c <= 0x007F)) {
		utflen++;
	    } else if (c > 0x07FF) {
		utflen += 3;
	    } else {
		utflen += 2;
	    }
	}

	if (utflen > 65535) {
	    throw new UTFDataFormatException();
        }

	byte[] bytearr = new byte[utflen+2];
	bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
	bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);
	for (int i = 0; i < strlen; i++) {
	    c = charr[i];
	    if ((c >= 0x0001) && (c <= 0x007F)) {
		bytearr[count++] = (byte) c;
	    } else if (c > 0x07FF) {
		bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
		bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
		bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
	    } else {
		bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
		bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
	    }
	}
        out.write(bytearr);

	return utflen + 2;
    }

    public final static String readUTF(DataInput in) throws IOException {
        int utflen = in.readUnsignedShort();
        StringBuffer str = new StringBuffer(utflen);
        byte bytearr [] = new byte[utflen];
        int c, char2, char3;
	int count = 0;

 	in.readFully(bytearr, 0, utflen);

	while (count < utflen) {
     	    c = (int) bytearr[count] & 0xff;
	    switch (c >> 4) {
	        case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
		    /* 0xxxxxxx*/
		    count++;
                    str.append((char)c);
		    break;
	        case 12: case 13:
		    /* 110x xxxx   10xx xxxx*/
		    count += 2;
		    if (count > utflen)
			throw new UTFDataFormatException();
		    char2 = (int) bytearr[count-1];
		    if ((char2 & 0xC0) != 0x80)
			throw new UTFDataFormatException();
                    str.append((char)(((c & 0x1F) << 6) | (char2 & 0x3F)));
		    break;
	        case 14:
		    /* 1110 xxxx  10xx xxxx  10xx xxxx */
		    count += 3;
		    if (count > utflen)
			throw new UTFDataFormatException();
		    char2 = (int) bytearr[count-2];
		    char3 = (int) bytearr[count-1];
		    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
			throw new UTFDataFormatException();
                    str.append((char)(((c     & 0x0F) << 12) |
                    	              ((char2 & 0x3F) << 6)  |
                    	              ((char3 & 0x3F) << 0)));
		    break;
	        default:
		    /* 10xx xxxx,  1111 xxxx */
		    throw new UTFDataFormatException();
		}
	}
        // The number of chars produced may be less than utflen
        return new String(str);
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

        String title = readUTF(in);
        System.out.println("Title: " + title);

        String language = readUTF(in);
        System.out.println("Language: " + language);

        /*
         * Skip value from the BEGINNING!
         */
        int skip = in.readInt();

        in.reset();
        in.skipBytes(skip);

        final int wordsCount = in.readInt();
        System.out.println("words count: " + wordsCount);
        Map<String, Integer> entries = new HashMap<String, Integer>(wordsCount);

        for (int i = 0; i < wordsCount; i++) {
            System.out.print("Reading entry #" + (i + 1));

            String word = readUTF(in);
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
            in.skipBytes(position);
            String definition = readUTF(in);

            System.out.println("Reading `" + word + "` @ " + position +
                    " (" + definition.length() + ")");
        }

        System.out.println("Dictionary tested successfully.");
    }
}