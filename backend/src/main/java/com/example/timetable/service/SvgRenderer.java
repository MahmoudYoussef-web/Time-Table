package com.example.timetable.service;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SvgRenderer {

    private final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();

    @PostConstruct
    void warmup() {
        try {
            render("assets/university_shield_navy.svg",   400, 400);
            render("assets/university_shield_black.svg",  400, 400);
            render("assets/university_building_navy.svg", 920, 424);
            render("assets/university_building_black.svg",852, 424);
            render("assets/divider_navy.svg",  700, 40);
            render("assets/divider_black.svg", 700, 40);
            render("assets/pin_icon.svg",      44, 44);
            render("assets/coffee_icon.svg",   36, 36);
        } catch (Exception e) {
            // log
        }
    }

    public BufferedImage render(String assetPath, int width, int height) {
        String key = assetPath + "@" + width + "x" + height;
        BufferedImage cached = cache.get(key);
        if (cached != null) return cached;

        try (InputStream in = new ClassPathResource(assetPath).getInputStream()) {
            BufferedImageTranscoder tx = new BufferedImageTranscoder();
            tx.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
            tx.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);
            tx.transcode(new TranscoderInput(in), null);
            BufferedImage img = tx.getBufferedImage();
            cache.put(key, img);
            return img;
        } catch (Exception e) {
            throw new RuntimeException("Failed to render SVG: " + assetPath, e);
        }
    }

    public String renderToBase64(String assetPath, int width, int height) {
        BufferedImage img = render(assetPath, width, height);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + b64;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode SVG to base64: " + assetPath, e);
        }
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput out) {
            this.image = img;
        }

        BufferedImage getBufferedImage() {
            return image;
        }
    }
}
