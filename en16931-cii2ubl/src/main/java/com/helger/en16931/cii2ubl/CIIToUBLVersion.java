/*
 * Copyright (C) 2019-2025 Philip Helger
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.rt.NonBlockingProperties;
import com.helger.base.rt.PropertiesHelper;
import com.helger.io.resource.ClassPathResource;

/**
 * Contains application wide constants.
 *
 * @author Philip Helger
 * @since 1.3.0
 */
@Immutable
public final class CIIToUBLVersion
{
  /** Current version - from properties file */
  public static final String BUILD_VERSION;
  /** Build timestamp - from properties file */
  public static final String BUILD_TIMESTAMP;

  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLVersion.class);

  static
  {
    String sProjectVersion = null;
    String sProjectTimestamp = null;
    final NonBlockingProperties p = PropertiesHelper.loadProperties (ClassPathResource.getInputStream ("cii2ubl-version.properties",
                                                                                                       CIIToUBLVersion.class.getClassLoader ()));
    if (p != null)
    {
      sProjectVersion = p.get ("version");
      sProjectTimestamp = p.get ("timestamp");
    }
    if (sProjectVersion == null)
    {
      sProjectVersion = "undefined";
      LOGGER.warn ("Failed to load version number");
    }
    BUILD_VERSION = sProjectVersion;
    if (sProjectTimestamp == null)
    {
      sProjectTimestamp = "undefined";
      LOGGER.warn ("Failed to load timestamp");
    }
    BUILD_TIMESTAMP = sProjectTimestamp;
  }

  private CIIToUBLVersion ()
  {}
}
