package com.example.demo.steganography;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@RestController
@RequestMapping("/steganography")
public class SteganographyController {

    private final SteganographyService steganographyService;

    @Autowired
    public SteganographyController(SteganographyService steganographyService) {
        this.steganographyService = steganographyService;
    }

    @GetMapping("/index")
    public ModelAndView showSteganographyPage(Model model) {
        // You can add any data you want to pass to the template using the model object
        model.addAttribute("message", "Welcome to Steganography Encoder/Decoder!");

        // Return ModelAndView with the template name
        return new ModelAndView("steganography");
    }

    @PostMapping("/encode")
    public ResponseEntity<Resource> encode(@RequestParam("inputImage") MultipartFile inputImage,
                                           @RequestParam("message") String message) throws Exception {
        byte[] encodedImageBytes = steganographyService.encodeMessage(inputImage, message);
        String fileName = "encoded_image.png";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(fileName, fileName);
        headers.setContentType(MediaType.IMAGE_PNG);

        Resource resource = new ByteArrayResource(encodedImageBytes);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @PostMapping("/decode")
    @ResponseBody
    public String decode(@RequestParam("encodedImage") MultipartFile encodedImage) throws Exception {
        String result = steganographyService.decodeMessage(encodedImage);
        return result;
    }
}
