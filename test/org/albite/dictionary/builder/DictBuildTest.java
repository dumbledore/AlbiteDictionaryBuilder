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

    private static final String folder = "./test/res/original/";

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

    public void testDicts()
        throws DictBuilderException, IOException {

        testDict("beg.xml", false);
        testDict("beg-filtered.xml", true);

        testDict("mid.xml", false);
        testDict("mid-filtered.xml", true);

        testDict("end.xml", false);
        testDict("end-filtered.xml", true);
    }

    private void testDict(
            final String fileName, final boolean filtered)
            throws DictBuilderException, IOException {

        final String dictFileName = folder + fileName;
        final String wordsFileName = (filtered ? folder + "3esl.txt" : null);

        System.out.println("TEST: " + fileName);
        System.out.println("-----------------------------------------");

        String f = DictBuilder.build(dictFileName, wordsFileName, true);

        DictBuilder.test(f);

        System.out.println();
    }
}
