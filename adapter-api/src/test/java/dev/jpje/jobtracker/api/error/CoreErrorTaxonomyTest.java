package dev.jpje.jobtracker.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.exception.DomainException;
import dev.jpje.jobtracker.domain.exception.ErrorCode;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

class CoreErrorTaxonomyTest {

  private static final String CORE_ERROR_PACKAGE = "dev.jpje.jobtracker.domain.exception";
  private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

  private GraphQlExceptionResolver resolver;
  private DataFetchingEnvironment env;

  @BeforeEach
  void setUp() {
    resolver = new GraphQlExceptionResolver();
    env = mock(DataFetchingEnvironment.class);

    final var opDef = mock(OperationDefinition.class);
    final var executionStepInfo = mock(graphql.execution.ExecutionStepInfo.class);

    when(opDef.getName()).thenReturn("testOperation");
    when(env.getOperationDefinition()).thenReturn(opDef);
    when(env.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(env.getExecutionStepInfo().getPath()).thenReturn(ResultPath.rootPath());
    when(env.getField()).thenReturn(mock(Field.class));
    when(env.getField().getSourceLocation()).thenReturn(new SourceLocation(1, 1));
  }

  private static Stream<Arguments> coreErrorTypes() {
    final var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
    return scanner.findCandidateComponents(CORE_ERROR_PACKAGE).stream()
      .map(candidate -> candidate.getBeanClassName())
      .map(CoreErrorTaxonomyTest::loadClass)
      .filter(DomainException.class::isAssignableFrom)
      .map(errorType -> errorType.asSubclass(DomainException.class))
      .map(errorType -> arguments(named(errorType.getSimpleName(), errorType)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("coreErrorTypes")
  void shouldResolveEveryCoreErrorTypeToItsDeclaredCode(final Class<? extends DomainException> errorType)
      throws ReflectiveOperationException {
    final var exception = instantiate(errorType);
    final ErrorCode declared = exception.errorCode();

    assertThat(declared)
      .as("core error type %s declares a taxonomy entry", errorType.getSimpleName())
      .isNotNull();

    final var error = resolver.resolveToSingleError(exception, env);

    assertThat(error.getExtensions().get("errorCode"))
      .as("core error type %s resolves to its declared code, never INTERNAL_ERROR", errorType.getSimpleName())
      .isEqualTo(declared.code())
      .isNotEqualTo(INTERNAL_ERROR);
    assertThat(error.getExtensions().get("classification"))
      .as("core error type %s resolves to its declared classification", errorType.getSimpleName())
      .isEqualTo(declared.classification());
  }

  private static Class<?> loadClass(final String className) {
    try {
      return Class.forName(className);
    } catch (final ClassNotFoundException e) {
      throw new IllegalStateException("core error package class is not loadable: " + className, e);
    }
  }

  private static DomainException instantiate(final Class<? extends DomainException> errorType)
      throws ReflectiveOperationException {
    final Constructor<?> constructor = errorType.getDeclaredConstructors()[0];
    final Object[] constructorArguments = Arrays.stream(constructor.getParameterTypes())
      .map(parameter -> parameter == String.class ? (Object) "message" : (Object) new IllegalStateException("cause"))
      .toArray();
    return (DomainException) constructor.newInstance(constructorArguments);
  }
}
