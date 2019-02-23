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
package soot.jimple.infoflow.android.TestApps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.stream.XMLStreamException;

import org.xmlpull.v1.XmlPullParserException;

import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration.CallgraphAlgorithm;
import soot.jimple.infoflow.InfoflowManager;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration.CallbackAnalyzer;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.source.AndroidSourceSinkManager.LayoutMatchingMode;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.ipc.IIPCManager;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.results.xml.InfoflowResultsSerializer;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.options.Options;

public class Test {

	private static final class MyResultsAvailableHandler implements ResultsAvailableHandler {
		private final BufferedWriter wr;

		private MyResultsAvailableHandler() {
			this.wr = null;
		}

		private MyResultsAvailableHandler(BufferedWriter wr) {
			this.wr = wr;
		}

		@Override
		public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
			// Dump the results
			if (results == null) {
				print("No results found.");
			} else {
				// Report the results
				for (ResultSinkInfo sink : results.getResults().keySet()) {
					print("Found a flow to sink " + sink + ", from the following sources:");
					for (ResultSourceInfo source : results.getResults().get(sink)) {
						print("\t- " + source.getSource() + " (in " + cfg.getMethodOf(source.getSource()).getSignature()
								+ ")");
						if (source.getPath() != null)
							print("\t\ton Path " + Arrays.toString(source.getPath()));
					}
				}

				// Serialize the results if requested
				// Write the results into a file if requested
				if (resultFilePath != null && !resultFilePath.isEmpty()) {
					InfoflowResultsSerializer serializer = new InfoflowResultsSerializer(cfg);
					try {
						serializer.serialize(results, resultFilePath);
					} catch (FileNotFoundException ex) {
						System.err.println("Could not write data flow results to file: " + ex.getMessage());
						ex.printStackTrace();
						throw new RuntimeException(ex);
					} catch (XMLStreamException ex) {
						System.err.println("Could not write data flow results to file: " + ex.getMessage());
						ex.printStackTrace();
						throw new RuntimeException(ex);
					}
				}
			}

		}

