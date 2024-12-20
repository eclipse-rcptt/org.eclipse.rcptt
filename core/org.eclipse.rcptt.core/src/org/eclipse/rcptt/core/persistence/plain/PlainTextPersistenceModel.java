/*******************************************************************************
 * Copyright (c) 2009, 2020 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.core.persistence.plain;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.DeprecationSwitch;
import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.VerificationTypeManager;
import org.eclipse.rcptt.core.persistence.BasePersistenceModel;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.persistence.plain.PlainReader.Entry;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.ProjectMetadata;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.scenario.ScenarioProperty;
import org.eclipse.rcptt.core.scenario.TestSuite;
import org.eclipse.rcptt.core.scenario.TestSuiteItem;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.core.workspace.Q7Utils;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.util.FileUtil;
import org.osgi.framework.Bundle;

public class PlainTextPersistenceModel extends BasePersistenceModel implements IPlainConstants {
	private static final String KIND_PROJECT_METADATA = "projectMetadata";
	private static final String KIND_TESTSUITE = "testsuite";
	private static final String KIND_CONTEXT = "context";
	private static final String KIND_VERIFICATION = "verification";
	private static final String KIND_TESTCASE = "testcase";

	private static final String TESTCASE_ITEMS = "testcase-items";
	private static final String ATTR_ELEMENT_TYPE = "Element-Type";
	private static final String ATTR_MANUALY_ORDERED = "Manually-Ordered";
	private static final String ATTR_CONTEXTS = "Contexts";
	private static final String ATTR_VERIFICATIONS = "Verifications";
	private static final String ATTR_IGNORED_TESTCASES = "Ignored-TestCases";
	private static final String ATTR_CONTEXT_TYPE = "Context-Type";
	private static final String ATTR_VERIFICATION_TYPE = "Verification-Type";

	private static final String ATTR_TESTCASE_TYPE = "Testcase-Type";
	private static final String ATTR_EXTERNAL_REFERENCE = "External-Reference";
	private static final String ATTR_PROPERTY_PREFIX = "Property-";
	private static final String ATTR_ELEMENT_VERSION = "Element-Version";
	// private static final String ATTR_DESCRIPTION = "Description";
	private static final String ATTR_ID = "Id";
	private static final String ATTR_TAGS = "Tags";
	private static final String ATTR_ELEMENT_NAME = "Element-Name";
	private Map<String, String> masterAttributes = new HashMap<String, String>();
	private String plainStoreFormat = PLAIN_HEADER;
	private static final DeprecationSwitch ADD_SAVE_TIME = new DeprecationSwitch("save_time", "RCPTT is removing Save-Time header from storage format. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=483929", false, false); 

	public PlainTextPersistenceModel(Resource element) {
		super(element);
	}

	public boolean isSupportMultiItems() {
		return true;
	}

	@Override
	protected synchronized void doExtractAll(InputStream contents) throws IOException {
		PlainReader reader = null;
		try {
			reader = new PlainReader(contents);
			Map<String, String> header = reader.readHeader();
			if (header != null) {
				String version = header.get(IPlainConstants.ATTR_FORMAT_VERSION);
				if (version != null && version.equals(IPlainConstants.FORMAT_VERSION)) {
					while (true) {
						Entry entry = reader.readEntry();
						if (entry == null) {
							break;
						}
						File file = files.get(entry.name);
						if (file != null && !file.exists()) {
							OutputStream outputStream = internalStore(entry.name);
							try {
								byte[] data = null;
								if (entry.getContent() instanceof String) {
									data = ((String) entry.getContent()).getBytes(ENCODING);
								} else if (entry.getContent() instanceof byte[]) {
									data = (byte[]) entry.getContent();
								}
								if (data != null) {
									FileUtil.copy(new ByteArrayInputStream(data), outputStream);
								} else {
									throw new IOException("Wrong Plain file format");
								}
							} finally {
								outputStream.close();
							}
						}
					}
				} else {
					throw new Exception("Q7 plain format version is unsupported.");
				}
			}
		} catch (Exception e) {
			RcpttPlugin.log(e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	protected void doExtractFile(String fName, InputStream contents) throws IOException {
		PlainReader reader = null;
		try {
			reader = new PlainReader(contents);
			Map<String, String> header = reader.readHeader();
			if (header != null) {
				String version = header.get(IPlainConstants.ATTR_FORMAT_VERSION);
				if (version != null && version.equals(IPlainConstants.FORMAT_VERSION)) {
					while (true) {
						Entry entry = reader.readEntry();
						if (entry == null) {
							break;
						}
						if (fName.equals(entry.name)) {
							OutputStream outputStream = internalStore(fName);
							byte[] data = null;
							if (entry.getContent() instanceof String) {
								data = ((String) entry.getContent()).getBytes(ENCODING);
							} else if (entry.getContent() instanceof byte[]) {
								data = (byte[]) entry.getContent();
							}
							if (data != null) {
								FileUtil.copy(new ByteArrayInputStream(data), outputStream);
							} else {
								outputStream.close();
								delete(fName);
								throw new IOException("Wrong Plain file format");
							}
							outputStream.close();
						}
					}
				} else {
					throw new Exception("Q7 plain format version is unsupported.");
				}
			}
		} catch (PlainFormatException e) {
			IFile file = element != null ? Q7Utils.getLocation(element) : null;
			if (file != null) {
				e.setFileName(file.getLocation().toString());
			}
			RcpttPlugin.log(e);
		} catch (Exception e) {
			RcpttPlugin.log(e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	protected synchronized void doReadIndex(InputStream contents) {
		PlainReader reader = null;
		try {
			reader = new PlainReader(contents);
			Map<String, String> header = reader.readHeader();
			if (header != null) {
				String version = header.get(IPlainConstants.ATTR_FORMAT_VERSION);
				if (version != null && version.equals(IPlainConstants.FORMAT_VERSION)) {
					while (true) {
						Entry entry = reader.readEntry();
						if (entry == null) {
							break;
						}
						IPath filePath = rootPath.append(new Path(entry.name));
						putFileItem(entry.name, filePath);
					}
				} else {
					throw new Exception("Q7 plain format version is unsupported.");
				}
			}
		} catch (Exception e) {
			RcpttPlugin.log(e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	protected synchronized void doStoreTo(File file) throws FileNotFoundException, IOException {
		PlainWriter writer = null;
		try {
			writer = new PlainWriter(new BufferedOutputStream(new FileOutputStream(file)), plainStoreFormat);
			Map<String, String> saveAttrs = new HashMap<String, String>();
			if (masterAttributes != null) {
				saveAttrs.putAll(masterAttributes);
			}
			if (ADD_SAVE_TIME.getResult()) {
				saveAttrs.put("Save-Time", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US)
					.format(new Date(System.currentTimeMillis())));
			}
			Bundle runtimeBundle = Platform.getBundle("org.eclipse.rcptt.updates.runtime.e4x");
			if (runtimeBundle != null) {
				saveAttrs.put("Runtime-Version", runtimeBundle.getVersion().toString());
			}

			writer.writeHeader(saveAttrs);
			List<String> ordered = new ArrayList<String>();
			Set<String> unordered = new HashSet<String>(files.keySet());
			if (unordered.contains(PersistenceManager.DESCRIPTION_ENTRY)) {
				ordered.add(PersistenceManager.DESCRIPTION_ENTRY);
				unordered.remove(PersistenceManager.DESCRIPTION_ENTRY);
			}
			if (unordered.contains(PersistenceManager.ECL_CONTENT_ENTRY)) {
				ordered.add(PersistenceManager.ECL_CONTENT_ENTRY);
				unordered.remove(PersistenceManager.ECL_CONTENT_ENTRY);
			}
			ordered.addAll(unordered);

			for (String fName : ordered) {
				File value = files.get(fName);
				if (value.exists()) {
					InputStream in = getInput(value);
					try {
						Map<String, String> attrs = new HashMap<String, String>();
						String storeAsText = null;
						IPlainTextPersistenceExtension[] extensions = PlainTextPersistenceExtensionManager.getInstance()
								.getExtensions();
						for (IPlainTextPersistenceExtension ext : extensions) {
							storeAsText = ext.getTextContentType(fName);
							if (storeAsText != null) {
								break;
							}
						}
						if (storeAsText != null) {
							attrs.put(IPlainConstants.ATTR_CONTENT_TYPE, storeAsText);
							byte[] content = FileUtil.getStreamContent(in);
							writer.writeNode(fName, attrs, new String(content, ENCODING));
						} else {
							if (fName.equals(PersistenceManager.ECL_CONTENT_ENTRY)) {
								attrs.put(IPlainConstants.ATTR_CONTENT_TYPE, "text/ecl");
								byte[] content = FileUtil.getStreamContent(in);
								writer.writeNode(fName, attrs, new String(content, ENCODING));
							} else if (fName.equals(PersistenceManager.DESCRIPTION_ENTRY)) {
								attrs.put(IPlainConstants.ATTR_CONTENT_TYPE, "text/plain");
								byte[] content = FileUtil.getStreamContent(in);
								writer.writeNode(fName, attrs, new String(content, ENCODING));
							} else if (fName.equals(TESTCASE_ITEMS)) {
								attrs.put(IPlainConstants.ATTR_CONTENT_TYPE, "text/testcase");
								byte[] content = FileUtil.getStreamContent(in);
								writer.writeNode(fName, attrs, new String(content, ENCODING));
							} else {
								byte[] content = FileUtil.getStreamContent(in);
								writer.writeNode(fName, attrs, content);
							}
						}
					} finally {
						FileUtil.safeClose(in);
					}
				}
			}
		} finally {
			writer.close();
		}
	}

	@Override
	public boolean isContentEntryRequired() {
		EList<EObject> contents = element.getContents();
		boolean optional = false;
		IPlainTextPersistenceExtension[] extensions = PlainTextPersistenceExtensionManager.getInstance()
				.getExtensions();
		for (IPlainTextPersistenceExtension ext : extensions) {
			if (ext.isContentEntryOptional(contents)) {
				optional = true;
				break;
			}
		}
		return contents.size() != 1 || !(contents.get(0) instanceof Scenario || contents.get(0) instanceof TestSuite
				|| contents.get(0) instanceof ProjectMetadata || optional);
	}

	@Override
	public boolean isAllowEmptyMetadataContent() {
		return true;
	}

	public void updateAttributes() {
		masterAttributes.clear();
		if (element == null) {
			return;
		}
		EList<EObject> contents = element.getContents();
		for (EObject eObject : contents) {
			if (eObject instanceof NamedElement) {
				updateGeneralAttributes(eObject);
			}
			if (eObject instanceof Scenario) {
				updateScenarioAttributes(eObject);
			}
			if (eObject instanceof Context) {
				updateContextAttributes(eObject);
			}
			if (eObject instanceof Verification) {
				updateVerificationAttributes(eObject);
			}
			if (eObject instanceof TestSuite) {
				updateTestSuiteAttributes(eObject);
			}
			if (eObject instanceof ProjectMetadata) {
				updateProjectMetadataAttributes(eObject);
			}
			IPlainTextPersistenceExtension[] extensions = PlainTextPersistenceExtensionManager.getInstance()
					.getExtensions();
			for (IPlainTextPersistenceExtension ext : extensions) {
				ext.updateAttributes(this, masterAttributes, eObject);
			}
		}
	}

	private void updateGeneralAttributes(EObject eObject) {
		NamedElement el = (NamedElement) eObject;
		masterAttributes.put(ATTR_ELEMENT_NAME, el.getName());
		if (el.getTags() != null && el.getTags().length() > 0) {
			masterAttributes.put(ATTR_TAGS, el.getTags());
		}
		masterAttributes.put(ATTR_ID, el.getId());
		// if (el.getDescription() != null
		// && el.getDescription().length() > 0) {
		// masterAttributes.put(ATTR_DESCRIPTION, el.getDescription());
		// }
		masterAttributes.put(ATTR_ELEMENT_VERSION, el.getVersion());
	}

	private void updateTestSuiteAttributes(EObject eObject) {
		TestSuite suite = (TestSuite) eObject;
		masterAttributes.put(ATTR_ELEMENT_TYPE, KIND_TESTSUITE);
		if (suite.isManuallyOrdered())
			masterAttributes.put(ATTR_MANUALY_ORDERED, String.valueOf(suite.isManuallyOrdered()));
		EList<TestSuiteItem> items = suite.getItems();
		delete(TESTCASE_ITEMS);
		if (items.size() > 0) {
			OutputStreamWriter stream = null;
			try {
				stream = new OutputStreamWriter(store(TESTCASE_ITEMS));
				for (TestSuiteItem testSuiteItem : items) {
					stream.append(testSuiteItem.getNamedElementId());
					stream.append("\t//");

					if (testSuiteItem.getKind() != null) {
						stream.append(" kind: '").append(testSuiteItem.getKind()).append("'");
					}
					if (testSuiteItem.getNamedElemetName() != null) {
						stream.append(" name: '").append(testSuiteItem.getNamedElemetName()).append("'");
					}
					if (testSuiteItem.getPath() != null) {
						stream.append(" path: '").append(testSuiteItem.getPath()).append("'");
					}
					stream.append("\n");
				}
			} catch (IOException e) {
				RcpttPlugin.log(e);
			} finally {
				FileUtil.safeClose(stream);
			}
		}
	}

	private void updateProjectMetadataAttributes(EObject eObject) {
		ProjectMetadata md = (ProjectMetadata) eObject;
		masterAttributes.put(ATTR_ELEMENT_TYPE, KIND_PROJECT_METADATA);
		if (md.getContexts().size() > 0) {
			masterAttributes.put(ATTR_CONTEXTS, namesToString(md.getContexts()));
		}
		if (md.getVerifications().size() > 0) {
			masterAttributes.put(ATTR_VERIFICATIONS, namesToString(md.getVerifications()));
		}
		if (md.getIgnores().size() > 0) {
			masterAttributes.put(ATTR_IGNORED_TESTCASES, namesToString(md.getIgnores()));
		}
	}

	private void updateContextAttributes(EObject eObject) {
		Context ctx = (Context) eObject;
		masterAttributes.put(ATTR_ELEMENT_TYPE, KIND_CONTEXT);
		masterAttributes.put(ATTR_CONTEXT_TYPE, ContextTypeManager.getInstance().getTypeByContext(ctx).getId());
	}

	private void updateVerificationAttributes(EObject eObject) {
		Verification verification = (Verification) eObject;
		masterAttributes.put(ATTR_ELEMENT_TYPE, KIND_VERIFICATION);
		masterAttributes.put(ATTR_VERIFICATION_TYPE,
				VerificationTypeManager.getInstance().getTypeByVerification(verification).getId());
	}

	private void updateScenarioAttributes(EObject eObject) {
		Scenario sc = (Scenario) eObject;
		masterAttributes.put(ATTR_ELEMENT_TYPE, KIND_TESTCASE);
		masterAttributes.put(ATTR_EXTERNAL_REFERENCE, sc.getExternalReference());

		EList<ScenarioProperty> properties = sc.getProperties();
		for (ScenarioProperty p : properties) {
			masterAttributes.put(ATTR_PROPERTY_PREFIX + p.getName(), p.getValue());
		}

		masterAttributes.put(ATTR_TESTCASE_TYPE, sc.getType());
		if (sc.getContexts().size() > 0) {
			masterAttributes.put(ATTR_CONTEXTS, namesToString(sc.getContexts()));
		}
		if (sc.getVerifications().size() > 0) {
			masterAttributes.put(ATTR_VERIFICATIONS, namesToString(sc.getVerifications()));
		}
	}

	private String namesToString(List<String> names) {
		StringBuilder bd = new StringBuilder();
		for (int i = 0; i < names.size(); i++) {
			bd.append(names.get(i));
			if (i != names.size() - 1) {
				bd.append(",");
			}
		}
		return bd.toString();
	}

	public void updateMetadata() {
		if (element != null) {
			IFile file = Q7Utils.getLocation(element);
			if (file != null && !file.exists()) {
				return;
			}
		}

		InputStream contents = getContentsStream();
		if (contents == null) {
			return;
		}
		try {
			PlainReader reader = null;
			try {
				reader = new PlainReader(contents);
				Map<String, String> header = reader.readHeader();
				if (header != null) {
					String version = header.get(IPlainConstants.ATTR_FORMAT_VERSION);
					if (version != null && version.equals(IPlainConstants.FORMAT_VERSION)) {
						EList<EObject> eList = element.getContents();
						if (eList.size() == 0) {
							// Create new empty object is type are specified
							String elementType = header.get(ATTR_ELEMENT_TYPE);
							if (KIND_TESTCASE.equals(elementType)) {
								Scenario scenario = ScenarioFactory.eINSTANCE.createScenario();
								eList.add(scenario);
							} else if (KIND_CONTEXT.equals(elementType)) {
								String contextType = header.get(ATTR_CONTEXT_TYPE);
								ContextType type = ContextTypeManager.getInstance().getTypeById(contextType);
								if (type != null) {
									Context context = type.create(element, "");
									eList.add(context);
								}
							} else if (KIND_VERIFICATION.equals(elementType)) {
								String verificationType = header.get(ATTR_VERIFICATION_TYPE);
								VerificationType type = VerificationTypeManager.getInstance()
										.getTypeById(verificationType);
								if (type != null) {
									Verification verification = type.create(element, "");
									eList.add(verification);
								}
							} else if (KIND_TESTSUITE.equals(elementType)) {
								TestSuite suite = ScenarioFactory.eINSTANCE.createTestSuite();
								eList.add(suite);
							} else if (KIND_PROJECT_METADATA.equals(elementType)) {
								ProjectMetadata suite = ScenarioFactory.eINSTANCE.createProjectMetadata();
								eList.add(suite);
							}
						}
						for (EObject eObject : eList) {
							if (eObject instanceof NamedElement) {
								processGeneralNamedElementAttrs(header, eObject);
							}
							if (eObject instanceof ProjectMetadata) {
								processProjectMetadataAttrs(header, eObject);
							}
							if (eObject instanceof Scenario) {
								processScenarioAttrs(header, eObject);
							}
							if (eObject instanceof TestSuite) {
								processTestSuiteAttrs(header, eObject);
							}
							IPlainTextPersistenceExtension[] extensions = PlainTextPersistenceExtensionManager
									.getInstance().getExtensions();
							for (IPlainTextPersistenceExtension ext : extensions) {
								ext.updateMetadata(this, header, eObject);
							}
						}

					} else {
						throw new Exception("Q7 plain format version is unsupported.");
					}
				}
			} catch (Exception e) {
				RcpttPlugin.log(e);
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} finally {
			try {
				contents.close();
			} catch (IOException e) {
			}
		}
		element.setModified(false);
	}

	private void processTestSuiteAttrs(Map<String, String> header, EObject eObject) throws IOException {
		TestSuite sc = (TestSuite) eObject;
		InputStream items = read(TESTCASE_ITEMS);

		sc.setManuallyOrdered(
				header.containsKey(ATTR_MANUALY_ORDERED) && header.get(ATTR_MANUALY_ORDERED).equalsIgnoreCase("true"));

		if (items != null) {
			try {
				BufferedReader stream = new BufferedReader(new InputStreamReader(items));
				while (true) {
					String line = stream.readLine();
					if (line == null) {
						break;
					}
					if (line.trim().length() > 0) {
						int pos = line.indexOf("\t//");
						if (pos != -1) {
							String id = line.substring(0, pos);
							TestSuiteItem item = ScenarioFactory.eINSTANCE.createTestSuiteItem();
							item.setNamedElementId(id);
							sc.getItems().add(item);
						}
					}
				}
			} finally {
				items.close();
			}
		}
	}

	private void processScenarioAttrs(Map<String, String> header, EObject eObject) {
		Scenario sc = (Scenario) eObject;

		if (header.containsKey(ATTR_EXTERNAL_REFERENCE)) {
			sc.setExternalReference(header.get(ATTR_EXTERNAL_REFERENCE));
		}
		if (header.containsKey(ATTR_TESTCASE_TYPE)) {
			sc.setType(header.get(ATTR_TESTCASE_TYPE));
		}
		for (Map.Entry<String, String> e : header.entrySet()) {
			if (e.getKey().startsWith(ATTR_PROPERTY_PREFIX)) {
				EList<ScenarioProperty> properties = sc.getProperties();
				ScenarioProperty prop = ScenarioFactory.eINSTANCE.createScenarioProperty();
				prop.setName(e.getKey().substring(ATTR_PROPERTY_PREFIX.length()));
				prop.setValue(e.getValue());
				properties.add(prop);
			}
		}

		if (header.containsKey(ATTR_CONTEXTS)) {
			String contexts = header.get(ATTR_CONTEXTS);
			sc.getContexts().clear();
			sc.getContexts().addAll(Arrays.asList(contexts.split(",")));
		}

		if (header.containsKey(ATTR_VERIFICATIONS)) {
			String verifications = header.get(ATTR_VERIFICATIONS);
			sc.getVerifications().clear();
			sc.getVerifications().addAll(Arrays.asList(verifications.split(",")));
		}
	}

	private void processProjectMetadataAttrs(Map<String, String> header, EObject eObject) throws IOException {
		ProjectMetadata md = (ProjectMetadata) eObject;
		if (header.containsKey(ATTR_CONTEXTS)) {
			String contexts = header.get(ATTR_CONTEXTS);
			md.getContexts().clear();
			md.getContexts().addAll(Arrays.asList(contexts.split(",")));
		}
		if (header.containsKey(ATTR_VERIFICATIONS)) {
			String verifications = header.get(ATTR_VERIFICATIONS);
			md.getVerifications().clear();
			md.getVerifications().addAll(Arrays.asList(verifications.split(",")));
		}
		if (header.containsKey(ATTR_IGNORED_TESTCASES)) {
			String ignores = header.get(ATTR_IGNORED_TESTCASES);
			md.getIgnores().clear();
			md.getIgnores().addAll(Arrays.asList(ignores.split(",")));
		}
	}

	private void processGeneralNamedElementAttrs(Map<String, String> header, EObject eObject) {
		NamedElement e = (NamedElement) eObject;
		if (header.containsKey(ATTR_ID)) {
			e.setId(header.get(ATTR_ID));
		}
		if (header.containsKey(ATTR_ELEMENT_NAME)) {
			e.setName(header.get(ATTR_ELEMENT_NAME));
		}
		if (header.containsKey(ATTR_TAGS)) {
			e.setTags(header.get(ATTR_TAGS));
		}
		if (header.containsKey(ATTR_ELEMENT_VERSION)) {
			e.setVersion(header.get(ATTR_ELEMENT_VERSION));
		}
	}

	public void setSaveFormat(String plainStoreFormat) {
		this.plainStoreFormat = plainStoreFormat;
	}
}
