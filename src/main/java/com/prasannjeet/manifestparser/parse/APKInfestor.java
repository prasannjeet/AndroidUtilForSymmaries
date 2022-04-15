package com.prasannjeet.manifestparser.parse;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.directory.DirectoryException;
import com.ebmwebsourcing.easycommons.xml.XMLPrettyPrinter;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.ltpeacock.sorter.xml.SortXMLEngine;
import com.prasannjeet.manifestparser.dto.manifest;
import com.prasannjeet.manifestparser.dto.manifest.Action;
import com.prasannjeet.manifestparser.dto.manifest.IntentFilter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.filters.StringInputStream;

public class APKInfestor {

  String apkPath;
  String outputDirectory;
  manifest manifestDto;
  List<String> intents = new ArrayList<>();

  public APKInfestor(String apkPath, String outputDirectory) {
    this.apkPath = apkPath;
    this.outputDirectory = outputDirectory;
    extractDataFromApk();
  }

  public void extractDataFromApk() {
    String tempDir = System.getProperty("java.io.tmpdir");
    String myTempDir = tempDir  + System.currentTimeMillis();

    File tempDirFile = null;
    String manifestXml = "";
    try {
      tempDirFile = new File(myTempDir);
      explodeAPKToDirectory(tempDirFile);

      String manifestLocation = myTempDir + "/AndroidManifest.xml";
      File manifestFile = new File(manifestLocation);

      manifestXml = sortAndPrettyXml(manifestFile);
      this.manifestDto = getXmlMapper().readValue(manifestXml, manifest.class);

      manifestDto.application.getActivities().forEach(activity ->
          activity.getIntentFilters().forEach(intentFilter ->
              intentFilter.getActions().forEach(action -> {
                  String name = action.getName();
                  if (name != null && !name.isEmpty()) {
                    this.intents.add(name);
                  }
              })));

    } catch (Exception e) {
      e.printStackTrace();
    } finally{
      try {
        FileUtils.deleteDirectory(tempDirFile);
      } catch (Exception e) {
        System.out.println("Could not delete temp directory");
      }
    }

    //Save manifestXml to file
    try {
      File manifestFile = new File(outputDirectory + java.io.File.separator + "AndroidManifestExtracted.xml");
      FileOutputStream fos = new FileOutputStream(manifestFile);
      fos.write(manifestXml.getBytes(StandardCharsets.UTF_8));
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error saving manifestXml to file");
    }
  }

  public List<String> getAllIntents() {
    return this.intents.stream().filter(intent -> intent.contains("android.intent.action")).collect(toList());
  }

  public Map<String, List<String>> getIntentsByActivity() {
    return this.manifestDto.application.getActivities().stream()
        .filter(a -> a.name != null && !a.name.isEmpty())
        .collect(
            Collectors.toMap(
                a -> a.name,
                a -> {
                  List<IntentFilter> intentFilters = a.getIntentFilters();
                  return intentFilters.stream()
                      .map(
                          intentFilter ->
                              intentFilter.getActions().stream()
                                  .map(Action::getName)
                                  .collect(toList()))
                      .collect(toList())
                      .stream()
                      .flatMap(List::stream)
                      .collect(toList());
                },
                (x, y) -> x))
        .entrySet()
        .stream()
        .filter(es -> !es.getValue().isEmpty())
        .collect(
            toMap(
                Entry::getKey,
                item -> {
                  List<String> allIntents = item.getValue();
                  return allIntents.stream()
                      .filter(allIntent -> allIntent.contains("android.intent.action"))
                      .collect(toList());
                }))
        .entrySet()
        .stream()
        .filter(es -> !es.getValue().isEmpty())
        .collect(
            toMap(Entry::getKey, item -> item.getValue().stream().distinct().collect(toList())));
  }



  private void explodeAPKToDirectory(File outputDir)
      throws AndrolibException, IOException, DirectoryException {
    ApkDecoder decoder = new ApkDecoder();
    File apkFile = new File(apkPath);
    decoder.setApkFile(apkFile);
    decoder.setOutDir(outputDir);
    decoder.setAnalysisMode(true);
    decoder.decode();
  }

  public static String inputStreamToString(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    return sb.toString();
  }

  public static XmlMapper getXmlMapper() {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.configure(Feature.ALLOW_COMMENTS, true);
    xmlMapper.configure(Feature.IGNORE_UNDEFINED, true);
    xmlMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
    xmlMapper.configure(Feature.STRICT_DUPLICATE_DETECTION, false);
    xmlMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    xmlMapper.configure(Feature.ALLOW_MISSING_VALUES, true);
    return xmlMapper;
  }

  public static String sortAndPrettyXml(File xmlFile) throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    FileInputStream fis = new FileInputStream(xmlFile);
    SortXMLEngine engine = new SortXMLEngine();
    engine.sort(fis, os);

    String sorted = os.toString(StandardCharsets.UTF_8);
    return prettyXml(sorted);
  }

  public static String prettyXml(String xml) throws Exception {
    StringInputStream inputStream = new StringInputStream(xml);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    XMLPrettyPrinter.prettify(inputStream, outputStream);

    return outputStream.toString(StandardCharsets.UTF_8);
  }
}