		private void print(String string) {
			try {
				System.out.println(string);
				if (wr != null)
					wr.write(string + "\n");
			} catch (IOException ex) {
				// ignore
			}
		}
	}

	private static InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();

	private static int repeatCount = 1;
	private static int timeout = -1;
	private static int sysTimeout = -1;

	private static boolean aggressiveTaintWrapper = false;
	private static boolean noTaintWrapper = false;
	private static String summaryPath = "";
	private static String resultFilePath = "";

	private static boolean DEBUG = false;

	private static IIPCManager ipcManager = null;

	public static void setIPCManager(IIPCManager ipcManager) {
		Test.ipcManager = ipcManager;
	}

	public static IIPCManager getIPCManager() {
		return Test.ipcManager;
	}

	/**
	 * @param args
	 *            Program arguments. args[0] = path to apk-file, args[1] = path
	 *            to android-dir (path/android-platforms/)
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		args=new String[2];
		args[0]="sms2.apk";
		args[1]="/home/zms/platforms";
		
		if (args.length < 2) {
			printUsage();
			return;
		}
		// start with cleanup:
		File outputDir = new File("JimpleOutput");
		if (outputDir.isDirectory()) {
			boolean success = true;
			for (File f : outputDir.listFiles()) {
				success = success && f.delete();
			}
			if (!success) {
				System.err.println("Cleanup of output directory " + outputDir + " failed!");
			}
			outputDir.delete();
		}

		// Parse additional command-line arguments
		if (!parseAdditionalOptions(args))// 从参数中获取一些值，如果获取失败，整个程序返回
			return;
		if (!validateAdditionalOptions())
			return;
		if (repeatCount <= 0)
			return;

		List<String> apkFiles = new ArrayList<String>();// apkFiles存储所有apk的名字
		File apkFile = new File(args[0]);
		if (apkFile.isDirectory()) {// 如果传入的第一个参数是文件目录的话，将其中的所有APK文件(以.apk为结尾的文件)挑选出来)
			String[] dirFiles = apkFile.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return (name.endsWith(".apk"));
				}

			});
			for (String s : dirFiles)
				apkFiles.add(s);
		} else {// 传入的参数是文件的名称，不是目录
			// apk is a file so grab the extension
			String extension = apkFile.getName().substring(apkFile.getName().lastIndexOf("."));
			if (extension.equalsIgnoreCase(".txt")) {// 如果传入的是txt文件，将其每一行的内容加入到apkFiles中
				BufferedReader rdr = new BufferedReader(new FileReader(apkFile));// 可能这个txt,存储了很多apk文件的地址
				String line = null;
				while ((line = rdr.readLine()) != null)
					apkFiles.add(line);
				rdr.close();
			} else if (extension.equalsIgnoreCase(".apk"))
				apkFiles.add(args[0]);
			else {
				System.err.println("Invalid input file format: " + extension);
				return;
			}
		}

		int oldRepeatCount = repeatCount;// repeatCount的值由参数设置，具体含义不知道：（？？？）
											// ，如果参数未设置，默认参数值为1
		for (final String fileName : apkFiles) {//fileName一个APK文件地址
			repeatCount = oldRepeatCount;
			final String fullFilePath;
			System.gc();// Calling the gc method suggests that the Java Virtual
						// Machine expend effort toward recycling unused objects
						// in order to make the memory they currently occupy
						// available for quick reuse. When control returns from
						// the method call, the Java Virtual Machine has made a
						// best effort to reclaim space from all discarded
						// objects.

			// Directory handling
			if (apkFiles.size() > 1) {//目录中的APK文件数量大于1
				// fullFilePath是apk文件的路径
				if (apkFile.isDirectory())//传入的是一个目录
					fullFilePath = args[0] + File.separator + fileName;//目录中的一个APK
				else//或者传入的是一个TXT文件
					fullFilePath = fileName;
				System.out.println("Analyzing file " + fullFilePath + "...");
				File flagFile = new File("_Run_" + new File(fileName).getName());
				if (flagFile.exists())
					continue;
				flagFile.createNewFile();// 创建_Run_+fileName文件（这个用来干嘛的？？？）
			} else
				fullFilePath = fileName;

			// Run the analysis
			while (repeatCount > 0) {
				System.gc();
				if (timeout > 0)
					runAnalysisTimeout(fullFilePath, args[1]);
				else if (sysTimeout > 0)
					runAnalysisSysTimeout(fullFilePath, args[1]);
				else
					runAnalysis(fullFilePath, args[1]);// 开始分析apk
				repeatCount--;
			}

			System.gc();
		}
	}

	/**
	 * Parses the optional command-line arguments
	 * 
	 * @param args
	 *            The array of arguments to parse
	 * @return True if all arguments are valid and could be parsed, otherwise
	 *         false
	 */
	private static boolean parseAdditionalOptions(String[] args) {
		int i = 2;
		while (i < args.length) {
			if (args[i].equalsIgnoreCase("--timeout")) {
				timeout = Integer.valueOf(args[i + 1]);
				i += 2;
			} else if (args[i].equalsIgnoreCase("--systimeout")) {
				sysTimeout = Integer.valueOf(args[i + 1]);
				i += 2;
			} else if (args[i].equalsIgnoreCase("--singleflow")) {
				config.setStopAfterFirstFlow(true);
				i++;
			} else if (args[i].equalsIgnoreCase("--implicit")) {
				config.setEnableImplicitFlows(true);
				i++;
			} else if (args[i].equalsIgnoreCase("--nostatic")) {
				config.setEnableStaticFieldTracking(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--aplength")) {
				InfoflowAndroidConfiguration.setAccessPathLength(Integer.valueOf(args[i + 1]));
				i += 2;
			} else if (args[i].equalsIgnoreCase("--cgalgo")) {
				String algo = args[i + 1];
				if (algo.equalsIgnoreCase("AUTO"))
					config.setCallgraphAlgorithm(CallgraphAlgorithm.AutomaticSelection);
				else if (algo.equalsIgnoreCase("CHA"))
					config.setCallgraphAlgorithm(CallgraphAlgorithm.CHA);
				else if (algo.equalsIgnoreCase("VTA"))
					config.setCallgraphAlgorithm(CallgraphAlgorithm.VTA);
				else if (algo.equalsIgnoreCase("RTA"))
					config.setCallgraphAlgorithm(CallgraphAlgorithm.RTA);
				else if (algo.equalsIgnoreCase("SPARK"))
					config.setCallgraphAlgorithm(CallgraphAlgorithm.SPARK);
				else if (algo.equalsIgnoreCase("GEOM"))
					config.setCallgraphAlgorithm(CallgraphAlgorithm.GEOM);
				else {
					System.err.println("Invalid callgraph algorithm");
					return false;
				}
				i += 2;
			} else if (args[i].equalsIgnoreCase("--nocallbacks")) {
				config.setEnableCallbacks(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--noexceptions")) {
				config.setEnableExceptionTracking(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--layoutmode")) {
				String algo = args[i + 1];
				if (algo.equalsIgnoreCase("NONE"))
					config.setLayoutMatchingMode(LayoutMatchingMode.NoMatch);
				else if (algo.equalsIgnoreCase("PWD"))
					config.setLayoutMatchingMode(LayoutMatchingMode.MatchSensitiveOnly);
				else if (algo.equalsIgnoreCase("ALL"))
					config.setLayoutMatchingMode(LayoutMatchingMode.MatchAll);
				else {
					System.err.println("Invalid layout matching mode");
					return false;
				}
				i += 2;
			} else if (args[i].equalsIgnoreCase("--aliasflowins")) {
				config.setFlowSensitiveAliasing(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--paths")) {
				config.setComputeResultPaths(true);
				i++;
			} else if (args[i].equalsIgnoreCase("--nopaths")) {
				config.setComputeResultPaths(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--aggressivetw")) {
				aggressiveTaintWrapper = false;
				i++;
			} else if (args[i].equalsIgnoreCase("--pathalgo")) {
				String algo = args[i + 1];
				if (algo.equalsIgnoreCase("CONTEXTSENSITIVE"))
					config.setPathBuilder(PathBuilder.ContextSensitive);
				else if (algo.equalsIgnoreCase("CONTEXTINSENSITIVE"))
					config.setPathBuilder(PathBuilder.ContextInsensitive);
				else if (algo.equalsIgnoreCase("SOURCESONLY"))
					config.setPathBuilder(PathBuilder.ContextInsensitiveSourceFinder);
				else {
					System.err.println("Invalid path reconstruction algorithm");
					return false;
				}
				i += 2;
			} else if (args[i].equalsIgnoreCase("--summarypath")) {
				summaryPath = args[i + 1];
				i += 2;
			} else if (args[i].equalsIgnoreCase("--saveresults")) {
				resultFilePath = args[i + 1];
				i += 2;
			} else if (args[i].equalsIgnoreCase("--sysflows")) {
				config.setIgnoreFlowsInSystemPackages(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--notaintwrapper")) {
				noTaintWrapper = true;
				i++;
			} else if (args[i].equalsIgnoreCase("--repeatcount")) {
				repeatCount = Integer.parseInt(args[i + 1]);
				i += 2;
			} else if (args[i].equalsIgnoreCase("--noarraysize")) {
				config.setEnableArraySizeTainting(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--arraysize")) {
				config.setEnableArraySizeTainting(true);
				i++;
			} else if (args[i].equalsIgnoreCase("--notypetightening")) {
				InfoflowAndroidConfiguration.setUseTypeTightening(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--safemode")) {
				InfoflowAndroidConfiguration.setUseThisChainReduction(false);
				i++;
			} else if (args[i].equalsIgnoreCase("--logsourcesandsinks")) {
				config.setLogSourcesAndSinks(true);
				i++;
			} else if (args[i].equalsIgnoreCase("--callbackanalyzer")) {
				String algo = args[i + 1];
				if (algo.equalsIgnoreCase("DEFAULT"))
					config.setCallbackAnalyzer(CallbackAnalyzer.Default);
				else if (algo.equalsIgnoreCase("FAST"))
					config.setCallbackAnalyzer(CallbackAnalyzer.Fast);
				else {
					System.err.println("Invalid callback analysis algorithm");
					return false;
				}
				i += 2;
			} else if (args[i].equalsIgnoreCase("--maxthreadnum")) {
				config.setMaxThreadNum(Integer.valueOf(args[i + 1]));
				i += 2;
			} else if (args[i].equalsIgnoreCase("--arraysizetainting")) {
				config.setEnableArraySizeTainting(true);
				i++;
			} else
				i++;
		}
		return true;
	}

	private static boolean validateAdditionalOptions() {
		if (timeout > 0 && sysTimeout > 0) {
			return false;
		}
		if (!config.getFlowSensitiveAliasing() && config.getCallgraphAlgorithm() != CallgraphAlgorithm.OnDemand
				&& config.getCallgraphAlgorithm() != CallgraphAlgorithm.AutomaticSelection) {
			System.err.println("Flow-insensitive aliasing can only be configured for callgraph "
					+ "algorithms that support this choice.");
			return false;
		}
		return true;
	}

	private static void runAnalysisTimeout(final String fileName, final String androidJar) {
		FutureTask<InfoflowResults> task = new FutureTask<InfoflowResults>(new Callable<InfoflowResults>() {

			@Override
			public InfoflowResults call() throws Exception {

				final BufferedWriter wr = new BufferedWriter(
						new FileWriter("_out_" + new File(fileName).getName() + ".txt"));
				try {
					final long beforeRun = System.nanoTime();
					wr.write("Running data flow analysis...\n");
					final InfoflowResults res = runAnalysis(fileName, androidJar);
					wr.write("Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds\n");

					wr.flush();
					return res;
				} finally {
					if (wr != null)
						wr.close();
				}
			}

		});
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(task);

		try {
			System.out.println("Running infoflow task...");
			task.get(timeout, TimeUnit.MINUTES);
		} catch (ExecutionException e) {
			System.err.println("Infoflow computation failed: " + e.getMessage());
			e.printStackTrace();
		} catch (TimeoutException e) {
			System.err.println("Infoflow computation timed out: " + e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Infoflow computation interrupted: " + e.getMessage());
			e.printStackTrace();
		}

		// Make sure to remove leftovers
		executor.shutdown();
	}

	private static void runAnalysisSysTimeout(final String fileName, final String androidJar) {
		String classpath = System.getProperty("java.class.path");
		String javaHome = System.getProperty("java.home");
		String executable = "/usr/bin/timeout";
		String[] command = new String[] { executable, "-s", "KILL", sysTimeout + "m", javaHome + "/bin/java", "-cp",
				classpath, "soot.jimple.infoflow.android.TestApps.Test", fileName, androidJar,
				config.getStopAfterFirstFlow() ? "--singleflow" : "--nosingleflow",
				config.getEnableImplicitFlows() ? "--implicit" : "--noimplicit",
				config.getEnableStaticFieldTracking() ? "--static" : "--nostatic", "--aplength",
				Integer.toString(InfoflowAndroidConfiguration.getAccessPathLength()), "--cgalgo",
				callgraphAlgorithmToString(config.getCallgraphAlgorithm()),
				config.getEnableCallbacks() ? "--callbacks" : "--nocallbacks",
				config.getEnableExceptionTracking() ? "--exceptions" : "--noexceptions", "--layoutmode",
				layoutMatchingModeToString(config.getLayoutMatchingMode()),
				config.getFlowSensitiveAliasing() ? "--aliasflowsens" : "--aliasflowins",
				config.getComputeResultPaths() ? "--paths" : "--nopaths",
				aggressiveTaintWrapper ? "--aggressivetw" : "--nonaggressivetw", "--pathalgo",
				pathAlgorithmToString(config.getPathBuilder()),
				(summaryPath != null && !summaryPath.isEmpty()) ? "--summarypath" : "",
				(summaryPath != null && !summaryPath.isEmpty()) ? summaryPath : "",
				(resultFilePath != null && !resultFilePath.isEmpty()) ? "--saveresults" : "",
				noTaintWrapper ? "--notaintwrapper" : "",
				// "--repeatCount", Integer.toString(repeatCount),
				config.getEnableArraySizeTainting() ? "" : "--noarraysize",
				InfoflowAndroidConfiguration.getUseTypeTightening() ? "" : "--notypetightening",
				InfoflowAndroidConfiguration.getUseThisChainReduction() ? "" : "--safemode",
				config.getLogSourcesAndSinks() ? "--logsourcesandsinks" : "", "--callbackanalyzer",
				callbackAlgorithmToString(config.getCallbackAnalyzer()), "--maxthreadnum",
				Integer.toString(config.getMaxThreadNum()),
				config.getEnableArraySizeTainting() ? "--arraysizetainting" : "" };
		System.out.println("Running command: " + executable + " " + Arrays.toString(command));
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectOutput(new File("out_" + new File(fileName).getName() + "_" + repeatCount + ".txt"));
			pb.redirectError(new File("err_" + new File(fileName).getName() + "_" + repeatCount + ".txt"));
			Process proc = pb.start();
			proc.waitFor();
		} catch (IOException ex) {
			System.err.println("Could not execute timeout command: " + ex.getMessage());
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			System.err.println("Process was interrupted: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private static String callgraphAlgorithmToString(CallgraphAlgorithm algorihm) {
		switch (algorihm) {
		case AutomaticSelection:
			return "AUTO";
		case CHA:
			return "CHA";
		case VTA:
			return "VTA";
		case RTA:
			return "RTA";
		case SPARK:
			return "SPARK";
		case GEOM:
			return "GEOM";
		default:
			return "unknown";
		}
	}

	private static String layoutMatchingModeToString(LayoutMatchingMode mode) {
		switch (mode) {
		case NoMatch:
			return "NONE";
		case MatchSensitiveOnly:
			return "PWD";
		case MatchAll:
			return "ALL";
		default:
			return "unknown";
		}
	}

	private static String pathAlgorithmToString(PathBuilder pathBuilder) {
		switch (pathBuilder) {
		case ContextSensitive:
			return "CONTEXTSENSITIVE";
		case ContextInsensitive:
			return "CONTEXTINSENSITIVE";
		case ContextInsensitiveSourceFinder:
			return "SOURCESONLY";
		default:
			return "UNKNOWN";
		}
	}

	private static String callbackAlgorithmToString(CallbackAnalyzer analyzer) {
		switch (analyzer) {
		case Default:
			return "DEFAULT";
		case Fast:
			return "FAST";
		default:
			return "UNKNOWN";
		}
	}

	private static InfoflowResults runAnalysis(final String fileName, final String androidJar) {
		try {
			final long beforeRun = System.nanoTime();// Returns the current
														// value of the running
														// Java Virtual
														// Machine's
														// high-resolution time
														// source, in
														// nanoseconds.

			final SetupApplication app;// app()
			if (null == ipcManager) {//这个ipcManager干什么的？？？
				app = new SetupApplication(androidJar, fileName);
			} else {
				app = new SetupApplication(androidJar, fileName, ipcManager);
			}

			// Set configuration object
			app.setConfig(config);// Sets the data flow configuration  InfoflowAndroidConfiguration
			if (noTaintWrapper)
				app.setSootConfig(new IInfoflowConfig() {// Sets the extra Soot
															// configuration
															// options to be
															// used when running
															// the analysis

					@Override
					public void setSootOptions(Options options) {// Interface to
																	// configure
																	// Soot
																	// options
																	// like the
																	// output
																	// format or
																	// a list of
																	// packages
																	// that
																	// should be
																	// included
																	// or
																	// excluded
																	// for
																	// analysis
						options.set_include_all(true);
					}

				});

			final ITaintPropagationWrapper taintWrapper;// This interface
														// declares methods to
														// define classes and
														// methods which should
														// not be analyzed
														// directly. Analysis
														// results are instead
														// taken from an
														// external model
														// containing method
														// summaries (which
														// improves performance
														// and helps if the
														// sources are not
														// available). Unless
														// stated otherwise, all
														// methods in this
														// interface must be
														// implemented thread
														// safe.
			if (noTaintWrapper)
				taintWrapper = null;
			else if (summaryPath != null && !summaryPath.isEmpty()) {// summaryPath默认为空，可由参数设置
				System.out.println("Using the StubDroid taint wrapper");
				taintWrapper = createLibrarySummaryTW();
				if (taintWrapper == null) {
					System.err.println("Could not initialize StubDroid");
					return null;
				}
			} else {
				//污点传播的规则！！！
				final EasyTaintWrapper easyTaintWrapper;// A list of methods is
														// passed which contains
														// signatures of
														// instance methods that
														// taint their base
														// objects if they are
														// called with a tainted
														// parameter. When a
														// base object is
														// tainted, all return
														// values are tainted,
														// too. For static
														// methods, only the
														// return value is
														// assumed to be tainted
														// when the method is
														// called with a tainted
														// parameter value.
				File twSourceFile = new File("../soot-infoflow/EasyTaintWrapperSource.txt");
				if (twSourceFile.exists())
					easyTaintWrapper = new EasyTaintWrapper(twSourceFile);
				else {
					twSourceFile = new File("EasyTaintWrapperSource.txt");//分析的包(分析所有的性能太差，有的也没必要分析) 污点源
					if (twSourceFile.exists())
						easyTaintWrapper = new EasyTaintWrapper(twSourceFile);
					else {
						System.err.println(
								"Taint wrapper definition file not found at " + twSourceFile.getAbsolutePath());
						return null;
					}
				}
				easyTaintWrapper.setAggressiveMode(aggressiveTaintWrapper);
				// Sets whether the taint wrapper shall always assume the return
				// value of a call "a = x.foo()" to be tainted if the base
				// object is tainted, even if the respective method is not in
				// the data file.
				taintWrapper = easyTaintWrapper;
			}
			app.setTaintWrapper(taintWrapper);//设置污点传播规则
			app.calculateSourcesSinksEntrypoints("SourcesAndSinks.txt");//定义了一些source和sink，然后找到这些入口点，然后再找数据流路径。

			if (DEBUG) {
				app.printEntrypoints();
				app.printSinks();
				app.printSources();
			}

			System.out.println("Running data flow analysis...");
			//进行数据流分析
			
			final InfoflowResults res = app.runInfoflow(new MyResultsAvailableHandler());//Runs the data flow analysis
			//MyResultsAvailableHandler() 里的方法会被调用当information flow results become available
			System.out.println("Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");

			if (config.getLogSourcesAndSinks()) {
				if (!app.getCollectedSources().isEmpty()) {
					System.out.println("Collected sources:");
					for (Stmt s : app.getCollectedSources())//打印source
						System.out.println("\t" + s);
				}
				if (!app.getCollectedSinks().isEmpty()) {//打印sink
					System.out.println("Collected sinks:");
					for (Stmt s : app.getCollectedSinks())
						System.out.println("\t" + s);
				}
			}

			return res;
		} catch (IOException ex) {
			System.err.println("Could not read file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (XmlPullParserException ex) {
			System.err.println("Could not read Android manifest file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates the taint wrapper for using library summaries
	 * 
	 * @return The taint wrapper for using library summaries
	 * @throws IOException
	 *             Thrown if one of the required files could not be read
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ITaintPropagationWrapper createLibrarySummaryTW() throws IOException {
		try {
			Class clzLazySummary = Class
					.forName("soot.jimple.infoflow.methodSummary.data.provider.LazySummaryProvider");
			Class itfLazySummary = Class
					.forName("soot.jimple.infoflow.methodSummary.data.provider.IMethodSummaryProvider");

			Object lazySummary = clzLazySummary.getConstructor(File.class).newInstance(new File(summaryPath));

			ITaintPropagationWrapper summaryWrapper = (ITaintPropagationWrapper) Class
					.forName("soot.jimple.infoflow.methodSummary.taintWrappers.SummaryTaintWrapper")
					.getConstructor(itfLazySummary).newInstance(lazySummary);

			ITaintPropagationWrapper systemClassWrapper = new ITaintPropagationWrapper() {

				private ITaintPropagationWrapper wrapper = new EasyTaintWrapper("EasyTaintWrapperSource.txt");

				private boolean isSystemClass(Stmt stmt) {
					if (stmt.containsInvokeExpr())
						return SystemClassHandler
								.isClassInSystemPackage(stmt.getInvokeExpr().getMethod().getDeclaringClass().getName());
					return false;
				}

				@Override
				public boolean supportsCallee(Stmt callSite) {
					return isSystemClass(callSite) && wrapper.supportsCallee(callSite);
				}

				@Override
				public boolean supportsCallee(SootMethod method) {
					return SystemClassHandler.isClassInSystemPackage(method.getDeclaringClass().getName())
							&& wrapper.supportsCallee(method);
				}

				@Override
				public boolean isExclusive(Stmt stmt, Abstraction taintedPath) {
					return isSystemClass(stmt) && wrapper.isExclusive(stmt, taintedPath);
				}

				@Override
				public void initialize(InfoflowManager manager) {
					wrapper.initialize(manager);
				}

				@Override
				public int getWrapperMisses() {
					return 0;
				}

				@Override
				public int getWrapperHits() {
					return 0;
				}

				@Override
				public Set<Abstraction> getTaintsForMethod(Stmt stmt, Abstraction d1, Abstraction taintedPath) {
					if (!isSystemClass(stmt))
						return null;
					return wrapper.getTaintsForMethod(stmt, d1, taintedPath);
				}

				@Override
				public Set<Abstraction> getAliasesForMethod(Stmt stmt, Abstraction d1, Abstraction taintedPath) {
					if (!isSystemClass(stmt))
						return null;
					return wrapper.getAliasesForMethod(stmt, d1, taintedPath);
				}

			};

			Method setFallbackMethod = summaryWrapper.getClass().getMethod("setFallbackTaintWrapper",
					ITaintPropagationWrapper.class);
			setFallbackMethod.invoke(summaryWrapper, systemClassWrapper);

			return summaryWrapper;
		} catch (ClassNotFoundException | NoSuchMethodException ex) {
			System.err.println("Could not find library summary classes: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		} catch (InvocationTargetException ex) {
			System.err.println("Could not initialize library summaries: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		} catch (IllegalAccessException | InstantiationException ex) {
			System.err.println("Internal error in library summary initialization: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	private static void printUsage() {
		System.out.println("FlowDroid (c) Secure Software Engineering Group @ EC SPRIDE");
		System.out.println();
		System.out.println("Incorrect arguments: [0] = apk-file, [1] = android-jar-directory");
		System.out.println("Optional further parameters:");
		System.out.println("\t--TIMEOUT n Time out after n seconds");
		System.out.println("\t--SYSTIMEOUT n Hard time out (kill process) after n seconds, Unix only");
		System.out.println("\t--SINGLEFLOW Stop after finding first leak");
		System.out.println("\t--IMPLICIT Enable implicit flows");
		System.out.println("\t--NOSTATIC Disable static field tracking");
		System.out.println("\t--NOEXCEPTIONS Disable exception tracking");
		System.out.println("\t--APLENGTH n Set access path length to n");
		System.out.println("\t--CGALGO x Use callgraph algorithm x");
		System.out.println("\t--NOCALLBACKS Disable callback analysis");
		System.out.println("\t--LAYOUTMODE x Set UI control analysis mode to x");
		System.out.println("\t--ALIASFLOWINS Use a flow insensitive alias search");
		System.out.println("\t--NOPATHS Do not compute result paths");
		System.out.println("\t--AGGRESSIVETW Use taint wrapper in aggressive mode");
		System.out.println("\t--PATHALGO Use path reconstruction algorithm x");
		System.out.println("\t--LIBSUMTW Use library summary taint wrapper");
		System.out.println("\t--SUMMARYPATH Path to library summaries");
		System.out.println("\t--SYSFLOWS Also analyze classes in system packages");
		System.out.println("\t--NOTAINTWRAPPER Disables the use of taint wrappers");
		System.out.println("\t--NOTYPETIGHTENING Disables the use of taint wrappers");
		System.out.println("\t--LOGSOURCESANDSINKS Print out concrete source/sink instances");
		System.out.println("\t--CALLBACKANALYZER x Uses callback analysis algorithm x");
		System.out.println("\t--MAXTHREADNUM x Sets the maximum number of threads to be used by the analysis to x");
		System.out.println();
		System.out.println("Supported callgraph algorithms: AUTO, CHA, RTA, VTA, SPARK, GEOM");
		System.out.println("Supported layout mode algorithms: NONE, PWD, ALL");
		System.out.println("Supported path algorithms: CONTEXTSENSITIVE, CONTEXTINSENSITIVE, SOURCESONLY");
		System.out.println("Supported callback algorithms: DEFAULT, FAST");
	}

}
