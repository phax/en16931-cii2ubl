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
package com.helger.en16931.cii2ubl;

import java.io.File;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.helger.annotation.Nonempty;
import com.helger.base.string.StringHelper;
import com.helger.xml.XMLHelper;
import com.helger.xml.serialize.read.DOMReader;

/**
 * The edition of EN 16931 a CII document conforms to. Each edition prescribes exactly one CII
 * release and one UBL version:
 * <ul>
 * <li>{@link #EN2017} - CII D16B to UBL 2.1</li>
 * <li>{@link #EN2026} - CII D25A to UBL 2.5</li>
 * </ul>
 * The edition cannot be told from the XML namespaces, because CII D16B, D22B and D25A all use the
 * identical namespace URIs, and it cannot be told from the XML Schema either, because a D16B
 * instance also validates against the D25A XSD. The only reliable discriminator is BT-24
 * (Specification identifier), which is mandatory within BG-2.
 *
 * @author Philip Helger
 * @since 4.0.0
 */
public enum EEN16931Edition
{
  /** EN 16931:2017 - CII D16B to UBL 2.1 */
  EN2017 ("2017", "urn:cen.eu:en16931:2017"),
  /** EN 16931:2026 - CII D25A to UBL 2.5 */
  EN2026 ("2026", "urn:cen.eu:en16931:2026");

  /** The CII rsm namespace URI. It is identical for all CII releases. */
  public static final String CII_NAMESPACE_URI = "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100";
  /** The CII ram namespace URI. It is identical for all CII releases. */
  public static final String RAM_NAMESPACE_URI = "urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100";

  private final String m_sID;
  private final String m_sSpecificationIdentifier;

  EEN16931Edition (@NonNull @Nonempty final String sID, @NonNull @Nonempty final String sSpecificationIdentifier)
  {
    m_sID = sID;
    m_sSpecificationIdentifier = sSpecificationIdentifier;
  }

  /**
   * @return The short identifier of this edition, e.g. "2017". Never <code>null</code>.
   */
  @NonNull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  /**
   * @return The BT-24 specification identifier of this edition, e.g.
   *         <code>urn:cen.eu:en16931:2017</code>. Never <code>null</code>.
   */
  @NonNull
  @Nonempty
  public String getSpecificationIdentifier ()
  {
    return m_sSpecificationIdentifier;
  }

  /**
   * Find the edition with the provided short identifier.
   *
   * @param sID
   *        The identifier to search, e.g. "2026". May be <code>null</code>.
   * @return <code>null</code> if no such edition exists.
   */
  @Nullable
  public static EEN16931Edition getFromIDOrNull (@Nullable final String sID)
  {
    if (StringHelper.isNotEmpty (sID))
      for (final EEN16931Edition e : values ())
        if (e.m_sID.equals (sID))
          return e;
    return null;
  }

  /**
   * Find the edition matching the provided BT-24 value. The value is matched by prefix, because
   * customizations append their own identifier, as in
   * <code>urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0</code>.
   *
   * @param sSpecificationIdentifier
   *        The BT-24 value. May be <code>null</code>.
   * @return <code>null</code> if the value matches no known edition.
   */
  @Nullable
  public static EEN16931Edition getFromSpecificationIdentifierOrNull (@Nullable final String sSpecificationIdentifier)
  {
    final String sTrimmed = StringHelper.trim (sSpecificationIdentifier);
    if (StringHelper.isNotEmpty (sTrimmed))
      for (final EEN16931Edition e : values ())
        if (sTrimmed.startsWith (e.m_sSpecificationIdentifier))
          return e;
    return null;
  }

  /**
   * Extract the BT-24 value from a CII document. This deliberately walks the DOM instead of
   * unmarshalling, because the correct JAXB model is exactly what is not known yet.
   *
   * @param aNode
   *        The CII document or its <code>rsm:CrossIndustryInvoice</code> element. May be
   *        <code>null</code>.
   * @return <code>null</code> if BT-24 is not present.
   */
  @Nullable
  public static String getSpecificationIdentifier (@Nullable final Node aNode)
  {
    if (aNode == null)
      return null;

    final Element aRoot = aNode instanceof final Document aDoc ? aDoc.getDocumentElement ()
                                                               : aNode instanceof final Element aElement ? aElement
                                                                                                         : null;
    if (aRoot == null)
      return null;

    final Element aContext = XMLHelper.getFirstChildElementOfName (aRoot,
                                                                   CII_NAMESPACE_URI,
                                                                   "ExchangedDocumentContext");
    if (aContext == null)
      return null;

    final Element aGuideline = XMLHelper.getFirstChildElementOfName (aContext,
                                                                    RAM_NAMESPACE_URI,
                                                                    "GuidelineSpecifiedDocumentContextParameter");
    if (aGuideline == null)
      return null;

    final Element aID = XMLHelper.getFirstChildElementOfName (aGuideline, RAM_NAMESPACE_URI, "ID");
    return aID == null ? null : StringHelper.trim (aID.getTextContent ());
  }

  /**
   * Determine the EN 16931 edition of a CII document from its BT-24 value.
   *
   * @param aNode
   *        The CII document or its <code>rsm:CrossIndustryInvoice</code> element. May be
   *        <code>null</code>.
   * @return <code>null</code> if BT-24 is absent or matches no known edition.
   */
  @Nullable
  public static EEN16931Edition detect (@Nullable final Node aNode)
  {
    return getFromSpecificationIdentifierOrNull (getSpecificationIdentifier (aNode));
  }

  /**
   * Determine the EN 16931 edition of a CII file from its BT-24 value.
   *
   * @param aFile
   *        The CII file to inspect. May be <code>null</code>.
   * @return <code>null</code> if the file is not well formed XML, or BT-24 is absent or matches no
   *         known edition.
   */
  @Nullable
  public static EEN16931Edition detect (@Nullable final File aFile)
  {
    return getFromSpecificationIdentifierOrNull (getSpecificationIdentifier (aFile));
  }

  /**
   * Extract the BT-24 value from a CII file.
   *
   * @param aFile
   *        The CII file to inspect. May be <code>null</code>.
   * @return <code>null</code> if the file cannot be read, is not well formed XML, or has no BT-24.
   */
  @Nullable
  public static String getSpecificationIdentifier (@Nullable final File aFile)
  {
    if (aFile == null || !aFile.isFile ())
      return null;

    // Only a DOM peek - the correct JAXB model is exactly what is not known yet
    final Document aDoc = DOMReader.readXMLDOM (aFile);
    return aDoc == null ? null : getSpecificationIdentifier (aDoc);
  }
}
