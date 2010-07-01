/*
 * Anarres C Preprocessor
 * Copyright (c) 2007-2008, Shevek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.anarres.cpp;

import static org.anarres.cpp.Token.CCOMMENT;
import static org.anarres.cpp.Token.CPPCOMMENT;
import static org.anarres.cpp.Token.EOF;

import java.io.IOException;
import java.io.Reader;

import de.fosd.typechef.featureexpr.FeatureExpr;

/**
 * A Reader wrapper around the Preprocessor.
 *
 * This is a utility class to provide a transparent {@link Reader}
 * which preprocesses the input text.
 *
 * @see Preprocessor
 * @see Reader
 */
public class CppReader extends Reader {

	private Preprocessor	cpp;
	private String			token;
	private int				idx;

	public CppReader(final Reader r) {
		cpp = new Preprocessor(new LexerSource(r, true) {
			@Override
			public String getName() {
				return "<CppReader Input@" +
						System.identityHashCode(r) + ">";
			}
		});
		token = "";
		idx = 0;
	}

	public CppReader(Preprocessor p) {
		cpp = p;
		token = "";
		idx = 0;
	}

	/**
	 * Returns the Preprocessor used by this CppReader.
	 */
	public Preprocessor getPreprocessor() {
		return cpp;
	}

	/**
	 * Defines the given name as a macro.
	 *
	 * This is a convnience method.
	 */
	public void addMacro(String name)
						throws LexerException {
		cpp.addMacro(name,new FeatureExpr().base());
	}
 
	/**
	 * Defines the given name as a macro.
	 *
	 * This is a convnience method.
	 */
	public void addMacro(String name, String value)
						throws LexerException {
		cpp.addMacro(name, new FeatureExpr().base(), value);
	}

	private boolean refill()
						throws IOException {
		try {
			assert cpp != null : "cpp is null : was it closed?";
			if (token == null)
				return false;
			while (idx >= token.length()) {
				Token	tok = cpp.getNextToken();
				switch (tok.getType()) {
					case EOF:
						token = null;
						return false;
					case CCOMMENT:
					case CPPCOMMENT:
						if (!cpp.getFeature(Feature.KEEPCOMMENTS)) {
							token = " ";
							break;
						}
					default:
						token = tok.getText();
						break;
				}
				idx = 0;
			}
			return true;
		}
		catch (LexerException e) {
			/* Never happens.
			if (e.getCause() instanceof IOException)
				throw (IOException)e.getCause();
			*/
			IOException	ie = new IOException(String.valueOf(e));
			ie.initCause(e);
			throw ie;
		}
	}

	public int read()
						throws IOException {
		if (!refill())
			return -1;
		return token.charAt(idx++);
	}

	/* XXX Very slow and inefficient. */
	public int read(char cbuf[], int off, int len)
						throws IOException {
		if (token == null)
			return -1;
		for (int i = 0; i < len; i++) {
			int	ch = read();
			if (ch == -1)
				return i;
			cbuf[off + i] = (char)ch;
		}
		return len;
	}

	public void close()
						throws IOException {
		if (cpp != null) {
			cpp.close();
			cpp = null;
		}
		token = null;
	}

}
