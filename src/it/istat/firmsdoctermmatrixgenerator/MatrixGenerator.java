package it.istat.firmsdoctermmatrixgenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.annolab.tt4j.ExecutableResolver;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.italianStemmer;

/**
* @author  Donato Summa
*/
public class MatrixGenerator {

	static Logger logger = Logger.getLogger(MatrixGenerator.class);
	private IndexReader reader;
	private IndexSearcher searcher;	
	private String csvCreationDateTime;
	private String csvFullPathName;
	private String goWordsFileFullPath;
	private String stopWordsFileFullPath;
	private List<String> sortedFirmIdsList;
	private List<String> termList;
	private int numOfFirms = 0;
	private int numOfWords = 0;
	private static Map<String,Integer> firmTermsCountMap;
	private static Map<String,String> normalStemmedMap;
	private File indexDirectory;
	private static Set<String> wordNotRecognized = new ConcurrentSkipListSet<String>();
	private List<String> goWordsList = new ArrayList<String>();
	private List<String> stopWordsList = new ArrayList<String>();
	
	// config parameters to be read from conf.properties input file at runtime
	private static String logFilePath;
	private static int MAX_RESULTS;
	private static String MATRIX_FILE_FOLDER;
	private static String SOLR_INDEX_DIRECTORY_PATH;	
	private static String TREE_TAGGER_EXE_FILEPATH;
	private static String FIRST_LANG_PAR_FILE_PATH;
	private static String SECOND_LANG_PAR_FILE_PATH;
	private static String FIRST_LANG;
	private static String SECOND_LANG;
	
	public MatrixGenerator(String[] args) throws IOException {
		this.numOfFirms = 0;
		this.numOfWords = 0;
		this.sortedFirmIdsList = new ArrayList<String>();
		this.termList = new ArrayList<String>();
		this.firmTermsCountMap = new HashMap<String, Integer>();
		this.normalStemmedMap = new HashMap<String, String>();
		configure(args);
		File indexDirectory = new File(SOLR_INDEX_DIRECTORY_PATH);
		reader = IndexReader.open(FSDirectory.open(indexDirectory));
		searcher = new IndexSearcher(reader);
	}

	public void generateMatrix() throws IOException {
		
		//Term termInstance = new Term("corpoPagina", "fucinati");
        //long termFreq = reader.totalTermFreq(termInstance);
        //long docCount = reader.docFreq(termInstance);
		csvCreationDateTime = Utils.getDateTimeAsString();
        csvFullPathName = MATRIX_FILE_FOLDER + "tdmatrix_" + csvCreationDateTime + ".csv";
		FileWriter fw = new FileWriter(csvFullPathName);
		BufferedWriter bw = new BufferedWriter(fw);
		
		// extract all the tokens in the fields of all documents in the index (only fields that i'm interested in) & print them in a file		
		
		//List<String> termsList = getIndexSortedListOfTerms(); // considering ALL terms including "ahvnsdipfjbvnduoybsefgwefu"		
		
		logger.info("getting the reduced, stemmed, sorted, indexed list of terms from documents contained in Solr");
		termList = getReducedStemmedSortedIndexedListOfTerms(); // considering JUST A PORTION of lemmed terms
		logger.info("list of reduced, stemmed, sorted, indexed list of terms acquired");
		
		logger.info("loading goWordListFile . . .");
		goWordsList = Utils.getWordListFromFile(goWordsFileFullPath);
		logger.info("goWordListFile loaded");
		
		logger.info("loading stopWordListFile . . .");
		stopWordsList = Utils.getWordListFromFile(stopWordsFileFullPath);
		logger.info("stopWordListFile loaded");
		
		logger.info("merging the normalStemmedMap with the goWordsList");
		for (String s : goWordsList){	normalStemmedMap.put(s, s);	}
		logger.info("normalStemmedMap merged with the goWordsList");
		
		logger.info("merging the normalStemmedMap with the stopWordsList");
		for (String s : stopWordsList){	normalStemmedMap.remove(s);	}		
		logger.info("normalStemmedMap merged with the stopWordsList");
		
		logger.info("merging the termList with the goWordsList");
		termList = Utils.getList1PlusList2(termList, goWordsList); // Add the words contained in goWordsList to the termList
		logger.info("termList merged with the goWordsList");
		
		logger.info("merging the termList with the stopWordsList");
		termList = Utils.getList1MinusList2(termList, stopWordsList); // Subtract the words contained in stopWordsList from the termList
		logger.info("termList merged with the stopWordsList");
		
		printFirstLineContainingSortedSet(termList, bw);
		logger.info("first line containing word sorted set printed on file");
		
		// acquire all the codiciAzienda (firmIds) present in Solr e sort them alphabetically
		logger.info("getting sorted list of firm ids");
		sortedFirmIdsList = getSortedListOfFirmIds();
		this.numOfWords = termList.size();
		this.numOfFirms = sortedFirmIdsList.size();
		logger.info("sorted list of firm ids acquired");
		logger.info("");
		logger.info("number of rows (firms) = " + numOfFirms);
		logger.info("number of columns (words) = " + numOfWords);
		logger.info("");
		
		// for each firm get all the tokens from all the fields in every document, count the occurrencies and print the line in the file		
		logger.info("Creation of the TD matrix started");		
		for (String firmId : sortedFirmIdsList){
			
			bw.write(firmId + "\t");	
			
			firmTermsCountMap = new HashMap<String, Integer>();
			firmTermsCountMap = getFirmTermsCountMap(firmId); 
			printFirmLine(termList, firmTermsCountMap, bw);
	        
			bw.newLine();
			
		}
		
		bw.close();
        fw.close();
        logger.info("TDMatrix printed on file");
        
//        for (String word : wordNotRecognized){
//        	logger.warn(word);
//        }
        
	}

