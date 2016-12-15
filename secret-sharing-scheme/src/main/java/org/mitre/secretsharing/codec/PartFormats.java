/*

Copyright 2014 The MITRE Corporation

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

package org.mitre.secretsharing.codec;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mitre.secretsharing.BigPoint;
import org.mitre.secretsharing.Part;
import org.mitre.secretsharing.PerBytePart;
import org.mitre.secretsharing.util.BytesReadable;
import org.mitre.secretsharing.util.BytesWritable;
import org.mitre.secretsharing.util.InputValidation;

//TODO javadoc
public abstract class PartFormats {
	//TODO javadoc
	public static PartFormat<String> stringFormat(int version) {
		return StringFormats.values()[version];
	}
	
	//TODO javadoc
	public static PartFormat<byte[]> bytesFormat(int version) {
		return BytesFormats.values()[version];
	}
	
	//TODO javadoc
	public static Part parse(String data) {
		InputValidation.begin().when(data == null, "data is null").validate();
		return stringFormat(StringFormats.detectVersion(data)).parse(data);
	}
	
	//TODO javadoc
	public static Part parse(byte[] data) {
		InputValidation.begin().when(data == null, "data is null").validate();
		return bytesFormat(BytesFormats.detectVersion(data)).parse(data);
	}
	
	//TODO javadoc
	public static PartFormat<String> currentStringFormat() {
		StringFormats[] fmt = StringFormats.values();
		return fmt[fmt.length-1];
	}
	
	//TODO javadoc
	public static PartFormat<byte[]> currentBytesFormat() {
		BytesFormats[] fmt = BytesFormats.values();
		return fmt[fmt.length-1];
	}
	
	//TODO javadoc
	public static enum StringFormats implements PartFormat<String> {
		//TODO javadoc
		VERSION_0 {

			private final String V = new BytesWritable().writeInt(0).toString();
			private final String DASHED32 = "((" + Base32.DIGIT.pattern() + "|-)+)";
			private final Pattern VALID = Pattern.compile(V + ":" + DASHED32 + "//" + DASHED32); 
					
			
			@Override
			@SuppressWarnings("deprecation")
			public String format(Part part) {
				InputValidation.begin().when(part == null, "part is null").validate();
				StringBuilder sb = new StringBuilder();
				BytesWritable w = new BytesWritable();
				
				sb.append(V + ":");
				sb.append(dash(w
						.writeInt(part.getLength())
						.writeBigInteger(part.getModulus())
						.reset()));
				sb.append("//");
				Checksum cx = new Checksum(part.getPoint());
				sb.append(dash(w
						.writeBigInteger(part.getPoint().getX())
						.writeBigInteger(part.getPoint().getY())
						.writeBytes(cx.getChecksumBytes())
						.reset()));
				
				return sb.toString();
			}

			@Override
			@SuppressWarnings("deprecation")
			public Part parse(String data) {
				InputValidation.begin().when(data == null, "data is null").validate();
				Matcher m = VALID.matcher(data);
				if(!m.matches())
					throw new IllegalArgumentException("Not parseable by " + this);
				BytesReadable r;
				
				r = new BytesReadable(m.group(1).replace("-", ""));
				int length = r.readInt();
				BigInteger modulus = r.readBigInteger();
				
				r = new BytesReadable(m.group(3).replace("-", ""));
				BigInteger x = r.readBigInteger();
				BigInteger y = r.readBigInteger();
				BigPoint point = new BigPoint(x, y);
				Checksum cx = new Checksum(r);
				Part part = new Part(0, length, -1, modulus, point);
				if(!cx.equals(new Checksum(point)))
					throw new IllegalArgumentException("Checksum mismatch");
				return part;
			}

			@Override
			public int getVersion() {
				return 0;
			}
			
		},
		
		//TODO javadoc
		VERSION_1 {

			private final String V = new BytesWritable().writeInt(1).toString();
			private final String DASHED32 = "((" + Base32.DIGIT.pattern() + "|-)+)";
			private final Pattern VALID = Pattern.compile(V + ":" + DASHED32 + "//" + DASHED32); 
					
			
			@Override
			@SuppressWarnings("deprecation")
			public String format(Part part) {
				InputValidation.begin().when(part == null, "part is null").validate();
				StringBuilder sb = new StringBuilder();
				BytesWritable w = new BytesWritable();
				
				BigInteger mod = part.getModulus();
				
				sb.append(V + ":");
				sb.append(dash(w
						.writeInt(part.getLength())
						.writeInt(part.getRequiredParts())
						.writeBigInteger(mod)
						.reset()));
				sb.append("//");
				Checksum cx = new Checksum(part.getPoint());
				sb.append(dash(w
						.writeBigInteger(part.getPoint().getX())
						.writeBigInteger(part.getPoint().getY())
						.writeBytes(cx.getChecksumBytes())
						.reset()));
				
				return sb.toString();
			}

			@Override
			@SuppressWarnings("deprecation")
			public Part parse(String data) {
				InputValidation.begin().when(data == null, "data is null").validate();
				Matcher m = VALID.matcher(data);
				if(!m.matches())
					throw new IllegalArgumentException("Not parseable by " + this);
				BytesReadable r;
				
				r = new BytesReadable(m.group(1).replace("-", ""));
				int length = r.readInt();
				int requiredParts = r.readInt();
				BigInteger modulus = r.readBigInteger();
				
				r = new BytesReadable(m.group(3).replace("-", ""));
				BigInteger x = r.readBigInteger();
				BigInteger y = r.readBigInteger();
				BigPoint point = new BigPoint(x, y);
				Checksum cx = new Checksum(r);
				Part part;
				part = new Part(1, length, requiredParts, modulus, point);
				if(!cx.equals(new Checksum(point)))
					throw new IllegalArgumentException("Checksum mismatch");
				return part;
			}

			@Override
			public int getVersion() {
				return 1;
			}
			
		},

		//TODO javadoc
		VERSION_2 {

			private final String V = new BytesWritable().writeInt(2).toString();
			private final String DASHED32 = "((" + Base32.DIGIT.pattern() + "|-)+)";
			private final Pattern VALID = Pattern.compile(V + ":" + DASHED32 + "//" + DASHED32); 
					
			
			@Override
			@SuppressWarnings("deprecation")
			public String format(Part part) {
				InputValidation.begin().when(part == null, "part is null").validate();
				StringBuilder sb = new StringBuilder();
				BytesWritable w = new BytesWritable();
				
				BigInteger mod = part.getModulus();
				if(part instanceof PerBytePart)
					mod = BigInteger.valueOf(-1);
				
				sb.append(V + ":");
				sb.append(dash(w
						.writeInt(part.getLength())
						.writeInt(part.getRequiredParts())
						.writeBigInteger(mod)
						.reset()));
				sb.append("//");
				Checksum cx = new Checksum(part.getPoint());
				sb.append(dash(w
						.writeBigInteger(part.getPoint().getX())
						.writeBigInteger(part.getPoint().getY())
						.writeBytes(cx.getChecksumBytes())
						.reset()));
				
				return sb.toString();
			}

			@Override
			@SuppressWarnings("deprecation")
			public Part parse(String data) {
				InputValidation.begin().when(data == null, "data is null").validate();
				Matcher m = VALID.matcher(data);
				if(!m.matches())
					throw new IllegalArgumentException("Not parseable by " + this);
				BytesReadable r;
				
				r = new BytesReadable(m.group(1).replace("-", ""));
				int length = r.readInt();
				int requiredParts = r.readInt();
				BigInteger modulus = r.readBigInteger();
				
				r = new BytesReadable(m.group(3).replace("-", ""));
				BigInteger x = r.readBigInteger();
				BigInteger y = r.readBigInteger();
				BigPoint point = new BigPoint(x, y);
				Checksum cx = new Checksum(r);
				Part part;
				if(BigInteger.valueOf(-1).equals(modulus))
					part = new PerBytePart(2, length, requiredParts, point);
				else
					part = new Part(2, length, requiredParts, modulus, point);
				if(!cx.equals(new Checksum(point)))
					throw new IllegalArgumentException("Checksum mismatch");
				return part;
			}

			@Override
			public int getVersion() {
				return 2;
			}
			
		},

		//TODO javadoc
		VERSION_3 {

			private final String V = new BytesWritable().writeInt(3).toString();
			private final String DASHED32 = "((" + Base32.DIGIT.pattern() + "|-)+)";
			private final Pattern VALID = Pattern.compile(V + ":" + DASHED32 + "//" + DASHED32); 
					
			
			@Override
			public String format(Part part) {
				InputValidation.begin().when(part == null, "part is null").validate();
				StringBuilder sb = new StringBuilder();
				BytesWritable w = new BytesWritable();
				
				BigInteger mod = part.getModulus();
				if(part instanceof PerBytePart)
					mod = BigInteger.valueOf(-1);
				
				sb.append(V + ":");
				sb.append(dash(w
						.writeInt(part.getLength())
						.writeInt(part.getRequiredParts())
						.writeBigInteger(mod)
						.reset()));
				sb.append("//");
				sb.append(dash(w
						.writeBigInteger(part.getPoint().getX())
						.writeBigInteger(part.getPoint().getY())
						.reset()));
				
				return sb.toString();
			}

			@Override
			public Part parse(String data) {
				InputValidation.begin().when(data == null, "data is null").validate();
				Matcher m = VALID.matcher(data);
				if(!m.matches())
					throw new IllegalArgumentException("Not parseable by " + this);
				BytesReadable r;
				
				r = new BytesReadable(m.group(1).replace("-", ""));
				int length = r.readInt();
				int requiredParts = r.readInt();
				BigInteger modulus = r.readBigInteger();
				
				r = new BytesReadable(m.group(3).replace("-", ""));
				BigInteger x = r.readBigInteger();
				BigInteger y = r.readBigInteger();
				BigPoint point = new BigPoint(x, y);
				Part part;
				if(BigInteger.valueOf(-1).equals(modulus))
					part = new PerBytePart(3, length, requiredParts, point);
				else
					part = new Part(3, length, requiredParts, modulus, point);
				return part;
			}

			@Override
			public int getVersion() {
				return 3;
			}
			
		}

		;
		
		//TODO javadoc
		private static String dash(String s) {
			s = s.replaceAll("(......)", "$1-");
			if(s.endsWith("-"))
				s = s.substring(0, s.length()-1);
			return s;
		}
		
 		//TODO javadoc
		@Override
		public abstract String format(Part part);
		
		//TODO javadoc
		@Override
		public abstract Part parse(String data);
		
		//TODO javadoc
		@Override
		public abstract int getVersion();
		
		//TODO javadoc
		public static int detectVersion(String data) {
			InputValidation.begin().when(data == null, "data is null").validate();
			return new BytesReadable(data.replaceAll(":.*", "")).readInt();
		}
	}
	
	//TODO javadoc
	public static enum BytesFormats implements PartFormat<byte[]> {
		//TODO javadoc
		VERSION_0 {

			@Override
			public byte[] format(Part part) {
				InputValidation.begin().when(part == null, "part is null").validate();
				BytesWritable w = new BytesWritable();
				w.writeInt(0);
				w.writeInt(part.getLength());
				w.writeBigInteger(part.getModulus());
				w.writeBigInteger(part.getPoint().getX());
				w.writeBigInteger(part.getPoint().getY());
				return w.toByteArray();
			}

			@Override
			public Part parse(byte[] data) {
				InputValidation.begin().when(data == null, "data is null").validate();
				BytesReadable r = new BytesReadable(data);
				if(r.readInt() != 0)
					throw new IllegalArgumentException("Not parsable by " + this);
				int length = r.readInt();
				BigInteger modulus = r.readBigInteger();
				BigInteger x = r.readBigInteger();
				BigInteger y = r.readBigInteger();
				return new Part(0, length, -1, modulus, new BigPoint(x, y));
			}

			@Override
			public int getVersion() {
				return 0;
			}
			
		},
		
		//TODO javadoc
		VERSION_1 {

			@Override
			public byte[] format(Part part) {
				InputValidation.begin().when(part == null, "part is null").validate();
				BytesWritable w = new BytesWritable();
				w.writeInt(1);
				w.writeInt(part.getLength());
				w.writeInt(part.getRequiredParts());
				w.writeBigInteger(part.getModulus());
				w.writeBigInteger(part.getPoint().getX());
				w.writeBigInteger(part.getPoint().getY());
				return w.toByteArray();
			}

			@Override
			public Part parse(byte[] data) {
				InputValidation.begin().when(data == null, "data is null").validate();
				BytesReadable r = new BytesReadable(data);
				if(r.readInt() != 1)
					throw new IllegalArgumentException("Not parsable by " + this);
				int length = r.readInt();
				int requiredParts = r.readInt();
				BigInteger modulus = r.readBigInteger();
				BigInteger x = r.readBigInteger();
				BigInteger y = r.readBigInteger();
				return new Part(1, length, requiredParts, modulus, new BigPoint(x, y));
			}

			@Override
			public int getVersion() {
				return 1;
			}
			
		},

		//TODO javadoc
		VERSION_2 {

			@Override
			public byte[] format(Part part) {
				InputValidation.begin().when(part == null, "part is null").validate();
				BytesWritable w = new BytesWritable();
				w.writeInt(2);
				w.writeInt(part.getLength());
				w.writeInt(part.getRequiredParts());
				w.writeBigInteger((part instanceof PerBytePart) ? BigInteger.valueOf(-1) : part.getModulus());
				w.writeBigInteger(part.getPoint().getX());
				w.writeBigInteger(part.getPoint().getY());
				return w.toByteArray();
			}

			@Override
			public Part parse(byte[] data) {
				InputValidation.begin().when(data == null, "data is null").validate();
				BytesReadable r = new BytesReadable(data);
				if(r.readInt() != 2)
					throw new IllegalArgumentException("Not parsable by " + this);
				int length = r.readInt();
				int requiredParts = r.readInt();
				BigInteger modulus = r.readBigInteger();
				BigInteger x = r.readBigInteger();
				BigInteger y = r.readBigInteger();
				if(BigInteger.valueOf(-1).equals(modulus))
					return new PerBytePart(2, length, requiredParts, new BigPoint(x, y));
				else
					return new Part(2, length, requiredParts, modulus, new BigPoint(x, y));
			}

			@Override
			public int getVersion() {
				return 2;
			}
			
		}

		;
		
		//TODO javadoc
		@Override
		public abstract byte[] format(Part part);
		
		//TODO javadoc
		@Override
		public abstract Part parse(byte[] data);
		
		//TODO javadoc
		@Override
		public abstract int getVersion();

		//TODO javadoc
		public static int detectVersion(byte[] data) {
			InputValidation.begin().when(data == null, "data is null").validate();
			return new BytesReadable(data).readInt();
		}
	}

	private PartFormats() {}
}
