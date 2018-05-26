package ratpack.studio.api

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacProvider
import java.security.Key;
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.util.logging.Slf4j
import rx.Observable
import org.apache.commons.lang.RandomStringUtils
import javax.inject.Inject
import static rx.Observable.zip

@Slf4j
class LoginService {
	private final UserDbCommands userDbCommands

	@Inject
	LoginService(UserDbCommands userDbCommands) {
		this.userDbCommands = userDbCommands
	}

	void createTable () {
		this.userDbCommands.createTable()
	}

	Observable<Object> login(String email, String passwordHash) {
		// We need a signing key, so we'll create one just for this example. Usually
		// the key would be read from your application configuration instead.
		Key key = MacProvider.generateKey();

		String compactJws = Jwts.builder()
			.setSubject("Joe")
			.signWith(SignatureAlgorithm.HS512, key)
			.compact()
	}

	Observable<Object> register(String email, String passwordHash) {

	}

	Observable<Object> getSalt(String email) {
		String salt = random(64)
		this.userDbCommands.insertSalt(email, salt)

		return [
			salt: salt,
			iterationCount: 1000
		]
	}
}
