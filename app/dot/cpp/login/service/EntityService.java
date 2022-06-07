package dot.cpp.login.service;

import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.repository.BaseRepository;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityService<T extends BaseEntity> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final BaseRepository<T> repository;

  @Inject
  public EntityService(BaseRepository<T> repository) {
    this.repository = repository;
  }

  public T findById(String id, Class<T> clazz) {
    return repository.findById(id, clazz);
  }

  public T findByField(String field, String value, Class<T> clazz) {
    return repository.findByField(field, value, clazz);
  }

  public List<T> listByField(String field, String value, Class<T> clazz) {
    return repository.listByField(field, value, clazz);
  }

  public void save(T entity) {
    try {
      repository.save(entity);
    } catch (Exception e) {
      logger.error(e.getMessage());
      // todo remove this or throw exception further, not sure if needed
    }
  }

  public void delete(T entity) {
    try {
      repository.save(entity);
    } catch (Exception e) {
      logger.error(e.getMessage());
      // todo
    }
  }
}
