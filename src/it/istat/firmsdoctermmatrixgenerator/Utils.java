package it.istat.firmsdoctermmatrixgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.log4j.Logger;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.danishStemmer;
import org.tartarus.snowball.ext.dutchStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.finnishStemmer;
import org.tartarus.snowball.ext.frenchStemmer;
import org.tartarus.snowball.ext.germanStemmer;
import org.tartarus.snowball.ext.hungarianStemmer;
import org.tartarus.snowball.ext.italianStemmer;
import org.tartarus.snowball.ext.norwegianStemmer;
import org.tartarus.snowball.ext.portugueseStemmer;
import org.tartarus.snowball.ext.romanianStemmer;
import org.tartarus.snowball.ext.russianStemmer;
import org.tartarus.snowball.ext.spanishStemmer;
import org.tartarus.snowball.ext.swedishStemmer;
import org.tartarus.snowball.ext.turkishStemmer;

/**
* @author  Donato Summa
*/
public class Utils {
	
	static Logger logger = Logger.getLogger(Utils.class);
	
	public static List<String> getList1PlusList2(List<String> list1, List<String> list2){
		Set<String> finalSet = new ConcurrentSkipListSet<String>();
		finalSet.addAll(list1);
		finalSet.addAll(list2);
		List<String> finalList = new ArrayList<String>(finalSet);
		Collections.sort(finalList);
		return finalList;
	}
	
	public static List<String> getList1MinusList2(List<String> list1, List<String> list2){
		Set<String> finalSet = new ConcurrentSkipListSet<String>();
		finalSet.addAll(list1);
		finalSet.removeAll(list2);
		List<String> finalList = new ArrayList<String>(finalSet);
		Collections.sort(finalList);
		return finalList;
	}
	
	public static List<String> getWordListFromFile(String filePath) {
		
		if(filePath==null){
			logger.warn("filePath received as input is null ! The returned wordList will be empty !");
			return new ArrayList<String>();
		}
		
		Set<String> wordSet = new ConcurrentSkipListSet<String>();
		try {
			
			FileInputStream fis = new FileInputStream(filePath);
			InputStream is = fis;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String strLine;
			
			//br.readLine(); // avoid the first line with headers
			while ((strLine = br.readLine()) != null) {
				wordSet.add(strLine);
			}
			
			br.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
		List<String> wordList = new ArrayList<String>(wordSet);
		Collections.sort(wordList);
		return wordList;
	}
	
	public static boolean isAValidFile(String filePathString) {
		File f = new File(filePathString);
		if(f.exists() && !f.isDirectory()) { 
			return true;
		}
		return false;
	}
	
	public static boolean isAValidDirectory(String dirPathString) {
		File f = new File(dirPathString);
		if(f.exists() && f.isDirectory()) { 
			return true;
		}
		return false;
	}
	
	public static String getDateTimeAsString(){
		
		Date nowDateTime = new Date();
		DateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return formatter.format(nowDateTime);
		
	}
	
	public static List<String> getItalianStemmedWordsList(List<String> wordsToBeStemmedList) {
		List<String> stemmedWordsList = new ArrayList<String>();
		SnowballStemmer stemmer = (SnowballStemmer) new italianStemmer();

        for (String s : wordsToBeStemmedList){
        	stemmer.setCurrent(s);
            stemmer.stem();
            String stemmed = stemmer.getCurrent();
            //System.out.println(stemmed);
            stemmedWordsList.add(stemmed);
        }
        
		return stemmedWordsList;
	}

	public static SnowballStemmer getStemmer(String language) {
		SnowballStemmer stemmer;
		
		switch(language) {
		    case "DAN":
		    	stemmer = (SnowballStemmer) new danishStemmer();
		    	break;
		    case "DUT":
		    	stemmer = (SnowballStemmer) new dutchStemmer();
		    	break;
		    case "ENG":
		    	stemmer = (SnowballStemmer) new englishStemmer();
		    	break;
		    case "FIN":
		    	stemmer = (SnowballStemmer) new finnishStemmer();
		    	break;
		    case "FRE":
		    	stemmer = (SnowballStemmer) new frenchStemmer();
		    	break;
		    case "GER":
		    	stemmer = (SnowballStemmer) new germanStemmer();
		    	break;
		    case "HUN":
		    	stemmer = (SnowballStemmer) new hungarianStemmer();
		    	break;
		    case "ITA":
		    	stemmer = (SnowballStemmer) new italianStemmer();
		    	break;
		    case "NOR":
		    	stemmer = (SnowballStemmer) new norwegianStemmer();
		    	break;
		    case "POR":
		    	stemmer = (SnowballStemmer) new portugueseStemmer();
		    	break;
		    case "ROM":
		    	stemmer = (SnowballStemmer) new romanianStemmer();
		    	break;
		    case "RUS":
		    	stemmer = (SnowballStemmer) new russianStemmer();
		    	break;
		    case "SPA":
		    	stemmer = (SnowballStemmer) new spanishStemmer();
		    	break;
		    case "SWE":
		    	stemmer = (SnowballStemmer) new swedishStemmer();
		    	break;
		    case "TUR":
		    	stemmer = (SnowballStemmer) new turkishStemmer();
		    	break;
		    default:
		    	stemmer = null;
		    	logger.info("The language string \"" + language + "\" set as FIRST_LANG or SECOND_LANG is not valid !");
		    	logger.info("The program will be terminated !");
		    	System.exit(0);
		    	break;
		}
		return stemmer;
	}
	
}
