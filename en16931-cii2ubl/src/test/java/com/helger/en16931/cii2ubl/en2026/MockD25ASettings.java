/*
 * Copyright (C) 2019-2026 Philip Helger
 * http://www.helger.com
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.en16931.cii2ubl.en2026;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helger.base.state.ESuccess;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.io.file.FilenameHelper;
import com.helger.ubl25.CUBL25;
import com.helger.xml.namespace.MapBasedNamespaceContext;
import com.helger.xml.xpath.XPathHelper;

import oasis.names.specification.ubl.schema.xsd.creditnote_25.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_25.InvoiceType;
import com.helger.ubl25.UBL25Marshaller;

/**
 * Test infrastructure for the EN 16931:2026 conversion.<br>
 * The conversion is verified in three steps:
 * <ol>
 * <li>the CII D25A source file is read through the D25A marshaller, which validates it against the
 * D25A XSD</li>
 * <li>the created UBL 2.5 document is written through the UBL 2.5 marshaller, which validates it
 * against the UBL 2.5 XSD - a schema invalid document makes the write fail</li>
 * <li>the individual business terms are asserted with XPath expressions taken from
 * <code>docs/en16931-2026-syntax.md</code></li>
 * </ol>
 * All XPath expressions are relative to the document element, exactly like the paths in the mapping
 * table, which are relative to <code>/Invoice</code> respectively <code>/CreditNote</code>.
 *
 * @author Philip Helger
 */
public final class MockD25ASettings
{
  public static final String BASE_TEST_DIR = "src/test/resources/external/cii-d25a/";
  public static final String BASE_DEST_DIR = "generated/toubl25/";

  private static final Logger LOGGER = LoggerFactory.getLogger (MockD25ASettings.class);

  private static final MapBasedNamespaceContext NS_CTX = new MapBasedNamespaceContext ().addMapping ("cac",
                                                                                                     CUBL25.XML_SCHEMA_CAC_NAMESPACE_URL)
                                                                                        .addMapping ("cbc",
                                                                                                     CUBL25.XML_SCHEMA_CBC_NAMESPACE_URL)
                                                                                        .addMapping ("cec",
                                                                                                     CUBL25.XML_SCHEMA_CEC_NAMESPACE_URL);

  private MockD25ASettings ()
  {}

  @NonNull
  private static XPathExpression _createXPath (@NonNull final String sXPath)
  {
    final XPathExpression ret = XPathHelper.createNewXPathExpression (XPathHelper.createNewXPath (NS_CTX), sXPath);
    assertNotNull ("Failed to compile the XPath expression '" + sXPath + "'", ret);
    return ret;
  }

  /**
   * Convert a CII D25A file to UBL 2.5 and return the result as a DOM document. The source is
   * validated against the D25A XSD on read and the result against the UBL 2.5 XSD on write.
   *
   * @param sFilename
   *        Filename relative to {@link #BASE_TEST_DIR}. May not be <code>null</code>.
   * @param bExpectInvoice
   *        <code>true</code> if an Invoice is expected, <code>false</code> for a CreditNote.
   * @return The document element of the created UBL 2.5 document. Never <code>null</code>.
   */
  @NonNull
  public static Element convertAndValidate (@NonNull final String sFilename, final boolean bExpectInvoice)
  {
    final File aSrcFile = new File (BASE_TEST_DIR, sFilename);
    assertTrue ("Not existing: " + aSrcFile.getAbsolutePath (), aSrcFile.exists ());

    LOGGER.info ("Converting " + aSrcFile.toString () + " to UBL 2.5");

    // Step 1 + 2: read (validates against the D25A XSD) and convert
    final ErrorList aErrorList = new ErrorList ();
    final Serializable aUBL = new CIID25AToUBL25Converter ().convertCIItoUBL (aSrcFile, aErrorList);
    assertTrue ("Errors: " + aErrorList.toString (), aErrorList.containsNoError ());
    assertNotNull ("Conversion of '" + sFilename + "' returned null", aUBL);

    final File aDestFile = new File (BASE_DEST_DIR, FilenameHelper.getBaseName (sFilename) + "-ubl.xml");
    final ESuccess eSuccess;
    final Document aDoc;
    if (bExpectInvoice)
    {
      assertTrue ("Expected an Invoice but got " + aUBL.getClass ().getName (), aUBL instanceof InvoiceType);
      final InvoiceType aUBLInvoice = (InvoiceType) aUBL;
      // Step 3: write (validates against the UBL 2.5 XSD)
      eSuccess = UBL25Marshaller.invoice ().setFormattedOutput (true).write (aUBLInvoice, aDestFile);
      aDoc = UBL25Marshaller.invoice ().getAsDocument (aUBLInvoice);
    }
    else
    {
      assertTrue ("Expected a CreditNote but got " + aUBL.getClass ().getName (), aUBL instanceof CreditNoteType);
      final CreditNoteType aUBLCreditNote = (CreditNoteType) aUBL;
      eSuccess = UBL25Marshaller.creditNote ().setFormattedOutput (true).write (aUBLCreditNote, aDestFile);
      aDoc = UBL25Marshaller.creditNote ().getAsDocument (aUBLCreditNote);
    }
    assertTrue ("The created UBL 2.5 document of '" + sFilename + "' is not XSD valid", eSuccess.isSuccess ());
    assertNotNull ("Failed to serialize the UBL 2.5 document of '" + sFilename + "'", aDoc);

    return aDoc.getDocumentElement ();
  }

