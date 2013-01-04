package com.github.danfickle.cpptojavasourceconverter;

import java.io.FileOutputStream;
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
	public static void main(String... args) throws Exception
	{
		IASTTranslationUnit tu = getTranslationUnit("/home/daniel/workspace/cpp-to-java-source-converter/test.cpp");
		SourceConverterStage2 parser = new SourceConverterStage2();
		String outputCode = parser.traverse(tu);

		FileOutputStream fos = new FileOutputStream("/home/daniel/workspace/cpp-to-java-source-converter/output.java");
		OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8"); 
		out.write(outputCode);
		out.close();
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
			return new String[] { "/home/daniel/workspace/cpp-to-java-source-converter/" };
		}
	}
	
}
