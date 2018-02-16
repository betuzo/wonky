# wonky
[![Build Status](https://travis-ci.org/domix/wonky.svg)](https://travis-ci.org/domix/wonky)
[![codecov.io](http://codecov.io/github/domix/wonky/coverage.svg?branch=master)](http://codecov.io/github/domix/wonky?branch=master)

Wonky is a port of [slacking](https://github.com/rauchg/slackin/), to the JVM written in [Groovy](http://www.groovy-lang.org) and [SpringBoot](http://projects.spring.io/spring-boot/).

Requirements
-----------

[!["JDK"](https://img.shields.io/badge/JDK-8.0+-F30000.svg?style=flat)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[!["Spring Boot"](https://img.shields.io/badge/Spring%20Boot-1.4.x-green.svg?style=flat)](http://docs.spring.io/spring-boot/docs/1.4.x/reference/htmlsingle/)

## Features

- A landing page you can point users to fill in their emails and receive an invite (`http://slack.yourdomain.com`)

## Build

```bash
./gradlew clean bootRepackage
```

## Run

To run wonky you need a `Slack API token`. Note that the user you use to generate the token must be an admin. You may want to create a dedicated @wonky-inviter user (or similar) for this.

You can find your API token [here](http://api.slack.com/web)

In order to run `wonky` you need to provide the following settings:

- `slack.token`
- `slack.host`

By default wonky runs on port `3030`, as any `Spring Boot` application you can chance the port as you wish.

### Setting configuration values

You have different options for this:

```bash
  java -Dslack.token={your_token} -Dslack.host={your_slack_host} -jar wonky-x.x.x.jar
```


```bash
  export SLACK_TOKEN={your_token_here}
  export SLACK_HOST={your_slack_host_here}
  java -jar build/libs/wonky-x.x.x.jar
  ```


### Communities using Wonky

- [The Data Pub](http://slack.thedata.pub)
- [JavaMexico.org](http://slack.javamexico.org)
- [SpringHispano.org](http://slack.springhispano.org)
- [Groovyando.org](http://slack.groovyando.org)
- [ComunidadDePHP.org](http://slack.comunidaddephp.org/)
- [AgilesMX](https://agilesmx.herokuapp.com/)
- [PeruJUG](http://slack.perujug.org/)
- [Functional Programming Club](http://functionalprogramming.club/)
- [La ruta del desarrollador-Azure](http://chat-azure.larutadeldesarrollador.mx/)
- [La Nube MS](http://slack.lanube.ms)
- [Software Perú](http://slack.peru-software.ovh)
- [JavaHispano.org](http://slack.javahispano.org)

### Development badges

[![Stories in Ready](https://badge.waffle.io/domix/wonky.svg?label=ready&title=Ready)](http://waffle.io/domix/wonky)

[![Throughput Graph](https://graphs.waffle.io/domix/wonky/throughput.svg)](https://waffle.io/domix/wonky/metrics)

![codecov.io](http://codecov.io/github/domix/wonky/branch.svg?branch=master)
