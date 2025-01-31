package ru.pyatkinmv.pognaleey.util.converter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

class Base64Converter extends PipedResourceConverter<String> {
  public static final String JPG = "jpg";

  @Override
  void doConvert(String imageBase64, PipedOutputStream outputStream) throws IOException {
    byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

    try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
      BufferedImage image = ImageIO.read(bis);
      ImageIO.write(image, JPG, outputStream);
    }
  }

  @Override
  public String buildResourceName(String baseName) {
    return String.format("%s.%s", baseName, JPG);
  }
}
