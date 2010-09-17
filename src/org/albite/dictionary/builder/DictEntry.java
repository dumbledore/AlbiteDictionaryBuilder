/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.albite.dictionary.builder;

/**
 *
 * @author albus
 */
public class DictEntry implements Comparable {
    public final String word;
    public String definition = null;
    public int position = -1;

    public DictEntry(final String word) {
        this.word = word;
    }

    public DictEntry(final String word, final String definition) {
        this.word = word;
        this.definition = definition;
    }

    public int compareTo(Object o) {

        if (o instanceof String) {
            String s = (String) o;
            return word.compareTo(s);
        }

        if (o instanceof DictEntry) {
            DictEntry e = (DictEntry) o;
            return word.compareTo(e.word);
        }

        return -1;
    }

    public boolean equals(Object o) {

        if (o instanceof String) {
            String s = (String) o;
            return word.equals(s);
        }

        if (o instanceof DictEntry) {
            DictEntry e = (DictEntry) o;
            return word.equals(e.word);
        }

        return false;
    }
}
