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
        /**
         * Syntax of pattern file is:
         * <ul>
         *     <li>All lines are trimmed first.</li>
         *     <li>Blank and lines beginning with # are ignored (formatting and comments.</li>
         *     <li>Remaining lines are split on <TAB></TAB></li>
         *     <li>The action depends on the number of tokens</li>
         *     <ul>
         *         <li>
         *             One: The single token becomes the default file matcher. It is overridden if subsequent lines
         *         contain a file pattern, but it's default status remains in effect until another single token line.'\
         *         The token <code>.*</code> is used to reset to the default state -- all files match unless a file
         *         match parameter is provided.
         *         </li>
         *         <li>
         *             Two: First token is an attribute matching regex with a single capture group, and
         *             the second token either:
         *             <ul>
         *                 <li><code>!!delete!!</code> meaning delete the attribute.</li>
         *                 <li>Any other string means replace the matched capture group with provided value.</li>
         *             </ul>
         *         </li>
         *         <li>
         *             Three: First token is a regex that must match the file name, and the remaining two tokens are
         *             treated likePattern.complile(".*")Pattern.complile(".*") the 2 token case above.
         *         </li>
         *     </ul>
         * </ul>
         *
         *
         */
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            int lc = 0;
            String line;
            Pattern defaultFilePat = null;
            Pattern filePat;
            Pattern attrPat;

            while ((line = br.readLine()) != null) {
                lc++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] tokens = line.split("\t");
                int tc = tokens.length;

                switch (tc) {
                    case 1:
                        defaultFilePat = tokens[0].equals(".*") ? null : Pattern.compile(tokens[0]);
                        break;

                    case 2:
                        filePat = defaultFilePat;
                        attrPat = Pattern.compile(tokens[0]);
                        if (tokens[1].equalsIgnoreCase("!!delete!!")) {
                            items.add(new Item(MatchType.ILE_ATTR_MATCH_DELETE, filePat, attrPat, null));
                        } else {
                            items.add(new Item(MatchType.FILE_ATTR_MATCH_REPLACE, filePat, attrPat, tokens[1]));
                        }
                        break;

                    case 3:
                        filePat = Pattern.compile(tokens[0]);
                        attrPat = Pattern.compile(tokens[1]);
                        if (tokens[2].equalsIgnoreCase("!!delete!!")) {
                            items.add(new Item(MatchType.ILE_ATTR_MATCH_DELETE, filePat, attrPat, null));
                        } else {
                            items.add(new Item(MatchType.FILE_ATTR_MATCH_REPLACE, filePat, attrPat, tokens[2]));
                        }
                        break;

                    default:
                        throw new IOException("File " + path + " is not properly formatted at line " + lc);
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
                    if (item.matchType == MatchType.ILE_ATTR_MATCH_DELETE) {
                        // We need to remove the entire attribute
                        sb.setLength(0);
                        sb.append(line, 0, matcher.start());
                        sb.append(line, matcher.end(), line.length());
                        line = sb.toString();
                        break;
                    } else {
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
        }

        return line;
    }

    private enum MatchType {
        FILE_BLOCK,
        ILE_ATTR_MATCH_DELETE,
        FILE_ATTR_MATCH_REPLACE
    }

    private class Item {
        private final MatchType matchType;
        private final Pattern filePat;
        private final Pattern attrPat;
        private final String replacement;

        Item(MatchType matchType, Pattern filePat, Pattern attrPat, String replacement) {
            this.matchType = matchType;
            this.filePat = filePat;
            this.attrPat = attrPat;
            this.replacement = replacement;
        }
    }
}

