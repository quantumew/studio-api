package ratpack.studio.api

import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.Blocking

import static ratpack.rx.RxRatpack.observe
import static ratpack.rx.RxRatpack.observeEach

class UserDbCommands {
    private final Sql sql
    private static final HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("sql-studiodb")

    @Inject
    public UserDbCommands(Sql sql) {
        this.sql = sql
    }

    void createTable () {
        sql.executeInsert('create table user (email varchar(256), passwordHash varchar(256), salt varchar(256), id integer not null auto_increment primary key)')
    }

    rx.Observable<String> insert(String email, String passwordHash) {
        return new HystrixObservableCommand<String>(
            HystrixObservableCommand.Setter.withGroupKey(HystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey('insert'))) {

            @Override
            protected rx.Observable<String> construct() {
                observe(Blocking.get {
                    sql.executeInsert('insert into user (email, passwordHash) value ($email, $passwordHash)')
                })
            }
        }.toObservable()
    }

    rx.Observable<String> insertSalt(String email, String salt) {
        return new HystrixObservableCommand<String>(
            HystrixObservableCommand.Setter.withGroupKey(HystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey('insertSalt'))) {

            @Override
            protected rx.Observable<String> construct() {
                observe(Blocking.get {
                    sql.executeInsert('insert into user (salt) value ($salt)');
                })
            }
        }.toObservable()
    }
}
