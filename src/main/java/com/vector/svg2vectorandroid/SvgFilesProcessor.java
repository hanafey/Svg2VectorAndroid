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
    private String extention;
    private String extentionSuffix;

    public SvgFilesProcessor(String sourceSvgDirectory) throws IOException {
        this(sourceSvgDirectory, null, "xml", "");
    }

    public SvgFilesProcessor(String sourceSvgDirectory, String destinationVectorDirectory) throws IOException {
        this(sourceSvgDirectory, destinationVectorDirectory, "xml", "");
    }

    public SvgFilesProcessor(String sourceSvgDirectory, String destinationVectorDirectory, String extention,
                             String extentionSuffix) throws IOException {

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

        this.extention = extention;
        this.extentionSuffix = extentionSuffix;

        Path resultPath = Files.createDirectories(destinationVectorPath);
        destinationVectorPath = resultPath;
    }

    public void process() throws IOException {
        EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(sourceSvgPath, options, Integer.MAX_VALUE, new FileVisitor<Path>() {

            public FileVisitResult postVisitDirectory(Path dir,
                                                      IOException exc) throws IOException {
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
        // convert only if it is .svg
        if (source.getFileName().toString().endsWith(".svg")) {
            File targetFile = getFileWithXMlExtention(target, extention, extentionSuffix);
            File tempFile = File.createTempFile("svgt-", ".xml");
            tempFile.deleteOnExit();
            FileOutputStream tos = new FileOutputStream(tempFile);
            Svg2Vector.parseSvgToXml(source.toFile(), tos);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile)));
            String line = null;
            IOException ex = null;
            try {
                while ((line = br.readLine()) != null) {
                    bw.write(transform(line));
                    bw.write(System.lineSeparator());
                }
            } finally {
                try {
                    br.close();
                } catch (IOException ignored) {
                    ex = ignored;
                }
                try {
                    bw.close();
                } catch (IOException ignored) {
                    ex = ignored;
                }
                tempFile.delete();
            }
            if (ex != null) {
                throw ex;
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

    private File getFileWithXMlExtention(Path target, String extention, String extentionSuffix) {
        String svgFilePath = target.toFile().getAbsolutePath();
        StringBuilder svgBaseFile = new StringBuilder();
        int index = svgFilePath.lastIndexOf(".");
        if (index != -1) {
            String subStr = svgFilePath.substring(0, index);
            svgBaseFile.append(subStr);
        }
        svgBaseFile.append(null != extentionSuffix ? extentionSuffix : "");
        svgBaseFile.append(".");
        svgBaseFile.append(extention);
        return new File(svgBaseFile.toString());
    }

}
