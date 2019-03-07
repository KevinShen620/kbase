/*
 * @(#)VamStringUtils.java 2012-8-4
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.vamsc.util.exception.VAMSCException;

/**
 * 对一些系统常用的字符串操作的封装,以后可能会陆续往这个类中添加方法
 * 
 * @author Kevin
 * 
 */
public class CommonStringUtils {

	private CommonStringUtils() {

	}

	private static final Pattern MAP_PATTERN;

	static {
		String key = "\\w+?";
		String value = "[^,]+";
		String split = ",?";
		MAP_PATTERN = Pattern
				.compile("(" + key + ")[:=](" + value + ")" + split);
	}

	/**
	 * 把字符串转换为一个map对象。
	 * <p>
	 * stringValue的形式必须是key:value,key:value的形式
	 * <p>
	 * 逗号用于分隔各个map，冒号用以区分key和value，暂时不支持key或value中包含这两个字符
	 * 
	 * 
	 * @param stringValue
	 * @return a map,and never returns <code>null</code>
	 */
	public static Map<String, String> buildMap(String stringValue) {
		Map<String, String> map = new HashMap<String, String>();
		Matcher m = MAP_PATTERN.matcher(stringValue);
		while (m.find()) {
			String key = m.group(1);
			String value = m.group(2);
			map.put(key, value);
		}
		return Collections.unmodifiableMap(map);
	}

