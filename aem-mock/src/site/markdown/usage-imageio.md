## Java ImageIO - Advanced Image File Format Support

The [Layer][aem-layer] class from AEM is very useful for doing image manipulation in AEM. It is also used in the [Adobe Core WCM Components][aem-core-components] and [wcm.io Media Handler][wcmio-media-handler] for image processing.

When testing AEM application code with AEM Mocks, the Layer class can be used as well. Out-of-the-box the following file formats are supported:

* JPEG
* PNG
* GIF

If you need support for additional file formats in your unit tests, you have to add Java ImageIO plugins to your test classpath. A good source for such plugins is [TwelveMonkeys ImageIO][twelvemonkeys-imageio].

### TIFF

For adding support for TIFF images add to your POM:

```xml
<dependency>
  <groupId>com.twelvemonkeys.imageio</groupId>
  <artifactId>imageio-tiff</artifactId>
  <version>3.7.0</version>
  <scope>test</scope>
</dependency>
```

### SVG

For adding support for SVG vector images add to your POM:

```xml
<dependency>
  <groupId>com.twelvemonkeys.imageio</groupId>
  <artifactId>imageio-batik</artifactId>
  <version>3.7.0</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.apache.xmlgraphics</groupId>
  <artifactId>batik-transcoder</artifactId>
  <version>1.9.1</version>
  <scope>test</scope>
</dependency>
```



[aem-layer]: https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/javadoc/com/day/image/Layer.html
[aem-core-components]: https://github.com/adobe/aem-core-wcm-components
[wcmio-media-handler]: https://wcm.io/handler/media/
[twelvemonkeys-imageio]: https://github.com/haraldk/TwelveMonkeys
