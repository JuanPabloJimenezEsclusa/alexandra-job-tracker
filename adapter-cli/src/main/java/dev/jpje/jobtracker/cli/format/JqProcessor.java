package dev.jpje.jobtracker.cli.format;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class JqProcessor {

  private final Scope scope;

  public JqProcessor() {
    scope = Scope.newEmptyScope();
    BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_7, scope);
  }

  public String process(final JsonNode input, @Nullable final String expression) {
    if (expression == null || expression.isBlank()) {
      return input.toPrettyString();
    }
    try {
      final var query = JsonQuery.compile(expression, Versions.JQ_1_7);
      final var results = new ArrayList<JsonNode>();
      query.apply(scope, input, results::add);
      if (results.isEmpty()) {
        return "";
      }
      return results.stream()
        .map(JsonNode::toPrettyString)
        .collect(Collectors.joining("\n"));
    } catch (final Exception e) {
      return "JQ error: " + e.getMessage();
    }
  }
}
