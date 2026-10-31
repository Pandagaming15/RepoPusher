package com.example.core;

import android.util.Log;

import com.example.core.models.GitHubRepo;
import com.example.core.models.GitHubUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GitHubApiClient {
    private static final String TAG = "GitHubApiClient";
    private static final String BASE_URL = "https://api.github.com";
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final KeystoreManager keystoreManager;

    public GitHubApiClient(KeystoreManager keystoreManager) {
        this.keystoreManager = keystoreManager;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private Request.Builder newAuthenticatedRequest(String path) {
        String token = keystoreManager.getGitHubToken();
        Request.Builder builder = new Request.Builder()
                .url(path.startsWith("http") ? path : BASE_URL + path)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "RepoStudio-Android-App");

        if (token != null && !token.trim().isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token.trim());
        }
        return builder;
    }

    public GitHubUser getCurrentUser() throws IOException {
        Request request = newAuthenticatedRequest("/user").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API request failed: HTTP " + response.code() + " " + response.message());
            }
            String body = response.body() != null ? response.body().string() : "{}";
            JSONObject obj = new JSONObject(body);

            GitHubUser user = new GitHubUser();
            user.setLogin(obj.optString("login", ""));
            user.setName(obj.optString("name", user.getLogin()));
            user.setAvatarUrl(obj.optString("avatar_url", ""));
            user.setEmail(obj.optString("email", ""));
            user.setBio(obj.optString("bio", ""));
            user.setPublicRepos(obj.optInt("public_repos", 0));
            user.setFollowers(obj.optInt("followers", 0));
            user.setFollowing(obj.optInt("following", 0));
            user.setHtmlUrl(obj.optString("html_url", "https://github.com/" + user.getLogin()));

            // Update default author in keystore if empty
            if (user.getName() != null && !user.getName().isEmpty()) {
                keystoreManager.setGitAuthorName(user.getName());
            }
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                keystoreManager.setGitAuthorEmail(user.getEmail());
            }

            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching GitHub user: " + e.getMessage());
            throw new IOException(e.getMessage(), e);
        }
    }

    public List<GitHubRepo> getUserRepositories() throws IOException {
        Request request = newAuthenticatedRequest("/user/repos?sort=updated&per_page=100&affiliation=owner,collaborator").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to load repositories: HTTP " + response.code());
            }
            String body = response.body() != null ? response.body().string() : "[]";
            JSONArray arr = new JSONArray(body);
            List<GitHubRepo> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                GitHubRepo repo = new GitHubRepo();
                repo.setId(obj.optLong("id"));
                repo.setName(obj.optString("name", ""));
                repo.setFullName(obj.optString("full_name", repo.getName()));
                repo.setDescription(obj.optString("description", ""));
                repo.setPrivate(obj.optBoolean("private", false));
                repo.setHtmlUrl(obj.optString("html_url", ""));
                repo.setCloneUrl(obj.optString("clone_url", ""));
                repo.setDefaultBranch(obj.optString("default_branch", "main"));
                repo.setStargazersCount(obj.optInt("stargazers_count", 0));
                repo.setForksCount(obj.optInt("forks_count", 0));
                repo.setLanguage(obj.optString("language", ""));
                repo.setUpdatedAt(obj.optString("updated_at", ""));
                list.add(repo);
            }
            return list;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching user repos: " + e.getMessage());
            throw new IOException(e.getMessage(), e);
        }
    }

    public GitHubRepo createRepository(String name, String description, boolean isPrivate, boolean autoInit) throws IOException {
        String token = keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new IOException("GitHub token is not set. Please configure your Personal Access Token in Settings before creating a repository.");
        }

        // Sanitize name: GitHub repo names cannot contain spaces or illegal characters
        String cleanName = name.trim().replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9_.-]", "-");
        if (cleanName.isEmpty()) {
            throw new IOException("Repository name cannot be empty. Please provide a valid repository name.");
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("name", cleanName);
            payload.put("description", description != null ? description : "");
            payload.put("private", isPrivate);
            payload.put("auto_init", autoInit);

            RequestBody body = RequestBody.create(payload.toString(), JSON_MEDIA);
            Request request = newAuthenticatedRequest("/user/repos").post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "{}";
                if (!response.isSuccessful()) {
                    String errorMsg = "HTTP " + response.code();
                    try {
                        JSONObject errObj = new JSONObject(responseBody);
                        if (errObj.has("message")) {
                            errorMsg = errObj.getString("message");
                        }
                        if (errObj.has("errors")) {
                            JSONArray errors = errObj.getJSONArray("errors");
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < errors.length(); i++) {
                                Object item = errors.get(i);
                                if (item instanceof JSONObject) {
                                    JSONObject eObj = (JSONObject) item;
                                    if (eObj.has("message")) {
                                        sb.append(" (").append(eObj.getString("message")).append(")");
                                    }
                                } else {
                                    sb.append(" (").append(item.toString()).append(")");
                                }
                            }
                            errorMsg += sb.toString();
                        }
                    } catch (Exception ignored) {}

                    if (response.code() == 401) {
                        throw new IOException("GitHub Authentication Failed (HTTP 401): Bad credentials. Please ensure your token is valid and active in Settings.");
                    } else if (response.code() == 403) {
                        throw new IOException("GitHub Permission Denied (HTTP 403): " + errorMsg + ". Please verify your token has the 'repo' scope.");
                    } else if (response.code() == 422) {
                        throw new IOException("Repository already exists or name invalid (HTTP 422): " + errorMsg);
                    } else {
                        throw new IOException("Failed to create repository on GitHub: " + errorMsg);
                    }
                }
                JSONObject obj = new JSONObject(responseBody);
                GitHubRepo repo = new GitHubRepo();
                repo.setId(obj.optLong("id"));
                repo.setName(obj.optString("name", cleanName));
                repo.setFullName(obj.optString("full_name", repo.getName()));
                repo.setDescription(obj.optString("description", ""));
                repo.setPrivate(obj.optBoolean("private", isPrivate));
                repo.setHtmlUrl(obj.optString("html_url", ""));
                repo.setCloneUrl(obj.optString("clone_url", ""));
                repo.setDefaultBranch(obj.optString("default_branch", "main"));
                return repo;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating repository: " + e.getMessage());
            throw new IOException(e.getMessage(), e);
        }
    }

    public String createPullRequest(String owner, String repo, String title, String bodyText, String headBranch, String baseBranch) throws IOException {
        try {
            JSONObject payload = new JSONObject();
            payload.put("title", title);
            payload.put("body", bodyText != null ? bodyText : "");
            payload.put("head", headBranch);
            payload.put("base", baseBranch);

            RequestBody body = RequestBody.create(payload.toString(), JSON_MEDIA);
            String path = String.format("/repos/%s/%s/pulls", owner, repo);
            Request request = newAuthenticatedRequest(path).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                String resStr = response.body() != null ? response.body().string() : "{}";
                if (!response.isSuccessful()) {
                    JSONObject err = new JSONObject(resStr);
                    throw new IOException(err.optString("message", "HTTP " + response.code()));
                }
                JSONObject obj = new JSONObject(resStr);
                return obj.optString("html_url", "");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public String createRelease(String owner, String repo, String tagName, String releaseTitle, String notes, boolean isDraft, boolean isPrerelease) throws IOException {
        try {
            JSONObject payload = new JSONObject();
            payload.put("tag_name", tagName);
            payload.put("name", releaseTitle);
            payload.put("body", notes != null ? notes : "");
            payload.put("draft", isDraft);
            payload.put("prerelease", isPrerelease);

            RequestBody body = RequestBody.create(payload.toString(), JSON_MEDIA);
            String path = String.format("/repos/%s/%s/releases", owner, repo);
            Request request = newAuthenticatedRequest(path).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                String resStr = response.body() != null ? response.body().string() : "{}";
                if (!response.isSuccessful()) {
                    JSONObject err = new JSONObject(resStr);
                    throw new IOException(err.optString("message", "HTTP " + response.code()));
                }
                JSONObject obj = new JSONObject(resStr);
                return obj.optString("html_url", "");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public String getReadmeContent(String owner, String repo) throws IOException {
        String path = String.format("/repos/%s/%s/readme", owner, repo);
        Request request = newAuthenticatedRequest(path)
                .addHeader("Accept", "application/vnd.github.v3.raw")
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return "# " + repo + "\n\nNo README.md file found in default branch.";
                }
                throw new IOException("HTTP " + response.code());
            }
            return response.body() != null ? response.body().string() : "";
        }
    }
}
