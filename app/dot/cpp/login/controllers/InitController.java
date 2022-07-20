package dot.cpp.login.controllers;

import dot.cpp.login.models.user.entity.User;
import java.util.List;
import javax.inject.Inject;
import play.mvc.Result;

public class InitController {
  private final dot.cpp.repository.controllers.InitController initRepositoryController;

  @Inject
  public InitController(dot.cpp.repository.controllers.InitController initRepositoryController) {
    this.initRepositoryController = initRepositoryController;
  }

  public Result init() {
    return initRepositoryController.init(List.of(User.class));
  }
}
