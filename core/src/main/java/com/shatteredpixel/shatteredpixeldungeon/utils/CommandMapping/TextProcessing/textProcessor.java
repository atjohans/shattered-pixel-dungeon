package com.shatteredpixel.shatteredpixeldungeon.utils.CommandMapping.TextProcessing;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class textProcessor {

    final String[] StopWords = new String[]{"a", "an", "and", "any", "are", "as", "at", "be", "been", "but", "by ", "few", "from", "for", "have", "he", "her", "here", "him", "his", "how", "i", "in", "is", "it", "its", "many", "me", "my", "none", "of", "on ", "or", "our", "she", "some", "the", "their", "them", "there", "they", "that ", "this", "to", "us", "was", "what", "when", "where", "which", "who", "why", "will", "with", "you", "your"};



    private final HashMap<String, String> commonContractions = new HashMap<String, String>() {{
        put("ain't", "am not");
        put("aren't", "are not");
        put("can't", "can not");
        put("could've", "could have");
        put("couldn't", "could not");
        put("didn't", "did not");
        put("don't", "do not");
        put("doesn't", "does not");
        put("hadn't", "had not");
        put("hasn't", "has not");
        put("haven't", "have not");
        put("he'd", "he would");
        put("here's", "here is");
        put("how'd", "how did");
        put("how'll", "how will");
        put("how're", "how are");
        put("I'd", "I would");
        put("I'll", "I will");
        put("I'm", "I am");
        put("I've", "I have");
        put("isn't", "Is not");
        put("it'd", "It would");
        put("it'll", "it will");
        put("it's", "it is");
        put("let's", "let us");
        put("ma'am", "madam");
        put("mayn't", "may not");
        put("may've", "may have");
        put("might've", "might have");
        put("mustn't", "must not");
        put("must've", "must have");
        put("needn't", "need not");
        put("o'clock", "of the clock");
        put("shalln't", "shall not");
        put("shan't", "shall not");
        put("she'll", "she will");
        put("she's", "she is");
        put("she'd", "she had");
        put("should've", "should have");
        put("shouldn't", "should not");
        put("that'll", "that will");
        put("there're", "there are");
        put("there'll", "there will");
        put("they're", "they are");
        put("they've", "they have");
        put("that'd", "that would");
        put("wasn't", "was not");
        put("weren't", "were not");
        put("what'd", "what did");
        put("what've", "what have");
        put("when's", "when is");
        put("where'd", "where did");
        put("where'll", "where will");
        put("which's", "which is");
        put("which've", "which have");
        put("who'll", "who will");
        put("who're", "who are");
        put("who's", "who is");
        put("who've", "who have");
        put("why'd", "why did");
        put("wouldn't", "would not");
        put("won't", "will not");
        put("you've", "you have");
        put("you're", "you are");
    }};


    private static HashMap<String, String> wordToNumber = new HashMap<String, String>();

    static{
        wordToNumber.put("zero", "0");
        wordToNumber.put("one", "1");
        wordToNumber.put("two", "2");

        //speech engine has a hard time determining what "too" to use, most liekly the number given context
        wordToNumber.put("to", "2");
        wordToNumber.put("too", "2");

        wordToNumber.put("three", "3");


        wordToNumber.put("four", "4");
        wordToNumber.put("for", "4");
        wordToNumber.put("fore", "4");


        wordToNumber.put("five", "5");
        wordToNumber.put("six", "6");
        wordToNumber.put("seven", "7");
        wordToNumber.put("eight", "8");
        wordToNumber.put("nine", "9");
        wordToNumber.put("ten", "10");
    }

    private HashMap<String, String> synonymList = new HashMap<String, String>();
    private HashMap<String, String> stemmedSynonymList = new HashMap<String, String>();

    Stemmer stemmer = new Stemmer();

    public textProcessor() {
        synonymList.put("observe", "look");
        synonymList.put("watch", "look");
        synonymList.put("view", "look");
        synonymList.put("inspect", "look");


        synonymList.put("walk", "move");
        synonymList.put("go", "move");
        synonymList.put("travel", "move");
        synonymList.put("run", "move");

        synonymList.put("route", "paths");
        synonymList.put("direction", "paths");
        synonymList.put("directions", "paths");


        synonymList.put("left", "west");
        synonymList.put("right", "east");
        synonymList.put("up", "north");
        synonymList.put("down", "south");


        synonymList.put("fight", "attack");
        synonymList.put("murder", "attack");
        synonymList.put("kill", "attack");
        synonymList.put("punch", "attack");
        synonymList.put("hit", "attack");

        synonymList.put("cute", "shoot");
        synonymList.put("ocean", "potion");

        synonymList.put("pickup", "grab");
        synonymList.put("get", "grab");
        synonymList.put("acquire", "grab");
        synonymList.put("pick", "grab");

        synonymList.put("backpack", "inventory");
        synonymList.put("bag", "inventory");
        synonymList.put("stuff", "inventory");
        synonymList.put("equipment", "inventory");


        //trouble with phonentically similar words
        synonymList.put("or", "door");
        synonymList.put("oar", "door");

        synonymList.put("rap", "rat");
        synonymList.put("ratt", "rat");
        synonymList.put("moo", "move");

        synonymList.put("northwestern", "northwest");
        synonymList.put("northern", "north");
        synonymList.put("northeastern", "northeast");
        synonymList.put("southern", "south");
        synonymList.put("southeastern", "southeast");
        synonymList.put("southwestern", "southwest");
        synonymList.put("western", "west");
        synonymList.put("eastern", "east");

        //generate a list of stemmed keys to compare to
        for (String key : synonymList.keySet()) {
           stemmedSynonymList.put(stemWord(key), synonymList.get(key));
        }
    }



    public ArrayList<String> tokenizeCommand(String input) {

        String[] tokens = input.split("\\s+");

        List<String> tokenList = (List<String>) Arrays.asList(tokens);

        ArrayList<String> tokenArrayList = new ArrayList<String>(tokenList);

        return tokenArrayList;
    }

    public void removeStopWords(ArrayList<String> input) {


        for (String word : StopWords) {
            for (int i = 0; i < input.size(); ++i) {
                if (input.get(i).equals(word)) {
                    input.remove(i);
                }
            }
        }
    }

    public void swapSynonyms(ArrayList<String> input){

        for (int i = 0; i < input.size(); ++i) {

            if (stemmedSynonymList.containsKey(input.get(i))){
                input.set(i,stemmedSynonymList.get(input.get(i)));
            }

        }

    }

    public void expandContractions(ArrayList<String> input) {

        for (int i = 0; i < input.size(); ++i) {
            if (commonContractions.containsKey(input.get(i))) {
                String[] expand = commonContractions.get(input.get(i)).split("\\s+");
                input.set(i, expand[0]);
                input.add(expand[1]);
            }
        }


    }


    public String stemWord(String string){
        char[] wordAsChar = string.toCharArray();
        for (char ch : wordAsChar) {
            stemmer.add(ch);
        }

        stemmer.stem();
        return stemmer.toString();
    }

    public void stemWords(ArrayList<String> input) {

        for (int i = 0; i < input.size(); ++i) {

            input.set(i, stemWord(input.get(i)));

        }

    }

    public void swapNumbers(ArrayList<String> input){
        for (int i = 0; i < input.size(); ++i) {

            if (wordToNumber.containsKey(input.get(i))){
                input.set(i,wordToNumber.get(input.get(i)));
            }

        }
    }

    public void mergeDirections(ArrayList<String> input){
        if (input.contains("north") && input.contains("west")){

            input.remove("north");
            input.remove("west");
            input.add("northwest");
        }
        if (input.contains("north") && input.contains("east")){

            input.remove("north");
            input.remove("east");
            input.add("northeast");
        }
        if (input.contains("south") && input.contains("west")){

            input.remove("south");
            input.remove("west");
            input.add("southwest");
        }
        if (input.contains("south") && input.contains("east")){

            input.remove("south");
            input.remove("east");
            input.add("southeast");
        }
    }

    public ArrayList<String> processCommand(String command) {
        //command = command.substring(0, command.length() - 1);
        command = command.toLowerCase(Locale.ROOT);
        ArrayList<String> returnArray = tokenizeCommand(command);
        //stemWords(returnArray);
        swapSynonyms(returnArray);
        swapNumbers(returnArray);
        expandContractions(returnArray);
        removeStopWords(returnArray);
        mergeDirections(returnArray);
        return returnArray;
    }

}
