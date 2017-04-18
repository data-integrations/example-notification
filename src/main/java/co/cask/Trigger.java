package co.cask;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Macro;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.cdap.etl.api.PipelineConfigurer;
import co.cask.cdap.etl.api.batch.BatchActionContext;
import co.cask.cdap.etl.api.batch.PostAction;
import co.cask.hydrator.common.batch.action.ConditionConfig;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trigger implementation of {@link PostAction}.
 *
 * <p>
 *   {@link PostAction} are type of plugins that are executed in the
 *   Batch pipeline at the end of execution. Irrespective of the status of
 *   the pipeline this plugin will be invoked.
 *
 *   This type of plugin can be used to send notifications to external
 *   system or notify other workflows.
 * </p>
 */
@Plugin(type = PostAction.PLUGIN_TYPE)
@Name("Trigger")
@Description("Triggers other pipelines based on the condition specified. Currently supports only non-secured mode.")
public final class Trigger extends PostAction {
  private static final Logger LOG = LoggerFactory.getLogger(Trigger.class);
  private final Config config;

  public static class Config extends ConditionConfig {
    @Description("Trigger Definition")
    @Name("trigger")
    @Macro
    private String trigger;
  }

  public Trigger(Config config) {
    this.config = config;
  }

  @Override
  public void configurePipeline(PipelineConfigurer configurer) {
    super.configurePipeline(configurer);
  }

  @Override
  public void run(BatchActionContext context) throws Exception {
    if (!config.shouldRun(context)) {
      return;
    }

    Map<String, String> arguments = new HashMap<>();
    Map<String, String> args = context.getRuntimeArguments();
    if (args.containsKey("trigger.pipeline.cycles.check")) {
      LOG.error("Cycle found in how the pipelines are triggered. Please check trigger definition.");
      return;
    } else {
      arguments.put("trigger.pipeline.cycles.check", "1");
      arguments.putAll(args);
    }

    String[] lines = config.trigger.split("\\n");
    List<String> toTriggerPipelines = new ArrayList<>();
    for (String line : lines) {
      if (line.startsWith("--") || line.startsWith("//")) {
        continue;
      }
      String[] definition = line.split("\\W+");
      if ("trigger".equalsIgnoreCase(definition[0])) {
        toTriggerPipelines.add(definition[1].trim());
      }
    }

    for (String pipeline : toTriggerPipelines) {
      try {
        LOG.info("Triggering pipeline '{}'", pipeline);
        trigger("localhost", 11015, "default", pipeline, arguments);
      } catch (Exception e) {
        LOG.warn("There was error trigger the pipeline '{}'. {}", pipeline, e.getMessage());
      }
    }
  }

  private void trigger(String host, int port, String namespace, String pipelineId,
                       Map<String, String> arguments) throws IOException {
    String url = String.format(
      "http://%s:%d/v3/namespaces/%s/apps/%s/workflows/%s/start",
      host, port, namespace, pipelineId, "DataPipelineWorkflow"
    );

    HttpPost post = new HttpPost(url);
    BasicHttpEntity entity = new BasicHttpEntity();
    String args = new Gson().toJson(arguments);
    InputStream stream = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
    entity.setContent(stream);
    post.setEntity(entity);

    CloseableHttpClient client = HttpClients.createDefault();
    CloseableHttpResponse response = client.execute(post);
    try {
      int code = response.getStatusLine().getStatusCode();
      if (code != 200) {
        LOG.warn("Failed to trigger pipeline '{}'. {}", pipelineId,
                 response.getStatusLine().getReasonPhrase());
      } else {
        LOG.info("Successfully triggered pipeline '{}'", pipelineId);
      }
    } finally {
      response.close();
      stream.close();
    }
  }
}
