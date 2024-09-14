package code;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveAttachments {

    public static List<File> getAttachments(String response, String uid){
        String mailID = extractMailId(response);

        int count = getAttachmentCount(response);
        if (count != 0){
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.apilayer.com/temp_mail/atchmnts/id/"+mailID)
                        .addHeader("apikey", "wcl7PVKKMHJgN0YAaY6cWDCroQGpj7tr")
                        .method("GET", null)
                        .build();
                Response responseServer = client.newCall(request).execute();
                String res = responseServer.body().string();
                return saveAttachments(res, uid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static String extractMailId(String res) {
        JSONArray jsonArray = new JSONArray(res);

        if (jsonArray.length() > 0) {
            JSONObject firstMail = jsonArray.getJSONObject(0);
            return firstMail.optString("mail_id", "");
        }
        return null; // Return an empty string if no mail_id is found
    }
    public static int getAttachmentCount(String mainResponse) {


        JSONArray jsonArray = new JSONArray(mainResponse);
            if (jsonArray.length() > 0) {
                JSONObject firstItem = jsonArray.getJSONObject(0);
                return firstItem.optInt("mail_attachments_count", 0);
            }

            return 0;
    }

    public static List<File> saveAttachments(String res, String uid) throws IOException {
        List<File> files = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(res);

        Map<String, byte[]> fileContentMap = extractFileContents(jsonArray);

        for (Map.Entry<String, byte[]> entry : fileContentMap.entrySet()) {
            files.add(createFile(entry.getValue(), entry.getKey(), uid));
        }
        if (!files.isEmpty()){
            return files;
        }
        return null;
    }
    private static File createFile(byte[] content, String fileName, String uid) throws IOException {
        Path path;
        try {

            path = Files.createFile(Path.of("data/"+uid+"/" + fileName));

        }catch (FileAlreadyExistsException ignored){
            path = Path.of("data/"+uid+"/"+ fileName);
        }catch (NoSuchFileException e){
            Files.createDirectories(Path.of("data/" + uid));
            path = Files.createFile(Path.of("data/"+uid+"/" + fileName));
        }
        FileUtils.writeByteArrayToFile(path.toFile(),content);
        return path.toFile();
    }

    public static Map<String, byte[]> extractFileContents(JSONArray jsonArray) {
        Map<String, byte[]> fileContentMap = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject fileObject = jsonArray.getJSONObject(i);
            String filename = fileObject.optString("name");
            String base64Content = fileObject.optString("content");
            // Decoding base64 content
            byte[] decodedBytes = decodeBase64ToFile(base64Content);
            fileContentMap.put(filename, decodedBytes);
        }
        return fileContentMap;
    }
    public static byte[] decodeBase64ToFile(String base64Data) {
        try {
            // Decode the base64 data

            return Base64.decodeBase64(base64Data);        // Write the decoded data to a file
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return null;
    }
}
