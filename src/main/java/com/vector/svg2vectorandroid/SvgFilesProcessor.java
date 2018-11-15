package com.vector.svg2vectorandroid;

import com.android.ide.common.vectordrawable.Svg2Vector;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Created by ravi on 18/12/17.
 */

public class SvgFilesProcessor {

    private Path sourceSvgPath;
    private Path destinationVectorPath;
    private String namePrefix = "";
    private String extentionSuffix = "";
    private Transformer transformer;


    public SvgFilesProcessor(
            String sourceSvgDirectory,
            String destinationVectorDirectory,
            Transformer transformer,
            String namePrefix,
            String nameSuffix) throws IOException {

        sourceSvgPath = Paths.get(sourceSvgDirectory);
        if (!Files.isDirectory(sourceSvgPath)) {
            throw new IOException(String.format("Source %s is not a directory.", sourceSvgPath.toAbsolutePath()));
        }

        if (destinationVectorDirectory == null) {
            // Default to a sibling directory with a suffix
            destinationVectorPath = new File(sourceSvgPath.getParent().toFile(), sourceSvgPath.toFile().getName() + "_vector").toPath();
        } else {
            destinationVectorPath = Paths.get(destinationVectorDirectory);
        }

        this.transformer = transformer;
        if (namePrefix != null) this.namePrefix = namePrefix;
        if (nameSuffix != null) this.extentionSuffix = nameSuffix;

        destinationVectorPath = Files.createDirectories(destinationVectorPath);
    }

    public void process() throws IOException {
        EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(sourceSvgPath, options, Integer.MAX_VALUE, new FileVisitor<Path>() {

            public FileVisitResult postVisitDirectory(Path dir,
                                                      IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult preVisitDirectory(Path dir,
                                                     BasicFileAttributes attrs) throws IOException {
                // Skip folder which is processing svgs to xml
                if (dir.equals(destinationVectorPath)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                Path newDirectory = destinationVectorPath.resolve(sourceSvgPath.relativize(dir));
                Files.createDirectories(newDirectory);
                return CONTINUE;
            }


            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs) throws IOException {
                convertToVector(file, destinationVectorPath.resolve(sourceSvgPath.relativize(file)));
                return CONTINUE;
            }


            public FileVisitResult visitFileFailed(Path file,
                                                   IOException exc) throws IOException {
                throw exc;
            }
        });
    }

    private void convertToVector(Path source, Path target) throws IOException {
        String targetName = String.valueOf(target.getFileName());
        // convert only if it is .svg
        if (targetName.endsWith(".svg")) {
            Path parent = target.getParent();
            StringBuilder fnb = new StringBuilder();
            if (!namePrefix.isEmpty()) fnb.append(namePrefix);
            fnb.append(targetName, 0, targetName.length() - 4);
            if (!extentionSuffix.isEmpty()) fnb.append(extentionSuffix);
            fnb.append(".xml");

            File targetFile = new File(parent.toFile(), fnb.toString());
            File tempFile = File.createTempFile("svgt-", ".xml");
            tempFile.deleteOnExit();
            FileOutputStream tos = new FileOutputStream(tempFile);
            Svg2Vector.parseSvgToXml(source.toFile(), tos);
            String line;
            try (
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile)))) {
                while ((line = br.readLine()) != null) {
                    if (transformer != null) {
                        bw.write(transformer.transform(targetFile.getName(), line));
                    } else {
                        bw.write(transform(line));
                    }
                    bw.write(System.lineSeparator());
                }
            } finally {
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }
        } else {
            System.out.println("Skipping file as its not svg " + source.getFileName().toString());
        }
    }

    private String transform(String line) {
        if (line.contains("android:width"))
            line = line.replaceFirst("android:width=\".*dp\"", "android:width=\"" + 48 + "dp\"");
        if (line.contains("android:height"))
            line = line.replaceFirst("android:height=\".*dp\"", "android:height=\"" + 48 + "dp\"");
        return line;
    }

}
