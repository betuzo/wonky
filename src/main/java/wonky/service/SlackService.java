package wonky.service;

import io.micronaut.context.annotation.Value;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.yaml.snakeyaml.Yaml;
import wonky.api.Invite;
import wonky.http.SlackClient;
import wonky.json.JacksonUtil;
import wonky.model.Organization;
import wonky.slack.Team;
import wonky.tracing.TraceUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Created by domix on 01/06/18.
 */
@Singleton
@Slf4j
public class SlackService {
  private static final String slack = "slack.com";
  @Value("${wonky.tenants.file:/etc/wonky/tenants.yaml}")
  private String tenantsFile;

  @Value("${wonky.tenants.file.pollinterval:100}")
  private int POLL_INTERVAL = 100;

  private List<SlackOrganization> orgs;

  @Inject
  private JacksonUtil jacksonUtil;

  @Inject
  private TraceUtil traceUtil;

  @Inject
  private SlackClient slackClient;

  @PostConstruct
  public void init() {
    log.info("Tenant file {}", tenantsFile);
    File file = new File(tenantsFile);
    String tenantsFileDirectory = file.getParentFile().getAbsolutePath();
    log.info("Watching changes in [{}]", tenantsFileDirectory);
    log.info("Poll interval [{}]", POLL_INTERVAL);
    FileAlterationObserver observer = new FileAlterationObserver(tenantsFileDirectory);
    FileAlterationMonitor monitor = new FileAlterationMonitor(POLL_INTERVAL);

    load();

    FileAlterationListener listener = new FileAlterationListenerAdaptor() {
      @Override
      public void onFileChange(File file) {
        log.debug("onFileChange");
        log.info("Reloading file [{}]", file.getAbsoluteFile().getName());
        //TODO: verify that the file changed is the configured file
        load();
      }
    };
    observer.addListener(listener);
    monitor.addObserver(observer);
    try {
      monitor.start();
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  public void load() {
    log.info("Loading [{}]", tenantsFile);
    Yaml yaml = new Yaml();

    try {
      File file = new File(tenantsFile);
      InputStream ios = new FileInputStream(file);
      orgs = yaml.load(ios);

      ios.close();
    } catch (FileNotFoundException e) {
      log.error(format("Can't load tenants file. '%s'", tenantsFile), e);
      throw new IllegalStateException(format("Can't load tenants file. '%s'", tenantsFile), e);
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
    }
  }

  public List<SlackOrganization> getOrgs() {
    return orgs;
  }

  //TODO: make this data cacheable
  public Maybe<Organization> get(String hostname) {
    log.warn("Getting {}", hostname);
    return traceUtil.trace(span ->
      findTenant(hostname)
        .map(slackOrganization -> tenantSlackInformation(slackOrganization.getToken())
          .map(team -> {
            Organization organization = new Organization();
            organization.setTeam(team);
            return organization;
          })).orElseThrow(() -> throwSlackOrganizationNotFoundException(hostname)));
  }

  public Optional<SlackOrganization> findTenant(String hostname) {
    return traceUtil.trace(span -> orgs.stream()
      .filter(slackOrganization -> slackOrganization.getWonkyDomain().equals(hostname))
      .findFirst());
  }

  public Maybe<Team> tenantSlackInformation(String token) {
    return slackClient.fetchTeamInfo(token).firstElement();
  }


  public void setTenantsFile(String tenantsFile) {
    this.tenantsFile = tenantsFile;
  }

  public Maybe<String> invite(String hostname, Invite invite) {
    SlackOrganization tenant = findTenant(hostname)
      .orElseThrow(() -> throwSlackOrganizationNotFoundException(hostname));

    return slackClient.invite(tenant, invite.getEmail())
      .firstElement();
  }

  private EntityNotFoundException throwSlackOrganizationNotFoundException(String hostname) {
    return new EntityNotFoundException("Slack Organization", hostname);
  }
}
