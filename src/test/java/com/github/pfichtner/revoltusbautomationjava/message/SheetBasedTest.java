package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.binToInt;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hex2Int;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToBin;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToHex;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.shiftLeft;
import static com.github.pfichtner.revoltusbautomationjava.message.Sheets.col;
import static com.github.pfichtner.revoltusbautomationjava.message.Sheets.columnHeaders;
import static com.github.pfichtner.revoltusbautomationjava.message.Strings.padLeft;
import static com.github.pfichtner.revoltusbautomationjava.message.Strings.padRight;
import static com.github.pfichtner.revoltusbautomationjava.message.Strings.trim;
import static java.lang.Integer.parseInt;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SheetBasedTest {

	private Map<ColumnHeader, String> row;

	public SheetBasedTest(Map<ColumnHeader, String> row) {
		this.row = row;
	}

	@Test
	public void testRow() {
		int rawId = parseInt(this.row.get(col("F")));
		String msgId = padLeft(intToHex(rawId), '0', 4);
		int rawFrame = parseInt(this.row.get(col("E"))); // resends

		Function function = Function.of(
				Outlet.forString(this.row.get(col("B"))),
				State.forString(this.row.get(col("C"))));

		MessageGenerator generator = new MessageGenerator().rawFrames(rawFrame)
				.rawId(rawId).msgFin(this.row.get(col("Z")));

		Map<ColumnHeader, String> actual = new HashMap<ColumnHeader, String>();

		actual.put(col("K"), padRight(function.asHex(), '0', 2));
		actual.put(
				col("L"),
				padLeft(intToBin(shiftLeft(binToInt(function.asByte()), 4)),
						'0', 8));
		actual.put(col("M"), function.asByte());
		actual.put(col("N"), String.valueOf(function.asInt()));
		actual.put(col("O"), String.valueOf(generator.getSum(function)));
		actual.put(col("P"), generator.getChecksum(function));
		actual.put(col("Q"), generator.getChecksum(function));
		String checkSum = padLeft(
				intToBin(hex2Int(generator.getChecksum(function))), '0', 8);
		actual.put(col("R"), checkSum);
		String checksumByte1 = checkSum.substring(0, 4);
		String checksumByte2 = checkSum.substring(4);
		actual.put(col("S"), checksumByte1);
		actual.put(col("T"), checksumByte2);
		actual.put(col("U"), String.valueOf(binToInt(checksumByte1)));
		actual.put(col("V"), String.valueOf(binToInt(checksumByte2)));
		actual.put(col("W"), String.valueOf(generator.getRawChecksum(function)));
		actual.put(col("Y"), padLeft(intToHex(rawFrame), '0', 2));
		assertRowEquals(row, actual);

		assertEquals(
				row.get(col("AA")).toLowerCase(),
				msgId + padRight(intToHex(function.asInt()), '0', 2)
						+ generator.getChecksum(function).toLowerCase()
						+ this.row.get(col("X"))
						+ padLeft(intToHex(rawFrame), '0', 2)
						+ this.row.get(col("Z")).toLowerCase());
		assertEquals(this.row.get(col("H")).toLowerCase(), generator
				.hexMessage(function).toLowerCase());
	}

	private void assertRowEquals(Map<ColumnHeader, String> expected,
			Map<ColumnHeader, String> actual) {
		for (Entry<ColumnHeader, String> entry : actual.entrySet()) {
			assertEquals("Key " + entry.getKey(),
					trim(expected.get(entry.getKey()).toLowerCase(), '0'),
					trim(entry.getValue().toLowerCase(), '0'));
		}
	}

	 @Parameters
	public static List<Object[]> parametersFromFile() throws IOException {
		return toObjectArrayList(loadSheet(SheetBasedTest.class
				.getResource("/revolt-usb-hid-payload-protocol.ods")));
	}

//	@Parameters
	public static List<Object[]> parametersFromURL() throws IOException {
		return toObjectArrayList(loadSheet(new URL(
				"https://github.com/kralo/revolt-usb-automation-python/blob/master/revolt-usb-hid-payload-protocol.ods?raw=true")));
	}

	private static List<Object[]> toObjectArrayList(
			List<Map<ColumnHeader, String>> content) {
		List<Object[]> objects = new ArrayList<Object[]>();
		for (Map<ColumnHeader, String> map : content) {
			objects.add(new Object[] { map });
		}
		return objects;
	}

	private static List<Map<ColumnHeader, String>> loadSheet(URL url)
			throws IOException {
		InputStream is = url.openStream();
		try {
			return mapContent(is);
		} finally {
			is.close();
		}
	}

	private static List<Map<ColumnHeader, String>> mapContent(InputStream is)
			throws IOException {
		List<Map<ColumnHeader, String>> rows = new ArrayList<Map<ColumnHeader, String>>();
		Sheet sheet = new ODPackage(is).getSpreadSheet().getSheet(0);
		int row = 1;
		Map<ColumnHeader, String> data;
		while (!rowIsEmpty(data = getColumns(sheet, row++))) {
			rows.add(data);
		}
		return rows;
	}

	private static Map<ColumnHeader, String> getColumns(Sheet sheet, int row) {
		Map<ColumnHeader, String> data = new LinkedHashMap<ColumnHeader, String>();
		int column = 0;
		for (ColumnHeader header : columnHeaders(0, 27)) {
			data.put(header,
					String.valueOf(sheet.getCellAt(column++, row).getValue()));
		}
		return data;
	}

	private static boolean rowIsEmpty(Map<ColumnHeader, String> row) {
		Collection<String> values = row.values();
		for (String string : values) {
			if (string != null && !string.isEmpty()) {
				return false;
			}
		}
		return true;
	}

}
