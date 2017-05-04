package it.istat.firmsdoctermmatrixgenerator;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
* @author  Donato Summa
*/
public class Main {

	static Logger logger = Logger.getLogger(Main.class);
	
	public static void main(String[] args) throws IOException{
		
		//=====================================================================================================
	    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date startDateTime = new Date();
        System.out.println("Starting datetime = " + dateFormat.format(startDateTime)); //15/12/2014 15:59:48
        logger.info("Starting datetime = " + dateFormat.format(startDateTime)); //15/12/2014 15:59:48
        //=====================================================================================================
		
        MatrixGenerator mg = new MatrixGenerator(args);
        mg.generateMatrix();
        //mg.readTransposeAndPrintToFileMatrixCsvFile(); // really heavy operation to be used just on a small matrixes for debugging purposes
        
        //=====================================================================================================
        Date endDateTime = new Date();
        System.out.println("Started at = " + dateFormat.format(startDateTime)); //15/12/2014 15:59:48
      	System.out.println("Ending datetime = " + dateFormat.format(endDateTime)); //15/12/2014 15:59:48
      	logger.info("Starting datetime = " + dateFormat.format(startDateTime)); //15/12/2014 15:59:48
      	logger.info("Ending datetime = " + dateFormat.format(endDateTime)); //15/12/2014 15:59:48
        //=====================================================================================================
      	
	}
	
	
}
