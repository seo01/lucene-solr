package org.apache.solr.handler.dataimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

//TODO: Refactor and test
public class SimpleHTMLMailParser
{

	public InputStream processInputStream(InputStream is)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		String str = null;
		try{
			String boundary = br.readLine();
			System.out.println(boundary);
			if(boundary.startsWith("--"))
				str = processContent(br,boundary);
			else //this is not a multipart
				str = processSimpleContent(boundary,br);
		}
		catch(IOException exp)
		{
			exp.printStackTrace();
		}
		if(str == null)
		{
			//TODO: Change this to logging
			System.out.println("Something went wrong");
			str = "";
		}

		InputStream outIs = new ByteArrayInputStream(str.getBytes());
		return outIs;
	}

	public String processSimpleContent(String head, BufferedReader br) throws IOException
	{
		String line;
		StringBuilder outStr = new StringBuilder();
		if(head != null)
			outStr.append(head);
		while ((line = br.readLine()) != null)
		{	
			outStr.append(line);
		}
		return outStr.toString();
	}

	public String processContent(BufferedReader br, String boundary) throws IOException
	{
		String line;
		StringBuilder outStr = new StringBuilder();
		while(true)
		{
			String contentTypeLine = br.readLine();
			if(contentTypeLine == null)
			{
				return null;//nothing to report
			}
			else if(contentTypeLine.contains("text/plain"))
			{
				//consume lines until you hit an empty line
				while ((line = br.readLine()) != null) {
					if(line.equals(""))
						break;
				}
				if(line == null)
					return null;
				while ((line = br.readLine()) != null) {
					if(line.startsWith(boundary))
						break;
					//cut the last two chars off
					String cutLine = ((line.length()>1 && line.charAt(line.length()-1) == '='))?line.substring(0,line.length()-2):(line+"\n");
					outStr.append(cutLine);
				}
				return outStr.toString();
			}
			else
			{	
				while ((line = br.readLine()) != null) {
					if(line.startsWith(boundary))
						break;
				}
				if(line == null)
					return null;
			}
		}
	}


	public static void main(String [] args) throws Exception
	{
		SimpleHTMLMailParser shtmlmailparser = new SimpleHTMLMailParser();
		//test this class -- read from stdin
		BufferedReader br = 
                      new BufferedReader(
                      	new InputStreamReader(
                      	shtmlmailparser.processInputStream(System.in)));
 
		String input;
 
		while((input=br.readLine())!=null){
			System.out.println(input);
		}
	}

}