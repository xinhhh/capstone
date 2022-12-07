package com.cmclinnovations.stack.clients.gdal;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmclinnovations.stack.clients.core.EndpointNames;
import com.cmclinnovations.stack.clients.core.StackClient;
import com.cmclinnovations.stack.clients.docker.ContainerClient;
import com.cmclinnovations.stack.clients.postgis.PostGISEndpointConfig;
import com.cmclinnovations.stack.clients.utils.FileUtils;
import com.cmclinnovations.stack.clients.utils.TempDir;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class GDALClient extends ContainerClient {

    private static final Logger logger = LoggerFactory.getLogger(GDALClient.class);

    private final PostGISEndpointConfig postgreSQLEndpoint;

    public GDALClient() {
        postgreSQLEndpoint = readEndpointConfig(EndpointNames.POSTGIS, PostGISEndpointConfig.class);
    }

    private String computePGSQLSourceString(String database) {
        return "PG:dbname=" + database + " host=" + postgreSQLEndpoint.getHostName()
                + " port=" + postgreSQLEndpoint.getPort() + " user=" + postgreSQLEndpoint.getUsername()
                + " password=" + postgreSQLEndpoint.getPassword();
    }

    public void uploadVectorStringToPostGIS(String database, String layername, String fileContents,
            Ogr2OgrOptions options, boolean append) {

        try (TempDir tmpDir = makeLocalTempDir()) {
            Path filePath = tmpDir.getPath().resolve(layername);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.append(fileContents);
                writer.flush();
                uploadVectorToPostGIS(database, layername, filePath.toString(), options, append);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write string for vector '" + layername
                        + "' layer to a file in a temporary directory.");
            }
        }
    }

    public void uploadVectorFilesToPostGIS(String database, String layername, String dirPath, Ogr2OgrOptions options,
            boolean append) {
        try (TempDir tmpDir = makeLocalTempDir()) {
            tmpDir.copyFrom(Path.of(dirPath));
            String gdalContainerId = getContainerId("gdal");
            Multimap<String, String> foundGeoFiles = findGeoFiles(gdalContainerId, tmpDir.toString());
            for (Collection<String> filesOfType : foundGeoFiles.asMap().values()) {
                for (String filePath : filesOfType) {
                    uploadVectorToPostGIS(database, layername, filePath, options, append);
                    // If inserting multiple sources into a single layer then ensure subsequent
                    // files are appended.
                    if (null != layername) {
                        append = true;
                    }
                }
            }
        }
    }

    public void uploadVectorFileToPostGIS(String database, String layername, String filePath, Ogr2OgrOptions options,
            boolean append) {

        try (TempDir tmpDir = makeLocalTempDir()) {
            Path sourcePath = Path.of(filePath);
            tmpDir.copyFrom(sourcePath);
            uploadVectorToPostGIS(database, layername, tmpDir.getPath().resolve(sourcePath.getFileName()).toString(),
                    options, append);
        }
    }

    public void uploadVectorURLToPostGIS(String database, String layername, String url, Ogr2OgrOptions options,
            boolean append) {
        uploadVectorToPostGIS(database, layername, url, options, append);
    }

    private void uploadVectorToPostGIS(String database, String layername, String filePath, Ogr2OgrOptions options,
            boolean append) {

        String containerId = getContainerId("gdal");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        String execId = createComplexCommand(containerId, options.appendToArgs(layername, "ogr2ogr",
                "-f", "PostgreSQL",
                computePGSQLSourceString(database),
                filePath,
                "--config", "OGR_TRUNCATE", append ? "NO" : "YES",
                "--config", "PG_USE_COPY", "YES"))
                .withOutputStream(outputStream)
                .withErrorStream(errorStream)
                .withEnvVars(options.getEnv())
                .withEvaluationTimeout(300)
                .exec();

        handleErrors(errorStream, execId);
    }

    private void handleErrors(ByteArrayOutputStream errorStream, String execId) {
        long commandErrorCode = getCommandErrorCode(execId);
        if (0 != commandErrorCode) {
            throw new RuntimeException("Docker exec command returned '" + commandErrorCode
                    + "' and wrote the following to stderr:\n" + errorStream.toString());
        } else {
            logger.warn("Docker exec command returned '0' but wrote the following to stderr:\n{}", errorStream);
        }
    }

    public void uploadRasterFilesToPostGIS(String database, String layername,
            String dirPath, GDALTranslateOptions options, boolean append) {

        String gdalContainerId = getContainerId("gdal");
        String postGISContainerId = getContainerId("postgis");

        try (TempDir tempDir = makeLocalTempDir()) {

            tempDir.copyFrom(Path.of(dirPath));

            List<String> geotiffFiles = convertRastersToGeoTiffs(gdalContainerId, layername, tempDir, options);

            ensurePostGISRasterSupportEnabled(postGISContainerId, database);

            uploadRasters(postGISContainerId, database, layername, geotiffFiles, append);
        }
    }

    private Multimap<String, String> findGeoFiles(String containerId, String dirPath) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        String execId = createComplexCommand(containerId, "gdalmanage", "identify", "-r", dirPath)
                .withOutputStream(outputStream)
                .withErrorStream(errorStream)
                .exec();

        handleErrors(errorStream, execId);

        return outputStream.toString().lines()
                .map(entry -> entry.split(": "))
                .collect(ArrayListMultimap::create,
                        (m, pair) -> m.put(pair[1], pair[0]),
                        Multimap::putAll);
    }

    private List<String> convertRastersToGeoTiffs(String gdalContainerId, String layername, TempDir tempDir,
            GDALTranslateOptions options) {

        Multimap<String, String> foundRasterFiles = findGeoFiles(gdalContainerId, tempDir.toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        Set<Path> createdDirectories = new HashSet<>();

        List<String> geotiffFiles = new ArrayList<>();

        for (Map.Entry<String, Collection<String>> fileTypeEntry : foundRasterFiles.asMap().entrySet()) {
            String inputFormat = fileTypeEntry.getKey();
            for (String filePath : fileTypeEntry.getValue()) {

                String outputPath = generateRasterOutPath(tempDir.toString(), filePath, layername);
                geotiffFiles.add(outputPath);

                Path directoryPath = Paths.get(outputPath).getParent();
                if (!createdDirectories.contains(directoryPath)) {
                    makeDir(gdalContainerId, directoryPath.toString());
                    executeSimpleCommand(gdalContainerId, "chown", "-R", "1000:1000", directoryPath.toString());
                    createdDirectories.add(directoryPath);
                }

                String execId = createComplexCommand(gdalContainerId, options.appendToArgs("gdal_translate",
                        "-if", inputFormat,
                        // https://gdal.org/drivers/raster/cog.html#raster-cog
                        "-of", "COG",
                        filePath,
                        outputPath))
                        .withOutputStream(outputStream)
                        .withErrorStream(errorStream)
                        .withEnvVars(options.getEnv())
                        .withEvaluationTimeout(300)
                        .exec();

                handleErrors(errorStream, execId);
            }
        }
        return geotiffFiles;
    }

    private void ensurePostGISRasterSupportEnabled(String postGISContainerId, String database) {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        String execId = createComplexCommand(postGISContainerId,
                "psql", "-U", postgreSQLEndpoint.getUsername(), "-d", database, "-w")
                .withHereDocument("CREATE EXTENSION IF NOT EXISTS postgis_raster;" +
                        "ALTER DATABASE " + database + " SET postgis.enable_outdb_rasters = True;" +
                        "ALTER DATABASE " + database + " SET postgis.gdal_enabled_drivers = 'GTiff';")
                .withErrorStream(errorStream)
                .exec();
        handleErrors(errorStream, execId);
    }

    private void uploadRasters(String postGISContainerId, String database, String layername,
            List<String> geotiffFiles, boolean append) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        String mode = append ? "-a" : "-d";
        String execId = createComplexCommand(postGISContainerId, "bash", "-c",
                // https://postgis.net/docs/using_raster_dataman.html#RT_Raster_Loader
                "raster2pgsql " + mode + " -C -t auto -R -F -I -M -Y"
                        + geotiffFiles.stream().collect(Collectors.joining(" ", " ", " "))
                        + layername
                        + " | psql -U " + postgreSQLEndpoint.getUsername() + " -d " + database + " -w")
                .withOutputStream(outputStream)
                .withErrorStream(errorStream)
                .withEvaluationTimeout(300)
                .exec();

        handleErrors(errorStream, execId);
    }

    private String generateRasterOutPath(String basePathIn, String filePath, String layerName) {
        return FileUtils.replaceExtension(
                Path.of(StackClient.GEOTIFFS_DIR, layerName)
                        .resolve(Path.of(basePathIn).relativize(Path.of(filePath)))
                        .toString(),
                ".tif");
    }

}