	private List<String> getIndexSortedListOfTerms() throws IOException {
		
		Set<String> termsSet = new ConcurrentSkipListSet<String>();
		Fields fields = MultiFields.getFields(reader);   
		for(String field : fields) {    
        	switch (field.toString()) {
            case "imgsrc":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "imgalt":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "links":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "ahref":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "aalt":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "inputvalue":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "inputname":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "metatagDescription":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "metatagKeywords":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "title":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            case "corpoPagina":
            	termsSet = getFieldTermsAddedToSet(fields,field,termsSet);
                break;
            }        	        	
        }
		
		List<String> termsList = new ArrayList<String>(termsSet);
		Collections.sort(termsList);
		return termsList;
		
	}

	private List<String> getReducedStemmedSortedIndexedListOfTerms() throws IOException {
		
		List<String> fullTermsList = new ArrayList<String>();
		fullTermsList = getIndexSortedListOfTerms(); // load the full term list
		Set<String> termSet = new ConcurrentSkipListSet<String>();
		
		// removing words having less than 3 or more than 24 characters
		for (String word : fullTermsList){
			if (word.length() > 2 && word.length() < 25){
				termSet.add(word); 
			}
		}
		
		//InputStream inputStream = MatrixGenerator.class.getClassLoader().getResourceAsStream("conf.properties");//se si vuole leggere il file di properties nel jar
		
		// removing words not existing in firstLanguage and in secondLanguage
		Set<String> firstLangStemmedTermSet = new ConcurrentSkipListSet<String>();
		Set<String> secondLangStemmedTermSet = new ConcurrentSkipListSet<String>();
		Set<String> diff_TermSet = new ConcurrentSkipListSet<String>();
		Set<String> firstLang_secondLang_StemmedTermSet = new ConcurrentSkipListSet<String>();
		
		logger.info("building the stemmed termSet of the first language ("+ FIRST_LANG +")");
		firstLangStemmedTermSet = getLanguageSpecificStemmedTermSet(termSet, FIRST_LANG, FIRST_LANG_PAR_FILE_PATH);
		logger.info("stemmed termSet of the first language built");
		
		diff_TermSet = termSet;
		//int size_before = diff_TermSet.size();
		//int normalStemmedMapSize_1 = normalStemmedMap.size(); 
		for (String term : diff_TermSet){
			if (normalStemmedMap.containsKey(term)){
				diff_TermSet.remove(term);
			}
		}
		//int size_after = diff_TermSet.size();
		
		logger.info("building the stemmed termSet of the second language ("+ SECOND_LANG +")");
		secondLangStemmedTermSet = getLanguageSpecificStemmedTermSet(diff_TermSet, SECOND_LANG, SECOND_LANG_PAR_FILE_PATH);
		logger.info("stemmed termSet of the second language built");
		
		firstLang_secondLang_StemmedTermSet.addAll(firstLangStemmedTermSet);
		firstLang_secondLang_StemmedTermSet.addAll(secondLangStemmedTermSet);
		//Set<String> overlapTermSet = new ConcurrentSkipListSet<String>();
		//overlapTermSet=englishStemmedTermSet;
		//overlapTermSet.removeAll(italianStemmedTermSet);
		//int size_overlap = overlapTermSet.size();
		//int size_ita = italianStemmedTermSet.size();
		//int size_eng = englishStemmedTermSet.size();
		//int size_ita_eng = ita_en_StemmedTermSet.size();
		//int normalStemmedMapSize_2 = normalStemmedMap.size();
				
		List<String> reducedStemmedTermList = new ArrayList<String>(firstLang_secondLang_StemmedTermSet);
		Collections.sort(reducedStemmedTermList);
		return reducedStemmedTermList;
		
	}

