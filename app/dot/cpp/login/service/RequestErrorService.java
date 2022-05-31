package dot.cpp.login.service;

import static play.mvc.Results.redirect;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;

@Singleton
public final class RequestErrorService {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final MessagesApi messagesApi;

  @Inject
  public RequestErrorService(MessagesApi messagesApi) {
    this.messagesApi = messagesApi;
  }

  /**
   * Handle generic errors.
   *
   * @param call Call
   * @param request Request
   */
  public Result handleGenericErrors(Call call, Http.Request request) {
    var messages = messagesApi.preferred(request);
    return getResult(call, messages.apply("general.session.expired"));
  }

  /**
   * Handle form errors.
   *
   * @param call Call
   * @param request Request
   * @param webForm Form
   */
  public Result handleFormErrors(Call call, Http.Request request, Form<?> webForm) {
    var messages = messagesApi.preferred(request);
    return getResult(call, getErrorMessage(webForm.errors(), messages));
  }

  /**
   * Handle form errors staying on the same page.
   *
   * @param request Request
   * @param webForm Form
   */
  public Result handleFormErrorWithRefresh(Http.Request request, Form<?> webForm) {
    var messages = messagesApi.preferred(request);
    return redirect(request.uri())
        .flashing("alert-danger", getErrorMessage(webForm.errors(), messages));
  }

  private String getErrorMessage(List<ValidationError> validationErrors, Messages messages) {
    return validationErrors.stream()
        .map(
            validationError -> {
              final var message = messages.apply(validationError.message());
              return validationError.key().isEmpty()
                  ? message
                  : validationError.key() + ": " + message;
            })
        .collect(Collectors.joining("; "));
  }

  /**
   * Handle handleXaftException.
   *
   * @param call Call
   * @param request Request
   * @param exception XaftException
   */
  /*
  public Result handleXaftException(Call call, Http.Request request, XaftException exception) {
    var messages = messagesApi.preferred(request);
    logger.error("ForKidsComponentException", exception);
    return getResult(call, messages.apply(exception.getMessage()));
  }
  */
  private Result getResult(Call call, String message) {
    return redirect(call).flashing("alert-danger", message);
  }
}
