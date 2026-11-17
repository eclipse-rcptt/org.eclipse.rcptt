/*******************************************************************************
 * Copyright (c) 2026 Aleksandar Kurtakov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Aleksandar Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.core.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.rcptt.ecl.core.util.ECLBinaryResourceImpl;
import org.eclipse.rcptt.ecl.core.util.ECLBinaryResourceImpl.EObjectInputStream;
import org.eclipse.rcptt.ecl.core.util.ECLBinaryResourceImpl.EObjectOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Byte arrays are serialized with a bulk hex encoder. The bytes on the wire
 * must stay identical to the generic EMF data type path so that IDE and AUT of
 * different versions keep understanding each other.
 */
public class ECLBinaryResourceImplTest {

	private static final EcoreFactory ECORE = EcoreFactory.eINSTANCE;

	private static EPackage ePackage;
	private static EClass rootClass;
	private static EClass fileClass;
	private static EAttribute contentAttribute;
	private static EReference filesReference;

	@BeforeClass
	public static void createModel() {
		ePackage = ECORE.createEPackage();
		ePackage.setName("bytes");
		ePackage.setNsPrefix("bytes");
		ePackage.setNsURI("http://eclipse.org/rcptt/ecl/core/tests/bytes");

		fileClass = ECORE.createEClass();
		fileClass.setName("File");
		contentAttribute = ECORE.createEAttribute();
		contentAttribute.setName("content");
		contentAttribute.setEType(EcorePackage.Literals.EBYTE_ARRAY);
		fileClass.getEStructuralFeatures().add(contentAttribute);

		rootClass = ECORE.createEClass();
		rootClass.setName("Root");
		filesReference = ECORE.createEReference();
		filesReference.setName("files");
		filesReference.setEType(fileClass);
		filesReference.setContainment(true);
		filesReference.setUpperBound(-1);
		rootClass.getEStructuralFeatures().add(filesReference);

		ePackage.getEClassifiers().add(rootClass);
		ePackage.getEClassifiers().add(fileClass);
		EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
	}

	private static List<byte[]> samples() {
		List<byte[]> result = new ArrayList<>();
		result.add(new byte[0]);
		byte[] allValues = new byte[256];
		for (int i = 0; i < allValues.length; i++) {
			allValues[i] = (byte) i;
		}
		result.add(allValues);
		Random random = new Random(42);
		// Larger than the internal buffer and not aligned to it
		for (int size : new int[] { 32 * 1024, 64 * 1024, 200_001 }) {
			byte[] data = new byte[size];
			random.nextBytes(data);
			result.add(data);
		}
		return result;
	}

	private static byte[] writeGeneric(byte[] value) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new EObjectOutputStream(out, null)
				.writeString(ECORE.convertToString(EcorePackage.Literals.EBYTE_ARRAY, value));
		return out.toByteArray();
	}

	private static byte[] writeHex(byte[] value) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new EObjectOutputStream(out, null).writeHexByteArray(value);
		return out.toByteArray();
	}

	private static byte[] readGeneric(byte[] stream) throws IOException {
		String literal = new EObjectInputStream(new ByteArrayInputStream(stream), null).readString();
		return (byte[]) ECORE.createFromString(EcorePackage.Literals.EBYTE_ARRAY, literal);
	}

	private static byte[] readHex(byte[] stream) throws IOException {
		return new EObjectInputStream(new ByteArrayInputStream(stream), null).readHexByteArray();
	}

	@Test
	public void hexEncodingMatchesGenericEncoding() throws IOException {
		assertArrayEquals(writeGeneric(null), writeHex(null));
		for (byte[] value : samples()) {
			assertArrayEquals("size " + value.length, writeGeneric(value), writeHex(value));
		}
	}

	@Test
	public void hexDecodingReadsBothEncodings() throws IOException {
		assertNull(readHex(writeGeneric(null)));
		assertNull(readGeneric(writeHex(null)));
		for (byte[] value : samples()) {
			assertArrayEquals("size " + value.length, value, readHex(writeGeneric(value)));
			assertArrayEquals("size " + value.length, value, readGeneric(writeHex(value)));
		}
	}

	@Test
	public void hexDecodingAcceptsLowerCase() throws IOException {
		byte[] value = { (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x01 };
		String literal = ECORE.convertToString(EcorePackage.Literals.EBYTE_ARRAY, value).toLowerCase();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new EObjectOutputStream(out, null).writeString(literal);
		assertArrayEquals(value, readHex(out.toByteArray()));
	}

	@Test
	public void hexDecodingRejectsMalformedInput() throws IOException {
		for (String literal : new String[] { "ABC", "0G", "zz" }) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new EObjectOutputStream(out, null).writeString(literal);
			assertThrows(literal, IOException.class, () -> readHex(out.toByteArray()));
		}
		byte[] truncated = writeHex(new byte[] { 1, 2, 3 });
		assertThrows(IOException.class, () -> readHex(Arrays.copyOf(truncated, truncated.length - 1)));
	}

	@Test
	public void hexDecodingHandlesShortReads() throws IOException {
		byte[] value = samples().get(3);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		EObjectOutputStream writer = new EObjectOutputStream(out, null);
		writer.writeHexByteArray(value);
		writer.writeString("next");
		// Like ZipInputStream, return fewer bytes than requested
		InputStream shortReads = new FilterInputStream(new ByteArrayInputStream(out.toByteArray())) {
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return super.read(b, off, Math.min(len, 3));
			}
		};
		EObjectInputStream reader = new EObjectInputStream(shortReads, null);
		assertArrayEquals(value, reader.readHexByteArray());
		assertEquals("next", reader.readString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void resourceRoundTrip() throws IOException {
		List<byte[]> values = new ArrayList<>(samples());
		values.add(null);
		EObject root = ePackage.getEFactoryInstance().create(rootClass);
		for (byte[] value : values) {
			EObject file = ePackage.getEFactoryInstance().create(fileClass);
			file.eSet(contentAttribute, value);
			((List<EObject>) root.eGet(filesReference)).add(file);
		}
		Resource out = new ECLBinaryResourceImpl();
		out.getContents().add(root);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		out.save(bytes, new HashMap<>());

		Resource in = new ECLBinaryResourceImpl();
		in.load(new ByteArrayInputStream(bytes.toByteArray()), new HashMap<>());
		List<EObject> files = (List<EObject>) in.getContents().get(0).eGet(filesReference);
		assertEquals(values.size(), files.size());
		for (int i = 0; i < values.size(); i++) {
			assertArrayEquals("file " + i, values.get(i), (byte[]) files.get(i).eGet(contentAttribute));
		}
	}

	@Test
	public void resourceStreamIsHexOnTheWire() throws IOException {
		byte[] value = { 0x12, (byte) 0xAB };
		EObject file = ePackage.getEFactoryInstance().create(fileClass);
		file.eSet(contentAttribute, value);
		Resource out = new ECLBinaryResourceImpl();
		out.getContents().add(file);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		out.save(bytes, new HashMap<>());
		String stream = new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1);
		assertEquals(1, stream.split("12AB", -1).length - 1);
	}
}
