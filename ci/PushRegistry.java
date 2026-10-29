import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.regex.Pattern;

/** Single-platform OCI publication with small chunks for the local Desktop proxy.
 * Uses only the JDK already present in Jenkins. Never prints credentials/upload URLs.
 * Spec: https://distribution.github.io/distribution/spec/api/#chunked-upload
 */
class PushRegistry {
    static final String REGISTRY = "https://registry-1.docker.io";
    static final String REPOSITORY = "ayyoub1/events-tache";
    static final String MANIFEST_TYPE = "application/vnd.oci.image.manifest.v1+json";
    static final Pattern DIGEST = Pattern.compile("\"digest\"\\s*:\\s*\"(sha256:[a-f0-9]{64})\"");
    static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20))
        .version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NEVER).build();
    static String token;
    static long authenticatedAt;

    static String sha256(byte[] bytes) throws Exception {
        return "sha256:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    static void authenticate() throws Exception {
        String user = System.getenv("DOCKER_USER");
        String password = System.getenv("DOCKER_PASSWORD");
        if (!"ayyoub1".equals(user) || password == null || password.isBlank()) {
            throw new IllegalStateException("Approved Docker Hub credentials are required");
        }
        String scopes = "&scope=" + URLEncoder.encode("repository:" + REPOSITORY + ":pull,push", StandardCharsets.UTF_8)
            + "&scope=" + URLEncoder.encode("repository:library/eclipse-temurin:pull", StandardCharsets.UTF_8);
        var request = HttpRequest.newBuilder(URI.create("https://auth.docker.io/token?service=registry.docker.io" + scopes))
            .timeout(Duration.ofSeconds(30)).header("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))).GET().build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) throw new IllegalStateException("Registry authentication HTTP " + response.statusCode());
        var match = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(new String(response.body(), StandardCharsets.UTF_8));
        if (!match.find()) throw new IllegalStateException("Registry token missing");
        token = match.group(1);
        authenticatedAt = System.nanoTime();
    }

    static HttpResponse<byte[]> request(String method, String location, byte[] body, String type,
                                         String range, Set<Integer> expected) throws Exception {
        URI uri = URI.create(location);
        if (!"https".equals(uri.getScheme()) || !"registry-1.docker.io".equals(uri.getHost())) {
            throw new IllegalStateException("Unexpected registry upload host");
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            if (token == null || System.nanoTime() - authenticatedAt > Duration.ofSeconds(180).toNanos()) authenticate();
            var builder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(120))
                .header("Authorization", "Bearer " + token).header("Accept", MANIFEST_TYPE);
            if (type != null) builder.header("Content-Type", type);
            if (range != null) builder.header("Content-Range", range);
            builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body));
            var response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 401 && attempt == 0) { authenticate(); continue; }
            if (!expected.contains(response.statusCode())) {
                throw new IllegalStateException("Registry " + method + " HTTP " + response.statusCode());
            }
            return response;
        }
        throw new IllegalStateException("Registry authentication retry failed");
    }

    static String location(HttpResponse<byte[]> response) {
        return URI.create(REGISTRY).resolve(response.headers().firstValue("Location").orElseThrow()).toString();
    }

    static void publish(Path directory, String tag) throws Exception {
        if (!tag.matches("tache-[a-f0-9]{12}-[0-9]+")) throw new IllegalArgumentException("Expected unique commit/build tag");
        var index = DIGEST.matcher(Files.readString(directory.resolve("index.json")));
        if (!index.find()) throw new IllegalStateException("OCI manifest descriptor missing");
        String manifestDigest = index.group(1);
        if (index.find()) throw new IllegalStateException("Archive must contain exactly one platform");
        byte[] manifest = Files.readAllBytes(directory.resolve("blobs/sha256/" + manifestDigest.substring(7)));
        if (!sha256(manifest).equals(manifestDigest)) throw new IllegalStateException("Manifest checksum mismatch");
        String tagUrl = REGISTRY + "/v2/" + REPOSITORY + "/manifests/" + tag;
        var existing = request("HEAD", tagUrl, null, null, null, Set.of(200, 404));
        if (existing.statusCode() == 200) {
            if (!existing.headers().firstValue("Docker-Content-Digest").orElse("").equals(manifestDigest)) {
                throw new IllegalStateException("Refusing to overwrite an existing tag with different content");
            }
            System.out.println("Exact manifest already published: " + REPOSITORY + ":" + tag);
            return;
        }
        var blobs = DIGEST.matcher(new String(manifest, StandardCharsets.UTF_8));
        while (blobs.find()) {
            String digest = blobs.group(1);
            String shortDigest = digest.substring(7, 19);
            String base = REGISTRY + "/v2/" + REPOSITORY + "/blobs/";
            if (request("HEAD", base + digest, null, null, null, Set.of(200, 404)).statusCode() == 200) {
                System.out.println(shortDigest + " already present");
                continue;
            }
            Path file = directory.resolve("blobs/sha256/" + digest.substring(7));
            MessageDigest checksum = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(file)) {
                byte[] buffer = new byte[65536];
                int count;
                while ((count = input.read(buffer)) != -1) checksum.update(buffer, 0, count);
            }
            if (!("sha256:" + HexFormat.of().formatHex(checksum.digest())).equals(digest)) {
                throw new IllegalStateException("Blob checksum mismatch");
            }
            var upload = request("POST", base + "uploads/?mount=" + digest + "&from=library/eclipse-temurin",
                null, null, null, Set.of(201, 202));
            if (upload.statusCode() == 201) {
                System.out.println(shortDigest + " mounted from public Temurin base");
                continue;
            }
            String next = location(upload);
            long offset = 0;
            try (InputStream input = Files.newInputStream(file)) {
                byte[] buffer = new byte[262144];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    upload = request("PATCH", next, Arrays.copyOf(buffer, count), "application/octet-stream",
                        offset + "-" + (offset + count - 1), Set.of(202));
                    next = location(upload);
                    offset += count;
                    if (offset % (4 * 1024 * 1024) == 0) System.out.println(shortDigest + " accepted " + offset + "/" + Files.size(file));
                }
            }
            // Preserve every signed Location parameter and append only the digest.
            request("PUT", next + (next.contains("?") ? "&" : "?") + "digest=" + digest,
                new byte[0], "application/octet-stream", null, Set.of(201));
            System.out.println(shortDigest + " registry verified digest");
        }
        request("PUT", tagUrl, manifest, MANIFEST_TYPE, null, Set.of(201));
        var verified = request("GET", tagUrl, null, null, null, Set.of(200));
        if (!sha256(verified.body()).equals(manifestDigest)) throw new IllegalStateException("Remote manifest mismatch");
        System.out.println("Published and verified " + REPOSITORY + ":" + tag + " @ " + manifestDigest);
    }

    public static void main(String[] args) {
        try {
            if (args.length != 3 || !REPOSITORY.equals(args[1])) throw new IllegalArgumentException("Unapproved destination");
            publish(Path.of(args[0]), args[2]);
        } catch (Exception error) {
            // Transport exceptions may contain signed URLs: expose only safe error classes.
            System.err.println(error instanceof IllegalStateException || error instanceof IllegalArgumentException
                ? error.getMessage() : "Registry transport failed: " + error.getClass().getSimpleName());
            System.exit(1);
        }
    }
}
