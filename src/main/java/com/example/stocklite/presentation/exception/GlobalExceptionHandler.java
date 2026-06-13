package com.example.stocklite.presentation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.stocklite.application.dto.ErrorResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.DefaultProfileNotFoundException;
import com.example.stocklite.application.exception.EmailAlreadyInUseException;
import com.example.stocklite.application.exception.InactiveProductMovementNotAllowedException;
import com.example.stocklite.application.exception.InvalidCredentialsException;
import com.example.stocklite.application.exception.ProductAlreadyExistsException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.exception.ProfileNotFoundException;
import com.example.stocklite.application.exception.SelfUserUpdateNotAllowedException;
import com.example.stocklite.application.exception.SelfUserDeletionNotAllowedException;
import com.example.stocklite.application.exception.UserAccessDeniedException;
import com.example.stocklite.application.exception.UserNotFoundException;
import com.example.stocklite.application.exception.UserUpdateConflictException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private static final String MENSAGEM_EMAIL_NAO_PROCESSADO =
			"Nao foi possivel concluir o cadastro com o email informado.";
	private static final String MENSAGEM_ERRO_INTERNO =
			"Ocorreu um erro interno ao processar a solicitacao.";
	private static final String MENSAGEM_ACESSO_NEGADO =
			"Usuario sem permissao para executar esta acao.";
	private static final String MENSAGEM_IDENTIFICADOR_PRODUTO_INVALIDO =
			"Identificador do produto invalido.";

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
		String mensagem = montarMensagemValidacao(exception);
		return criarResposta(HttpStatus.BAD_REQUEST, mensagem);
	}

	@ExceptionHandler(EmailAlreadyInUseException.class)
	public ResponseEntity<ErrorResponse> handleEmailAlreadyInUse(EmailAlreadyInUseException exception) {
		return criarResposta(HttpStatus.CONFLICT, MENSAGEM_EMAIL_NAO_PROCESSADO);
	}

	@ExceptionHandler(UserUpdateConflictException.class)
	public ResponseEntity<ErrorResponse> handleUserUpdateConflict(UserUpdateConflictException exception) {
		return criarResposta(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(ProductAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleProductAlreadyExists(ProductAlreadyExistsException exception) {
		return criarResposta(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(InactiveProductMovementNotAllowedException.class)
	public ResponseEntity<ErrorResponse> handleInactiveProductMovementNotAllowed(
			InactiveProductMovementNotAllowedException exception) {
		return criarResposta(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return criarResposta(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler(UserAccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleUserAccessDenied(UserAccessDeniedException exception) {
		return criarResposta(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(AuthenticatedUserInactiveOrNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticatedUserInactiveOrNotFound(
			AuthenticatedUserInactiveOrNotFoundException exception) {
		return criarResposta(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler({
			AuthorizationDeniedException.class,
			AccessDeniedException.class
	})
	public ResponseEntity<ErrorResponse> handleSpringSecurityAccessDenied(Exception exception) {
		return criarResposta(HttpStatus.FORBIDDEN, MENSAGEM_ACESSO_NEGADO);
	}

	@ExceptionHandler(SelfUserDeletionNotAllowedException.class)
	public ResponseEntity<ErrorResponse> handleSelfUserDeletionNotAllowed(
			SelfUserDeletionNotAllowedException exception) {
		return criarResposta(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(SelfUserUpdateNotAllowedException.class)
	public ResponseEntity<ErrorResponse> handleSelfUserUpdateNotAllowed(
			SelfUserUpdateNotAllowedException exception) {
		return criarResposta(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
		return criarResposta(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException exception) {
		return criarResposta(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(ProfileNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProfileNotFound(ProfileNotFoundException exception) {
		return criarResposta(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException exception) {
		return criarResposta(HttpStatus.BAD_REQUEST, MENSAGEM_IDENTIFICADOR_PRODUTO_INVALIDO);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException exception) {
		return criarResposta(HttpStatus.NOT_FOUND, "Recurso nao encontrado.");
	}

	@ExceptionHandler(DefaultProfileNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleDefaultProfileNotFound(DefaultProfileNotFoundException exception) {
		return criarResposta(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_INTERNO);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
		LOGGER.error("Erro interno nao tratado durante o processamento da requisicao.", exception);
		return criarResposta(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_INTERNO);
	}

	private String montarMensagemValidacao(MethodArgumentNotValidException exception) {
		StringBuilder mensagem = new StringBuilder();

		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			if (!mensagem.isEmpty()) {
				mensagem.append(" ");
			}

			mensagem.append(fieldError.getDefaultMessage());
		}

		return mensagem.toString();
	}

	private ResponseEntity<ErrorResponse> criarResposta(HttpStatus status, String mensagem) {
		ErrorResponse errorResponse = new ErrorResponse(mensagem);
		return ResponseEntity.status(status).body(errorResponse);
	}
}
