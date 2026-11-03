package com.example.core;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: classes5.dex */
public class SafFileManager {
    private static final Set<String> DEFAULT_EXCLUDED_DIRS = new HashSet(Arrays.asList(".git", "build", ".gradle", "node_modules", ".idea", "bin", "out", ".dart_tool", "target"));
    private static final Set<String> EXCLUDED_FROM_SYNC = new HashSet(Arrays.asList("node_modules", ".gradle", "build", ".idea", "bin", "out", ".dart_tool", "target"));
    private static final String TAG = "SafFileManager";
    private final File baseProjectsDir;
    private final Context context;

    public SafFileManager(Context context) {
        this.context = context.getApplicationContext();
        File externalFiles = context.getExternalFilesDir("projects");
        if (externalFiles != null && (externalFiles.exists() || externalFiles.mkdirs())) {
            this.baseProjectsDir = externalFiles;
            return;
        }
        this.baseProjectsDir = new File(context.getFilesDir(), "projects");
        if (!this.baseProjectsDir.exists()) {
            this.baseProjectsDir.mkdirs();
        }
    }

    public File getBaseProjectsDir() {
        return this.baseProjectsDir;
    }

    public List<LocalRepository> getDiscoveredRepositories() {
        File[] files;
        List<LocalRepository> repos = new ArrayList<>();
        if (!this.baseProjectsDir.exists() || (files = this.baseProjectsDir.listFiles()) == null) {
            return repos;
        }
        for (File dir : files) {
            if (dir.isDirectory()) {
                File gitDir = new File(dir, ".git");
                boolean isGit = gitDir.exists() && gitDir.isDirectory();
                LocalRepository repo = new LocalRepository(dir.getName(), dir.getAbsolutePath(), "", "main", isGit);
                repos.add(repo);
            }
        }
        return repos;
    }

    public File createNewProjectDirectory(String projectName) throws IOException {
        String cleanName = projectName.replaceAll("[^a-zA-Z0-9_.-]", "_");
        File dir = new File(this.baseProjectsDir, cleanName);
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
        if (projectDir == null || !projectDir.exists()) {
            return list;
        }
        Set<String> excluded = new HashSet<>(DEFAULT_EXCLUDED_DIRS);
        if (customExcluded != null) {
            excluded.addAll(customExcluded);
        }
        scanRecursive(projectDir, projectDir, list, excluded);
        return list;
    }

