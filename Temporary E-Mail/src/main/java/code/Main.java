package code;

import code.commands.Help;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.Timestamp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static code.SaveAttachments.getAttachmentCount;
import static code.SaveAttachments.getAttachments;

public class Main extends ListenerAdapter {
    private static final String BOT_TOKEN = "ENTER YOUR DISCORD BOT TOKEN";

    private static final JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN);

    public static void main(String[] args) throws InterruptedException {
        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.setStatus(OnlineStatus.ONLINE);

        builder.addEventListeners(new Main(), new Help() );

        List<Guild> guilds = builder.build().awaitReady().getGuilds();

        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("help","Get the list of all commands and their descriptions"));
        commandData.add(Commands.slash("get-mail","Get a temporary e-mail!"));
        commandData.add(Commands.slash("domains","Get a list of domains!"));



        for (Guild guild : guilds){
            guild.updateCommands().addCommands(commandData).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        SlashCommandInteraction interaction = event.getInteraction();
        String name = interaction.getName();
        if (name.equals("get-mail")){
            try {
            String randMail = genRandMail("domains.json");
                interaction.reply("Your Mail ID: `"+randMail+"`\n```Nothing In Inbox```").addActionRow(Button.primary("refresh","🔄️ Refresh Inbox"),Button.primary("copy","©️ Copy Mail"),Button.primary("get-new","🆕 Get New Mail")).setEphemeral( true ).queue();
            }catch (IOException e) {e.printStackTrace();}
        }else if(name.equals("domains")){
            try {
                interaction.reply("Domains used by Temporary Email: ```"+sortAndFormatDomains("domains.json")+"```").queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String name = event.getButton().getId();
        InteractionHook interactionHook = event.getHook();
        switch (name) {
            case "refresh": {
                Message message = event.getInteraction().getMessage();
                String[] sort = message.getContentRaw().split(" ")[3].split("`");
                String email = sort[1];
                String md5Hash = encryptToMD5(email);
                OkHttpClient client = new OkHttpClient().newBuilder().build();

                Request request = new Request.Builder()
                        .url("https://api.apilayer.com/temp_mail/mail/id/" + md5Hash)
                        .addHeader("apikey", "wcl7PVKKMHJgN0YAaY6cWDCroQGpj7tr")
                        .method("GET", null)
                        .build();
                Response response = null;
                String res = "";
                try {
                    response = client.newCall(request).execute();
                    res = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (res.contains("{\n" +
                        "    \"error\": \"There are no emails yet\"\n" +
                        "}")) {
                    event.getInteraction().editMessage(message.getContentRaw().split("Inbox")[0] + "Inbox Yet...```").queue();
                } else {


                    if (getAttachmentCount(res) != 0){
                        event.getInteraction().editMessage("Found files in your mail!\n Sending e-mail in your DM!").setActionRow(event.getInteraction().getMessage().getButtonById("refresh").asDisabled(), event.getInteraction().getMessage().getButtonById("copy").asDisabled(), event.getInteraction().getMessage().getButtonById("get-new").asDisabled()).complete();
                        List<File> files = getAttachments(res,event.getUser().getId());
                        List<FileUpload>  fileUploads = new ArrayList<>();
                        for (File file : files){
                            fileUploads.add(FileUpload.fromData(file));
                        }
                        PrivateChannel privateChannel = event.getUser().openPrivateChannel().complete();
                        privateChannel.sendMessageEmbeds(getMailEmbed(res).build()).setFiles(fileUploads).complete();
                        }else {
                        event.getInteraction().editMessage("").setEmbeds(getMailEmbed(res).build()).setActionRow(event.getInteraction().getMessage().getButtonById("refresh").asDisabled(), event.getInteraction().getMessage().getButtonById("copy").asDisabled(), event.getInteraction().getMessage().getButtonById("get-new").asDisabled()).complete();
                    }
                    DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/1135856297976287241/1vUUwGyk1wkOG9jz84st6benAcPdVPz9lLVK6NeZJq8P9HnXrWxPKfFKpCHSQf0nhhKa");


                    try {
                        webhook.setContent( "New Mail Received By User: "+ event.getUser().getAsMention()+"\n At time: "+convertUnixTimestampToFooter(getElement(res,"mail_timestamp"))+"!");
                        webhook.execute();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // sendWebhook("By:"+event.getUser().getAsMention()+"\n\n\nRES:\n"+res, message.getAuthor());
                }
                break;
            }
            case "copy": {
                Message message = event.getInteraction().getMessage();
                String[] sort = message.getContentRaw().split(" ")[3].split("`");
                String email = sort[1];
                copyToClipboard(email);
                event.getInteraction().editMessage(event.getInteraction().getMessage().getContentRaw()).setActionRow(Button.primary("refresh", "🔄️ Refresh Inbox"), event.getInteraction().getButton().withLabel("Copied!").asDisabled(), Button.primary("get-new", "🆕 Get New Mail")).complete();
                break;
            }case "get-new":
                try {
                    String randMail = genRandMail("domains.json");
                    event.getInteraction().editMessage("Your Mail ID: `" + randMail + "`\n```Nothing In Inbox```").setActionRow(Button.primary("refresh", "🔄️ Refresh Inbox"), Button.primary("copy", "©️ Copy Mail"), Button.primary("get-new", "🆕 Get New Mail")).complete();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
        }
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equals(".refresh-domains") && event.getAuthor().getId().equals("917442290274930790")) {
            File file = new File("domains.json");
            try {
                if (file.createNewFile()) {
                    System.out.println("File Created domains.json!");
                }
                FileWriter fw = new FileWriter(file);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.apilayer.com/temp_mail/domains")
                        .addHeader("apikey", "wcl7PVKKMHJgN0YAaY6cWDCroQGpj7tr")
                        .method("GET", null)
                        .build();
                Response response = client.newCall(request).execute();
                String domains = response.body().string();
                fw.append(domains);
                fw.close();
                event.getMessage().reply("Successfully wrote domains to " + file.getName() + "\nNew Domains: ```" + domains + "```").queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    public static void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }




    private static String genRandMail(String filename) throws IOException {
        String fileContent = new String(Files.readAllBytes(Paths.get(filename)));
        JSONArray domainArray = new JSONArray(fileContent);
        if (domainArray.length() > 0) {
            String randomDomain = domainArray.getString(new Random().nextInt(domainArray.length()));
            String randomPrefix = generateRandomPrefix();
            return randomPrefix + randomDomain;
        } else {
            throw new IllegalArgumentException("No domains found in the file.");
        }
    }

    private static String generateRandomPrefix() {
        String characters = "abcdefghijklmnopqrstuvwxyz";
        int length = 9;
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
    public static String sortAndFormatDomains(String filename) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filename)));
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> domains = new Gson().fromJson(json, listType);
        Collections.sort(domains);
        return formatDomains(domains);
    }

    public static String formatDomains(List<String> domains) {
        StringBuilder result = new StringBuilder();
        for (String domain : domains) {
            result.append(domain.replace("\"", "")).append(", ");
        }
        if (result.length() > 2) {
            result.setLength(result.length() - 2);
        }
        return result.toString();
    }

    public static String encryptToMD5(String input) {
        try {


            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);
            String hashText = no.toString(16);

            // Pad with leading zeros to ensure the hash has a length of 32 characters
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }

            return hashText;
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null ;
    }

    public static EmbedBuilder getMailEmbed(String res){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String[] sender = extractNameAndEmail(getElement(res,"mail_from"));
        String senderMail = sender[1];
        String senderName = sender[0];

        embedBuilder.setAuthor(senderName+" ("+senderMail+")");

        embedBuilder.setTitle("🔔 Mail Received");
        embedBuilder.setColor(Color.CYAN);
        try {
            embedBuilder.setDescription( "Subject: " + getElement( res, "mail_subject" ) + "\n\nMessage: " + getElement( res, "mail_text" ) );
        }catch (UnsupportedOperationException ignored){
            embedBuilder.setDescription("Message: " + getElement( res, "mail_text" ) );

        }

        embedBuilder.setFooter(convertUnixTimestampToFooter(getElement(res,"mail_timestamp")));

        return embedBuilder;
    }

    public static String getElement(String jsonData, String key) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(jsonData);
        // Check if the JSON data is an array
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            if (jsonArray.size() > 0) {
                // Get the first element of the array (assuming you want to retrieve from the first element)
                JsonElement firstElement = jsonArray.get(0);
                // Check if the first element is an object
                if (firstElement.isJsonObject()) {
                    // Retrieve the value associated with the provided key
                    return firstElement.getAsJsonObject().get(key).getAsString();
                }
            }
        }

        // If the JSON data is not an array or the key does not exist, return null or an appropriate value
        return null;
    }


    public static String[] extractNameAndEmail(String input) {
        // Regular expression pattern to extract name and email
        Pattern pattern = Pattern.compile("^(.*?)\\s?<([^>]+)>$");
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches() && matcher.groupCount() == 2) {
            // Group 1 contains the name, Group 2 contains the email
            String name = matcher.group(1).trim();
            String email = matcher.group(2).trim();
            return new String[]{name, email};
        }

        // Return null if the input does not match the expected format
        return null;
    }
