package ru.pyatkinmv.pognaleey.util.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Abstract class implementing the {@link ResourceConverter} interface. It uses piped streams to
 * perform the conversion in a separate thread, allowing the conversion process to occur without
 * fully loading the input data into memory.
 */
abstract class PipedResourceConverter<T> implements ResourceConverter<T> {

  /**
   * Converts the given input of type T into an InputStream using piped streams.
   *
   * <p>The conversion is performed in a separate thread to avoid blocking the main thread and to
   * allow for streaming processing.
   *
   * @param input the input data to convert
   * @return an InputStream representing the converted data
   * @throws IOException if an I/O error occurs during conversion
   */
  @Override
  public InputStream convert(T input) throws IOException {
    var pipedOutputStream = new PipedOutputStream();
    var pipedInputStream = new PipedInputStream(pipedOutputStream);

    new Thread(
            () -> {
              try (pipedOutputStream) {
                doConvert(input, pipedOutputStream);
              } catch (IOException e) {
                throw new RuntimeException("Image processing error", e);
              }
            })
        .start();

    return pipedInputStream;
  }

  /**
   * Performs the actual conversion of the input data into the output stream.
   *
   * <p>This method must be implemented by subclasses to define the specific conversion logic.
   *
   * @param input the input data to convert
   * @param outputStream the output stream where the converted data will be written
   * @throws IOException if an I/O error occurs during conversion
   */
  abstract void doConvert(T input, PipedOutputStream outputStream) throws IOException;
}
