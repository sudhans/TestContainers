import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class PostgresTestContainerTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.0")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    private static Connection connection;

    @BeforeAll
    static void setup() throws Exception {
        postgresContainer.start();

        // Connect to the PostgreSQL container
        connection = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword()
        );

        // Create a sample table
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE users (id SERIAL PRIMARY KEY, name VARCHAR(50))");
            stmt.executeUpdate("INSERT INTO users (name) VALUES ('Alice')");
            stmt.executeUpdate("INSERT INTO users (name) VALUES ('Bob')");
        }
    }

    @Test
    void testDatabaseConnection() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM users");
            resultSet.next();
            int userCount = resultSet.getInt(1);
            assertEquals(2, userCount, "User count should be 2");
        }
    }

    @AfterAll
    static void teardown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        postgresContainer.stop();
    }
}