	@SuppressWarnings("unchecked")
	private Set<String> getLanguageSpecificStemmedTermSet(Set<String> fullTermSet, final String language, String languageSpecificParFilePath) {
		//final SnowballStemmer firstLangStemmer = (SnowballStemmer) new italianStemmer();
		final SnowballStemmer firstLangStemmer = Utils.getStemmer(FIRST_LANG);
		//final SnowballStemmer secondLangStemmer = (SnowballStemmer) new englishStemmer();
		final SnowballStemmer secondLangStemmer = Utils.getStemmer(SECOND_LANG);
		final Set<String> languageSpecificTermSet = new ConcurrentSkipListSet<String>();
		ExecutableResolver executableResolver = new MyExecutableResolver(TREE_TAGGER_EXE_FILEPATH);		
		TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();
		tt.setExecutableProvider(executableResolver);
		String[] treeTaggerArgs = {"-token", "-lemma"};
		tt.setArguments(treeTaggerArgs);
		
		try {
			
			tt.setModel(languageSpecificParFilePath);
		    tt.setHandler(new TokenHandler<String>() {
		    	public void token(String token, String pos, String lemma) {
		    		//System.out.println(token + "\t" + pos + "\t" + lemma);
		    		if (!lemma.equals("<unknown>")){
		    			if (language.equals(FIRST_LANG)){
		    				firstLangStemmer.setCurrent(lemma.toLowerCase());
		    				firstLangStemmer.stem();
		    				pos = firstLangStemmer.getCurrent();
		    				languageSpecificTermSet.add(pos);	
		    				normalStemmedMap.put(token, pos);
		    			}else if (language.equals(SECOND_LANG)){
		    				secondLangStemmer.setCurrent(lemma.toLowerCase());
		    				secondLangStemmer.stem();
		    				pos = secondLangStemmer.getCurrent();
		    				languageSpecificTermSet.add(pos);
		    				normalStemmedMap.put(token, pos);
		    			}else{
		    				System.out.println("Error : Language not supported !");
		    				System.exit(0);
		    			}
		    		}
		        }
		    });
		    tt.process(fullTermSet);
		    
		} catch (TreeTaggerException | IOException e) {
			
			e.printStackTrace();
						
		} finally {
			
			tt.destroy();
			
		}
		
		return languageSpecificTermSet;
	}

	private List<String> getSortedListOfFirmIds() throws IOException {
		
		Set<String> firmIdsSet = new ConcurrentSkipListSet<String>();
		
		Fields fields = MultiFields.getFields(reader); 
		for(String field : fields) {
			if(field.toString().equals("codiceAzienda")){
				firmIdsSet = getFieldTermsAddedToSet(fields,field,firmIdsSet);
			}
		}
		
		List<String> firmIdsList = new ArrayList<String>(firmIdsSet);
		Collections.sort(firmIdsList);
		return firmIdsList;
		
	}

