package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import jp.gr.java_conf.tmatz.mushroom_safeincloud.db.Database;
import jp.gr.java_conf.tmatz.mushroom_safeincloud.db.DatabaseParser;
import jp.gr.java_conf.tmatz.mushroom_safeincloud.db.DatabaseReader;

public class DatabaseUnitTest {
    @Test
    public void openDatabase() throws Exception {
        try (InputStream inputStream = new BufferedInputStream(
                new FileInputStream("app/src/test/res/raw/safeincloud.db"))) {
            DatabaseReader databaseReader = new DatabaseReader();
            try (InputStream content = databaseReader.read(inputStream, "password")) {
                DatabaseParser parser = new DatabaseParser();
                Database database = parser.parse(content);

                database.dump(System.out, 0);
            }
        }
    }
}
