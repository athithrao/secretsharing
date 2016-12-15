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

package org.mitre.secretsharing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PerByteSecretsTest {

	@Parameters
	public static Iterable<Object[]> params() {
		List<Object[]> p = new ArrayList<Object[]>();
		Random rnd = new Random(0L);
		for(int i = 1; i <= 32; i++) {
			byte[] b = new byte[i];
			rnd.nextBytes(b);
			p.add(new Object[] {b});
		}
		return p;
	}

	private byte[] b;
	
	public PerByteSecretsTest(byte[] b) {
		this.b = b;
	}
	
	@Test
	public void testSecret() {
		PerBytePart[] parts = Secrets.splitPerByte(b, 5, 3, new Random(0L));
		byte[] r = Secrets.joinPerByte(Arrays.copyOf(parts, 3));
		Assert.assertTrue(Arrays.equals(b, r));
	}
}
