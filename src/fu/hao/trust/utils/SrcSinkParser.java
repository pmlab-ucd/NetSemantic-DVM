/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package fu.hao.trust.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fu.hao.trust.analysis.SrcSinkMethod;

/**
 * Parser for the permissions to method map of Adrienne Porter Felt. 
 * 
 * @author Siegfried Rasthofer
 */
public class SrcSinkParser {
	
	private Set<SrcSinkMethod> sourceList = null;
	private Set<SrcSinkMethod> sinkList = null;
	private Set<SrcSinkMethod> neitherList = null;
	
	private static final int INITIAL_SET_SIZE = 10000;
	
	private List<String> data;
	private final String regex = "^<(.+):\\s*(.+)\\s+(.+)\\s*\\((.*)\\)>\\s*(.*?)(\\s+->\\s+(.*))?$";
//	private final String regexNoRet = "^<(.+):\\s(.+)\\s?(.+)\\s*\\((.*)\\)>\\s+(.*?)(\\s+->\\s+(.*))?+$";
	private final String regexNoRet = "^<(.+):\\s*(.+)\\s*\\((.*)\\)>\\s*(.*?)?(\\s+->\\s+(.*))?$";
	
	public static SrcSinkParser fromFile(String fileName) throws IOException {
		SrcSinkParser pmp = new SrcSinkParser();
		pmp.readFile(fileName);
		return pmp;
	}
	
	public static SrcSinkParser fromStringList(List<String> data) throws IOException {
		SrcSinkParser pmp = new SrcSinkParser(data);
		return pmp;
	}

	private SrcSinkParser() {
	}
	
	private SrcSinkParser(List<String> data) {
		this.data = data;
	}

	private void readFile(String fileName) throws IOException{
		String line;
		this.data = new ArrayList<String>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
		    while((line = br.readLine()) != null)
		    	this.data.add(line);
		}
		finally {
			if (br != null)
				br.close();
			if (fr != null)
				fr.close();
		}
	}
	
	private Set<SrcSinkMethod> getSources() {
		if (sourceList == null || sinkList == null)
			parse();
		return this.sourceList;
	}
	
	/**
	* @Title: getSrcs
	* @Author: hao
	* @Description: TODO
	* @param @return  
	* @return Set<String>   
	* @throws
	*/
	public Set<String> getSrcStrs() {
		Set<SrcSinkMethod> sourceList = getSources();
		Set<String> res = new HashSet<>();
		for (SrcSinkMethod method : sourceList) {
			res.add(method.getSignature());
		}
		
		return res;
	}
	
	public Set<String> getSinkStrs() {
		Set<SrcSinkMethod> sourceList = getSinks();
		Set<String> res = new HashSet<>();
		for (SrcSinkMethod method : sourceList) {
			res.add(method.getSignature());
		}
		
		return res;
	}

	private Set<SrcSinkMethod> getSinks() {
		if (sourceList == null || sinkList == null)
			parse();
		return this.sinkList;
	}

	private void parse() {
		sourceList = new HashSet<SrcSinkMethod>(INITIAL_SET_SIZE);
		sinkList = new HashSet<SrcSinkMethod>(INITIAL_SET_SIZE);
		neitherList = new HashSet<SrcSinkMethod>(INITIAL_SET_SIZE);
		
		Pattern p = Pattern.compile(regex);
		Pattern pNoRet = Pattern.compile(regexNoRet);
		
		for(String line : this.data){	
			if (line.isEmpty() || line.startsWith("%"))
				continue;
			Matcher m = p.matcher(line);
			if(m.find()) {
				SrcSinkMethod am = parseMethod(m, true);
				SrcSinkMethod singleMethod = new SrcSinkMethod(am);
				
				if (am.isSource())
					sourceList.add(singleMethod);
				else if (am.isSink())
					sinkList.add(singleMethod);
				else if (am.isNeitherNor())
					neitherList.add(singleMethod);
			}
			else {
				Matcher mNoRet = pNoRet.matcher(line);
				if(mNoRet.find()) {
					SrcSinkMethod am = parseMethod(mNoRet, true);
					SrcSinkMethod singleMethod = new SrcSinkMethod(am);
					
					if (am.isSource())
						sourceList.add(singleMethod);
					else if (am.isSink())
						sinkList.add(singleMethod);
					else if (am.isNeitherNor())
						neitherList.add(singleMethod);
				}
				else
					System.err.println("Line does not match: " + line);
			}
		}
	}

	private SrcSinkMethod parseMethod(Matcher m, boolean hasReturnType) {
		assert(m.group(1) != null && m.group(2) != null && m.group(3) != null 
				&& m.group(4) != null);
		SrcSinkMethod singleMethod;
		int groupIdx = 1;
		
		//class name
		String className = m.group(groupIdx++).trim();
		
		String returnType = "";
		if (hasReturnType) {
			//return type
			returnType = m.group(groupIdx++).trim();
		}
		
		//method name
		String methodName = m.group(groupIdx++).trim();
		
		//method parameter
		List<String> methodParameters = new ArrayList<String>();
		String params = m.group(groupIdx++).trim();
		if (!params.isEmpty())
			for (String parameter : params.split(","))
				methodParameters.add(parameter.trim());
		
		//permissions
		String classData = "";
		String permData = "";
		Set<String> permissions = new HashSet<String>();
		if (groupIdx < m.groupCount() && m.group(groupIdx) != null) {
			permData = m.group(groupIdx);
			if (permData.contains("->")) {
				classData = permData.replace("->", "").trim();
				permData = "";
			}
			groupIdx++;
		}
		if (!permData.isEmpty())
			for(String permission : permData.split(" "))
				permissions.add(permission);
		
		//create method signature
		singleMethod = new SrcSinkMethod(methodName, methodParameters, returnType, className, permissions);
		
		if (classData.isEmpty())
			if(m.group(groupIdx) != null) {
				classData = m.group(groupIdx).replace("->", "").trim();
				groupIdx++;
			}
		if (!classData.isEmpty())
			for(String target : classData.split("\\s")) {
				target = target.trim();
				
				// Throw away categories
				if (target.contains("|"))
					target = target.substring(target.indexOf('|'));
				
				if (!target.isEmpty() && !target.startsWith("|")) {
					if(target.equals("_SOURCE_"))
						singleMethod.setSource(true);
					else if(target.equals("_SINK_"))
						singleMethod.setSink(true);
					else if(target.equals("_NONE_"))
						singleMethod.setNeitherNor(true);
					else
						throw new RuntimeException("error in target definition: " + target);
				}
			}
		return singleMethod;
	}

	public Set<SrcSinkMethod> getAllMethods() {
		if (sourceList == null || sinkList == null)
			parse();
		
		Set<SrcSinkMethod> sourcesSinks = new HashSet<>(sourceList.size()
				+ sinkList.size() + neitherList.size());
		sourcesSinks.addAll(sourceList);
		sourcesSinks.addAll(sinkList);
		sourcesSinks.addAll(neitherList);
		return sourcesSinks;
	}
}
