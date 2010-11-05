Usage
-----

    java -jar AlbDict.jar dictionary.xml *[wordlist.txt]*

* dictionary.xml is the dictionary itself
* wordlist.txt is used if you'd like to include only some words

*Note that both files must be in UTF-8.*

One can test a builded dictionary for errors, too:

    AlbDict.jar dictionary.ald

A built dictionary is distinguished by the app by its extension: **.ald**

The *\*.xml* file, containing the word definitons
-------------------------------------------------

    <dictionary title="Test" lang="en">
        <entry>
            <word>Wireworm</word>
            <definition>(n.) A galleyworm.</definition>
        </entry>
        <entry>
            <word>Wiriness</word>
            <definition>(n.) The quality of being wiry.</definition>
        </entry>
        <entry>
            <word>Wiry</word>
            <definition>(a.) Made of wire; like wire; drawn out like wire.</definition>
        </entry>
        <entry>
            <word>Wiry</word>
            <definition>(a.) Capable of endurance; tough; sinewy; as, a wiry frame or constitution.</definition>
        </entry>
        <entry>
            <word>Wis</word>
            <definition>(adv.) Certainly; really; indeed.</definition>
        </entry>
        <entry>
            <word>Wis</word>
            <definition>(v. t.) To think; to suppose; to imagine; – used chiefly in the first person sing. present tense, I wis.  See the Note under Ywis.</definition>
        </entry>
        <entry>
            <word>Wisard</word>
            <definition>(n.) See Wizard.</definition>
        </entry>
    </dictionary>

Multiple entries of the same word are automatically mixed into one.
	
The *\*.txt* wordlist file
--------------------------

It's just a plain text file, consisting of a list of words put on separate lines.
If a word definition from the *xml* file is not contained in this list, it won't
be added to the dictionary. So, it works like a filter.

The Binary Dictionary File (*\*.ald*)
-------------------------------------

**Main overview**

<table border="0" cellpadding="5">
  <thead>
    <tr>
      <th>Meaning</th>
      <th>Type</th>
      <th>Bytes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Magic number</td>
      <td>Integer</td>
      <td>4</td>
    </tr>
    <tr>
      <td>Dictionary title</td>
      <td>utf-8</td>
      <td>varies</td>
    </tr>
    <tr>
      <td>Dictionary language</td>
      <td>utf-8</td>
      <td>varies</td>
    </tr>
    <tr>
      <td>Position of word index</td>
      <td>Integer</td>
      <td>4</td>
    </tr>
    <tr>
      <td>Number of entries</td>
      <td>Integer</td>
      <td>4</td>
    </tr>
    <tr>
      <td>Entries' index blocks</td>
      <td>Block of utf-8 values and integers</td>
      <td>varies</td>
    </tr>
    <tr>
      <td>Entries' defintions blocks</td>
      <td>Block of utf-8 values</td>
      <td>varies</td>
    </tr>
  </tbody>
</table>


**Index block**

<table border="0" cellpadding="5">
  <thead>
    <tr>
      <th>Meaning</th>
      <th>Type</th>
      <th>Bytes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Word</td>
      <td>utf-8</td>
      <td>varies</td>
    </tr>
    <tr>
      <td>Definition position</td>
      <td>Integer</td>
      <td>4</td>
    </tr>
  </tbody>
</table>
