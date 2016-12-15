/*

Copyright 2016 The MITRE Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This project contains content developed by The MITRE Corporation. If this 
code is used in a deployment or embedded within another project, it is 
requested that you send an email to opensource@mitre.org in order to let 
us know where this software is being used.

 */

package org.mitre.secretsharing.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class BigIntegersTest {
	@Test
	public void testRandom() {
		BigInteger[] bigs = BigIntegers.random(
				BigInteger.ZERO,
				BigInteger.TEN,
				new Random(),
				1000,
				4);
		Set<BigInteger> numerals = new HashSet<BigInteger>(Arrays.asList(bigs));
		Set<BigInteger> expected = new HashSet<BigInteger>(Arrays.asList(
				BigInteger.valueOf(0),
				BigInteger.valueOf(1),
				BigInteger.valueOf(2),
				BigInteger.valueOf(3),
				BigInteger.valueOf(4),
				BigInteger.valueOf(5),
				BigInteger.valueOf(6),
				BigInteger.valueOf(7),
				BigInteger.valueOf(8),
				BigInteger.valueOf(9)
				));
		Assert.assertEquals(expected, numerals);
	}
}
