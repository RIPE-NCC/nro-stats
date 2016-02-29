package net.nro.stats.components;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Created by rudi on 29-02-16.
 */
@Component
@Profile("local")
public class FileURIBytesRetriever implements URIBytesRetriever {
    @Override
    public byte[] retrieveBytes(String uri) {

        ByteArrayOutputStream outputStream = null;
        try (FileInputStream inputStream = new FileInputStream(new File(uri)))
        {
            byte[] buffer = new byte[4096];
            outputStream = new ByteArrayOutputStream();
            int read = 0;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
