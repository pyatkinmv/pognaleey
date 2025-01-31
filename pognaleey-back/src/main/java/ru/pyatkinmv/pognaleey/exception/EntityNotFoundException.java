package ru.pyatkinmv.pognaleey.exception;

public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException(Long entityId, Class<?> entityClass) {
    super(String.format("Not found entity %s with id=%d", entityClass.getName(), entityId));
  }
}