	/**
	 * 把一个key和value都为字符串的map对象转为一个String对象，转换后的格式为“key1:value1,key2:value2的形式”
	 * 
	 * @param map
	 * @return
	 */
	public static String convertMap2String(Map<String, String> map) {
		StringBuilder builder = new StringBuilder(100);
		for (Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
				builder.append(key).append(":").append(value);
			}
		}
		return builder.toString();
	}

	/**
	 * 类似与{@link String#trim()}，但是支持中文空格和不间断空格
	 * 
	 * @param string
	 * @return trimed string
	 */
	public static String trim(String string) {
		int len = string.length();
		int count = len;
		int st = 0;
		int off = 0;
		char[] val = string.toCharArray();
		while ((st < len) && (trimAble(val[off + st]))) {
			st++;
		}
		while ((st < len) && (trimAble(val[off + len - 1]))) {
			len--;
		}
		return ((st > 0) || (len < count)) ? string.substring(st, len) : string;
	}

	private static final boolean trimAble(char c) {
		// \u3000为中文空格，160为不间断空格
		return c <= ' ' || c == '\u3000' || c == 160;
	}

	/**
	 * 解析表达是exp，exp中可以携带变量，变量的表示方式为#{var}，其中var为变量名。
	 * <p>
	 * 如果exp中的参数没有在arg中找到对应的值，则由defaultArgValue替换，如果defaultArgValue为
	 * <code>null</code>,则保留原先的变量表达式
	 * 
	 * @param exp
	 * @param arg
	 * @param defaultArgValue
	 * 
	 * @return
	 */
	public static String phraseExpression(String exp, Map<String, String> arg,
			String defaultArgValue) {
		String pattern = "#\\{\\w+\\}";
		Pattern pat = Pattern.compile(pattern);
		Matcher match = pat.matcher(exp);
		StringBuilder result = new StringBuilder();
		int endPoint = 0;
		while (match.find()) {
			int start = match.start();
			int end = match.end();
			if (start > 0) {
				result.append(exp.substring(endPoint, start));
			}
			String found = exp.substring(start, end);
			String var = exp.substring(start + 2, end - 1);// 提取变量
			String replacement = arg.get(var);
			if (replacement == null) {
				if (defaultArgValue == null) {
					replacement = found;
				} else {
					replacement = defaultArgValue;
				}
			}
			result.append(replacement);
			endPoint = end;
		}
		if (endPoint < exp.length()) {
			result.append(exp.substring(endPoint));
		}
		return result.toString();
	}

	/**
	 * the same as {@link #phraseExpression(String, Map, null)}
	 * 
	 * @param exp
	 * @param arg
	 * @return
	 */
	public static String phraseExpression(String exp, Map<String, String> arg) {
		return phraseExpression(exp, arg, null);
	}

	/**
	 * 
	 * @param pattern
	 * @param replacement
	 * @param string
	 * @return
	 */
	public static String replace(String pattern, String string,
			Replacement replacement) {
		Pattern pt = Pattern.compile(pattern);
		return replace(pt, string, replacement);
	}

	public static String replace(Pattern pattern, String string,
			Replacement replacement) {
		Matcher match = pattern.matcher(string);
		StringBuilder result = new StringBuilder(string.length() + 30);
		int endPoint = 0;
		int findex = 0;
		while (match.find()) {
			int start = match.start();
			if (start > 0) {
				result.append(string.substring(endPoint, start));
			}
			String rep = replacement.replaceFound(findex++, match);
			result.append(rep);
			endPoint = match.end();
		}
		if (endPoint < string.length()) {
			result.append(string.substring(endPoint));
		}
		return result.toString();
	}

	public static String joinString(Collection<String> collection, String join,
			String dec) {
		return joinString(collection.stream(), null, join, dec);
	}

	public static String joinString(String[] array, String join, String dec) {
		return joinString(Arrays.stream(array), null, join, dec);
	}

	/**
	 * 连接collection，如果元素包含需要转移的字符，则进行转移,主要用于sql查询。
	 * <p>
	 * 会进行转移的字符包括：%，空格，\,^,单引号
	 * 
	 * @param collection
	 * @param join
	 * @param dec
	 * @return
	 */
	public static String joinStringWithQueryable(Collection<String> collection,
			String join, String dec) {
		return joinString(collection.stream(),
				CommonStringUtils::escapeSearchValue, join, dec);
	}

	/**
	 * @see #joinStringWithQueryable(Collection, String, String)
	 * @param array
	 * @param join
	 * @param dec
	 * @return
	 */
	public static String joinStringWithQueryable(String[] array, String join,
			String dec) {
		return joinString(Arrays.stream(array),
				CommonStringUtils::escapeSearchValue, join, dec);
	}

	public static String joinString(Collection<String> collection,
			Function<String, String> elementConvert, String join, String dec) {
		return joinString(collection.stream(), elementConvert, join, dec);
	}

	public static String joinString(String[] array,
			Function<String, String> elementConvert, String join, String dec) {
		return joinString(Arrays.stream(array), elementConvert, join, dec);
	}

	/**
	 * 
	 * @param stream
	 * @param elementConvert
	 *            可以为空
	 * @param join
	 * @param dec
	 * @return
	 */
	public static String joinString(Stream<String> stream,
			Function<String, String> elementConvert, String join, String dec) {
		String _dec = dec == null ? "" : dec;
		String _join = join == null ? "" : join;
		StringBuilder builder = new StringBuilder();
		stream.forEach((String str) -> {
			String _str = elementConvert == null ? str
					: elementConvert.apply(str);
			if (StringUtils.isNotEmpty(_str)) {
				builder.append(_dec).append(_str).append(_dec).append(_join);
			}
		});
		if (builder.length() > 0) {
			builder.delete(builder.length() - _join.length(), builder.length());
		}
		return builder.toString();
	}

	public static String joinString(Stream<String> stream, String join,
			Function<String, String> elementConvert) {
		StringBuilder builder = new StringBuilder();
		String _join = join == null ? "" : join;
		stream.forEach(str -> {
			String _str = elementConvert == null ? str
					: elementConvert.apply(str);
			if (StringUtils.isNotEmpty(_str)) {
				builder.append(_str).append(_join);
			}
		});
		if (builder.length() > 0) {
			builder.delete(builder.length() - _join.length(), builder.length());
		}
		return builder.toString();

	}

	public static String joinString(Stream<String> stream, String join,
			String dec) {
		return joinString(stream, null, join, dec);
	}

	/**
	 * 在执行查询的时候，如果碰到查询字符串中包含%，',这些符号，需要转移，否则组装成查询语句的时候会出错
	 * 
	 * @param oriStrValue
	 * @return
	 */
	public static String escapeSearchValue(String oriStrValue) {
		int length = oriStrValue.length();
		StringBuilder ori = new StringBuilder(length + 10);
		boolean line = false;
		for (int i = 0; i < length; ++i) {
			char _char = oriStrValue.charAt(i);
			String toAppend = null;
			switch (_char) {
			case '&':
				toAppend = line ? "-&amp;" : "&amp;";
				line = false;
				break;
			case '<':
				toAppend = line ? "-&lt;" : "&lt;";
				line = false;
				break;
			case '>':
				toAppend = line ? "-&gt;" : "&gt;";
				line = false;
				break;
			case ';':
			case '\'':
			case '"':
			case '\\':
			case '/':
			case '%':
				toAppend = line ? "-" : null;
				line = false;
				break;
			case '-':
				if (line) {
					// 忽略'--'
					line = false;
				} else {
					line = true;
				}
				break;
			default:
				toAppend = line ? "-" + _char : String.valueOf(_char);
				line = false;
				break;
			}
			if (toAppend != null) {
				ori.append(toAppend);
			}
		}
		return ori.toString();
	}

	// private static boolean needEscape(char c) {
	// return c == '%' || c == ' ' || c == '\"' || c == '\\' || c == '^';
	// }

	public static interface Replacement {
		/**
		 * 
		 * @param foundIndex
		 *            第foundIndex次出现，从0开始
		 * @param currentMatcher
		 *            一个match对象
		 * @return 替换后的值
		 */
		String replaceFound(int foundIndex, Matcher currentMatcher);
	}

	public static interface FoundHandler {
		void handle(int lastEndIndex, int thisIndex, Matcher thisMatcher);
	}

	public static String randomeString(int size) {
		Random random = new Random();
		StringBuilder code = new StringBuilder();
		int len = 'a' - 0;
		for (int i = 0; i < size; i++) {
			int ivalue = random.nextInt(36);
			char c;
			if (ivalue <= 9) {
				c = String.valueOf(ivalue).charAt(0);
			} else {
				c = (char) (ivalue - 10 + len);
			}
			code.append(c);
		}
		return code.toString();

	}

	/**
	 * 半角，全角字符判断
	 * 
	 * @param c
	 * @return <code>true</code>：半角，<code>false</code>:全角
	 */
	public static boolean isDBCCase(char c) {
		// 基本拉丁字母（即键盘上可见的，空格、数字、字母、符号）
		if (c >= 32 && c <= 127) {
			return true;
		}
		// 日文半角片假名和符号
		else if (c >= 65377 && c <= 65439) {
			return true;
		}
		return false;
	}

	public static String decodeToString(ByteBuffer buffer, Charset charset) {
		CharsetDecoder decoder = charset.newDecoder();
		try {
			CharBuffer charbuffer = decoder.decode(buffer);
			return charbuffer.toString();
		} catch (CharacterCodingException e) {
			throw new VAMSCException(e);
		}
	}

}
