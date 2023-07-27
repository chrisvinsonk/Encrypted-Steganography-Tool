# Steganography Encoder/Decoder

## Introduction

Steganography Encoder/Decoder is a web application that allows users to encode and decode hidden messages within images using steganography techniques. Steganography is the art of concealing secret information within seemingly innocent carriers, such as images, audio files, or text.

This project is built using Spring Boot, a popular Java framework, to handle the backend functionalities and image processing. It provides a user-friendly web interface where users can upload images and enter their secret messages for encoding. The encoded images can later be uploaded for decoding to reveal the hidden messages.

## Features

- Encode secret messages into images using steganography techniques.
- Decode hidden messages from encoded images.
- Utilizes AES encryption to ensure the security of the hidden messages.
- Material Design-inspired styling for a modern and user-friendly interface.
- Supports various image formats for encoding and decoding.

## Prerequisites

Before running the Steganography Encoder/Decoder application, ensure you have the following dependencies installed:

- Java Development Kit (JDK) 8 or later
- Maven

## How to Run

1. Clone the repository to your local machine using the following command:

   ```
   git clone https://github.com/chrisvinsonk/Encrypted-Steganography-Tool.git
   ```

2. Navigate to the project directory:

   ```
   cd steganography-encoder-decoder
   ```

3. Build the application using Maven:

   ```
   mvn clean package
   ```

4. Run the Spring Boot application:

   ```
   java -jar target/steganography-encoder-decoder.jar
   ```

5. The application will start, and you can access it at `http://localhost:8080/steganography/index` in your web browser.

## Usage

1. Encoding:
   - Access the web application at `http://localhost:8080/steganography/index`.
   - Upload an image file using the "Upload Image" button.
   - Enter your secret message in the "Enter Message" text area.
   - Click the "Encode" button to encode the message into the image.
   - The encoded image will be automatically downloaded to your device.

2. Decoding:
   - Access the web application at `http://localhost:8080/steganography/index`.
   - Upload the encoded image using the "Upload Encoded Image" button.
   - Click the "Decode" button to reveal the hidden message.
   - The decoded message will be displayed on the web page.

**Note:** For security reasons, make sure to remember the secret key used to encrypt the message during encoding. The same secret key will be required for successful decoding.

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgments

- Special thanks to [OpenAI](https://openai.com) for providing the GPT-3.5 language model used to generate the project documentation.
- Thanks to the Material Design team at Google for the design inspiration for the user interface.
