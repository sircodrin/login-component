$(document).ready(function () {
  $('#password, #confirmPassword').on('keyup', function () {
    if ($('#password').val() === $('#confirmPassword').val()) {
      $('#submitButton').removeAttr("disabled");
      $('#message').attr('hidden', 'hidden');
    }
  });
  $('#submitButton').click(function () {
    if ($('#password').val() !== $('#confirmPassword').val()) {
      $('#message').removeAttr('hidden');
      $('#submitButton').attr("disabled", "disabled");
    }
  });
});