  @Nullable
  private static Node _selectSingle (@NonNull final Node aCtx, @NonNull final String sXPath)
  {
    try
    {
      return (Node) _createXPath (sXPath).evaluate (aCtx, XPathConstants.NODE);
    }
    catch (final XPathExpressionException ex)
    {
      fail ("Failed to evaluate the XPath expression '" + sXPath + "': " + ex.getMessage ());
      return null;
    }
  }

  @NonNull
  private static NodeList _selectAll (@NonNull final Node aCtx, @NonNull final String sXPath)
  {
    try
    {
      return (NodeList) _createXPath (sXPath).evaluate (aCtx, XPathConstants.NODESET);
    }
    catch (final XPathExpressionException ex)
    {
      fail ("Failed to evaluate the XPath expression '" + sXPath + "': " + ex.getMessage ());
      throw new IllegalStateException (ex);
    }
  }

  /**
   * Assert that the provided XPath selects exactly one node with the provided text content.
   *
   * @param aCtx
   *        The context node, usually the document element. May not be <code>null</code>.
   * @param sXPath
   *        The XPath relative to the context node. May not be <code>null</code>.
   * @param sExpected
   *        The expected text content. May not be <code>null</code>.
   */
  public static void assertXPath (@NonNull final Node aCtx,
                                  @NonNull final String sXPath,
                                  @NonNull final String sExpected)
  {
    final Node aNode = _selectSingle (aCtx, sXPath);
    assertNotNull ("No node found for '" + sXPath + "', expected the value '" + sExpected + "'", aNode);
    assertEquals ("Wrong value at '" + sXPath + "'", sExpected, aNode.getTextContent ());
  }

  /**
   * Assert that the provided XPath selects no node at all.
   *
   * @param aCtx
   *        The context node, usually the document element. May not be <code>null</code>.
   * @param sXPath
   *        The XPath relative to the context node. May not be <code>null</code>.
   */
  public static void assertNoXPath (@NonNull final Node aCtx, @NonNull final String sXPath)
  {
    final Node aNode = _selectSingle (aCtx, sXPath);
    if (aNode != null)
      fail ("Expected no node at '" + sXPath + "' but found one with the value '" + aNode.getTextContent () + "'");
  }

  /**
   * Assert the number of nodes selected by the provided XPath.
   *
   * @param aCtx
   *        The context node, usually the document element. May not be <code>null</code>.
   * @param sXPath
   *        The XPath relative to the context node. May not be <code>null</code>.
   * @param nExpected
   *        The expected number of nodes.
   */
  public static void assertXPathCount (@NonNull final Node aCtx, @NonNull final String sXPath, final int nExpected)
  {
    assertEquals ("Wrong number of nodes at '" + sXPath + "'", nExpected, _selectAll (aCtx, sXPath).getLength ());
  }
}
