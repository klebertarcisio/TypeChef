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

/**
 * An internal exception.
 *
 * This exception is thrown when an internal state violation is
 * encountered. This should never happen. If it ever happens, please
 * report it as a bug.
 */
@SuppressWarnings("serial")
public class InternalException extends RuntimeException {
	public InternalException(String msg) {
		super(msg);
	}
}