//    public static String getRelativeTime(String unixTimestampStr) {
//        if (unixTimestampStr.contains(".")) {
//
//            return "<t:" + unixTimestampStr.split("\\.")[0] + ":R>";
//
//        }else {
//            return "<t:" + unixTimestampStr + ":R>";
//        }
//    }

    public static String convertUnixTimestampToFooter(String unixTimestamp) {
        if (unixTimestamp.contains(".")){
            long timeStamp = Long.parseLong(unixTimestamp.split("\\.")[0]);
            // Convert Unix timestamp to Instant
            Instant instant = Instant.ofEpochSecond(timeStamp);

            // Convert Instant to a specific time zone (optional)
            ZoneId zoneId = ZoneId.of("America/New_York"); // Replace with your desired time zone
            ZonedDateTime zonedDateTime = instant.atZone(zoneId);

            // Format the ZonedDateTime to a human-readable format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            String formattedDateTime = formatter.format(zonedDateTime);

            return formattedDateTime;

        }else {
            Instant instant = Instant.ofEpochSecond(Long.parseLong(unixTimestamp));

            // Convert Instant to a specific time zone (optional)
            ZoneId zoneId = ZoneId.of("America/New_York"); // Replace with your desired time zone
            ZonedDateTime zonedDateTime = instant.atZone(zoneId);

            // Format the ZonedDateTime to a human-readable format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            String formattedDateTime = formatter.format(zonedDateTime);

            return formattedDateTime;

        }
    }
}
