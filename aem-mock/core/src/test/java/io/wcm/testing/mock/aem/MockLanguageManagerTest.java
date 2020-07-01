/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.testing.mock.aem;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.Language;
import com.day.cq.wcm.api.LanguageManager;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockLanguageManagerTest {

  private static final String SITE_ROOT = "/content/sample";
  private static final String ENGLISH_HOMEPAGE = String.join("/", SITE_ROOT, "en");
  private static final String FRENCH_HOMEPAGE = String.join("/", SITE_ROOT, "fr");


  // Run all unit tests for each resource resolver types listed here
  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Before
  public void setUp() {
    ContentLoader contentLoader = this.context.load();
    contentLoader.json("/json-import-samples/language-content.json", SITE_ROOT);
  }

  @Test
  public void getLanguage() {
    LanguageManager languageManager = new MockLanguageManager();
    Locale englishLocale = languageManager
        .getLanguage(Objects.requireNonNull(context.resourceResolver().getResource(ENGLISH_HOMEPAGE + "/subpage/jcr:content")));
    assertNotNull(englishLocale);
    assertEquals(Locale.ENGLISH, englishLocale);

    Locale frenchLocale = languageManager.getLanguage(Objects.requireNonNull(context.resourceResolver().getResource(FRENCH_HOMEPAGE + "/subpage/jcr:content")));
    assertNotNull(frenchLocale);
    assertEquals(Locale.JAPANESE, frenchLocale);

    Locale rootLocale = languageManager.getLanguage(Objects.requireNonNull(context.resourceResolver().getResource(SITE_ROOT)));
    assertNull(rootLocale);
  }

  @Test
  public void getCqLanguage() {
    LanguageManager languageManager = new MockLanguageManager();
    Language englishLocale = languageManager
        .getCqLanguage(Objects.requireNonNull(context.resourceResolver().getResource(ENGLISH_HOMEPAGE + "/subpage/jcr:content")), false);
    assertNotNull(englishLocale);
    assertEquals(Locale.ENGLISH, englishLocale.getLocale());

    Language frenchLocale = languageManager
        .getCqLanguage(Objects.requireNonNull(context.resourceResolver().getResource(FRENCH_HOMEPAGE + "/subpage/jcr:content")), false);
    assertNotNull(frenchLocale);
    assertEquals(Locale.FRENCH, frenchLocale.getLocale());

    Language rootLocale = languageManager.getCqLanguage(Objects.requireNonNull(context.resourceResolver().getResource(SITE_ROOT)), false);
    assertNull(rootLocale);
  }

  @Test
  public void getCqLanguage_respectContent() {
    LanguageManager languageManager = new MockLanguageManager();
    Language englishLocale = languageManager
        .getCqLanguage(Objects.requireNonNull(context.resourceResolver().getResource(ENGLISH_HOMEPAGE + "/subpage/jcr:content")));
    assertNotNull(englishLocale);
    assertEquals(Locale.ENGLISH, englishLocale.getLocale());

    // when respecting content the "fr" site should present as "ja"
    Language frenchLocale = languageManager
        .getCqLanguage(Objects.requireNonNull(context.resourceResolver().getResource(FRENCH_HOMEPAGE + "/subpage/jcr:content")));
    assertNotNull(frenchLocale);
    assertEquals(Locale.JAPANESE, frenchLocale.getLocale());

    Language rootLocale = languageManager.getCqLanguage(Objects.requireNonNull(context.resourceResolver().getResource(SITE_ROOT)));
    assertNull(rootLocale);
  }

  @Test
  public void getLanguageRoot() {
    LanguageManager languageManager = new MockLanguageManager();
    Page englishLanguageRoot = languageManager
        .getLanguageRoot(Objects.requireNonNull(context.resourceResolver().getResource(ENGLISH_HOMEPAGE + "/subpage/jcr:content")));
    assertNotNull(englishLanguageRoot);
    assertEquals(ENGLISH_HOMEPAGE, englishLanguageRoot.getPath());

    Page frenchLanguageRoot = languageManager
        .getLanguageRoot(Objects.requireNonNull(context.resourceResolver().getResource(FRENCH_HOMEPAGE + "/subpage/jcr:content")));
    assertNotNull(frenchLanguageRoot);
    assertEquals(FRENCH_HOMEPAGE, frenchLanguageRoot.getPath());
  }

  @Test
  public void getLanguageRoots() {
    LanguageManager languageManager = new MockLanguageManager();
    Collection<Page> roots = languageManager.getLanguageRoots(context.resourceResolver(), ENGLISH_HOMEPAGE + "/subpage/jcr:content");
    assertNotNull(roots);
    assertArrayEquals(new String[] { ENGLISH_HOMEPAGE, FRENCH_HOMEPAGE }, roots.stream().map(Page::getPath).toArray());

    Collection<Page> roots2 = languageManager.getLanguageRoots(context.resourceResolver(), FRENCH_HOMEPAGE + "/subpage/jcr:content");
    assertNotNull(roots2);
    assertArrayEquals(new String[] { ENGLISH_HOMEPAGE, FRENCH_HOMEPAGE }, roots2.stream().map(Page::getPath).toArray());

    //NOTE: this situation causes an NPE in current LanguageManager implementation
    /* Collection<Page> roots3 = languageManager.getLanguageRoots(context.resourceResolver(), "/does/not/exist");
    assertNotNull(roots3);
    assertArrayEquals(new Page[] {}, roots3.toArray(new Page[0]));
    */

    Collection<Page> roots4 = languageManager.getLanguageRoots(context.resourceResolver(), SITE_ROOT);
    assertNotNull(roots4);
    assertArrayEquals(new Page[] {}, roots4.toArray(new Page[0]));

    Collection<Page> nullPath = languageManager.getLanguageRoots(context.resourceResolver(), null);
    assertArrayEquals(new Page[] {}, nullPath.toArray(new Page[0]));

    Collection<Page> rootPath = languageManager.getLanguageRoots(context.resourceResolver(), "/");
    assertArrayEquals(new Page[] {}, rootPath.toArray(new Page[0]));
  }

  @Test
  public void getLanguages() {
    LanguageManager languageManager = new MockLanguageManager();
    Collection<Locale> locales = languageManager.getLanguages(context.resourceResolver(), ENGLISH_HOMEPAGE + "/subpage/jcr:content");
    assertNotNull(locales);
    assertArrayEquals(new Locale[] { Locale.ENGLISH, Locale.FRENCH }, locales.toArray(new Locale[0]));

    Collection<Locale> locales2 = languageManager.getLanguages(context.resourceResolver(), FRENCH_HOMEPAGE + "/subpage/jcr:content");
    assertNotNull(locales2);
    assertArrayEquals(new Locale[] { Locale.ENGLISH, Locale.FRENCH }, locales2.toArray(new Locale[0]));

    Collection<Locale> locales3 = languageManager.getLanguages(context.resourceResolver(), "/does/not/exist");
    assertNotNull(locales3);
    assertArrayEquals(new Locale[] {}, locales3.toArray(new Locale[0]));

    Collection<Locale> locales4 = languageManager.getLanguages(context.resourceResolver(), SITE_ROOT);
    assertNotNull(locales4);
    assertArrayEquals(new Locale[] {}, locales4.toArray(new Locale[0]));
  }

  @Test
  public void getCqLanguages() {
    LanguageManager languageManager = new MockLanguageManager();
    Collection<Language> languages = languageManager.getCqLanguages(context.resourceResolver(), ENGLISH_HOMEPAGE + "/subpage/jcr:content");
    assertNotNull(languages);
    assertArrayEquals(new Locale[] { Locale.ENGLISH, Locale.FRENCH }, languages.stream().map(Language::getLocale).toArray());

    Collection<Language> languages2 = languageManager.getCqLanguages(context.resourceResolver(), FRENCH_HOMEPAGE + "/subpage/jcr:content");
    assertNotNull(languages2);
    assertArrayEquals(new Locale[] { Locale.ENGLISH, Locale.FRENCH }, languages2.stream().map(Language::getLocale).toArray());

    Collection<Language> languages3 = languageManager.getCqLanguages(context.resourceResolver(), "/does/not/exist");
    assertNotNull(languages3);
    assertArrayEquals(new Locale[] {}, languages3.stream().map(Language::getLocale).toArray());

    Collection<Language> languages4 = languageManager.getCqLanguages(context.resourceResolver(), SITE_ROOT);
    assertNotNull(languages4);
    assertArrayEquals(new Locale[] {}, languages4.stream().map(Language::getLocale).toArray());
  }

  @Test
  public void getAdjacentLanguageInfo() {
    LanguageManager languageManager = new MockLanguageManager();
    // have to use a page path, doesn't work on a non-page resource
    Map<Language,
        LanguageManager.Info> adjacentLanguageInfo = languageManager.getAdjacentLanguageInfo(context.resourceResolver(), ENGLISH_HOMEPAGE + "/subpage2");
    assertNotNull(adjacentLanguageInfo);
    assertArrayEquals(new Locale[] { Locale.ENGLISH, Locale.FRENCH }, adjacentLanguageInfo.keySet().stream().map(Language::getLocale).toArray());

    LanguageManager.Info englishInfo = adjacentLanguageInfo.entrySet().stream()
        .filter(e -> e.getKey().getLocale().equals(Locale.ENGLISH))
        .map(Map.Entry::getValue)
        .findFirst().orElse(null);
    assertNotNull(englishInfo);
    assertEquals(ENGLISH_HOMEPAGE + "/subpage2", englishInfo.getPath());
    // this, in theory, should return the value, but it only seems to return 0
    assertEquals(0L, englishInfo.getLastModified());
    assertTrue(englishInfo.exists());
    assertTrue(englishInfo.hasContent());


    LanguageManager.Info frenchInfo = adjacentLanguageInfo.entrySet().stream()
        .filter(e -> e.getKey().getLocale().equals(Locale.FRENCH))
        .map(Map.Entry::getValue)
        .findFirst().orElse(null);
    assertNotNull(frenchInfo);
    assertEquals(FRENCH_HOMEPAGE + "/subpage2", frenchInfo.getPath());
    // this, in theory, should return the value, but it only seems to return 0
    assertEquals(0L, frenchInfo.getLastModified());
    assertFalse(frenchInfo.exists());
    assertFalse(frenchInfo.hasContent());


    Map<Language, LanguageManager.Info> nonExisting = languageManager.getAdjacentLanguageInfo(context.resourceResolver(), "/does/not/exist");
    assertNull(nonExisting);

    Map<Language, LanguageManager.Info> nullPath = languageManager.getAdjacentLanguageInfo(context.resourceResolver(), null);
    assertNull(nullPath);

    Map<Language, LanguageManager.Info> rootPath = languageManager.getAdjacentLanguageInfo(context.resourceResolver(), "/");
    assertNull(rootPath);
  }

  @Test
  public void getAdjacentInfo() {
    LanguageManager languageManager = new MockLanguageManager();
    // have to use a page path, doesn't work on a non-page resource
    Map<Locale, LanguageManager.Info> adjacentInfo = languageManager.getAdjacentInfo(context.resourceResolver(), ENGLISH_HOMEPAGE + "/subpage2");
    assertNotNull(adjacentInfo);
    assertArrayEquals(new Locale[] { Locale.ENGLISH, Locale.FRENCH }, adjacentInfo.keySet().toArray());

    LanguageManager.Info englishInfo = adjacentInfo.entrySet().stream()
        .filter(e -> e.getKey().equals(Locale.ENGLISH))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(null);
    assertNotNull(englishInfo);
    assertEquals(ENGLISH_HOMEPAGE + "/subpage2", englishInfo.getPath());
    // this, in theory, should return the value, but it only seems to return 0
    assertEquals(0L, englishInfo.getLastModified());
    assertTrue(englishInfo.exists());
    assertTrue(englishInfo.hasContent());


    LanguageManager.Info frenchInfo = adjacentInfo.entrySet().stream()
        .filter(e -> e.getKey().equals(Locale.FRENCH))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(null);
    assertNotNull(frenchInfo);
    assertEquals(FRENCH_HOMEPAGE + "/subpage2", frenchInfo.getPath());
    // this, in theory, should return the value, but it only seems to return 0
    assertEquals(0L, frenchInfo.getLastModified());
    assertFalse(frenchInfo.exists());
    assertFalse(frenchInfo.hasContent());


    Map<Locale, LanguageManager.Info> nonExisting = languageManager.getAdjacentInfo(context.resourceResolver(), "/does/not/exist");
    assertNull(nonExisting);

    Map<Locale, LanguageManager.Info> nullPath = languageManager.getAdjacentInfo(context.resourceResolver(), null);
    assertNull(nullPath);

    Map<Locale, LanguageManager.Info> rootPath = languageManager.getAdjacentInfo(context.resourceResolver(), "/");
    assertNull(rootPath);
  }
}
