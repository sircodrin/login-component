package dot.cpp.login.models.user.repository;

import dot.cpp.login.models.user.entity.User;
import dot.cpp.repository.repository.BaseRepository;
import it.unifi.cerm.playmorphia.PlayMorphia;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository extends BaseRepository<User> {

  @Inject
  public UserRepository(PlayMorphia morphia) {
    super(morphia);
  }
}
