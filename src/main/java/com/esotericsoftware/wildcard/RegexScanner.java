
package com.esotericsoftware.wildcard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

class RegexScanner {
    private final File rootDir;
    private final List<Pattern> includePatterns;
    private final List<String> matches = new ArrayList<>(128);

    public RegexScanner(File rootDir, List<String> includes, List<String> excludes) {
        if (rootDir == null) throw new IllegalArgumentException("rootDir cannot be null.");
        if (!rootDir.exists()) throw new IllegalArgumentException("Directory does not exist: " + rootDir);
        if (!rootDir.isDirectory()) throw new IllegalArgumentException("File must be a directory: " + rootDir);
        try {
            rootDir = rootDir.getCanonicalFile();
        } catch (IOException ex) {
            throw new RuntimeException("OS error determining canonical path: " + rootDir, ex);
        }
        this.rootDir = rootDir;

        if (includes == null) throw new IllegalArgumentException("includes cannot be null.");
        if (excludes == null) throw new IllegalArgumentException("excludes cannot be null.");

        includePatterns = new ArrayList<>();
        for (String include : includes)
            includePatterns.add(Pattern.compile(include, Pattern.CASE_INSENSITIVE));

        List<Pattern> excludePatterns = new ArrayList<>();
        for (String exclude : excludes)
            excludePatterns.add(Pattern.compile(exclude, Pattern.CASE_INSENSITIVE));

        scanDir(rootDir);

        for (Iterator<String> matchIter = matches.iterator(); matchIter.hasNext(); ) {
            String filePath = matchIter.next();
            for (Pattern exclude : excludePatterns)
                if (exclude.matcher(filePath).matches()) matchIter.remove();
        }
    }

    private void scanDir(File dir) {
        for (File file : dir.listFiles()) {
            for (Pattern include : includePatterns) {
                int length = rootDir.getPath().length();
                if (!rootDir.getPath().endsWith(File.separator)) length++; // Lose starting slash.
                String filePath = file.getPath().substring(length);
                if (include.matcher(filePath).matches()) {
                    matches.add(filePath);
                    break;
                }
            }
            if (file.isDirectory()) scanDir(file);
        }
    }

    public List<String> matches() {
        return matches;
    }

    public File rootDir() {
        return rootDir;
    }
}
