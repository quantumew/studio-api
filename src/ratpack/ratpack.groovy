import com.zaxxer.hikari.HikariConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.error.ServerErrorHandler
import ratpack.studio.api.*
import ratpack.form.Form
import ratpack.groovy.sql.SqlModule
import ratpack.handling.RequestLogger
import ratpack.health.HealthCheckHandler
import ratpack.hikari.HikariModule
import ratpack.hystrix.HystrixMetricsEventStreamHandler
import ratpack.hystrix.HystrixModule
import ratpack.pac4j.RatpackPac4j
import ratpack.rx.RxRatpack
import ratpack.server.Service
import ratpack.server.StartEvent
import ratpack.session.SessionModule
import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode
import static ratpack.jackson.Jackson.fromJson
import static ratpack.rx.RxRatpack.observe
import static ratpack.groovy.Groovy.ratpack

final Logger logger = LoggerFactory.getLogger(ratpack.class)

ratpack {
    serverConfig {
        props("application.properties")
        sysProps("eb.")
        env("EB_")
    }
    bindings {
        bind DatabaseHealthCheck
        module HikariModule, { HikariConfig c ->
            c.addDataSourceProperty("URL", "jdbc:h2:mem:dev;INIT=CREATE SCHEMA IF NOT EXISTS DEV")
            c.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource")
        }
        module SqlModule
        module LoginModule
        module new HystrixModule().sse()

        bindInstance Service, new Service() {
            @Override
            void onStart(StartEvent event) throws Exception {
                logger.info "Initializing RX"
                RxRatpack.initialize()
                event.registry.get(LoginService).createTable()
            }
        }
    }

    handlers { LoginService loginService ->
        all RequestLogger.ncsa(logger) // log all requests

        path("api/v1/login") {
            byMethod {
                post {
                    parse(jsonNode())
                        .observe()
                        .flatMap { input ->
                            loginService.login(
                                    input.get("email"),
                                    input.get("password")
                                )
                                .subscribe { tokenMap ->
                                    response.send(tokenMap)
                                };
                        }
                }
            }
        }

        path("api/v1/credentials") {
            byMethod {
                post {
                    logger.info 'here'
                    parse(jsonNode())
                        .observe()
                        .flatMap { input ->
                            getSalt(input.get("email"))
                                .subscribe { loginMap
                                    response.send(loginMap);
                                }
                        }
                }
            }
        }

        path("api/v1/renew") {
            byMethod {
                post {
                    parse(jsonNode())
                        .flatMap { input ->
                            loginService.refresh(
                                    input.get("refreshToken")
                                )
                                .subscribe { tokenMap ->
                                    response.send(tokenMap)
                                }
                        }
                }
            }
        }

        get("hystrix.stream", new HystrixMetricsEventStreamHandler())

        get('docs') {
            redirect('/docs/index.html')
        }
    }
}