	private Map<String,Integer> getFirmTermsCountMap(String firmId) throws IOException {
		Map<String,Integer> firmTermsCountMap = new HashMap<String, Integer>();
		TermQuery query;
		TopDocs topdocs;		
		query = new TermQuery(new Term("codiceAzienda", firmId));
        topdocs = searcher.search(query, MAX_RESULTS);       
        
        for(int i = 0 ; i < topdocs.scoreDocs.length ; i++){
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "imgsrc", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "imgalt", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "links", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "ahref", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "aalt", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "inputvalue", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "inputname", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "metatagDescription", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "metatagKeywords", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "title", firmTermsCountMap);
        	firmTermsCountMap = addTermsOfField(topdocs.scoreDocs[i].doc, "corpoPagina", firmTermsCountMap);
        }
		return firmTermsCountMap;
		
	}

	private Map<String,Integer> addTermsOfField(int doc, String field, Map<String,Integer> firmTermsCountMap) throws IOException {
		
		Terms terms;
		BytesRef text;
		TermsEnum termsEnum; 
    	String normalWord = "";
    	String stemmedWord = "";
		terms = reader.getTermVector(doc, field);
		
		if(terms != null){
			termsEnum = terms.iterator(null);    	
	    	
	    	while((text = termsEnum.next()) != null) {
	    		normalWord = text.utf8ToString();
	    		stemmedWord = normalStemmedMap.get(normalWord);
	    		if (stemmedWord == null){
	    			//System.out.println("Word not recognized : " + normalWord);
	    			//logger.warn("Word not recognized : " + normalWord);
	    			wordNotRecognized.add(normalWord);
	    		}else{
	    			if ( firmTermsCountMap.get(stemmedWord) != null ){
		    			firmTermsCountMap.put(stemmedWord, (firmTermsCountMap.get(stemmedWord)+1));
		    		}else{
		    			firmTermsCountMap.put(stemmedWord, 1); //inserisco la entry
		    		}
	    		}	    		
	    	}
    	}else{
			//System.out.println("Problem with the firm " + codAzienda + " doc num " + docNum + " campo " + field);
			//System.out.println("--> field = " + field);
		}
		
		return firmTermsCountMap;
    	
	}

	private Set<String> getFieldTermsAddedToSet(Fields fields, String field, Set<String> set) throws IOException{
		Terms terms; 
        TermsEnum termsEnum; 
        BytesRef text;
        
		terms = fields.terms(field);
        termsEnum = terms.iterator(null);
        
        while((text = termsEnum.next()) != null) {
          System.out.println("field=" + field + "; text=" + text.utf8ToString());
          set.add(text.utf8ToString());
        }
        
        return set;
        
	}
	
	private void printFirstLineContainingSortedSet(List<String> termsList, BufferedWriter bw) throws IOException {
		
		bw.write(" " + "\t");
		for (String s : termsList) {
			bw.write(s + "\t");
		}
		bw.newLine();
		
	}
	
	private void printFirstLineContainingSortedSetOfFirmIds(List<String> sortedFirmIdsList, BufferedWriter bw) throws IOException {
		
		bw.write(" " + "\t");
		for (String s : sortedFirmIdsList) {
			bw.write(s + "\t");
		}
		bw.newLine();
		
	}
	
	private void printFirmLine(List<String> termList, Map<String, Integer> firmTermsCountMap, BufferedWriter bw) throws IOException {
		
		for(String term : termList){
        	if ( firmTermsCountMap.get(term) != null ){
        		bw.write(firmTermsCountMap.get(term) + "\t");
        	}else{
        		bw.write("0" + "\t");
        	}
        }
		
	}
	
	public void readTransposeAndPrintToFileMatrixCsvFile(){
			
		int numOfRows = this.numOfFirms;
		int numOfColumns = this.numOfWords;
		short[][] matrix = new short[numOfRows][numOfColumns];
		int currentRowNum = 0;	
		
		// csv file reading and in-memory creation of the integers matrix
		logger.info("loading TD matrix from csv");
		try {
			FileInputStream fis = new FileInputStream(csvFullPathName);
			InputStream is = fis;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String strLine;
			String delimiter = "\t";
								
			br.readLine(); // avoid the first line with headers
			while ((strLine = br.readLine()) != null) {
				String[] tokens = strLine.split(delimiter);
				for (int i = 1 ; i < tokens.length ; i++){
					//matrix[currentRowNum][i-1] = Integer.parseInt(tokens[i]);
					matrix[currentRowNum][i-1] = Short.parseShort(tokens[i]);
				}
				currentRowNum = currentRowNum + 1;
			}
			
			br.close();
			is.close();
		}catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.err.println("Error: " + fnfe.getMessage());
			System.exit(1);
		}catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}
		
		// rows columns transposition    
		logger.info("transposition of the TD matrix");
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] trasposedMatrix = new int[n][m];
        for(int x = 0; x < n; x++) {
            for(int y = 0; y < m; y++) {
                trasposedMatrix[x][y] = matrix[y][x];
            }
        }
        
        // print the transposed matrix on file
        logger.info("creation of the transposed TD matrix started");	
        FileWriter fw;
        BufferedWriter bw;
        m = trasposedMatrix.length;
	    n = trasposedMatrix[0].length;
	    String currentLine = "";
        
		try {
			String transposedMatrixFullPathName = MATRIX_FILE_FOLDER + "tdmatrix_" + csvCreationDateTime + "_transposed.csv";
			fw = new FileWriter(transposedMatrixFullPathName);
			bw = new BufferedWriter(fw);
			printFirstLineContainingSortedSetOfFirmIds(sortedFirmIdsList,bw);
			for(int i = 0; i<m; i++) {
				currentLine = "";
				currentLine = currentLine + termList.get(i) + "\t";
				for(int j = 0; j<n; j++) {
					currentLine = currentLine + trasposedMatrix[i][j] + "\t";
	            }
				bw.write(currentLine);
            	bw.newLine();
	        }
			bw.close();
			fw.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("transposed TDMatrix printed on file");
    }

	private void configure(String[] args) throws IOException{
		if (args.length == 1){
			String fileProperties = args[0];
			if (Utils.isAValidFile(fileProperties)){
				customizeLog(fileProperties);
				FileInputStream fis = new FileInputStream(fileProperties);
				InputStream inputStream = fis;
				Properties props = new Properties();
				props.load(inputStream);
				
				// MATRIX_FILE_FOLDER
				if(props.getProperty("MATRIX_FILE_FOLDER") != null){
					if(Utils.isAValidDirectory(props.getProperty("MATRIX_FILE_FOLDER"))){
						MATRIX_FILE_FOLDER=props.getProperty("MATRIX_FILE_FOLDER");
					}else{
						logger.error("Specified directory for the parameter MATRIX_FILE_FOLDER not valid or inexistent !");
						System.exit(1);
					}
				}else{
					logger.error("Missing or bad configuration for the parameter MATRIX_FILE_FOLDER !");
					System.exit(1);
				}
				
				// SOLR_INDEX_DIRECTORY_PATH
				if(props.getProperty("SOLR_INDEX_DIRECTORY_PATH") != null){
					if(Utils.isAValidDirectory(props.getProperty("SOLR_INDEX_DIRECTORY_PATH"))){
						SOLR_INDEX_DIRECTORY_PATH=props.getProperty("SOLR_INDEX_DIRECTORY_PATH");
					}else{
						logger.error("Specified directory for the parameter SOLR_INDEX_DIRECTORY_PATH not valid or inexistent !");
						System.exit(1);
					}
				}else{
					logger.error("Missing or bad configuration for the parameter SOLR_INDEX_DIRECTORY_PATH !");
					System.exit(1);
				}
				
				// TREE_TAGGER_EXE_FILEPATH
				if(props.getProperty("TREE_TAGGER_EXE_FILEPATH") != null){
					if(Utils.isAValidFile(props.getProperty("TREE_TAGGER_EXE_FILEPATH"))){
						TREE_TAGGER_EXE_FILEPATH=props.getProperty("TREE_TAGGER_EXE_FILEPATH");
					}else{
						logger.error("Specified file for the parameter TREE_TAGGER_EXE_FILEPATH not valid or inexistent !");
						System.exit(1);
					}
				}else{
					logger.error("Missing or bad configuration for the parameter TREE_TAGGER_EXE_FILEPATH !");
					System.exit(1);
				}
				
				// FIRST_LANG_PAR_FILE_PATH
				if(props.getProperty("FIRST_LANG_PAR_FILE_PATH") != null){
					if(Utils.isAValidFile(props.getProperty("FIRST_LANG_PAR_FILE_PATH"))){
						FIRST_LANG_PAR_FILE_PATH=props.getProperty("FIRST_LANG_PAR_FILE_PATH");
					}else{
						logger.error("Specified file for the parameter FIRST_LANG_PAR_FILE_PATH not valid or inexistent !");
						System.exit(1);
					}
				}else{
					logger.error("Missing or bad configuration for the parameter FIRST_LANG_PAR_FILE_PATH !");
					System.exit(1);
				}
				
				// SECOND_LANG_PAR_FILE_PATH
				if(props.getProperty("SECOND_LANG_PAR_FILE_PATH") != null){
					if(Utils.isAValidFile(props.getProperty("SECOND_LANG_PAR_FILE_PATH"))){
						SECOND_LANG_PAR_FILE_PATH=props.getProperty("SECOND_LANG_PAR_FILE_PATH");
					}else{
						logger.error("Specified file for the parameter SECOND_LANG_PAR_FILE_PATH not valid or inexistent !");
						System.exit(1);
					}
				}else{
					logger.error("Missing or bad configuration for the parameter SECOND_LANG_PAR_FILE_PATH !");
					System.exit(1);
				}
				
				// GO_WORDS_FILE_PATH
				if(props.getProperty("GO_WORDS_FILE_PATH") != null){
					if(Utils.isAValidFile(props.getProperty("GO_WORDS_FILE_PATH"))){
						goWordsFileFullPath=props.getProperty("GO_WORDS_FILE_PATH");
					}else{
						logger.info("Specified file for the parameter GO_WORDS_FILE_PATH not valid or inexistent");
						System.exit(1);
					}
				}else{
					goWordsFileFullPath=null;
				}
				
				// STOP_WORDS_FILE_PATH
				if(props.getProperty("STOP_WORDS_FILE_PATH") != null){
					if(Utils.isAValidFile(props.getProperty("STOP_WORDS_FILE_PATH"))){
						stopWordsFileFullPath=props.getProperty("STOP_WORDS_FILE_PATH");
					}else{
						logger.error("Specified file for the parameter STOP_WORDS_FILE_PATH not valid or inexistent !");
						System.exit(1);
					}
				}else{
					stopWordsFileFullPath=null;
				}
				
				// FIRST_LANG
				if(props.getProperty("FIRST_LANG") != null){
					this.FIRST_LANG = props.getProperty("FIRST_LANG");
				}else{
					logger.error("Missing or bad configuration for the parameter FIRST_LANG !");
					System.exit(1);
				}
				
				// ENGLISH
				if(props.getProperty("SECOND_LANG") != null){
					this.SECOND_LANG = props.getProperty("SECOND_LANG");
				}else{
					logger.error("Missing or bad configuration for the parameter SECOND_LANG !");
					System.exit(1);
				}
				
				// log file
				if(props.getProperty("LOG_FILE_PATH") != null){
					this.logFilePath = props.getProperty("LOG_FILE_PATH");
				}else{
					logger.error("Missing or bad configuration for the parameter LOG_FILE_PATH !");
					System.exit(1);
				}
				
				this.MAX_RESULTS = 10000;
				if(props.getProperty("MAX_RESULTS") != null){this.MAX_RESULTS=Integer.parseInt(props.getProperty("MAX_RESULTS"));}
				
			} else {
				System.out.println("Error opening the file " + args[0] + " or missing file");
				System.exit(1);
			}	
			
		} else {
			System.out.println("usage: java -jar firmsDocTermMatrixGenerator.jar [conf.properties fullpath]");
			System.exit(1);
		}	
		//InputStream inputStream = MainController.class.getClassLoader().getResourceAsStream("conf.properties");//se si vuole leggere il file di properties nel jar
			
	}
	
	private void customizeLog(String fileProperties) throws IOException {
		
		FileInputStream fis = new FileInputStream(fileProperties);
		InputStream inputStream = fis;
		Properties props = new Properties();
		props.load(inputStream);
		
		if(props.getProperty("LOG_FILE_PATH") != null){
			
			logFilePath = props.getProperty("LOG_FILE_PATH");
			
			RollingFileAppender rfa = new RollingFileAppender();
			rfa.setName("FileLogger");
			rfa.setFile(logFilePath);
			rfa.setAppend(true);
			rfa.activateOptions();
			rfa.setMaxFileSize("20MB");
			rfa.setMaxBackupIndex(30);
			rfa.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));

			Logger.getRootLogger().addAppender(rfa);
			Logger.getLogger("logger.it.istat.firmsdoctermmatrixgenerator.*").addAppender(rfa);
			
		}else{
			logger.error("Missing or bad configuration for the parameter LOG_FILE_PATH !");
			System.exit(1);
		}
			
		inputStream.close();
		fis.close();
	}
	
}

// stampa i vari token dei vari field di un certo documento
//TermQuery query = new TermQuery(new Term("codiceAzienda", "108439"));
//TopDocs topdocs = searcher.search(query, MAX_RESULTS);       
//Terms terms = reader.getTermVector(topdocs.scoreDocs[0].doc, "corpoPagina");
//TermsEnum termsEnum; 
//BytesRef text;
//termsEnum = terms.iterator(null);        
//while((text = termsEnum.next()) != null) {
//System.out.println(text.utf8ToString());
//}

// solo per stampare il contenuto dei campi
//Term t = new Term("codiceAzienda", "108439");
//TopDocs topDocs = searcher.search( new TermQuery( t ), MAX_RESULTS );
//for ( ScoreDoc scoreDoc : topDocs.scoreDocs ) {
//  Document doc = searcher.doc( scoreDoc.doc );
//  IndexableField indf = doc.getField("corpoPagina");
//  Field f = (Field) indf;
//  String[] valori = doc.getValues("corpoPagina"); // parole non stemmate
//}
