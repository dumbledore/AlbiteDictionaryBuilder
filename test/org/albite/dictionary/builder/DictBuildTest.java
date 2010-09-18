/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.albite.dictionary.builder;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author albus
 */
public class DictBuildTest extends TestCase {

    public void testDictEntry() {
        final String w1 = "flabbergasted";
        final DictEntry e1 = new DictEntry(w1);

        assertTrue(e1.equals(e1));
        assertEquals(e1.compareTo(e1), 0);

        assertTrue(e1.equals(w1));
        assertEquals(e1.compareTo(w1), 0);

        final String w2 = "crancky";
        final DictEntry e2 = new DictEntry(w2);

        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(w1));
        assertTrue(e1.compareTo(e2) > 0);
        assertTrue(e2.compareTo(e1) < 0);

        final String w3 = "peckish";
        final DictEntry e3 = new DictEntry(w3);

        assertFalse(e3.equals(e1));
        assertFalse(e1.equals(w3));
        assertTrue(e1.compareTo(e3) < 0);
        assertTrue(e3.compareTo(e1) > 0);
    }

    public void testDictBuildWithoutWordlist()
            throws DictBuilderException, IOException {

        System.out.println("TEST: Dictionary without a wordlist");
        System.out.println("-----------------------------------");

        final String dictFileName = "./test/res/original/wb1913-test-1.xml";
        String f = DictBuilder.build(dictFileName, null, true);

        DictBuilder.test(f);

        System.out.println();
    }

    public void testDictBuild()
            throws DictBuilderException, IOException {

        System.out.println("TEST: Dictionary filtered WITH a wordlist");
        System.out.println("-----------------------------------------");

        final String dictFileName = "./test/res/original/wb1913-test-2.xml";
        final String wordsFileName = "./test/res/original/3esl.txt";
        String f = DictBuilder.build(dictFileName, wordsFileName, true);

        DictBuilder.test(f);

        System.out.println();
    }
}
