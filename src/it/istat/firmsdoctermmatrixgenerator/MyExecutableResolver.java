package it.istat.firmsdoctermmatrixgenerator;

import java.io.IOException;

import org.annolab.tt4j.ExecutableResolver;
import org.annolab.tt4j.PlatformDetector;
import org.apache.log4j.Logger;

/**
* @author  Donato Summa
*/
public class MyExecutableResolver implements ExecutableResolver{

	private String TREE_TAGGER_EXE_FILEPATH;
	static Logger logger = Logger.getLogger(MyExecutableResolver.class);
	
	public MyExecutableResolver(String TREE_TAGGER_EXE_FILEPATH) {
		this.TREE_TAGGER_EXE_FILEPATH = TREE_TAGGER_EXE_FILEPATH;
	}
	
	@Override
	public void setPlatformDetector(PlatformDetector aPlatform) {
		logger.debug("Method setPlatformDetector not yet implemented in MyExecutableResolver");
		//System.out.println("Method setPlatformDetector not yet implemented in MyExecutableResolver");		
	}

	@Override
	public void destroy() {
		logger.debug("Method destroy not yet implemented in MyExecutableResolver");
		//System.out.println("Method destroy not yet implemented in MyExecutableResolver");		
	}

	@Override
	public String getExecutable() throws IOException {
		return this.TREE_TAGGER_EXE_FILEPATH;
	}
	
}
