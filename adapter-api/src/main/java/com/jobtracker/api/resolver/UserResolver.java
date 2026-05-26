package com.jobtracker.api.resolver;

import java.util.Map;

import com.jobtracker.application.service.AuthenticationUseCaseImpl;
import com.jobtracker.auth.JwtProvider;
import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.out.LoadUserPort;
import com.jobtracker.domain.port.out.SaveUserPort;
import com.jobtracker.domain.vo.UserId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserResolver {
  private final AuthenticationUseCaseImpl authUseCase;
  private final JwtProvider jwtProvider;
  private final LoadUserPort loadUserPort;

  public UserResolver(SaveUserPort saveUserPort, LoadUserPort loadUserPort, JwtProvider jwtProvider) {
    this.authUseCase = new AuthenticationUseCaseImpl(saveUserPort, loadUserPort);
    this.jwtProvider = jwtProvider;
    this.loadUserPort = loadUserPort;
  }

  @MutationMapping
  public Map<String, Object> register(@Argument String username, @Argument String password) {
    var user = authUseCase.register(username, password);
    var token = jwtProvider.generateToken(user.id());
    return Map.of("token", token, "user", user);
  }

  @MutationMapping
  public Map<String, Object> login(@Argument String username, @Argument String password) {
    var user = authUseCase.login(username, password);
    var token = jwtProvider.generateToken(user.id());
    return Map.of("token", token, "user", user);
  }

  @QueryMapping
  public User me(@ContextValue UserId userId) {
    return loadUserPort.findById(userId).orElse(null);
  }
}
