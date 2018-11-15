package com.vector.svg2vectorandroid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transformer {
    private List<Item> items = new ArrayList<>(128);
    private StringBuilder sb = new StringBuilder(128);

    Transformer(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            int lc = 0;
            String line;

            while ((line = br.readLine()) != null) {
                lc++;
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] tokens = line.split("\t");
                    String file, pat, result;
                    if (tokens.length == 3) {
                        file = tokens[0];
                        pat = tokens[1];
                        result = tokens[2];
                    } else if (tokens.length == 2) {
                        file = ".*";
                        pat = tokens[0];
                        result = tokens[1];
                    } else {
                        throw new IOException("File " + path + " is not properly formatted at line " + lc);
                    }

                    Pattern filePat = file.equals(".*") ? null : Pattern.compile(file);
                    items.add(new Item(filePat, Pattern.compile(pat), result));
                }
            }

        }
    }

    public String transform(String fileName, String line) {
        for (Item item : items) {
            if (item.filePat == null || item.filePat.matcher(fileName).find()) {
                // File part is a go..
                Matcher matcher = item.attrPat.matcher(line);
                if (matcher.find()) {
                    if (matcher.groupCount() == 1) {
                        sb.setLength(0);
                        sb.append(line, 0, matcher.start(1));
                        sb.append(item.replacement);
                        sb.append(line, matcher.end(1), line.length());
                        line = sb.toString();
                        break;
                    }
                }
            }
        }

        return line;
    }

    private class Item {
        private final Pattern filePat;
        private final Pattern attrPat;
        private final String replacement;

        Item(Pattern filePat, Pattern attrPat, String replacement) {

            this.filePat = filePat;
            this.attrPat = attrPat;
            this.replacement = replacement;
        }
    }
}

