package com.popoaichuiniu.jacy.statistic.statistic;

import java.io.*;


public class findAPPHasVun{




	public static void main(String[] args) {

		File dir=new File("/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/preliminary_analysis_consequence_without_data_flow_and_some_details/permission_escalation_path");

		BufferedWriter bufferedWriter=null;
		try {

			 bufferedWriter = new BufferedWriter(new FileWriter("aPPHasVunList.txt"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		int count =0;//有Vun的app数量
		int count2 =0;//vun数量
		System.out.println("APP数量:"+dir.listFiles().length);

		for(File file: dir.listFiles())
		{
			try{
				BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
				String content=null;

				boolean flag=true;
				while(((content=bufferedReader.readLine())!=null))
				{
					if(flag) {
						bufferedWriter.write(file.getName() + "\n");
						count = count + 1;
						flag=false;
					}
					count2=count2+1;
				}
				bufferedReader.close();
			}
			catch (IOException io)
			{

			}

		}
		try {
			bufferedWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("有vunAPP数量:"+count);
		System.out.println("vun数量:"+count2);
	}
}
