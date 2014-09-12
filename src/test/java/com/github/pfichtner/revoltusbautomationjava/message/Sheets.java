package com.github.pfichtner.revoltusbautomationjava.message;

import java.util.Iterator;

public final class Sheets {

	private Sheets() {
		super();
	}

	public static ColumnHeader col(String string) {
		return new ColumnHeader(string);
	}

	public static Iterable<ColumnHeader> columnHeaders(final int start,
			final int end) {
		return new Iterable<ColumnHeader>() {

			public Iterator<ColumnHeader> iterator() {
				return new Iterator<ColumnHeader>() {

					private int i = start;

					public void remove() {
						throw new UnsupportedOperationException();
					}

					public ColumnHeader next() {
						return col(intToColumn(i++));
					}

					public boolean hasNext() {
						return i < end;
					}
				};
			}
		};
	}

	private static String intToColumn(int i) {
		String uppersChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int len = uppersChars.length();
		return (i > len - 1 ? intToColumn(i / len - 1) : "")
				+ uppersChars.charAt(i % len);
	}

}
