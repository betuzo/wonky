package wonky.service;

import io.micronaut.context.annotation.Value;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.yaml.snakeyaml.Yaml;
import wonky.slack.Team;
import wonky.http.Client;
import wonky.json.JacksonUtil;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.*;
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

/**
 * Created by domix on 01/06/18.
 */
@Singleton
public class SlackService {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SlackService.class);
  @Value("${wonky.tenants.file:/etc/wonky/tenants.yaml}")
  private String tenantsFile;

  @Value("${wonky.tenants.file.pollinterval:100}")
  private int POLL_INTERVAL = 100;

  private List<SlackOrganization> orgs;

  private JacksonUtil jacksonUtil;

  public void setJacksonUtil(JacksonUtil jacksonUtil) {
    this.jacksonUtil = jacksonUtil;
  }

  @PostConstruct
  public void init() {
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

  public void setOrgs(List<SlackOrganization> orgs) {
    this.orgs = orgs;
  }

  public SlackOrganization findTenant(String hostname) {
    return orgs.stream()
      .filter(slackOrganization -> slackOrganization.getWonkyDomain().equals(hostname))
      .findFirst().orElseThrow(() -> new IllegalArgumentException(""));
  }

  public Team tenantSlackInformation(String token, String host) {
    String slack = "slack.com";
    String uri = format("/api/team.info?token=%s", token);

    return Client.secure(slack)
      .createGet(uri)
      .flatMap(resp ->
        resp.getContent()
          .map(bb -> {
            String content = bb.toString(defaultCharset());
            log.info(content);
            return jacksonUtil.readValue(content, "team", Team.class);
          }))
      .toBlocking().firstOrDefault(null);
  }


  public void setTenantsFile(String tenantsFile) {
    this.tenantsFile = tenantsFile;
  }

  /*
  @Cacheable('slackPublicData')
  Map slack(String hostname) {
    findTenant(hostname)
      .map { publicData(it.token, it.teamDomain) }
      .orElse(emptyMap())
  }

  Map invite(String hostname, String email) {
    findTenant(hostname)
      .map { invite(it.token, it.teamDomain, email) }
      .orElse(emptyMap())
  }



  Map invite(String token, String host, String email) {
    Map response = remoteService.post("https://${host}.slack.com/api/users.admin.invite") {
      charset 'UTF-8'
      urlenc token: token, email: email
    }

    response + ofNullable(response.ok).filter { it == true }.map {
      [message: messageSource.get('invite.success')]
    }.orElseGet {
      [error: messageSource.get(response.error)]
    }
  }

  Map publicData(String token, String host) {
    log.info 'Searching public data in Slack for {}', host

    Map map = tenantSlackInformation(token, host)
    publicData(map)
  }

  private Predicate isSlackBot = { Map map ->
    'USLACKBOT' == map.id
  }

  private Predicate isActive = { Map map ->
    'active' == map.presence
  }

  Boolean isActiveUser(Map map) {
    isSlackBot.negate().and(isActive).test(map)
  }

  Map publicData(Map data) {
    int active = data.users.findAll {
      isActiveUser(it)
    }.size()

    int total = data.users.findAll {
      isSlackBot.negate().test(it)
    }.size()

    [
      name : data.team.name,
      logo : data.team.icon.image_132,
      users: [
        active: active,
        total : total
      ]
    ]
  }
   */
}