    private void scanRecursive(File root, File current, List<ProjectFile> results, Set<String> excluded) {
        String relative;
        File[] files = current.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String name = file.getName();
            if (!excluded.contains(name)) {
                String relative2 = root.toURI().relativize(file.toURI()).getPath();
                if (!relative2.endsWith("/")) {
                    relative = relative2;
                } else {
                    relative = relative2.substring(0, relative2.length() - 1);
                }
                ProjectFile pf = new ProjectFile(name, file.getAbsolutePath(), relative, file.isDirectory(), file.length(), file.lastModified());
                results.add(pf);
                if (file.isDirectory()) {
                    scanRecursive(root, file, results, excluded);
                }
            }
        }
    }

    public static class OpenFolderResult {
        private final int copiedFileCount;
        private final File directory;
        private final String folderName;
        private final boolean inPlace;
        private final String treeUriString;

        public OpenFolderResult(File directory, String folderName, boolean inPlace, int copiedFileCount, String treeUriString) {
            this.directory = directory;
            this.folderName = folderName;
            this.inPlace = inPlace;
            this.copiedFileCount = copiedFileCount;
            this.treeUriString = treeUriString != null ? treeUriString : "";
        }

        public File getDirectory() {
            return this.directory;
        }

        public String getFolderName() {
            return this.folderName;
        }

        public boolean isInPlace() {
            return this.inPlace;
        }

        public int getCopiedFileCount() {
            return this.copiedFileCount;
        }

        public String getTreeUriString() {
            return this.treeUriString;
        }
    }

    public String extractFolderName(Uri treeUri) {
        if (treeUri == null) {
            return "Project";
        }
        try {
            DocumentFile doc = DocumentFile.fromTreeUri(this.context, treeUri);
            if (doc != null && doc.getName() != null && !doc.getName().trim().isEmpty()) {
                return doc.getName().trim();
            }
        } catch (Exception e) {
        }
        try {
            String docId = DocumentsContract.getTreeDocumentId(treeUri);
            if (docId != null) {
                int colonIdx = docId.indexOf(58);
                String pathPart = colonIdx >= 0 ? docId.substring(colonIdx + 1) : docId;
                if (pathPart.endsWith("/")) {
                    pathPart = pathPart.substring(0, pathPart.length() - 1);
                }
                int slashIdx = pathPart.lastIndexOf(47);
                String name = slashIdx >= 0 ? pathPart.substring(slashIdx + 1) : pathPart;
                if (!name.trim().isEmpty()) {
                    return name.trim();
                }
            }
        } catch (Exception e2) {
        }
        String lastSeg = treeUri.getLastPathSegment();
        if (lastSeg != null) {
            int colonIdx2 = lastSeg.indexOf(58);
            String pathPart2 = colonIdx2 >= 0 ? lastSeg.substring(colonIdx2 + 1) : lastSeg;
            if (pathPart2.endsWith("/")) {
                pathPart2 = pathPart2.substring(0, pathPart2.length() - 1);
            }
            int slashIdx2 = pathPart2.lastIndexOf(47);
            String name2 = slashIdx2 >= 0 ? pathPart2.substring(slashIdx2 + 1) : pathPart2;
            if (!name2.trim().isEmpty()) {
                return name2.trim();
            }
        }
        return "Project";
    }

    public File resolveDirectFile(Uri treeUri) {
        if (treeUri == null) {
            return null;
        }
        try {
            String docId = DocumentsContract.getTreeDocumentId(treeUri);
            if (docId != null) {
                if (docId.startsWith("primary:")) {
                    String subPath = docId.substring("primary:".length());
                    if (subPath.startsWith("/")) {
                        subPath = subPath.substring(1);
                    }
                    File primaryDir = Environment.getExternalStorageDirectory();
                    File target = new File(primaryDir, subPath);
                    if (target.exists()) {
                        return target;
                    }
                    File emulated = new File("/storage/emulated/0", subPath);
                    if (emulated.exists()) {
                        return emulated;
                    }
                    if (target.getParentFile() != null && target.getParentFile().exists()) {
                        return target;
                    }
                } else if (docId.startsWith("raw:")) {
                    File raw = new File(docId.substring("raw:".length()));
                    if (raw.exists()) {
                        return raw;
                    }
                } else if (docId.contains(":")) {
                    String[] parts = docId.split(":", 2);
                    File storage = new File("/storage", parts[0]);
                    if (storage.exists() && parts.length > 1) {
                        File target2 = new File(storage, parts[1]);
                        if (target2.exists()) {
                            return target2;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        try {
            String path = treeUri.getPath();
            if (path != null) {
                if (path.startsWith("/tree/primary:")) {
                    String sub = path.substring("/tree/primary:".length());
                    if (sub.startsWith("/")) {
                        sub = sub.substring(1);
                    }
                    File target3 = new File(Environment.getExternalStorageDirectory(), sub);
                    if (target3.exists()) {
                        return target3;
                    }
                } else if (path.startsWith("/document/primary:")) {
                    String sub2 = path.substring("/document/primary:".length());
                    if (sub2.startsWith("/")) {
                        sub2 = sub2.substring(1);
                    }
                    File target4 = new File(Environment.getExternalStorageDirectory(), sub2);
                    if (target4.exists()) {
                        return target4;
                    }
                }
            }
        } catch (Exception e2) {
        }
        return null;
    }

    public boolean isDirectlyAccessible(File directFile, DocumentFile rootDoc) {
        if (directFile == null || !directFile.exists() || !directFile.isDirectory() || !directFile.canRead() || !directFile.canWrite()) {
            return false;
        }
        DocumentFile[] safChildren = null;
        if (rootDoc != null) {
            try {
                safChildren = rootDoc.listFiles();
            } catch (Exception e) {
                Log.w(TAG, "Direct write probe failed for " + directFile.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        File[] localChildren = directFile.listFiles();
        if (safChildren != null && safChildren.length > 0 && (localChildren == null || localChildren.length == 0)) {
            Log.w(TAG, "POSIX File sees 0 files while SAF has " + safChildren.length + " items. Bypassing in-place direct access.");
            return false;
        }
        try {
            File probe = new File(directFile, ".repostudio_probe_" + System.currentTimeMillis());
            if (probe.createNewFile()) {
                probe.delete();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public OpenFolderResult openFolder(Uri treeUri) throws IOException {
        if (treeUri == null) {
            throw new IOException("Storage URI cannot be null");
        }
        String realFolderName = extractFolderName(treeUri);
        DocumentFile rootDoc = DocumentFile.fromTreeUri(this.context, treeUri);
        File directFile = resolveDirectFile(treeUri);
        if (isDirectlyAccessible(directFile, rootDoc)) {
            Log.i(TAG, "Successfully opened folder in-place: " + directFile.getAbsolutePath());
            return new OpenFolderResult(directFile, realFolderName, true, 0, treeUri.toString());
        }
        Log.i(TAG, "Loading files from SAF for: " + realFolderName);
        String cleanName = realFolderName.replaceAll("[^a-zA-Z0-9_.-]", "_");
        if (cleanName.trim().isEmpty()) {
            cleanName = "Project";
        }
        File targetDir = new File(this.baseProjectsDir, cleanName);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        int count = importFromSafTree(treeUri, targetDir);
        Log.i(TAG, "Successfully loaded " + count + " files into " + targetDir.getAbsolutePath());
        return new OpenFolderResult(targetDir, realFolderName, false, count, treeUri.toString());
    }

    public int importFromSafTree(Uri treeUri, File targetDir) throws IOException {
        DocumentFile rootDoc = DocumentFile.fromTreeUri(this.context, treeUri);
        if (rootDoc == null || !rootDoc.isDirectory()) {
            throw new IOException("Selected storage location is not accessible as a directory.");
        }
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Could not create local repository directory: " + targetDir.getAbsolutePath());
        }
        return copyDocFolderToLocal(rootDoc, targetDir);
    }

    private int copyDocFolderToLocal(DocumentFile docFolder, File localDir) throws IOException {
        int count = 0;
        DocumentFile[] files = docFolder.listFiles();
        if (files == null) {
            return 0;
        }
        for (DocumentFile doc : files) {
            String name = doc.getName();
            if (name != null && !name.trim().isEmpty() && !EXCLUDED_FROM_SYNC.contains(name)) {
                if (doc.isDirectory()) {
                    File subDir = new File(localDir, name);
                    if (!subDir.exists() && !subDir.mkdirs()) {
                        Log.w(TAG, "Failed to create directory: " + subDir.getAbsolutePath());
                    }
                    count += copyDocFolderToLocal(doc, subDir);
                } else {
                    File localFile = new File(localDir, name);
                    if (copyDocFile(doc, localFile)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private boolean copyDocFile(DocumentFile doc, File localFile) {
        try (InputStream in = this.context.getContentResolver().openInputStream(doc.getUri());
             OutputStream out = new FileOutputStream(localFile)) {
            if (in == null) return false;
            byte[] buf = new byte[16384];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Failed to copy file " + doc.getName() + ": " + e.getMessage());
            return false;
        }
    }

    public int exportToSafTree(File localDir, Uri treeUri) throws IOException {
        DocumentFile rootDoc = DocumentFile.fromTreeUri(this.context, treeUri);
        if (rootDoc == null || !rootDoc.isDirectory()) {
            throw new IOException("Target storage location is not accessible as a directory");
        }
        return syncLocalFolderToSaf(localDir, rootDoc);
    }

    private int syncLocalFolderToSaf(File localDir, DocumentFile targetDoc) {
        int count = 0;
        File[] files = localDir.listFiles();
        if (files == null) {
            return 0;
        }
        for (File file : files) {
            String name = file.getName();
            if (name != null && !EXCLUDED_FROM_SYNC.contains(name)) {
                if (file.isDirectory()) {
                    DocumentFile subDoc = targetDoc.findFile(name);
                    if (subDoc == null || !subDoc.isDirectory()) {
                        subDoc = targetDoc.createDirectory(name);
                    }
                    if (subDoc != null) {
                        count += syncLocalFolderToSaf(file, subDoc);
                    }
                } else if (file.isFile()) {
                    DocumentFile fileDoc = targetDoc.findFile(name);
                    if (fileDoc == null) {
                        fileDoc = targetDoc.createFile("application/octet-stream", name);
                    }
                    if (fileDoc != null) {
                        try {
                            InputStream in = new FileInputStream(file);
                            try {
                                OutputStream out = this.context.getContentResolver().openOutputStream(fileDoc.getUri(), "wt");
                                if (out != null) {
                                    try {
                                        byte[] buf = new byte[16384];
                                        while (true) {
                                            int len = in.read(buf);
                                            if (len <= 0) {
                                                break;
                                            }
                                            out.write(buf, 0, len);
                                        }
                                        out.flush();
                                        count++;
                                    } catch (Throwable th) {
                                        if (out != null) {
                                            try {
                                                out.close();
                                            } catch (Throwable th2) {
                                                th.addSuppressed(th2);
                                            }
                                        }
                                        throw th;
                                    }
                                }
                                if (out != null) {
                                    out.close();
                                }
                                in.close();
                            } catch (Throwable th3) {
                                try {
                                    in.close();
                                } catch (Throwable th4) {
                                    th3.addSuppressed(th4);
                                }
                                throw th3;
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to write file back to SAF: " + name + ": " + e.getMessage());
                        }
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
        }
        return count;
    }

    public String readGitignore(File projectDir) {
        File gitignoreFile = new File(projectDir, ".gitignore");
        if (!gitignoreFile.exists()) {
            return "";
        }
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

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:20:0x003e  */
    public String getGitignorePreset(String presetType) {
        switch (presetType.toLowerCase()) {
            case "android":
                return "# Android Studio & Gradle\n*.iml\n.gradle/\n/local.properties\n/.idea/\n.DS_Store\n/build\n/*/build\n*.hprof\n*.apk\n*.aab\ncaptures/\n";
            case "java":
                return "# Java\n*.class\n*.log\n*.jar\n*.war\n*.nar\n*.ear\n*.zip\n*.tar.gz\n*.rar\nbin/\nbuild/\nout/\ntarget/\n";
            case "node":
                return "# Node.js & Web\nnode_modules/\nnpm-debug.log*\nyarn-debug.log*\nyarn-error.log*\n.pnpm-debug.log*\n.env\n.env.local\ndist/\n.cache/\n";
            case "cpp":
                return "# C / C++\n*.o\n*.obj\n*.dll\n*.so\n*.dylib\n*.exe\n*.out\n*.app\ncmake-build-*/\nCMakeFiles/\n";
            case "general":
            default:
                return "# General & Editor\n.DS_Store\nThumbs.db\n*.swp\n*~\n.vscode/\n.idea/\n*.log\n";
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:29:0x0080  */
    public void generateStarterProject(File projectDir, String templateType, String name) throws IOException {
        String str;
        if (!projectDir.exists()) {
            projectDir.mkdirs();
        }
        if (templateType.equals("android")) {
            str = "android";
        } else {
            str = templateType.equals("node") ? "node" : "java";
        }
        writeGitignore(projectDir, getGitignorePreset(str));
        File readme = new File(projectDir, "README.md");
        String readmeContent = "# " + name + "\n\nManaged and published with **RepoStudio** on Android.\n\n## Overview\nThis repository was created and configured on a mobile device using RepoStudio with full Git and GitHub integration.\n\n### Getting Started\nClone the repository:\n```bash\ngit clone <repository_url>\n```\n";
        FileWriter fw = new FileWriter(readme);
        try {
            fw.write(readmeContent);
            fw.close();
            switch (templateType.toLowerCase()) {
                case "android":
                    File srcMain = new File(projectDir, "src/main/java/com/example");
                    srcMain.mkdirs();
                    File mainActivity = new File(srcMain, "MainActivity.java");
                    FileWriter fw2 = new FileWriter(mainActivity);
                    try {
                        fw2.write("package com.example;\n\npublic class MainActivity {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from RepoStudio Android Project!\");\n    }\n}\n");
                        fw2.close();
                        return;
                    } catch (Throwable th) {
                        try {
                            fw2.close();
                            break;
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                        throw th;
                    }
                case "node":
                    File pkg = new File(projectDir, "package.json");
                    FileWriter fw3 = new FileWriter(pkg);
                    try {
                        fw3.write("{\n  \"name\": \"" + name.toLowerCase() + "\",\n  \"version\": \"1.0.0\",\n  \"description\": \"RepoStudio Web Project\",\n  \"main\": \"index.js\"\n}\n");
                        fw3.close();
                        File indexJs = new File(projectDir, "index.js");
                        FileWriter fw4 = new FileWriter(indexJs);
                        try {
                            fw4.write("// RepoStudio Node.js project\nconsole.log('RepoStudio is running on Android!');\n");
                            fw4.close();
                            return;
                        } catch (Throwable th3) {
                            try {
                                fw4.close();
                                break;
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                            throw th3;
                        }
                    } catch (Throwable th5) {
                        try {
                            fw3.close();
                            break;
                        } catch (Throwable th6) {
                            th5.addSuppressed(th6);
                        }
                        throw th5;
                    }
                case "web":
                    File indexHtml = new File(projectDir, "index.html");
                    FileWriter fw5 = new FileWriter(indexHtml);
                    try {
                        fw5.write("<!DOCTYPE html>\n<html>\n<head>\n  <title>" + name + "</title>\n  <style>body { font-family: sans-serif; background: #0d1117; color: #fff; padding: 2rem; }</style>\n</head>\n<body>\n  <h1>Welcome to " + name + "</h1>\n  <p>Published via RepoStudio Android GitHub Client.</p>\n</body>\n</html>\n");
                        fw5.close();
                        return;
                    } catch (Throwable th7) {
                        try {
                            fw5.close();
                            break;
                        } catch (Throwable th8) {
                            th7.addSuppressed(th8);
                        }
                        throw th7;
                    }
                case "java":
                default:
                    File src = new File(projectDir, "src");
                    src.mkdirs();
                    File app = new File(src, "App.java");
                    FileWriter fw6 = new FileWriter(app);
                    try {
                        fw6.write("public class App {\n    public static void main(String[] args) {\n        System.out.println(\"Welcome to " + name + " - Built with RepoStudio\");\n    }\n}\n");
                        fw6.close();
                        return;
                    } catch (Throwable th9) {
                        try {
                            fw6.close();
                            break;
                        } catch (Throwable th10) {
                            th9.addSuppressed(th10);
                        }
                        throw th9;
                    }
            }
        } catch (Throwable th11) {
            try {
                fw.close();
            } catch (Throwable th12) {
                th11.addSuppressed(th12);
            }
            throw th11;
        }
    }
}
