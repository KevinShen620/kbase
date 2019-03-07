/*
 * @(#) OsProcessUtil.java 2015年7月27日
 *
 */
package kevsn.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

import com.vamsc.util.exception.VAMSCException;

import kevsn.util.io.IOUtil;

/**
 * @author Kevin
 *
 */
public class OsProcessUtil {

	private OsProcessUtil() {

	}

	private static ResultInfo _getExecutedProcessResultInfo(Process process,
			boolean getError, boolean getInfo) throws Exception {
		String error = null;
		InputStream errorStream = process.getErrorStream();
		ResultInfo resultInfo = new ResultInfo();
		if (errorStream != null && getError) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(errorStream))) {
				error = IOUtil.readTextContent(reader);
				resultInfo.error = error;
			}
		}
		InputStream instream = process.getInputStream();
		if (instream != null && getInfo) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(instream))) {
				String info = IOUtil.readTextContent(reader);
				resultInfo.info = info;
			}
		}
		return resultInfo;
	}

	public static ResultInfo getExecutedProcessResultInfo(Process process,
			boolean getError, boolean getInfo) {
		try {
			return _getExecutedProcessResultInfo(process, getError, getInfo);
		} catch (Exception e) {
			throw new VAMSCException(e);
		}
	}

	public static class ResultInfo {

		private String info;

		private String error;

		private ResultInfo() {

		}

		public String getInfo() {
			return info;
		}

		public String getError() {
			return error;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (StringUtils.isNotEmpty(info)) {
				builder.append("info:").append(info).append(",");
			}
			if (StringUtils.isNotEmpty(error)) {
				builder.append("error:").append(error).append(",");
			}
			if (builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
			return builder.toString();
		}
	}
}
