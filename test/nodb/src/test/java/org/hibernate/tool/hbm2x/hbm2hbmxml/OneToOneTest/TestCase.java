/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 * 
 * Copyright 2004-2021 Red Hat, Inc.
 *
 * Licensed under the GNU Lesser General Public License (LGPL), 
 * version 2.1 or later (the "License").
 * You may not use this file except in compliance with the License.
 * You may read the licence in the 'lgpl.txt' file in the root folder of 
 * project or obtain a copy at
 *
 *     http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.tool.hbm2x.hbm2hbmxml.OneToOneTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.api.export.ExporterConstants;
import org.hibernate.tool.api.metadata.MetadataDescriptor;
import org.hibernate.tool.api.metadata.MetadataDescriptorFactory;
import org.hibernate.tool.internal.export.hbm.HbmExporter;
import org.hibernate.tools.test.util.HibernateUtil;
import org.hibernate.tools.test.util.JUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestCase {

	private static final String[] HBM_XML_FILES = new String[] {
			"PersonAddressOneToOnePrimaryKey.hbm.xml"
	};
	
	@TempDir
	public File outputFolder = new File("output");
	
	private File srcDir = null;
	private File resourcesDir = null;
	private HbmExporter hbmexporter = null;
	
	@BeforeEach
	public void setUp() throws Exception {
		srcDir = new File(outputFolder, "src");
		srcDir.mkdir();
		resourcesDir = new File(outputFolder, "resources");
		resourcesDir.mkdir();
		MetadataDescriptor metadataDescriptor = HibernateUtil
				.initializeMetadataDescriptor(this, HBM_XML_FILES, resourcesDir);
		hbmexporter = new HbmExporter();
		hbmexporter.getProperties().put(ExporterConstants.METADATA_DESCRIPTOR, metadataDescriptor);
		hbmexporter.getProperties().put(ExporterConstants.DESTINATION_FOLDER, srcDir);
		hbmexporter.start();		
	}
	
	@Test
	public void testAllFilesExistence() {
		assertFalse(new File(
				srcDir, "GeneralHbmSettings.hbm.xml")
			.exists());
		JUnitUtil.assertIsNonEmptyFile(
				new File(
						srcDir,
						"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Person.hbm.xml"));
		JUnitUtil.assertIsNonEmptyFile(
				new File(
						srcDir, 
						"/org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Address.hbm.xml"));		
	}
	
	@Test
	public void testArtifactCollection() {
		assertEquals(
				2,
				hbmexporter.getArtifactCollector().getFileCount("hbm.xml"));
	}
	
	@Test
	public void testReadable() {
        File personHbmXml = new File(
        		srcDir, 
        		"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Person.hbm.xml");
        File addressHbmXml = new File(
        		srcDir, 
        		"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Address.hbm.xml");
		Properties properties = new Properties();
		properties.setProperty(AvailableSettings.DIALECT, HibernateUtil.Dialect.class.getName());
		File[] files = new File[] { personHbmXml, addressHbmXml };
		MetadataDescriptor metadataDescriptor = MetadataDescriptorFactory
				.createNativeDescriptor(null, files, properties);
        assertNotNull(metadataDescriptor.createMetadata());
    }
	
	@Test
	public void testOneToOne() throws Exception {
		File xmlFile = new File(
        		srcDir, 
        		"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Person.hbm.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(xmlFile);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList)xpath
				.compile("//hibernate-mapping/class/one-to-one")
				.evaluate(document, XPathConstants.NODESET);
		assertEquals(1, nodeList.getLength(), "Expected to get one-to-one element");
		Element node = (Element) nodeList.item(0);
		assertEquals(node.getAttribute( "name" ),"address");
		assertEquals(node.getAttribute( "constrained" ),"false");
		xmlFile = new File(
        		srcDir, 
        		"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Address.hbm.xml");
		document = db.parse(xmlFile);
		nodeList = (NodeList)xpath
				.compile("//hibernate-mapping/class/one-to-one")
				.evaluate(document, XPathConstants.NODESET);
		assertEquals(1, nodeList.getLength(), "Expected to get one set element");
		node = (Element) nodeList.item(0);
		assertEquals(node.getAttribute( "name" ),"person");
		assertEquals(node.getAttribute( "constrained" ),"true");
		assertEquals(node.getAttribute( "access" ), "field");
	}

}
