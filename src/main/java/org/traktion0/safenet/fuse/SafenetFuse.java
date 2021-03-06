package org.traktion0.safenet.fuse;

import co.paralleluniverse.javafs.*;
import org.glassfish.jersey.client.ClientProperties;
import org.traktion0.safenet.client.beans.App;
import org.traktion0.safenet.client.beans.Auth;
import org.traktion0.safenet.client.commands.ErrorResponseFilter;
import org.traktion0.safenet.client.commands.SafenetFactory;
import org.traktion0.safenet.filesystem.SafenetFileSystemProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 11/10/16.
 */
public class SafenetFuse {

    private static final String LAUNCHER_URL = "http://localhost:8100";

    private static final String APP_NAME = "Safenet FUSE";
    private static final String APP_ID = "safenetfuse";
    private static final String APP_VERSION = "0.0.1";
    private static final String APP_VENDOR = "Paul Green";

    private static Auth auth;
    private static WebTarget webTarget;

    public static void main(String args[]) {

        // args[0] source URL - "safe://localhost/"
        // args[1] mount point - "/home/paul/tmp/safemount/"

        String[] permissions = {"SAFE_DRIVE_ACCESS"};
        auth = new Auth(
                new App(
                        APP_NAME,
                        APP_ID,
                        APP_VERSION,
                        APP_VENDOR
                ),
                permissions
        );

        Client client = ClientBuilder.newClient();
        client.register(ErrorResponseFilter.class);
        client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
        webTarget = client.target(LAUNCHER_URL);

        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetFactory.getInstance(webTarget, auth, "drive");
        env.put("SafenetFactory", safenetFactory);

        Path mountPoint = Paths.get(args[1]);
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(args[0]), env)) {
            JavaFS.mount(fileSystem, mountPoint, false, true);
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            try {
                JavaFS.unmount(mountPoint);
            } catch (Exception e2) {
                System.err.println(e2.getMessage());
            }
        }
    }
}
