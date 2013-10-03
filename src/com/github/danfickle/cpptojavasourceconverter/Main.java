package com.github.danfickle.cpptojavasourceconverter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;

public class Main
{
	final static String HOME_PATH = "/home/daniel/workspace/cpp-to-java-source-converter/";
	
	public static void main(String... args) throws Exception
	{
		GlobalCtx global = new GlobalCtx();
		
		BufferedReader br = new BufferedReader(new FileReader(HOME_PATH + "tests/" + "list-of-test-files.txt"));
	    String line = br.readLine();

	    while (line != null) {
	    	if (!line.isEmpty() && !line.startsWith("#"))
	    	{
	    		IASTTranslationUnit tu = getTranslationUnit(HOME_PATH + "tests/" + line + ".cpp");
	    		Traverser parser = new Traverser();
	    		String outputCode = parser.traverse(tu, global);
	    		
	    		FileOutputStream fos = new FileOutputStream(HOME_PATH + "crap/" + line + ".java");
	    		OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8"); 
	    		out.write(outputCode);
	    		out.close();
	    	}
	    	line = br.readLine();
	    }
	    br.close();
	}
	
	private static IASTTranslationUnit getTranslationUnit(String filename) throws Exception
	{
		IParserLogService log = new DefaultLogService();
		FileContent ct = FileContent.createForExternalFileLocation(filename);
		return GPPLanguage.getDefault().getASTTranslationUnit(ct, new Scanner(), null, null, 0, log);
	}
	
	private static class Scanner implements IScannerInfo
	{
		@Override
		public Map<String, String> getDefinedSymbols() {
			Map<String, String> hm = new HashMap<String, String>();
			// hm.put("NULL", "0"); // example only...
			return hm;
		}		
		
		@Override
		public String[] getIncludePaths() {
			return new String[] { HOME_PATH };
		}
	}
	
}
