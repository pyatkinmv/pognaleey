package ru.pyatkinmv.pognaleey.util.converter;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for converting a given input of type T into an InputStream. This interface is used to
 * transform the input data into a stream that can be saved or processed further.
 *
 * <p>Implementations of this interface should provide logic for both converting the input and
 * generating a resource name based on the original file name.
 */
public interface ResourceConverter<T> {
  /**
   * Converts the given input of type T into an InputStream.
   *
   * @param t the input data to convert
   * @return an InputStream representing the converted data
   * @throws IOException if an I/O error occurs during conversion
   */
  InputStream convert(T t) throws IOException;

  /**
   * Interface for converting a given input of type T into an InputStream. This interface is used to
   * transform the input data into a stream that can be saved or processed further.
   *
   * <p>Implementations of this interface should provide logic for both converting the input and
   * generating a resource name based on the original file name.
   */
  String buildResourceName(String originalFileName);
}
