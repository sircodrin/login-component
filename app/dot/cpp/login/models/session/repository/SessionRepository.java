package dot.cpp.login.models.session.repository;

import dot.cpp.login.models.session.entity.Session;
import dot.cpp.repository.repository.BaseRepository;
import it.unifi.cerm.playmorphia.PlayMorphia;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionRepository extends BaseRepository<Session> {

  @Inject
  public SessionRepository(PlayMorphia morphia) {
    super(morphia);
  }
}
