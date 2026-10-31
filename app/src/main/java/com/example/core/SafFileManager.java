package com.example.core;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.example.core.models.LocalRepository;
import com.example.core.models.ProjectFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SafFileManager {
    private static final String TAG = "SafFileManager";
    private final Context context;
    private final File baseProjectsDir;

    private static final Set<String> DEFAULT_EXCLUDED_DIRS = new HashSet<>(Arrays.asList(
            ".git", "build", ".gradle", "node_modules", ".idea", "bin", "out", ".dart_tool", "target"
    ));

    public SafFileManager(Context context) {
        this.context = context.getApplicationContext();
        File externalFiles = context.getExternalFilesDir("projects");
        if (externalFiles != null && (externalFiles.exists() || externalFiles.mkdirs())) {
            this.baseProjectsDir = externalFiles;
        } else {
            this.baseProjectsDir = new File(context.getFilesDir(), "projects");
            if (!baseProjectsDir.exists()) baseProjectsDir.mkdirs();
        }
    }

    public File getBaseProjectsDir() {
        return baseProjectsDir;
    }

    public List<LocalRepository> getDiscoveredRepositories() {
        List<LocalRepository> repos = new ArrayList<>();
        if (!baseProjectsDir.exists()) return repos;

        File[] files = baseProjectsDir.listFiles();
        if (files == null) return repos;

        for (File dir : files) {
            if (dir.isDirectory()) {
                File gitDir = new File(dir, ".git");
                boolean isGit = gitDir.exists() && gitDir.isDirectory();
                LocalRepository repo = new LocalRepository(
                        dir.getName(),
                        dir.getAbsolutePath(),
                        "",
                        "main",
                        isGit
                );
                repos.add(repo);
            }
        }
        return repos;
    }

    public File createNewProjectDirectory(String projectName) throws IOException {
        String cleanName = projectName.replaceAll("[^a-zA-Z0-9_.-]", "_");
        File dir = new File(baseProjectsDir, cleanName);
        if (dir.exists()) {
            throw new IOException("A project directory with this name already exists: " + cleanName);
        }
        if (!dir.mkdirs()) {
            throw new IOException("Failed to create project directory: " + cleanName);
        }
        return dir;
    }

    public List<ProjectFile> scanProjectFiles(File projectDir, Set<String> customExcluded) {
        List<ProjectFile> list = new ArrayList<>();
        if (projectDir == null || !projectDir.exists()) return list;

        Set<String> excluded = new HashSet<>(DEFAULT_EXCLUDED_DIRS);
        if (customExcluded != null) {
            excluded.addAll(customExcluded);
        }

        scanRecursive(projectDir, projectDir, list, excluded);
        return list;
    }

    private void scanRecursive(File root, File current, List<ProjectFile> results, Set<String> excluded) {
        File[] files = current.listFiles();
        if (files == null) return;

        for (File file : files) {
            String name = file.getName();
            if (excluded.contains(name)) {
                continue;
            }

            String relative = root.toURI().relativize(file.toURI()).getPath();
            if (relative.endsWith("/")) {
                relative = relative.substring(0, relative.length() - 1);
            }

            ProjectFile pf = new ProjectFile(
                    name,
                    file.getAbsolutePath(),
                    relative,
                    file.isDirectory(),
                    file.length(),
                    file.lastModified()
            );
            results.add(pf);

            if (file.isDirectory()) {
                scanRecursive(root, file, results, excluded);
            }
        }
    }

    public int importFromSafTree(Uri treeUri, File targetDir) throws IOException {
        DocumentFile rootDoc = DocumentFile.fromTreeUri(context, treeUri);
        if (rootDoc == null || !rootDoc.isDirectory()) {
            throw new IOException("Selected storage location is not accessible as a directory.");
        }
        if (!targetDir.exists()) targetDir.mkdirs();

        return copyDocFolderToLocal(rootDoc, targetDir);
    }

    private int copyDocFolderToLocal(DocumentFile docFolder, File localDir) throws IOException {
        int count = 0;
        DocumentFile[] files = docFolder.listFiles();
        for (DocumentFile doc : files) {
            String name = doc.getName();
            if (name == null || DEFAULT_EXCLUDED_DIRS.contains(name)) continue;

            if (doc.isDirectory()) {
                File subDir = new File(localDir, name);
                if (!subDir.exists()) subDir.mkdirs();
                count += copyDocFolderToLocal(doc, subDir);
            } else if (doc.isFile()) {
                File localFile = new File(localDir, name);
                try (InputStream in = context.getContentResolver().openInputStream(doc.getUri());
                     OutputStream out = new FileOutputStream(localFile)) {
                    if (in != null) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        count++;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to copy file " + name + ": " + e.getMessage());
                }
            }
        }
        return count;
    }

    public String readGitignore(File projectDir) {
        File gitignoreFile = new File(projectDir, ".gitignore");
        if (!gitignoreFile.exists()) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(gitignoreFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading .gitignore: " + e.getMessage());
        }
        return sb.toString();
    }

    public void writeGitignore(File projectDir, String content) throws IOException {
        File gitignoreFile = new File(projectDir, ".gitignore");
        try (FileWriter writer = new FileWriter(gitignoreFile, false)) {
            writer.write(content != null ? content : "");
        }
    }

    public String getGitignorePreset(String presetType) {
        switch (presetType.toLowerCase()) {
            case "android":
                return "# Android Studio & Gradle\n" +
                        "*.iml\n" +
                        ".gradle/\n" +
                        "/local.properties\n" +
                        "/.idea/\n" +
                        ".DS_Store\n" +
                        "/build\n" +
                        "/*/build\n" +
                        "*.hprof\n" +
                        "*.apk\n" +
                        "*.aab\n" +
                        "captures/\n";
            case "java":
                return "# Java\n" +
                        "*.class\n" +
                        "*.log\n" +
                        "*.jar\n" +
                        "*.war\n" +
                        "*.nar\n" +
                        "*.ear\n" +
                        "*.zip\n" +
                        "*.tar.gz\n" +
                        "*.rar\n" +
                        "bin/\n" +
                        "build/\n" +
                        "out/\n" +
                        "target/\n";
            case "node":
                return "# Node.js & Web\n" +
                        "node_modules/\n" +
                        "npm-debug.log*\n" +
                        "yarn-debug.log*\n" +
                        "yarn-error.log*\n" +
                        ".pnpm-debug.log*\n" +
                        ".env\n" +
                        ".env.local\n" +
                        "dist/\n" +
                        ".cache/\n";
            case "cpp":
                return "# C / C++\n" +
                        "*.o\n" +
                        "*.obj\n" +
                        "*.dll\n" +
                        "*.so\n" +
                        "*.dylib\n" +
                        "*.exe\n" +
                        "*.out\n" +
                        "*.app\n" +
                        "cmake-build-*/\n" +
                        "CMakeFiles/\n";
            case "general":
            default:
                return "# General & Editor\n" +
                        ".DS_Store\n" +
                        "Thumbs.db\n" +
                        "*.swp\n" +
                        "*~\n" +
                        ".vscode/\n" +
                        ".idea/\n" +
                        "*.log\n";
        }
    }

    public void generateStarterProject(File projectDir, String templateType, String name) throws IOException {
        if (!projectDir.exists()) projectDir.mkdirs();

        writeGitignore(projectDir, getGitignorePreset(templateType.equals("android") ? "android" : (templateType.equals("node") ? "node" : "java")));

        File readme = new File(projectDir, "README.md");
        String readmeContent = "# " + name + "\n\n" +
                "Managed and published with **RepoStudio** on Android.\n\n" +
                "## Overview\n" +
                "This repository was created and configured on a mobile device using RepoStudio with full Git and GitHub integration.\n\n" +
                "### Getting Started\n" +
                "Clone the repository:\n" +
                "```bash\n" +
                "git clone <repository_url>\n" +
                "```\n";
        try (FileWriter fw = new FileWriter(readme)) {
            fw.write(readmeContent);
        }

        switch (templateType.toLowerCase()) {
            case "android":
                File srcMain = new File(projectDir, "src/main/java/com/example");
                srcMain.mkdirs();
                File mainActivity = new File(srcMain, "MainActivity.java");
                try (FileWriter fw = new FileWriter(mainActivity)) {
                    fw.write("package com.example;\n\npublic class MainActivity {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from RepoStudio Android Project!\");\n    }\n}\n");
                }
                break;
            case "node":
                File pkg = new File(projectDir, "package.json");
                try (FileWriter fw = new FileWriter(pkg)) {
                    fw.write("{\n  \"name\": \"" + name.toLowerCase() + "\",\n  \"version\": \"1.0.0\",\n  \"description\": \"RepoStudio Web Project\",\n  \"main\": \"index.js\"\n}\n");
                }
                File indexJs = new File(projectDir, "index.js");
                try (FileWriter fw = new FileWriter(indexJs)) {
                    fw.write("// RepoStudio Node.js project\nconsole.log('RepoStudio is running on Android!');\n");
                }
                break;
            case "web":
                File indexHtml = new File(projectDir, "index.html");
                try (FileWriter fw = new FileWriter(indexHtml)) {
                    fw.write("<!DOCTYPE html>\n<html>\n<head>\n  <title>" + name + "</title>\n  <style>body { font-family: sans-serif; background: #0d1117; color: #fff; padding: 2rem; }</style>\n</head>\n<body>\n  <h1>Welcome to " + name + "</h1>\n  <p>Published via RepoStudio Android GitHub Client.</p>\n</body>\n</html>\n");
                }
                break;
            case "java":
            default:
                File src = new File(projectDir, "src");
                src.mkdirs();
                File app = new File(src, "App.java");
                try (FileWriter fw = new FileWriter(app)) {
                    fw.write("public class App {\n    public static void main(String[] args) {\n        System.out.println(\"Welcome to " + name + " - Built with RepoStudio\");\n    }\n}\n");
                }
                break;
        }
    }
}
