package com.popoaichuiniu.intentGen;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Globals {
	

	public static String[] bundleExtraDataMethods = {"getString","getShort","getFloat","getBoolean","getDouble","getInt","getLong","hasExtra","containsKey"};
	public static String[] getBundleMethods = {"getExtras","getBundleExtra"};
	public static String[] categoryMethods = {"hasCategory","getCategories"};
	public static String[] stringReturningIntentMethods = {"getAction","getStringExtra"};
	public static Set<String> bundleExtraDataMethodsSet = new LinkedHashSet<String>(Arrays.asList(bundleExtraDataMethods));
	public static Set<String> categoryMethodsSet = new LinkedHashSet<String>(Arrays.asList(categoryMethods));
	public static Set<String> stringReturningIntentMethodsSet = new LinkedHashSet<String>(Arrays.asList(stringReturningIntentMethods));
	public static Set<String> getBundleMethodsSet = new LinkedHashSet<String>(Arrays.asList(getBundleMethods));
	
	public static String[] androidPkgPrefixes = {"android.","com.android.","dalvik.","java.","javax.","junit.","org.apache.","org.json.","org.w3c.dom.","org.xml.sax","org.xmlpull."};
	public static Set<String> androidPkgPrefixesSet = new LinkedHashSet<String>(Arrays.asList(androidPkgPrefixes));

	public static String[] intentGetMethod={
			"getAction","getBooleanArrayExtra","getBooleanExtra","getBundleExtra","getByteArrayExtra","getByteExtra","getCategories","getCharArrayExtra","getCharExtra","getCharSequenceArrayExtra","getCharSequenceArrayListExtra",

			"getCharSequenceExtra",

			"getClipData",

			"getComponent",

			"getData",

			"getDataString",

			"getDoubleArrayExtra",

			"getDoubleExtra",

			"getExtras",

			"getFlags",

			"getFloatArrayExtra",

			"getFloatExtra",

			"getIntArrayExtra",

			"getIntExtra",

			"getIntegerArrayListExtra",

			"getIntent",

			"getIntentOld",
			"getLongArrayExtra",

			"getLongExtra",

			"getPackage",

			"getParcelableArrayExtra",

			"getParcelableArrayListExtra",

			"getParcelableExtra",

			"getScheme",

			"getSelector",

			"getSerializableExtra",

			"getShortArrayExtra",

			"getShortExtra",

			"getSourceBounds",

			"getStringArrayExtra",

			"getStringArrayListExtra",

			"getStringExtra",

			"getType",

			"hasCategory",
			"hasExtra",

			"hasFileDescriptors"};




}