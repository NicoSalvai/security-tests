Para que esta API con login functione correctamente es necesario setear las siguientes variables de entorno

secret.key=<Tu_Clave>; <br>
server.port=8080; <br>
access-token.header=access_token; <br>
access-token.expiration=600000; <br>
refresh-token.header=refresh_token; <br>
refresh-token.expiration=14400000; <br>
claim-name=roles; <br>
spring.application.name=<Nombre de la aplicacion>; <br>
login-path=/api/auth; <br>
refresh-path=/api/auth/refresh;

spring.datasource.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.password=
spring.datasource.platform=
spring.datasource.url=
spring.datasource.username=