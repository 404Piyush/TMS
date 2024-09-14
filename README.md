# Disposable Email Service

This project is a Discord bot that provides a temporary email service using the [temp-mail.io](https://temp-mail.io) API. It allows users to get a temporary email address, check for received emails, and view the email content and attachments if present.

## Features

- **Get Temporary Email**: Use the `/get-mail` command to generate a temporary email address.
- **Refresh Inbox**: Use the "🔄️ Refresh Inbox" button to check if any emails have been received.
- **Copy Email**: Use the "©️ Copy Mail" button to copy the email address to the clipboard.
- **Get New Mail**: Use the "🆕 Get New Mail" button to generate a new temporary email address.
- **View Domains**: Use the `/domains` command to retrieve a list of domains used by the temporary email service.

## How It Works

1. **Generate Temporary Email**: When you use the `/get-mail` command, the bot generates a temporary email address from a list of domains.
2. **Check for Emails**: Click the "🔄️ Refresh Inbox" button to check if there are any emails received for the generated address.
3. **View Email Content**: If an email is received, the bot sends the email content and any attachments to the user's direct messages (DMs).
4. **Handle Attachments**: The bot saves and sends any attachments included in the received email.

## Setup and Installation

1. **Clone the Repository**:

    ```sh
    git clone https://github.com/your-username/your-repository.git
    cd your-repository
    ```

2. **Install Dependencies**:

   This project uses the following dependencies:
   - [JDA](https://github.com/DV8FromTheWorld/JDA): Java Discord API
   - [Gson](https://github.com/google/gson): JSON library for Java
   - [OkHttp](https://square.github.io/okhttp/): HTTP client
   - [Apache HttpClient](https://hc.apache.org/httpcomponents-client-5.1.x/): HTTP client for making requests
   - [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/): Base64 encoding/decoding
   - [Apache Commons IO](https://commons.apache.org/proper/commons-io/): IO utilities

   You can add these dependencies to your `pom.xml` if you're using Maven:

    ```xml
    <dependencies>
        <dependency>
            <groupId>net.dv8tion</groupId>
            <artifactId>JDA</artifactId>
            <version>5.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.9</version>
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>5.0.0-alpha.6</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>5.1.3</version>
        </dependency>
        <dependency>
            <groupId>commons-codec</groupId>
            <artifactId>commons-codec</artifactId>
            <version>1.15</version>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.11.0</version>
        </dependency>
    </dependencies>
    ```

3. **Configure the Bot Token**:

   Replace `"ENTER YOUR DISCORD BOT TOKEN"` in `Main.java` with your actual Discord bot token.

4. **Run the Bot**:

    ```sh
    mvn exec:java -Dexec.mainClass="code.Main"
    ```

## Usage

- **/get-mail**: Generate a temporary email address.
- **/domains**: Get a list of domains used by the temporary email service.
- **Buttons**:
  - **🔄️ Refresh Inbox**: Check if there are new emails.
  - **©️ Copy Mail**: Copy the current email address to the clipboard.
  - **🆕 Get New Mail**: Generate a new temporary email address.

